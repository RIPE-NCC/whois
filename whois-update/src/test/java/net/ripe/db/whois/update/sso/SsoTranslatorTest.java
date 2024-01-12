package net.ripe.db.whois.update.sso;

import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.sso.AuthServiceClient;
import net.ripe.db.whois.common.sso.SsoTranslation;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SsoTranslatorTest {

    @Mock UpdateContext updateContext;
    @Mock SsoTranslation ssoTranslation;
    @Mock Update update;
    @Mock AuthServiceClient authServiceClient;
    SsoTranslator subject;

    @BeforeEach
    public void setup() {
        subject = new SsoTranslator(authServiceClient);
    }

    @Test
    public void translate_not_a_maintainer() {
        final RpslObject object = RpslObject.parse("person: test person\nnic-hdl: test-nic");

        final RpslObject result = subject.translateFromCacheAuthToUuid(updateContext, object);

        verifyNoMoreInteractions(updateContext);
        assertThat(result, is(object));
    }

    @Test
    public void translate_to_uuid_username_stored_in_context_already() {
        when(updateContext.getSsoTranslation()).thenReturn(ssoTranslation);
        when(ssoTranslation.getUuid("username@test.net")).thenReturn("BBBB-1234-CCCC-DDDD");

        final RpslObject object = RpslObject.parse("mntner: TEST-MNT\nauth: SSO username@test.net");
        final RpslObject result = subject.translateFromCacheAuthToUuid(updateContext, object);

        assertThat(result, is(RpslObject.parse("mntner: TEST-MNT\nauth: SSO BBBB-1234-CCCC-DDDD")));
    }

    @Test
    public void translate_to_uuid_username_not_stored_in_context() {
        when(updateContext.getSsoTranslation()).thenReturn(ssoTranslation);
        when(ssoTranslation.getUuid("username@test.net")).thenReturn("BBBB-1234-CCCC-DDDD");

        final RpslObject object = RpslObject.parse("mntner: TEST-MNT\nauth: SSO username@test.net");
        final RpslObject result = subject.translateFromCacheAuthToUuid(updateContext, object);

        assertThat(result, is(RpslObject.parse("mntner: TEST-MNT\nauth: SSO BBBB-1234-CCCC-DDDD")));
        verify(ssoTranslation).getUuid("username@test.net");
    }

    @Test
    public void translate_to_username_uuid_stored_in_context_already() {
        when(updateContext.getSsoTranslation()).thenReturn(ssoTranslation);
        when(ssoTranslation.getUsername("aadd-2132-aaa-fff")).thenReturn("username@test.net");

        final RpslObject object = RpslObject.parse("mntner: TEST-MNT\nauth: SSO aadd-2132-aaa-fff");
        final RpslObject result = subject.translateFromCacheAuthToUsername(updateContext, object);

        assertThat(result, is(RpslObject.parse("mntner: TEST-MNT\nauth: SSO username@test.net")));
    }

    @Test
    public void populate_not_maintainer_object() {
        final RpslObject object = RpslObject.parse("aut-num: AS1234");
        when(update.getSubmittedObject()).thenReturn(object);

        subject.populateCacheAuthToUuid(updateContext, update);

        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void populate_no_sso_auth() {
        final RpslObject object = RpslObject.parse("mntner: TEST-MNT\nauth: MD5-PW aaff1232431");
        when(update.getSubmittedObject()).thenReturn(object);

        subject.populateCacheAuthToUuid(updateContext, update);

        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void populate_sso_auth() {
        final RpslObject object = RpslObject.parse("mntner: TEST-MNT\nauth: SSO user@test.net");
        when(update.getSubmittedObject()).thenReturn(object);
        when(ssoTranslation.containsUsername("user@test.net")).thenReturn(true);
        when(updateContext.getSsoTranslation()).thenReturn(ssoTranslation);

        subject.populateCacheAuthToUuid(updateContext, update);

        verify(ssoTranslation, times(0)).put(eq("user@test.net"), anyString());
    }
}
