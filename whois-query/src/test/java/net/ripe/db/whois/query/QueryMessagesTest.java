package net.ripe.db.whois.query;

import com.google.common.collect.ImmutableMap;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.query.domain.QueryMessages;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public class QueryMessagesTest {

    @Test
    public void equality() {
        Message subject = QueryMessages.relatedTo("key");
        Message clone = QueryMessages.relatedTo("key");
        Message noClone = QueryMessages.relatedTo("key2");
        Message sameArgs = QueryMessages.invalidObjectType("key2");

        assertThat(subject, is(subject));
        assertThat(subject, is(clone));
        assertFalse(subject.equals(null));
        assertFalse(subject.equals(1));
        assertFalse(subject.equals(noClone));
        assertFalse(noClone.equals(sameArgs));

        assertThat(subject.hashCode(), is(clone.hashCode()));
    }

    @Test
    public void headerShouldContainLinkToTermsAndConditions() {
        assertThat(QueryMessages.termsAndConditions().toString(), containsString("http://www.ripe.net/db/support/db-terms-conditions.pdf"));
    }

    @Test
    public void duplicateIpFlagsPassedShouldContainError() {
        assertThat(QueryMessages.duplicateIpFlagsPassed().toString(), containsString("%ERROR:901:"));

    }

    @Test
    public void restApiExpectsAbuseContactsInSpecificFormat() {
        assertThat(QueryMessages.abuseCShown(ImmutableMap.of("193.0.0.0 - 193.0.7.255", "abuse@ripe.net")).toString(), is("% Abuse contact for '193.0.0.0 - 193.0.7.255' is 'abuse@ripe.net'\n"));
    }

    @Test
    public void internalErrorMessageShouldContainErrorCode() {
        assertThat(QueryMessages.internalErrorOccured().toString(), containsString("%ERROR:100:"));
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
        assertThat(QueryMessages.accessDeniedPermanently(InetAddress.getLocalHost()).toString(), containsString("%ERROR:201:"));
    }

    @Test
    public void accessDeniedTemporarilyMessageShouldContainErrorCode() throws UnknownHostException {
        assertThat(QueryMessages.accessDeniedTemporarily(InetAddress.getLocalHost()).toString(), containsString("%ERROR:201:"));
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
