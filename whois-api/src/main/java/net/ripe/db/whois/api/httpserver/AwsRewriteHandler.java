package net.ripe.db.whois.api.httpserver;

import net.ripe.db.whois.common.profiles.WhoisProfile;
import org.eclipse.jetty.rewrite.handler.RewriteHandler;
import org.eclipse.jetty.rewrite.handler.RewriteRegexRule;
import org.eclipse.jetty.rewrite.handler.VirtualHostRuleContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
@Profile(WhoisProfile.AWS_DEPLOYED)
public class AwsRewriteHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AwsRewriteHandler.class);

    private final String restVirtualHost;
    private final String syncupdatesVirtualHost;
    private final String rdapVirtualHost;

    @Autowired
    public AwsRewriteHandler(final @Value("${api.rest.baseurl}") String baseUrl) {
        URI uri = URI.create(baseUrl);
        restVirtualHost = uri.getHost();
        syncupdatesVirtualHost = restVirtualHost.replace("rest", "syncupdates");
        rdapVirtualHost = restVirtualHost.replace("rest", "rdap");

        LOGGER.info("AWS virtual host rewrites are active");
        LOGGER.info("REST virtual host: {}", restVirtualHost);
        LOGGER.info("Syncupdates virtual host: {}", syncupdatesVirtualHost);
        LOGGER.info("RDAP virtual host: {}", rdapVirtualHost);
    }

    public RewriteHandler getRewriteHandler() {
        final RewriteHandler rewriteHandler = new RewriteHandler();
        rewriteHandler.setRewriteRequestURI(true);
        rewriteHandler.setRewritePathInfo(false);

        // rest
        VirtualHostRuleContainer restVirtualHostRule = new VirtualHostRuleContainer();
        restVirtualHostRule.addVirtualHost(restVirtualHost);
        rewriteHandler.addRule(restVirtualHostRule);

        RewriteRegexRule restRule = new RewriteRegexRule(".+", "/whois/$1");
        restRule.setTerminating(true);
        restVirtualHostRule.addRule(restRule);

        // rdap
        VirtualHostRuleContainer rdapVirtualHostRule = new VirtualHostRuleContainer();
        rdapVirtualHostRule.addVirtualHost(rdapVirtualHost);
        rewriteHandler.addRule(rdapVirtualHostRule);

        RewriteRegexRule rdapRule = new RewriteRegexRule(".+", "/rdap/$1");
        rdapRule.setTerminating(true);
        rdapVirtualHostRule.addRule(rdapRule);

        // syncupdates
        VirtualHostRuleContainer syncupdatesVirtualHostRule = new VirtualHostRuleContainer();
        syncupdatesVirtualHostRule.addVirtualHost(syncupdatesVirtualHost);
        rewriteHandler.addRule(syncupdatesVirtualHostRule);

        RewriteRegexRule syncupdatesRule = new RewriteRegexRule(".+", "/whois/syncupdates/ripe/$1");
        syncupdatesRule.setTerminating(true);
        syncupdatesVirtualHostRule.addRule(syncupdatesRule);

        return rewriteHandler;
    }
}
