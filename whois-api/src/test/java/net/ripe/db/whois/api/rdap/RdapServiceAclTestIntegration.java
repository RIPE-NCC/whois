package net.ripe.db.whois.api.rdap;

import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.core.MediaType;
import net.ripe.db.whois.api.rdap.domain.Entity;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.acl.IpResourceConfiguration;
import net.ripe.db.whois.query.support.TestPersonalObjectAccounting;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import java.net.InetAddress;
import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;


@Tag("IntegrationTest")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RdapServiceAclTestIntegration extends AbstractRdapIntegrationTest {

    private static final String LOCALHOST = "127.0.0.1";
    private static final String LOCALHOST_WITH_PREFIX = "127.0.0.1/32";

    private static final RpslObject PAULETH_PALTHEN = RpslObject.parse("" +
            "person:    Pauleth Palthen\n" +
            "address:   Singel 258\n" +
            "phone:     +31-1234567890\n" +
            "fax-no:    +31-1234567891\n" +
            "e-mail:    noreply@ripe.net\n" +
            "mnt-by:    OWNER-MNT\n" +
            "nic-hdl:   PP1-TEST\n" +
            "remarks:   remark\n" +
            "created:        2001-02-04T17:00:00Z\n" +
            "last-modified:  2001-02-04T17:00:00Z\n" +
            "source:    TEST\n");

    private static final RpslObject OWNER_MNT = RpslObject.parse("" +
            "mntner:      OWNER-MNT\n" +
            "descr:       Owner Maintainer\n" +
            "admin-c:     TP1-TEST\n" +
            "e-mail:      owner@ripe.net\n" +
            "upd-to:      noreply@ripe.net\n" +
            "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
            "auth:        SSO person@net.net\n" +
            "mnt-by:      OWNER-MNT\n" +
            "created:        2001-02-04T17:00:00Z\n" +
            "last-modified:  2001-02-04T17:00:00Z\n" +
            "source:      TEST");

    private static final RpslObject TEST_PERSON = RpslObject.parse("" +
            "person:    Test Person\n" +
            "address:   Singel 258\n" +
            "phone:     +31 6 12345678\n" +
            "e-mail:    test@ripe.net\n" +
            "nic-hdl:   TP1-TEST\n" +
            "mnt-by:    OWNER-MNT\n" +
            "created:        2001-02-04T17:00:00Z\n" +
            "last-modified:  2001-02-04T17:00:00Z\n" +
            "source:    TEST\n");

    @Autowired
    private IpResourceConfiguration ipResourceConfiguration;
    @Autowired
    private TestPersonalObjectAccounting testPersonalObjectAccounting;

    @BeforeEach
    public void setup() {
        databaseHelper.addObject("person: Test Person\nnic-hdl: TP1-TEST");
        databaseHelper.addObject(OWNER_MNT);
        databaseHelper.addObject(PAULETH_PALTHEN);
        databaseHelper.updateObject(TEST_PERSON);
    }

    @Test
    public void lookup_person_entity_acl_denied() {
        try {
            databaseHelper.insertAclIpDenied(LOCALHOST_WITH_PREFIX);
            ipResourceConfiguration.reload();

            try {
                createResource("entity/PP1-TEST")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Entity.class);
                fail();
            } catch (ClientErrorException e) {
                assertErrorStatus(e, HttpStatus.TOO_MANY_REQUESTS_429);
                assertErrorTitleContains(e, "429 Too Many Requests");
                assertErrorDescriptionContains(e, "%ERROR:201: access denied for 127.0.0.1");
            }
        } finally {
            databaseHelper.unban(LOCALHOST_WITH_PREFIX);
            ipResourceConfiguration.reload();
            testPersonalObjectAccounting.resetAccounting();
        }
    }

    @Test
    public void lookup_person_acl_counted() throws Exception {
        createResource("entity/TP1-TEST")
            .request(MediaType.APPLICATION_JSON_TYPE)
            .get(Entity.class);

        assertThat(testPersonalObjectAccounting.getQueriedPersonalObjects(InetAddress.getByName(LOCALHOST).toString()), is(1));
    }

    // TODO: [ES] mntner e-mail is not filtered and mntner is not counted in ACL
    @Test
    public void lookup_mntner_acl_not_counted() throws Exception {
        final Entity entity = createResource("entity/OWNER-MNT")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Entity.class);

        assertThat(((ArrayList)entity.getVCardArray().get(1)).get(2).toString(), is("[kind, {}, text, individual]"));
        assertThat(testPersonalObjectAccounting.getQueriedPersonalObjects(InetAddress.getByName(LOCALHOST).toString()), is(0));
    }
}
