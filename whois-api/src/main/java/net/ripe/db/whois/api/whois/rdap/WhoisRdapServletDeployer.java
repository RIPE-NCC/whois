package net.ripe.db.whois.api.whois.rdap;

import com.fasterxml.jackson.databind.SerializationFeature;
import net.ripe.db.whois.api.httpserver.DefaultExceptionMapper;
import net.ripe.db.whois.api.httpserver.ServletDeployer;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WhoisRdapServletDeployer implements ServletDeployer {

    private final WhoisRdapService whoisRDAPService;
    private final DefaultExceptionMapper defaultExceptionMapper;

    @Autowired
    public WhoisRdapServletDeployer(final WhoisRdapService whoisRDAPService, final DefaultExceptionMapper defaultExceptionMapper) {
        this.whoisRDAPService = whoisRDAPService;
        this.defaultExceptionMapper = defaultExceptionMapper;
    }

    @Override
    public void deploy(final WebAppContext context) {
        final RdapJsonProvider rdapJsonProvider = new RdapJsonProvider();
        rdapJsonProvider.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, true);
        rdapJsonProvider.configure(SerializationFeature.INDENT_OUTPUT, true);

        final ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.register(whoisRDAPService);
        resourceConfig.register(defaultExceptionMapper);
        resourceConfig.register(rdapJsonProvider);
        context.addServlet(new ServletHolder("Whois RDAP REST API", new ServletContainer(resourceConfig)), "/rdap/*");


    }
}
