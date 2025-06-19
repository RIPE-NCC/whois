package net.ripe.db.whois.rdap;

import jakarta.ws.rs.core.HttpHeaders;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectMapper;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.rdap.domain.Entity;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@Tag("IntegrationTest")
public class RdapRewriteEngineTestIntegration extends AbstractRdapIntegrationTest {

    @BeforeAll
    public static void enableRewriteEngine() {
        System.setProperty("rewrite.engine.enabled", "true");
    }

    @AfterAll
    public static void disableRewriteEngine() {
        System.clearProperty("rewrite.engine.enabled");
    }

    @Value("${api.rest.baseurl}")
    private String restApiBaseUrl;

    @Autowired
    WhoisObjectMapper whoisObjectMapper;

    final RpslObject person = RpslObject.parse(
            "person:        Pauleth Palthen\n" +
                    "address:       Singel 258\n" +
                    "phone:         +31-1234567890\n" +
                    "e-mail:        noreply@ripe.net\n" +
                    "mnt-by:        TEST-MNT\n" +
                    "nic-hdl:       PP1-TEST\n" +
                    "remarks:       remark\n" +
                    "created:         2022-08-14T11:48:28Z\n" +
                    "last-modified:   2022-10-25T12:22:39Z\n" +
                    "source:        TEST\n");

    @BeforeEach
    public void setup() {
        databaseHelper.addObject(
                        "person: Test Person\n" +
                        "nic-hdl: TP1-TEST\n" +
                        "created:         2022-08-14T11:48:28Z\n" +
                        "last-modified:   2022-10-25T12:22:39Z\n" +
                        "source: TEST\n");
        final RpslObject mntner = RpslObject.parse(
                "mntner:        TEST-MNT\n" +
                        "descr:         Test maintainer\n" +
                        "admin-c:       TP1-TEST\n" +
                        "upd-to:        upd-to@ripe.net\n" +
                        "mnt-nfy:       mnt-nfy@ripe.net\n" +
                        "auth:          MD5-PW $1$EmukTVYX$Z6fWZT8EAzHoOJTQI6jFJ1  # 123\n" +
                        "mnt-by:        TEST-MNT\n" +
                        "created:         2022-08-14T11:48:28Z\n" +
                        "last-modified:   2022-10-25T12:22:39Z\n" +
                        "source:        TEST"
        );
        databaseHelper.addObject(mntner);
        databaseHelper.addObject(person);
    }

    @Test
    public void rdap_lookup_with_rewrite() {
        final Entity person = RestTest.target(getPort(), "entity/TP1-TEST")
                .request()
                .header(HttpHeaders.HOST, getHost(restApiBaseUrl).replace("rest", "rdap"))
                .get(Entity.class);

        assertThat(person.getHandle(), is("TP1-TEST"));
    }

    // helper methods

    private String getHost(final String url) {
        final URI uri = URI.create(url);
        return uri.getHost();
    }

}
