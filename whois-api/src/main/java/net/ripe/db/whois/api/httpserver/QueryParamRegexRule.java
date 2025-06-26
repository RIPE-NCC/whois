package net.ripe.db.whois.api.httpserver;

import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.rewrite.handler.Rule;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

/**
 * {@link Rule} to match on request parameter regex and return a response code.
 */
public class QueryParamRegexRule extends Rule {

    @Override
    public Handler matchAndApply(final Handler handler) {
        if (handler.getHttpURI().getQuery() != null && handler.getHttpURI().getQuery().toLowerCase().contains("password")) {

            return new Handler(handler)
            {
                @Override
                protected boolean handle(final Response response, final Callback callback)
                {
                    response.setStatus(HttpStatus.FORBIDDEN_403);
                    callback.succeeded();

                    return true;
                }
            };
        }

        return null; // no rewrite
    }
}