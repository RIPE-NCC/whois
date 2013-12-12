package net.ripe.db.whois.internal.api.sso;

import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rest.domain.WhoisObject;
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
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@Category(IntegrationTest.class)
@ContextConfiguration(locations = {"classpath:applicationContext-internal-test.xml"}, inheritLocations = false)
public class AuthServiceTestIntegration extends AbstractInternalTest {

    private ClassPathXmlApplicationContext applicationContextRest;
    private DatabaseHelper databaseHelperRest;

    @Before
    public void setUp() throws Exception {
        databaseHelper.insertApiKey(apiKey, "/api/user", apiKey);
        WhoisProfile.setEndtoend();

        applicationContextRest = new ClassPathXmlApplicationContext("applicationContext-api-test.xml");
        databaseHelperRest = applicationContextRest.getBean(DatabaseHelper.class);
        databaseHelperRest.setup();
        databaseHelperRest.insertApiKey(apiKey, "/api/user", "testing authservice");
    }

    @After
    public void tearDown() {
        applicationContextRest.close();
    }

    @Test
    public void no_organisations_found() throws InterruptedException {
        try {
            RestTest.target(getPort(), "api/user/aaaa-bbbb-cccc-dddd-1234/organisations", null, apiKey)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(WhoisResources.class);
            fail();
        } catch (NotFoundException expected) { }
    }

    @Test
    public void organisations_found_via_mnt_by() {
        databaseHelperRest.addObject("mntner: TEST-MNT\nmnt-by:TEST-MNT\nauth: SSO aaaa-bbbb-cccc-dddd-1234");
        final RpslObject organisation = RpslObject.parse("" +
                "organisation: ORG-TST-TEST\n" +
                "mnt-by: TEST-MNT\n" +
                "source: TEST");
        databaseHelperRest.addObject(organisation);

        final WhoisResources result = RestTest.target(getPort(), "api/user/aaaa-bbbb-cccc-dddd-1234/organisations", null, apiKey)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(WhoisResources.class);

        final RpslObject resultOrg = new WhoisObjectClientMapper("test.url").map(result.getWhoisObjects().get(0));

        assertThat(resultOrg, is(organisation));
    }

    @Test
    public void organisations_not_found_via_mnt_ref() {
        databaseHelperRest.addObject("mntner: TEST-MNT\nmnt-by:TEST-MNT\nauth: SSO aaaa-bbbb-cccc-dddd-1234");
        final RpslObject organisation = RpslObject.parse("" +
                "organisation: ORG-TST-TEST\n" +
                "mnt-ref: TEST-MNT\n" +
                "source: TEST");
        databaseHelperRest.addObject(organisation);
        try {
            RestTest.target(getPort(), "api/user/aaaa-bbbb-cccc-dddd-1234/organisations", null, apiKey)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(WhoisResources.class);
            fail();
        } catch (NotFoundException expected) {}
    }

    @Test
    public void organisation_only_added_once() {
        databaseHelperRest.addObject("mntner: TEST-MNT\nmnt-by:TEST-MNT\nauth: SSO aaaa-bbbb-cccc-dddd-1234");
        final RpslObject organisation = RpslObject.parse("" +
                "organisation: ORG-TST-TEST\n" +
                "mnt-by: TEST-MNT\n" +
                "mnt-by: TEST-MNT\n" +
                "source: TEST");
        databaseHelperRest.addObject(organisation);

        final WhoisResources result = RestTest.target(getPort(), "api/user/aaaa-bbbb-cccc-dddd-1234/organisations", null, apiKey)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(WhoisResources.class);

        final List<WhoisObject> whoisObjects = result.getWhoisObjects();
        assertThat(whoisObjects, hasSize(1));

        final RpslObject resultOrg = new WhoisObjectClientMapper("test.url").map(whoisObjects.get(0));

        assertThat(resultOrg, is(organisation));
    }

    @Test
    public void only_organisations_returned() {
        databaseHelperRest.addObject("mntner: TEST-MNT\nmnt-by:TEST-MNT\nauth: SSO aaaa-bbbb-cccc-dddd-1234");
        final RpslObject organisation = RpslObject.parse("" +
                "organisation: ORG-TST-TEST\n" +
                "mnt-ref: TEST-MNT\n" +
                "mnt-by: TEST-MNT\n" +
                "source: TEST");
        databaseHelperRest.addObject(organisation);
        databaseHelperRest.addObject("person: Test Person\nnic-hdl: TST-TEST\nmnt-by: TEST-MNT");

        final WhoisResources result = RestTest.target(getPort(), "api/user/aaaa-bbbb-cccc-dddd-1234/organisations", null, apiKey)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(WhoisResources.class);

        final List<WhoisObject> whoisObjects = result.getWhoisObjects();
        assertThat(whoisObjects, hasSize(1));

        final RpslObject resultOrg = new WhoisObjectClientMapper("test.url").map(whoisObjects.get(0));

        assertThat(resultOrg, is(organisation));
    }

    @Test
    public void only_auth_sso_mntners_yield_results() {
        databaseHelperRest.addObject("mntner: SSO-MNT\nmnt-by:SSO-MNT\nauth: SSO aaaa-bbbb-cccc-dddd-1234");
        databaseHelperRest.addObject("mntner: MD5-MNT\nmnt-by:MD5-MNT\nauth: MD5-PW aaaa-bbbb-cccc-dddd-1234");
        final RpslObject organisation = RpslObject.parse("" +
                "organisation: ORG-SSO-TEST\n" +
                "mnt-ref: SSO-MNT\n" +
                "mnt-by: SSO-MNT\n" +
                "source: TEST");
        databaseHelperRest.addObject(organisation);
        databaseHelperRest.addObject("" +
                "organisation: ORG-MD5-TEST\n" +
                "mnt-ref: MD5-MNT\n" +
                "mnt-by: MD5-MNT\n" +
                "source: TEST");

        final WhoisResources result = RestTest.target(getPort(), "api/user/aaaa-bbbb-cccc-dddd-1234/organisations", null, apiKey)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(WhoisResources.class);

        final List<WhoisObject> whoisObjects = result.getWhoisObjects();
        assertThat(whoisObjects, hasSize(1));

        final RpslObject resultOrg = new WhoisObjectClientMapper("test.url").map(whoisObjects.get(0));

        assertThat(resultOrg, is(organisation));
    }

    @Test
    public void all_organisations_returned() {
        databaseHelperRest.addObject("mntner: TEST-MNT\nmnt-by:TEST-MNT\nauth: SSO aaaa-bbbb-cccc-dddd-1234");
        final RpslObject org1 = RpslObject.parse("" +
                "organisation: ORG-TST1-TEST\n" +
                "mnt-by: TEST-MNT\n" +
                "source: TEST");
        databaseHelperRest.addObject(org1);
        final RpslObject org2 = RpslObject.parse("" +
                "organisation: ORG-MD5-TEST\n" +
                "mnt-by: TEST-MNT\n" +
                "source: TEST");
        databaseHelperRest.addObject(org2);

        final WhoisResources result = RestTest.target(getPort(), "api/user/aaaa-bbbb-cccc-dddd-1234/organisations", null, apiKey)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(WhoisResources.class);

        final List<WhoisObject> whoisObjects = result.getWhoisObjects();
        assertThat(whoisObjects, hasSize(2));

        final WhoisObjectClientMapper objectMapper = new WhoisObjectClientMapper("test.url");

        assertThat(objectMapper.map(whoisObjects.get(0)), is(org2));
        assertThat(objectMapper.map(whoisObjects.get(1)), is(org1));
    }
}
