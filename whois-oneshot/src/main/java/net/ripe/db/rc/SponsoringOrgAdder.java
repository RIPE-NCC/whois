package net.ripe.db.rc;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.ripe.db.LogUtil;
import net.ripe.db.whois.api.rest.client.RestClient;
import net.ripe.db.whois.api.rest.client.RestClientException;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.common.rpsl.attrs.OrgType;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.PatternLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

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
     * --rest-url - the rest api url to use, defaults to 'https://rest-test.db.ripe.net'. Required.
     * --rest-source - the rest source to use, defaults to 'RIPE'. Required.
     * --override-user - override username. Required.
     * --override-pass - override password. Required.
     * --dummy-org true/false - sets the sponsoring-org to 'ORG-DUMMY-RIPE'. Defaults to 'false'.
     * --notify - sends notifications
     *
     * Example call, use a dummy organisation with the value ORG-DUMMY-RIPE, and do not send notifications:
     * --rest-url https://rest-test.db.ripe.net --rest-source TEST --override-user user --override-pass password --dummy-org true
     *
     * The input file is expected to have the formats
     * ipv4: 127.0.0.0-127.0.0.1   - note the lack of space between dash and IPs
     * ipv6: 1000:200:3000::/32
     * autnum: 1234
     * One entry per line.
     *
     * Keep in mind that the override user must be able to update ipv4, ipv6, autnum & organisation.
     * Logs are written to file in var/log/SponsoringOrgAdder.log
     */
    public static void main(final String[] args) {
        setupLogging();
        final OptionSet options = setupOptionParser().parse(args);
        final String url = (String)options.valueOf(ARG_REST_URL);
        final String source = (String)options.valueOf(ARG_REST_SOURCE);
        final String overrideUser = (String)options.valueOf(ARG_OVERRIDE_USER);
        final String overridePass = (String)options.valueOf(ARG_OVERRIDE_PASS);
        final boolean useDummy = Boolean.parseBoolean((String)options.valueOf(ARG_USE_DUMMY_ORG));
        final boolean useNotify = options.hasArgument(ARG_NOTIFY);

        new SponsoringOrgAdder(url, source, overrideUser, overridePass, useDummy, useNotify).addSponsoringOrgs();
    }

    private static void setupLogging() {
        LogUtil.initLogger();
        final FileAppender fa = new FileAppender();
        fa.setFile("var/log/SponsoringOrgAdder.log");
        fa.setLayout(new PatternLayout("%d [%C{1}] %m%n"));
        fa.setThreshold(Level.INFO);
        fa.activateOptions();

        LogManager.getRootLogger().addAppender(fa);
    }

    private static OptionParser setupOptionParser() {
        final OptionParser parser = new OptionParser();
        parser.accepts(ARG_REST_URL).withRequiredArg().defaultsTo("https://rest-test.db.ripe.net").required();
        parser.accepts(ARG_REST_SOURCE).withRequiredArg().defaultsTo("RIPE").required();
        parser.accepts(ARG_OVERRIDE_USER).withRequiredArg().required();
        parser.accepts(ARG_OVERRIDE_PASS).withRequiredArg().required();
        parser.accepts(ARG_USE_DUMMY_ORG).withOptionalArg().defaultsTo("false");
        parser.accepts(ARG_NOTIFY);
        return parser;
    }


    private String DUMMY_ORG = "ORG-DUMMY-RIPE";
    private final RestClient restClient;
    private final boolean notify;
    private final String overrideUser;
    private final String overridePassword;

    public SponsoringOrgAdder(final String restUrl, final String source, final String overrideUser, final String overridePass, final boolean useDummy, final boolean notify) {
        this.restClient = new RestClient(restUrl, source);
        this.overrideUser = overrideUser;
        this.overridePassword = overridePass;
        this.notify = notify;

        if (useDummy) {
            setupDummy();
        }
    }

    public void addSponsoringOrgs() {
        try (BufferedReader in = new BufferedReader(new FileReader("sponsoredResources.txt"))) {
            String line;
            while ((line = in.readLine()) != null) {
                if (line.contains(",")) {
                    LOGGER.info("This shouldn't be happening {}", line);
                } else {
                    line = line.trim();
                    if (line.contains("::")) {
                        addSponsoringOrg(ObjectType.INET6NUM, line, DUMMY_ORG); //TODO change the dummy org to parsed value when available
                    } else if (line.contains(".")) {
                        addSponsoringOrg(ObjectType.INETNUM, line, DUMMY_ORG);
                    } else {
                        final String AUTNUM_PREFIX = "AS";
                        addSponsoringOrg(ObjectType.AUT_NUM, AUTNUM_PREFIX + line, DUMMY_ORG);
                    }
                }
            }
        } catch (IOException ex) {
            LOGGER.info(ex.toString());
        }
    }

    private void setupDummy() {
        //TODO change the dummy organisation object to fit the environment you're in, just make sure the org-type is LIR
        try {
            final RpslObject result = restClient.request()
                    .addParam("override", String.format("%s,%s,dummyOrg{notify=%s}", overrideUser, overridePassword, notify))
                    .create(RpslObject.parse("" +
                            "organisation: AUTO-1\n" +
                            "org-name: Dummy Organisation For SponsoringOrg Test\n" +
                            "org-type: LIR\n" +
                            "address: Street\n" +
                            "e-mail: test@ripe.net\n" +
                            "mnt-ref: TEST-DBM-MNT\n" +
                            "mnt-by: TEST-DBM-MNT\n" +
                            "changed: test@test.net\n" +
                            "source: TEST"));
            DUMMY_ORG = result.getKey().toString();
        } catch (RestClientException e) {
            LOGGER.info("Could not create dummy org {}", DUMMY_ORG);
            throw e;
        }
    }

    private void addSponsoringOrg(final ObjectType objectType, final String key, final String sponsoringOrg) {
        RpslObject rpslObject;
        try {
            rpslObject = restClient.request().addParam("unformatted", "true").addParam("unfiltered", "true").lookup(objectType, key);
        } catch (RestClientException e) {
            LOGGER.info("Entry {} not found in the db, skipping.", key);
            return;
        }

        if (rpslObject.containsAttribute(AttributeType.ORG)) {
            final RpslObject referencedOrganisation = restClient.request().lookup(ObjectType.ORGANISATION, rpslObject.getValueForAttribute(AttributeType.ORG).toString());
            if (OrgType.getFor(referencedOrganisation.getValueForAttribute(AttributeType.ORG_TYPE)) == OrgType.LIR) {
                LOGGER.info("Skipping {}, it's referencing an org of org-type LIR", key);
                return;
            }
        }

        try {
            final RpslObjectBuilder objectBuilder =
                    new RpslObjectBuilder(rpslObject)
                            .removeAttributeType(AttributeType.SPONSORING_ORG)
                            .addAttributeSorted(new RpslAttribute(AttributeType.SPONSORING_ORG, sponsoringOrg));

            restClient.request()
                    .addParam("override", String.format("%s,%s,bulkaddSponsoringOrg{notify=%s}", overrideUser, overridePassword, notify))
                            .update(objectBuilder.get());
            LOGGER.info("Success, updated {}", key);
        } catch (RestClientException e) {
            LOGGER.info("Failed {}, {}", key, e.toString());
        }
    }
}
