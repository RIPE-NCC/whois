package net.ripe.db.whois.rdap;

import net.ripe.db.whois.api.httpserver.ServletDeployer;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RdapServletDeployer implements ServletDeployer {

    private final RdapResourceConfig resourceConfig;

    @Autowired
    public RdapServletDeployer(final RdapResourceConfig resourceConfig) {
        this.resourceConfig = resourceConfig;
    }

    @Override
    public void deploy(final ServletContextHandler context) {
        context.addServlet(new ServletHolder("Whois RDAP REST API", new ServletContainer(resourceConfig)), "/rdap/*");
    }
}
