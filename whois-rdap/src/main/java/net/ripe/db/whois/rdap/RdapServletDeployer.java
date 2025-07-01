package net.ripe.db.whois.rdap;

import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.servlet.DispatcherType;
import net.ripe.db.whois.api.httpserver.ServletDeployer;
import org.eclipse.jetty.ee10.servlet.FilterHolder;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.ee10.servlets.CrossOriginFilter;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

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

        // allow cross-origin requests from ANY origin (by default)
        final FilterHolder crossOriginFilterHolder = context.addFilter(RdapCrossOriginFilter.class, "/rdap/*", EnumSet.allOf(DispatcherType.class));
        crossOriginFilterHolder.setInitParameter(CrossOriginFilter.ALLOW_CREDENTIALS_PARAM, "false");
        crossOriginFilterHolder.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "GET, OPTIONS");

        final ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.register(rdapController);
        resourceConfig.register(rdapRequestTypeConverter);
        resourceConfig.register(rdapExceptionMapper);
        resourceConfig.register(rdapJsonProvider);
        context.addServlet(new ServletHolder("Whois RDAP REST API", new ServletContainer(resourceConfig)), "/rdap/*");
    }
}
