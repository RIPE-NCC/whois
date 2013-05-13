package net.ripe.db.whois.query.support;

// Because I could not find the PatternMatcher in the current version of org.hamcrest
// Taken from http://piotrga.wordpress.com/2009/03/27/hamcrest-regex-matcher/

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;

import java.util.regex.Pattern;

public class PatternMatcher extends BaseMatcher<String> {
    private final Pattern pattern;

    public PatternMatcher(final String pattern) {
        this(Pattern.compile(pattern));
    }
    public PatternMatcher(final Pattern pattern) {
        this.pattern = pattern;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("to match regular expression ").appendValue(pattern);
    }

    @Override
    public boolean matches(Object o) {
        if (o instanceof String) {
            return pattern.matcher((String)o).find();
        }
        return false;
    }

    @Factory
    public static Matcher<String> matchesPattern(String pattern) {
        return new PatternMatcher(pattern);
    }

    @Factory
    public static Matcher<String> matchesPattern(Pattern pattern) {
        return new PatternMatcher(pattern);
    }
}
