package net.ripe.db.whois.rdap;

import com.fasterxml.jackson.databind.SerializationFeature;
import net.ripe.db.whois.api.httpserver.ServletDeployer;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RdapServletDeployer implements ServletDeployer {

    private final RdapController rdapController;
    private final RdapExceptionMapper rdapExceptionMapper;
    private final RdapRequestTypeConverter rdapRequestTypeConverter;

    @Autowired
    public RdapServletDeployer(final RdapController rdapController,
                               final RdapExceptionMapper rdapExceptionMapper,
                               final RdapRequestTypeConverter rdapRequestTypeConverter) {
        this.rdapController = rdapController;
        this.rdapExceptionMapper = rdapExceptionMapper;
        this.rdapRequestTypeConverter = rdapRequestTypeConverter;
    }

    @Override
    public void deploy(final ServletContextHandler context) {
        final RdapJsonProvider rdapJsonProvider = new RdapJsonProvider();
        rdapJsonProvider.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, true);
        rdapJsonProvider.configure(SerializationFeature.INDENT_OUTPUT, true);

        final ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.register(rdapController);
        resourceConfig.register(rdapRequestTypeConverter);
        resourceConfig.register(rdapExceptionMapper);
        resourceConfig.register(rdapJsonProvider);
        context.addServlet(new ServletHolder("Whois RDAP REST API", new ServletContainer(resourceConfig)), "/rdap/*");
    }
}
