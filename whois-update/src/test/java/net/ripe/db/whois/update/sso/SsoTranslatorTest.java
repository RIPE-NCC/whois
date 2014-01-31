package net.ripe.db.whois.update.sso;

import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.sso.CrowdClient;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SsoTranslatorTest {

    @Mock UpdateContext updateContext;
    @Mock Update update;
    @Mock CrowdClient crowdClient;


    private SsoTranslator subject;

    @Before
    public void setup() {
        subject = new SsoTranslator(crowdClient);
    }

    @Test
    public void translate_not_a_maintainer() {
        final RpslObject object = RpslObject.parse("person: test person\nnic-hdl: test-nic");

        final RpslObject result = subject.translateAuthToUuid(updateContext, object);

        verifyZeroInteractions(updateContext);
        assertThat(result, is(object));
    }

    @Test
    public void translate_to_uuid_username_stored_in_context_already() {
        final RpslObject object = RpslObject.parse("mntner: TEST-MNT\nauth: SSO username@test.net");
        when(updateContext.getSsoTranslationResult("username@test.net")).thenReturn("BBBB-1234-CCCC-DDDD");

        final RpslObject result = subject.translateAuthToUsername(updateContext, object);

        assertThat(result, is(RpslObject.parse("mntner: TEST-MNT\nauth: SSO BBBB-1234-CCCC-DDDD")));
    }

    @Test
    public void translate_to_uuid_username_not_stored_in_context() {
        final RpslObject object = RpslObject.parse("mntner: TEST-MNT\nauth: SSO username@test.net");
        when(updateContext.getSsoTranslationResult("username@test.net")).thenReturn("BBBB-1234-CCCC-DDDD");

        final RpslObject result = subject.translateAuthToUuid(updateContext, object);

        assertThat(result, is(RpslObject.parse("mntner: TEST-MNT\nauth: SSO BBBB-1234-CCCC-DDDD")));
        verify(updateContext).getSsoTranslationResult("username@test.net");
    }

    @Test
    public void translate_to_username_uuid_stored_in_context_already() {
        final RpslObject object = RpslObject.parse("mntner: TEST-MNT\nauth: SSO aadd-2132-aaa-fff");

        when(updateContext.getSsoTranslationResult("aadd-2132-aaa-fff")).thenReturn("username@test.net");

        final RpslObject result = subject.translateAuthToUuid(updateContext, object);
        assertThat(result, is(RpslObject.parse("mntner: TEST-MNT\nauth: SSO username@test.net")));
    }

    @Test
    public void populate_not_maintainer_object() {
        final RpslObject object = RpslObject.parse("aut-num: AS1234");
        when(update.getSubmittedObject()).thenReturn(object);

        subject.populate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void populate_no_sso_auth() {
        final RpslObject object = RpslObject.parse("mntner: TEST-MNT\nauth: MD5-PW aaff1232431");
        when(update.getSubmittedObject()).thenReturn(object);

        subject.populate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void populate_sso_auth() {
        final RpslObject object = RpslObject.parse("mntner: TEST-MNT\nauth: SSO user@test.net");
        when(update.getSubmittedObject()).thenReturn(object);
        when(updateContext.hasSsoTranslationResult("user@test.net")).thenReturn(true);

        subject.populate(update, updateContext);

        verify(updateContext, times(0)).addSsoTranslationResult(eq("user@test.net"), anyString());
    }
}
