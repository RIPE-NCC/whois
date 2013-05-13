package net.ripe.db.whois.api.httpserver;

import org.eclipse.jetty.webapp.WebAppContext;

public interface ServletDeployer {

    Audience getAudience();

    void deploy(WebAppContext context);
}
