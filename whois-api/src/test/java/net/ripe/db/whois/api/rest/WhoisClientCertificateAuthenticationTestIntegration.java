package net.ripe.db.whois.api.rest;

import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.SecureRestTest;
import net.ripe.db.whois.api.httpserver.AbstractClientCertificateIntegrationTest;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.mapper.FormattedClientAttributeMapper;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectMapper;
import net.ripe.db.whois.common.dao.jdbc.DatabaseHelper;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.fail;

// TODO
@Tag("IntegrationTest")
public class WhoisClientCertificateAuthenticationTestIntegration extends AbstractClientCertificateIntegrationTest {

    private static final RpslObject OWNER_MNT = RpslObject.parse("" +
            "mntner:      OWNER-MNT\n" +
            "descr:       Owner Maintainer\n" +
            "admin-c:     TP1-TEST\n" +
            "upd-to:      noreply@ripe.net\n" +
            "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
            "auth:        SSO person@net.net\n" +
            "mnt-by:      OWNER-MNT\n" +
            "source:      TEST");
    private static final RpslObject TEST_PERSON = RpslObject.parse(
             "person:     Test Person\n" +
            "address:     Amsterdam\n" +
            "phone:       +31 6 12345678\n" +
            "nic-hdl:     TP1-TEST\n" +
            "mnt-by:      OWNER-MNT\n" +
            "source:      TEST");

    @Autowired private WhoisObjectMapper whoisObjectMapper;

    @BeforeEach
    public void setup() {
        databaseHelper.addObject("person: Test Person\nnic-hdl: TP1-TEST");
        databaseHelper.addObject(OWNER_MNT);
        databaseHelper.updateObject(TEST_PERSON);
    }

    @Test
    public void update_person_with_client_cert_unauthorised() {
        final RpslObject updatedPerson = RpslObject.parse(
            "person: Test Person\n" +
             "address: Amsterdam\n" +
             "phone: +31 6 12345678\n" +
             "remarks: updated\n" +
             "nic-hdl: TP1-TEST\n" +
             "mnt-by: OWNER-MNT\n" +
             "source: TEST");

        try {
            SecureRestTest.target(getClientSSLContext(), getSecurePort(), "whois/test/person/TP1-TEST")
                .request()
                .put(Entity.entity(map(updatedPerson), MediaType.APPLICATION_XML), WhoisResources.class);
            fail();
        } catch (NotAuthorizedException e) {
            final WhoisResources whoisResources = e.getResponse().readEntity(WhoisResources.class);
            RestTest.assertErrorCount(whoisResources, 1);
            RestTest.assertErrorMessage(whoisResources, 0, "Error", "Authorisation for [%s] %s failed\nusing \"%s:\"\nnot authenticated by: %s", "person", "TP1-TEST", "mnt-by", "OWNER-MNT");
        }
    }

    @Test
    public void update_person_with_client_cert_successful() {
        final RpslObject updatedPerson = RpslObject.parse(
            "person: Test Person\n" +
             "address: Amsterdam\n" +
             "phone: +31 6 12345678\n" +
             "remarks: updated\n" +
             "nic-hdl: TP1-TEST\n" +
             "mnt-by: OWNER-MNT\n" +
             "source: TEST");

        final RpslObject keycertObject = createKeycertObject(getClientCertificate(), "OWNER-MNT");
        databaseHelper.addObject(keycertObject);
        final RpslObject updatedMntner = addAttribute(OWNER_MNT, AttributeType.AUTH, keycertObject.getKey());
        databaseHelper.updateObject(updatedMntner);

        DatabaseHelper.dumpSchema(databaseHelper.getWhoisTemplate().getDataSource());

        final WhoisResources whoisResources = SecureRestTest.target(getClientSSLContext(), getSecurePort(), "whois/test/person/TP1-TEST")
                .request()
                .put(Entity.entity(map(updatedPerson), MediaType.APPLICATION_XML), WhoisResources.class);

    }


//    @Test
//    public void update_route_authenticate_with_client_cert() throws Exception {
//
////        final X509Certificate cert = X509CertificateUtil.generateCertificate(testDateTimeProvider);
////        final RpslObject keycert = X509CertificateUtil.createKeycertObject(ciString("X509-1"), cert, ciString("OWNER-MNT"));
////        databaseHelper.addObject(keycert);
//
////        final RpslObject ownerWithX509 = new RpslObjectBuilder(OWNER_MNT)
////                .addAttributeSorted(new RpslAttribute(AttributeType.AUTH, keycert.getKey()))
////                .get();
////        databaseHelper.updateObject(ownerWithX509);
//
//        final RpslObject route = RpslObject.parse(
//            "route6:          2001::/32\n" +
//            "descr:           Test route\n" +
//            "origin:          AS12726\n" +
//            "mnt-by:          OWNER-MNT\n" +
//            "source:          TEST\n"
//        );
//        databaseHelper.addObject(route);
//
//        final RpslObject updatedObject = new RpslObjectBuilder(route)
//                .addAttributeSorted(new RpslAttribute(AttributeType.REMARKS, "updated"))
//                .get();
//        final WhoisResources updatedRoute = whoisObjectMapper.mapRpslObjects(FormattedClientAttributeMapper.class, updatedObject);
//
//        RestTest.target(getPort(), "whois/test/route6/2001::/32AS12726")
//                .request(MediaType.APPLICATION_XML)
////                .header("SSL_CLIENT_CERT", getCertificateAsString(cert).replace('\n', ' '))
////                .header("SSL_CLIENT_VERIFY", "GENEROUS")
//                .put(Entity.entity(updatedRoute, MediaType.APPLICATION_XML), WhoisResources.class);
//
//        assertThat(databaseHelper.lookupObject(ROUTE6, "2001::/32AS12726").containsAttribute(AttributeType.REMARKS), is(true));
//    }



    // helper methods

    private WhoisResources map(final RpslObject rpslObject) {
        return whoisObjectMapper.mapRpslObjects(FormattedClientAttributeMapper.class, rpslObject);
    }

    private RpslObject addAttribute(final RpslObject rpslObject, final AttributeType attributeType, final CIString attributeValue) {
        return new RpslObjectBuilder(rpslObject).addAttributeAfter(new RpslAttribute(attributeType, attributeValue), AttributeType.SOURCE).get();
    }

}
