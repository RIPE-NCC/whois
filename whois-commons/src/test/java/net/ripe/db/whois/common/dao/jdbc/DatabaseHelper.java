package net.ripe.db.whois.common.dao.jdbc;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.dao.RpslObjectUpdateDao;
import net.ripe.db.whois.common.dao.RpslObjectUpdateInfo;
import net.ripe.db.whois.common.domain.BlockEvent;
import net.ripe.db.whois.common.domain.User;
import net.ripe.db.whois.common.jdbc.driver.LoggingDriver;
import net.ripe.db.whois.common.rpsl.AttributeSanitizer;
import net.ripe.db.whois.common.rpsl.ObjectMessages;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectFilter;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.source.SourceAwareDataSource;
import net.ripe.db.whois.common.source.SourceContext;
import org.apache.commons.lang.Validate;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.StatementCallback;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.loadScripts;
import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.truncateTables;

@Component
public class DatabaseHelper {
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static final String LOGGING_HANDLER = "net.ripe.db.whois.common.jdbc.driver.DelegatingLoggingHandler";

    private DataSource mailupdatesDataSource;
    private DataSource dnsCheckDataSource;

    private JdbcTemplate aclTemplate;
    private JdbcTemplate schedulerTemplate;
    private JdbcTemplate mailupdatesTemplate;

    @Autowired Environment environment;
    @Autowired DateTimeProvider dateTimeProvider;
    @Autowired AttributeSanitizer attributeSanitizer;
    @Autowired RpslObjectDao rpslObjectDao;
    @Autowired RpslObjectUpdateDao rpslObjectUpdateDao;
    @Autowired SourceAwareDataSource sourceAwareDataSource;
    @Autowired SourceContext sourceContext;

    @Autowired(required = false)
    @Qualifier("aclDataSource")
    public void setAclTemplate(final DataSource aclDataSource) {
        this.aclTemplate = new JdbcTemplate(aclDataSource);
    }

    @Autowired(required = false)
    @Qualifier("schedulerDataSource")
    public void setSchedulerDataSource(DataSource schedulerDataSource) {
        schedulerTemplate = new JdbcTemplate(schedulerDataSource);
    }

    @Autowired(required = false)
    @Qualifier("mailupdatesDataSource")
    public void setMailupdatesDataSource(DataSource mailupdatesDataSource) {
        this.mailupdatesDataSource = mailupdatesDataSource;
        mailupdatesTemplate = new JdbcTemplate(mailupdatesDataSource);
    }

    @Autowired(required = false)
    @Qualifier("dnscheckDataSource")
    public void setDnsCheckDataSource(DataSource dnsCheckDataSource) {
        this.dnsCheckDataSource = dnsCheckDataSource;
    }

    private static String namePrefix;
    private static String dbName;

    public static synchronized void setupDatabase() {
        if (namePrefix != null) {
            return;
        }

        final JdbcTemplate jdbcTemplate = createDefaultTemplate();
        ensureLocalhost(jdbcTemplate);
        cleanupOldTables(jdbcTemplate);

        dbName = "test_" + System.currentTimeMillis() + "_" + DigestUtils.md5DigestAsHex(UUID.randomUUID().toString().getBytes());

        setupDatabase(jdbcTemplate, "acl.database", dbName, "ACL", "acl_schema.sql");
        setupDatabase(jdbcTemplate, "dnscheck.database", dbName, "DNSCHECK", "dnscheck_schema.sql");
        setupDatabase(jdbcTemplate, "scheduler.database", dbName, "SCHEDULER", "scheduler_schema.sql");
        setupDatabase(jdbcTemplate, "mailupdates.database", dbName, "MAILUPDATES", "mailupdates_schema.sql");
        setupDatabase(jdbcTemplate, "whois.db", dbName, "WHOIS", "whois_schema.sql", "whois_data.sql");

        System.setProperty("whois.source", "TEST");

        // TEST-GRS is an alias for TEST
        resetGrsSources();

        final String masterUrl = String.format("jdbc:log:mysql://localhost/%s_WHOIS;driver=%s;logger=%s", dbName, JDBC_DRIVER, LOGGING_HANDLER);
        System.setProperty("whois.db.master.driver", LoggingDriver.class.getName());
        System.setProperty("whois.db.master.url", masterUrl);

        final String slaveUrl = String.format("jdbc:mysql://localhost/%s_WHOIS", dbName);
        System.setProperty("whois.db.driver", JDBC_DRIVER);
        System.setProperty("whois.db.slave.url", slaveUrl);
        System.setProperty("whois.db.grs.slave.baseurl", slaveUrl);
        System.setProperty("whois.db.grs.master.baseurl", slaveUrl);

        namePrefix = dbName;
    }

    private static void cleanupOldTables(final JdbcTemplate jdbcTemplate) {
        final Pattern dbPattern = Pattern.compile("test_(\\d+)_.*");
        for (final String db : jdbcTemplate.queryForList("SHOW DATABASES LIKE 'test_%'", String.class)) {
            final Matcher dbMatcher = dbPattern.matcher(db);
            if (dbMatcher.matches()) {
                final String creationTimeString = dbMatcher.group(1);

                final LocalDateTime creationTime = new LocalDateTime(Long.parseLong(creationTimeString));
                if (creationTime.isBefore(new LocalDateTime().minusHours(1))) {
                    jdbcTemplate.execute("DROP DATABASE IF EXISTS " + db);
                }
            }
        }
    }

    public static void resetGrsSources() {
        System.setProperty("grs.sources", "TEST-GRS");
    }

    public static void addGrsDatabases(final String... names) {
        for (final String name : names) {
            Validate.isTrue(name.endsWith("-GRS"), name + " must end with -GRS");
            setupDatabase(createDefaultTemplate(), "whois.db." + name, dbName, "WHOIS_" + name.replace('-', '_'), "whois_schema.sql");
        }

        final Joiner joiner = Joiner.on(',');
        final String grsSources = joiner.join(System.getProperty("grs.sources", ""), joiner.join(names));
        System.setProperty("grs.sources", grsSources);
    }

    public static void setupDatabase(final String propertyBase, final String nameBase, final String name, final String... sql) {
        setupDatabase(createDefaultTemplate(), propertyBase, nameBase, name, sql);
    }

    static void setupDatabase(final JdbcTemplate jdbcTemplate, final String propertyBase, final String nameBase, final String name, final String... sql) {
        final String dbName = nameBase + "_" + name;
        jdbcTemplate.execute("CREATE DATABASE " + dbName);

        loadScripts(new JdbcTemplate(createDataSource(dbName)), sql);

        System.setProperty(propertyBase + ".url", "jdbc:mysql://localhost/" + dbName);
        System.setProperty(propertyBase + ".username", "dbint");
        System.setProperty(propertyBase + ".password", "");
    }

    private static JdbcTemplate createDefaultTemplate() {
        return new JdbcTemplate(createDataSource(""));  // database name can be empty for mysql
    }

    private static DataSource createDataSource(final String databaseName) {
        try {
            @SuppressWarnings("unchecked")
            final Class<? extends java.sql.Driver> driverClass = (Class<? extends java.sql.Driver>) Class.forName(JDBC_DRIVER);

            final SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
            dataSource.setDriverClass(driverClass);
            dataSource.setUrl("jdbc:mysql://localhost/" + databaseName);
            dataSource.setUsername("dbint");

            return dataSource;
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    static void ensureLocalhost(final JdbcTemplate jdbcTemplate) {
        final boolean isLocalhostOrRdonly = jdbcTemplate.execute(new ConnectionCallback<Boolean>() {
            @Override
            public Boolean doInConnection(Connection con) throws SQLException, DataAccessException {
                final DatabaseMetaData metaData = con.getMetaData();
                final String url = metaData.getURL();
                final String username = metaData.getUserName();

                return url.contains("localhost") || url.contains("127.0.0.1") || username.startsWith("rdonly");
            }
        });

        Validate.isTrue(isLocalhostOrRdonly, "Must be local connection or user rdonly");
    }

    public void setup() {
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(sourceAwareDataSource);
        truncateTables(jdbcTemplate);
        loadScripts(jdbcTemplate, "whois_data.sql");

        if (aclTemplate != null) {
            truncateTables(aclTemplate);
        }

        if (schedulerTemplate != null) {
            truncateTables(schedulerTemplate);
        }

        if (mailupdatesTemplate != null) {
            truncateTables(mailupdatesTemplate);
        }
    }

    public DataSource getMailupdatesDataSource() {
        return mailupdatesDataSource;
    }

    public DataSource getDnsCheckDataSource() {
        return dnsCheckDataSource;
    }

    public JdbcTemplate getWhoisTemplate() {
        return new JdbcTemplate(sourceAwareDataSource);
    }

    public void setCurrentSource(final Source source) {
        sourceContext.setCurrent(source);
    }

    public void setCurrentSourceToMaster() {
        sourceContext.setCurrentSourceToWhoisMaster();
    }

    public RpslObject addObject(final String rpslString) {
        return addObject(RpslObject.parse(rpslString));
    }

    public RpslObject addObject(final RpslObject rpslObject) {
        final RpslObjectUpdateInfo objectUpdateInfo = rpslObjectUpdateDao.createObject(rpslObject);
        return RpslObject.parse(objectUpdateInfo.getObjectId(), rpslObject.toByteArray());
    }

    // TODO: [AH] this is very similar to loader, should merge (also, claiming of IDs is missing from here)
    public Map<RpslObject, RpslObjectUpdateInfo> addObjects(final Collection<RpslObject> rpslObjects) {
        final Map<RpslObject, RpslObjectUpdateInfo> transformedInfoMap = Maps.newHashMap();
        final Map<RpslObject, RpslObjectUpdateInfo> updateInfoMap = Maps.newHashMap();

        for (final RpslObject rpslObject : rpslObjects) {
            // create object with key attribute(s) only - without reference to other objects
            RpslObject transformedObject = attributeSanitizer.sanitize(rpslObject, new ObjectMessages());
            final RpslObjectUpdateInfo updateInfo = addObjectWithoutReferences(transformedObject, rpslObjectUpdateDao);
            updateInfoMap.put(rpslObject, updateInfo);
            transformedInfoMap.put(transformedObject, updateInfo);
        }

        for (RpslObject transformedObject : transformedInfoMap.keySet()) {
            // update object with all attributes (including references)
            rpslObjectUpdateDao.updateObject(transformedInfoMap.get(transformedObject).getObjectId(), transformedObject);
        }
        return updateInfoMap;
    }

    private RpslObjectUpdateInfo addObjectWithoutReferences(final RpslObject rpslObject, final RpslObjectUpdateDao rpslObjectUpdateDao) {
        final List<RpslAttribute> attributes = RpslObjectFilter.keepKeyAttributesOnly(rpslObject);
        return rpslObjectUpdateDao.createObject(new RpslObject((Integer) null, attributes));
    }

    public RpslObject updateObject(final String rpslString) {
        return updateObject(RpslObject.parse(rpslString));
    }

    public RpslObject updateObject(final RpslObject rpslObject) {
        final RpslObjectInfo objectInfo = rpslObjectDao.findByKey(rpslObject.getType(), rpslObject.getKey().toString());

        rpslObjectUpdateDao.updateObject(objectInfo.getObjectId(), rpslObject);
        return RpslObject.parse(objectInfo.getObjectId(), rpslObject.toByteArray());
    }

    public void updateObjects(final Collection<RpslObject> rpslObjects) {
        for (final RpslObject rpslObject : rpslObjects) {
            this.updateObject(rpslObject);
        }
    }

    public RpslObjectUpdateInfo removeObject(final int id, final String rpslString) {
        return removeObject(RpslObject.parse(id, rpslString.getBytes()));
    }

    public RpslObjectUpdateInfo removeObject(final RpslObject rpslObject) {
        final RpslObjectInfo objectInfo = rpslObjectDao.findByKey(rpslObject.getType(), rpslObject.getKey().toString());
        return rpslObjectUpdateDao.deleteObject(objectInfo.getObjectId(), objectInfo.getKey());
    }

    public RpslObject lookupObject(final ObjectType type, final String pkey) {
        return rpslObjectDao.getByKey(type, pkey);
    }

    public void unban(final String prefix) throws InterruptedException {
        aclTemplate.update("INSERT INTO acl_event (prefix, event_time, daily_limit, event_type) VALUES (?, ?, ?, ?)",
                prefix,
                new LocalDateTime().toDate(),
                0,
                BlockEvent.Type.UNBLOCK.name());

        aclTemplate.update("DELETE FROM acl_denied WHERE prefix = ?", prefix);
    }

    public void insertAclIpDenied(final String prefix) {
        aclTemplate.update(
                "INSERT INTO acl_denied (prefix, comment, denied_date) VALUES (?, ?, ?)",
                prefix, "comment", new Date());
    }

    public void clearAclLimits() {
        aclTemplate.update("DELETE FROM acl_limit");
    }

    public void insertAclIpLimit(final String prefix, final int limit, final boolean unlimitedConnections) {
        aclTemplate.update(
                "INSERT INTO acl_limit (prefix, daily_limit, unlimited_connections, comment) VALUES (?, ?, ?, ?)",
                prefix, limit, unlimitedConnections, "comment");
    }

    public void insertAclIpProxy(final String prefix) {
        aclTemplate.update(
                "INSERT INTO acl_proxy (prefix, comment) VALUES (?, ?)",
                prefix, "comment");
    }

    public void insertAclMirror(final String prefix) {
        aclTemplate.update(
                "INSERT INTO acl_mirror (prefix, comment) VALUES (?, ?)",
                prefix, "comment");
    }

    public List<Map<String, Object>> listAclEvents() {
        return aclTemplate.queryForList(
                "SELECT * FROM acl_event"
        );
    }

    public void insertUser(final User user) {
        aclTemplate.update(
                "INSERT INTO override_users (username, password, objecttypes, last_changed) VALUES (?, ?, ?, ?)",
                user.getUsername(),
                user.getHashedPassword(),
                Joiner.on(',').join(user.getObjectTypes()),
                new Date());
    }

    public static void dumpSchema(final DataSource datasource) throws SQLException {

        new JdbcTemplate(datasource).execute(new StatementCallback<Object>() {
            @Override
            public Object doInStatement(Statement statement) throws SQLException, DataAccessException {
                final ResultSet resultSet = statement.executeQuery("SHOW TABLES");
                final List<String> tables = Lists.newArrayList();

                while (resultSet.next()) {
                    tables.add(resultSet.getString(1));
                }

                resultSet.close();

                for (final String table : tables) {
                    final ResultSet tableResultSet = statement.executeQuery("SELECT * FROM " + table);
                    while (tableResultSet.next()) {
                        ResultSetMetaData metadata = tableResultSet.getMetaData();
                        if (tableResultSet.isFirst()) {
                            System.out.println("\nTABLE: " + table.toUpperCase());
                            for (int column = 1; column <= metadata.getColumnCount(); column++) {
                                System.out.print(metadata.getColumnName(column) + " | ");
                            }
                            System.out.println();
                        }

                        for (int column = 1; column <= metadata.getColumnCount(); column++) {
                            System.out.print(tableResultSet.getString(column) + " | ");
                        }

                        System.out.println();
                    }
                }

                return null;
            }
        });
    }
}
