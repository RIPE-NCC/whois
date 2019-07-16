package net.ripe.db.whois.api.rdap;

import com.fasterxml.jackson.databind.SerializationFeature;
import net.ripe.db.whois.api.httpserver.ServletDeployer;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

@Component
public class WhoisRdapServletDeployer implements ServletDeployer {

    private final WhoisRdapService whoisRDAPService;
    private final RdapExceptionMapper rdapExceptionMapper;
    private final RdapRequestTypeConverter rdapRequestTypeConverter;

    @Autowired
    public WhoisRdapServletDeployer(final WhoisRdapService whoisRDAPService, final RdapExceptionMapper rdapExceptionMapper, final RdapRequestTypeConverter rdapRequestTypeConverter) {
        this.whoisRDAPService = whoisRDAPService;
        this.rdapExceptionMapper = rdapExceptionMapper;
        this.rdapRequestTypeConverter = rdapRequestTypeConverter;
    }

    @Override
    public void deploy(final WebAppContext context) {
        final RdapJsonProvider rdapJsonProvider = new RdapJsonProvider();
        rdapJsonProvider.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, true);
        rdapJsonProvider.configure(SerializationFeature.INDENT_OUTPUT, true);

        // allow cross-origin requests from ANY origin (by default)
        context.addFilter(org.eclipse.jetty.servlets.CrossOriginFilter.class, "/rdap/*", EnumSet.allOf(DispatcherType.class));

        final ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.register(whoisRDAPService);
        resourceConfig.register(rdapRequestTypeConverter);
        resourceConfig.register(rdapExceptionMapper);
        resourceConfig.register(rdapJsonProvider);
        context.addServlet(new ServletHolder("Whois RDAP REST API", new ServletContainer(resourceConfig)), "/rdap/*");
    }
}
