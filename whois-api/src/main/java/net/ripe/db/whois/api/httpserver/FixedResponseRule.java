package net.ripe.db.whois.api.httpserver;

import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.rewrite.handler.Rule;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

import java.io.IOException;

public class FixedResponseRule extends Rule {

    private final int responseCode;

    public FixedResponseRule(final int responseCode) {
        this.responseCode = responseCode;
    }

    @Override
    public Handler matchAndApply(Handler handler) throws IOException {
       return new Handler(handler)
            {
                @Override
                protected boolean handle(final Response response, final Callback callback)
                {
                    if (HttpStatus.isClientError(responseCode) || HttpStatus.isServerError(responseCode)) {
                        response.setStatus(responseCode);
                        callback.succeeded();
                    } else {
                        response.setStatus(responseCode);
                    }
                    return true;
                }
            };
    }

    @Override
    public boolean isTerminating() {
        return true;
    }
}
