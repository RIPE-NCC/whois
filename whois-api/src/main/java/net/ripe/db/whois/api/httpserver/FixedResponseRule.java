package net.ripe.db.whois.api.httpserver;

import org.eclipse.jetty.rewrite.handler.Rule;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

public class FixedResponseRule extends Rule {

    private final int responseCode;

    public FixedResponseRule(final int responseCode) {
        this.responseCode = responseCode;
    }

    @Override
    public Handler matchAndApply(Rule.Handler handler) {
        return new Handler(handler)
        {
            @Override
            protected boolean handle(final Response response, final Callback callback)
            {
                response.setStatus(responseCode);
                callback.succeeded();
                return true;
            }
        };
    }

    @Override
    public boolean isTerminating() {
        return true;
    }
}
