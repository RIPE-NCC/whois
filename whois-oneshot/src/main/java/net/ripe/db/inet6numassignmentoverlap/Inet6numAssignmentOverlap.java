package net.ripe.db.inet6numassignmentoverlap;

import com.google.common.collect.ImmutableSet;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.ripe.db.whois.api.rest.RestClient;
import net.ripe.db.whois.common.io.RpslObjectFileReader;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectMessages;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.QueryFlag;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.PatternLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

public class Inet6numAssignmentOverlap {
    private static final Logger LOGGER = LoggerFactory.getLogger(Inet6numAssignmentOverlap.class);

    private static final String ARG_DUMPFILE = "dump-file";

    private static final RestClient restClient = new RestClient();

    public static void main(final String[] argv) throws Exception {
        setupLogging();

        restClient.setRestApiUrl("http://rest.db.ripe.net");
        restClient.setSource("RIPE");

        final OptionSet options = setupOptionParser().parse(argv);
        String fileName = options.valueOf(ARG_DUMPFILE).toString();

        for (String object : new RpslObjectFileReader(fileName)) {
            if (StringUtils.isBlank(object)) {
                continue;
            }

            RpslObject rpslObject;
            try {
                rpslObject = RpslObject.parse(object);
            } catch (RuntimeException e) {
                LOGGER.warn(object, e);
                continue;
            }

            try {
                ObjectMessages objectMessages = new ObjectMessages();
                RpslAttribute status = rpslObject.findAttribute(AttributeType.STATUS);
                status.validateSyntax(ObjectType.INET6NUM, objectMessages);
                if (objectMessages.hasMessages()) {
                    throw new RuntimeException(objectMessages.toString());
                }

                if (status.getCleanValue().toLowerCase().equals("assigned")) {
                    Iterable<RpslObject> rpslObjects = restClient.search(rpslObject.getKey().toString(),
                            Collections.EMPTY_SET,
                            Collections.EMPTY_SET,
                            Collections.EMPTY_SET,
                            Collections.EMPTY_SET,
                            Collections.singleton(ObjectType.INET6NUM),
                            ImmutableSet.of(QueryFlag.ONE_LESS, QueryFlag.NO_REFERENCED));

                    for (RpslObject parent : rpslObjects) {
                        if (parent.getValueForAttribute(AttributeType.STATUS).toLowerCase().equals("assigned")) {
                            System.out.println(rpslObject.getFormattedKey() + ";" + parent.getFormattedKey() + " both assigned");
                        }
                    }
                }
            } catch (RuntimeException e) {
                LOGGER.error(rpslObject.getFormattedKey() + ": " + e.getMessage());
            }
        }
    }

    private static void setupLogging() {
        LogManager.getRootLogger().setLevel(Level.INFO);
        final ConsoleAppender console = new ConsoleAppender();
        console.setLayout(new PatternLayout("%d [%c{1}] %m%n"));
        console.setThreshold(Level.INFO);
        console.activateOptions();
        LogManager.getRootLogger().addAppender(console);
    }

    private static OptionParser setupOptionParser() {
        final OptionParser parser = new OptionParser();
        parser.accepts(ARG_DUMPFILE).withRequiredArg().required();
        return parser;
    }
}
