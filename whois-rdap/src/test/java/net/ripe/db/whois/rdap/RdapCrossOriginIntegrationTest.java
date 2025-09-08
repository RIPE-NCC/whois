package net.ripe.db.whois.rdap;

import com.google.common.net.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(MockitoExtension.class)
public class RdapCrossOriginIntegrationTest extends AbstractIntegrationTest {

    @Test
    public void cross_origin_outside_ripe() {

        final Response response = RestTest.target(getPort(), "rdap/ip/192.0.2.0/24")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header(com.google.common.net.HttpHeaders.ORIGIN, "https://example.net")
                .get(Response.class);

        assertThat(response.getHeaderString(com.google.common.net.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN), is("*"));
        assertThat(response.getHeaderString(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS), is("false"));
    }

    @Test
    public void cross_origin_ripe() {

        final Response response = RestTest.target(getPort(), "rdap/ip/192.0.2.0/24")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header(com.google.common.net.HttpHeaders.ORIGIN, "https://db.ripe.net")
                .get(Response.class);

        assertThat(response.getHeaderString(com.google.common.net.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN), is("*"));
        assertThat(response.getHeaderString(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS), is("false"));
    }
}
