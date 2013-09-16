package net.ripe.db.whois.api.acl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import net.ripe.db.whois.api.DefaultExceptionMapper;
import net.ripe.db.whois.api.abusec.AbuseCService;
import net.ripe.db.whois.api.httpserver.Audience;
import net.ripe.db.whois.api.httpserver.ServletDeployer;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.DispatcherType;
import javax.ws.rs.core.Application;
import java.util.EnumSet;
import java.util.Set;

@Component
public class ApiServletDeployer implements ServletDeployer {
    private final ApiKeyFilter apiKeyFilter;
    private final AclBanService aclBanService;
    private final AclLimitService aclLimitService;
    private final AclMirrorService aclMirrorService;
    private final AclProxyService aclProxyService;
    private final AbuseCService abuseCService;
    private final DefaultExceptionMapper defaultExceptionMapper;

    @Autowired
    public ApiServletDeployer(final ApiKeyFilter apiKeyFilter, final AclBanService aclBanService, final AclLimitService aclLimitService, final AclMirrorService aclMirrorService, final AclProxyService aclProxyService, final AbuseCService abuseCService, final DefaultExceptionMapper defaultExceptionMapper) {
        this.aclBanService = aclBanService;
        this.aclLimitService = aclLimitService;
        this.aclMirrorService = aclMirrorService;
        this.aclProxyService = aclProxyService;
        this.abuseCService = abuseCService;
        this.defaultExceptionMapper = defaultExceptionMapper;
        this.apiKeyFilter=apiKeyFilter;
    }


    @Override
    public Audience getAudience() {
        return Audience.INTERNAL;
    }

    @Override
    public void deploy(final WebAppContext context) {
        context.addFilter(new FilterHolder(apiKeyFilter), "/api/*", EnumSet.of(DispatcherType.REQUEST));
        context.addServlet(new ServletHolder("REST API", new ServletContainer(new Application() {
            @Override
            public Set<Object> getSingletons() {
                return Sets.newLinkedHashSet(Lists.newArrayList(
                        aclBanService,
                        aclLimitService,
                        aclMirrorService,
                        aclProxyService,
                        abuseCService,
                        defaultExceptionMapper,
                        new JacksonJaxbJsonProvider()));
            }
        })), "/api/*");
    }
}
