package net.ripe.db.whois.rdap;

import com.fasterxml.jackson.databind.SerializationFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RdapResourceConfig extends ResourceConfig {

    @Autowired
    public RdapResourceConfig(
            final RdapController rdapController,
            final RdapExceptionMapper rdapExceptionMapper,
            final RdapRequestTypeConverter rdapRequestTypeConverter) {
        register(rdapController);
        register(rdapRequestTypeConverter);
        register(rdapExceptionMapper);
        final RdapJsonProvider rdapJsonProvider = new RdapJsonProvider();
        rdapJsonProvider.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, true);
        rdapJsonProvider.configure(SerializationFeature.INDENT_OUTPUT, true);
        register(rdapJsonProvider);
    }

}
