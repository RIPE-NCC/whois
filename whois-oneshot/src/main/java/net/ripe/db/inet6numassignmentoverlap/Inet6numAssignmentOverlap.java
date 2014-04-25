package net.ripe.db.inet6numassignmentoverlap;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.ripe.db.LogUtil;
import net.ripe.db.whois.api.rest.client.RestClient;
import net.ripe.db.whois.common.io.RpslObjectFileReader;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectMessages;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.QueryFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class Inet6numAssignmentOverlap {
    private static final Logger LOGGER = LoggerFactory.getLogger(Inet6numAssignmentOverlap.class);

    private static final String ARG_DUMPFILE = "dump-file";

    private static final RestClient restClient = new RestClient("http://rest.db.ripe.net", "RIPE");

    public static void main(final String[] argv) throws Exception {
        LogUtil.initLogger();

        final OptionSet options = setupOptionParser().parse(argv);
        String fileName = options.valueOf(ARG_DUMPFILE).toString();

        for (String object : new RpslObjectFileReader(fileName)) {
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

                if (status.getCleanValue().equals("assigned")) {
                    final Collection<RpslObject> rpslObjects = restClient.request()
                            .addParam("query-string", rpslObject.getKey().toString())
                            .addParams("type-filter", ObjectType.INET6NUM.getName())
                            .addParams("flags", QueryFlag.ONE_LESS.getName(), QueryFlag.NO_REFERENCED.getName())
                            .search();

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

    private static OptionParser setupOptionParser() {
        final OptionParser parser = new OptionParser();
        parser.accepts(ARG_DUMPFILE).withRequiredArg().required();
        return parser;
    }
}
