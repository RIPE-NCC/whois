package net.ripe.db.whois.api.rest;

import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotAllowedException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ServiceUnavailableException;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Variant;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rest.domain.Attribute;
import net.ripe.db.whois.api.rest.domain.ErrorMessage;
import net.ripe.db.whois.api.rest.domain.Link;
import net.ripe.db.whois.api.rest.domain.Source;
import net.ripe.db.whois.api.rest.domain.WhoisObject;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.mapper.DirtyClientAttributeMapper;
import net.ripe.db.whois.api.rest.mapper.FormattedClientAttributeMapper;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectMapper;
import net.ripe.db.whois.common.ApplicationVersion;
import net.ripe.db.whois.common.MaintenanceMode;
import net.ripe.db.whois.common.TestDateTimeProvider;
import net.ripe.db.whois.common.dao.EmailStatusDao;
import net.ripe.db.whois.common.domain.User;
import net.ripe.db.whois.common.domain.io.Downloader;
import net.ripe.db.whois.common.mail.EmailStatusType;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.PasswordHelper;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.common.rpsl.RpslObjectFilter;
import net.ripe.db.whois.common.support.TelnetWhoisClient;
import net.ripe.db.whois.query.QueryServer;
import net.ripe.db.whois.update.mail.MailSenderStub;
import org.eclipse.jetty.http.HttpStatus;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.filter.EncodingFilter;
import org.glassfish.jersey.message.GZipEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.EmptyResultDataAccessException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import static net.ripe.db.whois.common.rpsl.RpslObjectFilter.buildGenericObject;
import static net.ripe.db.whois.common.support.StringMatchesRegexp.stringMatchesRegexp;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

// FIXME: make this into a suite that runs twice: once with XML, once with JSON
@Tag("IntegrationTest")
public class WhoisRestServiceTestIntegration extends AbstractIntegrationTest {

    @Autowired QueryServer queryServer;

    public static final String TEST_PERSON_STRING = "" +
            "person:         Test Person\n" +
            "address:        Singel 258\n" +
            "phone:          +31 6 12345678\n" +
            "nic-hdl:        TP1-TEST\n" +
            "mnt-by:         OWNER-MNT\n" +
            "source:         TEST\n";

    public static final RpslObject TEST_PERSON = RpslObject.parse(TEST_PERSON_STRING);
    private static final RpslObject PAULETH_PALTHEN = RpslObject.parse("" +
            "person:    Pauleth Palthen\n" +
            "address:   Singel 258\n" +
            "phone:     +31-1234567890\n" +
            "e-mail:    noreply@ripe.net\n" +
            "mnt-by:    OWNER-MNT\n" +
            "nic-hdl:   PP1-TEST\n" +
            "remarks:   remark\n" +
            "source:    TEST\n");

    private static final RpslObject RPSL_MNT_PERSON = RpslObject.parse("" +
            "person:    Pauleth Palthen \n" +
            "address:   Singel 258\n" +
            "phone:     +31-1234567890\n" +
            "e-mail:    noreply@ripe.net\n" +
            "mnt-by:    OWNER-MNT\n" +
            "nic-hdl:   PP2-TEST\n" +
            "remarks:   remark\n" +
            "source:    TEST\n");

    private static final RpslObject NOTIFY_PERSON = RpslObject.parse("" +
            "person:    Pauleth Palthen \n" +
            "address:   Singel 258\n" +
            "phone:     +31-1234567890\n" +
            "e-mail:    noreply@ripe.net\n" +
            "notify:    test@ripe.net\n" +
            "mnt-by:    OWNER-MNT\n" +
            "nic-hdl:   PP3-TEST\n" +
            "remarks:   remark\n" +
            "source:    TEST\n");

    private static final RpslObject OWNER_MNT = RpslObject.parse("" +
            "mntner:      OWNER-MNT\n" +
            "descr:       Owner Maintainer\n" +
            "admin-c:     TP1-TEST\n" +
            "upd-to:      noreply@ripe.net\n" +
            "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
            "auth:        SSO person@net.net\n" +
            "mnt-by:      OWNER-MNT\n" +
            "source:      TEST");

    private static final RpslObject PASSWORD_ONLY_MNT = RpslObject.parse("" +
            "mntner:      PASSWORD-ONLY-MNT\n" +
            "descr:       Maintainer\n" +
            "admin-c:     TP1-TEST\n" +
            "upd-to:      noreply@ripe.net\n" +
            "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
            "mnt-by:      PASSWORD-ONLY-MNT\n" +
            "source:      TEST");

    private static final RpslObject SSO_ONLY_MNT = RpslObject.parse("" +
            "mntner:         SSO-ONLY-MNT\n" +
            "descr:          Maintainer\n" +
            "admin-c:        TP1-TEST\n" +
            "auth:           SSO person@net.net\n" +
            "mnt-by:         SSO-ONLY-MNT\n" +
            "upd-to:         noreply@ripe.net\n" +
            "source:         TEST");

    private static final RpslObject SSO_AND_PASSWORD_MNT = RpslObject.parse("" +
            "mntner:         SSO-PASSWORD-MNT\n" +
            "descr:          Maintainer\n" +
            "admin-c:        TP1-TEST\n" +
            "auth:           SSO person@net.net\n" +
            "auth:           MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
            "mnt-by:         SSO-PASSWORD-MNT\n" +
            "upd-to:         noreply@ripe.net\n" +
            "source:         TEST");

    private static final String TEST_ROLE_STRING = "" +
            "role:           Test Role\n" +
            "address:        Singel 258\n" +
            "phone:          +31 6 12345678\n" +
            "nic-hdl:        TR1-TEST\n" +
            "admin-c:        TR1-TEST\n" +
            "abuse-mailbox:  abuse@test.net\n" +
            "mnt-by:         OWNER-MNT\n" +
            "source:         TEST\n";
    private static final RpslObject TEST_ROLE = RpslObject.parse(TEST_ROLE_STRING);

    private static final RpslObject TEST_IRT = RpslObject.parse("" +
            "irt:          irt-test\n" +
            "address:      RIPE NCC\n" +
            "e-mail:       noreply@ripe.net\n" +
            "admin-c:      TP1-TEST\n" +
            "tech-c:       TP1-TEST\n" +
            "auth:         MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
            "mnt-by:       OWNER-MNT\n" +
            "source:       TEST\n");
    public static final String INETNUM_OBJECT = "inetnum:        10.0.0.0 - 10.0.0.255\n" +
            "status:         ALLOCATED PA\n" +
            "org:            ORG-TO1-TEST\n" +
            "mnt-by:         OWNER-MNT\n" +
            "mnt-by:         RIPE-NCC-HM-MNT\n" +
            "source:         TEST\n";

    @Autowired private WhoisObjectMapper whoisObjectMapper;
    @Autowired private MaintenanceMode maintenanceMode;
    @Autowired private MailSenderStub mailSenderStub;
    @Autowired private TestDateTimeProvider testDateTimeProvider;
    @Autowired private ApplicationVersion applicationVersion;

    @Autowired private EmailStatusDao emailStatusDao;

    @BeforeEach
    public void setup() {
        databaseHelper.addObject("person: Test Person\nnic-hdl: TP1-TEST");
        databaseHelper.addObject("role: Test Role\nnic-hdl: TR1-TEST");
        databaseHelper.addObject(OWNER_MNT);
        databaseHelper.updateObject(TEST_PERSON);
        databaseHelper.updateObject(TEST_ROLE);
        maintenanceMode.set("FULL,FULL");
        testDateTimeProvider.setTime(LocalDateTime.parse("2001-02-04T17:00:00"));
    }

    @Test // check to see if we can change an attributed on an object that has RIPE-NCC-RPSL-MNT as mnt-by. should fail and tell them to fix
    public void existing_mntby_ncc_rpsl_test() {
        databaseHelper.addObject("" +
                "mntner:      RIPE-NCC-RPSL-MNT\n" +
                "descr:       Owner Maintainer\n" +
                "admin-c:     TP1-TEST\n" +
                "upd-to:      noreply@ripe.net\n" +
                "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "auth:        SSO person@net.net\n" +
                "mnt-by:      OWNER-MNT\n" +
                "source:      TEST");

        databaseHelper.addObject(RPSL_MNT_PERSON);

        final RpslObject updatedObject = new RpslObjectBuilder(RPSL_MNT_PERSON)
                .addAttributeSorted(new RpslAttribute(AttributeType.REMARKS, "updated"))
                .addAttributeSorted(new RpslAttribute(AttributeType.MNT_BY, "RIPE-NCC-RPSL-MNT"))
                .get();
        final WhoisResources updatedPerson = whoisObjectMapper.mapRpslObjects(FormattedClientAttributeMapper.class, updatedObject);
        try {
            RestTest.target(getPort(), "whois/test/person/PP2-TEST?password=test")
                    .request(MediaType.APPLICATION_XML)
                    .put(Entity.entity(updatedPerson, MediaType.APPLICATION_XML), WhoisResources.class);
            fail();
        } catch (BadRequestException ex) {
            final WhoisResources whoisResources = ex.getResponse().readEntity(WhoisResources.class);
            RestTest.assertErrorCount(whoisResources, 1);
            RestTest.assertErrorMessage(whoisResources, 0, "Error", "You cannot add or remove a RIPE NCC maintainer");
            assertThat(whoisResources.getTermsAndConditions().getHref(), is(WhoisResources.TERMS_AND_CONDITIONS));
        }
    }

    @Test
    public void modify_mntby_ncc_rpsl_test() {
        databaseHelper.addObject(PAULETH_PALTHEN);

        final RpslObject updatedObject = new RpslObjectBuilder(PAULETH_PALTHEN).replaceAttribute(
                new RpslAttribute(AttributeType.MNT_BY, "OWNER-MNT"),
                new RpslAttribute(AttributeType.MNT_BY, "RIPE-NCC-RPSL-MNT")).get();
        final WhoisResources updatedPerson = whoisObjectMapper.mapRpslObjects(FormattedClientAttributeMapper.class, updatedObject);
        try {
            RestTest.target(getPort(), "whois/test/person/PP1-TEST?password=test")
                    .request(MediaType.APPLICATION_XML)
                    .put(Entity.entity(updatedPerson, MediaType.APPLICATION_XML), WhoisResources.class);
            fail();
        } catch (BadRequestException ex) {
            final WhoisResources whoisResources = ex.getResponse().readEntity(WhoisResources.class);
            RestTest.assertErrorMessage(whoisResources, 0, "Error", "You cannot add or remove a RIPE NCC maintainer");
            RestTest.assertErrorMessage(whoisResources, 1, "Error", "The maintainer '%s' was not found in the database", "RIPE-NCC-RPSL-MNT");
            RestTest.assertErrorMessage(whoisResources, 2, "Error", "Unknown object referenced %s", "RIPE-NCC-RPSL-MNT");
            assertThat(whoisResources.getTermsAndConditions().getHref(), is(WhoisResources.TERMS_AND_CONDITIONS));
        }
    }

    @Test
    public void lookup_downloader_test() throws Exception {
        Path path = Files.createTempFile("downloader_test", "");
        Downloader downloader = new Downloader();
        downloader.downloadTo(LoggerFactory.getLogger("downloader_test"), new URL(String.format("http://localhost:%d/whois/test/mntner/owner-mnt", getPort())), path);
        final String result = new String(Files.readAllBytes(path));
        assertThat(result, containsString("OWNER-MNT"));
        assertThat(result, endsWith("</whois-resources>\n"));
    }

    @Test
    public void lookup_without_accepts_header() {
        final String query = TelnetWhoisClient.queryLocalhost(getPort(), "GET /whois/test/mntner/owner-mnt HTTP/1.1\nHost: localhost\nConnection: close\n");

        assertThat(query, containsString("HTTP/1.1 200 OK"));
        assertThat(query, containsString("<whois-resources xmlns"));
    }

    @Test
    public void lookup_with_empty_accepts_header() {
        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/mntner/owner-mnt")
                .request()
                .get(WhoisResources.class);

        assertThat(map(whoisResources.getWhoisObjects().get(0)), is(RpslObject.parse("" +
                "mntner:         OWNER-MNT\n" +
                "descr:          Owner Maintainer\n" +
                "admin-c:        TP1-TEST\n" +
                "auth:           MD5-PW # Filtered\n" +
                "auth:           SSO # Filtered\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST # Filtered")));
    }

    @Test
    public void lookup_inet6num_without_prefix_length() throws InterruptedException {
        databaseHelper.addObject(
                "inet6num:       2001:2002:2003::/48\n" +
                "netname:        RIPE-NCC\n" +
                "descr:          Private Network\n" +
                "country:        NL\n" +
                "tech-c:         TP1-TEST\n" +
                "status:         ASSIGNED PA\n" +
                "mnt-by:         OWNER-MNT\n" +
                "mnt-lower:      OWNER-MNT\n" +
                "source:         TEST"
        );
        ipTreeUpdater.rebuild();

        assertThrows(NotFoundException.class, () -> {
            RestTest.target(getPort(), "whois/test/inet6num/2001:2002:2003::").request().get(WhoisResources.class);
        });
    }

    @Test
    public void lookup_inet6num_with_prefix_length() {
        databaseHelper.addObject(
                "inet6num:       2001:2002:2003::/48\n" +
                "netname:        RIPE-NCC\n" +
                "descr:          Private Network\n" +
                "country:        NL\n" +
                "tech-c:         TP1-TEST\n" +
                "status:         ASSIGNED PA\n" +
                "mnt-by:         OWNER-MNT\n" +
                "mnt-lower:      OWNER-MNT\n" +
                "source:         TEST"
        );
        ipTreeUpdater.rebuild();

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/inet6num/2001:2002:2003::/48").request().get(WhoisResources.class);
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        assertThat(whoisResources.getErrorMessages(), is(empty()));
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getPrimaryKey().get(0).getValue(), is("2001:2002:2003::/48"));
    }

    @Test
    public void lookup_person_filtered() {
        databaseHelper.addObject(PAULETH_PALTHEN);

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person/PP1-TEST").request().get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getAttributes(), contains(
                new Attribute("person", "Pauleth Palthen"),
                new Attribute("address", "Singel 258"),
                new Attribute("phone", "+31-1234567890"),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", Link.create("http://rest-test.db.ripe.net/test/mntner/OWNER-MNT"), null),
                new Attribute("nic-hdl", "PP1-TEST"),
                new Attribute("remarks", "remark"),
                new Attribute("source", "TEST", "Filtered", null, null, null)));
    }

    @Test
    public void lookup_person_lowercase_source() {
        databaseHelper.addObject(RpslObject.parse("person: Test Person\nnic-hdl: TP2-TEST\nmnt-by: OWNER-MNT\nsource: test"));
        assertThat(databaseHelper.lookupObject(ObjectType.PERSON, "TP2-TEST").toString(), containsString("source:         test"));

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person/TP2-TEST").request().get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getAttributes().get(whoisObject.getAttributes().size() - 1).getValue(), is("test"));
    }

    @Test
    public void lookup_person_unfiltered() {
        databaseHelper.addObject(PAULETH_PALTHEN);

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person/PP1-TEST?unfiltered").request().get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getAttributes(), contains(
                new Attribute("person", "Pauleth Palthen"),
                new Attribute("address", "Singel 258"),
                new Attribute("phone", "+31-1234567890"),
                new Attribute("e-mail", "noreply@ripe.net"),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", Link.create("http://rest-test.db.ripe.net/test/mntner/OWNER-MNT"), null),
                new Attribute("nic-hdl", "PP1-TEST"),
                new Attribute("remarks", "remark"),
                new Attribute("source", "TEST")));
    }

    @Test
    public void lookup_person_unformatted() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "person:  Pauleth Palthen\n" +
                "address: Singel 258\n" +
                "phone:   +31\n" +
                "         1234567890\n" +
                "e-mail:  noreply@ripe.net\n" +
                "mnt-by:  OWNER-MNT\n" +
                "nic-hdl: PP1-TEST\n" +
                "remarks:  remark1 # comment1\n" +
                "          remark2 # comment2\n" +
                "          remark3 # comment3\n" +
                "fail:   fail1 # comment1\n" +
                "        fail2 # comment2\n" +
                "     # comment3\n" +
                "source:  TEST\n");

        databaseHelper.addObject(rpslObject);

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person/PP1-TEST?unfiltered&unformatted").request().get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));

        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));

        assertThat(whoisObject.getAttributes().get(2).getValue(), is(
                "          +31\n" +
                "                1234567890"));
        assertThat(whoisObject.getAttributes().get(6).getValue(), is(
                "        remark1 # comment1\n" +
                "                remark2 # comment2\n" +
                "                remark3 # comment3"));
        assertThat(whoisObject.getAttributes().get(7).getValue(), is(
                "           fail1 # comment1\n" +
                "                fail2 # comment2\n" +
                "                # comment3"));
    }

    @Test
    public void lookup_xml_text_not_contains_empty_xmlns() {
        final String whoisResources = RestTest.target(getPort(), "whois/test/person/TP1-TEST").request().get(String.class);
        assertThat(whoisResources, not(containsString("xmlns=\"\"")));
    }

    @Test
    public void lookup_xml_text_not_contains_root_level_locator() {
        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person/TP1-TEST").request().get(WhoisResources.class);
        assertThat(whoisResources.getLink(), nullValue());
    }

    @Test
    public void lookup_failure_with_xml_extension() {
        try {
            RestTest.target(getPort(), "whois/test/inet6num/No%20clue%20what%20the%20range%20is.xml")
                    .request()
                    .get(String.class);
            fail();
        } catch (BadRequestException e) {
            final WhoisResources whoisResources = e.getResponse().readEntity(WhoisResources.class);
            assertThat(whoisResources.getLink().getHref(), stringMatchesRegexp("http://localhost:\\d+/test/inet6num/No%20clue%20what%20the%20range%20is"));
            assertThat(whoisResources.getLink().getType(), is("locator"));
        }
    }

    @Test
    public void lookup_success_with_xml_extension() {
        databaseHelper.addObject(
            "mntner:      A-MNTNER-WITH-A-VERY-VERY-LONG-NAME-MNT\n" +
            "descr:       Owner Maintainer\n" +
            "admin-c:     TP1-TEST\n" +
            "upd-to:      noreply@ripe.net\n" +
            "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
            "auth:        SSO person@net.net\n" +
            "mnt-by:      OWNER-MNT\n" +
            "source:      TEST");

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/mntner/A-MNTNER-WITH-A-VERY-VERY-LONG-NAME-MNT.xml")
                .request()
                .get(WhoisResources.class);

        // TODO: [ES] xlink is not set to request URL on success
        assertThat(whoisResources.getLink(), is(nullValue()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        assertThat(whoisResources.getWhoisObjects().get(0).getLink().getHref(), stringMatchesRegexp("http://rest-test.db.ripe.net/test/mntner/A-MNTNER-WITH-A-VERY-VERY-LONG-NAME-MNT"));
        assertThat(whoisResources.getWhoisObjects().get(0).getLink().getType(), is("locator"));
    }

    @Test
    public void lookup_inet6num() {
        final RpslObject inet6num = RpslObject.parse("" +
                "inet6num: 2001::/48\n" +
                "netname: RIPE-NCC\n" +
                "descr: some description\n" +
                "country: DK\n" +
                "admin-c: TP1-TEST\n" +
                "tech-c: TP1-TEST\n" +
                "status: ASSIGNED\n" +
                "mnt-by: OWNER-MNT\n" +
                "source: TEST\n");
        databaseHelper.addObject(inet6num);
        ipTreeUpdater.rebuild();

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/inet6num/2001::/48").request().get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));

        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getAttributes(), contains(
                new Attribute("inet6num", "2001::/48"),
                new Attribute("netname", "RIPE-NCC"),
                new Attribute("descr", "some description"),
                new Attribute("country", "DK"),
                new Attribute("admin-c", "TP1-TEST", null, "person", Link.create("http://rest-test.db.ripe.net/test/person/TP1-TEST"), null),
                new Attribute("tech-c", "TP1-TEST", null, "person", Link.create("http://rest-test.db.ripe.net/test/person/TP1-TEST"), null),
                new Attribute("status", "ASSIGNED"),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", Link.create("http://rest-test.db.ripe.net/test/mntner/OWNER-MNT"), null),
                new Attribute("source", "TEST")
        ));
    }

    @Test
    public void lookup_route() {
        databaseHelper.addObject(
                "route:           193.254.30.0/24\n" +
                "descr:           Test route\n" +
                "origin:          AS12726\n" +
                "mnt-by:          OWNER-MNT\n" +
                "mnt-routes:      OWNER-MNT {192.168.0.0/16}\n" +
                "source:          TEST\n");
        ipTreeUpdater.rebuild();

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/route/193.254.30.0/24AS12726").request().get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));

        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getLink().getHref(), is("http://rest-test.db.ripe.net/test/route/193.254.30.0/24AS12726"));

        final List<Attribute> primaryKey = whoisObject.getPrimaryKey();
        assertThat(primaryKey, hasSize(2));
        assertThat(primaryKey, contains(new Attribute("route", "193.254.30.0/24"),
                new Attribute("origin", "AS12726")));

        assertThat(whoisObject.getAttributes(), contains(
                new Attribute("route", "193.254.30.0/24"),
                new Attribute("descr", "Test route"),
                new Attribute("origin", "AS12726"),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", Link.create("http://rest-test.db.ripe.net/test/mntner/OWNER-MNT"), null),
                new Attribute("mnt-routes", "OWNER-MNT {192.168.0.0/16}", null, "mntner", Link.create("http://rest-test.db.ripe.net/test/mntner/OWNER-MNT"), null),
                new Attribute("source", "TEST")
        ));
    }

    @Test
    public void lookup_route_encoded_forward_slash_in_primary_key() {
        databaseHelper.addObject(
                "route:           193.254.30.0/24\n" +
                "descr:           Test route\n" +
                "origin:          AS12726\n" +
                "mnt-by:          OWNER-MNT\n" +
                "mnt-routes:      OWNER-MNT {192.168.0.0/16}\n" +
                "source:          TEST\n");
        ipTreeUpdater.rebuild();

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/route/193.254.30.0%2F24AS12726").request().get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));

        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getLink().getHref(), is("http://rest-test.db.ripe.net/test/route/193.254.30.0/24AS12726"));
    }

    @Test
    public void lookup_route6() {
        databaseHelper.addObject(
                "route6:          2001::/32\n" +
                "descr:           Test route\n" +
                "origin:          AS12726\n" +
                "mnt-by:          OWNER-MNT\n" +
                "source:          TEST\n");
        ipTreeUpdater.rebuild();

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/route6/2001::/32AS12726").request().get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));

        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getLink().getHref(), is("http://rest-test.db.ripe.net/test/route6/2001::/32AS12726"));

        final List<Attribute> primaryKey = whoisObject.getPrimaryKey();
        assertThat(primaryKey, hasSize(2));
        assertThat(primaryKey, contains(new Attribute("route6", "2001::/32"), new Attribute("origin", "AS12726")));

        assertThat(whoisObject.getAttributes(), contains(
                new Attribute("route6", "2001::/32"),
                new Attribute("descr", "Test route"),
                new Attribute("origin", "AS12726"),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", Link.create("http://rest-test.db.ripe.net/test/mntner/OWNER-MNT"), null),
                new Attribute("source", "TEST")
        ));
    }

    @Test
    public void lookup_as_set() {
        databaseHelper.addObject(
                "as-set:         AS-Test\n" +
                "members:        AS1,AS2,AS3\n" +
                "tech-c:         TP1-TEST\n" +
                "admin-c:        TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST");

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/as-set/AS-test").request().get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));

        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getLink().getHref(), is("http://rest-test.db.ripe.net/test/as-set/AS-Test"));

        final List<Attribute> primaryKey = whoisObject.getPrimaryKey();
        assertThat(primaryKey, contains(new Attribute("as-set", "AS-Test")));

        assertThat(whoisObject.getAttributes(), contains(
                new Attribute("as-set", "AS-Test"),
                new Attribute("members", "AS1", null, "aut-num", Link.create("http://rest-test.db.ripe.net/test/aut-num/AS1"), null),
                new Attribute("members", "AS2", null, "aut-num", Link.create("http://rest-test.db.ripe.net/test-nonauth/aut-num/AS2"), null),
                new Attribute("members", "AS3", null, "aut-num", Link.create("http://rest-test.db.ripe.net/test-nonauth/aut-num/AS3"), null),
                new Attribute("tech-c", "TP1-TEST", null, "person", Link.create("http://rest-test.db.ripe.net/test/person/TP1-TEST"), null),
                new Attribute("admin-c", "TP1-TEST", null, "person", Link.create("http://rest-test.db.ripe.net/test/person/TP1-TEST"), null),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", Link.create("http://rest-test.db.ripe.net/test/mntner/OWNER-MNT"), null),
                new Attribute("source", "TEST")
        ));
    }

    @Test
    public void lookup_object_text_plain_accept_header() {
        final String rpslObject = RestTest.target(getPort(), "whois/test/person/TP1-TEST")
                .request(MediaType.TEXT_PLAIN)
                .get(String.class);

        assertThat(rpslObject, is(TEST_PERSON_STRING + '\n'));
    }

    @Test
    public void lookup_object_text_plain_extension() {
        final String rpslObject = RestTest.target(getPort(), "whois/test/person/TP1-TEST.txt")
                .request()
                .get(String.class);

        assertThat(rpslObject, is(TEST_PERSON_STRING + '\n'));
    }

    @Test
    public void lookup_inetnum_text_plain_accept_header() {
        databaseHelper.addObject(
                "mntner:       RIPE-NCC-HM-MNT\n" +
                        "source:   TEST");
        databaseHelper.addObject(
                "organisation: ORG-TO1-TEST\n" +
                        "org-name:     Test Organisation\n" +
                        "abuse-c:      TR1-TEST\n" +
                        "source:       TEST");
        databaseHelper.addObject(INETNUM_OBJECT);

        final String response = RestTest.target(getPort(), "whois/test/inetnum/10.0.0.0%20-%2010.0.0" +
                        ".255?managed-attributes&resource-holder&abuse-contact")
                .request(MediaType.TEXT_PLAIN)
                .get(String.class);

        assertThat(response, is(INETNUM_OBJECT + '\n'));
    }

    @Test
    public void lookup_inetnum_text_plain_extension() {
        databaseHelper.addObject(
                "mntner:       RIPE-NCC-HM-MNT\n" +
                        "source:   TEST");
        databaseHelper.addObject(
                "organisation: ORG-TO1-TEST\n" +
                        "org-name:     Test Organisation\n" +
                        "abuse-c:      TR1-TEST\n" +
                        "source:       TEST");
        databaseHelper.addObject(INETNUM_OBJECT);

        final String response = RestTest.target(getPort(), "whois/test/inetnum/10.0.0.0%20-%2010.0.0" +
                        ".255.txt?managed-attributes&resource-holder&abuse-contact")
                .request()
                .get(String.class);

        assertThat(response, is(INETNUM_OBJECT + '\n'));
    }

    @Test
    public void lookup_role_text_plain_accept_header() {
        final String response = RestTest.target(getPort(), "whois/test/role/TR1-TEST")
                .request(MediaType.TEXT_PLAIN)
                .get(String.class);

        assertThat(response, is(TEST_ROLE_STRING + '\n'));
    }

    @Test
    public void lookup_role_text_plain_extension() {
        final String response = RestTest.target(getPort(), "whois/test/role/TR1-TEST.txt")
                .request()
                .get(String.class);

        assertThat(response, is(TEST_ROLE_STRING + '\n'));
    }

    @Test
    public void lookup_object_text_plain_not_found_accept_header() {
        try {
            RestTest.target(getPort(), "whois/test/person/PP1-TEST")
                    .request(MediaType.TEXT_PLAIN)
                    .get(String.class);
            fail();
        } catch (NotFoundException e) {
            final String response = e.getResponse().readEntity(String.class);
            assertThat(response, is(String.format("http://localhost:%s/test/person/PP1-TEST\n" +
                    "Severity: Error\n" +
                    "Text: ERROR:101: no entries found\n" +
                    "\n" +
                    "No entries found in source %%s.\n" +
                    "[TEST]\n" +
                    "https://docs.db.ripe.net/terms-conditions.html", getPort())));
        }
    }

    @Test
    public void lookup_object_text_plain_not_found_extension() {
        try {
            RestTest.target(getPort(), "whois/test/person/PP1-TEST.txt")
                    .request()
                    .get(String.class);
            fail();
        } catch (NotFoundException e) {
            final String response = e.getResponse().readEntity(String.class);
            assertThat(response, is(String.format("http://localhost:%s/test/person/PP1-TEST\n" +
                    "Severity: Error\n" +
                    "Text: ERROR:101: no entries found\n" +
                    "\n" +
                    "No entries found in source %%s.\n" +
                    "[TEST]\n" +
                    "https://docs.db.ripe.net/terms-conditions.html", getPort())));
        }
    }

    @Test
    public void lookup_object_text_plain_bad_source_accept_header() {
        try {
            RestTest.target(getPort(), "whois/oez/org/MKQ-RIPE.txt")
                    .request()
                    .get(String.class);
            fail();
        } catch (BadRequestException e) {
            final String response = e.getResponse().readEntity(String.class);
            assertThat(response, is("Severity: Error\n" +
                    "Text: Invalid source 'oez'\n" +
                    "locator: https://docs.db.ripe.net/terms-conditions.html"));
        }
    }

    @Test
    public void lookup_object_text_plain_bad_format_accept_header() {
        try {
            RestTest.target(getPort(), "whois/test/org/MKQ-RIPE.txt")
                    .request()
                    .get(String.class);
            fail();
        } catch (BadRequestException e) {
            final String response = e.getResponse().readEntity(String.class);
            assertThat(response, is("Severity: Error\n" +
                    "Text: Invalid object type: org\n" +
                    "locator: https://docs.db.ripe.net/terms-conditions.html"));
        }
    }

    @Test
    public void lookup_person_utf8_normalised() {
        try {
            RestTest.target(getPort(), "whois/test/person/\u03A3-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(WhoisResources.class);
            fail();
        } catch (NotFoundException e) {
            assertThat(e.getResponse().readEntity(String.class), containsString("no entries found"));
        }
    }

    @Test
    public void lookup_person_json() {
        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person/TP1-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));

        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getAttributes(), contains(
                new Attribute("person", "Test Person"),
                new Attribute("address", "Singel 258"),
                new Attribute("phone", "+31 6 12345678"),
                new Attribute("nic-hdl", "TP1-TEST"),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", Link.create("http://rest-test.db.ripe.net/test/mntner/OWNER-MNT"), null),
                new Attribute("source", "TEST")));

        assertThat(whoisResources.getTermsAndConditions().getHref(), is(WhoisResources.TERMS_AND_CONDITIONS));
    }

    @Test
    public void create_person_json_ipv4_address() {
        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person?password=test", true)
                .request()
                .post(Entity.entity(map(PAULETH_PALTHEN), MediaType.APPLICATION_XML), WhoisResources.class);

        assertThat(whoisResources.getLink().getHref(), is(String.format("http://0.0.0.0:%s/test/person", getPort())));
        assertThat(whoisResources.getErrorMessages(), hasSize(1));
        assertThat(whoisResources.getErrorMessages().getFirst().getText(), is("MD5 hashed password authentication is deprecated and support will be " +
                "removed at the end of 2025. Please switch to an alternative authentication method before then."));
    }

    @Test
    public void create_person_json_ipv6_address() {
        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person?password=test", false)
                .request()
                .post(Entity.entity(map(PAULETH_PALTHEN), MediaType.APPLICATION_XML), WhoisResources.class);

        assertThat(whoisResources.getLink().getHref(), is(String.format("http://[::1]:%s/test/person", getPort())));
        assertThat(whoisResources.getErrorMessages(), hasSize(1));
        assertThat(whoisResources.getErrorMessages().getFirst().getText(), is("MD5 hashed password authentication is deprecated and support will be " +
                "removed at the end of 2025. Please switch to an alternative authentication method before then."));
    }


    @Test
    public void lookup_person_head() {
        final Response response = RestTest.target(getPort(), "whois/test/person/TP1-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .head();

        assertThat(response.getStatus(), is(200));
        assertThat(response.readEntity(String.class), is(emptyString()));
    }

    @Test
    public void lookup_person_head_not_found() {
        final Response response = RestTest.target(getPort(), "whois/test/person/NONEXISTANT")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .head();

        assertThat(response.getStatus(), is(404));
        assertThat(response.readEntity(String.class), is(emptyString()));
    }

    @Test
    public void lookup_correct_object_json() {
        final String whoisResources = RestTest.target(getPort(), "whois/test/person/TP1-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(String.class);

        assertThat(whoisResources, not(containsString("errormessages")));
        assertThat(whoisResources, containsString("{\"objects\":{\"object\":[ {\n  \"type\" : \"person\","));
        assertThat(whoisResources, containsString("" +
                "\"terms-and-conditions\" : {\n" +
                "\"type\" : \"locator\",\n" +
                "\"href\" : \"https://docs.db.ripe.net/terms-conditions.html\"\n" +
                "}"));
    }

    @Test
    public void lookup_role_accept_json() {
        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/role/TR1-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));

        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getAttributes(), contains(
                new Attribute("role", "Test Role"),
                new Attribute("address", "Singel 258"),
                new Attribute("phone", "+31 6 12345678"),
                new Attribute("nic-hdl", "TR1-TEST"),
                new Attribute("admin-c", "TR1-TEST", null, "role", Link.create("http://rest-test.db.ripe.net/test/role/TR1-TEST"), null),
                new Attribute("abuse-mailbox", "abuse@test.net"),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", Link.create("http://rest-test.db.ripe.net/test/mntner/OWNER-MNT"), null),
                new Attribute("source", "TEST")));
    }

    @Test
    public void lookup_person_accept_json() {
        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/TEST/person/TP1-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));

        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getPrimaryKey().get(0).getValue(), is("TP1-TEST"));
    }

    @Test
    public void lookup_object_json_extension() {
        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/TEST/person/TP1-TEST.json")
                .request()
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));

        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getPrimaryKey().get(0).getValue(), is("TP1-TEST"));
    }

    @Test
    public void lookup_object_not_found() {
        assertThrows(NotFoundException.class, () -> {
            RestTest.target(getPort(), "whois/test/person/PP1-TEST").request().get(WhoisResources.class);
        });
    }

    @Test
    public void lookup_object_wrong_source() {
        assertThrows(NotFoundException.class, () -> {
            RestTest.target(getPort(), "whois/test-grs/person/TP1-TEST").request().get(String.class);
        });
    }

    @Test
    public void lookup_mntner_does_not_have_referenced_type_in_sso() {
        databaseHelper.addObject("" +
                "mntner:         MNT-TEST" + "\n" +
                "descr:          test\n" +
                "admin-c:        TP1-TEST\n" +
                "upd-to:         noreply@ripe.net\n" +
                "auth:           MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "auth:           SSO test@ripe.net\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST");

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/TEST/mntner/MNT-TEST?password=test&unfiltered")
                .request(MediaType.APPLICATION_XML_TYPE)
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));

        Attribute expected = new Attribute("auth", "SSO test@ripe.net", null, null, null, null);
        assertThat(whoisResources.getWhoisObjects().get(0).getAttributes(), hasItem(expected));
    }
    @Test
    public void grs_lookup_object_wrong_source() {
        try {
            RestTest.target(getPort(), "whois/pez/person/PP1-TEST").request().get(String.class);
            fail();
        } catch (BadRequestException e) {
            RestTest.assertOnlyErrorMessage(e, "Error", "Invalid source '%s'", "pez");
        }
    }

    @Test
    public void grs_lookup_found() {
        databaseHelper.addObject("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST-GRS\n");

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test-grs/aut-num/AS102").request().get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        assertThat(whoisResources.getWhoisObjects().get(0).getPrimaryKey(), contains(new Attribute("aut-num", "AS102")));
        assertThat(whoisResources.getWhoisObjects().get(0).getSource(), is(new Source("test-grs")));
    }

    @Test
    public void lookup_autnum_out_of_region() {
        final RpslObject autnum = RpslObject.parse("" +
                "aut-num:        AS105\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST-NONAUTH\n");
        databaseHelper.addObject(autnum);

        final WhoisResources whoisResources = RestTest.target(getPort(),
                "whois/TEST-NONAUTH/aut-num/AS105")
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);

        assertThat(whoisObject.getAttributes(), contains(
                new Attribute("aut-num", "AS105"),
                new Attribute("as-name", "End-User-2"),
                new Attribute("descr", "description"),
                new Attribute("admin-c", "TP1-TEST", null, "person", Link.create("http://rest-test.db.ripe.net/test/person/TP1-TEST"), null),
                new Attribute("tech-c", "TP1-TEST", null, "person", Link.create("http://rest-test.db.ripe.net/test/person/TP1-TEST"), null),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", Link.create("http://rest-test.db.ripe.net/test/mntner/OWNER-MNT"), null),
                new Attribute("source", "TEST-NONAUTH")));
    }

    @Test
    public void lookup_mntner() {
        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT").request().get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getAttributes(), contains(
                new Attribute("mntner", "OWNER-MNT"),
                new Attribute("descr", "Owner Maintainer"),
                new Attribute("admin-c", "TP1-TEST", null, "person", Link.create("http://rest-test.db.ripe.net/test/person/TP1-TEST"), null),
                new Attribute("auth", "MD5-PW", "Filtered", null, null, null),
                new Attribute("auth", "SSO", "Filtered", null, null, null),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", Link.create("http://rest-test.db.ripe.net/test/mntner/OWNER-MNT"), null),
                new Attribute("source", "TEST", "Filtered", null, null, null)));
    }

    @Test
    public void lookup_unfiltered_param() {
        databaseHelper.addObject(PAULETH_PALTHEN);

        final String unfiltered = RestTest.target(getPort(), "whois/test/person/PP1-TEST?unfiltered").request().get(String.class);
        assertThat(unfiltered, containsString("attribute name=\"e-mail\" value=\"noreply@ripe.net\""));

        final String unfilteredEquals = RestTest.target(getPort(), "whois/test/person/PP1-TEST?unfiltered=").request().get(String.class);
        assertThat(unfilteredEquals, containsString("attribute name=\"e-mail\" value=\"noreply@ripe.net\""));

        final String unfilteredEqualsTrue = RestTest.target(getPort(), "whois/test/person/PP1-TEST?unfiltered=true").request().get(String.class);
        assertThat(unfilteredEqualsTrue, containsString("attribute name=\"e-mail\" value=\"noreply@ripe.net\""));

        final String unfilteredEqualsFalse = RestTest.target(getPort(), "whois/test/person/PP1-TEST?unfiltered=false").request().get(String.class);
        assertThat(unfilteredEqualsFalse, not(containsString("attribute name=\"e-mail\" value=\"noreply@ripe.net\"")));

        final String withOtherParameters = RestTest.target(getPort(), "whois/test/person/PP1-TEST?unfiltered=true&pretty=false").request().get(String.class);
        assertThat(withOtherParameters, containsString("attribute name=\"e-mail\" value=\"noreply@ripe.net\""));

        final String filteredByDefault = RestTest.target(getPort(), "whois/test/person/PP1-TEST").request().get(String.class);
        assertThat(filteredByDefault, not(containsString("attribute name=\"e-mail\" value=\"noreply@ripe.net\"")));
    }

    @Test
    public void lookup_mntner_without_password_and_unfiltered_param_is_partially_filtered() {
        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT?unfiltered").request().get(WhoisResources.class);

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
                new Attribute("source", "TEST", "Filtered", null, null, null)));
    }

    @Test
    public void lookup_mntner_correct_password_and_unfiltered_param_is_fully_unfiltered() {
        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT?password=test&unfiltered").request().get(WhoisResources.class);

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
                new Attribute("source", "TEST", null, null, null, null)));
    }

    @Test
    public void lookup_mntner_correct_password_without_unfiltered_param_is_partially_filtered() {
        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT?password=test").request().get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getAttributes(), contains(
                new Attribute("mntner", "OWNER-MNT"),
                new Attribute("descr", "Owner Maintainer"),
                new Attribute("admin-c", "TP1-TEST", null, "person", Link.create("http://rest-test.db.ripe.net/test/person/TP1-TEST"), null),
                new Attribute("auth", "MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/", "test", null, null, null),
                new Attribute("auth", "SSO person@net.net", null, null, null, null),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", Link.create("http://rest-test.db.ripe.net/test/mntner/OWNER-MNT"), null),
                new Attribute("source", "TEST", "Filtered", null, null, null)));
    }

    @Test
    public void lookup_mntner_incorrect_password_without_unfiltered_param_is_fully_filtered() {
        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT?password=incorrect").request().get(WhoisResources.class);

        //TODO [TP] there should be an error message in the response for the lookup with incorrect password
        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getAttributes(), contains(
                new Attribute("mntner", "OWNER-MNT"),
                new Attribute("descr", "Owner Maintainer"),
                new Attribute("admin-c", "TP1-TEST", null, "person", Link.create("http://rest-test.db.ripe.net/test/person/TP1-TEST"), null),
                new Attribute("auth", "MD5-PW", "Filtered", null, null, null),
                new Attribute("auth", "SSO", "Filtered", null, null, null),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", Link.create("http://rest-test.db.ripe.net/test/mntner/OWNER-MNT"), null),
                new Attribute("source", "TEST", "Filtered", null, null, null)));
    }

    @Test
    public void lookup_mntner_multiple_passwords_and_unfiltered_param_is_unfiltered() {
        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT?password=incorrect&password=test&unfiltered").request().get(WhoisResources.class);

        //TODO [TP] there should be an error message in the response for the lookup with incorrect password
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
                new Attribute("source", "TEST", null, null, null, null)));
    }

    @Test
    public void lookup_mntner_multiple_auth_attributes_and_unfiltered_param_is_unfiltered() {
        databaseHelper.addObject("" +
                "mntner:      AUTH-MNT\n" +
                "descr:       Maintainer\n" +
                "admin-c:     TP1-TEST\n" +
                "upd-to:      noreply@ripe.net\n" +
                "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "auth:        MD5-PW $1$5XCg9Q1W$O7g9bgeJPkpea2CkBGnz/0 #test1\n" +
                "auth:        MD5-PW $1$ZjlXZmWO$VKyuYp146Vx5b1.398zgH/ #test2\n" +
                "mnt-by:      AUTH-MNT\n" +
                "source:      TEST");

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/mntner/AUTH-MNT?password=incorrect&password=test&unfiltered").request().get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getAttributes(), contains(
                new Attribute("mntner", "AUTH-MNT"),
                new Attribute("descr", "Maintainer"),
                new Attribute("admin-c", "TP1-TEST", null, "person", Link.create("http://rest-test.db.ripe.net/test/person/TP1-TEST"), null),
                new Attribute("upd-to", "noreply@ripe.net", null, null, null, null),
                new Attribute("auth", "MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/", "test", null, null, null),
                new Attribute("auth", "MD5-PW $1$5XCg9Q1W$O7g9bgeJPkpea2CkBGnz/0", "test1", null, null, null),
                new Attribute("auth", "MD5-PW $1$ZjlXZmWO$VKyuYp146Vx5b1.398zgH/", "test2", null, null, null),
                new Attribute("mnt-by", "AUTH-MNT", null, "mntner", Link.create("http://rest-test.db.ripe.net/test/mntner/AUTH-MNT"), null),
                new Attribute("source", "TEST", null, null, null, null)));
    }

    @Test
    public void lookup_mntner_with_valid_crowd_token_without_unfiltered_param_is_filtered() {
        final WhoisResources whoisResources =
                RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT")
                        .request(MediaType.APPLICATION_XML)
                        .cookie("crowd.token_key", "valid-token")
                        .get(WhoisResources.class);

        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getAttributes(), hasItems(
                new Attribute("auth", "MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/", "test", null, null, null),
                new Attribute("auth", "SSO person@net.net", null, null, null, null),
                new Attribute("source", "TEST", "Filtered", null, null, null)));
    }

    @Test
    public void lookup_mntner_with_valid_crowd_token_and_unfiltered_param_is_unfiltered() {
        final WhoisResources whoisResources =
                RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT?unfiltered")
                        .request(MediaType.APPLICATION_XML)
                        .cookie("crowd.token_key", "valid-token")
                        .get(WhoisResources.class);

        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getAttributes(), hasItems(
                new Attribute("auth", "MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/", "test", null, null, null),
                new Attribute("auth", "SSO person@net.net"),
                new Attribute("source", "TEST")));
    }

    @Test
    public void lookup_maintainer_invalid_crowd_uuid() {
        databaseHelper.addObject(
                "mntner:         MNT-TEST\n" +
                "descr:          Test maintainer\n" +
                "admin-c:        TP1-TEST\n" +
                "upd-to:         noreply@ripe.net\n" +
                "auth:           MD5-PW $1$EmukTVYX$Z6fWZT8EAzHoOJTQI6jFJ1  # 123\n" +
                "auth:           SSO e58f4ee0-5d26-450f-a933-349ce1440fbc\n" +
                "mnt-by:         MNT-TEST\n" +
                "source:         TEST");

        try {
            RestTest.target(getPort(), "whois/TEST/mntner/MNT-TEST?password=123&unfiltered")
                    .request(MediaType.APPLICATION_XML_TYPE)
                    .cookie("crowd.access_key", "xyzinvalid")
                    .get(WhoisResources.class);
            fail();
        } catch (InternalServerErrorException e) {
            // TODO: [ES] also test that we log the error on the server side.
            assertThat(e.getResponse().readEntity(String.class), containsString("internal software error"));
        }
    }

    @Test
    public void lookup_mntner_encoded_password() {
        databaseHelper.addObject(
                "mntner:         TEST-MNT\n" +
                "descr:          Test Organisation\n" +
                "admin-c:        TP1-TEST\n" +
                "upd-to:         noreply@ripe.net\n" +
                "auth:           MD5-PW $1$GVXqt/5m$TaeN0iPr84mNoz8j3IDs//  # auth?auth \n" +
                "mnt-by:         TEST-MNT\n" +
                "source:         TEST");

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/mntner/TEST-MNT?password=auth%3Fauth")
                .request(MediaType.APPLICATION_XML_TYPE)
                .get(WhoisResources.class);

        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getAttributes(), hasItems(
                new Attribute("auth", "MD5-PW $1$GVXqt/5m$TaeN0iPr84mNoz8j3IDs//", "auth?auth", null, null, null)));
    }


    @Test
    public void lookup_irt_correct_password_and_unfiltered_param_is_unfiltered() {
        databaseHelper.addObject(TEST_IRT);

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/irt/irt-test?password=test&unfiltered").request().get(WhoisResources.class);

        //TODO [TP] there should be an error message in the response for the incorrect password
        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getAttributes(), contains(
                new Attribute("irt", "irt-test"),
                new Attribute("address", "RIPE NCC"),
                new Attribute("e-mail", "noreply@ripe.net"),
                new Attribute("admin-c", "TP1-TEST", null, "person", Link.create("http://rest-test.db.ripe.net/test/person/TP1-TEST"), null),
                new Attribute("tech-c", "TP1-TEST", null, "person", Link.create("http://rest-test.db.ripe.net/test/person/TP1-TEST"), null),
                new Attribute("auth", "MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/", "test", null, null, null),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", Link.create("http://rest-test.db.ripe.net/test/mntner/OWNER-MNT"), null),
                new Attribute("source", "TEST")));
    }

    @Test
    public void lookup_irt_incorrect_password_and_unfiltered_param_is_filtered() {
        databaseHelper.addObject(TEST_IRT);

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/irt/irt-test?password=incorrect").request().get(WhoisResources.class);

        //TODO [TP] there should be an error message in the response for the incorrect password
        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getAttributes(), contains(
                new Attribute("irt", "irt-test"),
                new Attribute("address", "RIPE NCC"),
                new Attribute("admin-c", "TP1-TEST", null, "person", Link.create("http://rest-test.db.ripe.net/test/person/TP1-TEST"), null),
                new Attribute("tech-c", "TP1-TEST", null, "person", Link.create("http://rest-test.db.ripe.net/test/person/TP1-TEST"), null),
                new Attribute("auth", "MD5-PW", "Filtered", null, null, null),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", Link.create("http://rest-test.db.ripe.net/test/mntner/OWNER-MNT"), null),
                new Attribute("source", "TEST", "Filtered", null, null, null)));
    }

    @Test
    public void lookup_mntner_xml_text() {
        databaseHelper.addObject(RpslObjectFilter.buildGenericObject(OWNER_MNT, "mntner: TRICKY-MNT", "remarks: ", "remarks: remark with # comment"));

        final String response = RestTest.target(getPort(), "whois/test/mntner/TRICKY-MNT")
                .request(MediaType.APPLICATION_XML_TYPE)
                .get(String.class);

        assertThat(response, is("" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<whois-resources xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n" +
                "<objects>\n" +
                "<object type=\"mntner\">\n" +
                "    <link xlink:type=\"locator\" xlink:href=\"http://rest-test.db.ripe.net/test/mntner/TRICKY-MNT\"/>\n" +
                "    <source id=\"test\"/>\n" +
                "    <primary-key>\n" +
                "        <attribute name=\"mntner\" value=\"TRICKY-MNT\"/>\n" +
                "    </primary-key>\n" +
                "    <attributes>\n" +
                "        <attribute name=\"mntner\" value=\"TRICKY-MNT\"/>\n" +
                "        <attribute name=\"descr\" value=\"Owner Maintainer\"/>\n" +
                "        <attribute name=\"admin-c\" value=\"TP1-TEST\" referenced-type=\"person\">\n" +
                "            <link xlink:type=\"locator\" xlink:href=\"http://rest-test.db.ripe.net/test/person/TP1-TEST\"/>\n" +
                "        </attribute>\n" +
                "        <attribute name=\"auth\" value=\"MD5-PW\" comment=\"Filtered\"/>\n" +
                "        <attribute name=\"auth\" value=\"SSO\" comment=\"Filtered\"/>\n" +
                "        <attribute name=\"remarks\" value=\"\"/>\n" +
                "        <attribute name=\"remarks\" value=\"remark with\" comment=\"comment\"/>\n" +
                "        <attribute name=\"mnt-by\" value=\"OWNER-MNT\" referenced-type=\"mntner\">\n" +
                "            <link xlink:type=\"locator\" xlink:href=\"http://rest-test.db.ripe.net/test/mntner/OWNER-MNT\"/>\n" +
                "        </attribute>\n" +
                "        <attribute name=\"source\" value=\"TEST\" comment=\"Filtered\"/>\n" +
                "    </attributes>\n" +
                "</object>\n" +
                "</objects>\n" +
                "<terms-and-conditions xlink:type=\"locator\" xlink:href=\"https://docs.db.ripe.net/terms-conditions.html\"/>\n" +
                "<version " +
                "version=\"" + applicationVersion.getVersion() + "\" " +
                "timestamp=\"" + applicationVersion.getTimestamp() + "\" " +
                "commit-id=\"" + applicationVersion.getCommitId() + "\"/>\n" +
                "</whois-resources>\n"));
    }

    @Test
    public void lookup_mntner_json_text() {
        databaseHelper.addObject(RpslObjectFilter.buildGenericObject(OWNER_MNT, "mntner: TRICKY-MNT", "remarks: ", "remarks: remark with # comment"));

        final String response = RestTest.target(getPort(), "whois/test/mntner/TRICKY-MNT")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(String.class);

        assertThat(response, is("" +
                        "{\"objects\":{\"object\":[ {\n" +
                        "  \"type\" : \"mntner\",\n" +
                        "  \"link\" : {\n" +
                        "    \"type\" : \"locator\",\n" +
                        "    \"href\" : \"http://rest-test.db.ripe.net/test/mntner/TRICKY-MNT\"\n" +
                        "  },\n" +
                        "  \"source\" : {\n" +
                        "    \"id\" : \"test\"\n" +
                        "  },\n" +
                        "  \"primary-key\" : {\n" +
                        "    \"attribute\" : [ {\n" +
                        "      \"name\" : \"mntner\",\n" +
                        "      \"value\" : \"TRICKY-MNT\"\n" +
                        "    } ]\n" +
                        "  },\n" +
                        "  \"attributes\" : {\n" +
                        "    \"attribute\" : [ {\n" +
                        "      \"name\" : \"mntner\",\n" +
                        "      \"value\" : \"TRICKY-MNT\"\n" +
                        "    }, {\n" +
                        "      \"name\" : \"descr\",\n" +
                        "      \"value\" : \"Owner Maintainer\"\n" +
                        "    }, {\n" +
                        "      \"link\" : {\n" +
                        "        \"type\" : \"locator\",\n" +
                        "        \"href\" : \"http://rest-test.db.ripe.net/test/person/TP1-TEST\"\n" +
                        "      },\n" +
                        "      \"name\" : \"admin-c\",\n" +
                        "      \"value\" : \"TP1-TEST\",\n" +
                        "      \"referenced-type\" : \"person\"\n" +
                        "    }, {\n" +
                        "      \"name\" : \"auth\",\n" +
                        "      \"value\" : \"MD5-PW\",\n" +
                        "      \"comment\" : \"Filtered\"\n" +
                        "    }, {\n" +
                        "      \"name\" : \"auth\",\n" +
                        "      \"value\" : \"SSO\",\n" +
                        "      \"comment\" : \"Filtered\"\n" +
                        "    }, {\n" +
                        "      \"name\" : \"remarks\",\n" +
                        "      \"value\" : \"\"\n" +
                        "    }, {\n" +
                        "      \"name\" : \"remarks\",\n" +
                        "      \"value\" : \"remark with\",\n" +
                        "      \"comment\" : \"comment\"\n" +
                        "    }, {\n" +
                        "      \"link\" : {\n" +
                        "        \"type\" : \"locator\",\n" +
                        "        \"href\" : \"http://rest-test.db.ripe.net/test/mntner/OWNER-MNT\"\n" +
                        "      },\n" +
                        "      \"name\" : \"mnt-by\",\n" +
                        "      \"value\" : \"OWNER-MNT\",\n" +
                        "      \"referenced-type\" : \"mntner\"\n" +
                        "    }, {\n" +
                        "      \"name\" : \"source\",\n" +
                        "      \"value\" : \"TEST\",\n" +
                        "      \"comment\" : \"Filtered\"\n" +
                        "    } ]\n" +
                        "  }\n" +
                        "} ]\n" +
                        "},\n" +
                        "\"terms-and-conditions\" : {\n" +
                        "\"type\" : \"locator\",\n" +
                        "\"href\" : \"https://docs.db.ripe.net/terms-conditions.html\"\n" +
                        "},\n" +
                        "\"version\" : {\n" +
                        "\"version\" : \"" + applicationVersion.getVersion() + "\",\n" +
                        "\"timestamp\" : \"" + applicationVersion.getTimestamp() + "\",\n" +
                        "\"commit-id\" : \"" + applicationVersion.getCommitId() + "\"\n" +
                        "}\n" +
                        "}"
        ));
    }

    @Test
    public void lookup_person_json_text() {
        final String result = RestTest.target(getPort(), "whois/test/person/TP1-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(String.class);
        assertThat(result, is(
                "{\"objects\":{\"object\":[ {\n" +
                "  \"type\" : \"person\",\n" +
                "  \"link\" : {\n" +
                "    \"type\" : \"locator\",\n" +
                "    \"href\" : \"http://rest-test.db.ripe.net/test/person/TP1-TEST\"\n" +
                "  },\n" +
                "  \"source\" : {\n" +
                "    \"id\" : \"test\"\n" +
                "  },\n" +
                "  \"primary-key\" : {\n" +
                "    \"attribute\" : [ {\n" +
                "      \"name\" : \"nic-hdl\",\n" +
                "      \"value\" : \"TP1-TEST\"\n" +
                "    } ]\n" +
                "  },\n" +
                "  \"attributes\" : {\n" +
                "    \"attribute\" : [ {\n" +
                "      \"name\" : \"person\",\n" +
                "      \"value\" : \"Test Person\"\n" +
                "    }, {\n" +
                "      \"name\" : \"address\",\n" +
                "      \"value\" : \"Singel 258\"\n" +
                "    }, {\n" +
                "      \"name\" : \"phone\",\n" +
                "      \"value\" : \"+31 6 12345678\"\n" +
                "    }, {\n" +
                "      \"name\" : \"nic-hdl\",\n" +
                "      \"value\" : \"TP1-TEST\"\n" +
                "    }, {\n" +
                "      \"link\" : {\n" +
                "        \"type\" : \"locator\",\n" +
                "        \"href\" : \"http://rest-test.db.ripe.net/test/mntner/OWNER-MNT\"\n" +
                "      },\n" +
                "      \"name\" : \"mnt-by\",\n" +
                "      \"value\" : \"OWNER-MNT\",\n" +
                "      \"referenced-type\" : \"mntner\"\n" +
                "    }, {\n" +
                "      \"name\" : \"source\",\n" +
                "      \"value\" : \"TEST\"\n" +
                "    } ]\n" +
                "  }\n" +
                "} ]\n" +
                "},\n" +
                "\"terms-and-conditions\" : {\n" +
                "\"type\" : \"locator\",\n" +
                "\"href\" : \"https://docs.db.ripe.net/terms-conditions.html\"\n" +
                "},\n" +
                "\"version\" : {\n" +
                "\"version\" : \"" + applicationVersion.getVersion() + "\",\n" +
                "\"timestamp\" : \"" + applicationVersion.getTimestamp() + "\",\n" +
                "\"commit-id\" : \"" + applicationVersion.getCommitId() + "\"\n" +
                "}\n" +
                "}"
        ));
    }

    @Test
    public void grs_lookup_autnum_json_text() {
        databaseHelper.addObject("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST-GRS\n");

        final String result = RestTest.target(getPort(), "whois/test-grs/aut-num/AS102.json").request().get(String.class);

        assertThat(result, is(
                "{\"objects\":{\"object\":[ {\n" +
                "  \"type\" : \"aut-num\",\n" +
                "  \"link\" : {\n" +
                "    \"type\" : \"locator\",\n" +
                "    \"href\" : \"http://rest-test.db.ripe.net/test-grs/aut-num/AS102\"\n" +
                "  },\n" +
                "  \"source\" : {\n" +
                "    \"id\" : \"test-grs\"\n" +
                "  },\n" +
                "  \"primary-key\" : {\n" +
                "    \"attribute\" : [ {\n" +
                "      \"name\" : \"aut-num\",\n" +
                "      \"value\" : \"AS102\"\n" +
                "    } ]\n" +
                "  },\n" +
                "  \"attributes\" : {\n" +
                "    \"attribute\" : [ {\n" +
                "      \"name\" : \"aut-num\",\n" +
                "      \"value\" : \"AS102\"\n" +
                "    }, {\n" +
                "      \"name\" : \"as-name\",\n" +
                "      \"value\" : \"End-User-2\"\n" +
                "    }, {\n" +
                "      \"name\" : \"descr\",\n" +
                "      \"value\" : \"description\"\n" +
                "    }, {\n" +
                "      \"name\" : \"admin-c\",\n" +
                "      \"value\" : \"DUMY-RIPE\"\n" +
                "    }, {\n" +
                "      \"name\" : \"tech-c\",\n" +
                "      \"value\" : \"DUMY-RIPE\"\n" +
                "    }, {\n" +
                "      \"link\" : {\n" +
                "        \"type\" : \"locator\",\n" +
                "        \"href\" : \"http://rest-test.db.ripe.net/test-grs/mntner/OWNER-MNT\"\n" +
                "      },\n" +
                "      \"name\" : \"mnt-by\",\n" +
                "      \"value\" : \"OWNER-MNT\",\n" +
                "      \"referenced-type\" : \"mntner\"\n" +
                "    }, {\n" +
                "      \"name\" : \"source\",\n" +
                "      \"value\" : \"TEST-GRS\"\n" +
                "    }, {\n" +
                "      \"name\" : \"remarks\",\n" +
                "      \"value\" : \"****************************\"\n" +
                "    }, {\n" +
                "      \"name\" : \"remarks\",\n" +
                "      \"value\" : \"* THIS OBJECT IS MODIFIED\"\n" +
                "    }, {\n" +
                "      \"name\" : \"remarks\",\n" +
                "      \"value\" : \"* Please note that all data that is generally regarded as personal\"\n" +
                "    }, {\n" +
                "      \"name\" : \"remarks\",\n" +
                "      \"value\" : \"* data has been removed from this object.\"\n" +
                "    }, {\n" +
                "      \"name\" : \"remarks\",\n" +
                "      \"value\" : \"* To view the original object, please query the RIPE Database at:\"\n" +
                "    }, {\n" +
                "      \"name\" : \"remarks\",\n" +
                "      \"value\" : \"* http://www.ripe.net/whois\"\n" +
                "    }, {\n" +
                "      \"name\" : \"remarks\",\n" +
                "      \"value\" : \"****************************\"\n" +
                "    } ]\n" +
                "  }\n" +
                "} ]\n" +
                "},\n" +
                "\"terms-and-conditions\" : {\n" +
                "\"type\" : \"locator\",\n" +
                "\"href\" : \"https://docs.db.ripe.net/terms-conditions.html\"\n" +
                "},\n" +
                "\"version\" : {\n" +
                "\"version\" : \"" + applicationVersion.getVersion() + "\",\n" +
                "\"timestamp\" : \"" + applicationVersion.getTimestamp() + "\",\n" +
                "\"commit-id\" : \"" + applicationVersion.getCommitId() + "\"\n" +
                "}\n" +
                "}"
        ));
    }

    @Test
    public void grs_lookup_autnum_xml_text() {
        databaseHelper.addObject("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST-GRS\n");

        final String result = RestTest.target(getPort(), "whois/test-grs/aut-num/AS102.xml").request().get(String.class);

        assertThat(result, is("" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<whois-resources xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n" +
                "<objects>\n" +
                "<object type=\"aut-num\">\n" +
                "    <link xlink:type=\"locator\" xlink:href=\"http://rest-test.db.ripe.net/test-grs/aut-num/AS102\"/>\n" +
                "    <source id=\"test-grs\"/>\n" +
                "    <primary-key>\n" +
                "        <attribute name=\"aut-num\" value=\"AS102\"/>\n" +
                "    </primary-key>\n" +
                "    <attributes>\n" +
                "        <attribute name=\"aut-num\" value=\"AS102\"/>\n" +
                "        <attribute name=\"as-name\" value=\"End-User-2\"/>\n" +
                "        <attribute name=\"descr\" value=\"description\"/>\n" +
                "        <attribute name=\"admin-c\" value=\"DUMY-RIPE\"/>\n" +
                "        <attribute name=\"tech-c\" value=\"DUMY-RIPE\"/>\n" +
                "        <attribute name=\"mnt-by\" value=\"OWNER-MNT\" referenced-type=\"mntner\">\n" +
                "            <link xlink:type=\"locator\" xlink:href=\"http://rest-test.db.ripe.net/test-grs/mntner/OWNER-MNT\"/>\n" +
                "        </attribute>\n" +
                "        <attribute name=\"source\" value=\"TEST-GRS\"/>\n" +
                "        <attribute name=\"remarks\" value=\"****************************\"/>\n" +
                "        <attribute name=\"remarks\" value=\"* THIS OBJECT IS MODIFIED\"/>\n" +
                "        <attribute name=\"remarks\" value=\"* Please note that all data that is generally regarded as personal\"/>\n" +
                "        <attribute name=\"remarks\" value=\"* data has been removed from this object.\"/>\n" +
                "        <attribute name=\"remarks\" value=\"* To view the original object, please query the RIPE Database at:\"/>\n" +
                "        <attribute name=\"remarks\" value=\"* http://www.ripe.net/whois\"/>\n" +
                "        <attribute name=\"remarks\" value=\"****************************\"/>\n" +
                "    </attributes>\n" +
                "</object>\n" +
                "</objects>\n" +
                "<terms-and-conditions xlink:type=\"locator\" xlink:href=\"https://docs.db.ripe.net/terms-conditions.html\"/>\n" +
                "<version " +
                "version=\"" + applicationVersion.getVersion() + "\" " +
                "timestamp=\"" + applicationVersion.getTimestamp() + "\" " +
                "commit-id=\"" + applicationVersion.getCommitId() + "\"/>\n" +
                "</whois-resources>\n"));
    }

    @Test
    public void lookup_xml_response_doesnt_contain_invalid_values() {
        databaseHelper.addObject("" +
                "mntner:      TEST-MNT\n" +
                "descr:       escape invalid values like \uDC00Brat\u001b$B!l\u001b <b> <!-- &#x0;\n" +
                "admin-c:     TP1-TEST\n" +
                "upd-to:      noreply@ripe.net\n" +
                "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "mnt-by:      TEST-MNT\n" +
                "source:      TEST");

        final String response = RestTest.target(getPort(), "whois/test/mntner/TEST-MNT")
                .request(MediaType.APPLICATION_XML)
                .get(String.class);

        assertThat(response, not(containsString("\u001b")));
        assertThat(response, not(containsString("<b>")));
        assertThat(response, not(containsString("&#x0;")));
        assertThat(response, not(containsString("<!--")));
    }

    @Test
    public void lookup_successful_error_message_not_included() {
        final String response = RestTest.target(getPort(), "whois/test/person?password=test")
                .request()
                .post(Entity.entity(map(PAULETH_PALTHEN), MediaType.APPLICATION_XML), String.class);

        assertThat(response, containsString(""+
                "    <errormessages>\n" +
                "        <errormessage severity=\"Warning\" text=\"MD5 hashed password authentication is deprecated and support will be removed at the end of 2025. Please switch to an alternative authentication method before then.\"/>\n" +
                "    </errormessages>"));
    }


    @Test
    public void lookup_invalid_suffix_should_not_return_plaintext() {
        try {
            RestTest.target(getPort(), "whois/test/aut-num/AS44217.html")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(String.class);
            fail();
        } catch (BadRequestException e) {
            assertThat(e.getResponse().readEntity(String.class), containsString("" +
                    "  \"errormessages\" : {\n" +
                    "    \"errormessage\" : [ {\n" +
                    "      \"severity\" : \"Error\",\n" +
                    "      \"text\" : \"ERROR:115: invalid search key\\n\\nSearch key entered is not valid for the specified object type(s)\\n\"\n" +
                    "    } ]\n" +
                    "  },"));
        }
    }

    @Test
    public void lookup_xml_script_injection_not_possible() {
        databaseHelper.addObject(
                "person:         Test Person\n" +
                "nic-hdl:        TP9-TEST\n" +
                "remarks:        <script>alert('hello');</script>\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST\n");

        final String response = RestTest.target(getPort(), "whois/test/person/TP9-TEST.xml")
                    .request(MediaType.APPLICATION_XML_TYPE)
                    .get(String.class);

        assertThat(response, containsString("&lt;script&gt;alert('hello');&lt;/script&gt;"));
    }

    @Test
    public void lookup_json_script_injection_not_possible() {
        databaseHelper.addObject(
                "person:         Test Person\n" +
                "nic-hdl:        TP9-TEST\n" +
                "remarks:        <script>alert('hello');</script>\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST\n");

        final String response = RestTest.target(getPort(), "whois/test/person/TP9-TEST.xml")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(String.class);

        assertThat(response, containsString("&lt;script&gt;alert('hello');&lt;/script&gt;"));
    }

    @Test
    public void lookup_autnum_with_managed_attributes() {
        databaseHelper.addObject("mntner: RIPE-NCC-END-MNT");
        databaseHelper.addObject("" +
                "aut-num:        AS102\n" +
                "as-name:        ASNAME\n" +
                "status:         ASSIGNED\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "mnt-by:         RIPE-NCC-END-MNT\n" +
                "source:         TEST\n");

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/aut-num/AS102?managed-attributes")
                .request()
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        assertThat(whoisResources.getWhoisObjects().get(0).getPrimaryKey(), contains(new Attribute("aut-num", "AS102")));
        assertThat(whoisResources.getWhoisObjects().get(0).isManaged(), is(true));
        assertThat(whoisResources.getWhoisObjects().get(0).getAttributes().get(2).getName(), is("status"));
        assertThat(whoisResources.getWhoisObjects().get(0).getAttributes().get(2).getManaged(), is(true));
    }

    @Test
    public void lookup_autnum_without_managed_attributes() {
        databaseHelper.addObject("" +
                "aut-num:        AS102\n" +
                "as-name:        ASNAME\n" +
                "status:         OTHER\n" +
                "descr:          description\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST\n");

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/aut-num/AS102?managed-attributes")
                .request()
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        assertThat(whoisResources.getWhoisObjects().get(0).getPrimaryKey(), contains(new Attribute("aut-num", "AS102")));
        assertThat(whoisResources.getWhoisObjects().get(0).isManaged(), is(false));
        assertThat(whoisResources.getWhoisObjects().get(0).getAttributes().get(2).getName(), is("status"));
        assertThat(whoisResources.getWhoisObjects().get(0).getAttributes().get(2).getManaged(), is(nullValue()));
    }

    @Test
    public void lookup_inetnum_managed_attributes_resource_holder_abuse_contact() {
        databaseHelper.addObject(
                "mntner:       RIPE-NCC-HM-MNT\n" +
                "source:   TEST");
        databaseHelper.addObject(
                "organisation: ORG-TO1-TEST\n" +
                 "org-name:     Test Organisation\n" +
                 "abuse-c:      TR1-TEST\n" +
                 "source:       TEST");
        databaseHelper.addObject(
                INETNUM_OBJECT);

        final WhoisResources response = RestTest.target(getPort(), "whois/test/inetnum/10.0.0.0%20-%2010.0.0.255?managed-attributes&resource-holder&abuse-contact")
                .request(MediaType.APPLICATION_XML_TYPE)
                .get(WhoisResources.class);

        assertThat(response.getWhoisObjects(), hasSize(1));
        assertThat(response.getWhoisObjects().get(0).getPrimaryKey().get(0).getValue(), is("10.0.0.0 - 10.0.0.255"));
        assertThat(response.getWhoisObjects().get(0).getResourceHolder().getOrgName(), is("Test Organisation"));
        assertThat(response.getWhoisObjects().get(0).getResourceHolder().getOrgKey(), is("ORG-TO1-TEST"));
        assertThat(response.getWhoisObjects().get(0).getAbuseContact().getEmail(), is("abuse@test.net"));
        assertThat(response.getWhoisObjects().get(0).getAbuseContact().getKey(), is("TR1-TEST"));
        assertThat(response.getWhoisObjects().get(0).isManaged(), is(true));
        assertThat(response.getWhoisObjects().get(0).getAttributes().get(0).getManaged(), is(true));    // inetnum
        assertThat(response.getWhoisObjects().get(0).getAttributes().get(1).getManaged(), is(true));    // status
        assertThat(response.getWhoisObjects().get(0).getAttributes().get(2).getManaged(), is(true));    // org
        assertThat(response.getWhoisObjects().get(0).getAttributes().get(3).getManaged(), is(nullValue()));   // mnt-by
        assertThat(response.getWhoisObjects().get(0).getAttributes().get(4).getManaged(), is(true));    // mnt-by
        assertThat(response.getWhoisObjects().get(0).getAttributes().get(5).getManaged(), is(true));    // source
    }

    @Test
    public void validate_country_managed_attribute_only_for_organisation() {
        databaseHelper.addObject(
                "mntner:       RIPE-NCC-HM-MNT\n" +
                        "source:   TEST");
        databaseHelper.addObject(
                "organisation: ORG-TO1-TEST\n" +
                        "org-name:     Test Organisation\n" +
                        "org-type:     LIR\n" +
                        "country:      NL\n" +
                        "abuse-c:      TR1-TEST\n" +
                        "source:       TEST");
        databaseHelper.addObject(
                "inetnum:       10.0.0.0 - 10.0.0.255\n" +
                        "status:   ALLOCATED PA\n" +
                        "org:      ORG-TO1-TEST\n" +
                        "country:  NL\n" +
                        "mnt-by:   OWNER-MNT\n" +
                        "mnt-by:   RIPE-NCC-HM-MNT\n" +
                        "source:   TEST");


        final WhoisResources searchOrg = RestTest.target(getPort(), "whois/test/organisation/ORG-TO1-TEST?managed-attributes")
                .request(MediaType.APPLICATION_XML_TYPE)
                .get(WhoisResources.class);

        assertThat(searchOrg.getWhoisObjects(), hasSize(1));
        assertThat(searchOrg.getWhoisObjects().get(0).getPrimaryKey().get(0).getValue(), is("ORG-TO1-TEST"));
        assertThat(searchOrg.getWhoisObjects().get(0).isManaged(), is(true));
        assertThat(searchOrg.getWhoisObjects().get(0).getAttributes().get(3).getName(), is("country"));
        assertThat(searchOrg.getWhoisObjects().get(0).getAttributes().get(3).getManaged(), is(true));


        final WhoisResources searchinetnum = RestTest.target(getPort(), "whois/test/inetnum/10.0.0.0%20-%2010.0.0.255?managed-attributes&resource-holder&abuse-contact")
                .request(MediaType.APPLICATION_XML_TYPE)
                .get(WhoisResources.class);

        assertThat(searchinetnum.getWhoisObjects(), hasSize(1));
        assertThat(searchinetnum.getWhoisObjects().get(0).getPrimaryKey().get(0).getValue(), is("10.0.0.0 - 10.0.0.255"));
        assertThat(searchinetnum.getWhoisObjects().get(0).getAttributes().get(3).getName(), is("country"));
        assertThat(searchinetnum.getWhoisObjects().get(0).getAttributes().get(3).getManaged(), is(nullValue()));
    }

    @Test
    public void lookup_inetnum_non_managed_attributes_resource_holder_abuse_contact() {
        databaseHelper.addObject(
                "mntner:       RIPE-NCC-HM-MNT\n" +
                "source:   TEST");
        databaseHelper.addObject(
                "inetnum:       11.0.0.0 - 11.0.0.255\n" +
                 "status:   ASSIGNED PI\n" +
                 "mnt-by:   OWNER-MNT\n" +
                 "source:   TEST");

        final WhoisResources response = RestTest.target(getPort(), "whois/test/inetnum/11.0.0.0%20-%2011.0.0.255?managed-attributes&resource-holder&abuse-contact")
                .request(MediaType.APPLICATION_XML_TYPE)
                .get(WhoisResources.class);

        assertThat(response.getWhoisObjects(), hasSize(1));
        assertThat(response.getWhoisObjects().get(0).getPrimaryKey().get(0).getValue(), is("11.0.0.0 - 11.0.0.255"));
        assertThat(response.getWhoisObjects().get(0).getResourceHolder(), is(nullValue()));
        assertThat(response.getWhoisObjects().get(0).getAbuseContact(), is(nullValue()));
        assertThat(response.getWhoisObjects().get(0).isManaged(), is(false));
        assertThat(response.getWhoisObjects().get(0).getAttributes().get(0).getManaged(), is(nullValue()));     // inetnum attribute
    }


    @Test
    public void lookup_with_override_succeeds() {
        databaseHelper.insertUser(User.createWithPlainTextPassword("mherran", "zoh", ObjectType.MNTNER));

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT")
                .queryParam("unfiltered", "")
                .queryParam("override", encode("mherran,zoh,reason {notify=false}"))
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject object = whoisResources.getWhoisObjects().getFirst();

        assertThat(object.getAttributes(), containsInAnyOrder(
                new Attribute("mntner", "OWNER-MNT"),
                new Attribute("descr", "Owner Maintainer"),
                new Attribute("admin-c", "TP1-TEST", null, "person", Link.create("http://rest-test.db.ripe.net/test/person/TP1-TEST"), null),
                new Attribute("upd-to", "noreply@ripe.net"),
                new Attribute("auth", "MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/", "test", null, null, null),
                new Attribute("auth", "SSO person@net.net"),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", Link.create("http://rest-test.db.ripe.net/test/mntner/OWNER-MNT"), null),
                new Attribute("source", "TEST")));

        assertThat(whoisResources.getTermsAndConditions().getHref(), is(WhoisResources.TERMS_AND_CONDITIONS));
    }

    @Test
    public void lookup_with_multiple_override_succeeds() {
        databaseHelper.insertUser(User.createWithPlainTextPassword("mherran", "zoh", ObjectType.MNTNER));

        // Just the first override is taken into consideration
        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT")
                .queryParam("unfiltered", "")
                .queryParam("override", encode("mherran,zoh,reason {notify=false}"))
                .queryParam("override", encode("mherran,zohadfasfd,reason {notify=false}"))
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject object = whoisResources.getWhoisObjects().getFirst();

        assertThat(object.getAttributes(), containsInAnyOrder(
                new Attribute("mntner", "OWNER-MNT"),
                new Attribute("descr", "Owner Maintainer"),
                new Attribute("admin-c", "TP1-TEST", null, "person", Link.create("http://rest-test.db.ripe.net/test/person/TP1-TEST"), null),
                new Attribute("upd-to", "noreply@ripe.net"),
                new Attribute("auth", "MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/", "test", null, null, null),
                new Attribute("auth", "SSO person@net.net"),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", Link.create("http://rest-test.db.ripe.net/test/mntner/OWNER-MNT"), null),
                new Attribute("source", "TEST")));

        assertThat(whoisResources.getTermsAndConditions().getHref(), is(WhoisResources.TERMS_AND_CONDITIONS));
    }

    @Test
    public void lookup_with_override_non_trusted_ip_with_sso_succeeds() {
        databaseHelper.insertUser(User.createWithPlainTextPassword("db_e2e_1", "zoh", ObjectType.MNTNER));

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT")
                .queryParam("unfiltered", "")
                .queryParam("override", encode("db_e2e_1,zoh,reason {notify=false}"))
                .queryParam("clientIp", "2001:fff:001::")
                .request(MediaType.APPLICATION_XML)
                .cookie("crowd.token_key", "db_e2e_1")
                .get(WhoisResources.class);

        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject object = whoisResources.getWhoisObjects().getFirst();

        assertThat(object.getAttributes(), containsInAnyOrder(
                new Attribute("mntner", "OWNER-MNT"),
                new Attribute("descr", "Owner Maintainer"),
                new Attribute("admin-c", "TP1-TEST", null, "person", Link.create("http://rest-test.db.ripe.net/test/person/TP1-TEST"), null),
                new Attribute("upd-to", "noreply@ripe.net"),
                new Attribute("auth", "MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/", "test", null, null, null),
                new Attribute("auth", "SSO person@net.net"),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", Link.create("http://rest-test.db.ripe.net/test/mntner/OWNER-MNT"), null),
                new Attribute("source", "TEST")));

        assertThat(whoisResources.getTermsAndConditions().getHref(), is(WhoisResources.TERMS_AND_CONDITIONS));
    }

    @Test
    public void lookup_with_override_non_trusted_ip_without_sso_then_filtered() {
        databaseHelper.insertUser(User.createWithPlainTextPassword("db_e2e_1", "zoh", ObjectType.MNTNER));

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT")
                .queryParam("unfiltered", "")
                .queryParam("override", encode("db_e2e_1,zoh,reason {notify=false}"))
                .queryParam("clientIp", "2001:fff:001::")
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject object = whoisResources.getWhoisObjects().getFirst();

        assertThat(object.getAttributes(), containsInAnyOrder(
                new Attribute("mntner", "OWNER-MNT"),
                new Attribute("descr", "Owner Maintainer"),
                new Attribute("upd-to", "noreply@ripe.net"),
                new Attribute("admin-c", "TP1-TEST", null, "person", Link.create("http://rest-test.db.ripe.net/test/person/TP1-TEST"), null),
                new Attribute("auth", "MD5-PW", "Filtered", null, null, null),
                new Attribute("auth", "SSO", "Filtered", null, null, null),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", Link.create("http://rest-test.db.ripe.net/test/mntner/OWNER-MNT"), null),
                new Attribute("source", "TEST", "Filtered", null, null, null)));

        assertThat(whoisResources.getTermsAndConditions().getHref(), is(WhoisResources.TERMS_AND_CONDITIONS));
    }

    @Test
    public void lookup_with_override_with_null_username_then_filtered() {
        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT")
                .queryParam("unfiltered", "")
                .queryParam("override", encode("null,zoh,reason {notify=false}"))
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject object = whoisResources.getWhoisObjects().getFirst();

        assertThat(object.getAttributes(), containsInAnyOrder(
                new Attribute("mntner", "OWNER-MNT"),
                new Attribute("descr", "Owner Maintainer"),
                new Attribute("upd-to", "noreply@ripe.net"),
                new Attribute("admin-c", "TP1-TEST", null, "person", Link.create("http://rest-test.db.ripe.net/test/person/TP1-TEST"), null),
                new Attribute("auth", "MD5-PW", "Filtered", null, null, null),
                new Attribute("auth", "SSO", "Filtered", null, null, null),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", Link.create("http://rest-test.db.ripe.net/test/mntner/OWNER-MNT"), null),
                new Attribute("source", "TEST", "Filtered", null, null, null)));

        assertThat(whoisResources.getTermsAndConditions().getHref(), is(WhoisResources.TERMS_AND_CONDITIONS));
    }

    // create

    @Test
    public void create_succeeds() {
        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person?password=test")
                .request()
                .post(Entity.entity(map(PAULETH_PALTHEN), MediaType.APPLICATION_XML), WhoisResources.class);

        assertThat(whoisResources.getLink().getHref(), is(String.format("http://localhost:%s/test/person", getPort())));
        assertThat(whoisResources.getErrorMessages(), hasSize(1));
        assertThat(whoisResources.getErrorMessages().getFirst().getText(), is("MD5 hashed password authentication is deprecated and support will be " +
                "removed at the end of 2025. Please switch to an alternative authentication method before then."));

        final WhoisObject object = whoisResources.getWhoisObjects().getFirst();

        assertThat(object.getAttributes(), containsInAnyOrder(
                new Attribute("person", "Pauleth Palthen"),
                new Attribute("address", "Singel 258"),
                new Attribute("phone", "+31-1234567890"),
                new Attribute("e-mail", "noreply@ripe.net"),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", Link.create("http://rest-test.db.ripe.net/test/mntner/OWNER-MNT"), null),
                new Attribute("nic-hdl", "PP1-TEST"),
                new Attribute("remarks", "remark"),
                new Attribute("created", "2001-02-04T17:00:00Z"),
                new Attribute("last-modified", "2001-02-04T17:00:00Z"),
                new Attribute("source", "TEST")));

        assertThat(whoisResources.getTermsAndConditions().getHref(), is(WhoisResources.TERMS_AND_CONDITIONS));
    }

    @Test
    public void create_asSet_same_source_aut_num_succeeds() {
        final RpslObject TEST_AS_SET = RpslObject.parse("""
                as-set:     AS3333:AS-TEST
                tech-c:     TP1-TEST
                admin-c:    TP1-TEST
                mnt-by:     OWNER-MNT
                source:     TEST-NONAUTH
                """);

        databaseHelper.addObject("""
                    aut-num:    AS3333
                    as-name:    End-User-1
                    tech-c:     TP1-TEST
                    admin-c:    TP1-TEST
                    mnt-by:     OWNER-MNT
                    source:     TEST-NONAUTH
                    """);

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/as-set?password=test")
                .request()
                .post(Entity.entity(map(TEST_AS_SET), MediaType.APPLICATION_XML), WhoisResources.class);
        assertThat(whoisResources.getErrorMessages(), hasSize(1));
        assertThat(whoisResources.getErrorMessages().getFirst().getText(), is("MD5 hashed password authentication is deprecated and support will be " +
                "removed at the end of 2025. Please switch to an alternative authentication method before then."));
        final WhoisObject object = whoisResources.getWhoisObjects().getFirst();

        assertThat(object.getSource().getId(), is("test-nonauth"));
    }
    @Test
    public void create_asSet_non_auth_aut_num_succeeds() {
        final RpslObject TEST_AS_SET = RpslObject.parse("""
                as-set:     AS3333:AS-TEST
                tech-c:     TP1-TEST
                admin-c:    TP1-TEST
                mnt-by:     OWNER-MNT
                source:     TEST
                """);

        databaseHelper.addObject("""
                    aut-num:    AS3333
                    as-name:    End-User-1
                    tech-c:     TP1-TEST
                    admin-c:    TP1-TEST
                    mnt-by:     OWNER-MNT
                    source:     TEST-NONAUTH
                    """);

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/as-set?password=test")
                .request()
                .post(Entity.entity(map(TEST_AS_SET), MediaType.APPLICATION_XML), WhoisResources.class);
        assertThat(whoisResources.getErrorMessages(), hasSize(2));
        assertThat(whoisResources.getErrorMessages().getFirst().getText(), is("MD5 hashed password authentication is deprecated and support will be " +
                "removed at the end of 2025. Please switch to an alternative authentication method before then."));
        assertThat(whoisResources.getErrorMessages().get(1).toString(), is("The \"source:\" attribute value has been updated from \"TEST\" to \"TEST-NONAUTH\" to match the referenced AUT-NUM \"AS3333\""));
        final WhoisObject object = whoisResources.getWhoisObjects().getFirst();

        assertThat(object.getSource().getId(), is("test-nonauth"));
    }


    @Test
    public void create_asSet_non_auth_two_aut_num_succeeds() {
        final RpslObject TEST_AS_SET = RpslObject.parse("""
                as-set:     AS3333:AS-TEST:AS12654
                tech-c:     TP1-TEST
                admin-c:    TP1-TEST
                mnt-by:     OWNER-MNT
                source:     TEST
                """);

        databaseHelper.addObject("""
                    aut-num:    AS3333
                    as-name:    End-User-1
                    tech-c:     TP1-TEST
                    admin-c:    TP1-TEST
                    mnt-by:     OWNER-MNT
                    source:     TEST-NONAUTH
                    """);

        databaseHelper.addObject("""
                    as-set:     AS3333:AS-TEST
                    tech-c:     TP1-TEST
                    admin-c:    TP1-TEST
                    mnt-by:     OWNER-MNT
                    source:     TEST
                    """);

        databaseHelper.addObject("""
                    mntner:      ANOTHER-MNT
                    descr:       Owner Maintainer
                    admin-c:     TP1-TEST
                    upd-to:      noreply@ripe.net
                    auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/
                    auth:        SSO person@net.net
                    source:      TEST
                    """);

        databaseHelper.addObject("""
                    aut-num:    AS12654
                    as-name:    End-User-1
                    tech-c:     TP1-TEST
                    admin-c:    TP1-TEST
                    mnt-by:     ANOTHER-MNT
                    source:     TEST
                    """);

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/as-set?password=test")
                .request()
                .post(Entity.entity(map(TEST_AS_SET), MediaType.APPLICATION_XML), WhoisResources.class);
        assertThat(whoisResources.getErrorMessages(), hasSize(2));
        assertThat(whoisResources.getErrorMessages().getFirst().getText(), is("MD5 hashed password authentication is deprecated and support will be " +
                "removed at the end of 2025. Please switch to an alternative authentication method before then."));

        assertThat(whoisResources.getErrorMessages().get(1).toString(), is("The \"source:\" attribute value has been updated from \"TEST\" to \"TEST-NONAUTH\" to match the referenced AUT-NUM \"AS3333\""));
        final WhoisObject object = whoisResources.getWhoisObjects().getFirst();

        assertThat(object.getSource().getId(), is("test-nonauth"));
    }

    @Test
    public void create_nonauth_asSet_test_aut_num_succed() {

            final RpslObject TEST_AS_SET = RpslObject.parse("""
                    as-set:     AS3333:AS-TEST
                    tech-c:     TP1-TEST
                    admin-c:    TP1-TEST
                    mnt-by:     OWNER-MNT
                    source:     TEST-NONAUTH
                    """);

            databaseHelper.addObject("""
                    aut-num:    AS3333
                    as-name:    End-User-1
                    tech-c:     TP1-TEST
                    admin-c:    TP1-TEST
                    mnt-by:     OWNER-MNT
                    source:     TEST
                    """);

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/as-set?password=test")
                    .request()
                    .post(Entity.entity(map(TEST_AS_SET), MediaType.APPLICATION_XML), WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), hasSize(2));
        assertThat(whoisResources.getErrorMessages().getFirst().getText(), is("MD5 hashed password authentication is deprecated and support will be " +
                "removed at the end of 2025. Please switch to an alternative authentication method before then."));
        assertThat(whoisResources.getErrorMessages().get(1).toString(), is("The \"source:\" attribute value has been updated from \"TEST-NONAUTH\" to \"TEST\" to match the referenced AUT-NUM \"AS3333\""));
        final WhoisObject object = whoisResources.getWhoisObjects().getFirst();

        assertThat(object.getSource().getId(), is("test"));
    }

    @Test
    public void update_person_add_ripe_ncc_remarks_fails() {
        try {
            final RpslObject update = new RpslObjectBuilder(TEST_PERSON)
                    .addAttributeSorted(new RpslAttribute(AttributeType.REMARKS, "Remark added by the RIPE NCC: Test"))
                    .get();

            RestTest.target(getPort(), "whois/test/person/TP1-TEST?password=test")
                    .request()
                    .put(Entity.entity(whoisObjectMapper.mapRpslObjects(FormattedClientAttributeMapper.class, update), MediaType.APPLICATION_XML),
                                    WhoisResources.class);
            fail();
        } catch (BadRequestException e) {
            assertThat(e.getResponse().readEntity(WhoisResources.class).getErrorMessages().get(0).toString(),
                    containsString("The \"remarks\" attribute can only be added or removed by the RIPE NCC"));
        }
    }

    @Test
    public void update_person_remove_ripe_ncc_remarks_fails() {
        final RpslObject update = new RpslObjectBuilder(TEST_PERSON)
                .addAttributeSorted(new RpslAttribute(AttributeType.REMARKS, "Remark added by the RIPE NCC: Test"))
                .get();

        databaseHelper.updateObject(update);

        try {

            final RpslObject updatedRemovedRemarks = new RpslObjectBuilder(TEST_PERSON)
                    .removeAttribute(new RpslAttribute(AttributeType.REMARKS, "Remark added by the RIPE NCC: Test"))
                    .get();

            RestTest.target(getPort(), "whois/test/person/TP1-TEST?password=test")
                    .request()
                    .put(Entity.entity(whoisObjectMapper.mapRpslObjects(FormattedClientAttributeMapper.class, updatedRemovedRemarks), MediaType.APPLICATION_XML),
                                    WhoisResources.class);
            fail();
        } catch (BadRequestException e) {
            assertThat(e.getResponse().readEntity(WhoisResources.class).getErrorMessages().get(0).toString(),
                    containsString("The \"remarks\" attribute can only be added or removed by the RIPE NCC"));
        }
    }

    @Test
    public void update_person_edit_ripe_ncc_remarks_fails() {
        final RpslObject update = new RpslObjectBuilder(TEST_PERSON)
                .addAttributeSorted(new RpslAttribute(AttributeType.REMARKS, "Remark added by the RIPE NCC: Test"))
                .get();

        databaseHelper.updateObject(update);

        try {

            final RpslObject updatedRemovedRemarks = new RpslObjectBuilder(TEST_PERSON)
                    .removeAttribute(new RpslAttribute(AttributeType.REMARKS, "Remark added by the RIPE NCC: 123 Test 123"))
                    .get();

            RestTest.target(getPort(), "whois/test/person/TP1-TEST?password=test")
                    .request()
                    .put(Entity.entity(whoisObjectMapper.mapRpslObjects(FormattedClientAttributeMapper.class, updatedRemovedRemarks), MediaType.APPLICATION_XML),
                            WhoisResources.class);
            fail();
        } catch (BadRequestException e) {
            assertThat(e.getResponse().readEntity(WhoisResources.class).getErrorMessages().get(0).toString(),
                    containsString("The \"remarks\" attribute can only be added or removed by the RIPE NCC"));
        }
    }

    @Test
    public void update_person_add_ripe_ncc_remarks_override_success() {
        databaseHelper.insertUser(User.createWithPlainTextPassword("agoston", "zoh", ObjectType.PERSON));

        final RpslObject update = new RpslObjectBuilder(TEST_PERSON)
                    .addAttributeSorted(new RpslAttribute(AttributeType.REMARKS, "Remark added by the RIPE NCC: Test"))
                    .get();

        final Response response = RestTest.target(getPort(), "whois/test/person/TP1-TEST")
                .queryParam("override", "agoston,zoh,reason")
                .request()
                    .put(Entity.entity(whoisObjectMapper.mapRpslObjects(FormattedClientAttributeMapper.class, update), MediaType.APPLICATION_XML),
                            Response.class);

        assertThat(response.getStatus(), is(HttpStatus.OK_200));
        assertThat(databaseHelper.lookupObject(ObjectType.PERSON, "TP1-TEST").getValueForAttribute(AttributeType.REMARKS).toString(), is("Remark added by the RIPE NCC: Test"));
    }

    @Test
    public void update_person_remove_ripe_ncc_remarks_override_sucess() {
        databaseHelper.insertUser(User.createWithPlainTextPassword("agoston", "zoh", ObjectType.PERSON));

        final RpslObject update = new RpslObjectBuilder(TEST_PERSON)
                .addAttributeSorted(new RpslAttribute(AttributeType.REMARKS, "Remark added by the RIPE NCC: Test"))
                .get();

        databaseHelper.updateObject(update);

        final RpslObject updatedRemovedRemarks = new RpslObjectBuilder(TEST_PERSON)
                    .removeAttribute(new RpslAttribute(AttributeType.REMARKS, "Remark added by the RIPE NCC: Test"))
                    .get();

        final Response response = RestTest.target(getPort(), "whois/test/person/TP1-TEST")
                    .queryParam("override", "agoston,zoh,reason")
                    .request()
                    .put(Entity.entity(whoisObjectMapper.mapRpslObjects(FormattedClientAttributeMapper.class, updatedRemovedRemarks), MediaType.APPLICATION_XML),
                            Response.class);

        assertThat(response.getStatus(), is(HttpStatus.OK_200));
    }

    @Test
    public void update_person_remove_ripe_ncc_remarks_rs_sucess() {
        databaseHelper.addObject(
                "mntner:       RIPE-NCC-HM-MNT\n" +
                "auth:        MD5-PW $1$mV2gSZtj$1oVwjZr0ecFZQHsNbw2Ss.  #hm\n" +
                        "source:   TEST");

        final RpslObject update = new RpslObjectBuilder(TEST_PERSON)
                .addAttributeSorted(new RpslAttribute(AttributeType.MNT_BY, "RIPE-NCC-HM-MNT"))
                .addAttributeSorted(new RpslAttribute(AttributeType.REMARKS, "Remark added by the RIPE NCC: Test"))
                .get();

        databaseHelper.updateObject(update);

        final RpslObject updatedRemovedRemarks = new RpslObjectBuilder(TEST_PERSON)
                .removeAttribute(new RpslAttribute(AttributeType.REMARKS, "Remark added by the RIPE NCC: Test"))
                .get();

        final Response response = RestTest.target(getPort(), "whois/test/person/TP1-TEST?password=hm")
                .request()
                .put(Entity.entity(whoisObjectMapper.mapRpslObjects(FormattedClientAttributeMapper.class, updatedRemovedRemarks), MediaType.APPLICATION_XML),
                        Response.class);

        assertThat(response.getStatus(), is(HttpStatus.OK_200));
    }

    @Test
    public void create_object_add_ripe_ncc_remarks_override_success() {
        databaseHelper.insertUser(User.createWithPlainTextPassword("agoston", "zoh", ObjectType.PERSON));

        final RpslObject update = new RpslObjectBuilder(PAULETH_PALTHEN)
                .addAttributeSorted(new RpslAttribute(AttributeType.REMARKS, "Remark added by the RIPE NCC: Test"))
                .get();

        final Response whoisResources = RestTest.target(getPort(), "whois/test/person")
                .queryParam("override", "agoston,zoh,reason")
                .request()
                .post(Entity.entity(map(update), MediaType.APPLICATION_XML), Response.class);

        assertThat(whoisResources.getStatus(), is(HttpStatus.OK_200));
    }

    @Test
    public void create_object_add_ripe_ncc_remarks_fails() {

        final RpslObject update = new RpslObjectBuilder(PAULETH_PALTHEN)
                .addAttributeSorted(new RpslAttribute(AttributeType.REMARKS, "Remark added by the RIPE NCC: Test"))
                .get();

       try {
           RestTest.target(getPort(), "whois/test/person?password=test")
                   .request()
                   .post(Entity.entity(map(update), MediaType.APPLICATION_XML), WhoisResources.class);
           fail();
       } catch (BadRequestException e) {
           assertThat(e.getResponse().readEntity(WhoisResources.class).getErrorMessages().get(0).toString(),
                   containsString("The \"remarks\" attribute can only be added or removed by the RIPE NCC"));
       }
    }

    @Test
    public void create_asSet_non_existing_aut_num_bad_request() {
        try {
            final RpslObject TEST_AS_SET = RpslObject.parse("""
                    as-set:    AS3333:AS-TEST
                    tech-c:   TP1-TEST
                    admin-c:  TP1-TEST
                    mnt-by:    OWNER-MNT
                    source:    TEST
                    """);
            RestTest.target(getPort(), "whois/test/as-set?password=test")
                    .request()
                    .post(Entity.entity(map(TEST_AS_SET), MediaType.APPLICATION_JSON_TYPE), WhoisResources.class);
            fail();
        } catch (BadRequestException e) {
            assertThat(e.getResponse().readEntity(WhoisResources.class).getErrorMessages().get(0).toString(),
                    containsString("Parent object AS3333 not found"));
        }
    }

    @Test
    public void create_flat_asSet_bad_request() {
        try {
            final RpslObject TEST_AS_SET = RpslObject.parse("""
                    as-set:     AS-TEST
                    tech-c:     TP1-TEST
                    admin-c:    TP1-TEST
                    mnt-by:     OWNER-MNT
                    source:     TEST-NONAUTH
                    """);

            databaseHelper.addObject("""
                    aut-num:    AS3333
                    as-name:    End-User-1
                    tech-c:     TP1-TEST
                    admin-c:    TP1-TEST
                    mnt-by:     OWNER-MNT
                    source:     TEST
                    """);

            RestTest.target(getPort(), "whois/test/as-set?password=test")
                    .request()
                    .post(Entity.entity(map(TEST_AS_SET), MediaType.APPLICATION_XML), WhoisResources.class);
            fail();
        } catch (BadRequestException e) {
            final WhoisResources whoisResources = e.getResponse().readEntity(WhoisResources.class);
            assertThat(whoisResources.getErrorMessages().get(0).toString(), containsString("Cannot create AS-SET " +
                    "object with a " +
                    "short format name. Only hierarchical AS-SET creation is allowed, i.e. at least one ASN must be referenced"));
        }
    }

    @Test
    public void create_succeeds_plus_character_and_space_in_password() {
        final RpslObject ownerMnt = new RpslObjectBuilder(OWNER_MNT)
                    .addAttribute(1, new RpslAttribute(AttributeType.AUTH, String.format("MD5-PW %s", PasswordHelper.hashMd5Password("+Pass word+"))))
                    .get();
        databaseHelper.updateObject(ownerMnt);

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person")
                .queryParam("password", "+Pass word+")
                .request()
                .post(Entity.entity(map(PAULETH_PALTHEN), MediaType.APPLICATION_XML), WhoisResources.class);

        assertThat(whoisResources.getLink().getHref(), is(String.format("http://localhost:%s/test/person", getPort())));
        assertThat(whoisResources.getErrorMessages(), hasSize(1));
        assertThat(whoisResources.getErrorMessages().getFirst().getText(), is("MD5 hashed password authentication is deprecated and support will be " +
                "removed at the end of 2025. Please switch to an alternative authentication method before then."));
        assertThat(databaseHelper.lookupObject(ObjectType.PERSON, "PP1-TEST"), is(not(nullValue())));
    }

    @Test
    public void create_contains_changed_attribute() {
        final RpslObject paulethPalthenWithChanged = RpslObject.parse("" +
            "person:    Pauleth Palthen\n" +
            "address:   Singel 258\n" +
            "phone:     +31-1234567890\n" +
            "e-mail:    noreply@ripe.net\n" +
            "mnt-by:    OWNER-MNT\n" +
            "nic-hdl:   PP1-TEST\n" +
            "remarks:   remark\n" +
            "changed:   user@host.org 20171025\n" +
            "source:    TEST\n");

        RestTest.target(getPort(), "whois/test/person?password=test")
                .request()
                .post(Entity.entity(map(paulethPalthenWithChanged), MediaType.APPLICATION_XML), WhoisResources.class);

        assertThat(databaseHelper.lookupObject(ObjectType.PERSON, "PP1-TEST").containsAttribute(AttributeType.CHANGED), is(false));
    }

    @Test
    public void create_contains_changed_attributes() {
        final RpslObject paulethPalthenWithChanged = RpslObject.parse("" +
            "person:    Pauleth Palthen\n" +
            "address:   Singel 258\n" +
            "phone:     +31-1234567890\n" +
            "e-mail:    noreply@ripe.net\n" +
            "mnt-by:    OWNER-MNT\n" +
            "nic-hdl:   PP1-TEST\n" +
            "remarks:   remark\n" +
            "changed:   user@host.org 20171025\n" +
            "changed:   user1@host.org 20171026\n" +
            "changed:   user2@host.org 20171027\n" +
            "source:    TEST\n");

        RestTest.target(getPort(), "whois/test/person?password=test")
                .request()
                .post(Entity.entity(map(paulethPalthenWithChanged), MediaType.APPLICATION_XML), WhoisResources.class);

        assertThat(databaseHelper.lookupObject(ObjectType.PERSON, "PP1-TEST").containsAttribute(AttributeType.CHANGED), is(false));
    }

    @Disabled("TODO: [ES] #320 confusing error response")
    @Test
    public void create_invalid_object_type_on_first_attribute() {
        try {
         RestTest.target(getPort(), "whois/test/domain?password=test")
            .request()
            .post(Entity.entity(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n" +
                        "<whois-resources>\n" +
                        "<objects>\n" +
                        "<object type=\"domain\">\n" +
                        "<source id=\"ripe\"/>\n" +
                        "<attributes>\n" +
                        "<attribute name=\"descr\" value=\"description\"/>\n" +
                        "</attributes>\n" +
                        "</object>\n" +
                        "</objects>\n" +
                        "</whois-resources>", MediaType.APPLICATION_XML), String.class);
            fail();
        } catch (BadRequestException e) {
            assertThat(e.getResponse().readEntity(String.class), not(containsString("Invalid object type: descr")));
        }
    }

    @Test
    public void create_xml_internal_entity_expansion_limit() {
        final String whoisResources =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE lolz [\n" +
                "<!ENTITY lol \"lol\">\n" +
                "<!ENTITY lol1 \"&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;\">\n" +
                "<!ENTITY lol2 \"&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;\">\n" +
                "<!ENTITY lol3 \"&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;\">\n" +
                "<!ENTITY lol4 \"&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;\">\n" +
                "<!ENTITY lol5 \"&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;\">\n" +
                "<!ENTITY lol6 \"&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;\">\n" +
                "<!ENTITY lol7 \"&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;\">\n" +
                "<!ENTITY lol8 \"&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;\">\n" +
                "<!ENTITY lol9 \"&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;\">\n" +
                "]>\n" +
                "<whois-resources xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n" +
                "    <objects>\n" +
                "        <object type=\"person\">\n" +
                "            <source id=\"TEST\">&lol9;</source>\n" +
                "            <attributes>\n" +
                "                <attribute name=\"person\" value=\"New Person\"/>\n" +
                "                <attribute name=\"address\" value=\"Amsterdam\"/>\n" +
                "                <attribute name=\"phone\" value=\"+31-1234567890\"/>\n" +
                "                <attribute name=\"mnt-by\" value=\"OWNER-MNT\"/>\n" +
                "                <attribute name=\"nic-hdl\" value=\"AUTO-1\"/>\n" +
                "                <attribute name=\"source\" value=\"TEST\"/>\n" +
                "            </attributes>\n" +
                "        </object>\n" +
                "    </objects>\n" +
                "</whois-resources>\n";

        try {
            RestTest.target(getPort(), "whois/test/person?password=test")
                .request()
                .post(Entity.entity(whoisResources, MediaType.APPLICATION_XML_TYPE), WhoisResources.class);
            fail();
        } catch (BadRequestException e) {
            final WhoisResources response = e.getResponse().readEntity(WhoisResources.class);
            RestTest.assertErrorCount(response, 1);
            RestTest.assertErrorMessage(response, 0, "Error",
                "XML processing exception: %s (line: %s, column: %s)",
                "DOCTYPE is disallowed when the feature \"http://apache.org/xml/features/disallow-doctype-decl\" set to true.", "2", "10");
        }
    }

    @Test
    public void create_xml_quadratic_blowup() {
        final String whoisResources =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE quadratic [\n" +
                "<!ENTITY a \"" + Strings.repeat("a", 1_000_000) + "a" + "\">\n" +
                "]>\n" +
                "<whois-resources xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n" +
                "    <objects>\n" +
                "        <object type=\"person\">\n" +
                "            <source id=\"TEST\">" + Strings.repeat("&a;", 1_000) + "</source>\n" +
                "            <attributes>\n" +
                "                <attribute name=\"person\" value=\"New Person\"/>\n" +
                "                <attribute name=\"address\" value=\"Amsterdam\"/>\n" +
                "                <attribute name=\"phone\" value=\"+31-1234567890\"/>\n" +
                "                <attribute name=\"mnt-by\" value=\"OWNER-MNT\"/>\n" +
                "                <attribute name=\"nic-hdl\" value=\"AUTO-1\"/>\n" +
                "                <attribute name=\"source\" value=\"TEST\"/>\n" +
                "            </attributes>\n" +
                "        </object>\n" +
                "    </objects>\n" +
                "</whois-resources>\n";

        try {
            RestTest.target(getPort(), "whois/test/person?password=test")
                .request()
                .post(Entity.entity(whoisResources, MediaType.APPLICATION_XML_TYPE), WhoisResources.class);
            fail();
        } catch (BadRequestException e) {
            final WhoisResources response = e.getResponse().readEntity(WhoisResources.class);
            RestTest.assertErrorCount(response, 1);
            RestTest.assertErrorMessage(response, 0, "Error",
                "XML processing exception: %s (line: %s, column: %s)",
                "DOCTYPE is disallowed when the feature \"http://apache.org/xml/features/disallow-doctype-decl\" set to true.", "2", "10");
        }
    }

    @Test
    public void create_xml_external_entity() {
        final String whoisResources =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE external [\n" +
                "<!ENTITY externalEntity SYSTEM \"/etc/passwd\">\n" +
                "]>" +
                "<whois-resources xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n" +
                "    <objects>\n" +
                "        <object type=\"person\">\n" +
                "            <source id=\"TEST\"/>\n" +
                "            <attributes>\n" +
                "                <attribute name=\"person\" value=\"New Person\"/>\n" +
                "                <attribute name=\"address\" value=\"&externalEntity;\"/>\n" +
                "                <attribute name=\"phone\" value=\"+31-1234567890\"/>\n" +
                "                <attribute name=\"mnt-by\" value=\"OWNER-MNT\"/>\n" +
                "                <attribute name=\"nic-hdl\" value=\"AUTO-1\"/>\n" +
                "                <attribute name=\"source\" value=\"TEST\"/>\n" +
                "            </attributes>\n" +
                "        </object>\n" +
                "    </objects>\n" +
                "</whois-resources>\n";
        try {
            RestTest.target(getPort(), "whois/test/person?password=test")
                .request()
                .post(Entity.entity(whoisResources, MediaType.APPLICATION_XML_TYPE), WhoisResources.class);
            fail();
        } catch (BadRequestException e) {
            final WhoisResources response = e.getResponse().readEntity(WhoisResources.class);
            RestTest.assertErrorCount(response, 1);
            RestTest.assertErrorMessage(response, 0, "Error",
                "XML processing exception: %s (line: %s, column: %s)",
                "DOCTYPE is disallowed when the feature \"http://apache.org/xml/features/disallow-doctype-decl\" set to true.", "2", "10");
        }
    }

    @Test
    public void create_succeeds_non_latin1_characters_substituted_in_response() {
        final String response = RestTest.target(getPort(), "whois/test/person?password=test")
            .request()
            .post(Entity.entity(
                "<whois-resources>\n" +
                "    <objects>\n" +
                "        <object type=\"person\">\n" +
                "            <source id=\"TEST\"/>\n" +
                "            <attributes>\n" +
                "                <attribute name=\"person\" value=\"New Person\"/>\n" +
                "                <attribute name=\"remarks\" value=\"ελληνικά\"/>\n" +      // attribute value is not latin-1
                "                <attribute name=\"address\" value=\"Amsterdam\"/>\n" +
                "                <attribute name=\"phone\" value=\"+31-1234567890\"/>\n" +
                "                <attribute name=\"mnt-by\" value=\"OWNER-MNT\"/>\n" +
                "                <attribute name=\"nic-hdl\" value=\"AUTO-1\"/>\n" +
                "                <attribute name=\"source\" value=\"TEST\"/>\n" +
                "            </attributes>\n" +
                "        </object>\n" +
                "    </objects>\n" +
                "</whois-resources>", MediaType.APPLICATION_XML), String.class);

        assertThat(response, containsString("<attribute name=\"remarks\" value=\"????????\"/>"));
        assertThat(response, containsString("<errormessage severity=\"Warning\" text=\"Value changed due to conversion into the ISO-8859-1 (Latin-1) character set\"/>"));
    }

    @Test
    public void create_succeeds_control_characters_are_substituted() {
        final String response = RestTest.target(getPort(), "whois/test/person?password=test")
            .request()
            .post(Entity.entity(
                "<whois-resources>\n" +
                "    <objects>\n" +
                "        <object type=\"person\">\n" +
                "            <source id=\"TEST\"/>\n" +
                "            <attributes>\n" +
                "                <attribute name=\"person\" value=\"New Person\"/>\n" +
                "                <attribute name=\"address\" value=\"Test\u007F\u008f Address\"/>\n" +      // attribute value contains control characters
                "                <attribute name=\"phone\" value=\"+31-1234567890\"/>\n" +
                "                <attribute name=\"mnt-by\" value=\"OWNER-MNT\"/>\n" +
                "                <attribute name=\"nic-hdl\" value=\"AUTO-1\"/>\n" +
                "                <attribute name=\"source\" value=\"TEST\"/>\n" +
                "            </attributes>\n" +
                "        </object>\n" +
                "    </objects>\n" +
                "</whois-resources>", MediaType.APPLICATION_XML), String.class);

        assertThat(response, containsString("<attribute name=\"address\" value=\"Test?? Address\"/>"));
        assertThat(response, containsString("<errormessage severity=\"Warning\" text=\"Invalid character(s) were substituted in attribute &quot;%s&quot; value\">"));
        assertThat(response, containsString("<attribute name=\"address\" value=\"        Test?? Address\"/>")); // TODO: [ES] attribute value not trimmed
        assertThat(response, containsString("<args value=\"address\"/>"));
        assertThat(response, containsString("</errormessage>"));
    }

    @Test
    public void create_succeeds_extended_ascii_latin1_characters_are_preserved() {
        final String response = RestTest.target(getPort(), "whois/test/person?password=test")
            .request()
            .post(Entity.entity(
                "<whois-resources>\n" +
                "    <objects>\n" +
                "        <object type=\"person\">\n" +
                "            <source id=\"TEST\"/>\n" +
                "            <attributes>\n" +
                "                <attribute name=\"person\" value=\"New Person\"/>\n" +
                "                <attribute name=\"remarks\" value=\"ÖÜëñ\"/>\n" +      // extended ASCII latin-1 characters
                "                <attribute name=\"address\" value=\"Amsterdam\"/>\n" +
                "                <attribute name=\"phone\" value=\"+31-1234567890\"/>\n" +
                "                <attribute name=\"mnt-by\" value=\"OWNER-MNT\"/>\n" +
                "                <attribute name=\"nic-hdl\" value=\"AUTO-1\"/>\n" +
                "                <attribute name=\"source\" value=\"TEST\"/>\n" +
                "            </attributes>\n" +
                "        </object>\n" +
                "    </objects>\n" +
                "</whois-resources>", MediaType.APPLICATION_XML), String.class);

        assertThat(response, containsString("<attribute name=\"remarks\" value=\"ÖÜëñ\"/>"));
    }

    @Test
    public void create_succeeds_idn_email_address_converted_to_punycode() {
        final String response = RestTest.target(getPort(), "whois/test/person?password=test")
            .request()
            .post(Entity.entity(
                "<whois-resources>\n" +
                "    <objects>\n" +
                "        <object type=\"person\">\n" +
                "            <source id=\"TEST\"/>\n" +
                "            <attributes>\n" +
                "                <attribute name=\"person\" value=\"New Person\"/>\n" +
                "                <attribute name=\"address\" value=\"Moscow\"/>\n" +
                "                <attribute name=\"e-mail\" value=\"example@москва.ru\"/>\n" +
                "                <attribute name=\"phone\" value=\"+31-1234567890\"/>\n" +
                "                <attribute name=\"mnt-by\" value=\"OWNER-MNT\"/>\n" +
                "                <attribute name=\"nic-hdl\" value=\"AUTO-1\"/>\n" +
                "                <attribute name=\"source\" value=\"TEST\"/>\n" +
                "            </attributes>\n" +
                "        </object>\n" +
                "    </objects>\n" +
                "</whois-resources>", MediaType.APPLICATION_XML), String.class);

        assertThat(response, containsString("<attribute name=\"e-mail\" value=\"example@xn--80adxhks.ru\"/>"));
    }

    @Test
    public void create_succeeds_non_break_space_substituted_with_regular_space() {
        final WhoisResources response = RestTest.target(getPort(), "whois/test/person?password=test")
            .request()
            .post(Entity.entity(
                "<whois-resources>\n" +
                "    <objects>\n" +
                "        <object type=\"person\">\n" +
                "            <source id=\"TEST\"/>\n" +
                "            <attributes>\n" +
                "                <attribute name=\"person\" value=\"New\u00a0Person\"/>\n" +    // non-break space
                "                <attribute name=\"remarks\" value=\"Test\"/>\n" +
                "                <attribute name=\"address\" value=\"Amsterdam\"/>\n" +
                "                <attribute name=\"phone\" value=\"+31-1234567890\"/>\n" +
                "                <attribute name=\"mnt-by\" value=\"OWNER-MNT\"/>\n" +
                "                <attribute name=\"nic-hdl\" value=\"AUTO-1\"/>\n" +
                "                <attribute name=\"source\" value=\"TEST\"/>\n" +
                "            </attributes>\n" +
                "        </object>\n" +
                "    </objects>\n" +
                "</whois-resources>", MediaType.APPLICATION_XML), WhoisResources.class);

        assertThat(response.getErrorMessages(), hasSize(2));
        assertThat(response.getErrorMessages().getFirst().getText(), is("MD5 hashed password authentication is deprecated and support will be " +
                "removed at the end of 2025. Please switch to an alternative authentication method before then."));

        assertThat(response.getErrorMessages().get(1).getText(), is("Invalid character(s) were substituted in attribute \"%s\" value"));
        assertThat(response.getErrorMessages().get(1).getArgs(), hasSize(1));
        assertThat(response.getErrorMessages().get(1).getArgs().getFirst().getValue(), is("person"));
        assertThat(response.getWhoisObjects(), hasSize(1));
        assertThat(response.getWhoisObjects().getFirst().getAttributes().getFirst().getValue(), is("New Person"));
    }

    @Test
    public void modify_succeeds_non_break_space_substituted_with_regular_space_noop() {
        databaseHelper.addObject(
            "person:    New Person\n" +
            "remarks:   Test\n" +
            "address:   Amsterdam\n" +
            "phone:     +31-1234567890\n" +
            "mnt-by:    OWNER-MNT\n" +
            "nic-hdl:   NP1-TEST\n" +
            "source:    TEST");

        final WhoisResources response = RestTest.target(getPort(), "whois/test/person/NP1-TEST?password=test")
            .request()
            .put(Entity.entity(
                "<whois-resources>\n" +
                "    <objects>\n" +
                "        <object type=\"person\">\n" +
                "            <source id=\"TEST\"/>\n" +
                "            <attributes>\n" +
                "                <attribute name=\"person\" value=\"New\u00a0Person\"/>\n" +    // non-break space
                "                <attribute name=\"remarks\" value=\"Test\"/>\n" +
                "                <attribute name=\"address\" value=\"Amsterdam\"/>\n" +
                "                <attribute name=\"phone\" value=\"+31-1234567890\"/>\n" +
                "                <attribute name=\"mnt-by\" value=\"OWNER-MNT\"/>\n" +
                "                <attribute name=\"nic-hdl\" value=\"NP1-TEST\"/>\n" +
                "                <attribute name=\"source\" value=\"TEST\"/>\n" +
                "            </attributes>\n" +
                "        </object>\n" +
                "    </objects>\n" +
                "</whois-resources>", MediaType.APPLICATION_XML), WhoisResources.class);

        RestTest.assertWarningCount(response, 3);

        RestTest.assertErrorMessage(response, 0, "Warning", "MD5 hashed password authentication is deprecated and support will be " +
                "removed at the end of 2025. Please switch to an alternative authentication method before then.");
        RestTest.assertErrorMessage(response, 1, "Warning", "Submitted object identical to database object");
        RestTest.assertErrorMessage(response, 2, "Warning", "Invalid character(s) were substituted in attribute " +
                "\"%s\" value", "person");
    }

    @Test
    public void create_concurrent() throws Exception {
        final int numThreads = 10;
        final AtomicInteger exceptions = new AtomicInteger();

        final ExecutorService requestsWithInvalidSource = Executors.newFixedThreadPool(numThreads);
        for (int thread = 0; thread < numThreads; thread++) {
            requestsWithInvalidSource.submit(new Runnable() {
                @Override public void run() {
                    final RpslObject person = RpslObject.parse(
                            "person:    Pauleth Palthen\n" +
                            "address:   Singel 258\n" +
                            "phone:     +31-1234567890\n" +
                            "e-mail:    noreply@ripe.net\n" +
                            "mnt-by:    OWNER-MNT\n" +
                            "nic-hdl:   AUTO-1\n" +
                            "remarks:   remark\n" +
                            "source:    INVALID\n");

                    try {
                        RestTest.target(getPort(), "whois/INVALID/person?password=test")
                                .request()
                                .post(Entity.entity(map(person), MediaType.APPLICATION_XML), WhoisResources.class);
                        fail();
                    } catch (BadRequestException e) {
                        // expected
                        exceptions.incrementAndGet();
                    }
                }
            });
        }

        requestsWithInvalidSource.shutdown();
        requestsWithInvalidSource.awaitTermination(10, TimeUnit.SECONDS);
        assertThat(exceptions.getAndSet(0), is(numThreads));

        final ExecutorService createRequests = Executors.newFixedThreadPool(numThreads);
        for (int thread = 0; thread < numThreads; thread++) {
            createRequests.submit(new Runnable() {
                @Override public void run() {
                    final RpslObject person = RpslObject.parse(
                            "person:    Pauleth Palthen\n" +
                            "address:   Singel 258\n" +
                            "phone:     +31-1234567890\n" +
                            "e-mail:    noreply@ripe.net\n" +
                            "mnt-by:    OWNER-MNT\n" +
                            "nic-hdl:   AUTO-1\n" +
                            "remarks:   remark\n" +
                            "source:    TEST\n");

                    try {
                        RestTest.target(getPort(), "whois/test/person?password=test")
                                .request()
                                .post(Entity.entity(map(person), MediaType.APPLICATION_XML), WhoisResources.class);
                        fail();
                    } catch (Exception e) {
                        // unexpected
                        exceptions.incrementAndGet();
                    }
                }
            });
        }

        createRequests.shutdown();
        createRequests.awaitTermination(10, TimeUnit.SECONDS);
        assertThat(exceptions.get(), is(0));
    }


    @Test
    public void create_password_attribute_in_body() {
        try {
            RestTest.target(getPort(), "whois/test/person")
                    .request()
                    .post(Entity.entity("<whois-resources>\n" +
                            "    <objects>\n" +
                            "        <object type=\"person\">\n" +
                            "            <source id=\"RIPE\"/>\n" +
                            "            <attributes>\n" +
                            "                <attribute name=\"person\" value=\"Pauleth Palthen\"/>\n" +
                            "                <attribute name=\"address\" value=\"Singel 258\"/>\n" +
                            "                <attribute name=\"phone\" value=\"+31-1234567890\"/>\n" +
                            "                <attribute name=\"e-mail\" value=\"noreply@ripe.net\"/>\n" +
                            "                <attribute name=\"mnt-by\" value=\"OWNER-MNT\"/>\n" +
                            "                <attribute name=\"nic-hdl\" value=\"PP1-TEST\"/>\n" +
                            "                <attribute name=\"source\" value=\"TEST\"/>\n" +
                            "                <attribute name=\"password\" value=\"test\"/>\n" +
                            "            </attributes>\n" +
                            "        </object>\n" +
                            "    </objects>\n" +
                            "</whois-resources>", MediaType.APPLICATION_XML), String.class);
            fail();
        } catch (BadRequestException e) {
            final WhoisResources whoisResources = e.getResponse().readEntity(WhoisResources.class);
            assertThat(whoisResources.getErrorMessages(), hasSize(1));
            assertThat(whoisResources.getErrorMessages().get(0).toString(), is("\"password\" is not a known RPSL attribute"));
        }
    }

    @Test
    public void create_person_invalid_source_in_request_body() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "person:  Pauleth Palthen\n" +
                "address: Singel 258\n" +
                "phone:   +31-1234567890\n" +
                "e-mail:  noreply@ripe.net\n" +
                "mnt-by:  OWNER-MNT\n" +
                "nic-hdl: PP1-TEST\n" +
                "remarks: remark\n" +
                "source:  NONE\n");
        try {
            RestTest.target(getPort(), "whois/test/person?password=test")
                    .request()
                    .post(Entity.entity(map(rpslObject), MediaType.APPLICATION_XML), String.class);
            fail("expected request to fail");
        } catch (BadRequestException e) {
            RestTest.assertOnlyErrorMessage(e, "Error", "Unrecognized source: %s", "NONE");
        }
    }

    @Test
    public void create_inetnum_multiple_errors() {
        final RpslObject rpslObject = RpslObject.parse(
                "inetnum:   10.0.0.0 - 10.255.255.255\n" +
                "netname:   TEST-NET\n" +
                "descr:     description\n" +
                "country:       NONE\n" +
                "admin-c:       INVALID-1\n" +
                "tech-c:        INVALID-2\n" +
                "status:    ASSIGNED PI\n" +
                "mnt-by:    OWNER-MNT\n" +
                "source:    TEST\n");

        try {
            RestTest.target(getPort(), "whois/test/inetnum?password=test")
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(map(rpslObject), MediaType.APPLICATION_JSON), String.class);
            fail();
        } catch (BadRequestException e) {
            final WhoisResources whoisResources = RestTest.mapClientException(e);
            RestTest.assertErrorCount(whoisResources, 3);
            RestTest.assertErrorMessage(whoisResources, 0, "Error", "Syntax error in %s", "NONE");
            RestTest.assertErrorMessage(whoisResources, 1, "Error", "Syntax error in %s", "INVALID-1");
            RestTest.assertErrorMessage(whoisResources, 2, "Error", "Syntax error in %s", "INVALID-2");
        }
    }

    @Test
    public void create_invalid_json_format_no_attributes() {
        try {
            RestTest.target(getPort(), "whois/test/person.json?password=test")
                    .request()
                    .post(Entity.entity("{\n" +
                            "  \"objects\" : {\n" +
                            "    \"object\" : [ {\n" +
                            "      \"type\" : \"inetnum\",\n" +
                            "      \"source\" : {\n" +
                            "        \"id\" : \"test\"\n" +
                            "      },\n" +
                            "      \"primary-key\" : {\n" +
                            "        \"attribute\" : [ {\n" +
                            "          \"name\" : \"inetnum\",\n" +
                            "          \"value\" : \"10.0.0.0 - 10.255.255.255\"\n" +
                            "        } ]\n" +
                            "      }\n" +
                            "    } ]\n" +
                            "  }\n" +
                            "}", MediaType.APPLICATION_JSON), String.class);
            fail();
        } catch (BadRequestException e) {
            assertThat(e.getResponse().readEntity(String.class), is("" +
                    "{\n" +
                    "  \"errormessages\" : {\n" +
                    "    \"errormessage\" : [ {\n" +
                    "      \"severity\" : \"Error\",\n" +
                    "      \"text\" : \"The validated collection is empty\"\n" +
                    "    } ]\n" +
                    "  },\n" +
                    "  \"terms-and-conditions\" : {\n" +
                    "    \"type\" : \"locator\",\n" +
                    "    \"href\" : \"https://docs.db.ripe.net/terms-conditions.html\"\n" +
                    "  }\n" +
                    "}"));
        }
    }

    @Test
    public void create_invalid_json_format_empty_string() {
        try {
            RestTest.target(getPort(), "whois/test/person.json?password=test")
                    .request()
                    .post(Entity.entity("{\n" +
                            "  \"objects\" : {\n" +
                            "    \"object\" : [ {\n" +
                            "      \"type\" : \"inetnum\",\n" +
                            "      \"source\" : {\n" +
                            "        \"id\" : \"test\"\n" +
                            "      },\n" +
                            "      \"primary-key\" : {\n" +
                            "        \"attribute\" : [ {\n" +
                            "          \"\"" +
                            "        } ]\n" +
                            "      }\n" +
                            "    } ]\n" +
                            "  }\n" +
                            "}", MediaType.APPLICATION_JSON), String.class);
            fail();
        } catch (BadRequestException e) {
            final WhoisResources whoisResources = e.getResponse().readEntity(WhoisResources.class);
            assertThat(whoisResources.getErrorMessages(), hasSize(1));
            assertThat(whoisResources.getErrorMessages().get(0).toString(),
                startsWith("JSON processing exception: Unexpected character ('}' (code 125)): was expecting a colon to separate field name and value"));
        }
    }

    @Test
    public void create_invalid_reference() {
        try {
            RestTest.target(getPort(), "whois/test/person?password=test")
                    .request()
                    .post(Entity.entity("<whois-resources>\n" +
                            "    <objects>\n" +
                            "        <object type=\"person\">\n" +
                            "            <source id=\"RIPE\"/>\n" +
                            "            <attributes>\n" +
                            "                <attribute name=\"person\" value=\"Pauleth Palthen\"/>\n" +
                            "                <attribute name=\"address\" value=\"Singel 258\"/>\n" +
                            "                <attribute name=\"phone\" value=\"+31-1234567890\"/>\n" +
                            "                <attribute name=\"e-mail\" value=\"noreply@ripe.net\"/>\n" +
                            "                <attribute name=\"admin-c\" value=\"INVALID\"/>\n" +
                            "                <attribute name=\"mnt-by\" value=\"OWNER-MNT\"/>\n" +
                            "                <attribute name=\"nic-hdl\" value=\"PP1-TEST\"/>\n" +
                            "                <attribute name=\"source\" value=\"RIPE\"/>\n" +
                            "            </attributes>\n" +
                            "        </object>\n" +
                            "    </objects>\n" +
                            "</whois-resources>", MediaType.APPLICATION_XML), String.class);
            fail();
        } catch (BadRequestException e) {
            final WhoisResources whoisResources = RestTest.mapClientException(e);
            RestTest.assertErrorCount(whoisResources, 1);
            RestTest.assertErrorMessage(whoisResources, 0, "Error", "\"%s\" is not valid for this object type", "admin-c");
        }
    }

    @Test
    public void create_bad_input_empty_objects_element() {
        assertThrows(BadRequestException.class, () -> {
            RestTest.target(getPort(), "whois/test/person?password=test")
                    .request()
                    .post(Entity.entity("<whois-resources>\n<objects/>\n</whois-resources>", MediaType.APPLICATION_XML), String.class);

        });
    }

    @Test
    public void create_bad_input_no_objects_element() {
        assertThrows(BadRequestException.class, () -> {
            RestTest.target(getPort(), "whois/test/person?password=test")
                    .request()
                    .post(Entity.entity("<whois-resources/>", MediaType.APPLICATION_XML), String.class);

        });
    }

    @Test
    public void create_bad_input_empty_body() {
        assertThrows(BadRequestException.class, () -> {
            RestTest.target(getPort(), "whois/test/person?password=test")
                    .request()
                    .post(Entity.entity("", MediaType.APPLICATION_XML), String.class);
        });
    }

    @Test
    public void create_bad_input_null_body() {
        assertThrows(BadRequestException.class, () -> {
            RestTest.target(getPort(), "whois/test/mntner")
                    .request()
                    .post( null, WhoisResources.class);
        });
    }

    @Test
    public void create_bad_input_no_closing_element() {
        try {
             RestTest.target(getPort(), "whois/test/domain?password=test")
                .request()
                .post(Entity.entity(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n" +
                    "<whois-resources>\n", MediaType.APPLICATION_XML), String.class);       // no closing element
            fail();
        } catch (BadRequestException e) {
            final WhoisResources whoisResources = e.getResponse().readEntity(WhoisResources.class);
            assertThat(whoisResources.getErrorMessages(), hasSize(1));
            assertThat(whoisResources.getErrorMessages().get(0).toString(),
                is("XML processing exception: XML document structures must start and end within the same entity. (line: 3, column: 1)"));
        }
    }

    @Test
    public void create_invalid_xml_missing_space() {
        try {
             RestTest.target(getPort(), "whois/test/domain?password=test")
                .request()
                .post(Entity.entity(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n" +
                    "<whois-resources>\n" +
                    "<objects>\n" +
                    "<object type=\"domain\">\n" +
                    "<source id=\"ripe\"/>\n" +
                    "<attributes>\n" +
                    "<attribute name=\"descr\"value=\"description\"/>\n" +      // no space between name and value
                    "</attributes>\n" +
                    "</object>\n" +
                    "</objects>\n" +
                    "</whois-resources>", MediaType.APPLICATION_XML), String.class);
            fail();
        } catch (BadRequestException e) {
            final WhoisResources whoisResources = e.getResponse().readEntity(WhoisResources.class);
            assertThat(whoisResources.getErrorMessages(), hasSize(1));
            assertThat(whoisResources.getErrorMessages().get(0).toString(),
                is("XML processing exception: Element type \"attribute\" must be followed by either attribute specifications, \">\" or \"/>\". (line: 7, column: 24)"));
        }
    }

    @Test
    public void create_invalid_json_missing_closing_brace() {
        try {
             RestTest.target(getPort(), "whois/test/domain?password=test")
                .request()
                .post(Entity.entity(
                    "{", MediaType.APPLICATION_JSON), String.class);
            fail();
        } catch (BadRequestException e) {
            final WhoisResources whoisResources = e.getResponse().readEntity(WhoisResources.class);
            assertThat(whoisResources.getErrorMessages(), hasSize(1));
            assertThat(whoisResources.getErrorMessages().get(0).toString(),
                is("JSON processing exception: Unexpected end-of-input: expected close marker for Object (line: 1, column: 2)"));
        }
    }

    @Test
    public void create_invalid_json_missing_closing_array_bracket() {
        try {
            RestTest.target(getPort(), "whois/test/person.json?password=test")
                    .request()
                    .post(Entity.entity("{\n" +
                            "  \"objects\" : {\n" +
                            "    \"object\" : [ {\n" +
                            "      \"type\" : \"inetnum\",\n" +
                            "      \"source\" : {\n" +
                            "        \"id\" : \"test\"\n" +
                            "      },\n" +
                            "      \"primary-key\" : {\n" +
                            "        \"attribute\" : [\n" +        // missing closing array bracket
                            "      }\n" +
                            "    } ]\n" +
                            "  }\n" +
                            "}", MediaType.APPLICATION_JSON), String.class);
            fail();
        } catch (BadRequestException e) {
            final WhoisResources whoisResources = e.getResponse().readEntity(WhoisResources.class);
            assertThat(whoisResources.getErrorMessages(), hasSize(1));
            assertThat(whoisResources.getErrorMessages().get(0).toString(),
                startsWith("JSON processing exception: Unexpected close marker '}': expected ']'"));
        }
    }

    @Test
    public void create_multiple_passwords() {
        WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person?password=invalid&password=test")
                .request()
                .post(Entity.entity(map(PAULETH_PALTHEN), MediaType.APPLICATION_XML), WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), hasSize(1));
        assertThat(whoisResources.getErrorMessages().getFirst().getText(), is("MD5 hashed password authentication is deprecated and support will be " +
                "removed at the end of 2025. Please switch to an alternative authentication method before then."));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
    }

    @Test
    public void create_invalid_password() {
        try {
            RestTest.target(getPort(), "whois/test/person?password=invalid")
                    .request()
                    .post(Entity.entity(map(PAULETH_PALTHEN), MediaType.APPLICATION_XML), String.class);
            fail();
        } catch (NotAuthorizedException e) {
            RestTest.assertOnlyErrorMessage(e, "Error", "Authorisation for [%s] %s failed\nusing \"%s:\"\nnot authenticated by: %s", "person", "PP1-TEST", "mnt-by", "OWNER-MNT");
        }
    }

    @Test
    public void create_no_password() {
        try {
            RestTest.target(getPort(), "whois/test/person")
                    .request(MediaType.APPLICATION_XML)
                    .post(Entity.entity(map(PAULETH_PALTHEN), MediaType.APPLICATION_XML), String.class);
            fail();
        } catch (NotAuthorizedException e) {
            RestTest.assertOnlyErrorMessage(e, "Error", "Authorisation for [%s] %s failed\nusing \"%s:\"\nnot authenticated by: %s", "person", "PP1-TEST", "mnt-by", "OWNER-MNT");
        }
    }

    @Test
    public void create_mntner_encoded_password() {
        final RpslObject rpslObject = RpslObject.parse(
                "mntner:         TEST-MNT\n" +
                "descr:          Test Organisation\n" +
                "admin-c:        TP1-TEST\n" +
                "upd-to:         noreply@ripe.net\n" +
                "auth:           MD5-PW $1$GVXqt/5m$TaeN0iPr84mNoz8j3IDs//  # auth?auth \n" +
                "mnt-by:         TEST-MNT\n" +
                "source:         TEST"
        );

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/mntner?password=auth%3Fauth")
                .request(MediaType.APPLICATION_XML_TYPE)
                .post(Entity.entity(map(rpslObject), MediaType.APPLICATION_JSON), WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), hasSize(1));
        assertThat(whoisResources.getErrorMessages().getFirst().getText(), is("MD5 hashed password authentication is deprecated and support will be " +
                "removed at the end of 2025. Please switch to an alternative authentication method before then."));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        assertThat(whoisResources.getLink().getHref(), is(String.format("http://localhost:%d/test/mntner", getPort())));
    }

    @Test
    public void create_already_exists() {
        databaseHelper.addObject(PAULETH_PALTHEN);
        try {
            RestTest.target(getPort(), "whois/test/person?password=test")
                    .request()
                    .post(Entity.entity(map(PAULETH_PALTHEN), MediaType.APPLICATION_XML), String.class);
            fail();
        } catch (ClientErrorException e1) {
            assertThat(e1.getResponse().getStatus(), is(Response.Status.CONFLICT.getStatusCode()));
            RestTest.assertOnlyErrorMessage(e1, "Error", "Enforced new keyword specified, but the object already exists in the database");
        }
    }

    @Test
    public void create_validate_objecttype() {
        try {
            RestTest.target(getPort(), "whois/test/person?password=test")
                    .request()
                    .post(Entity.entity(map(OWNER_MNT), MediaType.APPLICATION_XML), String.class);
            fail();
        } catch (ClientErrorException e1) {
            assertThat(e1.getResponse().getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
            RestTest.assertOnlyErrorMessage(e1, "Error", "Object type specified in URI (%s) does not match the WhoisResources contents", "person");
        }
    }

    @Test
    public void get_method_without_primary_key_not_allowed() {
        assertThrows(NotAllowedException.class, () -> {
            RestTest.target(getPort(), "whois/ripe/route6")
                    .request()
                    .get(String.class);
        });
    }

    @Test
    public void delete_method_without_primary_key_not_allowed() {
        assertThrows(NotAllowedException.class, () -> {
            RestTest.target(getPort(), "whois/test/person")
                    .request()
                    .delete(String.class);
        });
    }

    @Test
    public void create_get_resource_not_found() {
        try {
            RestTest.target(getPort(), "whois/test")
                    .request(MediaType.APPLICATION_XML)
                    .get(WhoisResources.class);
            fail();
        } catch (NotFoundException e) {
            // expected
        }
    }

    @Test
    public void create_person_xml_text() {
        final String request = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n" +
                "<whois-resources>\n" +
                "  <objects>\n" +
                "    <object type=\"person\">\n" +
                "      <source id=\"test\"/>\n" +
                "      <attributes>\n" +
                "        <attribute name=\"person\" value=\"Pauleth Palthen\"/>\n" +
                "        <attribute name=\"address\" value=\"Singel 258\"/>\n" +
                "        <attribute name=\"phone\" value=\"+31-1234567890\"/>\n" +
                "        <attribute name=\"e-mail\" value=\"noreply@ripe.net\"/>\n" +
                "        <attribute name=\"mnt-by\" value=\"OWNER-MNT\"/>\n" +
                "        <attribute name=\"nic-hdl\" value=\"PP1-TEST\"/>\n" +
                "        <attribute name=\"remarks\" value=\"remark\"/>\n" +
                "        <attribute name=\"source\" value=\"TEST\"/>\n" +
                "      </attributes>\n" +
                "    </object>\n" +
                "  </objects>\n" +
                "</whois-resources>\n";

        final String response = RestTest.target(getPort(), "whois/test/person?password=test")
                .request(MediaType.APPLICATION_XML_TYPE)
                .post(Entity.entity(request, MediaType.APPLICATION_XML), String.class);

        assertThat(response, is(String.format(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<whois-resources xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n" +
                "    <link xlink:type=\"locator\" xlink:href=\"http://localhost:%d/test/person\"/>\n" +
                "    <objects>\n" +
                "        <object type=\"person\">\n" +
                "            <link xlink:type=\"locator\" xlink:href=\"http://rest-test.db.ripe.net/test/person/PP1-TEST\"/>\n" +
                "            <source id=\"test\"/>\n" +
                "            <primary-key>\n" +
                "                <attribute name=\"nic-hdl\" value=\"PP1-TEST\"/>\n" +
                "            </primary-key>\n" +
                "            <attributes>\n" +
                "                <attribute name=\"person\" value=\"Pauleth Palthen\"/>\n" +
                "                <attribute name=\"address\" value=\"Singel 258\"/>\n" +
                "                <attribute name=\"phone\" value=\"+31-1234567890\"/>\n" +
                "                <attribute name=\"e-mail\" value=\"noreply@ripe.net\"/>\n" +
                "                <attribute name=\"mnt-by\" value=\"OWNER-MNT\" referenced-type=\"mntner\">\n" +
                "                    <link xlink:type=\"locator\" xlink:href=\"http://rest-test.db.ripe.net/test/mntner/OWNER-MNT\"/>\n" +
                "                </attribute>\n" +
                "                <attribute name=\"nic-hdl\" value=\"PP1-TEST\"/>\n" +
                "                <attribute name=\"remarks\" value=\"remark\"/>\n" +
                "                <attribute name=\"created\" value=\"2001-02-04T17:00:00Z\"/>\n" +
                "                <attribute name=\"last-modified\" value=\"2001-02-04T17:00:00Z\"/>\n" +
                "                <attribute name=\"source\" value=\"TEST\"/>\n" +
                "            </attributes>\n" +
                "        </object>\n" +
                "    </objects>\n" +
                "    <errormessages>\n" +
                "        <errormessage severity=\"Warning\" text=\"MD5 hashed password authentication is deprecated and support will be removed at the end of 2025. Please switch to an alternative authentication method before then.\"/>\n" +
                "    </errormessages>\n" +
                "    <terms-and-conditions xlink:type=\"locator\" xlink:href=\"https://docs.db.ripe.net/terms-conditions.html\"/>\n" +
                "</whois-resources>", getPort())));
    }

    @Test
    public void create_person_json_text() {
        final String request = "{\"objects\":{\n" +
                "  \"object\":[ {\n" +
                "  \"type\" : \"person\",\n" +
                "  \"source\" : {\n" +
                "    \"id\" : \"test\"\n" +
                "  },\n" +
                "  \"attributes\" : {\n" +
                "    \"attribute\" : [ {\n" +
                "      \"name\" : \"person\",\n" +
                "      \"value\" : \"Pauleth Palthen\"\n" +
                "    }, {\n" +
                "      \"name\" : \"address\",\n" +
                "      \"value\" : \"Singel 258\"\n" +
                "    }, {\n" +
                "      \"name\" : \"phone\",\n" +
                "      \"value\" : \"+31-1234567890\"\n" +
                "    }, {\n" +
                "      \"name\" : \"e-mail\",\n" +
                "      \"value\" : \"noreply@ripe.net\"\n" +
                "    }, {\n" +
                "      \"name\" : \"mnt-by\",\n" +
                "      \"value\" : \"OWNER-MNT\"\n" +
                "    }, {\n" +
                "      \"name\" : \"nic-hdl\",\n" +
                "      \"value\" : \"PP1-TEST\"\n" +
                "    }, {\n" +
                "      \"name\" : \"remarks\",\n" +
                "      \"value\" : \"remark\"\n" +
                "    }, {\n" +
                "      \"name\" : \"source\",\n" +
                "      \"value\" : \"TEST\"\n" +
                "    } ]\n" +
                "  }\n" +
                "} ]\n" +
                "}\n" +
                "}";

        final String response = RestTest.target(getPort(), "whois/test/person?password=test")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(request, MediaType.APPLICATION_JSON), String.class);

        assertThat(response, is(String.format("" +
                "{\n" +
                "  \"link\" : {\n" +
                "    \"type\" : \"locator\",\n" +
                "    \"href\" : \"http://localhost:%d/test/person\"\n" +
                "  },\n" +
                "  \"objects\" : {\n" +
                "    \"object\" : [ {\n" +
                "      \"type\" : \"person\",\n" +
                "      \"link\" : {\n" +
                "        \"type\" : \"locator\",\n" +
                "        \"href\" : \"http://rest-test.db.ripe.net/test/person/PP1-TEST\"\n" +
                "      },\n" +
                "      \"source\" : {\n" +
                "        \"id\" : \"test\"\n" +
                "      },\n" +
                "      \"primary-key\" : {\n" +
                "        \"attribute\" : [ {\n" +
                "          \"name\" : \"nic-hdl\",\n" +
                "          \"value\" : \"PP1-TEST\"\n" +
                "        } ]\n" +
                "      },\n" +
                "      \"attributes\" : {\n" +
                "        \"attribute\" : [ {\n" +
                "          \"name\" : \"person\",\n" +
                "          \"value\" : \"Pauleth Palthen\"\n" +
                "        }, {\n" +
                "          \"name\" : \"address\",\n" +
                "          \"value\" : \"Singel 258\"\n" +
                "        }, {\n" +
                "          \"name\" : \"phone\",\n" +
                "          \"value\" : \"+31-1234567890\"\n" +
                "        }, {\n" +
                "          \"name\" : \"e-mail\",\n" +
                "          \"value\" : \"noreply@ripe.net\"\n" +
                "        }, {\n" +
                "          \"link\" : {\n" +
                "            \"type\" : \"locator\",\n" +
                "            \"href\" : \"http://rest-test.db.ripe.net/test/mntner/OWNER-MNT\"\n" +
                "          },\n" +
                "          \"name\" : \"mnt-by\",\n" +
                "          \"value\" : \"OWNER-MNT\",\n" +
                "          \"referenced-type\" : \"mntner\"\n" +
                "        }, {\n" +
                "          \"name\" : \"nic-hdl\",\n" +
                "          \"value\" : \"PP1-TEST\"\n" +
                "        }, {\n" +
                "          \"name\" : \"remarks\",\n" +
                "          \"value\" : \"remark\"\n" +
                "        }, {\n" +
                "          \"name\" : \"created\",\n" +
                "          \"value\" : \"2001-02-04T17:00:00Z\"\n" +
                "        }, {\n" +
                "          \"name\" : \"last-modified\",\n" +
                "          \"value\" : \"2001-02-04T17:00:00Z\"\n" +
                "        }, {\n" +
                "          \"name\" : \"source\",\n" +
                "          \"value\" : \"TEST\"\n" +
                "        } ]\n" +
                "      }\n" +
                "    } ]\n" +
                "  },\n" +
                "  \"errormessages\" : {\n" +
                "    \"errormessage\" : [ {\n" +
                "      \"severity\" : \"Warning\",\n" +
                "      \"text\" : \"MD5 hashed password authentication is deprecated and support will be removed at the end of 2025. Please switch to an alternative authentication method before then.\"\n" +
                "    } ]\n" +
                "  },\n" +
                "  \"terms-and-conditions\" : {\n" +
                "    \"type\" : \"locator\",\n" +
                "    \"href\" : \"https://docs.db.ripe.net/terms-conditions.html\"\n" +
                "  }\n" +
                "}", getPort())));
    }

    @Test
    public void create_non_latin1_characters_are_substituted() {
        final RpslObject person = RpslObject.parse("" +
                "person:    Pauleth Palthen\n" +
                "address:   Тверская улица,москва\n" +
                "phone:     +31-1234567890\n" +
                "e-mail:    noreply@ripe.net\n" +
                "mnt-by:    OWNER-MNT\n" +
                "nic-hdl:   PP1-TEST\n" +
                "remarks:   remark\n" +
                "source:    TEST\n");

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person?password=test")
                .request()
                .post(Entity.entity(map(person), MediaType.APPLICATION_XML), WhoisResources.class);

        final WhoisObject responseObject = whoisResources.getWhoisObjects().get(0);
        assertThat(responseObject.getAttributes().get(1).getValue(), is("???????? ?????,??????"));
    }

    @Test
    public void create_self_referencing_maintainer_password_auth_only() {
        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/mntner?password=test")
                .request()
                .post(Entity.entity(map(PASSWORD_ONLY_MNT), MediaType.APPLICATION_XML), WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), hasSize(1));
        assertThat(whoisResources.getErrorMessages().getFirst().getText(), is("MD5 hashed password authentication is deprecated and support will be " +
                "removed at the end of 2025. Please switch to an alternative authentication method before then."));
        final WhoisObject object = whoisResources.getWhoisObjects().getFirst();
        assertThat(object.getType(), is("mntner"));
        assertThat(object.getLink(), is(Link.create("http://rest-test.db.ripe.net/test/mntner/PASSWORD-ONLY-MNT")));
        assertThat(object.getPrimaryKey(), contains(new Attribute("mntner", "PASSWORD-ONLY-MNT")));
        assertThat(object.getAttributes(), hasItems(new Attribute("auth", "MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/", "test", null, null, null)));
    }

    @Test
    public void create_self_referencing_maintainer_sso_auth_only() {
        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/mntner")
                .request()
                .cookie("crowd.token_key", "valid-token")
                .post(Entity.entity(map(SSO_ONLY_MNT), MediaType.APPLICATION_XML), WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        assertThat(whoisResources.getWhoisObjects().get(0).getAttributes(), hasItems(new Attribute("auth", "SSO person@net.net")));

        assertThat(databaseHelper.lookupObject(ObjectType.MNTNER, "SSO-ONLY-MNT").findAttributes(AttributeType.AUTH),
                contains(new RpslAttribute(AttributeType.AUTH, "SSO 906635c2-0405-429a-800b-0602bd716124")));
    }

    @Test
    public void create_self_referencing_maintainer_sso_auth_only_invalid_username() {
        try {
            final RpslObject updatedObject = buildGenericObject(SSO_ONLY_MNT, "auth: SSO in@valid.net");

            RestTest.target(getPort(), "whois/test/mntner")
                    .request()
                    .cookie("crowd.token_key", "valid-token")
                    .post(Entity.entity(map(updatedObject), MediaType.APPLICATION_XML), WhoisResources.class);
            fail();
        } catch (BadRequestException e) {
            RestTest.assertOnlyErrorMessage(e, "Error", "No RIPE NCC Access Account found for %s", "in@valid.net");
        }
    }

    @Test
    public void create_self_referencing_maintainer_sso_auth_only_invalid_token() {
        try {
            RestTest.target(getPort(), "whois/test/mntner")
                    .request()
                    .cookie("crowd.token_key", "invalid")
                    .post(Entity.entity(map(SSO_ONLY_MNT), MediaType.APPLICATION_XML), WhoisResources.class);
            fail();
        } catch (NotAuthorizedException e) {
            final WhoisResources whoisResources = RestTest.mapClientException(e);
            RestTest.assertErrorCount(whoisResources, 1);
            RestTest.assertErrorMessage(whoisResources, 0, "Error", "Authorisation for [%s] %s failed\nusing \"%s:\"\nnot authenticated by: %s", "mntner", "SSO-ONLY-MNT", "mnt-by", "SSO-ONLY-MNT");
            RestTest.assertInfoCount(whoisResources, 1);
            RestTest.assertErrorMessage(whoisResources, 1, "Info", "RIPE NCC Access token ignored");
        }
    }

    @Test
    public void create_self_referencing_maintainer_password_auth_only_with_invalid_sso_username() {
        final RpslObject updatedObject = new RpslObjectBuilder(PASSWORD_ONLY_MNT).append(new RpslAttribute(AttributeType.AUTH, "SSO in@valid.net")).get();

        try {
            RestTest.target(getPort(), "whois/test/mntner?password=test")
                    .request()
                    .post(Entity.entity(map(updatedObject), MediaType.APPLICATION_XML), WhoisResources.class);
            fail();
        } catch (BadRequestException e) {
            RestTest.assertOnlyErrorMessage(e, "Error", "No RIPE NCC Access Account found for %s", "in@valid.net");
        }
    }

    @Test
    public void create_with_utf8_non_ascii_characters_are_preserved() {
        assertThat(RestTest.target(getPort(), "whois/test/person?password=test")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity("" +
                        "{ \"objects\": {\n" +
                        "   \"object\": [ {\n" +
                        "    \"source\": { \"id\": \"RIPE\" }, \n" +
                        "    \"attributes\": {\n" +
                        "       \"attribute\": [\n" +
                        "        { \"name\": \"person\", \"value\": \"Pauleth Palthen\" },\n" +
                        "        { \"name\": \"address\", \"value\": \"Flughafenstraße 109/a\" },\n" +
                        "        { \"name\": \"phone\", \"value\": \"+31-2-1234567\" },\n" +
                        "        { \"name\": \"e-mail\", \"value\": \"noreply@ripe.net\" },\n" +
                        "        { \"name\": \"mnt-by\", \"value\": \"OWNER-MNT\" },\n" +
                        "        { \"name\": \"nic-hdl\", \"value\": \"PP1-TEST\" },\n" +
                        "        { \"name\": \"remarks\", \"value\": \"created\" },\n" +
                        "        { \"name\": \"source\", \"value\": \"TEST\" }\n" +
                        "        ] }\n" +
                        "    }] \n" +
                        "}}", new MediaType("application", "json", StandardCharsets.UTF_8.displayName())), String.class), containsString("Flughafenstraße 109/a"));

        assertThat(RestTest.target(getPort(), "whois/test/person/PP1-TEST")
                .request(MediaType.APPLICATION_JSON)
                .get(String.class), containsString("Flughafenstraße 109/a"));

        assertThat(RestTest.target(getPort(), "whois/search?query-string=PP1-TEST&source=TEST")
                .request(MediaType.APPLICATION_JSON)
                .get(String.class), containsString("Flughafenstraße 109/a"));

        assertThat(RestTest.target(getPort(), "whois/test/person/PP1-TEST?password=test")
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.entity("" +
                        "{ \"objects\": {\n" +
                        "   \"object\": [ {\n" +
                        "    \"source\": { \"id\": \"RIPE\" }, \n" +
                        "    \"attributes\": {\n" +
                        "       \"attribute\": [\n" +
                        "        { \"name\": \"person\", \"value\": \"Pauleth Palthen\" },\n" +
                        "        { \"name\": \"address\", \"value\": \"Flughafenstraße 109/a\" },\n" +
                        "        { \"name\": \"phone\", \"value\": \"+31-2-1234567\" },\n" +
                        "        { \"name\": \"e-mail\", \"value\": \"noreply@ripe.net\" },\n" +
                        "        { \"name\": \"mnt-by\", \"value\": \"OWNER-MNT\" },\n" +
                        "        { \"name\": \"nic-hdl\", \"value\": \"PP1-TEST\" },\n" +
                        "        { \"name\": \"remarks\", \"value\": \"created\" },\n" +
                        "        { \"name\": \"source\", \"value\": \"TEST\" }\n" +
                        "        ] }\n" +
                        "    }] \n" +
                        "}}", MediaType.APPLICATION_JSON), String.class), containsString("Flughafenstraße 109/a"));
    }

    @Test
    public void create_latin1_non_ascii_characters_encoded_in_latin1_fails() {
        try {
            RestTest.target(getPort(), "whois/test/person?password=test")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity("" +
                        "{ \"objects\": {\n" +
                        "   \"object\": [ {\n" +
                        "    \"source\": { \"id\": \"RIPE\" }, \n" +
                        "    \"attributes\": {\n" +
                        "       \"attribute\": [\n" +
                        "        { \"name\": \"person\", \"value\": \"Pauleth Palthen\" },\n" +
                        "        { \"name\": \"address\", \"value\": \"Flughafenstraße 109/a\" },\n" +
                        "        { \"name\": \"phone\", \"value\": \"+31-2-1234567\" },\n" +
                        "        { \"name\": \"e-mail\", \"value\": \"noreply@ripe.net\" },\n" +
                        "        { \"name\": \"mnt-by\", \"value\": \"OWNER-MNT\" },\n" +
                        "        { \"name\": \"nic-hdl\", \"value\": \"PP1-TEST\" },\n" +
                        "        { \"name\": \"remarks\", \"value\": \"created\" },\n" +
                        "        { \"name\": \"source\", \"value\": \"TEST\" }\n" +
                        "        ] }\n" +
                        "    }] \n" +
                        "}}", new MediaType("application", "json", StandardCharsets.ISO_8859_1.displayName())), String.class);
            fail();
        } catch (BadRequestException e) {
            final WhoisResources whoisResources = e.getResponse().readEntity(WhoisResources.class);
            assertThat(whoisResources.getErrorMessages(), hasSize(1));
            assertThat(whoisResources.getErrorMessages().get(0).toString(), startsWith("JSON processing exception: Invalid UTF-8 middle byte 0x65"));
        }
    }

    @Test
    public void create_dryRun() {
        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person?password=test&dry-run=true")
                .request()
                .post(Entity.entity(map(PAULETH_PALTHEN), MediaType.APPLICATION_XML), WhoisResources.class);

        final List<ErrorMessage> messages = whoisResources.getErrorMessages();
        assertThat(messages, hasSize(2));
        assertThat(messages.getFirst().getText(), is("MD5 hashed password authentication is deprecated and support will be " +
                "removed at the end of 2025. Please switch to an alternative authentication method before then."));
        assertThat(messages.get(1).getText(), is("Dry-run performed, no changes to the database have been made"));
        assertThat(RestTest.target(getPort(), "whois/test/person/PP1-TEST").request().get().getStatus(), is(HttpStatus.NOT_FOUND_404));
    }

    @Test
    public void create_dryRun_queryparam_with_no_value() {
        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person?password=test&dry-run")
                .request()
                .post(Entity.entity(map(PAULETH_PALTHEN), MediaType.APPLICATION_XML), WhoisResources.class);

        final List<ErrorMessage> messages = whoisResources.getErrorMessages();
        assertThat(messages, hasSize(2));
        assertThat(messages.getFirst().getText(), is("MD5 hashed password authentication is deprecated and support will be " +
                "removed at the end of 2025. Please switch to an alternative authentication method before then."));
        assertThat(messages.get(1).getText(), is("Dry-run performed, no changes to the database have been made"));
        assertThat(RestTest.target(getPort(), "whois/test/person/PP1-TEST").request().get().getStatus(), is(HttpStatus.NOT_FOUND_404));
    }

    @Test
    public void create_dryRun_equals_false() {
        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person?password=test&dry-run=false")
                .request()
                .post(Entity.entity(map(PAULETH_PALTHEN), MediaType.APPLICATION_XML), WhoisResources.class);

        final List<ErrorMessage> messages = whoisResources.getErrorMessages();
        assertThat(messages, hasSize(1));
        assertThat(messages.getFirst().getText(), is("MD5 hashed password authentication is deprecated and support will be " +
                "removed at the end of 2025. Please switch to an alternative authentication method before then."));
        assertThat(RestTest.target(getPort(), "whois/test/person/PP1-TEST").request().get().getStatus(), is(HttpStatus.OK_200));
    }

    @Test
    public void update_person_non_latin1_characters_are_substituted() {
        {
            final RpslObject update = new RpslObjectBuilder(TEST_PERSON)
                    .replaceAttribute(TEST_PERSON.findAttribute(AttributeType.ADDRESS),
                            new RpslAttribute(AttributeType.ADDRESS, "Тверская улица,москва")).sort().get();

            final WhoisResources response =
                    RestTest.target(getPort(), "whois/test/person/TP1-TEST?password=test")
                            .request()
                            .put(Entity.entity(whoisObjectMapper.mapRpslObjects(FormattedClientAttributeMapper.class, update), MediaType.APPLICATION_XML),
                                    WhoisResources.class);

            RestTest.assertWarningCount(response, 2);
            RestTest.assertErrorMessage(response, 1, "Warning", "Value changed due to conversion into the ISO-8859-1 (Latin-1) character set");

            final RpslObject lookupObject = databaseHelper.lookupObject(ObjectType.PERSON, "TP1-TEST");
            assertThat(lookupObject.findAttribute(AttributeType.ADDRESS).getValue(), is("        ???????? ?????,??????"));
        }
        {
            final WhoisResources response =
                    RestTest.target(getPort(), "whois/test/person/TP1-TEST?password=test")
                            .request()
                            .get(WhoisResources.class);

            assertThat(response.getWhoisObjects().get(0).getAttributes(), hasItem(new Attribute("address", "???????? ?????,??????")));
        }
    }

    @Test
    public void create_invalid_control_character() {
        try {
            RestTest.target(getPort(), "whois/test/person?password=test")
                .request()
                .post(Entity.entity(
                    "<whois-resources>\n" +
                    "    <objects>\n" +
                    "        <object type=\"person\">\n" +
                    "            <source id=\"TEST\"/>\n" +
                    "            <attributes>\n" +
                    "                <attribute name=\"person\" value=\"New Person\"/>\n" +
                    "                <attribute name=\"address\" value=\"Test\u000cAddress\"/>\n" +
                    "                <attribute name=\"phone\" value=\"+31-1234567890\"/>\n" +
                    "                <attribute name=\"mnt-by\" value=\"OWNER-MNT\"/>\n" +
                    "                <attribute name=\"nic-hdl\" value=\"AUTO-1\"/>\n" +
                    "                <attribute name=\"source\" value=\"TEST\"/>\n" +
                    "            </attributes>\n" +
                    "        </object>\n" +
                    "    </objects>\n" +
                    "</whois-resources>", MediaType.APPLICATION_XML), String.class);
            fail();
        } catch (BadRequestException e) {
            final WhoisResources response = e.getResponse().readEntity(WhoisResources.class);
            RestTest.assertErrorCount(response, 1);
            RestTest.assertErrorMessage(response, 0, "Error",
                "XML processing exception: %s (line: %s, column: %s)",
                "An invalid XML character (Unicode: 0xc) was found in the value of attribute \"value\" and element is \"attribute\".", "7", "54");
        }
    }

    @Test
    public void create_gzip_compressed_request_and_response() {
        final Response response = RestTest.target(getPort(), "whois/test/person?password=test")
                .property(ClientProperties.USE_ENCODING, "gzip")
                .register(EncodingFilter.class)
                .register(GZipEncoder.class)
                .request()
                .post(Entity.entity(map(PAULETH_PALTHEN), new Variant(MediaType.APPLICATION_XML_TYPE, (String) null, "gzip")), Response.class);

        assertThat(response.getHeaderString("Content-Type"), is(MediaType.APPLICATION_XML));
        assertThat(response.getHeaderString("Content-Encoding"), is("gzip"));

        final WhoisResources whoisResources = response.readEntity(WhoisResources.class);
        assertThat(whoisResources.getErrorMessages(), is(not(empty())));
        assertThat(whoisResources.getErrorMessages().getFirst().getText(), is("MD5 hashed password authentication is deprecated and support will be " +
                "removed at the end of 2025. Please switch to an alternative authentication method before then."));
        final WhoisObject object = whoisResources.getWhoisObjects().get(0);
        assertThat(object.getAttributes(), hasItem(new Attribute("person", "Pauleth Palthen")));
    }

    @Test
    public void update_huge_object_with_syntax_error_compressed_response() throws IOException {
        databaseHelper.addAuthoritativeResource("TEST", "AS3333");

        databaseHelper.addObject("aut-num: AS3333\nsource: TEST");

        try {
            RestTest.target(getPort(), "whois/test/aut-num/AS3333.json?password=123")
                    .request()
                    .header(HttpHeaders.ACCEPT_ENCODING, "gzip")
                    .put(Entity.entity(gunzip(new ClassPathResource("as3333.json.gz").getFile()), MediaType.APPLICATION_JSON), WhoisResources.class);
            fail();
        } catch (BadRequestException e) {
            assertThat(e.getResponse().hasEntity(), is(true));

            final String response = gunzip(e.getResponse().readEntity(byte[].class));
            assertThat(response, containsString("\"text\" : \"Syntax error in %s\""));
        }
    }

    @Test
    public void create_person_succeeds_with_notification() throws Exception {
        databaseHelper.addObject(
                "mntner:        TEST-MNT\n" +
                "descr:         Test maintainer\n" +
                "admin-c:       TP1-TEST\n" +
                "upd-to:        upd-to@ripe.net\n" +
                "mnt-nfy:       mnt-nfy@ripe.net\n" +
                "auth:          MD5-PW $1$EmukTVYX$Z6fWZT8EAzHoOJTQI6jFJ1  # 123\n" +
                "mnt-by:        TEST-MNT\n" +
                "source:        TEST"
        );
        final RpslObject person = RpslObject.parse(
                "person:        Pauleth Palthen\n" +
                "address:       Singel 258\n" +
                "phone:         +31-1234567890\n" +
                "e-mail:        noreply@ripe.net\n" +
                "mnt-by:        TEST-MNT\n" +
                "nic-hdl:       PP1-TEST\n" +
                "remarks:       remark\n" +
                "source:        TEST\n"
        );

        RestTest.target(getPort(), "whois/test/person?password=123")
                .request()
                .post(Entity.entity(map(person), MediaType.APPLICATION_XML), WhoisResources.class);

        final String message = mailSenderStub.getMessage("mnt-nfy@ripe.net").getContent().toString();
        assertThat(message, containsString("Pauleth Palthen"));
        assertThat(mailSenderStub.anyMoreMessages(), is(false));
    }

    @Test
    public void create_person_fails_with_notification() throws Exception {
        databaseHelper.addObject(
                "mntner:        TEST-MNT\n" +
                "descr:         Test maintainer\n" +
                "admin-c:       TP1-TEST\n" +
                "upd-to:        upd-to@ripe.net\n" +
                "mnt-nfy:       mnt-nfy@ripe.net\n" +
                "auth:          MD5-PW $1$EmukTVYX$Z6fWZT8EAzHoOJTQI6jFJ1  # 123\n" +
                "mnt-by:        TEST-MNT\n" +
                "source:        TEST"
        );
        final RpslObject person = RpslObject.parse(
                "person:        Pauleth Palthen\n" +
                "address:       Singel 258\n" +
                "phone:         +31-1234567890\n" +
                "e-mail:        noreply@ripe.net\n" +
                "mnt-by:        TEST-MNT\n" +
                "nic-hdl:       PP1-TEST\n" +
                "remarks:       remark\n" +
                "source:        TEST\n"
        );

        try {
            RestTest.target(getPort(), "whois/test/person?password=invalid")
                    .request()
                    .post(Entity.entity(map(person), MediaType.APPLICATION_XML), WhoisResources.class);
            fail();
        } catch (NotAuthorizedException e) {
            final String message = mailSenderStub.getMessage("upd-to@ripe.net").getContent().toString();
            assertThat(message, containsString("Pauleth Palthen"));
            assertThat(mailSenderStub.anyMoreMessages(), is(false));
        }
    }

    @Test
    public void create_person_notifications_with_override() throws Exception {
        databaseHelper.insertUser(User.createWithPlainTextPassword("agoston", "zoh", ObjectType.PERSON));
        databaseHelper.addObject(
                "mntner:        TEST-MNT\n" +
                "descr:         Test maintainer\n" +
                "admin-c:       TP1-TEST\n" +
                "upd-to:        upd-to@ripe.net\n" +
                "mnt-nfy:       mnt-nfy@ripe.net\n" +
                "auth:          MD5-PW $1$EmukTVYX$Z6fWZT8EAzHoOJTQI6jFJ1  # 123\n" +
                "mnt-by:        TEST-MNT\n" +
                "source:        TEST"
        );
        final RpslObject person = RpslObject.parse(
                "person:        Pauleth Palthen\n" +
                "address:       Singel 258\n" +
                "phone:         +31-1234567890\n" +
                "e-mail:        noreply@ripe.net\n" +
                "mnt-by:        TEST-MNT\n" +
                "nic-hdl:       PP1-TEST\n" +
                "remarks:       remark\n" +
                "source:        TEST\n"
        );

        RestTest.target(getPort(), "whois/test/person")
                .queryParam("override", "agoston,zoh,reason")
                .request()
                .post(Entity.entity(map(person), MediaType.APPLICATION_XML), WhoisResources.class);

        final String message = mailSenderStub.getMessage("mnt-nfy@ripe.net").getContent().toString();
        assertThat(message, containsString("Pauleth Palthen"));
        assertThat(mailSenderStub.anyMoreMessages(), is(false));
    }

    @Test
    public void create_person_disable_notifications_with_override() {
        databaseHelper.insertUser(User.createWithPlainTextPassword("agoston", "zoh", ObjectType.PERSON));
        databaseHelper.addObject(
                "mntner:        TEST-MNT\n" +
                "descr:         Test maintainer\n" +
                "admin-c:       TP1-TEST\n" +
                "upd-to:        upd-to@ripe.net\n" +
                "mnt-nfy:       mnt-nfy@ripe.net\n" +
                "auth:          MD5-PW $1$EmukTVYX$Z6fWZT8EAzHoOJTQI6jFJ1  # 123\n" +
                "mnt-by:        TEST-MNT\n" +
                "source:        TEST"
        );
        final RpslObject person = RpslObject.parse(
                "person:        Pauleth Palthen\n" +
                "address:       Singel 258\n" +
                "phone:         +31-1234567890\n" +
                "e-mail:        noreply@ripe.net\n" +
                "mnt-by:        TEST-MNT\n" +
                "nic-hdl:       PP1-TEST\n" +
                "remarks:       remark\n" +
                "source:        TEST\n"
        );

        RestTest.target(getPort(), "whois/test/person")
                .queryParam("override", encode("agoston,zoh,reason {notify=false}"))
                .request()
                .post(Entity.entity(map(person), MediaType.APPLICATION_XML), WhoisResources.class);

        assertThat(mailSenderStub.anyMoreMessages(), is(false));
    }

    @Test
    public void create_person_unformatted() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "person:  Pauleth Palthen\n" +
                "address: Singel 258\n" +
                "phone:   +31\n" +
                "         1234567890\n" +
                "e-mail:  noreply@ripe.net\n" +
                "mnt-by:  OWNER-MNT\n" +
                "nic-hdl: PP1-TEST\n" +
                "remarks: +----------+  #  +-----------+\n" +
                "         |  remark  |  #  |  comment  |\n" +
                "         +----------+  #  +-----------+\n" +
                "source:  TEST\n");

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person?password=test&unformatted")
                .request()
                .post(Entity.entity(mapDirty(rpslObject), MediaType.APPLICATION_XML), WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(not(empty())));
        assertThat(whoisResources.getErrorMessages().getFirst().getText(), is("MD5 hashed password authentication is deprecated and support will be " +
                "removed at the end of 2025. Please switch to an alternative authentication method before then."));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        assertThat(whoisResources.getLink().getHref(), is(String.format("http://localhost:%s/test/person?unformatted", getPort())));

        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getAttributes().get(2).getValue(), is(
                "          " +
                "+31\n" +
                "                " +
                "1234567890"));
        assertThat(whoisObject.getAttributes().get(6).getValue(), is(
                "        " +
                "+----------+  #  +-----------+\n" +
                "                " +
                "|  remark  |  #  |  comment  |\n" +
                "                " +
                "+----------+  #  +-----------+"));

        final String queryResponse = queryTelnet("-r PP1-TEST");
        assertThat(queryResponse, containsString(
                "phone:          +31\n" +
                "                1234567890\n"));
        assertThat(queryResponse, containsString(
                "remarks:        +----------+  #  +-----------+\n" +
                "                |  remark  |  #  |  comment  |\n" +
                "                +----------+  #  +-----------+"));
    }

    @Test
    public void create_multiple_objects_fails() {
        final RpslObject personObject = RpslObject.parse("" +
            "person:    Some Person\n" +
            "address:   Singel 258\n" +
            "phone:     +31-1234567890\n" +
            "e-mail:    noreply@ripe.net\n" +
            "mnt-by:    OWNER-MNT\n" +
            "nic-hdl:   AUTO-1\n" +
            "remarks:   remark\n" +
            "source:    TEST\n");

        try {
            RestTest.target(getPort(), "whois/test/person?password=test")
                    .request()
                    .post(Entity.entity(map(personObject, personObject), MediaType.APPLICATION_XML), String.class);
            fail();
        } catch (BadRequestException e) {
            assertThat(e.getResponse().readEntity(String.class), containsString("Single object expected in WhoisResources"));
        }
    }

    // delete

    @Test
    public void delete_succeeds() {
        databaseHelper.addObject(PAULETH_PALTHEN);

        WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person/PP1-TEST")
                .queryParam("password", "test")
                .request()
                .delete(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), hasSize(1));
        assertThat(whoisResources.getErrorMessages().getFirst().getText(), is("MD5 hashed password authentication is deprecated and support will be " +
                "removed at the end of 2025. Please switch to an alternative authentication method before then."));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        try {
            databaseHelper.lookupObject(ObjectType.PERSON, "PP1-TEST");
            fail();
        } catch (EmptyResultDataAccessException ignored) {
            // expected
        }
    }

    @Test
    public void delete_with_reason_succeeds() {
        databaseHelper.addObject(PAULETH_PALTHEN);

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person/PP1-TEST")
                .queryParam("password", "test")
                .queryParam("reason", "not_needed_no_more")
                .request()
                .delete(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), hasSize(1));
        assertThat(whoisResources.getErrorMessages().getFirst().getText(), is("MD5 hashed password authentication is deprecated and support will be " +
                "removed at the end of 2025. Please switch to an alternative authentication method before then."));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        try {
            databaseHelper.lookupObject(ObjectType.PERSON, "PP1-TEST");
            fail();
        } catch (EmptyResultDataAccessException ignored) {
            // expected
        }
    }

    @Test
    public void delete_person_with_crowd_token_succeeds() {
        databaseHelper.addObject(PAULETH_PALTHEN);

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person/PP1-TEST")
                .request()
                .cookie("crowd.token_key", "valid-token")
                .delete(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        try {
            databaseHelper.lookupObject(ObjectType.PERSON, "PP1-TEST");
            fail();
        } catch (EmptyResultDataAccessException ignored) {
            // expected
        }
    }

    @Test
    public void delete_self_referencing_maintainer_with_sso_auth_attribute_authenticated_with_crowd_token_succeeds() {
        databaseHelper.addObject(SSO_ONLY_MNT);

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/mntner/SSO-ONLY-MNT")
                .request()
                .cookie("crowd.token_key", "valid-token")
                .delete(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        assertThat(whoisResources.getWhoisObjects().get(0).getAttributes(), hasItem(new Attribute("auth", "SSO person@net.net")));

        try {
            databaseHelper.lookupObject(ObjectType.MNTNER, "SSO-ONLY-MNT");
            fail();
        } catch (EmptyResultDataAccessException ignored) {
            // expected
        }
    }

    @Test
    public void delete_self_referencing_maintainer_with_sso_auth_attribute_authenticated_with_password_succeeds() {
        databaseHelper.addObject(SSO_AND_PASSWORD_MNT);

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/mntner/SSO-PASSWORD-MNT")
                .queryParam("password", "test")
                .request()
                .delete(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), hasSize(1));
        assertThat(whoisResources.getErrorMessages().getFirst().getText(), is("MD5 hashed password authentication is deprecated and support will be " +
                "removed at the end of 2025. Please switch to an alternative authentication method before then."));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        assertThat(whoisResources.getWhoisObjects().getFirst().getAttributes(), hasItem(new Attribute("auth", "SSO person@net.net")));

        try {
            databaseHelper.lookupObject(ObjectType.MNTNER, "SSO-PASSWORD-MNT");
            fail();
        } catch (EmptyResultDataAccessException ignored) {
            // expected
        }
    }

    @Test
    public void delete_self_referencing_maintainer_with_sso_auth_attribute_invalid_token_authenticated_with_password_succeeds() {
        databaseHelper.addObject(SSO_AND_PASSWORD_MNT);

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/mntner/SSO-PASSWORD-MNT")
                .queryParam("password", "test")
                .request()
                .cookie("crowd.token_key", "invalid-token")
                .delete(WhoisResources.class);

        RestTest.assertInfoCount(whoisResources, 1);
        RestTest.assertErrorMessage(whoisResources, 1, "Info", "RIPE NCC Access token ignored");
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        assertThat(whoisResources.getWhoisObjects().get(0).getAttributes(), hasItem(new Attribute("auth", "SSO person@net.net")));

        try {
            databaseHelper.lookupObject(ObjectType.MNTNER, "SSO-PASSWORD-MNT");
            fail();
        } catch (EmptyResultDataAccessException ignored) {
            // expected
        }
    }

    @Test
    public void delete_nonexistant() {
        assertThrows(NotFoundException.class, () -> {
            RestTest.target(getPort(), "whois/test/person/NON-EXISTANT")
                    .request()
                    .delete(String.class);
        });
    }

    @Test
    public void delete_referenced_from_other_objects() {
        try {
            RestTest.target(getPort(), "whois/test/person/TP1-TEST")
                    .queryParam("password", "test")
                    .request()
                    .delete(WhoisResources.class);
            fail();
        } catch (BadRequestException e) {
            RestTest.assertOnlyErrorMessage(e, "Error", "Object [%s] %s is referenced from other objects", "person", "TP1-TEST");
        }
    }

    @Test
    public void delete_invalid_password() {
        try {
            databaseHelper.addObject(PAULETH_PALTHEN);
            RestTest.target(getPort(), "whois/test/person/PP1-TEST")
                    .queryParam("password", "invalid")
                    .request()
                    .delete(String.class);
            fail();
        } catch (NotAuthorizedException e) {
            RestTest.assertOnlyErrorMessage(e, "Error", "Authorisation for [%s] %s failed\nusing \"%s:\"\nnot authenticated by: %s", "person", "PP1-TEST", "mnt-by", "OWNER-MNT");
        }
    }

    @Test
    public void delete_no_password() {
        try {
            databaseHelper.addObject(PAULETH_PALTHEN);
            RestTest.target(getPort(), "whois/test/person/PP1-TEST")
                    .request()
                    .delete(String.class);
            fail();
        } catch (NotAuthorizedException e) {
            RestTest.assertOnlyErrorMessage(e, "Error", "Authorisation for [%s] %s failed\nusing \"%s:\"\nnot authenticated by: %s", "person", "PP1-TEST", "mnt-by", "OWNER-MNT");
        }
    }

    @Test
    public void delete_no_auth_does_not_contain_tobedeleted_object() {
        try {
            RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT.json")
                    .request()
                    .delete(String.class);
            fail();
        } catch (NotAuthorizedException e) {
            final WhoisResources whoisResources = RestTest.mapClientException(e);
            assertThat(whoisResources.getWhoisObjects(), is(empty()));
        }
    }

    @Test
    public void delete_person_succeeds_with_notification() throws Exception {
        databaseHelper.addObject(
                "mntner:        TEST-MNT\n" +
                "descr:         Test maintainer\n" +
                "admin-c:       TP1-TEST\n" +
                "upd-to:        upd-to@ripe.net\n" +
                "mnt-nfy:       mnt-nfy@ripe.net\n" +
                "auth:          MD5-PW $1$EmukTVYX$Z6fWZT8EAzHoOJTQI6jFJ1  # 123\n" +
                "mnt-by:        TEST-MNT\n" +
                "source:        TEST"
        );
        databaseHelper.addObject(
                "person:        Pauleth Palthen\n" +
                "address:       Singel 258\n" +
                "phone:         +31-1234567890\n" +
                "e-mail:        noreply@ripe.net\n" +
                "mnt-by:        TEST-MNT\n" +
                "nic-hdl:       PP1-TEST\n" +
                "remarks:       remark\n" +
                "source:        TEST\n"
        );

        RestTest.target(getPort(), "whois/test/person/PP1-TEST")
                .queryParam("password", "123")
                .request()
                .delete(String.class);

        final String message = mailSenderStub.getMessage("mnt-nfy@ripe.net").getContent().toString();
        assertThat(message, containsString("Pauleth Palthen"));
        assertThat(mailSenderStub.anyMoreMessages(), is(false));
    }

    @Test
    public void delete_person_fails_with_notification() throws Exception {
        databaseHelper.addObject(
                "mntner:        TEST-MNT\n" +
                "descr:         Test maintainer\n" +
                "admin-c:       TP1-TEST\n" +
                "upd-to:        upd-to@ripe.net\n" +
                "mnt-nfy:       mnt-nfy@ripe.net\n" +
                "auth:          MD5-PW $1$EmukTVYX$Z6fWZT8EAzHoOJTQI6jFJ1  # 123\n" +
                "mnt-by:        TEST-MNT\n" +
                "source:        TEST"
        );
        databaseHelper.addObject(
                "person:        Pauleth Palthen\n" +
                "address:       Singel 258\n" +
                "phone:         +31-1234567890\n" +
                "e-mail:        noreply@ripe.net\n" +
                "mnt-by:        TEST-MNT\n" +
                "nic-hdl:       PP1-TEST\n" +
                "remarks:       remark\n" +
                "source:        TEST\n"
        );

        try {
            RestTest.target(getPort(), "whois/test/person/PP1-TEST")
                    .queryParam("password", "invalid")
                    .request()
                    .delete(String.class);
            fail();
        } catch (NotAuthorizedException e) {
            final String message = mailSenderStub.getMessage("upd-to@ripe.net").getContent().toString();
            assertThat(message, containsString("Pauleth Palthen"));
            assertThat(mailSenderStub.anyMoreMessages(), is(false));
        }
    }

    @Test
    public void delete_person_notifications_with_override() throws Exception {
        databaseHelper.insertUser(User.createWithPlainTextPassword("agoston", "zoh", ObjectType.PERSON));
        databaseHelper.addObject(
                "mntner:        TEST-MNT\n" +
                "descr:         Test maintainer\n" +
                "admin-c:       TP1-TEST\n" +
                "upd-to:        upd-to@ripe.net\n" +
                "mnt-nfy:       mnt-nfy@ripe.net\n" +
                "auth:          MD5-PW $1$EmukTVYX$Z6fWZT8EAzHoOJTQI6jFJ1  # 123\n" +
                "mnt-by:        TEST-MNT\n" +
                "source:        TEST"
        );
        databaseHelper.addObject(
                "person:        Pauleth Palthen\n" +
                "address:       Singel 258\n" +
                "phone:         +31-1234567890\n" +
                "e-mail:        noreply@ripe.net\n" +
                "mnt-by:        TEST-MNT\n" +
                "nic-hdl:       PP1-TEST\n" +
                "remarks:       remark\n" +
                "source:        TEST\n"
        );

        RestTest.target(getPort(), "whois/test/person/PP1-TEST")
                .queryParam("override", "agoston,zoh,reason")
                .request()
                .delete(String.class);

        final String message = mailSenderStub.getMessage("mnt-nfy@ripe.net").getContent().toString();
        assertThat(message, containsString("Pauleth Palthen"));
        assertThat(mailSenderStub.anyMoreMessages(), is(false));
    }

    @Test
    public void delete_person_disable_notifications_with_override() {
        databaseHelper.insertUser(User.createWithPlainTextPassword("agoston", "zoh", ObjectType.PERSON));
        databaseHelper.addObject(
                "mntner:        TEST-MNT\n" +
                "descr:         Test maintainer\n" +
                "admin-c:       TP1-TEST\n" +
                "upd-to:        upd-to@ripe.net\n" +
                "mnt-nfy:       mnt-nfy@ripe.net\n" +
                "auth:          MD5-PW $1$EmukTVYX$Z6fWZT8EAzHoOJTQI6jFJ1  # 123\n" +
                "mnt-by:        TEST-MNT\n" +
                "source:        TEST"
        );
        databaseHelper.addObject(
                "person:        Pauleth Palthen\n" +
                "address:       Singel 258\n" +
                "phone:         +31-1234567890\n" +
                "e-mail:        noreply@ripe.net\n" +
                "mnt-by:        TEST-MNT\n" +
                "nic-hdl:       PP1-TEST\n" +
                "remarks:       remark\n" +
                "source:        TEST\n"
        );

        RestTest.target(getPort(), "whois/test/person/PP1-TEST")
                .queryParam("override", encode("agoston,zoh,reason {notify=false}"))
                .request()
                .delete(String.class);

        assertThat(mailSenderStub.anyMoreMessages(), is(false));
    }

    @Test
    public void delete_dryrun() {
        databaseHelper.addObject(PAULETH_PALTHEN);

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person/PP1-TEST?password=test&dry-run")
                .request()
                .delete(WhoisResources.class);

        final List<ErrorMessage> messages = whoisResources.getErrorMessages();
        assertThat(messages, hasSize(2));
        assertThat(messages.getFirst().getText(), is("MD5 hashed password authentication is deprecated and support will be " +
                "removed at the end of 2025. Please switch to an alternative authentication method before then."));
        assertThat(messages.get(1).getText(), is("Dry-run performed, no changes to the database have been made"));
        assertThat(RestTest.target(getPort(), "whois/test/person/PP1-TEST").request().get().getStatus(), is(HttpStatus.OK_200));
    }

    // update

    @Test
    public void update_succeeds() {
        databaseHelper.addObject(PAULETH_PALTHEN);
        final RpslObject updatedObject = new RpslObjectBuilder(PAULETH_PALTHEN).append(new RpslAttribute(AttributeType.REMARKS, "updated")).sort().get();

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person/PP1-TEST?password=test")
                .request(MediaType.APPLICATION_XML)
                .put(Entity.entity(map(updatedObject), MediaType.APPLICATION_XML), WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), hasSize(1));
        assertThat(whoisResources.getErrorMessages().getFirst().getText(), is("MD5 hashed password authentication is deprecated and support will be " +
                "removed at the end of 2025. Please switch to an alternative authentication method before then."));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject object = whoisResources.getWhoisObjects().get(0);
        assertThat(object.getAttributes(), contains(
                new Attribute("person", "Pauleth Palthen"),
                new Attribute("address", "Singel 258"),
                new Attribute("phone", "+31-1234567890"),
                new Attribute("e-mail", "noreply@ripe.net"),
                new Attribute("nic-hdl", "PP1-TEST"),
                new Attribute("remarks", "remark"),
                new Attribute("remarks", "updated"),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", Link.create("http://rest-test.db.ripe.net/test/mntner/OWNER-MNT"), null),
                new Attribute("last-modified", "2001-02-04T17:00:00Z"),
                new Attribute("source", "TEST")));

        assertThat(whoisResources.getTermsAndConditions().getHref(), is(WhoisResources.TERMS_AND_CONDITIONS));
    }

    @Test
    public void update_lower_case_source() {
        databaseHelper.addObject(RpslObject.parse("person: Test Person\nnic-hdl: TP2-TEST\nmnt-by: OWNER-MNT\nsource: test"));
        assertThat(databaseHelper.lookupObject(ObjectType.PERSON, "TP2-TEST").toString(), containsString("source:         test"));
        final RpslObject updatedObject = RpslObject.parse(
                "person: Test Person\n" +
                "nic-hdl: TP2-TEST\n" +
                "address: Amsterdam\n" +
                "phone: +31-6-12345678\n" +
                "mnt-by: OWNER-MNT\n" +
                "source: test");

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person/TP2-TEST?password=test")
                .request(MediaType.APPLICATION_XML)
                .put(Entity.entity(map(updatedObject), MediaType.APPLICATION_XML), WhoisResources.class);

        RestTest.assertInfoCount(whoisResources, 1);
        RestTest.assertErrorMessage(whoisResources, 1, "Info", "Value %s converted to %s", "test", "TEST");
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject object = whoisResources.getWhoisObjects().get(0);
        assertThat(object.getAttributes().get(object.getAttributes().size() - 1).getValue(), is("TEST"));
    }

    @Test
    public void update_as_set_succeeds() {
        final RpslObject TEST_AS_SET = RpslObject.parse("""
                as-set:     AS3334:AS-TEST
                tech-c:     TP1-TEST
                admin-c:    TP1-TEST
                mnt-by:     OWNER-MNT
                source:     TEST
                """);

        databaseHelper.addObject(TEST_AS_SET);
        databaseHelper.addObject("""
                    aut-num:    AS3334
                    as-name:    End-User-1
                    tech-c:     TP1-TEST
                    admin-c:    TP1-TEST
                    mnt-by:     OWNER-MNT
                    source:     TEST-NONAUTH
                    """);


        final RpslObject updatedObject = new RpslObjectBuilder(TEST_AS_SET).replaceAttribute(TEST_AS_SET.findAttribute(AttributeType.SOURCE),
                        new RpslAttribute(AttributeType.SOURCE, "TEST-NONAUTH")).sort().get();

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/as-set/AS3334:AS-TEST?password" +
                        "=test")
                .request(MediaType.APPLICATION_XML)
                .put(Entity.entity(map(updatedObject), MediaType.APPLICATION_XML), WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), hasSize(1));
        assertThat(whoisResources.getErrorMessages().getFirst().getText(), is("MD5 hashed password authentication is deprecated and support will be " +
                "removed at the end of 2025. Please switch to an alternative authentication method before then."));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject object = whoisResources.getWhoisObjects().get(0);
        assertThat(object.getSource().getId(), is("test-nonauth"));
    }


    @Test
    public void update_as_set_when_aut_num_deleted_succeeds() {
        final RpslObject TEST_AS_SET = RpslObject.parse("""
                as-set:     AS3334:AS-TEST
                tech-c:     TP1-TEST
                admin-c:    TP1-TEST
                mnt-by:     OWNER-MNT
                source:     TEST
                """);

        databaseHelper.addObject(TEST_AS_SET);


        final RpslObject updatedObject = new RpslObjectBuilder(TEST_AS_SET).replaceAttribute(TEST_AS_SET.findAttribute(AttributeType.SOURCE),
                new RpslAttribute(AttributeType.SOURCE, "TEST-NONAUTH")).sort().get();

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/as-set/AS3334:AS-TEST?password" +
                        "=test")
                .request(MediaType.APPLICATION_XML)
                .put(Entity.entity(map(updatedObject), MediaType.APPLICATION_XML), WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), hasSize(1));
        assertThat(whoisResources.getErrorMessages().getFirst().getText(), is("MD5 hashed password authentication is deprecated and support will be " +
                "removed at the end of 2025. Please switch to an alternative authentication method before then."));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject object = whoisResources.getWhoisObjects().get(0);
        assertThat(object.getSource().getId(), is("test-nonauth"));
    }
    @Test
    public void update_as_set_with_wrong_source_succeeds() {
        final RpslObject TEST_AS_SET = RpslObject.parse("""
                as-set:     AS3334:AS-TEST
                tech-c:     TP1-TEST
                admin-c:    TP1-TEST
                mnt-by:     OWNER-MNT
                source:     TEST-NONAUTH
                """);

        databaseHelper.addObject(TEST_AS_SET);
        databaseHelper.addObject("""
                    aut-num:    AS3334
                    as-name:    End-User-1
                    tech-c:     TP1-TEST
                    admin-c:    TP1-TEST
                    mnt-by:     OWNER-MNT
                    source:     TEST-NONAUTH
                    """);


        final RpslObject updatedObject = new RpslObjectBuilder(TEST_AS_SET).replaceAttribute(TEST_AS_SET.findAttribute(AttributeType.SOURCE),
                new RpslAttribute(AttributeType.SOURCE, "TEST")).sort().get();

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/as-set/AS3334:AS-TEST?password" +
                        "=test")
                .request(MediaType.APPLICATION_XML)
                .put(Entity.entity(map(updatedObject), MediaType.APPLICATION_XML), WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), hasSize(3));
        assertThat(whoisResources.getErrorMessages().getFirst().getText(), is("MD5 hashed password authentication is deprecated and support will be " +
                "removed at the end of 2025. Please switch to an alternative authentication method before then."));
        assertThat(whoisResources.getErrorMessages().get(1).toString(), is("The \"source:\" attribute value has been updated from \"TEST\" to \"TEST-NONAUTH\" to match the referenced AUT-NUM \"AS3334\""));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject object = whoisResources.getWhoisObjects().get(0);
        assertThat(object.getSource().getId(), is("test-nonauth"));
    }


    @Test
    public void update_flat_as_set_change_source_bad_request() {
        final RpslObject TEST_AS_SET = RpslObject.parse("""
                as-set:     AS-TEST
                tech-c:     TP1-TEST
                admin-c:    TP1-TEST
                mnt-by:     OWNER-MNT
                source:     TEST
                """);

        databaseHelper.addObject(TEST_AS_SET);
        databaseHelper.addObject("""
                    aut-num:    AS3334
                    as-name:    End-User-1
                    tech-c:     TP1-TEST
                    admin-c:    TP1-TEST
                    mnt-by:     OWNER-MNT
                    source:     TEST-NONAUTH
                    """);


        final RpslObject updatedObject = new RpslObjectBuilder(TEST_AS_SET).replaceAttribute(TEST_AS_SET.findAttribute(AttributeType.SOURCE),
                new RpslAttribute(AttributeType.SOURCE, "TEST-NONAUTH")).sort().get();


        final WhoisResources whoisResources =RestTest.target(getPort(), "whois/test/as-set/AS-TEST?password" +
                            "=test")
                    .request(MediaType.APPLICATION_XML)
                    .put(Entity.entity(map(updatedObject), MediaType.APPLICATION_XML), WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), hasSize(3));
        assertThat(whoisResources.getErrorMessages().getFirst().getText(), is("MD5 hashed password authentication is deprecated and support will be " +
                "removed at the end of 2025. Please switch to an alternative authentication method before then."));
        assertThat(whoisResources.getErrorMessages().get(1).toString(), is("Supplied attribute 'source' has been replaced with a generated value"));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject object = whoisResources.getWhoisObjects().getFirst();
        assertThat(object.getSource().getId(), is("test"));
    }
    @Test
    public void update_missing_attribute_value() {
        try {
            RestTest.target(getPort(), "whois/test/person/PP1-TEST?password=test")
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.entity(
                    "{\n" +
                            "  \"objects\": {\n" +
                            "    \"object\": [\n" +
                            "      {\n" +
                            "        \"attributes\": {\n" +
                            "          \"attribute\": [\n" +
                            "            {\n" +
                            "              \"name\": \"person\",\n" +
                            "              \"value\": \"Pauleth Palthen\"\n" +
                            "            },\n" +
                            "            {\n" +
                            "              \"name\": \"source\"\n" +
                            "            }\n" +
                            "          ]\n" +
                            "        }\n" +
                            "      }\n" +
                            "    ]\n" +
                            "  }\n" +
                            "}",
                    MediaType.APPLICATION_JSON), String.class);
            fail();
        } catch (BadRequestException expected) {
            final WhoisResources whoisResources = expected.getResponse().readEntity(WhoisResources.class);
            RestTest.assertErrorCount(whoisResources, 1);
            RestTest.assertErrorMessage(whoisResources, 0, "Error", "Attribute source has no value");
        }
    }

    @Test
    public void update_noop() {
        databaseHelper.addObject(PAULETH_PALTHEN);

        WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person/PP1-TEST?password=test")
                .request(MediaType.APPLICATION_XML)
                .put(Entity.entity(map(PAULETH_PALTHEN), MediaType.APPLICATION_XML), WhoisResources.class);

        RestTest.assertWarningCount(whoisResources, 2);
        RestTest.assertErrorMessage(whoisResources, 1, "Warning", "Submitted object identical to database object");

        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject object = whoisResources.getWhoisObjects().get(0);
        assertThat(object.getAttributes(), containsInAnyOrder(
                new Attribute("person", "Pauleth Palthen"),
                new Attribute("address", "Singel 258"),
                new Attribute("phone", "+31-1234567890"),
                new Attribute("e-mail", "noreply@ripe.net"),
                new Attribute("nic-hdl", "PP1-TEST"),
                new Attribute("remarks", "remark"),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", Link.create("http://rest-test.db.ripe.net/test/mntner/OWNER-MNT"), null),
                new Attribute("source", "TEST")));

        assertThat(whoisResources.getTermsAndConditions().getHref(), is(WhoisResources.TERMS_AND_CONDITIONS));
    }

    @Test
    public void update_noop_with_override() {
        databaseHelper.addObject(PAULETH_PALTHEN);
        databaseHelper.insertUser(User.createWithPlainTextPassword("agoston", "zoh", ObjectType.PERSON));

        WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person/PP1-TEST?override=agoston,zoh,reason")
                .request(MediaType.APPLICATION_XML)
                .put(Entity.entity(map(PAULETH_PALTHEN), MediaType.APPLICATION_XML), WhoisResources.class);

        RestTest.assertWarningCount(whoisResources, 1);
        RestTest.assertErrorMessage(whoisResources, 0, "Warning", "Submitted object identical to database object");
        RestTest.assertInfoCount(whoisResources, 1);
        RestTest.assertErrorMessage(whoisResources, 1, "Info", "Authorisation override used");

        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject object = whoisResources.getWhoisObjects().get(0);
        assertThat(object.getAttributes(), containsInAnyOrder(
                new Attribute("person", "Pauleth Palthen"),
                new Attribute("address", "Singel 258"),
                new Attribute("phone", "+31-1234567890"),
                new Attribute("e-mail", "noreply@ripe.net"),
                new Attribute("nic-hdl", "PP1-TEST"),
                new Attribute("remarks", "remark"),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", Link.create("http://rest-test.db.ripe.net/test/mntner/OWNER-MNT"), null),
                new Attribute("source", "TEST")));

        assertThat(whoisResources.getTermsAndConditions().getHref(), is(WhoisResources.TERMS_AND_CONDITIONS));
    }

    @Test
    public void update_spaces_in_password_succeeds() {
        databaseHelper.addObject(RpslObject.parse("" +
                "mntner:      OWNER2-MNT\n" +
                "descr:       Owner Maintainer\n" +
                "admin-c:     TP1-TEST\n" +
                "upd-to:      noreply@ripe.net\n" +
                "auth:        MD5-PW $1$d9fKeTr2$NitG3QQZnA4z6zp1o.qmm/ # ' spaces '\n" +
                "mnt-by:      OWNER2-MNT\n" +
                "source:      TEST"));

        final String response = RestTest.target(getPort(), "whois/test/mntner/OWNER2-MNT?password=%20spaces%20")
                .request(MediaType.APPLICATION_XML)
                .put(Entity.entity("" +
                        "<whois-resources>\n" +
                        "    <objects>\n" +
                        "        <object type=\"mntner\">\n" +
                        "            <source id=\"TEST\"/>\n" +
                        "            <attributes>\n" +
                        "                <attribute name=\"mntner\" value=\"OWNER2-MNT\"/>\n" +
                        "                <attribute name=\"descr\" value=\"Owner Maintainer\"/>\n" +
                        "                <attribute name=\"admin-c\" value=\"TP1-TEST\"/>\n" +
                        "                <attribute name=\"upd-to\" value=\"noreply@ripe.net\"/>\n" +
                        "                <attribute name=\"auth\" value=\"MD5-PW $1$d9fKeTr2$NitG3QQZnA4z6zp1o.qmm/\"/>\n" +
                        "                <attribute name=\"remarks\" value=\"updated\"/>\n" +
                        "                <attribute name=\"mnt-by\" value=\"OWNER2-MNT\"/>\n" +
                        "                <attribute name=\"source\" value=\"TEST\"/>\n" +
                        "            </attributes>\n" +
                        "        </object>\n" +
                        "    </objects>\n" +
                        "</whois-resources>", MediaType.APPLICATION_XML), String.class);

        assertThat(response, containsString("<attribute name=\"remarks\" value=\"updated\"/>"));
        assertThat(response, containsString("MD5 hashed password authentication is deprecated and support will be " +
                "removed at the end of 2025. Please switch to an alternative authentication method before then."));
    }

    @Test
    public void update_json_request_and_response_content() {
        final String update = "" +
                "{\n" +
                "  \"objects\": {\n" +
                "    \"object\": [\n" +
                "      {\n" +
                "        \"source\": {\n" +
                "          \"id\": \"test\"\n" +
                "        },\n" +
                "        \"attributes\": {\n" +
                "          \"attribute\": [\n" +
                "            {\n" +
                "              \"name\": \"mntner\",\n" +
                "              \"value\": \"OWNER-MNT\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"name\": \"descr\",\n" +
                "              \"value\": \"description\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"name\": \"admin-c\",\n" +
                "              \"value\": \"TP1-TEST\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"name\": \"upd-to\",\n" +
                "              \"value\": \"noreply@ripe.net\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"name\": \"auth\",\n" +
                "              \"value\": \"MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n\\/cqk\\/\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"name\": \"mnt-by\",\n" +
                "              \"value\": \"OWNER-MNT\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"name\": \"source\",\n" +
                "              \"value\": \"TEST\"\n" +
                "            }\n" +
                "          ]\n" +
                "        }\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}";

        final String response = RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT?password=test")
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.entity(update, MediaType.APPLICATION_JSON), String.class);

        assertThat(response, is(String.format("" +
                "{\n" +
                "  \"link\" : {\n" +
                "    \"type\" : \"locator\",\n" +
                "    \"href\" : \"http://localhost:%s/test/mntner/OWNER-MNT\"\n" +
                "  },\n" +
                "  \"objects\" : {\n" +
                "    \"object\" : [ {\n" +
                "      \"type\" : \"mntner\",\n" +
                "      \"link\" : {\n" +
                "        \"type\" : \"locator\",\n" +
                "        \"href\" : \"http://rest-test.db.ripe.net/test/mntner/OWNER-MNT\"\n" +
                "      },\n" +
                "      \"source\" : {\n" +
                "        \"id\" : \"test\"\n" +
                "      },\n" +
                "      \"primary-key\" : {\n" +
                "        \"attribute\" : [ {\n" +
                "          \"name\" : \"mntner\",\n" +
                "          \"value\" : \"OWNER-MNT\"\n" +
                "        } ]\n" +
                "      },\n" +
                "      \"attributes\" : {\n" +
                "        \"attribute\" : [ {\n" +
                "          \"name\" : \"mntner\",\n" +
                "          \"value\" : \"OWNER-MNT\"\n" +
                "        }, {\n" +
                "          \"name\" : \"descr\",\n" +
                "          \"value\" : \"description\"\n" +
                "        }, {\n" +
                "          \"link\" : {\n" +
                "            \"type\" : \"locator\",\n" +
                "            \"href\" : \"http://rest-test.db.ripe.net/test/person/TP1-TEST\"\n" +
                "          },\n" +
                "          \"name\" : \"admin-c\",\n" +
                "          \"value\" : \"TP1-TEST\",\n" +
                "          \"referenced-type\" : \"person\"\n" +
                "        }, {\n" +
                "          \"name\" : \"upd-to\",\n" +
                "          \"value\" : \"noreply@ripe.net\"\n" +
                "        }, {\n" +
                "          \"name\" : \"auth\",\n" +
                "          \"value\" : \"MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/\"\n" +
                "        }, {\n" +
                "          \"link\" : {\n" +
                "            \"type\" : \"locator\",\n" +
                "            \"href\" : \"http://rest-test.db.ripe.net/test/mntner/OWNER-MNT\"\n" +
                "          },\n" +
                "          \"name\" : \"mnt-by\",\n" +
                "          \"value\" : \"OWNER-MNT\",\n" +
                "          \"referenced-type\" : \"mntner\"\n" +
                "        }, {\n" +
                "          \"name\" : \"last-modified\",\n" +
                "          \"value\" : \"2001-02-04T17:00:00Z\"\n" +
                "        }, {\n" +
                "          \"name\" : \"source\",\n" +
                "          \"value\" : \"TEST\"\n" +
                "        } ]\n" +
                "      }\n" +
                "    } ]\n" +
                "  },\n" +
                "  \"errormessages\" : {\n" +
                "    \"errormessage\" : [ {\n" +
                "      \"severity\" : \"Warning\",\n" +
                "      \"text\" : \"MD5 hashed password authentication is deprecated and support will be removed at the end of 2025. Please switch to an alternative authentication method before then.\"\n" +
                "    } ]\n" +
                "  },\n" +
                "  \"terms-and-conditions\" : {\n" +
                "    \"type\" : \"locator\",\n" +
                "    \"href\" : \"https://docs.db.ripe.net/terms-conditions.html\"\n" +
                "  }\n" +
                "}", getPort())));
    }

    @Test
    public void update_path_vs_object_mismatch_objecttype() {
        try {
            databaseHelper.addObject(PAULETH_PALTHEN);
            RestTest.target(getPort(), "whois/test/mntner/PP1-TEST?password=test")
                    .request(MediaType.APPLICATION_XML)
                    .put(Entity.entity(map(PAULETH_PALTHEN), MediaType.APPLICATION_XML), WhoisResources.class);
            fail();
        } catch (BadRequestException e) {
            RestTest.assertOnlyErrorMessage(e, "Error", "Object type and key specified in URI (%s: %s) do not match the WhoisResources contents", "mntner", "PP1-TEST");
        }
    }

    @Test
    public void update_path_vs_object_mismatch_key() {
        try {
            RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT?password=test")
                    .request(MediaType.APPLICATION_XML)
                    .put(Entity.entity(map(PAULETH_PALTHEN), MediaType.APPLICATION_XML), WhoisResources.class);
            fail();
        } catch (BadRequestException e) {
            RestTest.assertOnlyErrorMessage(e, "Error", "Object type and key specified in URI (%s: %s) do not match the WhoisResources contents", "mntner", "OWNER-MNT");
        }
    }

    @Test
    public void update_without_query_params() {
        try {
            databaseHelper.addObject(PAULETH_PALTHEN);
            RestTest.target(getPort(), "whois/test/person/PP1-TEST")
                    .request(MediaType.APPLICATION_XML)
                    .put(Entity.entity(map(PAULETH_PALTHEN), MediaType.APPLICATION_XML), WhoisResources.class);
            fail();
        } catch (NotAuthorizedException e) {
            RestTest.assertOnlyErrorMessage(e, "Error", "Authorisation for [%s] %s failed\nusing \"%s:\"\nnot authenticated by: %s", "person", "PP1-TEST", "mnt-by", "OWNER-MNT");
        }
    }

    @Test
    public void update_post_not_allowed() {
        assertThrows(NotAllowedException.class, () -> {
            RestTest.target(getPort(), "whois/test/person/PP1-TEST?password=test")
                    .request(MediaType.APPLICATION_XML)
                    .post(Entity.entity(map(PAULETH_PALTHEN), MediaType.APPLICATION_XML), String.class);
        });
    }

    @Test
    public void update_missing_mandatory_fields() {
        final RpslObject updatedObject = new RpslObjectBuilder(PAULETH_PALTHEN).removeAttributeType(AttributeType.MNT_BY).get();

        try {
            RestTest.target(getPort(), "whois/test/person/PP1-TEST?password=test")
                    .request(MediaType.APPLICATION_XML)
                    .put(Entity.entity(map(updatedObject), MediaType.APPLICATION_XML), WhoisResources.class);
            fail();
        } catch (BadRequestException e) {
            RestTest.assertOnlyErrorMessage(e, "Error", "Mandatory attribute \"%s\" is missing", "mnt-by");
        }
    }

    @Test
    public void update_person_with_crowd_token_succeeds() {
        databaseHelper.addObject(PAULETH_PALTHEN);
        final RpslObject updatedObject = new RpslObjectBuilder(PAULETH_PALTHEN).append(new RpslAttribute(AttributeType.REMARKS, "updated")).sort().get();

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person/PP1-TEST")
                .request(MediaType.APPLICATION_XML)
                .cookie("crowd.token_key", "valid-token")
                .put(Entity.entity(map(updatedObject), MediaType.APPLICATION_XML), WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        assertThat(whoisResources.getWhoisObjects().get(0).getAttributes(), hasItem(new Attribute("remarks", "updated")));
    }

    @Test
    public void update_maintainer_with_invalid_sso_username_fails() {
        final RpslObject updatedObject = new RpslObjectBuilder(OWNER_MNT).replaceAttribute(
                new RpslAttribute(AttributeType.AUTH, "SSO person@net.net"),
                new RpslAttribute(AttributeType.AUTH, "SSO in@valid.net")).get();

        try {
            RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT")
                    .request(MediaType.APPLICATION_XML)
                    .cookie("crowd.token_key", "valid-token")
                    .put(Entity.entity(map(updatedObject), MediaType.APPLICATION_XML), WhoisResources.class);
            fail();
        } catch (BadRequestException e) {
            RestTest.assertOnlyErrorMessage(e, "Error", "No RIPE NCC Access Account found for %s", "in@valid.net");
        }
    }

    @Test
    public void update_person_with_invalid_crowd_token_fails() {
        databaseHelper.addObject(PAULETH_PALTHEN);
        final RpslObject updatedObject = new RpslObjectBuilder(PAULETH_PALTHEN).append(new RpslAttribute(AttributeType.REMARKS, "updated")).sort().get();

        try {
            RestTest.target(getPort(), "whois/test/person/PP1-TEST")
                    .request(MediaType.APPLICATION_XML)
                    .cookie("crowd.token_key", "invalid-token")
                    .put(Entity.entity(map(updatedObject), MediaType.APPLICATION_XML), WhoisResources.class);
            fail();
        } catch (NotAuthorizedException e) {
            final WhoisResources whoisResources = RestTest.mapClientException(e);
            RestTest.assertErrorCount(whoisResources, 1);
            RestTest.assertErrorMessage(whoisResources, 0, "Error", "Authorisation for [%s] %s failed\nusing \"%s:\"\nnot authenticated by: %s", "person", "PP1-TEST", "mnt-by", "OWNER-MNT");
            RestTest.assertInfoCount(whoisResources, 1);
            RestTest.assertErrorMessage(whoisResources, 1, "Info", "RIPE NCC Access token ignored");
        }
    }

    @Test
    public void update_mntner_with_invalid_auth_returns_supplied_object_and_not_sensitive_info() {
        final RpslObject updatedObject = new RpslObjectBuilder(OWNER_MNT)
                .removeAttributeType(AttributeType.AUTH)
                .addAttributeSorted(new RpslAttribute(AttributeType.AUTH, "SSO random@ripe.net")).get();

        try {
            RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT")
                    .request(MediaType.APPLICATION_XML)
                    .put(Entity.entity(map(updatedObject), MediaType.APPLICATION_XML), WhoisResources.class);
            fail();
        } catch (NotAuthorizedException e) {
            final String response = e.getResponse().readEntity(String.class);
            assertThat(response, containsString("SSO random@ripe.net"));
            assertThat(response, not(containsString("SSO person@net.net")));
            assertThat(response, not(containsString("MD5-PW")));
            assertThat(response, not(containsString("MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/")));
        }
    }

    @Test
    public void update_bad_input_empty_body() {
        assertThrows(BadRequestException.class, () -> {
            RestTest.target(getPort(), "whois/test/person/TP1-TEST?password=test")
                    .request(MediaType.APPLICATION_XML)
                    .put(Entity.entity("", MediaType.APPLICATION_XML), WhoisResources.class);
        });
    }

    @Test
    public void update_comment_is_not_noop() {
        assertThat(TEST_PERSON.findAttributes(AttributeType.REMARKS), hasSize(0));
        final RpslObjectBuilder builder = new RpslObjectBuilder(TEST_PERSON);
        final RpslAttribute remarks = new RpslAttribute(AttributeType.REMARKS, "updated # comment");
        builder.append(remarks);

        RestTest.target(getPort(), "whois/test/person/TP1-TEST?password=test")
                    .request(MediaType.APPLICATION_XML)
                .put(Entity.entity(map(builder.sort().get()), MediaType.APPLICATION_XML), WhoisResources.class);

        builder.replaceAttribute(remarks, new RpslAttribute(AttributeType.REMARKS, "updated # new comment"));

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person/TP1-TEST?password=test")
                .request(MediaType.APPLICATION_XML)
                .put(Entity.entity(map(builder.sort().get()), MediaType.APPLICATION_XML), WhoisResources.class);

        final WhoisObject object = whoisResources.getWhoisObjects().get(0);
        assertThat(object.getAttributes(), hasItem(new Attribute("remarks", "updated", "new comment", null, null, null)));
    }

    @Test
    public void update_with_override_succeeds() {
        databaseHelper.addObject(PAULETH_PALTHEN);
        databaseHelper.insertUser(User.createWithPlainTextPassword("agoston", "zoh", ObjectType.PERSON));

        final RpslObject updatedObject = new RpslObjectBuilder(PAULETH_PALTHEN).append(new RpslAttribute(AttributeType.REMARKS, "updated")).sort().get();

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person/PP1-TEST?override=agoston,zoh,reason")
                .request(MediaType.APPLICATION_XML)
                .put(Entity.entity(map(updatedObject), MediaType.APPLICATION_XML), WhoisResources.class);

        RestTest.assertInfoCount(whoisResources, 1);
        RestTest.assertErrorMessage(whoisResources, 0, "Info", "Authorisation override used");

        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject object = whoisResources.getWhoisObjects().get(0);
        assertThat(object.getAttributes(), contains(
                new Attribute("person", "Pauleth Palthen"),
                new Attribute("address", "Singel 258"),
                new Attribute("phone", "+31-1234567890"),
                new Attribute("e-mail", "noreply@ripe.net"),
                new Attribute("nic-hdl", "PP1-TEST"),
                new Attribute("remarks", "remark"),
                new Attribute("remarks", "updated"),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", Link.create("http://rest-test.db.ripe.net/test/mntner/OWNER-MNT"), null),
                new Attribute("last-modified", "2001-02-04T17:00:00Z"),
                new Attribute("source", "TEST")));

        assertThat(whoisResources.getTermsAndConditions().getHref(), is(WhoisResources.TERMS_AND_CONDITIONS));
    }

    @Test
    public void update_with_override_trusted_ip_with_sso_succeeds() {
        databaseHelper.insertUser(User.createWithPlainTextPassword("db_e2e_1", "zoh", ObjectType.PERSON));

        final RpslObject TEST_PERSON_2 = RpslObject.parse("" +
                "person:    Test Person2\n" +
                "address:   Singel 258\n" +
                "phone:     +31 6 12345678\n" +
                "nic-hdl:   TP2-TEST\n" +
                "mnt-by:    OWNER-MNT\n" +
                "source:    TEST\n");

        databaseHelper.addObject(TEST_PERSON_2);

        final RpslObject updatedObject = new RpslObjectBuilder(TEST_PERSON_2).append(new RpslAttribute(AttributeType.REMARKS, "updated")).sort().get();

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person/TP2-TEST")
                .queryParam("override", encode("db_e2e_1,zoh,reason {notify=false}"))
                .request(MediaType.APPLICATION_XML)
                .cookie("crowd.token_key", "db_e2e_1")
                .put(Entity.entity(map(updatedObject), MediaType.APPLICATION_XML), WhoisResources.class);

        RestTest.assertInfoCount(whoisResources, 1);
        RestTest.assertErrorMessage(whoisResources, 0, "Info", "Authorisation override used");

        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject object = whoisResources.getWhoisObjects().get(0);
        assertThat(object.getPrimaryKey(), contains(new Attribute("nic-hdl", "TP2-TEST")));
        assertThat(object.getAttributes().stream().filter( (attribute) -> attribute.getName().equals("remarks")).collect(Collectors.toSet()), contains(new Attribute("remarks", "updated")));

    }

    @Test
    public void update_person_succeeds_with_notification() throws Exception {
        final RpslObject mntner = RpslObject.parse(
                "mntner:        TEST-MNT\n" +
                "descr:         Test maintainer\n" +
                "admin-c:       TP1-TEST\n" +
                "upd-to:        upd-to@ripe.net\n" +
                "mnt-nfy:       mnt-nfy@ripe.net\n" +
                "auth:          MD5-PW $1$EmukTVYX$Z6fWZT8EAzHoOJTQI6jFJ1  # 123\n" +
                "mnt-by:        TEST-MNT\n" +
                "source:        TEST"
        );
        databaseHelper.addObject(mntner);
        final RpslObject person = RpslObject.parse(
                "person:        Pauleth Palthen\n" +
                "address:       Singel 258\n" +
                "phone:         +31-1234567890\n" +
                "e-mail:        noreply@ripe.net\n" +
                "mnt-by:        TEST-MNT\n" +
                "nic-hdl:       PP1-TEST\n" +
                "remarks:       remark\n" +
                "source:        TEST\n"
        );
        databaseHelper.addObject(person);
        final RpslObject updatedPerson = new RpslObjectBuilder(person).append(new RpslAttribute(AttributeType.REMARKS, "updated")).get();

        RestTest.target(getPort(), "whois/test/person/PP1-TEST?password=123")
                .request()
                .put(Entity.entity(map(updatedPerson), MediaType.APPLICATION_XML), WhoisResources.class);

        final String message = mailSenderStub.getMessage("mnt-nfy@ripe.net").getContent().toString();
        assertThat(message, containsString("Pauleth Palthen"));
        assertThat(mailSenderStub.anyMoreMessages(), is(false));
    }

    @Test
    public void update_person_fails_with_notification() throws Exception {
        final RpslObject mntner = RpslObject.parse(
                "mntner:        TEST-MNT\n" +
                "descr:         Test maintainer\n" +
                "admin-c:       TP1-TEST\n" +
                "upd-to:        upd-to@ripe.net\n" +
                "mnt-nfy:       mnt-nfy@ripe.net\n" +
                "auth:          MD5-PW $1$EmukTVYX$Z6fWZT8EAzHoOJTQI6jFJ1  # 123\n" +
                "mnt-by:        TEST-MNT\n" +
                "source:        TEST"
        );
        databaseHelper.addObject(mntner);
        final RpslObject person = RpslObject.parse(
                "person:        Pauleth Palthen\n" +
                "address:       Singel 258\n" +
                "phone:         +31-1234567890\n" +
                "e-mail:        noreply@ripe.net\n" +
                "mnt-by:        TEST-MNT\n" +
                "nic-hdl:       PP1-TEST\n" +
                "remarks:       remark\n" +
                "source:        TEST\n"
        );
        databaseHelper.addObject(person);
        final RpslObject updatedPerson = new RpslObjectBuilder(person).append(new RpslAttribute(AttributeType.REMARKS, "updated")).get();

        try {
            RestTest.target(getPort(), "whois/test/person/PP1-TEST?password=invalid")
                    .request()
                    .put(Entity.entity(map(updatedPerson), MediaType.APPLICATION_XML), WhoisResources.class);
            fail();
        } catch (NotAuthorizedException e) {
            final String message = mailSenderStub.getMessage("upd-to@ripe.net").getContent().toString();
            assertThat(message, containsString("Pauleth Palthen"));
            assertThat(mailSenderStub.anyMoreMessages(), is(false));
        }
    }

    @Test
    public void update_person_fails_when_pkey_changes() {

        final RpslObject person = RpslObject.parse(
                "person:        Pauleth Palthen\n" +
                        "address:       Singel 258\n" +
                        "phone:         +31-1234567890\n" +
                        "e-mail:        noreply@ripe.net\n" +
                        "mnt-by:        OWNER-MNT\n" +
                        "nic-hdl:       PP2-TEST\n" +
                        "remarks:       remarks\n" +
                        "source:        TEST\n"
        );

        try {
            RestTest.target(getPort(), "whois/test/person/PP1-TEST?password=test")
                    .request()
                    .put(Entity.entity(map(person), MediaType.APPLICATION_XML), WhoisResources.class);
            fail();
        } catch (BadRequestException e) {
            assertThat(e.getResponse().readEntity(String.class), containsString("Primary key (%s) cannot be modified"));
        }
    }

    @Test
    public void update_person_fails_no_notification_on_syntax_error() {
        final RpslObject mntner = RpslObject.parse(
                "mntner:        TEST-MNT\n" +
                "descr:         Test maintainer\n" +
                "admin-c:       TP1-TEST\n" +
                "upd-to:        upd-to@ripe.net\n" +
                "mnt-nfy:       mnt-nfy@ripe.net\n" +
                "auth:          MD5-PW $1$EmukTVYX$Z6fWZT8EAzHoOJTQI6jFJ1  # 123\n" +
                "mnt-by:        TEST-MNT\n" +
                "source:        TEST"
        );
        databaseHelper.addObject(mntner);
        final RpslObject person = RpslObject.parse(
                "person:        Pauleth Palthen\n" +
                "address:       Singel 258\n" +
                "phone:         +31-1234567890\n" +
                "e-mail:        noreply@ripe.net\n" +
                "mnt-by:        TEST-MNT\n" +
                "nic-hdl:       PP1-TEST\n" +
                "remarks:       remark\n" +
                "source:        TEST\n"
        );
        databaseHelper.addObject(person);

        final RpslObject updatedPerson = new RpslObjectBuilder(person).append(new RpslAttribute(AttributeType.PHONE, "invalid")).get();

        try {
            RestTest.target(getPort(), "whois/test/person/PP1-TEST?password=123")
                    .request()
                    .put(Entity.entity(map(updatedPerson), MediaType.APPLICATION_XML), WhoisResources.class);
            fail();
        } catch (BadRequestException e) {
            assertThat(e.getResponse().readEntity(String.class), containsString("Syntax error"));
            assertThat(mailSenderStub.anyMoreMessages(), is(false));
        }
    }

    @Test
    public void update_person_notifications_with_override() throws Exception {
        databaseHelper.insertUser(User.createWithPlainTextPassword("agoston", "zoh", ObjectType.PERSON));
        final RpslObject mntner = RpslObject.parse(
                "mntner:        TEST-MNT\n" +
                "descr:         Test maintainer\n" +
                "admin-c:       TP1-TEST\n" +
                "upd-to:        upd-to@ripe.net\n" +
                "mnt-nfy:       mnt-nfy@ripe.net\n" +
                "auth:          MD5-PW $1$EmukTVYX$Z6fWZT8EAzHoOJTQI6jFJ1  # 123\n" +
                "mnt-by:        TEST-MNT\n" +
                "source:        TEST"
        );
        databaseHelper.addObject(mntner);
        final RpslObject person = RpslObject.parse(
                "person:        Pauleth Palthen\n" +
                "address:       Singel 258\n" +
                "phone:         +31-1234567890\n" +
                "e-mail:        noreply@ripe.net\n" +
                "mnt-by:        TEST-MNT\n" +
                "nic-hdl:       PP1-TEST\n" +
                "remarks:       remark\n" +
                "source:        TEST\n"
        );
        databaseHelper.addObject(person);
        final RpslObject updatedPerson = new RpslObjectBuilder(person).append(new RpslAttribute(AttributeType.REMARKS, "updated")).get();

        RestTest.target(getPort(), "whois/test/person/PP1-TEST?override=agoston,zoh,reason")
                .request()
                .put(Entity.entity(map(updatedPerson), MediaType.APPLICATION_XML), WhoisResources.class);

        final String message = mailSenderStub.getMessage("mnt-nfy@ripe.net").getContent().toString();
        assertThat(message, containsString("Pauleth Palthen"));
        assertThat(mailSenderStub.anyMoreMessages(), is(false));
    }

    @Test
    public void update_person_disable_notifications_with_override() {
        databaseHelper.insertUser(User.createWithPlainTextPassword("agoston", "zoh", ObjectType.PERSON));
        final RpslObject mntner = RpslObject.parse(
                "mntner:        TEST-MNT\n" +
                "descr:         Test maintainer\n" +
                "admin-c:       TP1-TEST\n" +
                "upd-to:        upd-to@ripe.net\n" +
                "mnt-nfy:       mnt-nfy@ripe.net\n" +
                "auth:          MD5-PW $1$EmukTVYX$Z6fWZT8EAzHoOJTQI6jFJ1  # 123\n" +
                "mnt-by:        TEST-MNT\n" +
                "source:        TEST"
        );
        databaseHelper.addObject(mntner);
        final RpslObject person = RpslObject.parse(
                "person:        Pauleth Palthen\n" +
                "address:       Singel 258\n" +
                "phone:         +31-1234567890\n" +
                "e-mail:        noreply@ripe.net\n" +
                "mnt-by:        TEST-MNT\n" +
                "nic-hdl:       PP1-TEST\n" +
                "remarks:       remark\n" +
                "source:        TEST\n"
        );
        databaseHelper.addObject(person);
        final RpslObject updatedPerson = new RpslObjectBuilder(person).append(new RpslAttribute(AttributeType.REMARKS, "updated")).get();

        RestTest.target(getPort(), "whois/test/person/PP1-TEST")
                .queryParam("override", encode("agoston,zoh,reason {notify=false}"))
                .request()
                .put(Entity.entity(map(updatedPerson), MediaType.APPLICATION_XML), WhoisResources.class);

        assertThat(mailSenderStub.anyMoreMessages(), is(false));
    }

    @Test
    public void update_person_unformatted() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "person:  Pauleth Palthen\n" +
                "address: Singel 258\n" +
                "phone:   +31\n" +
                "         1234567890\n" +
                "e-mail:  noreply@ripe.net\n" +
                "mnt-by:  OWNER-MNT\n" +
                "nic-hdl: PP1-TEST\n" +
                "remarks: +----------+  #  +-----------+\n" +
                "         |  remark  |  #  |  comment  |\n" +
                "         +----------+  #  +-----------+\n" +
                "source:  TEST\n");

        databaseHelper.addObject(rpslObject);

        final RpslObject updatedObject = new RpslObjectBuilder(rpslObject).append(new RpslAttribute(AttributeType.FAX_NO, "+30 123")).get();

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person/PP1-TEST?password=test&unformatted")
                .request()
                .put(Entity.entity(mapDirty(updatedObject), MediaType.APPLICATION_XML), WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), hasSize(1));
        assertThat(whoisResources.getErrorMessages().getFirst().getText(), is("MD5 hashed password authentication is deprecated and support will be " +
                "removed at the end of 2025. Please switch to an alternative authentication method before then."));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        assertThat(whoisResources.getLink().getHref(), is(String.format("http://localhost:%s/test/person/PP1-TEST?unformatted", getPort())));

        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);

        assertThat(whoisObject.getAttributes().get(2).getValue(), is(
                "          " +
                "+31\n" +
                "                1234567890"));
        assertThat(whoisObject.getAttributes().get(6).getValue(), is(
                "        " +
                "+----------+  #  +-----------+\n" +
                "                " +
                "|  remark  |  #  |  comment  |\n" +
                "                " +
                "+----------+  #  +-----------+"));
        assertThat(whoisObject.getAttributes().get(9).getValue(), is(
                "         " +
                "+30 123"));

        final String queryResponse = queryTelnet("-r PP1-TEST");
        assertThat(queryResponse, containsString(
                "phone:          +31\n" +
                "                1234567890\n"));
        assertThat(queryResponse, containsString(
                "remarks:        +----------+  #  +-----------+\n" +
                "                |  remark  |  #  |  comment  |\n" +
                "                +----------+  #  +-----------+\n"));
    }

    @Test
    public void update_dryrun() {
        databaseHelper.addObject(PAULETH_PALTHEN);
        final RpslObject updatedObject = new RpslObjectBuilder(PAULETH_PALTHEN).addAttribute(4, new RpslAttribute(AttributeType.REMARKS, "this_is_another_remark")).get();

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person/PP1-TEST?password=test&dry-run")
                .request()
                .put(Entity.entity(mapDirty(updatedObject), MediaType.APPLICATION_XML), WhoisResources.class);

        final List<ErrorMessage> messages = whoisResources.getErrorMessages();
        assertThat(messages, hasSize(2));
        assertThat(messages.getFirst().getText(), is("MD5 hashed password authentication is deprecated and support will be " +
                "removed at the end of 2025. Please switch to an alternative authentication method before then."));
        assertThat(messages.get(1).getText(), is("Dry-run performed, no changes to the database have been made"));

        final String storedObject = RestTest.target(getPort(), "whois/test/person/PP1-TEST").request().get(String.class);
        assertThat(storedObject, not(containsString("this_is_another_remark")));
    }

    @Test
    public void use_override_to_skip_updating_last_modified() {
        databaseHelper.insertUser(User.createWithPlainTextPassword("dbint", "dbint", ObjectType.PERSON));

        final ZonedDateTime oldDateTime = testDateTimeProvider.getCurrentZonedDateTime();
        final ZonedDateTime newDateTime = oldDateTime.plusDays(10);
        testDateTimeProvider.setTime(oldDateTime.toLocalDateTime());

        final WhoisResources initialObject = RestTest.target(getPort(), "whois/test/person?password=test")
                .request()
                .post(Entity.entity(map(PAULETH_PALTHEN), MediaType.APPLICATION_XML), WhoisResources.class);

        assertThat(initialObject.getWhoisObjects().get(0).getAttributes(), hasItem(new Attribute("created", "2001-02-04T17:00:00Z")));
        assertThat(initialObject.getWhoisObjects().get(0).getAttributes(), hasItem(new Attribute("last-modified", "2001-02-04T17:00:00Z")));

        final RpslObject updatedObject = new RpslObjectBuilder(PAULETH_PALTHEN).addAttribute(4, new RpslAttribute(AttributeType.REMARKS, "this_is_another_remark")).get();

        testDateTimeProvider.setTime(newDateTime.toLocalDateTime());

        RestTest.target(getPort(), "whois/test/person/PP1-TEST")
                .queryParam("override", encode("dbint,dbint,{skip-last-modified=true}"))
                .request()
                .put(Entity.entity(mapDirty(updatedObject), MediaType.APPLICATION_XML), WhoisResources.class);

        final WhoisResources storedObject = RestTest.target(getPort(), "whois/test/person/PP1-TEST").request().get(WhoisResources.class);

        assertThat(storedObject.getWhoisObjects().get(0).getAttributes(), hasItem(new Attribute("last-modified", "2001-02-04T17:00:00Z")));
        assertThat(storedObject.getWhoisObjects().get(0).getAttributes(), not(hasItem(new Attribute("last-modified", "2001-02-14T17:00:00Z"))));
        assertThat(storedObject.getWhoisObjects().get(0).getAttributes(), hasItem(new Attribute("created", "2001-02-04T17:00:00Z")));
    }

    @Test
    public void use_override_explicit_not_skip_updating_last_modified() {
        databaseHelper.insertUser(User.createWithPlainTextPassword("dbint", "dbint", ObjectType.PERSON));

        final ZonedDateTime oldDateTime = testDateTimeProvider.getCurrentZonedDateTime();
        final ZonedDateTime newDateTime = oldDateTime.plusDays(10);
        testDateTimeProvider.setTime(oldDateTime.toLocalDateTime());

        final WhoisResources initialObject = RestTest.target(getPort(), "whois/test/person?password=test")
                .request()
                .post(Entity.entity(map(PAULETH_PALTHEN), MediaType.APPLICATION_XML), WhoisResources.class);

        assertThat(initialObject.getWhoisObjects().get(0).getAttributes(), hasItem(new Attribute("created", "2001-02-04T17:00:00Z")));
        assertThat(initialObject.getWhoisObjects().get(0).getAttributes(), hasItem(new Attribute("last-modified", "2001-02-04T17:00:00Z")));

        final RpslObject updatedObject = new RpslObjectBuilder(PAULETH_PALTHEN).addAttribute(4, new RpslAttribute(AttributeType.REMARKS, "this_is_another_remark")).get();

        testDateTimeProvider.setTime(newDateTime.toLocalDateTime());

        RestTest.target(getPort(), "whois/test/person/PP1-TEST")
                .queryParam("override", encode("dbint,dbint,{skip-last-modified=false}"))
                .request()
                .put(Entity.entity(mapDirty(updatedObject), MediaType.APPLICATION_XML), WhoisResources.class);

        final WhoisResources storedObject = RestTest.target(getPort(), "whois/test/person/PP1-TEST").request().get(WhoisResources.class);

        assertThat(storedObject.getWhoisObjects().get(0).getAttributes(), hasItem(new Attribute("last-modified", "2001-02-14T17:00:00Z")));
        assertThat(storedObject.getWhoisObjects().get(0).getAttributes(), not(hasItem(new Attribute("last-modified", "2001-02-04T17:00:00Z"))));
        assertThat(storedObject.getWhoisObjects().get(0).getAttributes(), hasItem(new Attribute("created", "2001-02-04T17:00:00Z")));
    }

    // response format

    @Test
    public void lookup_accept_application_xml() {
        final String response = RestTest.target(getPort(), "whois/test/person/TP1-TEST")
                .request(MediaType.APPLICATION_XML)
                .get(String.class);

        assertThat(response, containsString("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        assertThat(response, containsString("<whois-resources"));
    }

    @Test
    public void lookup_accept_application_json() {
        final String response = RestTest.target(getPort(), "whois/test/person/TP1-TEST")
                .request(MediaType.APPLICATION_JSON)
                .get(String.class);

        assertThat(response, containsString("\"objects\""));
        assertThat(response, containsString("\"object\""));
        assertThat(response, containsString("\"type\""));
        assertThat(response, containsString("\"href\""));
    }

    @Test
    public void lookup_json_extension() {
        final String response = RestTest.target(getPort(), "whois/test/person/TP1-TEST.json")
                .request()
                .get(String.class);
        assertThat(response, containsString("\"objects\""));
        assertThat(response, containsString("\"object\""));
        assertThat(response, containsString("\"type\""));
        assertThat(response, containsString("\"href\""));
    }

    @Test
    public void lookup_unfiltered_queryparameter() {
        databaseHelper.addObject(PAULETH_PALTHEN);

        final String response = RestTest.target(getPort(), "whois/test/person/PP1-TEST?unfiltered=").request().get(String.class);
        assertThat(response, containsString("attribute name=\"e-mail\" value=\"noreply@ripe.net\""));

        final String noEqualSign = RestTest.target(getPort(), "whois/test/person/PP1-TEST?unfiltered").request().get(String.class);
        assertThat(noEqualSign, containsString("attribute name=\"e-mail\" value=\"noreply@ripe.net\""));

        final String withOtherParameters = RestTest.target(getPort(), "whois/test/person/PP1-TEST?unfiltered=true&pretty=false").request().get(String.class);
        assertThat(withOtherParameters, containsString("attribute name=\"e-mail\" value=\"noreply@ripe.net\""));

        final String filtered = RestTest.target(getPort(), "whois/test/person/PP1-TEST?pretty=false").request().get(String.class);
        assertThat(filtered, not(containsString("attribute name=\"e-mail\" value=\"noreply@ripe.net\"")));
    }

    // maintenance mode

    // TODO: [AH] also test origin, i.e. maintenanceMode.set("NONE,READONLY")

    @Test
    public void maintenance_mode_readonly_update() {
        assertThrows(ServiceUnavailableException.class, () -> {
            maintenanceMode.set("READONLY,READONLY");
            RestTest.target(getPort(), "whois/test/person/PP1-TEST")
                    .request(MediaType.APPLICATION_XML)
                    .put(Entity.entity(map(PAULETH_PALTHEN), MediaType.APPLICATION_XML), String.class);
        });
    }

    @Test
    public void maintenance_mode_readonly_query() {
        maintenanceMode.set("READONLY,READONLY");
        WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person/TP1-TEST").request().get(WhoisResources.class);
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
    }

    @Test
    public void maintenance_mode_none_update() {
        assertThrows(ServiceUnavailableException.class, () -> {
            maintenanceMode.set("NONE,NONE");
            RestTest.target(getPort(), "whois/test/person/PP1-TEST")
                    .request(MediaType.APPLICATION_XML)
                    .put(Entity.entity(map(PAULETH_PALTHEN), MediaType.APPLICATION_XML), String.class);
        });
    }

    @Test
    public void maintenance_mode_none_query() {
        assertThrows(ServiceUnavailableException.class, () -> {
            maintenanceMode.set("NONE,NONE");
            RestTest.target(getPort(), "whois/test/person/TP1-TEST").request().get(WhoisResources.class);
        });
    }

    @Test
    public void primary_key_comment() {
        final RpslObject createPerson = RpslObject.parse("" +
                "person:    Pauleth Palthen\n" +
                "address:   Singel 258\n" +
                "phone:     +31-1234567890\n" +
                "e-mail:    noreply@ripe.net\n" +
                "mnt-by:    OWNER-MNT\n" +
                "nic-hdl:   PP1-TEST # create comment\n" +
                "remarks:   remark\n" +
                "source:    TEST\n");

        final WhoisResources createResponse = RestTest.target(getPort(), "whois/test/person?password=test")
                .request()
                .post(Entity.entity(map(createPerson), MediaType.APPLICATION_XML), WhoisResources.class);

        RestTest.assertInfoCount(createResponse, 1);
        RestTest.assertErrorMessage(createResponse, 1, "Info", "Please use the \"remarks:\" attribute instead of end " +
                "of line comment on primary key");
        assertThat(createResponse.getErrorMessages().get(1).getAttribute(), is(new Attribute("nic-hdl", "PP1-TEST # create comment")));

        final RpslObject updatePerson = RpslObject.parse("" +
                "person:    Pauleth Palthen # comment\n" +
                "address:   Singel 258\n" +
                "phone:     +31-1234567890\n" +
                "e-mail:    noreply@ripe.net\n" +
                "remarks:   updated\n" +
                "mnt-by:    OWNER-MNT\n" +
                "nic-hdl:   PP1-TEST   # update comment\n" +
                "remarks:   remark\n" +
                "source:    TEST\n");

        final WhoisResources updateResponse = RestTest.target(getPort(), "whois/test/person/PP1-TEST?password=test")
                .request()
                .put(Entity.entity(map(updatePerson), MediaType.APPLICATION_XML), WhoisResources.class);

        RestTest.assertInfoCount(updateResponse, 2);
        RestTest.assertErrorMessage(updateResponse, 1, "Info", "Please use the \"remarks:\" attribute instead of end of line comment on primary key");
        assertThat(updateResponse.getErrorMessages().get(1).getAttribute(), is(new Attribute("person", "Pauleth Palthen # comment")));
        RestTest.assertErrorMessage(updateResponse, 2, "Info", "Please use the \"remarks:\" attribute instead of end of line comment on primary key");
        assertThat(updateResponse.getErrorMessages().get(2).getAttribute(), is(new Attribute("nic-hdl", "PP1-TEST # update comment")));
    }
    @Test
    public void comment_separator_not_included_in_response() {
        databaseHelper.addObject(
                "person:    Pauleth Palthen\n" +
                "address:   Singel 258\n" +
                "phone:     +31-1234567890\n" +
                "e-mail:    noreply@ripe.net\n" +
                "mnt-by:    OWNER-MNT\n" +
                "nic-hdl:   PP1-TEST\n" +
                "remarks:   remark # comment\n" +
                "source:    TEST\n");

        final WhoisResources response = RestTest.target(getPort(), "whois/test/person/PP1-TEST.json?unfiltered")
                .request(MediaType.APPLICATION_JSON)
                .get(WhoisResources.class);

        final WhoisObject person = response.getWhoisObjects().get(0);
        assertThat(person.getAttributes().get(6).getName(), is("remarks"));
        assertThat(person.getAttributes().get(6).getValue(), is("remark"));
        assertThat(person.getAttributes().get(6).getComment(), is("comment"));
    }

    @Test
    public void multiple_comment_separators_not_included_in_response() {
        databaseHelper.addObject(
                "person:    Pauleth Palthen\n" +
                "address:   Singel 258\n" +
                "phone:     +31-1234567890\n" +
                "e-mail:    noreply@ripe.net\n" +
                "mnt-by:    OWNER-MNT\n" +
                "nic-hdl:   PP1-TEST\n" +
                "remarks:   remark # # comment\n" +
                "source:    TEST\n");

        final WhoisResources response = RestTest.target(getPort(), "whois/test/person/PP1-TEST.json?unfiltered")
                .request(MediaType.APPLICATION_JSON)
                .get(WhoisResources.class);

        final WhoisObject person = response.getWhoisObjects().get(0);
        assertThat(person.getAttributes().get(6).getName(), is("remarks"));
        assertThat(person.getAttributes().get(6).getValue(), is("remark"));
        assertThat(person.getAttributes().get(6).getComment(), is("comment"));
    }

    @Test
    public void lookup_route_out_of_region_redirect() {
        databaseHelper.deleteAuthoritativeResource("test", "0.0.0.0/0");
        databaseHelper.addObject(
                "route:           192.168.0.0/24\n" +
                        "descr:           Test route\n" +
                        "origin:          AS12726\n" +
                        "mnt-by:          OWNER-MNT\n" +
                        "mnt-routes:      OWNER-MNT {192.168.0.0/16}\n" +
                        "source:          TEST-NONAUTH\n");
        ipTreeUpdater.rebuild();


        final Response response = RestTest.target(getPort(), "whois/test/route/192.168.0.0/24AS12726")
                .property(ClientProperties.FOLLOW_REDIRECTS, Boolean.FALSE)
                .request().get();

        assertThat(response.getStatus(), is(Response.Status.MOVED_PERMANENTLY.getStatusCode()));
        assertThat(response.getHeaderString("Location").toLowerCase(), endsWith("test-nonauth/route/192.168.0.0/24AS12726".toLowerCase()));
        databaseHelper.addAuthoritativeResource("test", "0.0.0.0/0");
    }

    @Test
    public void lookup_route6_out_of_region_redirect() {
        databaseHelper.deleteAuthoritativeResource("test", "::/0");

        databaseHelper.addObject(
                "route6:           2a01:400::/22\n" +
                        "descr:           Test route\n" +
                        "origin:          AS12726\n" +
                        "mnt-by:          OWNER-MNT\n" +
                        "mnt-routes:      OWNER-MNT {192.168.0.0/16}\n" +
                        "source:          TEST-NONAUTH\n");
        ipTreeUpdater.rebuild();

        final Response response = RestTest.target(getPort(), "whois/test/route6/2a01:400::/22AS12726")
                .property(ClientProperties.FOLLOW_REDIRECTS, Boolean.FALSE)
                .request().get();

        assertThat(response.getStatus(), is(Response.Status.MOVED_PERMANENTLY.getStatusCode()));
        assertThat(response.getHeaderString("Location").toLowerCase(), endsWith("test-nonauth/route6/2a01:400::/22AS12726".toLowerCase()));

        databaseHelper.addAuthoritativeResource("test", "::/0");

    }

    @Test
    public void delete_route_out_of_region_redirect() {
        databaseHelper.deleteAuthoritativeResource("test", "0.0.0.0/0");
        databaseHelper.addObject(
                "route:           192.168.0.0/24\n" +
                        "descr:           Test route\n" +
                        "origin:          AS12726\n" +
                        "mnt-by:          OWNER-MNT\n" +
                        "mnt-routes:      OWNER-MNT {192.168.0.0/16}\n" +
                        "source:          TEST-NONAUTH\n");
        ipTreeUpdater.rebuild();


        final Response response = RestTest.target(getPort(), "whois/test/route/192.168.0.0/24AS12726")
                .property(ClientProperties.FOLLOW_REDIRECTS, Boolean.FALSE)
                .request().delete();

        assertThat(response.getStatus(), is(Response.Status.MOVED_PERMANENTLY.getStatusCode()));
        assertThat(response.getHeaderString("Location").toLowerCase(), endsWith("test-nonauth/route/192.168.0.0/24AS12726".toLowerCase()));
        databaseHelper.addAuthoritativeResource("test", "0.0.0.0/0");
    }

    @Test
    public void delete_domain_trailing_dot_nserver() {
        databaseHelper.addObject(
                "domain:        193.in-addr.arpa4\n" +
                        "nserver:         test.ns.\n" +
                        "mnt-by:          OWNER-MNT\n" +
                        "source:          TEST\n");

        final Response response = RestTest.target(getPort(), "whois/test/domain/193.in-addr.arpa4?password=test")
                .request().delete();
        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
    }

    @Test
    public void delete_route6_out_of_region_redirect() {
        databaseHelper.deleteAuthoritativeResource("test", "::/0");

        databaseHelper.addObject(
                "route6:           2a01:400::/22\n" +
                        "descr:           Test route\n" +
                        "origin:          AS12726\n" +
                        "mnt-by:          OWNER-MNT\n" +
                        "mnt-routes:      OWNER-MNT {192.168.0.0/16}\n" +
                        "source:          TEST-NONAUTH\n");
        ipTreeUpdater.rebuild();

        final Response response = RestTest.target(getPort(), "whois/test/route6/2a01:400::/22AS12726")
                .property(ClientProperties.FOLLOW_REDIRECTS, Boolean.FALSE)
                .request().delete();

        assertThat(response.getStatus(), is(Response.Status.MOVED_PERMANENTLY.getStatusCode()));
        assertThat(response.getHeaderString("Location").toLowerCase(), endsWith("test-nonauth/route6/2a01:400::/22AS12726".toLowerCase()));

        databaseHelper.addAuthoritativeResource("test", "::/0");

    }

    @Test
    public void update_route_out_of_region_redirect() {
        databaseHelper.deleteAuthoritativeResource("test", "0.0.0.0/0");
        final RpslObject rpslObject = databaseHelper.addObject(
                "route:           192.168.0.0/24\n" +
                        "descr:           Test route\n" +
                        "origin:          AS12726\n" +
                        "mnt-by:          OWNER-MNT\n" +
                        "mnt-routes:      OWNER-MNT {192.168.0.0/16}\n" +
                        "source:          TEST-NONAUTH\n");
        ipTreeUpdater.rebuild();


        final Response response = RestTest.target(getPort(), "whois/test/route/192.168.0.0/24AS12726")
                .property(ClientProperties.FOLLOW_REDIRECTS, Boolean.FALSE)
                .request()
                .put(Entity.entity(map(rpslObject), MediaType.APPLICATION_XML));

        assertThat(response.getStatus(), is(Response.Status.MOVED_PERMANENTLY.getStatusCode()));
        assertThat(response.getHeaderString("Location").toLowerCase(), endsWith("test-nonauth/route/192.168.0.0/24AS12726".toLowerCase()));
        databaseHelper.addAuthoritativeResource("test", "0.0.0.0/0");
    }

    @Test
    public void update_route6_out_of_region_redirect() {
        databaseHelper.deleteAuthoritativeResource("test", "::/0");

        final RpslObject rpslObject = databaseHelper.addObject(
                "route6:           2a01:400::/22\n" +
                        "descr:           Test route\n" +
                        "origin:          AS12726\n" +
                        "mnt-by:          OWNER-MNT\n" +
                        "mnt-routes:      OWNER-MNT {192.168.0.0/16}\n" +
                        "source:          TEST-NONAUTH\n");
        ipTreeUpdater.rebuild();

        final Response response = RestTest.target(getPort(), "whois/test/route6/2a01:400::/22AS12726")
                .property(ClientProperties.FOLLOW_REDIRECTS, Boolean.FALSE)
                .request()
                .put(Entity.entity(map(rpslObject), MediaType.APPLICATION_XML));

        assertThat(response.getStatus(), is(Response.Status.MOVED_PERMANENTLY.getStatusCode()));
        assertThat(response.getHeaderString("Location").toLowerCase(), endsWith("test-nonauth/route6/2a01:400::/22AS12726".toLowerCase()));

        databaseHelper.addAuthoritativeResource("test", "::/0");

    }

    @Test
    public void lookup_route_out_of_region_no_redirect() {
        databaseHelper.deleteAuthoritativeResource("test", "0.0.0.0/0");
        databaseHelper.addObject(
                "route:           192.168.0.0/24\n" +
                        "descr:           Test route\n" +
                        "origin:          AS12726\n" +
                        "mnt-by:          OWNER-MNT\n" +
                        "mnt-routes:      OWNER-MNT {192.168.0.0/16}\n" +
                        "source:          TEST-NONAUTH\n");
        ipTreeUpdater.rebuild();


        final Response response = RestTest.target(getPort(), "whois/test-nonauth/route/192.168.0.0/24AS12726")
                .property(ClientProperties.FOLLOW_REDIRECTS, Boolean.FALSE)
                .request().get();

        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
        databaseHelper.addAuthoritativeResource("test", "0.0.0.0/0");
    }

    @Test
    public void lookup_route6_out_of_region_no_redirect() {
        databaseHelper.deleteAuthoritativeResource("test", "::/0");

        databaseHelper.addObject(
                "route6:           2a01:400::/22\n" +
                        "descr:           Test route\n" +
                        "origin:          AS12726\n" +
                        "mnt-by:          OWNER-MNT\n" +
                        "mnt-routes:      OWNER-MNT {192.168.0.0/16}\n" +
                        "source:          TEST-NONAUTH\n");
        ipTreeUpdater.rebuild();

        final Response response = RestTest.target(getPort(), "whois/test-nonauth/route6/2a01:400::/22AS12726")
                .property(ClientProperties.FOLLOW_REDIRECTS, Boolean.FALSE)
                .request().get();

        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));

        databaseHelper.addAuthoritativeResource("test", "::/0");

    }

    @Test
    public void delete_route_out_of_region_no_redirect() {
        databaseHelper.deleteAuthoritativeResource("test", "0.0.0.0/0");
        databaseHelper.addObject(
                "route:           192.168.0.0/24\n" +
                        "descr:           Test route\n" +
                        "origin:          AS12726\n" +
                        "mnt-by:          OWNER-MNT\n" +
                        "mnt-routes:      OWNER-MNT {192.168.0.0/16}\n" +
                        "source:          TEST-NONAUTH\n");
        ipTreeUpdater.rebuild();


        final Response response = RestTest.target(getPort(), "whois/test-nonauth/route/192.168.0.0/24AS12726")
                .property(ClientProperties.FOLLOW_REDIRECTS, Boolean.FALSE)
                .queryParam("password", "test")
                .request().delete();

        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
        databaseHelper.addAuthoritativeResource("test", "0.0.0.0/0");
    }

    @Test
    public void delete_route6_out_of_region_no_redirect() {
        databaseHelper.deleteAuthoritativeResource("test", "::/0");

        databaseHelper.addObject(
                "route6:           2a01:400::/22\n" +
                        "descr:           Test route\n" +
                        "origin:          AS12726\n" +
                        "mnt-by:          OWNER-MNT\n" +
                        "mnt-routes:      OWNER-MNT \n" +
                        "source:          TEST-NONAUTH\n");
        ipTreeUpdater.rebuild();

        final Response response = RestTest.target(getPort(), "whois/test-nonauth/route6/2a01:400::/22AS12726")
                .property(ClientProperties.FOLLOW_REDIRECTS, Boolean.FALSE)
                .queryParam("password", "test")
                .request()
                .delete();

        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));

        databaseHelper.addAuthoritativeResource("test", "::/0");

    }

    @Test
    public void update_route_out_of_region_no_redirect() {
        databaseHelper.deleteAuthoritativeResource("test", "0.0.0.0/0");
        final RpslObject rpslObject = databaseHelper.addObject(
                "route:           192.168.0.0/24\n" +
                        "descr:           Test route\n" +
                        "origin:          AS12726\n" +
                        "mnt-by:          OWNER-MNT\n" +
                        "mnt-routes:      OWNER-MNT {192.168.0.0/16}\n" +
                        "source:          TEST-NONAUTH\n");
        ipTreeUpdater.rebuild();


        final Response response = RestTest.target(getPort(), "whois/test-nonauth/route/192.168.0.0/24AS12726")
                .property(ClientProperties.FOLLOW_REDIRECTS, Boolean.FALSE)
                .queryParam("password", "test")
                .request()
                .put(Entity.entity(map(rpslObject), MediaType.APPLICATION_XML));

        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
        databaseHelper.addAuthoritativeResource("test", "0.0.0.0/0");
    }

    @Test
    public void update_route6_out_of_region_no_redirect() {
        databaseHelper.deleteAuthoritativeResource("test", "::/0");

        final RpslObject rpslObject = databaseHelper.addObject(
                "route6:           2a01:400::/22\n" +
                        "descr:           Test route\n" +
                        "origin:          AS12726\n" +
                        "mnt-by:          OWNER-MNT\n" +
                        "mnt-routes:      OWNER-MNT \n" +
                        "source:          TEST-NONAUTH\n");
        ipTreeUpdater.rebuild();

        final Response response = RestTest.target(getPort(), "whois/test-nonauth/route6/2a01:400::/22AS12726")
                .property(ClientProperties.FOLLOW_REDIRECTS, Boolean.FALSE)
                .queryParam("password", "test")
                .request()
                .put(Entity.entity(map(rpslObject), MediaType.APPLICATION_XML));

        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));

        databaseHelper.addAuthoritativeResource("test", "::/0");

    }


    @Test
    public void lookup_route_in_region_redirect() {
        databaseHelper.addObject(
                "route:           192.168.0.0/24\n" +
                        "descr:           Test route\n" +
                        "origin:          AS12726\n" +
                        "mnt-by:          OWNER-MNT\n" +
                        "mnt-routes:      OWNER-MNT {192.168.0.0/16}\n" +
                        "source:          TEST\n");
        ipTreeUpdater.rebuild();


        final Response response = RestTest.target(getPort(), "whois/test-nonauth/route/192.168.0.0/24AS12726")
                .property(ClientProperties.FOLLOW_REDIRECTS, Boolean.FALSE)
                .request().get();

        assertThat(response.getStatus(), is(Response.Status.MOVED_PERMANENTLY.getStatusCode()));
        assertThat(response.getHeaderString("Location").toLowerCase(), endsWith("test/route/192.168.0.0/24AS12726".toLowerCase()));
    }

    @Test
    public void lookup_route6_in_region_redirect() {
        databaseHelper.addObject(
                "route6:           2a01:400::/22\n" +
                        "descr:           Test route\n" +
                        "origin:          AS12726\n" +
                        "mnt-by:          OWNER-MNT\n" +
                        "mnt-routes:      OWNER-MNT {192.168.0.0/16}\n" +
                        "source:          TEST\n");
        ipTreeUpdater.rebuild();

        final Response response = RestTest.target(getPort(), "whois/test-nonauth/route6/2a01:400::/22AS12726")
                .property(ClientProperties.FOLLOW_REDIRECTS, Boolean.FALSE)
                .request().get();

        assertThat(response.getStatus(), is(Response.Status.MOVED_PERMANENTLY.getStatusCode()));
        assertThat(response.getHeaderString("Location").toLowerCase(), endsWith("test/route6/2a01:400::/22AS12726".toLowerCase()));

    }

    @Test
    public void delete_route_in_region_redirect() {
        databaseHelper.addObject(
                "route:           192.168.0.0/24\n" +
                        "descr:           Test route\n" +
                        "origin:          AS12726\n" +
                        "mnt-by:          OWNER-MNT\n" +
                        "mnt-routes:      OWNER-MNT {192.168.0.0/16}\n" +
                        "source:          TEST\n");
        ipTreeUpdater.rebuild();


        final Response response = RestTest.target(getPort(), "whois/test-nonauth/route/192.168.0.0/24AS12726")
                .property(ClientProperties.FOLLOW_REDIRECTS, Boolean.FALSE)
                .request().delete();

        assertThat(response.getStatus(), is(Response.Status.MOVED_PERMANENTLY.getStatusCode()));
        assertThat(response.getHeaderString("Location").toLowerCase(), endsWith("test/route/192.168.0.0/24AS12726".toLowerCase()));
    }

    @Test
    public void delete_route6_in_region_redirect() {
        databaseHelper.addObject(
                "route6:           2a01:400::/22\n" +
                        "descr:           Test route\n" +
                        "origin:          AS12726\n" +
                        "mnt-by:          OWNER-MNT\n" +
                        "mnt-routes:      OWNER-MNT {192.168.0.0/16}\n" +
                        "source:          TEST\n");
        ipTreeUpdater.rebuild();

        final Response response = RestTest.target(getPort(), "whois/test-nonauth/route6/2a01:400::/22AS12726")
                .property(ClientProperties.FOLLOW_REDIRECTS, Boolean.FALSE)
                .request().delete();

        assertThat(response.getStatus(), is(Response.Status.MOVED_PERMANENTLY.getStatusCode()));
        assertThat(response.getHeaderString("Location").toLowerCase(), endsWith("test/route6/2a01:400::/22AS12726".toLowerCase()));

    }

    @Test
    public void update_route_in_region_redirect() {
        final RpslObject rpslObject = databaseHelper.addObject(
                "route:           192.168.0.0/24\n" +
                        "descr:           Test route\n" +
                        "origin:          AS12726\n" +
                        "mnt-by:          OWNER-MNT\n" +
                        "mnt-routes:      OWNER-MNT {192.168.0.0/16}\n" +
                        "source:          TEST\n");
        ipTreeUpdater.rebuild();


        final Response response = RestTest.target(getPort(), "whois/test-nonauth/route/192.168.0.0/24AS12726")
                .property(ClientProperties.FOLLOW_REDIRECTS, Boolean.FALSE)
                .request()
                .put(Entity.entity(map(rpslObject), MediaType.APPLICATION_XML));

        assertThat(response.getStatus(), is(Response.Status.MOVED_PERMANENTLY.getStatusCode()));
        assertThat(response.getHeaderString("Location").toLowerCase(), endsWith("test/route/192.168.0.0/24AS12726".toLowerCase()));
    }

    @Test
    public void update_route6_in_region_redirect() {
        final RpslObject rpslObject = databaseHelper.addObject(
                "route6:           2a01:400::/22\n" +
                        "descr:           Test route\n" +
                        "origin:          AS12726\n" +
                        "mnt-by:          OWNER-MNT\n" +
                        "mnt-routes:      OWNER-MNT {192.168.0.0/16}\n" +
                        "source:          TEST\n");
        ipTreeUpdater.rebuild();

        final Response response = RestTest.target(getPort(), "whois/test-nonauth/route6/2a01:400::/22AS12726")
                .property(ClientProperties.FOLLOW_REDIRECTS, Boolean.FALSE)
                .request()
                .put(Entity.entity(map(rpslObject), MediaType.APPLICATION_XML));

        assertThat(response.getStatus(), is(Response.Status.MOVED_PERMANENTLY.getStatusCode()));
        assertThat(response.getHeaderString("Location").toLowerCase(), endsWith("test/route6/2a01:400::/22AS12726".toLowerCase()));

    }

    @Test
    public void lookup_route_in_region_no_redirect() {
        databaseHelper.addObject(
                "route:           192.168.0.0/24\n" +
                        "descr:           Test route\n" +
                        "origin:          AS12726\n" +
                        "mnt-by:          OWNER-MNT\n" +
                        "mnt-routes:      OWNER-MNT {192.168.0.0/16}\n" +
                        "source:          TEST\n");
        ipTreeUpdater.rebuild();


        final Response response = RestTest.target(getPort(), "whois/test/route/192.168.0.0/24AS12726")
                .property(ClientProperties.FOLLOW_REDIRECTS, Boolean.FALSE)
                .request().get();

        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
    }

    @Test
    public void lookup_route6_in_region_no_redirect() {
        databaseHelper.addObject(
                "route6:           2a01:400::/22\n" +
                        "descr:           Test route\n" +
                        "origin:          AS12726\n" +
                        "mnt-by:          OWNER-MNT\n" +
                        "mnt-routes:      OWNER-MNT {192.168.0.0/16}\n" +
                        "source:          TEST\n");
        ipTreeUpdater.rebuild();

        final Response response = RestTest.target(getPort(), "whois/test/route6/2a01:400::/22AS12726")
                .property(ClientProperties.FOLLOW_REDIRECTS, Boolean.FALSE)
                .request().get();

        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));

    }

    @Test
    public void delete_route_in_region_no_redirect() {
        databaseHelper.addObject(
                "route:           192.168.0.0/24\n" +
                        "descr:           Test route\n" +
                        "origin:          AS12726\n" +
                        "mnt-by:          OWNER-MNT\n" +
                        "mnt-routes:      OWNER-MNT {192.168.0.0/16}\n" +
                        "source:          TEST\n");
        ipTreeUpdater.rebuild();


        final Response response = RestTest.target(getPort(), "whois/test/route/192.168.0.0/24AS12726")
                .property(ClientProperties.FOLLOW_REDIRECTS, Boolean.FALSE)
                .queryParam("password", "test")
                .request().delete();

        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
    }

    @Test
    public void delete_route6_in_region_no_redirect() {

        databaseHelper.addObject(
                "route6:           2a01:400::/22\n" +
                        "descr:           Test route\n" +
                        "origin:          AS12726\n" +
                        "mnt-by:          OWNER-MNT\n" +
                        "mnt-routes:      OWNER-MNT \n" +
                        "source:          TEST\n");
        ipTreeUpdater.rebuild();

        final Response response = RestTest.target(getPort(), "whois/test/route6/2a01:400::/22AS12726")
                .property(ClientProperties.FOLLOW_REDIRECTS, Boolean.FALSE)
                .queryParam("password", "test")
                .request()
                .delete();

        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));

    }

    @Test
    public void update_route_in_region_no_redirect() {
        final RpslObject rpslObject = databaseHelper.addObject(
                "route:           192.168.0.0/24\n" +
                        "descr:           Test route\n" +
                        "origin:          AS12726\n" +
                        "mnt-by:          OWNER-MNT\n" +
                        "mnt-routes:      OWNER-MNT {192.168.0.0/16}\n" +
                        "source:          TEST\n");
        ipTreeUpdater.rebuild();


        final Response response = RestTest.target(getPort(), "whois/test/route/192.168.0.0/24AS12726")
                .property(ClientProperties.FOLLOW_REDIRECTS, Boolean.FALSE)
                .queryParam("password", "test")
                .request()
                .put(Entity.entity(map(rpslObject), MediaType.APPLICATION_XML));

        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
    }

    @Test
    public void update_route6_in_region_no_redirect() {

        final RpslObject rpslObject = databaseHelper.addObject(
                "route6:           2a01:400::/22\n" +
                        "descr:           Test route\n" +
                        "origin:          AS12726\n" +
                        "mnt-by:          OWNER-MNT\n" +
                        "mnt-routes:      OWNER-MNT \n" +
                        "source:          TEST\n");
        ipTreeUpdater.rebuild();

        final Response response = RestTest.target(getPort(), "whois/test/route6/2a01:400::/22AS12726")
                .property(ClientProperties.FOLLOW_REDIRECTS, Boolean.FALSE)
                .queryParam("password", "test")
                .request()
                .put(Entity.entity(map(rpslObject), MediaType.APPLICATION_XML));

        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));

    }

    @Test
    public void unsubscribed_notify_user_gets_warn_when_updating() {
        databaseHelper.addObject(NOTIFY_PERSON);
        final String unsubscribedEmail = "test@ripe.net";

        final RpslObject rpslObject = RpslObject.parse("" +
                "person:    Pauleth Palthen \n" +
                        "address:   Singel 258 test\n" +
                        "phone:     +31-1234567890\n" +
                        "e-mail:    noreply@ripe.net\n" +
                        "notify:    test@ripe.net\n" +
                        "mnt-by:    OWNER-MNT\n" +
                        "nic-hdl:   PP3-TEST\n" +
                        "remarks:   remark\n" +
                        "source:    TEST\n");

        emailStatusDao.createEmailStatus(unsubscribedEmail, EmailStatusType.UNSUBSCRIBE);

        final WhoisResources response = RestTest.target(getPort(), "whois/test/person/PP3-TEST?password=test")
                .request(MediaType.APPLICATION_XML)
                .put(Entity.entity(map(rpslObject), MediaType.APPLICATION_XML), WhoisResources.class);

        RestTest.assertWarningCount(response, 2);
        RestTest.assertErrorMessage(response, 1, "Warning", "Not sending notification to %s because it is %s.",
                unsubscribedEmail, EmailStatusType.UNSUBSCRIBE.getValue());
    }


    @Test
    public void undeliverable_notify_user_gets_warn_when_updating() {
        databaseHelper.addObject(NOTIFY_PERSON);
        final String undeliverableEmail = "test@ripe.net";

        final RpslObject rpslObject = RpslObject.parse("" +
                "person:    Pauleth Palthen \n" +
                "address:   Singel 258 test\n" +
                "phone:     +31-1234567890\n" +
                "e-mail:    noreply@ripe.net\n" +
                "notify:    test@ripe.net\n" +
                "mnt-by:    OWNER-MNT\n" +
                "nic-hdl:   PP3-TEST\n" +
                "remarks:   remark\n" +
                "source:    TEST\n");

        emailStatusDao.createEmailStatus(undeliverableEmail, EmailStatusType.UNDELIVERABLE);

        final WhoisResources response = RestTest.target(getPort(), "whois/test/person/PP3-TEST?password=test")
                .request(MediaType.APPLICATION_XML)
                .put(Entity.entity(map(rpslObject), MediaType.APPLICATION_XML), WhoisResources.class);

        RestTest.assertWarningCount(response, 2);
        RestTest.assertErrorMessage(response, 1, "Warning", "Not sending notification to %s because it is %s.",
                undeliverableEmail, EmailStatusType.UNDELIVERABLE.getValue());
    }


    @Test
    public void create_too_big_address_then_error() {
        final RpslObject PAULETH_PALTHEN_LONG_EMAIL = RpslObject.parse("" +
                "person:    Pauleth Palthen\n" +
                "address:   Singel 258\n" +
                "phone:     +31-1234567890\n" +
                "e-mail:    G=noreply/S=noreply/O=noreplynoreplynoreplnoreplynoreplynoreplnoreplynoreplynorepl" +
                    "noreplynoreplynoreplnoreplynoreplynoreplnoreplynoreplynoreplnoreplynoreplynoreplnoreply" +
                    "noreplynoreplnoreplynoreplynoreplnoreplynoreplynoreplnoreplynoreplynoreplnoreplynoreply" +
                    "noreplnoreplynoreplynoreplnoreplynoreplynoreplnoreplynoreplynorepl/P=AA/A=ripe.net/C=SP/" +
                    "@noreply.ripe.net\n" +
                "mnt-by:    OWNER-MNT\n" +
                "nic-hdl:   PP1-TEST\n" +
                "remarks:   remark\n" +
                "source:    TEST\n");

        final BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> {
            RestTest.target(getPort(), "whois/test/person?password=test")
                    .request()
                    .post(Entity.entity(map(PAULETH_PALTHEN_LONG_EMAIL), MediaType.APPLICATION_JSON_TYPE), WhoisResources.class);
        });

        assertThat(badRequestException.getMessage(), is("HTTP 400 Bad Request"));
        final WhoisResources whoisResources = RestTest.mapClientException(badRequestException);
        RestTest.assertErrorCount(whoisResources, 1);
        RestTest.assertErrorMessage(whoisResources, 0, "Error", "Syntax error in %s",
                    "G=noreply/S=noreply/O=noreplynoreplynoreplnoreplynoreplynoreplnoreplynoreplynorepl" +
                    "noreplynoreplynoreplnoreplynoreplynoreplnoreplynoreplynoreplnoreplynoreplynoreplnoreply" +
                    "noreplynoreplnoreplynoreplynoreplnoreplynoreplynoreplnoreplynoreplynoreplnoreplynoreply" +
                    "noreplnoreplynoreplynoreplnoreplynoreplynoreplnoreplynoreplynorepl/P=AA/A=ripe.net/C=SP/" +
                    "@noreply.ripe.net");
    }

    @Test
    public void mp_memebers_should_have_referenced_link_when_as_set() {
        databaseHelper.addObject(
                """
                route-set:    AS7775535:RS-CUSTOMERS:AS94967295
                descr:        test route-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       OWNER-MNT
                mnt-lower:    OWNER-MNT
                source:  TEST
                """
        );

        databaseHelper.addObject(
                """
                route-set:    AS1234:RS-CUSTOMERS:AS1234
                descr:        test route-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       OWNER-MNT
                mnt-lower:    OWNER-MNT
                source:  TEST
                """
        );


        databaseHelper.addObject(RpslObject.parse(
                """
                route-set:    RS-CUSTOMERS
                descr:        test route-set
                members:      AS7775535:RS-CUSTOMERS:AS94967295
                mp-members:   AS1234:RS-CUSTOMERS:AS1234
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       OWNER-MNT
                mnt-lower:    OWNER-MNT
                source:  TEST
                """));

        final WhoisResources response = RestTest.target(getPort(), "whois/test/route-set/RS-CUSTOMERS?password=test")
                    .request()
                    .get(WhoisResources.class);

        final List<Attribute> memberAttributes = response.getWhoisObjects().getFirst()
                .getAttributes().stream()
                .filter(attribute -> attribute.getName().equals("members") || attribute.getName().equals("mp-members"))
                .toList();

        assertThat(memberAttributes.size(), is(2));

        assertAsSetMember(memberAttributes.get(0), "members");
        assertAsSetMember(memberAttributes.get(1), "mp-members");

    }



    // helper methods

    private static void assertAsSetMember(final Attribute member, final String attributeExpectedType) {
        assertThat(member.getName(), is(attributeExpectedType));
        assertThat(member.getReferencedType(), is("route-set"));
        assertThat(member.getLink().getHref(), is("http://rest-test.db.ripe.net/test/route-set/" + member.getValue()));
    }

    private WhoisResources map(final RpslObject ... rpslObjects) {
        return whoisObjectMapper.mapRpslObjects(FormattedClientAttributeMapper.class, rpslObjects);
    }

    private WhoisResources mapDirty(final RpslObject ... rpslObjects) {
        return whoisObjectMapper.mapRpslObjects(DirtyClientAttributeMapper.class, rpslObjects);
    }

    private RpslObject map(final WhoisObject whoisObject) {
        return whoisObjectMapper.map(whoisObject, FormattedClientAttributeMapper.class);
    }

    private String queryTelnet(final String query) {
        return TelnetWhoisClient.queryLocalhost(queryServer.getPort(), query);
    }

    private static String gunzip(final byte[] bytes) {
        try {
            return new String(
                    ByteStreams.toByteArray(
                            new GZIPInputStream(
                                    new ByteArrayInputStream(bytes))));
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static String gunzip(final File file) {
        try {
            return gunzip(
                    ByteStreams.toByteArray(new FileInputStream(file)));
        } catch (IOException e) {
            throw new IllegalArgumentException(e);

        }
    }

}
