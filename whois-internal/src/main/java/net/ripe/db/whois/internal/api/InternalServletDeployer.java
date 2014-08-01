package net.ripe.db.whois.internal.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import net.ripe.db.whois.api.httpserver.DefaultExceptionMapper;
import net.ripe.db.whois.api.httpserver.ServletDeployer;
import net.ripe.db.whois.internal.api.abusec.AbuseCService;
import net.ripe.db.whois.internal.api.acl.AclBanService;
import net.ripe.db.whois.internal.api.acl.AclLimitService;
import net.ripe.db.whois.internal.api.acl.AclMirrorService;
import net.ripe.db.whois.internal.api.acl.AclProxyService;
import net.ripe.db.whois.internal.api.acl.ApiKeyFilter;
import net.ripe.db.whois.internal.api.logsearch.LogSearchService;
import net.ripe.db.whois.internal.api.rnd.rest.VersionsRestService;
import net.ripe.db.whois.internal.api.sso.UserOrgFinderService;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

@Component
public class InternalServletDeployer implements ServletDeployer {
    private final ApiKeyFilter apiKeyFilter;
    private final AclBanService aclBanService;
    private final AclLimitService aclLimitService;
    private final AclMirrorService aclMirrorService;
    private final AclProxyService aclProxyService;
    private final AbuseCService abuseCService;
    private final LogSearchService logSearchService;
    private final DefaultExceptionMapper defaultExceptionMapper;
    private final UserOrgFinderService organisationsForSSOAuthService;
    private final VersionsRestService versionListService;

    @Autowired
    public InternalServletDeployer(final ApiKeyFilter apiKeyFilter,
                                   final AclBanService aclBanService,
                                   final AclLimitService aclLimitService,
                                   final AclMirrorService aclMirrorService,
                                   final AclProxyService aclProxyService,
                                   final AbuseCService abuseCService,
                                   final LogSearchService logSearchService,
                                   final DefaultExceptionMapper defaultExceptionMapper,
                                   final UserOrgFinderService organisationsForSSOAuthService,
                                   final VersionsRestService versionListService) {
        this.aclBanService = aclBanService;
        this.aclLimitService = aclLimitService;
        this.aclMirrorService = aclMirrorService;
        this.aclProxyService = aclProxyService;
        this.abuseCService = abuseCService;
        this.apiKeyFilter = apiKeyFilter;
        this.logSearchService = logSearchService;
        this.defaultExceptionMapper = defaultExceptionMapper;
        this.organisationsForSSOAuthService = organisationsForSSOAuthService;
        this.versionListService = versionListService;
    }

    @Override
    public void deploy(final WebAppContext context) {
        context.addFilter(new FilterHolder(apiKeyFilter), "/api/*", EnumSet.of(DispatcherType.REQUEST));

        final ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.register(aclBanService);
        resourceConfig.register(aclLimitService);
        resourceConfig.register(aclMirrorService);
        resourceConfig.register(aclProxyService);
        resourceConfig.register(abuseCService);
        resourceConfig.register(logSearchService);
        resourceConfig.register(organisationsForSSOAuthService);
        resourceConfig.register(versionListService);
        resourceConfig.register(defaultExceptionMapper);

        final JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
        provider.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, false);
        provider.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        resourceConfig.register(provider);

        context.addServlet(new ServletHolder("INTERNAL API", new ServletContainer(resourceConfig)), "/api/*");
    }
}
