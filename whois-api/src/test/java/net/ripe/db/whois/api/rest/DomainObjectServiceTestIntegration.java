package net.ripe.db.whois.api.rest;

import com.google.common.collect.Lists;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.mapper.FormattedClientAttributeMapper;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectMapper;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.dao.jdbc.DatabaseHelper;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

@Category(IntegrationTest.class)
public class DomainObjectServiceTestIntegration extends AbstractIntegrationTest {

    @Autowired
    private WhoisObjectMapper whoisObjectMapper;

    @Before
    public void setup() {
        databaseHelper.addObject("" +
                "person:        Jaap Veldhuis\n" +
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
                "mntner:        XS4ALL-MNT\n" +
                "descr:         XS4ALL Maintainer\n" +
                "admin-c:       JAAP-TEST\n" +
                "auth:          SSO person@net.net\n" +
                "auth:          MD5-PW $1$Y8rG1Ste$QzFB.VSD5mDy7nlb/BJix/ #XS4ALL-MNT\n" +
                "mnt-by:        TEST-MNT\n" +
                "source:        TEST");

        databaseHelper.updateObject("" +
                "person:        Jaap Veldhuis\n" +
                "e-mail:        jaapx@ripe.net\n" +
                "address:       Hoofdstraat 1\n" +
                "phone:         +31 6 12345678\n" +
                "nic-hdl:       JAAP-TEST\n" +
                "mnt-by:        TEST-MNT\n" +
                "mnt-by:        XS4ALL-MNT\n" +
                "source:        TEST"
        );
    }

    @Test
    public void create_multiple_domain_objects_success() {

        databaseHelper.addObject("" +
                "inet6num:      2a01:500::/22\n" +
                "mnt-by:        TEST-MNT\n" +
                "mnt-domains:   TEST-MNT\n" +
                "mnt-domains:   XS4ALL-MNT\n" +
                "source:        TEST");

// uncomment line below to debug the server on a local machine
//       stopExecutionHereButKeepTheServerRunning();

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

    private WhoisResources mapRpslObjects(final RpslObject... rpslObjects) {

        return whoisObjectMapper.mapRpslObjects(FormattedClientAttributeMapper.class, rpslObjects);
    }
}
