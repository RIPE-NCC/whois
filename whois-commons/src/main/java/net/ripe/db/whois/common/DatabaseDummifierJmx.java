package net.ripe.db.whois.common;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.ripe.db.whois.common.dao.jdbc.JdbcStreamingHelper;
import net.ripe.db.whois.common.jdbc.SimpleDataSourceFactory;
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
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@ManagedResource(objectName = JmxBase.OBJECT_NAME_BASE + "Dummifier", description = "Whois data dummifier")
/**
 * in jmxterm, run with:
 *      run dummify jdbc:mariadb://<host>/<db> <user> <pass> <env>
 *
 * in console, run with
 *      java -Xmx1G -cp whois.jar net.ripe.db.whois.common.DatabaseDummifierJmx --jdbc-url jdbc:mariadb://localhost/BLAH --user XXX --pass XXX --env XXX
 *
 */
public class DatabaseDummifierJmx extends JmxBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseDummifierJmx.class);

    private static final String ARG_JDBCURL = "jdbc-url";
    private static final String ARG_USER = "user";
    private static final String ARG_PASS = "pass";

    private static final String ARG_ENV = "env";

    private static TransactionTemplate transactionTemplate;
    private static JdbcTemplate jdbcTemplate;

    private static final DummifierRC dummifier = new DummifierRC();

    private static final AtomicInteger jobsAdded = new AtomicInteger();
    private static final AtomicInteger jobsDone = new AtomicInteger();

    public DatabaseDummifierJmx() {
        super(LOGGER);
    }

    @ManagedOperation(description = "Dummify")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "jdbcUrl", description = "jdbc url"),
            @ManagedOperationParameter(name = "user", description = "jdbc username"),
            @ManagedOperationParameter(name = "pass", description = "jdbc password"),
            @ManagedOperationParameter(name= "env", description = "current environment")
    })
    public String dummify(final String jdbcUrl, final String user, final String pass, final EnvironmentEnum env) {
        return invokeOperation("dummify", jdbcUrl, new Callable<String>() {
            @Override
            public String call() {
                final SimpleDataSourceFactory simpleDataSourceFactory = new SimpleDataSourceFactory("org.mariadb.jdbc.Driver");
                final DataSource dataSource = simpleDataSourceFactory.createDataSource(jdbcUrl, user, pass);
                jdbcTemplate = new JdbcTemplate(dataSource);
                validateEnvironment(env);

                final DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
                transactionTemplate = new TransactionTemplate(transactionManager);
                transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

                // sadly Executors don't offer a bounded/blocking submit() implementation
                int numThreads = Runtime.getRuntime().availableProcessors();
                final ArrayBlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(numThreads * 64);
                final ExecutorService executorService = new ThreadPoolExecutor(numThreads, numThreads,
                        0L, TimeUnit.MILLISECONDS, workQueue, new ThreadPoolExecutor.CallerRunsPolicy());

                LOGGER.info("Started {} threads", numThreads);

                addWork("last", jdbcTemplate, executorService);
                addWork("history", jdbcTemplate, executorService);
                cleanUpAuthIndex(jdbcTemplate, executorService);

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
            }
        });
    }

    private void validateEnvironment(final EnvironmentEnum env) {
        final String dbEnvironment = jdbcTemplate.queryForObject("SELECT name FROM environment LIMIT 1", String.class);
        if (dbEnvironment == null){
            throw new IllegalStateException("Environment not specified in the schema");
        }
        if (!env.name().equalsIgnoreCase(dbEnvironment)){
            throw new IllegalArgumentException("Requested environment and database environment doesn't match");
        }
        if (EnvironmentEnum.PROD.equals(env)) {
            throw new IllegalArgumentException("dummifier runs on non-production environments only");
        }
    }

    private void addWork(final String table, final JdbcTemplate jdbcTemplate, final ExecutorService executorService) {
        LOGGER.info("Dummifying {}", table);
        transactionTemplate.execute(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus status) {
                JdbcStreamingHelper.executeStreaming(jdbcTemplate, "SELECT object_id, sequence_id, object FROM " + table + " WHERE sequence_id > 0", new ResultSetExtractor<DatabaseObjectProcessor>() {
                    @Override
                    public DatabaseObjectProcessor extractData(final ResultSet rs) throws SQLException, DataAccessException {
                        while (rs.next()) {
                            executorService.submit(new DatabaseObjectProcessor(rs.getInt(1), rs.getInt(2), rs.getBytes(3), table));
                            jobsAdded.incrementAndGet();
                        }
                        return null;
                    }
                });
                return null;
            }
        });
        LOGGER.info("Jobs size:{}", jobsAdded);
    }

    private void cleanUpAuthIndex(final JdbcTemplate jdbcTemplate, final ExecutorService executorService) {
        LOGGER.info("Removing index entries for SSO lines");
        transactionTemplate.execute(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus status) {
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
                return null;
            }
        });
        LOGGER.info("Jobs size:{}", jobsAdded);
    }

    static final class DatabaseObjectProcessor implements Runnable {
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
            transactionTemplate.execute(new TransactionCallback<Object>() {
                @Override
                public Object doInTransaction(TransactionStatus status) {
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
                    return null;
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

    private static OptionParser setupOptionParser() {
        final OptionParser parser = new OptionParser();
        parser.accepts(ARG_JDBCURL).withRequiredArg().required();
        parser.accepts(ARG_USER).withRequiredArg().required();
        parser.accepts(ARG_PASS).withRequiredArg().required();
        parser.accepts(ARG_ENV).withRequiredArg().required();
        return parser;
    }

    public static void main(String[] argv) {
        final OptionSet options = setupOptionParser().parse(argv);
        String jdbcUrl = options.valueOf(ARG_JDBCURL).toString();
        String user = options.valueOf(ARG_USER).toString();
        String pass = options.valueOf(ARG_PASS).toString();
        EnvironmentEnum env;
        try {
            env = EnvironmentEnum.valueOf(options.valueOf(ARG_ENV).toString().toUpperCase());
        } catch (IllegalArgumentException ex){
            throw new IllegalArgumentException("Env property doesn't match. Available env: DEV, PREPDEV, TRAINING, " +
                    "TEST, RC, " +
                    "PROD");
        }
        new DatabaseDummifierJmx().dummify(jdbcUrl, user, pass, env);
    }
}
