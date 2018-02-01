package net.ripe.db.whois.api.rest;

import com.google.common.collect.Lists;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.mapper.FormattedClientAttributeMapper;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectMapper;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.dns.DnsGatewayStub;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@Category(IntegrationTest.class)
public class DomainObjectServiceTestIntegration extends AbstractIntegrationTest {

    @Autowired
    private WhoisObjectMapper whoisObjectMapper;

    @Autowired
    private DnsGatewayStub dnsGatewayStub;

    @Before
    public void setup() {
        databaseHelper.addObject("" +
                "person:        Jaap Knasterhuis\n" +
                "nic-hdl:       JAAP-TEST\n" +
                "source:        TEST");

        databaseHelper.addObject("" +
                "mntner:        TEST-MNT\n" +
                "descr:         Test Maintainer\n" +
                "admin-c:       JAAP-TEST\n" +
                "auth:          SSO person@net.net\n" +
                "auth:          MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "mnt-by:        TEST-MNT\n" +
                "source:        TEST");

        databaseHelper.addObject("" +
                "mntner:        TEST2-MNT\n" +
                "descr:         XS4ALL Maintainer\n" +
                "admin-c:       JAAP-TEST\n" +
                "auth:          SSO person@net.net\n" +
                "auth:          MD5-PW $1$Y8rG1Ste$QzFB.VSD5mDy7nlb/BJix/ #TEST2-MNT\n" +
                "mnt-by:        TEST-MNT\n" +
                "source:        TEST");

        databaseHelper.updateObject("" +
                "person:        Jaap Veldhuis\n" +
                "e-mail:        jaapx@ripe.net\n" +
                "address:       Hoofdstraat 1\n" +
                "phone:         +31 6 12345678\n" +
                "nic-hdl:       JAAP-TEST\n" +
                "mnt-by:        TEST-MNT\n" +
                "mnt-by:        TEST2-MNT\n" +
                "source:        TEST"
        );
    }

    @Test
    public void create_multiple_domain_objects_success() {
        databaseHelper.addObject("" +
                "inet6num:      2a01:500::/22\n" +
                "mnt-by:        TEST-MNT\n" +
                "mnt-domains:   TEST-MNT\n" +
                "mnt-domains:   TEST2-MNT\n" +
                "source:        TEST");

        final List<RpslObject> domains = Lists.newArrayList();

        for (int i = 4; i < 8; i++) {
            final RpslObject domain = RpslObject.parse(String.format("" +
                    "domain:        %d.0.1.0.a.2.ip6.arpa\n" +
                    "descr:         Reverse delegation for 2a01:500::/22\n" +
                    "admin-c:       JAAP-TEST\n" +
                    "tech-c:        JAAP-TEST\n" +
                    "zone-c:        JAAP-TEST\n" +
                    "nserver:       ns1.example.com\n" +
                    "nserver:       ns2.example.com\n" +
                    "mnt-by:        TEST-MNT\n" +
                    "source:        TEST", i));

            domains.add(domain);
        }

        final WhoisResources response = RestTest.target(getPort(), "whois/domain-objects/TEST")
                .request()
                .cookie("crowd.token_key", "valid-token")
                .post(Entity.entity(mapRpslObjects(domains.toArray(new RpslObject[0])), MediaType.APPLICATION_JSON_TYPE), WhoisResources.class);

        RestTest.assertErrorCount(response, 0);
        assertThat(response.getWhoisObjects(), hasSize(4));
    }

    @Test
    public void create_multiple_domain_objects_fail_bad_domain_syntacs() {
        databaseHelper.addObject("" +
                "inet6num:      2a01:500::/21\n" +
                "mnt-by:        TEST-MNT\n" +
                "mnt-domains:   TEST-MNT\n" +
                "mnt-domains:   TEST2-MNT\n" +
                "source:        TEST");
        final RpslObject domain = RpslObject.parse("" +
                    "domain:        bug!-4.0.1.0.a.2.ip6.arpa\n" +
                    "descr:         Reverse delegation for 2a01:500::/22\n" +
                    "admin-c:       JAAP-TEST\n" +
                    "tech-c:        JAAP-TEST\n" +
                    "zone-c:        JAAP-TEST\n" +
                    "nserver:       ns1.example.com\n" +
                    "nserver:       ns2.example.com\n" +
                    "mnt-by:        TEST-MNT\n" +
                    "source:        TEST");

        try {
            RestTest.target(getPort(), "whois/domain-objects/TEST")
                    .request()
                    .cookie("crowd.token_key", "valid-token")
                    .post(Entity.entity(mapRpslObjects(new RpslObject[]{domain}), MediaType.APPLICATION_JSON_TYPE), WhoisResources.class);
            fail();
        } catch (BadRequestException e) {
            final WhoisResources response = e.getResponse().readEntity(WhoisResources.class);

            RestTest.assertErrorCount(response, 1);
            RestTest.assertErrorMessage(response, 0, "Error", "Syntax error in %s", "bug!-4.0.1.0.a.2.ip6.arpa");
        }
    }

    @Test
    public void create_domain_object_fail_non_existent_mntner() {
        databaseHelper.addObject("" +
                "inet6num:      1a00:fb8::/23\n" +
                "mnt-by:        TEST-MNT\n" +
                "mnt-domains:   TEST2-MNT\n" +
                "source:        TEST");
        final RpslObject domain = RpslObject.parse("" +
                "domain:        e.0.0.0.a.1.ip6.arpa\n" +
                "descr:         Reverse delegation for 1a00:fb8::/23\n" +
                "admin-c:       JAAP-TEST\n" +
                "tech-c:        JAAP-TEST\n" +
                "zone-c:        JAAP-TEST\n" +
                "nserver:       ns1.example.com\n" +
                "nserver:       ns2.example.com\n" +
                "mnt-by:        NON-EXISTING-MNT\n" +
                "source:        TEST");

        try {
            RestTest.target(getPort(), "whois/domain-objects/TEST")
                    .request()
                    .cookie("crowd.token_key", "valid-token")
                    .post(Entity.entity(mapRpslObjects(new RpslObject[]{domain}), MediaType.APPLICATION_JSON_TYPE), WhoisResources.class);
            fail();
        } catch (NotAuthorizedException e) {
            final WhoisResources response = e.getResponse().readEntity(WhoisResources.class);

            RestTest.assertErrorCount(response, 3);

            RestTest.assertErrorMessage(response, 0, "Error", "Authorisation for [%s] %s failed\nusing \"%s:\"\nno valid maintainer found\n",
                    "domain", "e.0.0.0.a.1.ip6.arpa", "mnt-by", ""); // todo: [TK] suspicious extra (empty) argument
            RestTest.assertErrorMessage(response, 1, "Error", "The maintainer '%s' was not found in the database", "NON-EXISTING-MNT");
            RestTest.assertErrorMessage(response, 2, "Error", "Unknown object referenced %s", "NON-EXISTING-MNT");
        }
    }

    @Test
    public void create_domain_object_fail_dns_timeout() {
        databaseHelper.addObject("" +
                "inet6num:      1a00:fb8::/23\n" +
                "mnt-by:        TEST-MNT\n" +
                "mnt-domains:   TEST2-MNT\n" +
                "source:        TEST");
        final RpslObject domain = RpslObject.parse("" +
                "domain:        e.0.0.0.a.1.ip6.arpa\n" +
                "descr:         Reverse delegation for 1a00:fb8::/23\n" +
                "admin-c:       JAAP-TEST\n" +
                "tech-c:        JAAP-TEST\n" +
                "zone-c:        JAAP-TEST\n" +
                "nserver:       ns1.xs4all.nl\n" +
                "nserver:       ns2.xs4all.nl\n" +
                "mnt-by:        NON-EXISTING-MNT\n" +
                "source:        TEST");

        try {
            dnsGatewayStub.setProduceTimeouts(true);

            RestTest.target(getPort(), "whois/domain-objects/TEST")
                    .request()
                    .cookie("crowd.token_key", "valid-token")
                    .post(Entity.entity(mapRpslObjects(new RpslObject[]{domain}), MediaType.APPLICATION_JSON_TYPE), WhoisResources.class);
            fail();
        } catch (BadRequestException e) {
            final WhoisResources response = e.getResponse().readEntity(WhoisResources.class);

            RestTest.assertErrorCount(response, 1);
            RestTest.assertErrorMessage(response, 0, "Error", "Timeout performing DNS check");
        } finally {
            dnsGatewayStub.setProduceTimeouts(false);
        }
    }

    @Test
    public void create_domain_object_unparsable_json() {
        databaseHelper.addObject("" +
                "inetnum:      33.33.33.0/24\n" +
                "mnt-by:        TEST-MNT\n" +
                "mnt-domains:   TEST2-MNT\n" +
                "source:        TEST");

        try {
            RestTest.target(getPort(), "whois/domain-objects/TEST")
                    .request()
                    .cookie("crowd.token_key", "valid-token")
                    .post(Entity.entity("{ \"objects\": { \"object\": [ bad syntacs right here! ]}}", MediaType.APPLICATION_JSON_TYPE), WhoisResources.class);
            fail();
        } catch (BadRequestException e) {
            final WhoisResources response = e.getResponse().readEntity(WhoisResources.class);
            RestTest.assertErrorCount(response, 1);
            RestTest.assertErrorMessage(response, 0, "Error", "JSON processing exception: %s (line: %s, column: %s)",
                    "Unrecognized token 'bad': was expecting", "1", "28");
        }
    }

    private WhoisResources mapRpslObjects(final RpslObject... rpslObjects) {
        return whoisObjectMapper.mapRpslObjects(FormattedClientAttributeMapper.class, rpslObjects);
    }
}
