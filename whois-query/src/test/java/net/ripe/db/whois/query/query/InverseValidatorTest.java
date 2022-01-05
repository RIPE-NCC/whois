package net.ripe.db.whois.query.query;

import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.query.QueryMessages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InverseValidatorTest {
    @Mock Query query;
    Messages messages;

    InverseValidator subject;

    @BeforeEach
    public void setup() {
        subject = new InverseValidator();
        messages = new Messages();
    }

    @Test
    public void not_inverse_query() {
        when(query.isInverse()).thenReturn(FALSE);

        subject.validate(query, messages);

        assertThat(messages.hasMessages(), is(false));
    }

    @Test
    public void searchValue_not_SSO_nor_MD5() {
        when(query.isInverse()).thenReturn(TRUE);
        when(query.getSearchValue()).thenReturn("ORG-TP1-TEST");

        subject.validate(query, messages);

        assertThat(messages.hasMessages(), is(false));
    }

    @Test
    public void searchValue_SSO() {
        when(query.isInverse()).thenReturn(TRUE);
        when(query.getSearchValue()).thenReturn("SSO test@test.net");

        subject.validate(query, messages);

        assertThat(messages.getErrors(), contains(QueryMessages.inverseSearchNotAllowed()));
    }

    @Test
    public void searchValue_MD5() {
        when(query.isInverse()).thenReturn(TRUE);
        when(query.getSearchValue()).thenReturn("MD5-PW \\$1\\$fU9ZMQN9\\$QQtm3kRqZXWAuLpeOiLN7.");

        subject.validate(query, messages);

        assertThat(messages.getErrors(), contains(QueryMessages.inverseSearchNotAllowed()));
    }
}
