package net.ripe.db.abusec;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.ripe.db.LogUtil;
import net.ripe.db.whois.api.rest.RestClient;
import net.ripe.db.whois.common.io.RpslObjectFileReader;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.internal.api.abusec.AbuseCService;

public class SetDefaultAbuseC {
    private static final String ARG_BATCHSIZE = "batch-size";
    private static final String ARG_ORGFILE = "org-file";
    private static final String ARG_OVERRIDE = "override";

    public static void main(final String[] argv) throws Exception {
        LogUtil.initLogger();

        final OptionSet options = setupOptionParser().parse(argv);
        int batchSize = Integer.parseInt(options.valueOf(ARG_BATCHSIZE).toString());
        String fileName = options.valueOf(ARG_ORGFILE).toString();
        String override = options.valueOf(ARG_OVERRIDE).toString();

        final RestClient restClient = new RestClient("https://rest.db.ripe.net", "RIPE");
        final AbuseCService abuseCService = null; //new AbuseCService(restClient, "RIPE"); FIX THIS @ next time use

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

                if (!dumpOrg.getValueForAttribute(AttributeType.ORG_TYPE).equals("lir")) {
                    continue;
                }

                RpslObject currentOrg = restClient.request().lookup(ObjectType.ORGANISATION, dumpOrg.getKey().toString());
                RpslAttribute abusecAttr = currentOrg.findAttribute(AttributeType.ABUSE_C);
                RpslObject abuseRole = restClient.request().lookup(ObjectType.ROLE, abusecAttr.getCleanValue().toString());

                RpslObjectBuilder builder = new RpslObjectBuilder(abuseRole);
                builder.removeAttribute(new RpslAttribute(AttributeType.MNT_BY, "RIPE-NCC-HM-MNT"));
                if (builder.size() < abuseRole.size()) {
                    RpslObject withoutRipeMntner = builder.get();

                    restClient.request()
                            .addParam("override", override)
                            .update(withoutRipeMntner);

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

    private static OptionParser setupOptionParser() {
        final OptionParser parser = new OptionParser();
        parser.accepts(ARG_BATCHSIZE).withRequiredArg().required().ofType(Integer.class);
        parser.accepts(ARG_ORGFILE).withRequiredArg().required();
        parser.accepts(ARG_OVERRIDE).withRequiredArg().required();
        return parser;
    }
}
