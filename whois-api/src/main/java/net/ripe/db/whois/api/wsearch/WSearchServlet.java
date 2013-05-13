package net.ripe.db.whois.api.wsearch;

import net.ripe.db.whois.api.httpserver.Audience;
import net.ripe.db.whois.api.httpserver.ServletDeployer;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class WSearchServlet extends HttpServlet implements ServletDeployer {
    @Override
    public Audience getAudience() {
        return Audience.INTERNAL;
    }

    @Override
    public void deploy(final WebAppContext context) {
        context.addServlet(new ServletHolder("WSearch", this), "/wsearch/*");
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        final Resource resource = new ClassPathResource("/html/wsearch.html");
        FileCopyUtils.copy(resource.getInputStream(), response.getOutputStream());
    }
}
