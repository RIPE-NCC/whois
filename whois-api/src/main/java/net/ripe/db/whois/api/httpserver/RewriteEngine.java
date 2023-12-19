package net.ripe.db.whois.api.httpserver;

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

    @Autowired
    public RewriteEngine(final @Value("${api.rest.baseurl}") String baseUrl,
                         final @Value("${whois.source}") String source,
                         final @Value("${whois.nonauth.source}") String nonAuthSource) {
        this.source = source;
        this.nonAuthSource = nonAuthSource;
        URI uri = URI.create(baseUrl);
        restVirtualHost = uri.getHost();
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
        VirtualHostRuleContainer restVirtualHostRule = new VirtualHostRuleContainer();
        restVirtualHostRule.addVirtualHost(restVirtualHost);
        rewriteHandler.addRule(restVirtualHostRule);
        restRedirectRules(restVirtualHostRule);

        // rdap
        VirtualHostRuleContainer rdapVirtualHostRule = new VirtualHostRuleContainer();
        rdapVirtualHostRule.addVirtualHost(rdapVirtualHost);
        rewriteHandler.addRule(rdapVirtualHostRule);

        RewriteRegexRule rdapRule = new RewriteRegexRule("/(.+)", "/rdap/$1");
        rdapRule.setTerminating(true);
        rdapVirtualHostRule.addRule(rdapRule);

        // syncupdates
        VirtualHostRuleContainer syncupdatesVirtualHostRule = new VirtualHostRuleContainer();
        syncupdatesVirtualHostRule.addVirtualHost(syncupdatesVirtualHost);
        rewriteHandler.addRule(syncupdatesVirtualHostRule);

        RewriteRegexRule syncupdatesEmptyQueryStringRule = new RewriteRegexRule(
            "/$",
            String.format("/whois/syncupdates/%s/?HELP=yes", source)
        );
        RewriteRegexRule syncupdatesRule = new RewriteRegexRule(
            "/(.*)",
            String.format("/whois/syncupdates/%s/$1", source)
        );

        syncupdatesEmptyQueryStringRule.setTerminating(true);
        syncupdatesRule.setTerminating(true);
        syncupdatesVirtualHostRule.addRule(syncupdatesEmptyQueryStringRule);
        syncupdatesVirtualHostRule.addRule(syncupdatesRule);

        return rewriteHandler;
    }

    private void restRedirectRules(final VirtualHostRuleContainer virtualHost) {
        virtualHost.addRule(new CaseInsensitiveRewriteRegexRule(
    "^/(fulltextsearch|search|geolocation|metadata|abuse-contact|references|autocomplete|domain|client)/?(.*)$",
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
                new HttpMethodRule(HttpMethod.GET, new RequestParamRegexRule(
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
        "https://apps.db.ripe.net/docs/RIPE-Database-Structure/REST-API-Data-model/#whoisresources"
        ));

        // catch-all fallthrough; return 400
        virtualHost.addRule(new FixedResponseRule(HttpStatus.BAD_REQUEST_400));
    }

}
