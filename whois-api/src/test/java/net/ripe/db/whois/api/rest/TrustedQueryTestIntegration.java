package net.ripe.db.whois.api.rest;

import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.domain.IpRanges;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@Category(IntegrationTest.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class TrustedQueryTestIntegration extends AbstractIntegrationTest {

    @Autowired IpRanges ipRanges;

    @Before
    public void setup() {
        databaseHelper.addObject(
                "person:    Test Person\n" +
                "nic-hdl:   TP1-TEST\n" +
                "source:    TEST");
        databaseHelper.addObject(
                "mntner:    OWNER-MNT\n" +
                "source:    TEST");
        databaseHelper.addObject(
                "aut-num:   AS102\n" +
                "source:    TEST\n");
        databaseHelper.addObject(RpslObject.parse("" +
                "organisation: ORG-SPONSOR\n" +
                "org-name:     Sponsoring Org Ltd\n" +
                "org-type:     LIR\n" +
                "descr:        test org\n" +
                "address:      street 5\n" +
                "e-mail:       org1@test.com\n" +
                "mnt-ref:      OWNER-MNT\n" +
                "mnt-by:       OWNER-MNT\n" +
                "changed:      dbtest@ripe.net 20120505\n" +
                "source:       TEST\n" +
                ""));
        databaseHelper.addObject(RpslObject.parse("" +
                "organisation: ORG-RIPE\n" +
                "org-name:     Test Organisation Ltd\n" +
                "org-type:     LIR\n" +
                "descr:        test org\n" +
                "address:      street 5\n" +
                "e-mail:       org1@test.com\n" +
                "mnt-ref:      OWNER-MNT\n" +
                "mnt-by:       OWNER-MNT\n" +
                "changed:      dbtest@ripe.net 20120505\n" +
                "source:       TEST\n" +
                ""));
        databaseHelper.addObject(RpslObject.parse("" +
                "inetnum:       194.0.0.0 - 194.255.255.255\n" +
                "org:           ORG-RIPE\n" +
                "sponsoring-org: ORG-SPONSOR\n" +
                "netname:       TEST-NET\n" +
                "descr:         description\n" +
                "country:       NL\n" +
                "admin-c:       TP1-TEST\n" +
                "tech-c:        TP1-TEST\n" +
                "status:        ALLOCATED PA\n" +
                "mnt-by:        OWNER-MNT\n" +
                "mnt-lower:     OWNER-MNT\n" +
                "changed:       ripe@test.net 20120505\n" +
                "source:        TEST\n"));
        databaseHelper.addObject(RpslObject.parse(
                "mntner:        SSO-MNT\n" +
                "descr:         description\n" +
                "admin-c:       TP1-TEST\n" +
                "upd-to:        noreply@ripe.net\n" +
                "auth:          SSO person@net.net\n" +
                "mnt-by:        SSO-MNT\n" +
                "referral-by:   SSO-MNT\n" +
                "changed:       noreply@ripe.net 20120801\n" +
                "source:        TEST"));
    }

    // inverse lookup on sponsoring org attribute

    @Test
    public void inverse_lookup_sponsoring_org_from_untrusted_range_returns_empty() {
        ipRanges.setTrusted("1/8");

        try {
            RestTest.target(getPort(), "whois/search?query-string=ORG-SPONSOR&inverse-attribute=sponsoring-org").request().get(String.class);
            fail();
        } catch (NotFoundException e) {
            // TODO: this should be 400 bad request really
            final String response = e.getResponse().readEntity(String.class);
            assertThat(response, containsString("attribute is not searchable"));
            assertThat(response, containsString("is not an inverse searchable attribute"));
        }
    }

    @Test
    public void inverse_lookup_sponsoring_org_from_untrusted_range_succeeds() {
        ipRanges.setTrusted("127/8","::1");
        final String response = RestTest.target(getPort(), "whois/search?query-string=ORG-SPONSOR&inverse-attribute=sponsoring-org").request().get(String.class);
        assertThat(response, containsString("<attribute name=\"inetnum\" value=\"194.0.0.0 - 194.255.255.255\"/>"));
    }

    // inverse lookup on auth sso attribute

    @Test
    public void inverse_lookup_auth_sso_from_trusted_range() throws Exception {
        ipRanges.setTrusted("127/8", "::1");

        final WhoisResources whoisResources = RestTest.target(getPort(),
                "whois/search?query-string=SSO%20906635c2-0405-429a-800b-0602bd716124&inverse-attribute=auth&flags=rB")
                .request()
                .get(WhoisResources.class);

        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        assertThat(whoisResources.getWhoisObjects().get(0).getPrimaryKey().get(0).toString(), is("mntner: SSO-MNT"));
    }

    @Test
    public void inverse_lookup_auth_sso_from_untrusted_range() {
        ipRanges.setTrusted("::0");

        try {
            RestTest.target(getPort(), "whois/search?query-string=SSO%20906635c2-0405-429a-800b-0602bd716124&inverse-attribute=auth").request().get(String.class);
            fail();
        } catch (BadRequestException e) {
            RestTest.assertOnlyErrorMessage(e, "Error", "Inverse search on 'auth' attribute is limited to 'key-cert' objects only");
        }
    }
}
