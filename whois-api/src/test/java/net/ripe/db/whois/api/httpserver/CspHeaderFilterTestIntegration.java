package net.ripe.db.whois.api.httpserver;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@Tag("IntegrationTest")
public class CspHeaderFilterTestIntegration extends AbstractIntegrationTest {
    private static final String CSP_HEADER = "Content-Security-Policy";
    private static final String EXPECTED_CSP = "default-src 'none'; frame-ancestors 'none'; sandbox";

    @BeforeEach
    public void setup() {
        databaseHelper.addObject("person: Test Person\nnic-hdl: TP1-TEST");
        databaseHelper.addObject(
                """
                aut-num:        AS102
                as-name:        End-User-2
                descr:          description
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                source:         TEST
                """);
    }

    @Test
    public void csp_header_on_successful_lookup() {
        final Response response = RestTest.target(getPort(), "whois/test/aut-num/AS102")
                .request(MediaType.APPLICATION_XML)
                .get(Response.class);

        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
        assertThat(response.getHeaderString(CSP_HEADER), is(EXPECTED_CSP));
    }

    @Test
    public void csp_header_on_not_found() {
        final Response response = RestTest.target(getPort(), "whois/test/person/NONEXISTENT-TEST")
                .request(MediaType.APPLICATION_XML)
                .get(Response.class);

        assertThat(response.getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
        assertThat(response.getHeaderString(CSP_HEADER), is(EXPECTED_CSP));
    }

    @Test
    public void csp_header_on_bad_request() {
        final Response response = RestTest.target(getPort(), "whois/test/aut-num/AS102/version")
                .request(MediaType.APPLICATION_XML)
                .get(Response.class);

        assertThat(response.getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
        assertThat(response.getHeaderString(CSP_HEADER), is(EXPECTED_CSP));
    }

    @Test
    public void csp_header_on_text_plain() {
        final Response response = RestTest.target(getPort(), "whois/test/aut-num/AS102")
                .request(MediaType.TEXT_PLAIN)
                .get(Response.class);

        assertThat(response.getHeaderString(CSP_HEADER), is(EXPECTED_CSP));
    }

    @Test
    public void csp_header_set_once() {
        final Response response = RestTest.target(getPort(), "whois/test/aut-num/AS102")
                .request(MediaType.APPLICATION_XML)
                .get(Response.class);

        assertThat(response.getHeaders().get(CSP_HEADER), hasSize(1));
    }
}
