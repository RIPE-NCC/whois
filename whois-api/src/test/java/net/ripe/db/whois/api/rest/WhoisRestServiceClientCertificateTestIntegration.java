package net.ripe.db.whois.api.rest;

import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.SecureRestTest;
import net.ripe.db.whois.api.httpserver.AbstractClientCertificateIntegrationTest;
import net.ripe.db.whois.api.httpserver.CertificatePrivateKeyPair;
import net.ripe.db.whois.api.httpserver.WhoisKeystore;
import net.ripe.db.whois.api.rest.domain.Attribute;
import net.ripe.db.whois.api.rest.domain.Link;
import net.ripe.db.whois.api.rest.domain.WhoisObject;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.mapper.FormattedClientAttributeMapper;
import net.ripe.db.whois.api.rest.mapper.FormattedServerAttributeMapper;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectMapper;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.net.ssl.SSLContext;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;

@Tag("IntegrationTest")
public class WhoisRestServiceClientCertificateTestIntegration extends AbstractClientCertificateIntegrationTest {

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
    public void update_person_incorrect_password_auth_mntner_with_client_cert_unauthorised() {
        final RpslObject updatedPerson = RpslObject.parse(
            "person: Test Person\n" +
             "address: Amsterdam\n" +
             "phone: +31 6 12345678\n" +
             "remarks: updated\n" +
             "nic-hdl: TP1-TEST\n" +
             "mnt-by: OWNER-MNT\n" +
             "source: TEST");

        final RpslObject keycertObject = createKeycertObject(new CertificatePrivateKeyPair().getCertificate(), "OWNER-MNT");
        databaseHelper.addObject(keycertObject);
        final RpslObject updatedMntner = addAttribute(OWNER_MNT, AttributeType.AUTH, keycertObject.getKey());
        databaseHelper.updateObject(updatedMntner);

        try {
            RestTest.target(getPort(), "whois/test/person/TP1-TEST?password=invalid")
                .request()
                .put(Entity.entity(map(updatedPerson), MediaType.APPLICATION_XML), WhoisResources.class);
            fail();
        } catch (NotAuthorizedException e) {
            final WhoisResources whoisResources = e.getResponse().readEntity(WhoisResources.class);
            RestTest.assertErrorCount(whoisResources, 1);
            RestTest.assertErrorMessage(whoisResources, 0, "Error",
                "Authorisation for [%s] %s failed\nusing \"%s:\"\n" +
                 "not authenticated by: %s", "person", "TP1-TEST", "mnt-by", "OWNER-MNT");
        }
    }

    @Test
    public void update_person_correct_password_auth_mntner_with_client_cert_successful() {
        final RpslObject updatedPerson = RpslObject.parse(
            "person: Test Person\n" +
             "address: Amsterdam\n" +
             "phone: +31 6 12345678\n" +
             "remarks: updated\n" +
             "nic-hdl: TP1-TEST\n" +
             "mnt-by: OWNER-MNT\n" +
             "source: TEST");

        final RpslObject keycertObject = createKeycertObject(new CertificatePrivateKeyPair().getCertificate(), "OWNER-MNT");
        databaseHelper.addObject(keycertObject);
        final RpslObject updatedMntner = addAttribute(OWNER_MNT, AttributeType.AUTH, keycertObject.getKey());
        databaseHelper.updateObject(updatedMntner);

        final WhoisResources whoisResources = SecureRestTest.target(getClientSSLContext(),getClientCertificatePort(),"whois/test/person/TP1-TEST?password=test")
            .request()
            .put(Entity.entity(map(updatedPerson), MediaType.APPLICATION_XML), WhoisResources.class);

        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        assertThat(whoisResources.getWhoisObjects().get(0).getAttributes().get(3).getValue(), containsString("updated"));
    }

    @Test
    public void update_person_with_client_cert_no_mntner_cert_unauthorised() {
        final RpslObject updatedPerson = RpslObject.parse(
            "person: Test Person\n" +
             "address: Amsterdam\n" +
             "phone: +31 6 12345678\n" +
             "remarks: updated\n" +
             "nic-hdl: TP1-TEST\n" +
             "mnt-by: OWNER-MNT\n" +
             "source: TEST");

        try {
            SecureRestTest.target(getClientSSLContext(), getClientCertificatePort(), "whois/test/person/TP1-TEST")
                .request()
                .put(Entity.entity(map(updatedPerson), MediaType.APPLICATION_XML), WhoisResources.class);
            fail();
        } catch (NotAuthorizedException e) {
            final WhoisResources whoisResources = e.getResponse().readEntity(WhoisResources.class);
            RestTest.assertErrorCount(whoisResources, 1);
            RestTest.assertErrorMessage(whoisResources, 0, "Error",
                "Authorisation for [%s] %s failed\nusing \"%s:\"\n" +
                 "not authenticated by: %s", "person", "TP1-TEST", "mnt-by", "OWNER-MNT");
        }
    }

    @Test
    public void update_person_with_client_cert_auth_different_mntner_cert_unauthorised() {
        final RpslObject updatedPerson = RpslObject.parse(
            "person: Test Person\n" +
             "address: Amsterdam\n" +
             "phone: +31 6 12345678\n" +
             "remarks: updated\n" +
             "nic-hdl: TP1-TEST\n" +
             "mnt-by: OWNER-MNT\n" +
             "source: TEST");

        final RpslObject keycertObject = createKeycertObject(new CertificatePrivateKeyPair().getCertificate(), "OWNER-MNT");
        databaseHelper.addObject(keycertObject);
        final RpslObject updatedMntner = addAttribute(OWNER_MNT, AttributeType.AUTH, keycertObject.getKey());
        databaseHelper.updateObject(updatedMntner);

        try {
            SecureRestTest.target(getClientSSLContext(), getClientCertificatePort(), "whois/test/person/TP1-TEST")
                .request()
                .put(Entity.entity(map(updatedPerson), MediaType.APPLICATION_XML), WhoisResources.class);
            fail();
        } catch (NotAuthorizedException e) {
            final WhoisResources whoisResources = e.getResponse().readEntity(WhoisResources.class);
            RestTest.assertErrorCount(whoisResources, 1);
            RestTest.assertErrorMessage(whoisResources, 0, "Error",
                "Authorisation for [%s] %s failed\nusing \"%s:\"\n" +
                 "not authenticated by: %s", "person", "TP1-TEST", "mnt-by", "OWNER-MNT");
        }
    }

    @Test
    public void update_person_with_client_cert_and_mntner_cert_successful() {
        final RpslObject updatedPerson = RpslObject.parse(
            "person: Test Person\n" +
             "address: Amsterdam\n" +
             "phone: +31 6 12345678\n" +
             "remarks: updated\n" +
             "nic-hdl: TP1-TEST\n" +
             "mnt-by: OWNER-MNT\n" +
             "source: TEST");

        // generate client cert and add to mntner
        final RpslObject keycertObject = createKeycertObject(getClientCertificate(), "OWNER-MNT");
        databaseHelper.addObject(keycertObject);
        final RpslObject updatedMntner = addAttribute(OWNER_MNT, AttributeType.AUTH, keycertObject.getKey());
        databaseHelper.updateObject(updatedMntner);

        // connect with mntner's client cert for authentication
        final WhoisResources whoisResources = SecureRestTest.target(getClientSSLContext(), getClientCertificatePort(), "whois/test/person/TP1-TEST")
                .request()
                .put(Entity.entity(map(updatedPerson), MediaType.APPLICATION_XML), WhoisResources.class);

        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        assertThat(whoisResources.getWhoisObjects().get(0).getAttributes().get(3).getValue(), containsString("updated"));
    }

    @Test
    public void update_person_missing_private_key_unauthorised() throws Exception {
        // create certificate and don't use private key
        final CertificatePrivateKeyPair certificatePrivateKeyPair = new CertificatePrivateKeyPair();
        final Path emptyFile = Files.createTempFile("privateKey", "key");
        final WhoisKeystore whoisKeystore = new WhoisKeystore(new String[]{emptyFile.toString()}, new String[]{certificatePrivateKeyPair.getCertificateFilename()}, null);
        final SSLContext sslContext = createSSLContext(whoisKeystore.getKeystore(), whoisKeystore.getPassword());

        final RpslObject updatedPerson = RpslObject.parse(
            "person: Test Person\n" +
             "address: Amsterdam\n" +
             "phone: +31 6 12345678\n" +
             "remarks: updated\n" +
             "nic-hdl: TP1-TEST\n" +
             "mnt-by: OWNER-MNT\n" +
             "source: TEST");

        // generate client cert and add to mntner
        final RpslObject keycertObject = createKeycertObject(certificatePrivateKeyPair.getCertificate(), "OWNER-MNT");
        databaseHelper.addObject(keycertObject);
        final RpslObject updatedMntner = addAttribute(OWNER_MNT, AttributeType.AUTH, keycertObject.getKey());
        databaseHelper.updateObject(updatedMntner);

        try {
            SecureRestTest.target(sslContext, getClientCertificatePort(), "whois/test/person/TP1-TEST")
                    .request()
                    .put(Entity.entity(map(updatedPerson), MediaType.APPLICATION_XML), WhoisResources.class);
            fail();
        } catch (ProcessingException e) {
            assertThat(e.getMessage(), containsString("javax.net.ssl.SSLHandshakeException: Received fatal alert: bad_certificate"));
        }
    }

    @Test
    public void update_route6_with_client_cert_and_mntner_cert_successful() {
        final RpslObject route6 = RpslObject.parse(
            "route6:          2001::/32\n" +
            "descr:           Test route\n" +
            "origin:          AS12726\n" +
            "mnt-by:          OWNER-MNT\n" +
            "source:          TEST\n"
        );
        databaseHelper.addObject(route6);

        // generate client cert and add to mntner
        final RpslObject keycertObject = createKeycertObject(getClientCertificate(), "OWNER-MNT");
        databaseHelper.addObject(keycertObject);
        final RpslObject updatedMntner = addAttribute(OWNER_MNT, AttributeType.AUTH, keycertObject.getKey());
        databaseHelper.updateObject(updatedMntner);

        final RpslObject updatedRoute6 = new RpslObjectBuilder(route6).append(new RpslAttribute(AttributeType.REMARKS, "updated")).get();

        // connect with mntner's client cert for authentication
        final WhoisResources whoisResources = SecureRestTest.target(getClientSSLContext(), getClientCertificatePort(), "whois/test/route6/2001::/32AS12726")
                .request()
                .put(Entity.entity(map(updatedRoute6), MediaType.APPLICATION_XML), WhoisResources.class);

        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        assertThat(whoisResources.getWhoisObjects().get(0).getAttributes().get(6).getValue(), containsString("updated"));

        assertThat(databaseHelper.lookupObject(ObjectType.ROUTE6, "2001::/32AS12726").containsAttribute(AttributeType.REMARKS), is(true));
    }

    @Test
    public void lookup_mntner_incorrect_client_cert_and_unfiltered_param_is_partially_unfiltered() {
        final RpslObject keycertObject = createKeycertObject(new CertificatePrivateKeyPair().getCertificate(), "OWNER-MNT");
        databaseHelper.addObject(keycertObject);
        final RpslObject updatedMntner = addAttribute(OWNER_MNT, AttributeType.AUTH, keycertObject.getKey());
        databaseHelper.updateObject(updatedMntner);

        final WhoisResources whoisResources = SecureRestTest.target(getClientSSLContext(),getClientCertificatePort(), "whois/test/mntner/OWNER-MNT?unfiltered")
                .request()
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getAttributes(), contains(
                new Attribute("mntner", "OWNER-MNT"),
                new Attribute("descr", "Owner Maintainer"),
                new Attribute("admin-c", "TP1-TEST", null, "person", Link.create("http://rest-test.db.ripe.net/test/person/TP1-TEST"), null),
                new Attribute("upd-to", "noreply@ripe.net", null, null, null, null),
                new Attribute("auth", "MD5-PW", "Filtered", null, null, null),
                new Attribute("auth", "SSO", "Filtered", null, null, null),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", Link.create("http://rest-test.db.ripe.net/test/mntner/OWNER-MNT"), null),
                new Attribute("source", "TEST", "Filtered", null, null, null),
                new Attribute("auth", keycertObject.getKey().toString(), null, "key-cert", Link.create("http://rest-test.db.ripe.net/test/key-cert/" + keycertObject.getKey()), null)));
    }

    @Test
    public void lookup_mntner_correct_client_cert_and_unfiltered_param_is_fully_unfiltered() {
        final RpslObject keycertObject = createKeycertObject(getClientCertificate(), "OWNER-MNT");
        databaseHelper.addObject(keycertObject);
        final RpslObject updatedMntner = addAttribute(OWNER_MNT, AttributeType.AUTH, keycertObject.getKey());
        databaseHelper.updateObject(updatedMntner);

        final WhoisResources whoisResources = SecureRestTest.target(getClientSSLContext(),getClientCertificatePort(), "whois/test/mntner/OWNER-MNT?unfiltered")
                .request()
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getAttributes(), contains(
                new Attribute("mntner", "OWNER-MNT"),
                new Attribute("descr", "Owner Maintainer"),
                new Attribute("admin-c", "TP1-TEST", null, "person", Link.create("http://rest-test.db.ripe.net/test/person/TP1-TEST"), null),
                new Attribute("upd-to", "noreply@ripe.net", null, null, null, null),
                new Attribute("auth", "MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/", "test", null, null, null),
                new Attribute("auth", "SSO person@net.net", null, null, null, null),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", Link.create("http://rest-test.db.ripe.net/test/mntner/OWNER-MNT"), null),
                new Attribute("source", "TEST", null, null, null, null),
                new Attribute("auth", keycertObject.getKey().toString(), null, "key-cert", Link.create("http://rest-test.db.ripe.net/test/key-cert/" + keycertObject.getKey()), null)));
    }

    // helper methods

    private WhoisResources map(final RpslObject rpslObject) {
        return whoisObjectMapper.mapRpslObjects(FormattedClientAttributeMapper.class, rpslObject);
    }

    private RpslObject map(final WhoisObject whoisObject) {
        return whoisObjectMapper.map(whoisObject, FormattedServerAttributeMapper.class);
    }

    private RpslObject addAttribute(final RpslObject rpslObject, final AttributeType attributeType, final CIString attributeValue) {
        return new RpslObjectBuilder(rpslObject).addAttributeAfter(new RpslAttribute(attributeType, attributeValue), AttributeType.SOURCE).get();
    }

}
