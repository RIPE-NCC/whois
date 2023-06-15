package net.ripe.db.whois.api.httpserver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.rewrite.handler.Rule;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link Rule} to match on request parameter regex and return a response code.
 */
public class RequestParamRegexRule extends Rule {

    private final Pattern pattern;
    private final int responseCode;

    public RequestParamRegexRule(final String regex, final int responseCode) {
        pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        this.responseCode = responseCode;
        setTerminating(true);
    }

    @Override
    public String matchAndApply(String target, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (StringUtils.isNotBlank(request.getQueryString())) {
            Matcher matcher = pattern.matcher(request.getQueryString());
            boolean matches = matcher.matches();
            if (matches) {
                return apply(target, response);
            }
        }
        return null;
    }

    private String apply(final String target, final HttpServletResponse response) throws IOException {
        if (HttpStatus.isClientError(responseCode) || HttpStatus.isServerError(responseCode)) {
            response.sendError(responseCode);
            this.setHandling(true);
        } else {
            response.setStatus(responseCode);
        }
        return target;
    }

}
