package net.ripe.db.whois.api.httpserver;

import org.eclipse.jetty.webapp.WebAppContext;

public interface ServletDeployer {
    void deploy(WebAppContext context);
}
