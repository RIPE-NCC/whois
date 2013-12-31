package net.ripe.db.rcdummifier;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.ripe.db.whois.common.dao.jdbc.JdbcStreamingHelper;
import net.ripe.db.whois.common.jdbc.SimpleDataSourceFactory;
import net.ripe.db.whois.common.rpsl.DummifierCurrent;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.PatternLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
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

public class RcDatabaseDummifier {
    private static final Logger LOGGER = LoggerFactory.getLogger(RcDatabaseDummifier.class);

    private static final String ARG_JDBCURL = "jdbc-url";
    private static final String ARG_USER = "user";
    private static final String ARG_PASS = "pass";

    private static TransactionTemplate transactionTemplate;
    private static JdbcTemplate jdbcTemplate;

    private static final DummifierCurrent dummifier = new DummifierCurrent();

    private static final AtomicInteger jobsAdded = new AtomicInteger();
    private static final AtomicInteger jobsDone = new AtomicInteger();

    public static void main(final String[] argv) throws Exception {
        setupLogging();

        final OptionSet options = setupOptionParser().parse(argv);
        final String jdbcUrl = options.valueOf(ARG_JDBCURL).toString();
        final String user = options.valueOf(ARG_USER).toString();
        final String pass = options.valueOf(ARG_PASS).toString();

        final SimpleDataSourceFactory simpleDataSourceFactory = new SimpleDataSourceFactory("com.mysql.jdbc.Driver");
        final DataSource dataSource = simpleDataSourceFactory.createDataSource(jdbcUrl, user, pass);
        jdbcTemplate = new JdbcTemplate(dataSource);

        final DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
        transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        // sadly Executors don't offer a bounded/blocking submit() implementation
        int numThreads = Runtime.getRuntime().availableProcessors();
        final ArrayBlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(numThreads*64);
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
    }

    private static void addWork(final String table, final JdbcTemplate jdbcTemplate, final ExecutorService executorService) {
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

    private static final class DatabaseObjectProcessor implements Runnable {
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
                        final RpslObject dummyObject = dummifier.dummify(3, rpslObject);
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
    }

    private static void setupLogging() {
        LogManager.getRootLogger().setLevel(Level.INFO);
        final ConsoleAppender console = new ConsoleAppender();
        console.setLayout(new PatternLayout("%d [%c|%C{1}] %m%n"));
        console.setThreshold(Level.INFO);
        console.activateOptions();
        LogManager.getRootLogger().addAppender(console);
    }

    private static OptionParser setupOptionParser() {
        final OptionParser parser = new OptionParser();
        parser.accepts(ARG_JDBCURL).withRequiredArg().required();
        parser.accepts(ARG_USER).withRequiredArg().required();
        parser.accepts(ARG_PASS).withRequiredArg().required();
        return parser;
    }
}
