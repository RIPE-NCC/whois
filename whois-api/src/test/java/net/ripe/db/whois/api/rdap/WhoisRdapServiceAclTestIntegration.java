package net.ripe.db.whois.api.rdap;

import net.ripe.db.whois.api.rdap.domain.Entity;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.query.acl.AccessControlListManager;
import net.ripe.db.whois.query.acl.IpResourceConfiguration;
import net.ripe.db.whois.query.support.TestPersonalObjectAccounting;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.MediaType;

import static org.junit.Assert.fail;

@Category(IntegrationTest.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class WhoisRdapServiceAclTestIntegration extends AbstractRdapIntegrationTest {

    private static final String LOCALHOST_WITH_PREFIX = "127.0.0.1/32";

    @Autowired
    private IpResourceConfiguration ipResourceConfiguration;
    @Autowired
    private TestPersonalObjectAccounting testPersonalObjectAccounting;

    @Test
    public void lookup_person_entity_acl_denied() throws Exception {
        try {
            databaseHelper.insertAclIpDenied(LOCALHOST_WITH_PREFIX);
            ipResourceConfiguration.reload();

            try {
                createResource("entity/PP1-TEST")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Entity.class);
                fail();
            } catch (ClientErrorException e) {
                assertErrorStatus(e, 429);
                assertErrorTitleContains(e, "%ERROR:201: access denied for 127.0.0.1");
            }
        } finally {
            databaseHelper.unban(LOCALHOST_WITH_PREFIX);
            ipResourceConfiguration.reload();
            testPersonalObjectAccounting.resetAccounting();
        }
    }

}
