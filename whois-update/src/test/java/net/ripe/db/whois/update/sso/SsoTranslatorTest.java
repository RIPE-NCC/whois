package net.ripe.db.whois.update.sso;

import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.UpdateContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SsoTranslatorTest {

    @Mock UpdateContext updateContext;


    @Test
    public void translate_not_a_maintainer() {
        final RpslObject object = RpslObject.parse("person: test person\nnic-hdl: test-nic");

        final RpslObject result = new SsoTranslator().translateAuthToUuid(updateContext, object);

        verifyZeroInteractions(updateContext);
        assertThat(result, is(object));
    }

    @Test
    public void translate_to_uuid_username_stored_in_context_already() {
        final RpslObject object = RpslObject.parse("mntner: TEST-MNT\nauth: SSO aadd-2132-aaa-fff");

        when(updateContext.getSsoTranslationResult("AADD-2132-AAA-FFF")).thenReturn("username@test.net");

        final RpslObject result = new SsoTranslator().translateAuthToUuid(updateContext, object);
        assertThat(result, is(RpslObject.parse("mntner: TEST-MNT\nauth: SSO username@test.net")));
    }

    @Test
    public void translate_to_uuid_username_not_stored_in_context() {
        final RpslObject object = RpslObject.parse("mntner: TEST-MNT\nauth: SSO aadd-2132-aaa-fff");
        when(updateContext.getSsoTranslationResult("AADD-2132-AAA-FFF")).thenReturn(null);

        final RpslObject result = new SsoTranslator().translateAuthToUuid(updateContext, object);

        assertThat(result, is(RpslObject.parse("mntner: TEST-MNT\nauth: SSO username@test.net")));
        verify(updateContext).addSsoTranslationResult("username@test.net", "1234-5678-90AB-DCEF");
    }
}
