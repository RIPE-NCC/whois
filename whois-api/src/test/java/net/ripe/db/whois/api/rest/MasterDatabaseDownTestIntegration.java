package net.ripe.db.whois.api.rest;

import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.Proxy;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rest.domain.Attribute;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.mapper.FormattedClientAttributeMapper;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectMapper;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kubek2k.springockito.annotations.SpringockitoContextLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import static net.ripe.db.whois.api.RpslObjectFixtures.OWNER_MNT;
import static net.ripe.db.whois.api.RpslObjectFixtures.TEST_PERSON;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(loader = SpringockitoContextLoader.class, locations = {"classpath:applicationContext-api-test.xml"}, inheritLocations = false)
@Category(IntegrationTest.class)
public class MasterDatabaseDownTestIntegration extends AbstractIntegrationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MasterDatabaseDownTestIntegration.class);

    @Autowired
    private WhoisObjectMapper whoisObjectMapper;

    private static Proxy proxy;

    @BeforeClass
    public static void proxyMasterDatabaseConnections() {
        proxy = new Proxy("localhost", 3306);
        proxy.start();
        final String url = System.getProperty("whois.db.master.url");
        System.setProperty("whois.db.master.url", url.replace("localhost", String.format("localhost:%d", proxy.getPort())));
    }

    @Before
    public void setup() {
        databaseHelper.addObject("person: Test Person\nnic-hdl: TP1-TEST");
        databaseHelper.addObject("role: Test Role\nnic-hdl: TR1-TEST");
        databaseHelper.addObject(OWNER_MNT);
        databaseHelper.updateObject(TEST_PERSON);
        proxy.setRunning(false);
    }

    @After
    public void after() {
        proxy.setRunning(true);
    }

    @Test
    public void rest_update_fails_when_master_is_down() throws Exception {
        final RpslObject update = new RpslObjectBuilder(TEST_PERSON)
                .replaceAttribute(TEST_PERSON.findAttribute(AttributeType.ADDRESS), new RpslAttribute(AttributeType.ADDRESS, "Amsterdam")).sort().get();

        try {
            RestTest.target(getPort(), "whois/test/person/TP1-TEST?password=test")
                    .request()
                    .put(Entity.entity(whoisObjectMapper.mapRpslObjects(FormattedClientAttributeMapper.class, update), MediaType.APPLICATION_XML),
                            WhoisResources.class);
            fail();
        } catch (BadRequestException e) {
            // TODO: [ES] expect 500 Internal Server Error response ?
            final String response = e.getResponse().readEntity(String.class);
            assertThat(response, containsString("Unexpected error occurred"));
        }
    }

    @Test
    public void rest_lookup_succeeds_when_master_is_down() throws Exception {
        final WhoisResources response = RestTest.target(getPort(), "whois/test/person/TP1-TEST").request().get(WhoisResources.class);

        assertThat(response.getWhoisObjects(), hasSize(1));
        assertThat(response.getWhoisObjects().get(0).getPrimaryKey(), contains(new Attribute("nic-hdl", "TP1-TEST")));
    }

    @Test
    public void rest_search_succeeds_when_master_is_down() throws Exception {
        final WhoisResources response = RestTest.target(getPort(), "whois/search?query-string=TP1-TEST").request().get(WhoisResources.class);

        assertThat(response.getWhoisObjects(), hasSize(1));
        assertThat(response.getWhoisObjects().get(0).getPrimaryKey(), contains(new Attribute("nic-hdl", "TP1-TEST")));
    }

}
