package net.ripe.db.whois.internal.api.sso;

import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectClientMapper;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.dao.jdbc.DatabaseHelper;
import net.ripe.db.whois.common.profiles.WhoisProfile;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.internal.AbstractInternalTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.MediaType;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@Category(IntegrationTest.class)
@ContextConfiguration(locations = {"classpath:applicationContext-internal-test.xml"}, inheritLocations = false)
public class AuthServiceTestIntegration extends AbstractInternalTest {
    @Autowired private AuthService authService;

    private ClassPathXmlApplicationContext applicationContextRest;
    private DatabaseHelper databaseHelperRest;

    @Before
    public void setUp() throws Exception {
        databaseHelper.insertApiKey(apiKey, "/api/sso", apiKey);
        WhoisProfile.setEndtoend();

        applicationContextRest = new ClassPathXmlApplicationContext("applicationContext-api-test.xml");
        databaseHelperRest = applicationContextRest.getBean(DatabaseHelper.class);
        databaseHelperRest.setup();
        databaseHelperRest.insertApiKey(apiKey, "/api/sso", "testing authservice");
    }

    @After
    public void tearDown() {
        applicationContextRest.close();
    }

    @Test
    public void no_organisations_found() throws InterruptedException {
        try {
        final WhoisResources result = RestTest.target(getPort(), "api/sso/aaaa-bbbb-cccc-dddd-1234", null, apiKey)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(WhoisResources.class);
            fail();
        } catch (NotFoundException expected) { }
    }

    @Test
    public void organisations_found() {
        databaseHelperRest.addObject("mntner: TEST-MNT\nmnt-by:TEST-MNT\nauth: SSO aaaa-bbbb-cccc-dddd-1234");
        final RpslObject organisation = RpslObject.parse("" +
                "organisation: ORG-TST-TEST\n" +
                "mnt-ref: TEST-MNT\n" +
                "mnt-by: TEST-MNT");
        databaseHelperRest.addObject(organisation);

        final WhoisResources result = RestTest.target(getPort(), "api/sso/aaaa-bbbb-cccc-dddd-1234", null, apiKey)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(WhoisResources.class);

        final RpslObject resultOrg = new WhoisObjectClientMapper("test.url.net").map(result.getWhoisObjects().get(0));

        assertThat(resultOrg, is(organisation));
    }
}
