package net.ripe.db.whois.api.httpserver;

import org.eclipse.jetty.ee10.servlet.ServletContextHandler;

public interface ServletDeployer {
    void deploy(ServletContextHandler context);
}
