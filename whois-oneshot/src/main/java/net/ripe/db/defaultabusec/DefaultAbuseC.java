package net.ripe.db.defaultabusec;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.ripe.db.whois.api.rest.RestClient;
import net.ripe.db.whois.common.io.RpslObjectFileReader;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.internal.api.abusec.AbuseCService;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.PatternLayout;

public class DefaultAbuseC {
    private static final String ARG_BATCHSIZE = "batch-size";
    private static final String ARG_ORGFILE = "org-file";
    private static final String ARG_OVERRIDE = "override";

    private static final RestClient restClient = new RestClient();
    private static final AbuseCService abuseCService = new AbuseCService(restClient, "RIPE");

    public static void main(final String[] argv) throws Exception {
        setupLogging();

        final OptionSet options = setupOptionParser().parse(argv);
        int batchSize = Integer.parseInt(options.valueOf(ARG_BATCHSIZE).toString());
        String fileName = options.valueOf(ARG_ORGFILE).toString();
        String override = options.valueOf(ARG_OVERRIDE).toString();

        restClient.setRestApiUrl("https://rest.db.ripe.net");
        restClient.setSource("RIPE");
        abuseCService.setOverride(override);

        for (String nextObject : new RpslObjectFileReader(fileName)) {
            RpslObject dumpOrg;
            try {
                dumpOrg = RpslObject.parse(nextObject);
            } catch (Exception e) {
                continue;
            }

            try {
                if (dumpOrg.getType() != ObjectType.ORGANISATION) {
                    throw new IllegalArgumentException("Found non-organisation object: " + nextObject);
                }

                if (!dumpOrg.getValueForAttribute(AttributeType.ORG_TYPE).toLowerCase().equals("lir")) {
                    continue;
                }

                RpslObject currentOrg = restClient.lookup(ObjectType.ORGANISATION, dumpOrg.getKey().toString());
                RpslAttribute abusecAttr = currentOrg.findAttribute(AttributeType.ABUSE_C);
                RpslObject abuseRole = restClient.lookup(ObjectType.ROLE, abusecAttr.getCleanValue().toString());

                RpslObjectBuilder builder = new RpslObjectBuilder(abuseRole);
                builder.removeAttribute(new RpslAttribute(AttributeType.MNT_BY, "RIPE-NCC-HM-MNT"));
                if (builder.size() < abuseRole.size()) {
                    RpslObject withoutRipeMntner = builder.get();

                    restClient.updateOverride(withoutRipeMntner, override);
                    System.out.println(dumpOrg.getFormattedKey());

                    if (batchSize-- <= 0) {
                        System.err.println("Batch size limit reached, ending run");
                        break;
                    }
                } else {
                    System.err.println("No mntner found: " + dumpOrg.getKey() + "("+abuseRole.getKey()+")");
                }
            } catch (Exception e) {
                System.err.println("Error processing " + dumpOrg.getFormattedKey());
                e.printStackTrace(System.err);
            }
        }
    }

    private static void setupLogging() {
        LogManager.getRootLogger().setLevel(Level.INFO);
        ConsoleAppender console = new ConsoleAppender();
        console.setLayout(new PatternLayout("%d [%c|%C{1}] %m%n"));
        console.setThreshold(Level.INFO);
        console.activateOptions();
        LogManager.getRootLogger().addAppender(console);
    }

    private static OptionParser setupOptionParser() {
        final OptionParser parser = new OptionParser();
        parser.accepts(ARG_BATCHSIZE).withRequiredArg().required().ofType(Integer.class);
        parser.accepts(ARG_ORGFILE).withRequiredArg().required();
        parser.accepts(ARG_OVERRIDE).withRequiredArg().required();
        return parser;
    }
}
