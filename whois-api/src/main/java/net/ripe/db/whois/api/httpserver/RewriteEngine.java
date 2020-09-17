package net.ripe.db.whois.api.httpserver;

import org.eclipse.jetty.rewrite.handler.RewriteHandler;
import org.eclipse.jetty.rewrite.handler.RewriteRegexRule;
import org.eclipse.jetty.rewrite.handler.VirtualHostRuleContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
public class RewriteEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(RewriteEngine.class);

    private final String restVirtualHost;
    private final String syncupdatesVirtualHost;
    private final String rdapVirtualHost;
    private final String source;

    @Autowired
    public RewriteEngine(final @Value("${api.rest.baseurl}") String baseUrl,
                         final @Value("${whois.source}") String source) {
        this.source = source;
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

        RewriteRegexRule restRule = new RewriteRegexRule("/(.+)", "/whois/$1");
        restRule.setTerminating(true);
        restVirtualHostRule.addRule(restRule);

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

        RewriteRegexRule syncupdatesRule = new RewriteRegexRule(
            "/(.*)",
            String.format("/whois/syncupdates/%s/$1", source)
        );

        syncupdatesRule.setTerminating(true);
        syncupdatesVirtualHostRule.addRule(syncupdatesRule);

        return rewriteHandler;
    }

}
