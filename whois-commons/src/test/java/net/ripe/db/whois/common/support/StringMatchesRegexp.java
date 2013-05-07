package net.ripe.db.whois.common.support;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.regex.Pattern;

public class StringMatchesRegexp extends TypeSafeMatcher<String> {

    private final Pattern pattern;

    public StringMatchesRegexp(String regexp) {
        pattern = Pattern.compile(regexp);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("a String matching the regexp ").appendValue(pattern.pattern());
    }

    @Override
    public boolean matchesSafely(String item) {
        return pattern.matcher(item).matches();
    }

    @Factory
    public static Matcher<String> stringMatchesRegexp(String regexp) {
        return new StringMatchesRegexp(regexp);
    }

}
