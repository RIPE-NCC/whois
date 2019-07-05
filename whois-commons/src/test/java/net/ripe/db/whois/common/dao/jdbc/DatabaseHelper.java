package net.ripe.db.whois.common.dao.jdbc;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.dao.RpslObjectUpdateDao;
import net.ripe.db.whois.common.dao.RpslObjectUpdateInfo;
import net.ripe.db.whois.common.domain.BlockEvent;
import net.ripe.db.whois.common.domain.User;
import net.ripe.db.whois.common.grs.AuthoritativeResourceData;
import net.ripe.db.whois.common.jdbc.driver.LoggingDriver;
import net.ripe.db.whois.common.rpsl.AttributeSanitizer;
import net.ripe.db.whois.common.rpsl.ObjectMessages;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.common.source.IllegalSourceException;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.source.SourceAwareDataSource;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.common.sso.AuthTranslator;
import net.ripe.db.whois.common.sso.CrowdClient;
import net.ripe.db.whois.common.sso.CrowdClientException;
import net.ripe.db.whois.common.sso.SsoHelper;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.StatementCallback;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringValueResolver;

import javax.annotation.CheckForNull;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.loadScripts;
import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.truncateTables;
import static net.ripe.db.whois.common.rpsl.RpslObjectFilter.keepKeyAttributesOnly;

@Component
public class DatabaseHelper implements EmbeddedValueResolverAware {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseHelper.class);

    private static final String JDBC_DRIVER = "org.mariadb.jdbc.Driver";

    private DataSource mailupdatesDataSource;

    private JdbcTemplate aclTemplate;
    private JdbcTemplate mailupdatesTemplate;
    private JdbcTemplate internalsTemplate;
    private SourceAwareDataSource sourceAwareDataSource;

    @Autowired ApplicationContext applicationContext;
    @Autowired AttributeSanitizer attributeSanitizer;
    @Autowired SourceContext sourceContext;
    @Autowired AuthoritativeResourceData authoritativeResourceData;


    RpslObjectDao rpslObjectDao;
    RpslObjectUpdateDao rpslObjectUpdateDao;
    CrowdClient crowdClient;
    private StringValueResolver valueResolver;

    @Autowired(required = false)
    @Qualifier("aclDataSource")
    public void setAclDataSource(final DataSource aclDataSource) {
        this.aclTemplate = new JdbcTemplate(aclDataSource);
    }

    public JdbcTemplate getAclTemplate() {
        return aclTemplate;
    }

    @Autowired(required = false)
    @Qualifier("mailupdatesDataSource")
    public void setMailupdatesDataSource(DataSource mailupdatesDataSource) {
        this.mailupdatesDataSource = mailupdatesDataSource;
        mailupdatesTemplate = new JdbcTemplate(mailupdatesDataSource);
    }

    @Autowired(required = false)
    @Qualifier("internalsDataSource")
    public void setInternalsDataSource(DataSource internalsDataSource) {
        internalsTemplate = new JdbcTemplate(internalsDataSource);
    }

    // TODO: [AH] autowire these fields once whois-internals has proper wiring set up
    @Autowired
    public void setCrowdClient(CrowdClient crowdClient) {
        this.crowdClient = crowdClient;
    }

    @Autowired
    @Qualifier("sourceAwareDataSource")
    public void setSourceAwareDataSource(final SourceAwareDataSource sourceAwareDataSource) {
        this.sourceAwareDataSource = sourceAwareDataSource;
    }


    @Autowired
    public void setRpslObjectDao(RpslObjectDao rpslObjectDao) {
        this.rpslObjectDao = rpslObjectDao;
    }

    @Autowired
    public void setRpslObjectUpdateDao(RpslObjectUpdateDao rpslObjectUpdateDao) {
        this.rpslObjectUpdateDao = rpslObjectUpdateDao;
    }

    @Override
    public void setEmbeddedValueResolver(final StringValueResolver valueResolver) {
        this.valueResolver = valueResolver;
    }



    private static String dbBaseName;
    private static Map<String, String> grsDatabaseNames = Maps.newHashMap();

    public static synchronized void setupDatabase() {
        if (dbBaseName != null) {
            return;
        }

        final JdbcTemplate jdbcTemplate = createDefaultTemplate();
        ensureLocalhost(jdbcTemplate);
        cleanupOldTables(jdbcTemplate);

        final String uniqueForkId = DigestUtils.md5DigestAsHex(UUID.randomUUID().toString().getBytes());

        dbBaseName = "test_" + System.currentTimeMillis() + "_" + uniqueForkId;

        setupDatabase(jdbcTemplate, "acl.database", "ACL", "acl_schema.sql");
        setupDatabase(jdbcTemplate, "mailupdates.database", "MAILUPDATES", "mailupdates_schema.sql");
        setupDatabase(jdbcTemplate, "whois.db", "WHOIS", "whois_schema.sql", "whois_data.sql");
        setupDatabase(jdbcTemplate, "internals.database", "INTERNALS", "internals_schema.sql", "internals_data.sql");

        final String masterUrl = String.format("jdbc:log:mariadb://localhost/%s_WHOIS;driver=%s", dbBaseName, JDBC_DRIVER);
        System.setProperty("whois.db.master.url", masterUrl);
        System.setProperty("whois.db.master.driver", LoggingDriver.class.getName());

        final String slaveUrl = String.format("jdbc:mariadb://localhost/%s_WHOIS", dbBaseName);
        System.setProperty("whois.db.slave.url", slaveUrl);
        System.setProperty("whois.db.driver", JDBC_DRIVER);

        final String grsSlaveUrl = String.format("jdbc:mariadb://localhost/%s", dbBaseName);
        System.setProperty("whois.db.grs.slave.baseurl", grsSlaveUrl);
        System.setProperty("whois.db.grs.master.baseurl", grsSlaveUrl);
    }

    private static void cleanupOldTables(final JdbcTemplate jdbcTemplate) {
        final Pattern dbPattern = Pattern.compile("test_(\\d+)_.*");
        for (final String db : jdbcTemplate.queryForList("SHOW DATABASES LIKE 'test_%'", String.class)) {
            final Matcher dbMatcher = dbPattern.matcher(db);
            if (dbMatcher.matches()) {
                final String creationTimeString = dbMatcher.group(1);

                final LocalDateTime creationTime = DateTimeProvider.fromEpochMilli(Long.parseLong(creationTimeString));
                if (creationTime.isBefore(LocalDateTime.now().minusHours(1))) {
                    jdbcTemplate.execute("DROP DATABASE IF EXISTS " + db);
                }
            }
        }
    }

    public static void addGrsDatabases(final String... sourceNames) {
        for (final String sourceName : sourceNames) {
            Validate.isTrue(sourceName.endsWith("-GRS"), sourceName + " must end with -GRS");
            final String propertyName = "whois.db." + sourceName;
            final String dbName = sourceName.replace('-', '_');

            if (!grsDatabaseNames.containsKey(sourceName)) {
                setupDatabase(createDefaultTemplate(), propertyName, dbName, "whois_schema.sql");
                grsDatabaseNames.put(sourceName, dbName);
            }
        }

        final Joiner joiner = Joiner.on(',');
        final String grsSources = joiner.join(System.getProperty("grs.sources", ""), joiner.join(sourceNames));
        System.setProperty("grs.sources", grsSources);
    }

    static void setupDatabase(final JdbcTemplate jdbcTemplate, final String propertyBase, final String name, final String... sql) {
        final String dbName = dbBaseName + "_" + name;
        jdbcTemplate.execute("CREATE DATABASE " + dbName + " CHARACTER SET latin1 COLLATE latin1_swedish_ci");

        loadScripts(new JdbcTemplate(createDataSource(dbName)), sql);

        System.setProperty(propertyBase + ".url", "jdbc:mariadb://localhost/" + dbName);
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
            dataSource.setUrl("jdbc:mariadb://localhost/" + databaseName);
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
        // Setup configured sources
        final Splitter splitter = Splitter.on(',').trimResults().omitEmptyStrings();
        final String sourcesConfig = valueResolver.resolveStringValue("${whois.source},${nrtm.import.sources:},${grs.sources:}");
        final Set<String> sources = Sets.newHashSet(splitter.split(sourcesConfig));

        for (final String source : sources) {
            try {
                final JdbcTemplate jdbcTemplate = sourceContext.getSourceConfiguration(Source.master(source)).getJdbcTemplate();
                setupWhoisDatabase(jdbcTemplate);
            } catch (IllegalSourceException e) {
                LOGGER.warn("Source not configured, check test: {}", source);
            }
        }

        setupInternalsDatabase();
        setupMailupdatesDatabase();
        setupAclDatabase();
    }

    public void setupWhoisDatabase(JdbcTemplate jdbcTemplate) {
        truncateTables(jdbcTemplate);
        loadScripts(jdbcTemplate, "whois_data.sql");
    }

    public void setupAclDatabase() {
        truncateTables(aclTemplate);
    }

    public void setupMailupdatesDatabase() {
        truncateTables(mailupdatesTemplate);
    }

    public void setupInternalsDatabase() {
        truncateTables(internalsTemplate);
        loadScripts(internalsTemplate, "internals_data.sql");
    }

    public DataSource getMailupdatesDataSource() {
        return mailupdatesDataSource;
    }

    public JdbcTemplate getMailupdatesTemplate() {
        return mailupdatesTemplate;
    }

    public JdbcTemplate getInternalsTemplate() {
        return internalsTemplate;
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

    public RpslObject translateAuth(final RpslObject rpslObject) {
        return SsoHelper.translateAuth(rpslObject, new AuthTranslator() {
            @Override
            @CheckForNull
            public RpslAttribute translate(String authType, String authToken, RpslAttribute originalAttribute) {
                if (authType.equals("SSO")) {
                    try {
                        final String uuid = crowdClient.getUuid(authToken);
                        return new RpslAttribute(originalAttribute.getKey(), "SSO " + uuid);
                    } catch (CrowdClientException e) {
                        LOGGER.info(e.getMessage());
                    }
                }
                return null;
            }
        });
    }

    // TODO: [AH] we should sanitize when setting up test DB, like we do in production.
    // TODO: [AH] use AttributeSanitizer here when the SQL DB is fully cleaned up
    public RpslObject addObject(final RpslObject rpslObject) {
        final RpslObjectUpdateInfo objectUpdateInfo = rpslObjectUpdateDao.createObject(translateAuth(rpslObject));
        claimId(rpslObject);
        return new RpslObject(objectUpdateInfo.getObjectId(), rpslObject);
    }

    public RpslObject addObjectToSource(final String source, final String rpslString) {
        return addObjectToSource(source, RpslObject.parse(rpslString));
    }

    public RpslObject addObjectToSource(final String source, final RpslObject rpslObject) {
        try {
            sourceContext.setCurrent(Source.master(source));
            return addObject(rpslObject);
        } finally {
            sourceContext.removeCurrentSource();
        }
    }

    public Map<RpslObject, RpslObjectUpdateInfo> addObjects(final RpslObject... rpslObjects) {
        return addObjects(Arrays.asList(rpslObjects));
    }

    public Map<RpslObject, RpslObjectUpdateInfo> addObjects(final Collection<RpslObject> rpslObjects) {
        final Map<RpslObject, RpslObjectUpdateInfo> transformedInfoMap = Maps.newHashMap();
        final Map<RpslObject, RpslObjectUpdateInfo> updateInfoMap = Maps.newHashMap();

        for (final RpslObject rpslObject : rpslObjects) {
            // create object with key attribute(s) only - without reference to other objects
            RpslObject sanitizedObject = attributeSanitizer.sanitize(rpslObject, new ObjectMessages());
            sanitizedObject = translateAuth(sanitizedObject);
            RpslObject keysOnlyObject = keepKeyAttributesOnly(new RpslObjectBuilder(sanitizedObject)).get();
            final RpslObjectUpdateInfo updateInfo = addObjectWithoutReferences(keysOnlyObject, rpslObjectUpdateDao);
            claimId(sanitizedObject);

            updateInfoMap.put(rpslObject, updateInfo);
            transformedInfoMap.put(sanitizedObject, updateInfo);
        }

        for (RpslObject transformedObject : transformedInfoMap.keySet()) {
            // update object with all attributes (including references)
            rpslObjectUpdateDao.updateObject(transformedInfoMap.get(transformedObject).getObjectId(), transformedObject);
        }
        return updateInfoMap;
    }

    // TODO: move claimIds from ObjectLoader to whois-commons
    private void claimId(RpslObject rpslObject) {
        // claim IDs. ugly, but ObjectLoader is in whois-update
        try {
            Object bean = applicationContext.getBean("objectLoader");
            bean.getClass().getMethod("claimIds", RpslObject.class).invoke(bean, rpslObject);
            LOGGER.info("Claimed IDs for {}", rpslObject.getFormattedKey());
        } catch (Exception ignored) {}
    }

    private RpslObjectUpdateInfo addObjectWithoutReferences(final RpslObject rpslObject, final RpslObjectUpdateDao rpslObjectUpdateDao) {
        return rpslObjectUpdateDao.createObject(keepKeyAttributesOnly(new RpslObjectBuilder(rpslObject)).get());
    }

    public RpslObject updateObject(final String rpslString) {
        return updateObject(RpslObject.parse(rpslString));
    }

    public RpslObject updateObject(final RpslObject rpslObject) {
        final RpslObjectInfo objectInfo = rpslObjectDao.findByKey(rpslObject.getType(), rpslObject.getKey().toString());

        rpslObjectUpdateDao.updateObject(objectInfo.getObjectId(), rpslObject);
        return RpslObject.parse(objectInfo.getObjectId(), rpslObject.toByteArray());
    }

    public RpslObjectUpdateInfo deleteObject(final RpslObject rpslObject) {
        final RpslObjectInfo objectInfo = rpslObjectDao.findByKey(rpslObject.getType(), rpslObject.getKey().toString());
        return rpslObjectUpdateDao.deleteObject(objectInfo.getObjectId(), objectInfo.getKey());
    }

    public RpslObject lookupObject(final ObjectType type, final String pkey) {
        return rpslObjectDao.getByKey(type, pkey);
    }

    public void unban(final String prefix) throws InterruptedException {
        aclTemplate.update("INSERT INTO acl_event (prefix, event_time, daily_limit, event_type) VALUES (?, ?, ?, ?)",
                prefix,
                new Date(),
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

    public void clearAclMirrors() {
        aclTemplate.update("DELETE FROM acl_mirror");
    }

    public List<Map<String, Object>> listAclEvents() {
        return aclTemplate.queryForList(
                "SELECT * FROM acl_event"
        );
    }

    public void insertApiKey(final String apiKey, final String uri, final String comment) {
        aclTemplate.update(
                "INSERT INTO apikeys (apikey, uri_prefix, comment) VALUES(?, ?, ?)",
                apiKey, uri, comment);
    }

    public void insertUser(final User user) {
        aclTemplate.update(
                "INSERT INTO override_users (username, password, objecttypes, last_changed) VALUES (?, ?, ?, ?)",
                user.getUsername(),
                user.getHashedPassword(),
                Joiner.on(',').join(user.getObjectTypes()),
                new java.sql.Date(new Date().getTime()));
    }

    public static void dumpSchema(final DataSource datasource) {
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

    public void deleteAuthoritativeResource(final String source, final String resource) {
        internalsTemplate.execute("delete from authoritative_resource where source ='"+source+"' and resource = '"+resource+"'");
        authoritativeResourceData.refreshAuthoritativeResourceCacheOnChange();
    }


    public void addAuthoritativeResource(final String source, final String resource) {
        internalsTemplate.execute("insert into authoritative_resource (source, resource) values ('"+source+"', '"+resource+"')");
        authoritativeResourceData.refreshAuthoritativeResourceCacheOnChange();
    }

}
