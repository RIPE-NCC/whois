package net.ripe.db.ascleanup;


import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.ripe.db.whois.api.rest.client.RestClient;
import net.ripe.db.whois.api.rest.client.RestClientException;
import net.ripe.db.whois.api.rest.client.RestClientUtils;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.io.RpslObjectFileReader;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.PatternLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Map;

/**
 --overrideuser dbint
 --overridepassword pass
 --overrideoptions rollback_autnumcleanup {notify=false}
 --source RIPE
 --resturl http://localhost:1080/whois
 --filebefore autnumCleanupDeleter.before
 --fileafter autnumCleanupDeleter.after
 --dryrun true
 */

public class RollbackScript {
    private static final Logger LOGGER = LoggerFactory.getLogger(String.format("%s.log", RollbackScript.class.getSimpleName()));


    private static final String ARG_RESTURL = "resturl";
    private static final String ARG_RESTSOURCE = "source";
    private static final String ARG_OVERRIDEUSER = "overrideuser";
    private static final String ARG_OVERRIDEPASSWORD = "overridepassword";
    private static final String ARG_OVERRIDEOPTIONS = "overrideoptions";

    private static final String ARG_DRYRUN = "dryrun";
    private static final String ARG_FILEBEFORE = "filebefore";
    private static final String ARG_FILEAFTER = "fileafter";

    private final RestClient restClient;
    private final String override;
    private final String fileBefore;
    private final String fileAfter;
    private final boolean dryRun;

    public static void main(final String[] argv) throws Exception {
        setupLogging();
        final OptionSet options = setupOptionParser().parse(argv);

        new RollbackScript(
                options.valueOf(ARG_RESTURL).toString(),
                options.valueOf(ARG_RESTSOURCE).toString(),
                options.valueOf(ARG_OVERRIDEUSER).toString(),
                options.valueOf(ARG_OVERRIDEPASSWORD).toString(),
                options.valueOf(ARG_OVERRIDEOPTIONS).toString(),
                options.valueOf(ARG_FILEBEFORE).toString(),
                options.valueOf(ARG_FILEAFTER).toString(),
                (Boolean)options.valueOf(ARG_DRYRUN)).execute();
    }

    private void execute() {

        final Map<CIString, RpslObject> beforeMap = createObjectMapFromFile(fileBefore);
        LOGGER.info("{} in before file", beforeMap.size());

        rollback(beforeMap);
    }

    private void rollback(final Map<CIString, RpslObject> beforeMap) {
        int restored=0;
        int changed=0;

        RpslObject afterObject;
        for (final String nextObject : new RpslObjectFileReader(Resources.getResource(fileAfter).getFile())) {
            try {
                 afterObject = RpslObject.parse(nextObject);

            } catch (Exception e) {
                LOGGER.info("Malformed RPSL: \n" + nextObject, e);
                continue;
            }

            final CIString key = afterObject.getKey();
            final RpslObject dbObject = restClient.request().addParam("unformatted", "").lookup(afterObject.getType(), key.toString());

            if (!dbObject.equals(afterObject)){
                LOGGER.info("Object {} is changed. It manual restore.", key);
                changed++;
                continue;
            }

            if (dbObject.equals(afterObject)){
                //object has not changed since the updateScript ran
                final RpslObject beforeObject = beforeMap.get(key);
                if (!dryRun) {
                    try {
                        final RpslObject updatedObject = restClient.request().addParam("unformatted", "").addParam("override", override).update(beforeObject);
                        if (!updatedObject.equals(beforeObject)){
                            LOGGER.info("Object {} is restored but differs from before version.", key);
                        } else {
                            LOGGER.info("Object {} is restored.", key);
                        }
                        restored++;
                    } catch (RestClientException e) {
                        LOGGER.info("Object {} could not be updated.", key);
                    }
                } else {
                    LOGGER.info("Object {} is restored in dry run.", key);
                    restored++;
                }
            }
        }
        LOGGER.info("{} objects were restored.", restored);
        LOGGER.info("{} objects are changed and need manual restore.", changed);
    }

    private Map<CIString, RpslObject> createObjectMapFromFile(final String filepath){
        final Map<CIString, RpslObject> map = Maps.newLinkedHashMap();

        for (final String nextObject : new RpslObjectFileReader(Resources.getResource(filepath).getFile())) {
            try {
                final RpslObject rpslObject = RpslObject.parse(nextObject);
                map.put(rpslObject.getKey(), rpslObject);
            } catch (Exception e) {
                LOGGER.info("Malformed RPSL: \n" + nextObject, e);
                continue;
            }
        }
        return map;
    }

    private static void setupLogging() {
        final FileAppender appender = new FileAppender();

        appender.setFile(String.format("%s.log", RollbackScript.class.getSimpleName()));
        appender.setLayout(new PatternLayout("%m%n"));
        appender.setThreshold(Level.INFO);
        appender.setAppend(false);
        appender.activateOptions();
        LogManager.getRootLogger().addAppender(appender);
    }

    public RollbackScript(final String restUrl, final String source,
                          final String overrideUser, final String overridePassword, final String overrideOptions,
                          final String fileBefore, final String fileAfter, final boolean dryRun) throws SQLException {
        this.restClient = RestClientUtils.createRestClient(restUrl, source);

        final Joiner commaJoiner = Joiner.on(',');

        if (StringUtils.isBlank(overrideOptions)) {
            this.override = commaJoiner.join(overrideUser, overridePassword);
        } else {
            this.override = commaJoiner.join(overrideUser, overridePassword, overrideOptions);
        }

        this.fileBefore = fileBefore;
        this.fileAfter = fileAfter;
        this.dryRun = dryRun;
    }

    private static OptionParser setupOptionParser() {
        final OptionParser parser = new OptionParser();
        parser.accepts(ARG_RESTURL).withRequiredArg().required();
        parser.accepts(ARG_RESTSOURCE).withRequiredArg().required();
        parser.accepts(ARG_OVERRIDEUSER).withRequiredArg().required();
        parser.accepts(ARG_OVERRIDEPASSWORD).withRequiredArg().required();
        parser.accepts(ARG_OVERRIDEOPTIONS).withOptionalArg();
        parser.accepts(ARG_FILEBEFORE).withRequiredArg().required();
        parser.accepts(ARG_FILEAFTER).withRequiredArg().required();
        parser.accepts(ARG_DRYRUN).withOptionalArg().ofType(Boolean.class).defaultsTo(true);
        return parser;
    }
}
