package net.ripe.db.whois.update.domain;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
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

    @Test(expected = UnsupportedOperationException.class)
    public void credentials_are_immutable() {
        final ContentWithCredentials subject = new ContentWithCredentials("test", Lists.newArrayList(credential));
        subject.getCredentials().add(mock(Credential.class));
    }
}
