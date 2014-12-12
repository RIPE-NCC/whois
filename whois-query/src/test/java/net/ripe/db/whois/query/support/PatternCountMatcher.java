package net.ripe.db.whois.query.support;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;

import java.util.regex.Pattern;

public class PatternCountMatcher extends BaseMatcher<String> {
    private final Pattern pattern;
    private final int count;

    public PatternCountMatcher(final String pattern, final int count) {
        this(Pattern.compile(pattern), count);
    }
    public PatternCountMatcher(final Pattern pattern, final int count) {
        this.pattern = pattern;
        this.count = count;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("to match regular expression ").appendValue(pattern).appendText(" ").appendValue(count).appendText(" times");
    }

    @Override
    public boolean matches(Object o) {
        if (o instanceof String) {
            final java.util.regex.Matcher matcher = pattern.matcher((String) o);
            int hit = 0;
            while (matcher.find()) {
                hit++;
            }
            return hit == count;
        }
        return false;
    }

    @Factory
    public static Matcher<String> matchesPatternCount(String pattern, int count) {
        return new PatternCountMatcher(pattern, count);
    }

    @Factory
    public static Matcher<String> matchesPatternCount(Pattern pattern, int count) {
        return new PatternCountMatcher(pattern, count);
    }
}
