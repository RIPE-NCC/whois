package net.ripe.db.whois.rdap;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;

import static com.hazelcast.com.google.common.net.HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS;
import static com.hazelcast.com.google.common.net.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;
import static com.hazelcast.com.google.common.net.HttpHeaders.ORIGIN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@Tag("IntegrationTest")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class RdapCrossOriginTestIntegration extends AbstractRdapIntegrationTest {

    @Test
    public void cross_origin_outside_ripe() {

        final Response response = createResource("ip/192.0.2.0/24")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header(ORIGIN, "https://example.com")
                .get(Response.class);

        assertThat(response.getHeaderString(ACCESS_CONTROL_ALLOW_ORIGIN), is("*"));
        assertThat(response.getHeaderString(ACCESS_CONTROL_ALLOW_CREDENTIALS), is("false"));
    }

    @Test
    public void cross_origin_ripe() {

        final Response response = createResource("ip/192.0.2.0/24")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header(ORIGIN, "https://stat.ripe.net")
                .get(Response.class);

        assertThat(response.getHeaderString(ACCESS_CONTROL_ALLOW_ORIGIN), is("*"));
        assertThat(response.getHeaderString(ACCESS_CONTROL_ALLOW_CREDENTIALS), is("false"));
    }
}
