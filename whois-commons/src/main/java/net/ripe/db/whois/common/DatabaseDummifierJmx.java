package net.ripe.db.whois.common;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.ripe.db.whois.common.dao.jdbc.JdbcStreamingHelper;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.jdbc.SimpleDataSourceFactory;
import net.ripe.db.whois.common.jmx.JmxBase;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.DummifierCurrent;
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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@ManagedResource(objectName = JmxBase.OBJECT_NAME_BASE + "Dummifier", description = "Whois data dummifier")
/**
 * in jmxterm, run with:
 *      run dummify jdbc:mysql://<host>/<db> <user> <pass>
 *
 * in console, run with
 *      java -cp whois.jar net.ripe.db.whois.common.DatabaseDummifierJmx --jdbcUrl jdbc:mysql://localhost/BLAH --user XXX --pass XXX
 *
 */
public class DatabaseDummifierJmx extends JmxBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseDummifierJmx.class);

    private static final String ARG_JDBCURL = "jdbc-url";
    private static final String ARG_USER = "user";
    private static final String ARG_PASS = "pass";

    private static TransactionTemplate transactionTemplate;
    private static JdbcTemplate jdbcTemplate;

    private static final DummifierCurrent dummifier = new DummifierCurrent();

    private static final AtomicInteger jobsAdded = new AtomicInteger();
    private static final AtomicInteger jobsDone = new AtomicInteger();

    public DatabaseDummifierJmx() {
        super(LOGGER);
    }

    @ManagedOperation(description = "Dummify")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "jdbcUrl", description = "jdbc url"),
            @ManagedOperationParameter(name = "user", description = "jdbc username"),
            @ManagedOperationParameter(name = "pass", description = "jdbc password")
    })
    public String dummify(final String jdbcUrl, final String user, final String pass) {
        validateJdbcUrl(user, pass);
        final SimpleDataSourceFactory simpleDataSourceFactory = new SimpleDataSourceFactory("com.mysql.jdbc.Driver");
        final DataSource dataSource = simpleDataSourceFactory.createDataSource(jdbcUrl, user, pass);
        jdbcTemplate = new JdbcTemplate(dataSource);

        final DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
        transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        // sadly Executors don't offer a bounded/blocking submit() implementation
        int numThreads = Runtime.getRuntime().availableProcessors();
        final ArrayBlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(numThreads * 64);
        final ExecutorService executorService = new ThreadPoolExecutor(numThreads, numThreads,
                0L, TimeUnit.MILLISECONDS, workQueue, new ThreadPoolExecutor.CallerRunsPolicy());

        LOGGER.info("Started " + numThreads + " threads");

        addWork("last", jdbcTemplate, executorService);
        addWork("history", jdbcTemplate, executorService);

        executorService.shutdown();
        try {
            executorService.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            LOGGER.error("shutdown", e);
        }
        return "Database dummified";
    }

    private void validateJdbcUrl(final String user, final String password) {
        if (!user.equals(password)) {
            throw new IllegalArgumentException("dummifier runs on non-production environments only (user==password)");
        }
    }

    private void addWork(final String table, final JdbcTemplate jdbcTemplate, final ExecutorService executorService) {
        LOGGER.info("Dummifying " + table);
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
        LOGGER.info("Jobs size:" + jobsAdded);
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

                        if (ObjectType.MNTNER.equals(rpslObject.getType()) && hasPassword(rpslObject)) {
                            dummyObject = replaceWithMntnerNamePassword(dummyObject);
                        }

                        jdbcTemplate.update("UPDATE " + table + " SET object = ? WHERE object_id = ? AND sequence_id = ?", dummyObject.toByteArray(), objectId, sequenceId);
                    } catch (RuntimeException e) {
                        LOGGER.error(table + ": " + objectId + "," + sequenceId + " failed\n" + new String(object), e);
                    }
                    int count = jobsDone.incrementAndGet();
                    if (count % 100000 == 0) {
                        LOGGER.info("Finished jobs: " + count);
                    }
                    return null;
                }
            });
        }

        static RpslObject replaceWithMntnerNamePassword(RpslObject rpslObject) {
            boolean foundPassword = false;
            RpslObjectBuilder builder = new RpslObjectBuilder(rpslObject);
            for (int i = 0; i < builder.size(); i++) {
                RpslAttribute attribute = builder.get(i);
                if (AttributeType.AUTH.equals(attribute.getType()) && (attribute.getCleanValue().startsWith("md5-pw"))) {
                    if (foundPassword) {
                        builder.remove(i);
                    } else {
                        builder.set(i, new RpslAttribute(AttributeType.AUTH, "MD5-PW " + PasswordHelper.hashMd5Password(rpslObject.getKey().toString())));
                        foundPassword = true;
                    }
                }
            }

            if (!foundPassword) {
                throw new IllegalStateException("No 'auth: md5-pw' found after dummifying " + rpslObject.getFormattedKey());
            }

            return builder.get();
        }

        static boolean hasPassword(RpslObject rpslObject) {
            for (CIString auth : rpslObject.getValuesForAttribute(AttributeType.AUTH)) {
                if (auth.startsWith("md5-pw")) {
                    return true;
                }
            }
            return false;
        }
    }

    private static OptionParser setupOptionParser() {
        final OptionParser parser = new OptionParser();
        parser.accepts(ARG_JDBCURL).withRequiredArg().required();
        parser.accepts(ARG_USER).withRequiredArg().required();
        parser.accepts(ARG_PASS).withRequiredArg().required();
        return parser;
    }

    public static void main(String[] argv) {
        final OptionSet options = setupOptionParser().parse(argv);
        String jdbcUrl = options.valueOf(ARG_JDBCURL).toString();
        String user = options.valueOf(ARG_USER).toString();
        String pass = options.valueOf(ARG_PASS).toString();
        new DatabaseDummifierJmx().dummify(jdbcUrl, user, pass);
    }
}
