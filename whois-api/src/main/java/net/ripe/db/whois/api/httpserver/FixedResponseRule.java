package net.ripe.db.whois.api.httpserver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.rewrite.handler.Rule;

import java.io.IOException;

public class FixedResponseRule extends Rule {

    private final int responseCode;

    public FixedResponseRule(final int responseCode) {
        this.responseCode = responseCode;
    }

    @Override
    public String matchAndApply(String target, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (HttpStatus.isClientError(responseCode) || HttpStatus.isServerError(responseCode)) {
            response.sendError(responseCode);
            this.setHandling(true);
        } else {
            response.setStatus(responseCode);
        }
        return target;
    }

    @Override
    public boolean isTerminating() {
        return true;
    }

    @Override
    public boolean isHandling() {
        return true;
    }
}
