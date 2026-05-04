package net.ripe.db.nrtm4.servlet;

import jakarta.ws.rs.ext.MessageBodyWriter;
import net.ripe.db.whois.api.rest.Utf8StringWriter;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NrtmResourceConfig extends ResourceConfig {

    @Autowired
    public NrtmResourceConfig(
            final NrtmService nrtmClientService,
            final NrtmExceptionMapper nrtmExceptionMapper) {
        register(nrtmClientService);
        register(nrtmExceptionMapper);
        register(new NrtmCacheControl());

        final MessageBodyWriter<String> utf8MessageBodyWriter = new Utf8StringWriter();
        register(utf8MessageBodyWriter);
    }
}
