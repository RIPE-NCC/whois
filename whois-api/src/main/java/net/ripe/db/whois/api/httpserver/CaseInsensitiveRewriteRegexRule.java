package net.ripe.db.whois.api.httpserver;

import org.eclipse.jetty.rewrite.handler.RegexRule;
import org.eclipse.jetty.rewrite.handler.RewriteRegexRule;

import java.util.regex.Pattern;

public class CaseInsensitiveRewriteRegexRule extends RewriteRegexRule {

    public CaseInsensitiveRewriteRegexRule(String regex, String replacement) {
        super.setReplacement(replacement);
        setCaseInsensitiveRegex(regex);
    }

    private void setCaseInsensitiveRegex(final String regex) {
        try {
            var field = RegexRule.class.getDeclaredField("_regex");
            field.setAccessible(true);
            field.set(this, Pattern.compile(regex, Pattern.CASE_INSENSITIVE));
        } catch (Exception e) {
            throw new RuntimeException("Failed to set case-insensitive regex", e);
        }
    }

}

