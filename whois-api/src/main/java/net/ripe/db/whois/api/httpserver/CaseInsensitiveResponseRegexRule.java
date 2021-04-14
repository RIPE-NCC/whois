package net.ripe.db.whois.api.httpserver;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Matcher;

public class CaseInsensitiveResponseRegexRule extends CaseInsensitiveRewriteRegexRule {

    private final int code;

    public CaseInsensitiveResponseRegexRule(String regex, int code) {
        super(regex, null);
        this.code = code;
    }

    @Override
    public String matchAndApply(String target, HttpServletRequest request, HttpServletResponse response) throws IOException {
        return StringUtils.isNotBlank(request.getQueryString())?
                super.matchAndApply(target + "?" + request.getQueryString(), request, response) :
                super.matchAndApply(target, request, response);
    }

    public String apply(String target, HttpServletRequest request, HttpServletResponse response, Matcher matcher) throws IOException {
        if (code > 0) {
            response.setStatus(code);
        }
        return target;
    }

}
