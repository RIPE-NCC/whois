package net.ripe.db.whois.api.freetext;

import net.ripe.db.whois.api.httpserver.Audience;
import net.ripe.db.whois.api.httpserver.ServletDeployer;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.io.PrintWriter;

@Component
public class FreeTextSearchServlet extends HttpServlet implements ServletDeployer {
    private static final Logger LOGGER = LoggerFactory.getLogger(FreeTextSearchServlet.class);

    private final FreeTextSearch freeTextSearch;

    @Autowired
    public FreeTextSearchServlet(final FreeTextSearch freeTextSearch) {
        this.freeTextSearch = freeTextSearch;
    }

    @Override
    public Audience getAudience() {
        return Audience.PUBLIC;
    }

    @Override
    public void deploy(final WebAppContext context) {
        context.addServlet(new ServletHolder("Free-text search", this), "/search/*");
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        try {
            response.setHeader(HttpHeaders.CONTENT_TYPE, "text/xml;charset=UTF-8");
            final PrintWriter writer = response.getWriter();
            freeTextSearch.freeTextSearch(request.getQueryString(), writer);
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Unexpected exception on query: {}", request.getQueryString(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unexpected error");
        }
    }
}
