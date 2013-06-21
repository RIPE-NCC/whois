package net.ripe.db.whois.api.whois;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import net.ripe.db.whois.api.DefaultExceptionMapper;
import net.ripe.db.whois.api.httpserver.Audience;
import net.ripe.db.whois.api.httpserver.ServletDeployer;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.map.SerializationConfig;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.Application;
import java.util.Set;

@Component
public class WhoisRDAPServletDeployer implements ServletDeployer {

    private final WhoisRDAPService whoisRDAPService;
    private final DefaultExceptionMapper defaultExceptionMapper;

    @Autowired
    public WhoisRDAPServletDeployer(final WhoisRDAPService whoisRDAPService, final DefaultExceptionMapper defaultExceptionMapper) {
        this.whoisRDAPService = whoisRDAPService;
        this.defaultExceptionMapper = defaultExceptionMapper;
    }

    @Override
    public Audience getAudience() {
        return Audience.RDAP;
    }

    @Override
    public void deploy(WebAppContext context) {
        final ServletHolder servlet = new ServletHolder("Whois RDAP REST API", new ServletContainer(new Application() {
            @Override
            public Set<Object> getSingletons() {
                final JacksonJaxbJsonProvider jaxbJsonProvider = new JacksonJaxbJsonProvider();
                jaxbJsonProvider.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
                return Sets.newLinkedHashSet(Lists.<Object>newArrayList(
                        whoisRDAPService,
                        defaultExceptionMapper,
                        jaxbJsonProvider));
            }
        }));

        servlet.setInitParameter("com.sun.jersey.config.property.packages", "net.ripe.db");
        //System.out.println("YO SHIZZLE: " + servlet.getInitParameters());
        context.addServlet(servlet, "/*");
    }
}