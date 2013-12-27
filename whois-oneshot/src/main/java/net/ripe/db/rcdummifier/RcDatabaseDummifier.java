package net.ripe.db.rcdummifier;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.ripe.db.whois.common.dao.jdbc.JdbcStreamingHelper;
import net.ripe.db.whois.common.dao.jdbc.domain.RpslObjectRowMapper;
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

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class RcDatabaseDummifier {
    private static final Logger LOGGER = LoggerFactory.getLogger("AutNumCleanup");

    private static final String ARG_JDBCURL = "jdbc-url";
    private static final String ARG_USER = "user";
    private static final String ARG_PASS = "pass";

    private static final SimpleDataSourceFactory simpleDataSourceFactory = new SimpleDataSourceFactory("com.mysql.jdbc.Driver");
    private static JdbcTemplate jdbcTemplate;

    private static DummifierCurrent dummifier = new DummifierCurrent();

    public static void main(final String[] argv) throws Exception {
        setupLogging();

        final OptionSet options = setupOptionParser().parse(argv);
        final String jdbcUrl = options.valueOf(ARG_JDBCURL).toString();
        final String user = options.valueOf(ARG_USER).toString();
        final String pass = options.valueOf(ARG_PASS).toString();

        final DataSource dataSource = simpleDataSourceFactory.createDataSource(jdbcUrl, user, pass);
        jdbcTemplate = new JdbcTemplate(dataSource);

        // default threadpool is backed by unlimited linkedlist, just the right one for us
        final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

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
        JdbcStreamingHelper.executeStreaming(jdbcTemplate, "SELECT object_id, sequence_id FROM "+table+" WHERE sequence_id > 0", new ResultSetExtractor<DatabaseObjectProcessor>() {
            @Override
            public DatabaseObjectProcessor extractData(final ResultSet rs) throws SQLException, DataAccessException {
                executorService.submit(new DatabaseObjectProcessor(rs.getInt(1), rs.getInt(2), table));
                return null;
            }
        });
    }

    private static class DatabaseObjectProcessor implements Runnable {
        final int objectId;
        final int sequenceId;
        final String table;

        private DatabaseObjectProcessor(final int objectId, final int sequenceId, final String table) {
            this.objectId = objectId;
            this.sequenceId = sequenceId;
            this.table = table;
        }

        @Override
        public void run() {
            final RpslObject rpslObject = jdbcTemplate.queryForObject("SELECT object_id, object FROM "+table+" WHERE object_id = ? AND sequence_id = ?",
                    new RpslObjectRowMapper(), objectId, sequenceId);
            final RpslObject dummyObject = dummifier.dummify(3, rpslObject);
            jdbcTemplate.update("UPDATE "+table+" SET object = ? WHERE object_id = ? AND sequence_id = ?", dummyObject);
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
