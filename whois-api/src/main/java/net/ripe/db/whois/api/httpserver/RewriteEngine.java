package net.ripe.db.whois.api.httpserver;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpScheme;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.rewrite.handler.RedirectRegexRule;
import org.eclipse.jetty.rewrite.handler.RewriteHandler;
import org.eclipse.jetty.rewrite.handler.RewriteRegexRule;
import org.eclipse.jetty.rewrite.handler.VirtualHostRuleContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Set;

@Component
public class RewriteEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(RewriteEngine.class);

    private final String restVirtualHost;
    private final String syncupdatesVirtualHost;
    private final String rdapVirtualHost;
    private final String source;
    private final String nonAuthSource;

    private String clientAuthHost;

    @Autowired
    public RewriteEngine(@Value("${api.rest.baseurl}") final String baseUrl,
                         @Value("${whois.source}") final String source,
                         @Value("${whois.nonauth.source}") final String nonAuthSource,
                         @Value("${api.client.auth.baseurl:}") final String clientAuthBaseUrl) {
        this.source = source;
        this.nonAuthSource = nonAuthSource;

        URI restBaseUri = URI.create(baseUrl);
        restVirtualHost = restBaseUri.getHost();

        if (StringUtils.isNotBlank(clientAuthBaseUrl)) {
            URI clientAuthBaseUri = URI.create(clientAuthBaseUrl);
            clientAuthHost = clientAuthBaseUri.getHost();
            LOGGER.info("Client Auth virtual host: {}", clientAuthHost);
        }

        syncupdatesVirtualHost = restVirtualHost.replace("rest", "syncupdates");
        rdapVirtualHost = restVirtualHost.replace("rest", "rdap");

        LOGGER.info("REST virtual host: {}", restVirtualHost);
        LOGGER.info("Syncupdates virtual host: {}", syncupdatesVirtualHost);
        LOGGER.info("RDAP virtual host: {}", rdapVirtualHost);
    }

    public RewriteHandler getRewriteHandler() {
        final RewriteHandler rewriteHandler = new RewriteHandler();
        rewriteHandler.setRewriteRequestURI(true);
        rewriteHandler.setRewritePathInfo(true);

        // rest
        final VirtualHostRuleContainer restVirtualHostRule = new VirtualHostRuleContainer();
        restVirtualHostRule.addVirtualHost(restVirtualHost);
        rewriteHandler.addRule(restVirtualHostRule);
        restRedirectRules(restVirtualHostRule);

        if (StringUtils.isNotBlank(clientAuthHost)) {
            // Client Auth
            VirtualHostRuleContainer clientAuthVirtualHostRule = new VirtualHostRuleContainer();
            clientAuthVirtualHostRule.addVirtualHost(clientAuthHost);
            rewriteHandler.addRule(clientAuthVirtualHostRule);
            restRedirectRules(clientAuthVirtualHostRule);
        }

        // rdap
        final VirtualHostRuleContainer rdapVirtualHostRule = new VirtualHostRuleContainer();
        rdapVirtualHostRule.addVirtualHost(rdapVirtualHost);
        rewriteHandler.addRule(rdapVirtualHostRule);
        final RewriteRegexRule rdapRule = new RewriteRegexRule("/(.+)", "/rdap/$1");
        rdapRule.setTerminating(true);
        rdapVirtualHostRule.addRule(rdapRule);

        // syncupdates
        final VirtualHostRuleContainer syncupdatesVirtualHostRule = new VirtualHostRuleContainer();
        syncupdatesVirtualHostRule.addVirtualHost(syncupdatesVirtualHost);
        rewriteHandler.addRule(syncupdatesVirtualHostRule);
        final RewriteRegexRule syncupdatesRule = new RewriteRegexRule(
            "/(.*)",
            String.format("/whois/syncupdates/%s/$1", source)
        );
        syncupdatesRule.setTerminating(true);
        syncupdatesVirtualHostRule.addRule(syncupdatesRule);

        // whois
        final VirtualHostRuleContainer whoisVirtualHostRule = new VirtualHostRuleContainer();
        whoisVirtualHostRule.addVirtualHost("whois.ripe.net");
        final RedirectRegexRule whoisRule = new RedirectRegexRule(".*", "https://apps.db.ripe.net/db-web-ui/query");
        whoisRule.setStatusCode(HttpStatus.MOVED_PERMANENTLY_301);
        whoisVirtualHostRule.addRule(whoisRule);
        rewriteHandler.addRule(whoisVirtualHostRule);

        return rewriteHandler;
    }

    private void restRedirectRules(final VirtualHostRuleContainer virtualHost) {
        virtualHost.addRule(new CaseInsensitiveRewriteRegexRule(
    "^/(fulltextsearch|search|geolocation|metadata|abuse-contact|references|autocomplete|domain-objects|client)/?(.*)$",
    "/whois/$1/$2"
        ));

        virtualHost.addRule(
            new HttpTransportRule(HttpScheme.HTTPS,
                new HttpMethodRule(Set.of(HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE), new CaseInsensitiveRewriteRegexRule(
                String.format("^/(%s|%s)/(.*)$", source, nonAuthSource),
                "/whois/$1/$2"
        ))));

        // Don't allow passwords over plain HTTP
        virtualHost.addRule(
            new HttpTransportRule(HttpScheme.HTTP,
                new HttpMethodRule(HttpMethod.GET, new QueryParamRegexRule(
                "(&?password=(.*))*$",
                HttpStatus.FORBIDDEN_403
        ))));

        // Lookups
        virtualHost.addRule(
            new HttpMethodRule(HttpMethod.GET, new CaseInsensitiveRewriteRegexRule(
                    String.format("^/(%s|%s|[a-z]+-grs)/(.*)$", source, nonAuthSource),
            "/whois/$1/$2"
        )));

        // CORS preflight request
        virtualHost.addRule(
            new HttpMethodRule(HttpMethod.OPTIONS, new CaseInsensitiveRewriteRegexRule(
                    String.format("^/(%s|%s|[a-z]+-grs)/(.*)$", source, nonAuthSource),
            "/whois/$1/$2"
        )));

        //
        // Batch
        virtualHost.addRule(new HttpTransportRule(HttpScheme.HTTPS,
            new HttpMethodRule(HttpMethod.POST, new CaseInsensitiveRewriteRegexRule(
            "^/batch/?(.*)$",
            "/whois/batch/$1"
        ))));

        // Slash
        virtualHost.addRule(new RedirectRegexRule(
        "^/$",
        "https://docs.db.ripe.net/RIPE-Database-Structure/REST-API-Data-model/#whoisresources"
        ));

        // catch-all fallthrough; return 400
        virtualHost.addRule(new FixedResponseRule(HttpStatus.BAD_REQUEST_400));
    }

}
