package net.ripe.db.whois.api.httpserver;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.rewrite.handler.RegexRule;
import org.eclipse.jetty.rewrite.handler.Rule;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link Rule} to match on request parameter regex and return a response code.
 */
public class RequestParamRegexRule extends RegexRule {

    private final String regex;
    private final int responseCode;

    public RequestParamRegexRule(final String regex, final int responseCode) {
        this.regex = regex;
        this.responseCode = responseCode;
        setTerminating(true);
    }

    @Override
    public Rule.Handler matchAndApply(Rule.Handler input) {
        if(StringUtils.isEmpty(input.getHttpURI().getQuery())) {
            return null;
        }

        final String target = input.getHttpURI().getQuery();
        Matcher matcher =  Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(target);
        return matcher.matches() ? this.apply(input, matcher) : null;
    }

    @Override
    protected Handler apply(Handler handler, Matcher matcher) {
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
}