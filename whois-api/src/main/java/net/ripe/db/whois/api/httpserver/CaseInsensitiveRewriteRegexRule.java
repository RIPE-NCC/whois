package net.ripe.db.whois.api.httpserver;

import org.eclipse.jetty.rewrite.handler.RewriteRegexRule;

import java.util.regex.Pattern;

public class CaseInsensitiveRewriteRegexRule extends RewriteRegexRule {

    public CaseInsensitiveRewriteRegexRule(String regex, String replacement) {
        super(regex, replacement);
        setTerminating(true);
    }

    @Override
    public void setRegex(String regex) {
        _regex = regex == null ? null : Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

}
