package net.ripe.db.defaultabusec;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.ripe.db.whois.api.rest.RestClient;
import net.ripe.db.whois.common.io.RpslObjectFileReader;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.internal.api.abusec.AbuseCService;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.PatternLayout;

import javax.ws.rs.core.Response;
import java.util.List;

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
            RpslObject object;
            try {
                object = RpslObject.parse(nextObject);
            } catch (Exception e) {
                continue;
            }

            try {
                if (object.getType() != ObjectType.ORGANISATION) {
                    throw new IllegalArgumentException("Found non-organisation object: " + nextObject);
                }

                if (!object.getValueForAttribute(AttributeType.ORG_TYPE).toLowerCase().equals("lir")) {
                    continue;
                }

                List<RpslAttribute> attributes = object.findAttributes(AttributeType.E_MAIL);
                if (attributes.size() > 1) {
                    System.err.println("More than 1 email in " + object.getFormattedKey() + ", picking first");
                }
                String email = attributes.get(0).getCleanValue().toString();

                String orgkey = object.getKey().toString();
                Response abuseRole = abuseCService.createAbuseRole(orgkey, email);
                System.err.println(orgkey + ": " + abuseRole.toString());

                if (abuseRole.getStatus() != 200) {
                    continue;
                }

                System.out.println(orgkey + "|" + email);

                if (batchSize-- <= 0) {
                    System.err.println("Batch size limit reached, ending run");
                    break;
                }
            } catch (Exception e) {
                System.err.println("Error processing " + object.getFormattedKey());
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
