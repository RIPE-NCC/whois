package net.ripe.db.whois.common.grs;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class AuthoritativeResourceDataJmxTest {
    @Mock AuthoritativeResourceData authoritativeResourceData;
    @InjectMocks AuthoritativeResourceDataJmx subject;

    @Test
    public void refreshCache() {
        final String msg = subject.refreshCache("comment");
        assertThat(msg, is("Refreshed caches"));

        verify(authoritativeResourceData).refreshAuthoritativeResourceCache();
    }
}
