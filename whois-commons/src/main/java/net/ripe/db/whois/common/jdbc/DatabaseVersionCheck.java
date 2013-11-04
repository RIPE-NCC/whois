package net.ripe.db.whois.common.jdbc;

import com.google.common.base.CharMatcher;
import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import net.ripe.db.whois.common.profiles.DeployedProfile;
import net.ripe.db.whois.common.source.SourceAwareDataSource;
import net.ripe.db.whois.common.source.SourceConfiguration;
import net.ripe.db.whois.common.source.SourceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@DeployedProfile
public class DatabaseVersionCheck {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseVersionCheck.class);
    static final Pattern RESOURCE_MATCHER = Pattern.compile("(?i)^([a-z]+)-([0-9.-]+)$");
    static final Splitter VERSION_SPLITTER = Splitter.on(CharMatcher.anyOf(".-")).omitEmptyStrings();

    private final ApplicationContext applicationContext;
    @Value("${application.version}")
    private String applicationVersion;

    @Autowired
    public DatabaseVersionCheck(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * expects versions to be in the format '1.20.03.4'
     * Returns <0, 0, >0  if v1 is <, =, > than v2
     */
    public static int compareVersions(final String v1, final String v2) {
        Iterator<String> i1 = VERSION_SPLITTER.split(v1).iterator();
        Iterator<String> i2 = VERSION_SPLITTER.split(v2).iterator();
        while (i1.hasNext() && i2.hasNext()) {
            int res = Integer.parseInt(i1.next()) - Integer.parseInt(i2.next());
            if (res != 0) {
                return res;
            }
        }

        if (i1.hasNext() && !i2.hasNext()) {
            return 1;
        }
        if (!i1.hasNext() && i2.hasNext()) {
            return -1;
        }
        return 0;
    }

    public Iterable<String> getSqlPatchResources() throws IOException {
        return Iterables.transform(Arrays.asList(applicationContext.getResources("patch/*-*.*.sql")), new Function<Resource, String>() {
            @Nullable
            @Override
            public String apply(@Nullable Resource input) {
                final String ret = input.getFilename();
                if (ret.endsWith(".sql")) {
                    return ret.substring(0, ret.length() - 4);
                }
                return ret;
            }
        });
    }

    @PostConstruct
    public void checkDatabaseVersions() {
        try {
            checkAllDataSources();
        } catch (Exception e) {
            LOGGER.error("Database version check failed", e);
        }
    }

    private void checkAllDataSources() throws IOException {
        final Iterable<String> resources = getSqlPatchResources();
        final Map<String, DataSource> beansOfType = applicationContext.getBeansOfType(DataSource.class);

        for (Map.Entry<String, DataSource> dataSourceEntry : beansOfType.entrySet()) {
            final DataSource dataSource = dataSourceEntry.getValue();

            // peek deep into sourceawaredatasource
            if (dataSource instanceof SourceAwareDataSource) {
                SourceContext sourceContext = applicationContext.getBean(SourceContext.class);
                for (SourceConfiguration source : sourceContext.getAllSourceConfigurations()) {
                    try {
                        sourceContext.setCurrent(source.getSource());
                        final String dataSourceName = dataSourceEntry.getKey() + "/" + source;
                        checkDataSource(resources, dataSource, dataSourceName);
                    } finally {
                        sourceContext.removeCurrentSource();
                    }
                }
            } else {
                final String dataSourceName = dataSourceEntry.getKey() + "[" + dataSource.getClass() + "]";
                checkDataSource(resources, dataSource, dataSourceName);
            }
        }
    }

    private void checkDataSource(Iterable<String> resources, DataSource dataSource, String dataSourceName) {
        try {
            final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            final String dbVersion = jdbcTemplate.queryForObject("SELECT version FROM version", String.class);
            checkDatabase(resources, dataSourceName, dbVersion);
        } catch (Exception e) {
            if (e.getMessage().contains("SELECT command denied to user")) { // ugly but no other way to get this, sadly
                LOGGER.info("Datasource {} skipped (no rights)", dataSourceName);
            } else {
                LOGGER.warn("Error checking datasource {}", dataSourceName, e);
            }
        }
    }

    public void checkDatabase(Iterable<String> resources, String dataSourceName, String dbVersion) {
        Matcher dbVersionMatcher = RESOURCE_MATCHER.matcher(dbVersion);
        if (!dbVersionMatcher.matches()) {
            throw new IllegalStateException("Invalid version: " + dbVersion);
        }

        for (String resource : resources) {
            Matcher resourceMatcher = RESOURCE_MATCHER.matcher(resource);
            if (!resourceMatcher.matches()) continue;
            if (!dbVersionMatcher.group(1).equals(resourceMatcher.group(1))) continue;

            final String dbVersionNumber = dbVersionMatcher.group(2);
            final String resourceVersionNumber = resourceMatcher.group(2);
            if (compareVersions(dbVersionNumber, resourceVersionNumber) < 0) {
                throw new IllegalStateException("Patch " + resource + ".sql was not applied");
            }
        }
        LOGGER.info("Datasource {} validated OK (DB version: {})", dataSourceName, dbVersion);
    }
}
