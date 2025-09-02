package net.ripe.db.whois.rdap;

import com.fasterxml.jackson.databind.SerializationFeature;
import net.ripe.db.whois.api.httpserver.ServletDeployer;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.handler.CrossOriginHandler;
import org.eclipse.jetty.util.Callback;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class RdapCrossOriginDeployer extends CrossOriginHandler implements ServletDeployer {

    private final RdapController rdapController;
    private final RdapExceptionMapper rdapExceptionMapper;
    private final RdapRequestTypeConverter rdapRequestTypeConverter;

    @Autowired
    public RdapCrossOriginDeployer(final RdapController rdapController,
                                   final RdapExceptionMapper rdapExceptionMapper,
                                   final RdapRequestTypeConverter rdapRequestTypeConverter) {
        this.rdapController = rdapController;
        this.rdapExceptionMapper = rdapExceptionMapper;
        this.rdapRequestTypeConverter = rdapRequestTypeConverter;
    }

    @Override
    public void deploy(ServletContextHandler context) {
        final RdapJsonProvider rdapJsonProvider = new RdapJsonProvider();
        rdapJsonProvider.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, true);
        rdapJsonProvider.configure(SerializationFeature.INDENT_OUTPUT, true);

        setAllowCredentials(false);
        setAllowedMethods(Set.of("GET", "OPTIONS")); //Only configures Jetty's internal CORS logic
        setAllowedOriginPatterns(Set.of("*"));

        final ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.register(rdapController);
        resourceConfig.register(rdapRequestTypeConverter);
        resourceConfig.register(rdapExceptionMapper);
        resourceConfig.register(rdapJsonProvider);
        context.addServlet(new ServletHolder("Whois RDAP REST API", new ServletContainer(resourceConfig)), "/rdap/*");

        context.setHandler(this);
    }

    @Override
    public boolean handle(Request request, Response response, Callback callback) throws Exception{
        final String origin = request.getHeaders().get(HttpHeader.ORIGIN);
        final String method = request.getMethod();

        if (origin == null && HttpMethod.GET.is(method)) {
            response.getHeaders().put(HttpHeader.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        }

        if (HttpMethod.OPTIONS.is(method)) {
            response.getHeaders().put("Allow", String.join(",", getAllowedMethods()));
        }

        return super.handle(request, response, callback);
    }

}
