package net.ripe.db.whois.update.domain;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.credentials.Credential;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class ContentWithCredentialsTest {
    @Mock Credential credential;

    @Test
    public void no_credentials() {
        final ContentWithCredentials subject = new ContentWithCredentials("test");
        assertThat(subject.getContent(), is("test"));
        assertThat(subject.getCredentials(), hasSize(0));
    }

    @Test
    public void one_credentials() {
        final ContentWithCredentials subject = new ContentWithCredentials("test", Lists.newArrayList(credential));
        assertThat(subject.getContent(), is("test"));
        assertThat(subject.getCredentials(), hasSize(1));
    }

    @Test
    public void credentials_are_immutable() {
        assertThrows(UnsupportedOperationException.class, () -> {
            final ContentWithCredentials subject = new ContentWithCredentials("test", Lists.newArrayList(credential));
            subject.getCredentials().add(mock(Credential.class));
        });
    }
}
