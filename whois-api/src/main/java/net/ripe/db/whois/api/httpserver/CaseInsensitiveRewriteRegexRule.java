package net.ripe.db.whois.api.httpserver;

import org.eclipse.jetty.rewrite.handler.RewriteRegexRule;

import java.util.regex.Pattern;

public class CaseInsensitiveRewriteRegexRule extends RewriteRegexRule {

    public CaseInsensitiveRewriteRegexRule(final String regex, final String replacement) {
        // Compile regex with CASE_INSENSITIVE flag
        Pattern caseInsensitivePattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        setRegex(caseInsensitivePattern.pattern());   // This initializes the internal _regex field
        setReplacement(replacement);        // Sets the target replacement string
    }
}