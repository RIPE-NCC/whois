package net.ripe.db.whois.common.support;

import net.ripe.db.whois.common.Message;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class ContainsMessage extends TypeSafeMatcher<Message> {

    private final String substring;

    public ContainsMessage(String substring) {
        this.substring = substring;
    }

    @Override
    protected boolean matchesSafely(final Message message) {
        return message != null && (message.toString()).contains(substring);
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("a message containing ")
                .appendText(" ")
                .appendValue(substring);
    }

    public static Matcher containsMessage(final String substring) {
        return new ContainsMessage(substring);
    }

}
