package net.ripe.db.whois.api.httpserver;

import org.eclipse.jetty.rewrite.handler.RewriteRegexRule;
import org.eclipse.jetty.rewrite.handler.Rule;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CaseInsensitiveRewriteRegexRule extends RewriteRegexRule {

    private final String regex;

    public CaseInsensitiveRewriteRegexRule(String regex, String replacement) {
        this.regex = regex;
        setReplacement(replacement);
        setTerminating(true);
    }

    @Override
    public Rule.Handler matchAndApply(Rule.Handler input) throws IOException {
        String target = this.isMatchQuery() ? input.getHttpURI().getPathQuery() : input.getHttpURI().getPath();
        Matcher matcher =  Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(target);
        return matcher.matches() ? this.apply(input, matcher) : null;
    }

}

