package net.ripe.db.rc;

import com.google.common.collect.Lists;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.ripe.db.LogUtil;
import net.ripe.db.whois.api.rest.RestClient;
import net.ripe.db.whois.api.rest.RestClientException;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.common.rpsl.attrs.OrgType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SponsoringOrgAdder {
    private static final Logger LOGGER = LoggerFactory.getLogger(SponsoringOrgAdder.class);

    private static final String ARG_USE_DUMMY_ORG = "dummy-org";
    private static final String ARG_NOTIFY = "notify";
    private static final String ARG_REST_URL = "rest-url";
    private static final String ARG_REST_SOURCE = "rest-source";
    private static final String ARG_OVERRIDE_USER = "override-user";
    private static final String ARG_OVERRIDE_PASS = "override-pass";

    /**
     *
     * Accepted arguments:
     * --rest-url - the rest api url to use, defaults to 'http://rest-test.db.ripe.net'. Required.
     * --rest-source - the rest source to use, defaults to 'RIPE'. Required.
     * --override-user - override username. Required.
     * --override-pass - override password. Required.
     * --dummy-org true/false - sets the sponsoring-org to 'ORG-DUMMY-RIPE'. Defaults to 'false'.
     * --notify - sends notifications
     *
     * Example call, use a dummy organisation with the value ORG-DUMMY-RIPE, and do not send notifications:
     * --rest-url http://rest-test.db.ripe.net --rest-source TEST --override-user user --override-pass password --dummy-org true
     */
    public static void main(final String[] args) {
        LogUtil.initLogger();

        final OptionSet options = setupOptionParser().parse(args);
        final String url = (String)options.valueOf(ARG_REST_URL);
        final String source = (String)options.valueOf(ARG_REST_SOURCE);
        final String overrideUser = (String)options.valueOf(ARG_OVERRIDE_USER);
        final String overridePass = (String)options.valueOf(ARG_OVERRIDE_PASS);
        final boolean useDummy = Boolean.parseBoolean((String)options.valueOf(ARG_USE_DUMMY_ORG));

        new SponsoringOrgAdder(url, source, overrideUser, overridePass, useDummy, options.hasArgument(ARG_NOTIFY)).addSponsoringOrgs();
    }

    private static OptionParser setupOptionParser() {
        final OptionParser parser = new OptionParser();
        parser.accepts(ARG_REST_URL).withRequiredArg().defaultsTo("http://rest-test.db.ripe.net").required();
        parser.accepts(ARG_REST_SOURCE).withRequiredArg().defaultsTo("RIPE").required();
        parser.accepts(ARG_OVERRIDE_USER).withRequiredArg().required();
        parser.accepts(ARG_OVERRIDE_PASS).withRequiredArg().required();
        parser.accepts(ARG_USE_DUMMY_ORG).withOptionalArg().defaultsTo("false");
        parser.accepts(ARG_NOTIFY);
        return parser;
    }


    private static final String DUMMY_ORG = "ORG-DUMMY-RIPE";
    private final RestClient restClient;
    private final boolean useDummyOrg;
    private final boolean notify;
    private final String overrideUser;
    private final String overridePassword;
    public SponsoringOrgAdder(final String restUrl, final String source, final String overrideUser, final String overridePass, final boolean useDummy, final boolean notify) {
        this.restClient = new RestClient(restUrl, source);
        this.overrideUser = overrideUser;
        this.overridePassword = overridePass;
        this.useDummyOrg = useDummy;
        this.notify = notify;
    }

    public void addSponsoringOrgs() {
        //TODO parse input file(s), dummy for now. Expect failures, the dummy objects might not exist.
        for (ParseResult parseResult : dummyParse()) {
            RpslObject rpslObject;
            try {
                rpslObject = restClient.request().lookup(parseResult.objectType, parseResult.pkey);
            } catch (RestClientException e) {
                LOGGER.info("Entry {} not found in the db, skipping.", parseResult.pkey);
                continue;
            }

            if (rpslObject.containsAttribute(AttributeType.ORG)) {
                final RpslObject referencedOrganisation = restClient.request().lookup(ObjectType.ORGANISATION, rpslObject.getValueForAttribute(AttributeType.ORG).toString());
                if (OrgType.getFor(referencedOrganisation.getValueForAttribute(AttributeType.ORG_TYPE)) == OrgType.LIR) {
                    LOGGER.info("Skipping {}, it's referencing an org of org-type LIR", parseResult.pkey);
                    continue;
                }
            }

            final String sponsoringOrgValue = useDummyOrg ? DUMMY_ORG : parseResult.sponsoringOrg;

            try {
                restClient.request()
                        .addParam("override", String.format("%s,%s {notify=%s}", overrideUser, overridePassword, notify))
                        .update(new RpslObjectBuilder(rpslObject).addAttributeSorted(new RpslAttribute(AttributeType.SPONSORING_ORG, sponsoringOrgValue)).get());
            } catch (RestClientException e) {
                LOGGER.info("Failed {}, {}", parseResult.pkey, e.toString());
            }
        }
    }


    private List<ParseResult> dummyParse() {
        return Lists.newArrayList(
                new ParseResult(ObjectType.AUT_NUM, "AS101111", "ORG-TEST1-RIPE"),
                new ParseResult(ObjectType.INETNUM, "10.11.13.0/30", "ORG-TEST2-RIPE"),
                new ParseResult(ObjectType.INET6NUM, "2001:db8:60::/48", "ORG-TEST3-RIPE")
        );
    }

    private class ParseResult {
        private final ObjectType objectType;
        private final String pkey;
        private final String sponsoringOrg;

        private ParseResult(final ObjectType objectType, final String pkey, final String sponsoringOrg) {
            this.objectType = objectType;
            this.pkey = pkey;
            this.sponsoringOrg = sponsoringOrg;
        }
    }
}