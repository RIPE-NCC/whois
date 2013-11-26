package net.ripe.db.defaultabusec;

import com.google.common.collect.Lists;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.ripe.db.whois.api.rest.RestClient;
import net.ripe.db.whois.api.rest.domain.AbuseContact;
import net.ripe.db.whois.common.io.RpslObjectFileReader;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.internal.api.abusec.AbuseCService;

import java.util.List;

public class DefaultAbuseC {
    private static final String ARG_BATCHSIZE = "batch-size";
    private static final String ARG_ORGFILE = "org-file";
    private static final String ARG_OVERRIDE = "override";

    private static final RestClient restClient = new RestClient();
    private static final AbuseCService abuseCService = new AbuseCService(restClient, "RIPE");

    public static void main(final String[] argv) throws Exception {
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
            abuseCService.createAbuseRole(orgkey, email);

            System.out.println(orgkey + "\t" + email);

            if (batchSize-- <= 0) {
                System.err.println("Batch size limit reached, ending run");
                break;
            }
        }
    }

    private static OptionParser setupOptionParser() {
        final OptionParser parser = new OptionParser();
        parser.accepts(ARG_BATCHSIZE).withRequiredArg().required().ofType(Integer.class);
        parser.accepts(ARG_ORGFILE).withRequiredArg().required();
        parser.accepts(ARG_OVERRIDE).withRequiredArg().required();
        return parser;
    }
}
