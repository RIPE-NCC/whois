package net.ripe.db.ascleanup;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mysql.jdbc.Driver;
import com.sun.istack.Nullable;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.ripe.db.whois.api.rest.client.RestClient;
import net.ripe.db.whois.api.rest.client.RestClientException;
import net.ripe.db.whois.api.rest.client.RestClientUtils;
import net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations;
import net.ripe.db.whois.common.dao.jdbc.domain.ObjectTypeIds;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.io.Downloader;
import net.ripe.db.whois.common.grs.AuthoritativeResource;
import net.ripe.db.whois.common.grs.AuthoritativeResourceLoader;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.PatternLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.ripe.db.whois.common.domain.CIString.ciString;

/**
 * --source RIPE --overrideuser dbint --overridepassword dbint --resturl http://localost:1080/whois --jdbcurl "jdbc:mysql://localhost/WHOIS_UPDATE_RIPE --dbuser dbint --dbpassword dbint
 */
public class AutnumCleanupDeleter {
    private static final Logger BEFORE_LOGGER = LoggerFactory.getLogger("before");
    private static final Logger AFTER_LOGGER = LoggerFactory.getLogger("after");
    private static final Logger INFO_LOGGER = LoggerFactory.getLogger("info");
    private static final Logger ERROR_LOGGER = LoggerFactory.getLogger("errors");

    private static final List<AttributeType> ATTRIBUTES_TO_CHECK = Lists.newArrayList(
            AttributeType.MEMBERS
//            AttributeType.DEFAULT,
//            AttributeType.EXPORT,
//            AttributeType.IMPORT,
//            AttributeType.MP_EXPORT,
//            AttributeType.MP_IMPORT,
//            AttributeType.FILTER,
//            AttributeType.PEER,
//            AttributeType.MP_PEERING,
//            AttributeType.PEERING,
//            AttributeType.AGGR_BNDRY
    );

    private static final Pattern AS_PATTERN = Pattern.compile("(?i)(?<![\\w:])(AS\\d+)(?![\\w:])");

    private static final String ARG_RESTURL = "resturl";
    private static final String ARG_RESTSOURCE = "source";
    private static final String ARG_OVERRIDEUSER = "overrideuser";
    private static final String ARG_OVERRIDEPASSWORD = "overridepassword";
    private static final String ARG_JDBCURL = "jdbcurl";
    private static final String ARG_DBUSER = "dbuser";
    private static final String ARG_DBPASSWORD = "dbpassword";
    private static final Joiner COMMA_JOINER = Joiner.on(',');

    private long objectsWithReferenceUpdated;
    private long objectsWithReferenceFailed;
    private long attributesChanged;
    private long references;

    private JdbcTemplate jdbcTemplate;
    private AuthoritativeResource authoritativeResource;

    private final RestClient restClient;
    private final String overrideUser;
    private final String overridePassword;

    public static void main(final String[] argv) throws Exception {
        setupLogging();

        final OptionSet options = setupOptionParser().parse(argv);

        final String restUrl = options.valueOf(ARG_RESTURL).toString();
        final String restSource = options.valueOf(ARG_RESTSOURCE).toString();
        final String overrideUser = options.valueOf(ARG_OVERRIDEUSER).toString();
        final String overridePassword = options.valueOf(ARG_OVERRIDEPASSWORD).toString();
        final String jdbcUrl = options.valueOf(ARG_JDBCURL).toString();
        final String dbuser = options.valueOf(ARG_DBUSER).toString();
        final String dbpassword = options.valueOf(ARG_DBPASSWORD).toString();

        new AutnumCleanupDeleter(restUrl, restSource, overrideUser, overridePassword, jdbcUrl, dbuser, dbpassword).execute();
    }

    private static OptionParser setupOptionParser() {
        final OptionParser parser = new OptionParser();
        parser.accepts(ARG_RESTURL).withRequiredArg().required();
        parser.accepts(ARG_RESTSOURCE).withRequiredArg().required();
        parser.accepts(ARG_OVERRIDEUSER).withRequiredArg().required();
        parser.accepts(ARG_OVERRIDEPASSWORD).withRequiredArg().required();
        parser.accepts(ARG_JDBCURL).withRequiredArg();
        parser.accepts(ARG_DBUSER).withRequiredArg().required();
        parser.accepts(ARG_DBPASSWORD).withRequiredArg();
        return parser;
    }

    public AutnumCleanupDeleter(final String restUrl, final String source, final String overrideUser, final String overridePassword, final String jdbcUrl, final String dbUser, final String dbPassword) throws SQLException {
        this.restClient = RestClientUtils.createRestClient(restUrl, source);
        this.overrideUser = overrideUser;
        this.overridePassword = overridePassword;
        this.jdbcTemplate = new JdbcTemplate(new SimpleDriverDataSource(new Driver(), jdbcUrl, dbUser, dbPassword));
    }

    private void execute() {
        loadAuthoritativeResource();

        if (jdbcTemplate != null) {
            executeJdbc();
        } else {
            ERROR_LOGGER.info("Database connection error");
        }
    }

    private void executeJdbc() {
        final List<Integer> objectIds = jdbcTemplate.queryForList("" +
                "SELECT object_id " +
                "FROM last " +
                "WHERE sequence_id != 0 " +
                "AND object_type IN (" +
                COMMA_JOINER.join(
                        ObjectTypeIds.getId(ObjectType.AS_SET),
                        ObjectTypeIds.getId(ObjectType.RTR_SET),
//                        ObjectTypeIds.getId(ObjectType.AUT_NUM),
//                        ObjectTypeIds.getId(ObjectType.FILTER_SET),
//                        ObjectTypeIds.getId(ObjectType.INET_RTR),
//                        ObjectTypeIds.getId(ObjectType.PEERING_SET),
//                        ObjectTypeIds.getId(ObjectType.ROUTE),
                        ObjectTypeIds.getId(ObjectType.ROUTE_SET)
                ) + ")", Integer.class);

        INFO_LOGGER.info("Got {} objectIds", objectIds.size());
        int count = 0;
        for (Integer objectId : objectIds) {
            if (++count % 1000 == 0) {
                INFO_LOGGER.info("Looked at {} objects", count);
            }
            RpslObject object = null;

            try {
                object = JdbcRpslObjectOperations.getObjectById(jdbcTemplate, objectId);

                final RpslObject newObject = replaceAttributes(object);

                if (!object.equals(newObject)) {
                    BEFORE_LOGGER.info(object.toString() + "\n");

                    final RpslObject updatedObject = updateObject(newObject);

                    if (updatedObject != null) {
                        INFO_LOGGER.info("{}: updated", object.getKey());
                        AFTER_LOGGER.info(updatedObject.toString() + "\n");
                        objectsWithReferenceUpdated++;
                    } else {
                        INFO_LOGGER.info("{}: failed", object.getKey());
                        AFTER_LOGGER.info(object.toString() + "\n");
                        objectsWithReferenceFailed++;
                    }
                }
            } catch (RuntimeException e) {
                if (object == null) {
                    ERROR_LOGGER.error("Unable to process RPSL object with object_id: {}", objectId);
                } else {
                    ERROR_LOGGER.error("Unable to process RPSL object with key: {}", object.getKey());
                }
                INFO_LOGGER.info("{}: was not processed", objectId);
                continue;
            }
        }

        INFO_LOGGER.info("\n");
        INFO_LOGGER.info("{} objects with references were updated", objectsWithReferenceUpdated);
        INFO_LOGGER.info("{} objects with references failed to update", objectsWithReferenceFailed);
        INFO_LOGGER.info("{} attributes changed", attributesChanged);
        INFO_LOGGER.info("{} references found", references);
    }

    @Nullable
    private RpslObject updateObject(final RpslObject object) {
        //TODO turn on notify if it's needed
        try {
            return restClient.request()
                    .addParam("override", String.format("%s,%s,autnumCleanup{notify=%s}", overrideUser, overridePassword, "false"))
                    .update(object);
        } catch (RestClientException e) {
            ERROR_LOGGER.info("{} update failed with message(s): {}", object.getKey(), e.toString());
            return null;
        }
    }

    private RpslObject replaceAttributes(final RpslObject object) {
        final RpslObjectBuilder builder = new RpslObjectBuilder(object);

        for (int i = 0; i < builder.size(); i++) {
            try {
                final RpslAttribute rpslAttribute = builder.get(i);
                if (!ATTRIBUTES_TO_CHECK.contains(rpslAttribute.getType())) {
                    continue;
                }

                final RpslAttribute newAttribute = cleanupAttribute(rpslAttribute);

                if (!rpslAttribute.equals(newAttribute)) {
                    attributesChanged++;
                }

                if (newAttribute == null) {
                    builder.remove(i--);
                } else {
                    builder.set(i, newAttribute);
                }
            } catch (RuntimeException ignored) {
                // todo log 'cannot process this complex/broken attribute, leaving as it is'
                INFO_LOGGER.error("{} needs manual update", object.getKey());
                ERROR_LOGGER.info("{} needs manual update", object.getKey(), ignored);
            }
        }

        return builder.get();
    }

    Collection<CIString> findDirty(final RpslAttribute rpslAttribute) {
        final CIString value = rpslAttribute.getCleanValue();
        final Matcher asMatcher = AS_PATTERN.matcher(value);

        final Set<CIString> asns = Sets.newHashSet();
        while (asMatcher.find()) {
            asns.add(ciString(asMatcher.group(1)));
        }

        return Sets.intersection(authoritativeResource.getAutNums(), asns);
    }

    @Nullable
    RpslAttribute cleanupAttribute(final RpslAttribute rpslAttribute) {
        switch (rpslAttribute.getType()) {
//            case EXPORT:
//                return cleanupExportAttribute(rpslAttribute);
//
//            case IMPORT:
//                return cleanupImportAttribute(rpslAttribute);
//
            case MEMBERS:
                return cleanupMembersAttribute(rpslAttribute);

            default:
                return rpslAttribute;
        }
    }


    /**
     * @param rpslAttribute
     * @return null               if attribute should be dropped from the object
     * rpslAttribute      replacement attribute (can be the argument if no change necessary)
     */
    RpslAttribute cleanupMembersAttribute(final RpslAttribute rpslAttribute) {
        RpslAttribute result = rpslAttribute;
        for (CIString value : rpslAttribute.getCleanValues()) {
            if (StringUtils.isBlank(value.toString())) continue;

            if (authoritativeResource.isMaintainedByRir(ObjectType.AUT_NUM, value)) {
                final String match = value.toString();
                references++;
                final String updatedValue = rpslAttribute.getValue().replaceAll(
                        String.format("(?i)\\s*(?<!:)\\b%s\\b(?!:)\\s*,|,\\s*(?<!:)\\b%s\\b(?!:)|\\s*(?<!:)\\b%s\\b(?!:)", match, match, match), "");

                final RpslAttribute updatedAttribute = new RpslAttribute(rpslAttribute.getKey(), updatedValue);

                if (updatedAttribute.getCleanValues().isEmpty()) {
                    return null;
                }
                result = updatedAttribute;
            }
        }

        return result;
    }

    final static Pattern SIMPLE_TO = Pattern.compile("(?i)to\\s+(AS\\d+)\\s+announce\\s+.*");
    final static Pattern SIMPLE_ANNOUNCE = Pattern.compile("(?i)to\\s+.*\\s*announce\\s+(AS\\d+)");

    /**
     * Removes lines like:
     * to AS123 announce AS-TEST
     * to AS123 announce ANY
     * which seems to be 4500 out of 5800 dirty export attributes
     *
     * @param rpslAttribute
     * @return null               if attribute should be dropped from the object
     * rpslAttribute      replacement attribute (can be the argument if no change necessary)
     * @throws java.lang.IllegalArgumentException If attribute requires human attention
     */
    RpslAttribute cleanupExportAttribute(final RpslAttribute rpslAttribute) {
        final Collection<CIString> dirtyFound = findDirty(rpslAttribute);
        if (dirtyFound.isEmpty()) return rpslAttribute;

        final CIString value = rpslAttribute.getCleanValue();

        if (value.contains("{") || value.contains("}")) {
            throw new IllegalArgumentException();
        }

        // simple to
        Matcher matcher = SIMPLE_TO.matcher(value);
        if (matcher.matches()) {
            if (dirtyFound.contains(ciString(matcher.group(1)))) {
                return null;
            }
        }

        // simple announce
        matcher = SIMPLE_ANNOUNCE.matcher(value);
        if (matcher.matches()) {
            if (dirtyFound.contains(ciString(matcher.group(1)))) {
                return null;
            }
        }

        throw new IllegalArgumentException();
    }

    final static Pattern SIMPLE_FROM = Pattern.compile("(?i)from\\s+(AS\\d+)\\s+accept\\s+.*");
    final static Pattern SIMPLE_ACCEPT = Pattern.compile("(?i)from\\s+.*\\s*accept\\s+(AS\\d+)");

    /**
     * @param rpslAttribute
     * @return null               if attribute should be dropped from the object
     * rpslAttribute      replacement attribute (can be the argument if no change necessary)
     * @throws java.lang.IllegalArgumentException If attribute requires human attention
     */
    RpslAttribute cleanupImportAttribute(final RpslAttribute rpslAttribute) {
        final Collection<CIString> dirtyFound = findDirty(rpslAttribute);
        if (dirtyFound.isEmpty()) return rpslAttribute;

        final CIString value = rpslAttribute.getCleanValue();

        if (value.contains("{") || value.contains("}")) {
            throw new IllegalArgumentException();
        }

        // simple from
        Matcher matcher = SIMPLE_FROM.matcher(value);
        if (matcher.matches()) {
            if (dirtyFound.contains(ciString(matcher.group(1)))) {
                return null;
            }
        }

        // simple accept
        matcher = SIMPLE_ACCEPT.matcher(value);
        if (matcher.matches()) {
            if (dirtyFound.contains(ciString(matcher.group(1)))) {
                return null;
            }
        }

        throw new IllegalArgumentException();
    }

    public void setAuthoritativeResource(AuthoritativeResource authoritativeResource) {
        this.authoritativeResource = authoritativeResource;
    }

    public AuthoritativeResource getAuthoritativeResource(final Path dataFile) throws IOException {
        final Downloader downloader = new Downloader();
        downloader.downloadToWithMd5Check(INFO_LOGGER, new URL("ftp://ftp.ripe.net/ripe/stats/delegated-ripencc-extended-latest"), dataFile);

        final AuthoritativeResourceLoader authoritativeResourceLoader = new AuthoritativeResourceLoader(INFO_LOGGER, "ripe", new Scanner(dataFile), ImmutableSet.of("reserved"));
        return authoritativeResourceLoader.load();
    }

    private void loadAuthoritativeResource() {
        try {
            authoritativeResource = getAuthoritativeResource(Files.createTempFile("autnumCleanup", ""));
            INFO_LOGGER.info("# " + authoritativeResource.getAutNums());
            INFO_LOGGER.info("# Number of autnums: " + authoritativeResource.getNrAutNums());
        } catch (IOException e) {
            INFO_LOGGER.info("couldn't get authoritative resource");
        }
    }

    private static void setupLogging() {
        LogManager.getLogger("before").addAppender(createAppender("before"));
        LogManager.getLogger("after").addAppender(createAppender("after"));
        LogManager.getLogger("info").addAppender(createAppender("info"));
        LogManager.getLogger("errors").addAppender(createAppender("errors"));
    }

    private static Appender createAppender(final String name) {
        final FileAppender appender = new FileAppender();

        appender.setFile(String.format("autnumCleanupDeleter.%s", name));
        appender.setLayout(new PatternLayout("%m%n"));
        if (name.equals("errors")) {
            appender.setThreshold(Level.DEBUG);
        } else {
            appender.setThreshold(Level.INFO);
        }
        appender.setAppend(false);
        appender.activateOptions();
        return appender;
    }
}
