package net.ripe.db.whois.query;

import net.ripe.db.whois.common.Message;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class QueryMessagesTest {

    @Test
    public void equality() {
        Message subject = QueryMessages.relatedTo("key");
        Message clone = QueryMessages.relatedTo("key");
        Message noClone = QueryMessages.relatedTo("key2");
        Message sameArgs = QueryMessages.invalidObjectType("key2");

        assertThat(subject, is(subject));
        assertThat(subject, is(clone));
        assertThat(subject, not(equalTo(null)));
        assertThat(subject, not(equalTo(1)));
        assertThat(subject, not(equalTo(noClone)));
        assertThat(noClone, not(equalTo(sameArgs)));

        assertThat(subject.hashCode(), is(clone.hashCode()));
    }

    @Test
    public void headerShouldContainLinkToTermsAndConditions() {
        assertThat(QueryMessages.termsAndConditions().toString(), containsString("https://docs.db.ripe.net/terms-conditions.html"));
    }

    @Test
    public void duplicateIpFlagsPassedShouldContainError() {
        assertThat(QueryMessages.duplicateIpFlagsPassed().toString(), containsString("%ERROR:901:"));

    }

    @Test
    public void restApiExpectsAbuseContactsInSpecificFormat() {
        assertThat(QueryMessages.abuseCShown("193.0.0.0 - 193.0.7.255", "abuse@ripe.net").toString(), is("% Abuse contact for '193.0.0.0 - 193.0.7.255' is 'abuse@ripe.net'\n"));
    }

    @Test
    public void internalErrorMessageShouldContainErrorCode() {
        assertThat(QueryMessages.internalErroroccurred().toString(), containsString("%ERROR:100:"));
    }

    @Test
    public void noSearchKeySpecifiedShouldContainError() {
        assertThat(QueryMessages.noSearchKeySpecified().toString(), containsString("%ERROR:106:"));
    }

    @Test
    public void noResultsMessageShouldContainErrorCode() {
        assertThat(QueryMessages.noResults("RIPE").toString(), containsString("%ERROR:101:"));
    }

    @Test
    public void accessDeniedPermanentlyShouldContainErrorCode() throws UnknownHostException {
        assertThat(QueryMessages.accessDeniedPermanently(InetAddress.getLocalHost().getHostAddress()).toString(), containsString("%ERROR:201:"));
    }

    @Test
    public void accessDeniedTemporarilyMessageShouldContainErrorCode() throws UnknownHostException {
        assertThat(QueryMessages.accessDeniedTemporarily(InetAddress.getLocalHost().getHostAddress()).toString(), containsString("%ERROR:201:"));
    }

    @Test
    public void tooLongInputStringShouldContainErrorCode() {
        assertThat(QueryMessages.inputTooLong().toString(), containsString("%ERROR:107:"));
    }

    @Test
    public void invalidObjectTypeShouldContainErrorCode() {
        assertThat(QueryMessages.invalidObjectType("").toString(), containsString("%ERROR:103:"));

    }

    @Test
    public void invalidInetnumMessageShouldContainErrorCode() {
        assertThat(QueryMessages.uselessIpFlagPassed().toString(), containsString("%WARNING:902:"));
    }

    @Test
    public void malformedQueryShouldContainError() {
        assertThat(QueryMessages.malformedQuery().toString(), containsString("%ERROR:111:"));
    }

    @Test
    public void notAllowedToProxyShouldContainError() {
        assertThat(QueryMessages.notAllowedToProxy().toString(), containsString("%ERROR:203:"));
    }
}
