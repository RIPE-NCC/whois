package net.ripe.db.whois.api.httpserver;


import jakarta.ws.rs.core.Response;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import org.eclipse.jetty.http.HttpHeader;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;

@Tag("IntegrationTest")
public class JettyServerVersionIntegrationTest extends AbstractIntegrationTest {


    @Test
    public void jetty_server_version_not_sent() {
        final Response response = RestTest.target(getPort(), "/")
                .request()
                .get(Response.class);

        assertThat(response.getHeaderString(HttpHeader.SERVER.name()), is(nullValue()));
    }





}
