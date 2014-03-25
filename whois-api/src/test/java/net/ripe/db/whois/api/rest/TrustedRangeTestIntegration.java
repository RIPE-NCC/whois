package net.ripe.db.whois.api.rest;

import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.domain.IpRanges;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import javax.ws.rs.ClientErrorException;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@Category(IntegrationTest.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class TrustedRangeTestIntegration extends AbstractIntegrationTest {

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
        databaseHelper.addObject("aut-num:   AS102\n" + "source:    TEST\n");

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
                "inetnum: 194.0.0.0 - 194.255.255.255\n" +
                "org: ORG-RIPE\n" +
                "sponsoring-org: ORG-SPONSOR\n" +
                "netname: TEST-NET\n" +
                "descr: description\n" +
                "country: NL\n" +
                "admin-c: TP1-TEST\n" +
                "tech-c: TP1-TEST\n" +
                "status: ALLOCATED PA\n" +
                "mnt-by: OWNER-MNT\n" +
                "mnt-lower: OWNER-MNT\n" +
                "changed: ripe@test.net 20120505\n" +
                "source: TEST\n"));
    }

    @Test
    public void lookup_from_untrusted_range_returns_empty() throws Exception {
        ipRanges.setTrusted("1/8");

        try {
            RestTest.target(getPort(), "whois/search?query-string=ORG-SPONSOR&inverse-attribute=sponsoring-org").request().get(String.class);
            fail();
        } catch (ClientErrorException e) {
            final String response = e.getResponse().readEntity(String.class);
            assertThat(response, containsString("attribute is not searchable"));
            assertThat(response, containsString("is not an inverse searchable attribute"));
            // TODO: this should be 400 bad request really
            assertThat(e.getResponse().getStatus(), is(404));
        }
    }

    @Test
    public void lookup_from_untrusted_range_succeeds() throws Exception {
        ipRanges.setTrusted("127/8","::1");
        final String response = RestTest.target(getPort(), "whois/search?query-string=ORG-SPONSOR&inverse-attribute=sponsoring-org").request().get(String.class);
        assertThat(response, containsString("<attribute name=\"inetnum\" value=\"194.0.0.0 - 194.255.255.255\"/>"));
    }

}
