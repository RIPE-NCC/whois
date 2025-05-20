package net.ripe.db.whois.api.httpserver;

import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.rewrite.handler.Rule;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link Rule} to match on request parameter regex and return a response code.
 */
public class QueryParamRegexRule extends Rule {

    private final Pattern pattern;
    private final int responseCode;

    public QueryParamRegexRule(final String regex, final int responseCode) {
        pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        this.responseCode = responseCode;
        setTerminating(true);
    }

    @Override
    public Handler matchAndApply(final Handler handler) {
        if (matches(handler)) {
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
        return null;
    }

    private boolean matches(final Request request) {
        final Matcher matcher = pattern.matcher(request.getHttpURI().getQuery());
        return matcher.matches();
    }
}
