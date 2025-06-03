package net.ripe.db.whois.common;

import net.ripe.db.whois.common.dao.jdbc.JdbcStreamingHelper;
import net.ripe.db.whois.common.jmx.JmxBase;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.DummifierRC;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.PasswordHelper;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@ManagedResource(objectName = JmxBase.OBJECT_NAME_BASE + "Dummifier", description = "Whois data dummifier")
/**
 * in jmxterm, run with:
 *      run dummify
 *
 * in console, run with
 *      java -Xmx1G -cp whois.jar net.ripe.db.whois.common.DatabaseDummifierJmx
 *
 */
public class DatabaseDummifierJmx extends JmxBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseDummifierJmx.class);
    private final TransactionTemplate transactionTemplate;

    private final Environment environment;
    private final JdbcTemplate jdbcTemplate;

    private static final DummifierRC dummifier = new DummifierRC();
    private static final AtomicInteger jobsAdded = new AtomicInteger();
    private static final AtomicInteger jobsDone = new AtomicInteger();

    @Autowired
    public DatabaseDummifierJmx(@Value("${whois.environment}") final String environment,
                                @Qualifier("whoisMasterDataSource") final DataSource writeDataSource) {
        super(LOGGER);

        try {
            this.environment = Environment.valueOf(environment.toUpperCase());
        } catch (final IllegalArgumentException e) {
            throw new IllegalArgumentException(unsupportedEnvironment(environment), e);
        }

        this.jdbcTemplate = new JdbcTemplate(writeDataSource);
        this.transactionTemplate = new TransactionTemplate(new DataSourceTransactionManager(writeDataSource));
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @ManagedOperation(description = "Dummify")
    public String dummify() {
        return invokeOperation("dummy", "Dummification of " + environment, () -> {
            if (Environment.PROD.equals(environment)) {
                throw new IllegalArgumentException("dummifier runs on non-production environments only");
            }

            // sadly Executors don't offer a bounded/blocking submit() implementation
            int numThreads = Runtime.getRuntime().availableProcessors();
            final ArrayBlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(numThreads * 64);
            final ExecutorService executorService = new ThreadPoolExecutor(numThreads, numThreads,
                    0L, TimeUnit.MILLISECONDS, workQueue, new ThreadPoolExecutor.CallerRunsPolicy());

            LOGGER.info("Started {} threads", numThreads);

            addWork("last", executorService);
            addWork("history", executorService);
            cleanUpAuthIndex(executorService);

            executorService.shutdown();

            try {
                while (!executorService.awaitTermination(1, TimeUnit.MINUTES)) {
                    LOGGER.info("ExecutorService {} active {} completed {} tasks",
                            ((ThreadPoolExecutor) executorService).getActiveCount(),
                            ((ThreadPoolExecutor) executorService).getCompletedTaskCount(),
                            ((ThreadPoolExecutor) executorService).getTaskCount());
                }
            } catch (InterruptedException e) {
                LOGGER.error("shutdown", e);
            }

            return "Database dummified";
        });
    }


    private void addWork(final String table,  final ExecutorService executorService) {
        LOGGER.info("Dummifying {}", table);
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(final TransactionStatus status) {
                JdbcStreamingHelper.executeStreaming(jdbcTemplate,
                    "SELECT object_id, sequence_id, object FROM " + table + " WHERE sequence_id > 0",
                    (ResultSetExtractor<DatabaseObjectProcessor>) rs -> {
                        while (rs.next()) {
                            executorService.submit(new DatabaseObjectProcessor(rs.getInt(1), rs.getInt(2), rs.getBytes(3), table));
                            jobsAdded.incrementAndGet();
                        }
                        return null;
                    });
            }
        });
        LOGGER.info("Jobs size:{}", jobsAdded);
    }

    private void cleanUpAuthIndex(final ExecutorService executorService) {
        LOGGER.info("Removing index entries for SSO lines");
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(final TransactionStatus status) {
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            jdbcTemplate.update("DELETE FROM auth WHERE auth LIKE 'SSO %' AND object_type=9");
                            jobsAdded.incrementAndGet();
                        } catch (RuntimeException e) {
                            LOGGER.error("Failed to delete SSO auth lines from auth index table", e);
                        }
                    }
                });
            }
        });
        LOGGER.info("Jobs size:{}", jobsAdded);
    }

    final class DatabaseObjectProcessor implements Runnable {
        final int objectId;
        final int sequenceId;
        final String table;
        final byte[] object;

        private DatabaseObjectProcessor(int objectId, int sequenceId, byte[] object, String table) {
            this.objectId = objectId;
            this.sequenceId = sequenceId;
            this.table = table;
            this.object = object;
        }

        @Override
        public void run() {
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(final TransactionStatus status) {
                    try {
                        final RpslObject rpslObject = RpslObject.parse(object);
                        RpslObject dummyObject = dummifier.dummify(3, rpslObject);

                        if (ObjectType.MNTNER.equals(rpslObject.getType())) {
                            dummyObject = replaceAuthAttributes(dummyObject);
                        }

                        jdbcTemplate.update("UPDATE " + table + " SET object = ? WHERE object_id = ? AND sequence_id = ?", dummyObject.toByteArray(), objectId, sequenceId);
                    } catch (RuntimeException e) {
                        LOGGER.error(String.format("%s: %s,%d failed\n%s", table, objectId, sequenceId, new String(object)), e);
                    }
                    int count = jobsDone.incrementAndGet();
                    if (count % 100000 == 0) {
                        LOGGER.info("Finished jobs: {}", count);
                    }
                }
            });
        }

        static RpslObject replaceAuthAttributes(final RpslObject rpslObject) {
            RpslObjectBuilder builder = new RpslObjectBuilder(rpslObject);

            final Iterator<RpslAttribute> attributes = builder.getAttributes().iterator();
            while (attributes.hasNext()) {
                final RpslAttribute attribute = attributes.next();
                if (AttributeType.AUTH.equals(attribute.getType())) {
                    if (attribute.getCleanValue().startsWith("md5-pw") ||
                            attribute.getCleanValue().startsWith("sso")) {
                        attributes.remove();
                    }
                }
            }

            builder.addAttributeAfter(new RpslAttribute(AttributeType.AUTH, "MD5-PW " + PasswordHelper.hashMd5Password(rpslObject.getKey().toUpperCase())), AttributeType.MNTNER);

            return builder.get();
        }
    }

    private static String unsupportedEnvironment(final String environment) {
        return String.format("Invalid environment: %s, available environments are: %s",
                environment,
                String.join(", ", Arrays.stream(Environment.values())
                        .map(Enum::name)
                        .toArray(String[]::new)));
    }
}
