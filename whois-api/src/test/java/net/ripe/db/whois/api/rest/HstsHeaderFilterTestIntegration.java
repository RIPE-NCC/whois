package net.ripe.db.whois.api.rest;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.SecureRestTest;
import net.ripe.db.whois.api.httpserver.AbstractHttpsIntegrationTest;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@Tag("IntegrationTest")
public class HstsHeaderFilterTestIntegration extends AbstractHttpsIntegrationTest {
    private static final String HSTS = HttpHeader.STRICT_TRANSPORT_SECURITY.asString();
    private static final String X_FORWARDED_PROTO = HttpHeader.X_FORWARDED_PROTO.asString();
    private static final String MAX_AGE = "max-age=31536000";

    private static final RpslObject OWNER_MNT = RpslObject.parse(
            """
            mntner:      OWNER-MNT
            descr:       Owner Maintainer
            admin-c:     TP1-TEST
            upd-to:      noreply@ripe.net
            auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test
            mnt-by:      OWNER-MNT
            source:      TEST
            """);

    private static final RpslObject TEST_PERSON = RpslObject.parse(
            """
            person:         Test Person
            address:        Singel 258
            phone:          +31 6 12345678
            nic-hdl:        TP1-TEST
            mnt-by:         OWNER-MNT
            source:         TEST
            """);

    @BeforeEach
    public void createTestObjects() {
        databaseHelper.addObject("person: Test Person\nnic-hdl: TP1-TEST");
        databaseHelper.addObject(OWNER_MNT);
        databaseHelper.updateObject(TEST_PERSON);
    }

    @Test
    public void hsts_header_over_https() {
        final Response response = SecureRestTest.target(getSecurePort(), "whois/test/person/TP1-TEST")
                .request(MediaType.APPLICATION_JSON)
                .get();

        assertThat(response.getStatus(), is(HttpStatus.OK_200));
        assertThat(response.getHeaderString(HSTS), is(MAX_AGE));
    }

    @Test
    public void hsts_header_over_https_on_error_response() {
        final Response response = SecureRestTest.target(getSecurePort(), "whois/test/person/NONEXISTANT")
                .request(MediaType.APPLICATION_JSON)
                .get();

        assertThat(response.getStatus(), is(HttpStatus.NOT_FOUND_404));
        assertThat(response.getHeaderString(HSTS), is(MAX_AGE));
    }

    @Test
    public void hsts_header_when_forwarded_proto_https() {
        final Response response = RestTest.target(getPort(), "whois/test/person/TP1-TEST")
                .request(MediaType.APPLICATION_JSON)
                .header(X_FORWARDED_PROTO, "https")
                .get();

        assertThat(response.getStatus(), is(HttpStatus.OK_200));
        assertThat(response.getHeaderString(HSTS), is(MAX_AGE));
    }

    @Test
    public void hsts_header_uses_last_forwarded_proto_value() {
        final Response response = RestTest.target(getPort(), "whois/test/person/TP1-TEST")
                .request(MediaType.APPLICATION_JSON)
                .header(X_FORWARDED_PROTO, "http, https")
                .get();

        assertThat(response.getStatus(), is(HttpStatus.OK_200));
        assertThat(response.getHeaderString(HSTS), is(MAX_AGE));
    }

    @Test
    public void no_hsts_header_on_plain_http() {
        final Response response = RestTest.target(getPort(), "whois/test/person/TP1-TEST")
                .request(MediaType.APPLICATION_JSON)
                .get();

        assertThat(response.getStatus(), is(HttpStatus.OK_200));
        assertThat(response.getHeaderString(HSTS), is(nullValue()));
    }

    @Test
    public void no_hsts_header_when_forwarded_proto_http() {
        final Response response = RestTest.target(getPort(), "whois/test/person/TP1-TEST")
                .request(MediaType.APPLICATION_JSON)
                .header(X_FORWARDED_PROTO, "http")
                .get();

        assertThat(response.getStatus(), is(HttpStatus.OK_200));
        assertThat(response.getHeaderString(HSTS), is(nullValue()));
    }

    @Test
    public void no_hsts_header_when_last_forwarded_proto_value_is_http() {
        final Response response = RestTest.target(getPort(), "whois/test/person/TP1-TEST")
                .request(MediaType.APPLICATION_JSON)
                .header(X_FORWARDED_PROTO, "https, http")
                .get();

        assertThat(response.getStatus(), is(HttpStatus.OK_200));
        assertThat(response.getHeaderString(HSTS), is(nullValue()));
    }
}
