package net.ripe.db.whois.api.whois;

import com.sun.jersey.api.client.WebResource;
import net.ripe.db.whois.api.AbstractRestClientTest;
import net.ripe.db.whois.api.httpserver.Audience;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.domain.IpRanges;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.mail.MailSenderStub;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.ws.rs.core.MediaType;
import java.net.HttpURLConnection;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SyncUpdatesServiceTestIntegration extends AbstractRestClientTest {

    private static final Audience AUDIENCE = Audience.PUBLIC;

    private static final String MNTNER_TEST_MNTNER = "" +
            "mntner: mntner\n" +
            "descr: description\n" +
            "admin-c: ANY1-TEST\n" +
            "upd-to:noreply@ripe.net\n" +
            "notify: noreply@ripe.net\n" +
            "auth: MD5-PW $1$TTjmcwVq$zvT9UcvASZDQJeK8u9sNU.\n" +
            "mnt-by: mntner\n" +
            "referral-by: mntner\n" +
            "changed: noreply@ripe.net 20120801\n" +
            "source: TEST";

    private static final String PERSON_ANY1_TEST = "" +
            "person: test\n" +
            "nic-hdl: ANY1-TEST\n" +
            "source: TEST";

    @Autowired private MailSenderStub mailSender;
    @Autowired private IpRanges ipRanges;

    @Test
    public void empty_request() throws Exception {
        doGetRequest(getUrl("test", ""), HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    public void get_help_parameter_only() throws Exception {
        String response = doGetRequest(getUrl("test", "HELP=yes"), HttpURLConnection.HTTP_OK);

        assertThat(response, containsString("You have requested Help information from the RIPE NCC Database"));
        assertThat(response, containsString("From-Host: 127.0.0.1"));
        assertThat(response, containsString("Date/Time: "));
        assertThat(response, not(containsString("$")));
    }

    @Test
    public void post_multipart_form_help_parameter_only() {
        String response = createResource(AUDIENCE, "whois/syncupdates/test")
                    .entity("HELP=help", MediaType.MULTIPART_FORM_DATA)
                    .post(String.class);

        assertThat(response, containsString("You have requested Help information from the RIPE NCC Database"));
    }

    @Test
    public void post_url_encoded_form_help_parameter_only() {
        String response = createResource(AUDIENCE, "whois/syncupdates/test")
                    .entity("HELP=yes", MediaType.APPLICATION_FORM_URLENCODED)
                    .post(String.class);

        assertThat(response, containsString("You have requested Help information from the RIPE NCC Database"));
    }

    @Test
    public void help_and_invalid_parameter() throws Exception {
        String response = doGetRequest(getUrl("test", "HELP=yes&INVALID=true"), HttpURLConnection.HTTP_OK);

        assertThat(response, containsString("You have requested Help information from the RIPE NCC Database"));
    }

    @Test
    public void diff_parameter_only() throws Exception {
        String response = doGetRequest(getUrl("test", "DIFF=yes"), HttpURLConnection.HTTP_BAD_REQUEST);

        assertThat(response, is("the DIFF method is not actually supported by the Syncupdates interface\n"));
    }

    @Test
    public void redirect_not_allowed() throws Exception {
        ipRanges.setRipeRanges();
        rpslObjectUpdateDao.createObject(RpslObject.parse(PERSON_ANY1_TEST));

        String response = doGetRequest(getUrl("test", "REDIRECT=yes&DATA=" + encode(MNTNER_TEST_MNTNER + "\npassword: emptypassword")), HttpURLConnection.HTTP_FORBIDDEN);

        assertThat(response, not(containsString("Create SUCCEEDED: [mntner] mntner")));
        assertThat(response, containsString("Not allowed to disable notifications: 127.0.0.1\n"));
    }

    @Test
    public void redirect_allowed() throws Exception {
        ipRanges.setRipeRanges("0/0", "::0/0");
        rpslObjectUpdateDao.createObject(RpslObject.parse(PERSON_ANY1_TEST));

        String response = doGetRequest(getUrl("test", "REDIRECT=yes&DATA=" + encode(MNTNER_TEST_MNTNER + "\nremarks: updated" + "\npassword: emptypassword")), HttpURLConnection.HTTP_OK);

        assertThat(response, containsString("Create SUCCEEDED: [mntner] mntner"));
        assertThat(response, not(containsString("Not allowed to disable notifications: 127.0.0.1\n")));
        assertFalse(anyMoreMessages());
    }

    @Test
    public void notify_without_redirect() throws Exception {
        rpslObjectUpdateDao.createObject(RpslObject.parse(PERSON_ANY1_TEST));
        rpslObjectUpdateDao.createObject(RpslObject.parse(MNTNER_TEST_MNTNER));

        String response = doGetRequest(getUrl("test", "DATA=" + encode(MNTNER_TEST_MNTNER + "\nremarks: updated" + "\npassword: emptypassword")), HttpURLConnection.HTTP_OK);

        assertThat(response, containsString("Modify SUCCEEDED: [mntner] mntner"));

        assertNotNull(getMessage("noreply@ripe.net"));
        assertThat(anyMoreMessages(), is(false));
    }

    @Test
    public void only_data_parameter_create_object() throws Exception {
        rpslObjectUpdateDao.createObject(RpslObject.parse(PERSON_ANY1_TEST));

        String response = doGetRequest(getUrl("test", "DATA=" + encode(MNTNER_TEST_MNTNER + "\npassword: emptypassword")), HttpURLConnection.HTTP_OK);

        assertThat(response, containsString("Create SUCCEEDED: [mntner] mntner"));
    }

    @Test
    public void only_data_parameter_create_object_incorrect_source() throws Exception {
        rpslObjectUpdateDao.createObject(RpslObject.parse(PERSON_ANY1_TEST));
        final String mntnerInvalidSource = MNTNER_TEST_MNTNER.replaceAll("source: TEST", "source: invalid");

        String response = doGetRequest(getUrl("test", "DATA=" + encode(mntnerInvalidSource + "\npassword: emptypassword")), HttpURLConnection.HTTP_OK);

        assertThat(response, containsString("Error:   Unrecognized source: invalid"));
    }

    @Test
    public void only_data_parameter_update_object() throws Exception {
        rpslObjectUpdateDao.createObject(RpslObject.parse(PERSON_ANY1_TEST));
        rpslObjectUpdateDao.createObject(RpslObject.parse(MNTNER_TEST_MNTNER));

        String response = doGetRequest(getUrl("test", "DATA=" + encode(MNTNER_TEST_MNTNER + "\nremarks: new" + "\npassword: emptypassword")), HttpURLConnection.HTTP_OK);

        assertThat(response, containsString("Modify SUCCEEDED: [mntner] mntner"));
    }

    @Test
    public void only_new_parameter() throws Exception {
        String response = doGetRequest(getUrl("test", "NEW=yes"), HttpURLConnection.HTTP_BAD_REQUEST);

        assertThat(response, containsString("DATA parameter is missing"));
    }

    @Test
    public void new_and_data_parameters_get_request() throws Exception {
        rpslObjectUpdateDao.createObject(RpslObject.parse(PERSON_ANY1_TEST));

        String response = doGetRequest(getUrl("test", "DATA=" + encode(MNTNER_TEST_MNTNER + "\npassword: emptypassword") + "&NEW=yes"), HttpURLConnection.HTTP_OK);

        assertThat(response, containsString("Create SUCCEEDED: [mntner] mntner"));
    }

    @Test
    public void new_and_data_parameters_existing_object_get_request() throws Exception {
        rpslObjectUpdateDao.createObject(RpslObject.parse(PERSON_ANY1_TEST));
        rpslObjectUpdateDao.createObject(RpslObject.parse(MNTNER_TEST_MNTNER));

        String response = doGetRequest(getUrl("test", "DATA=" + encode(MNTNER_TEST_MNTNER + "\nremarks: new" + "\npassword: emptypassword") + "&NEW=yes"), HttpURLConnection.HTTP_OK);

        assertThat(response, containsString(
                "***Error:   Enforced new keyword specified, but the object already exists in the\n" +
                "            database"));
    }

    @Test
    public void new_and_data_parameters_urlencoded_post_request() throws Exception {
        rpslObjectUpdateDao.createObject(RpslObject.parse(PERSON_ANY1_TEST));

        String response = doPostOrPutRequest(getUrl("test", ""), "POST", "DATA=" + encode(MNTNER_TEST_MNTNER + "\npassword: emptypassword") + "&NEW=yes", MediaType.APPLICATION_FORM_URLENCODED, HttpURLConnection.HTTP_OK);

        assertThat(response, containsString("Create SUCCEEDED: [mntner] mntner"));
    }

    @Test
    public void new_and_data_parameters_multipart_post_request() throws Exception {
        rpslObjectUpdateDao.createObject(RpslObject.parse(PERSON_ANY1_TEST));

        String response = doPostOrPutRequest(getUrl("test", ""), "POST", "DATA=" + encode(MNTNER_TEST_MNTNER + "\npassword: emptypassword") + "&NEW=yes", MediaType.MULTIPART_FORM_DATA, HttpURLConnection.HTTP_OK);

        assertThat(response, containsString("Create SUCCEEDED: [mntner] mntner"));
    }

    @Test
    public void invalid_source() throws Exception {
        rpslObjectUpdateDao.createObject(RpslObject.parse(PERSON_ANY1_TEST));

        String response = doGetRequest(getUrl("invalid", "DATA=" + encode(MNTNER_TEST_MNTNER + "\npassword: emptypassword")), HttpURLConnection.HTTP_BAD_REQUEST);

        assertThat(response, containsString("Invalid source specified: invalid"));
    }

    // helper methods

    private MimeMessage getMessage(final String to) throws MessagingException {
        return mailSender.getMessage(to);
    }

    private boolean anyMoreMessages() {
        return mailSender.anyMoreMessages();
    }

    private String getUrl(final String instance, final String command) {
        return "http://localhost:" + getPort(Audience.PUBLIC) + String.format("/whois/syncupdates/%s?%s", instance, command);
    }

    @Override
    protected WebResource createResource(final Audience audience, final String path) {
        return client.resource(String.format("http://localhost:%s/%s", getPort(audience), path));
    }
}
