package net.ripe.db.whois.common.support;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.time.LocalDateTime;

public class DateMatcher {

    public static Matcher<LocalDateTime> isBefore(final LocalDateTime value) {
        return new IsBefore<>(value);
    }

    public static class IsBefore<T> extends BaseMatcher<T> {

        private final LocalDateTime value;

        public IsBefore(final LocalDateTime value) {
            this.value = value;
        }

        @Override
        public boolean matches(final Object actual) {
            return (actual instanceof LocalDateTime) && (((LocalDateTime)actual).isBefore(value));
        }

        @Override public void describeTo(final Description description) {
            description.appendText("before");
        }
    }


}
