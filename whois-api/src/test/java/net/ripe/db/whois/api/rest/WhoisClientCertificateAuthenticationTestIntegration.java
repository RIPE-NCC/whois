package net.ripe.db.whois.api.rest;

import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.mapper.FormattedClientAttributeMapper;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectMapper;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.TestDateTimeProvider;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.update.keycert.X509CertificateTestUtil;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import java.security.cert.X509Certificate;

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static net.ripe.db.whois.common.rpsl.ObjectType.ROUTE6;
import static net.ripe.db.whois.update.keycert.X509CertificateTestUtil.asPem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@Category(IntegrationTest.class)
public class WhoisClientCertificateAuthenticationTestIntegration extends AbstractIntegrationTest {

    private static final RpslObject OWNER_MNT = RpslObject.parse("" +
            "mntner:      OWNER-MNT\n" +
            "descr:       Owner Maintainer\n" +
            "admin-c:     TP1-TEST\n" +
            "upd-to:      noreply@ripe.net\n" +
            "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
            "auth:        SSO person@net.net\n" +
            "mnt-by:      OWNER-MNT\n" +
            "source:      TEST");

    @Autowired private WhoisObjectMapper whoisObjectMapper;
    @Autowired private TestDateTimeProvider testDateTimeProvider;

    @BeforeClass
    public static void setProperties() {
        System.setProperty("client.cert.auth.enabled", "true");
    }

    @Before
    public void setup() {
        databaseHelper.addObject("person: Test Person\nnic-hdl: TP1-TEST");
        databaseHelper.addObject("role: Test Role\nnic-hdl: TR1-TEST");
        databaseHelper.addObject(OWNER_MNT);
    }

    @Test
    public void update_route_authenticate_with_client_cert() throws Exception {

        final X509Certificate cert = X509CertificateTestUtil.generate("test-cn", testDateTimeProvider);
        final RpslObject keycert = X509CertificateTestUtil.createKeycertObject(ciString("X509-1"), cert, ciString("OWNER-MNT"));
        databaseHelper.addObject(keycert);

        final RpslObject ownerWithX509 = new RpslObjectBuilder(OWNER_MNT)
                .addAttributeSorted(new RpslAttribute(AttributeType.AUTH, keycert.getKey()))
                .get();
        databaseHelper.updateObject(ownerWithX509);

        final RpslObject route = RpslObject.parse(
            "route6:          2001::/32\n" +
            "descr:           Test route\n" +
            "origin:          AS12726\n" +
            "mnt-by:          OWNER-MNT\n" +
            "source:          TEST\n"
        );
        databaseHelper.addObject(route);

        final RpslObject updatedObject = new RpslObjectBuilder(route)
                .addAttributeSorted(new RpslAttribute(AttributeType.REMARKS, "updated"))
                .get();
        final WhoisResources updatedRoute = whoisObjectMapper.mapRpslObjects(FormattedClientAttributeMapper.class, updatedObject);

        RestTest.target(getPort(), "whois/test/route6/2001::/32AS12726")
                .request(MediaType.APPLICATION_XML)
                .header("SSL_CLIENT_CERT", asPem(cert))
                .header("SSL_CLIENT_VERIFY", "GENEROUS")
                .put(Entity.entity(updatedRoute, MediaType.APPLICATION_XML), WhoisResources.class);

        assertThat(databaseHelper.lookupObject(ROUTE6, "2001::/32AS12726").containsAttribute(AttributeType.REMARKS), is(true));
    }

}
