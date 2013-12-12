package net.ripe.db.whois.update.sso;

import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(MockitoJUnitRunner.class)
public class SsoTranslatorTest {

    @Mock UpdateContext updateContext;
    @Mock Update update;

    @Test
    public void translate_not_a_maintainer() {
        final RpslObject object = RpslObject.parse("person: test person\nnic-hdl: test-nic");

//        final RpslObject result = new SsoTranslator().translateAuthToUuid(updateContext, object);

        verifyZeroInteractions(updateContext);
//        assertThat(result, is(object));
    }

//    @Test
//    public void translate_to_uuid_username_stored_in_context_already() {
//        final RpslObject object = RpslObject.parse("mntner: TEST-MNT\nauth: SSO username@test.net");
//        when(updateContext.getSsoTranslationResult("username@test.net")).thenReturn("BBBB-1234-CCCC-DDDD");
//
//        final RpslObject result = new SsoTranslator().translateAuthToUsername(updateContext, object);
//
//        assertThat(result, is(RpslObject.parse("mntner: TEST-MNT\nauth: SSO BBBB-1234-CCCC-DDDD")));
//    }
//
//    @Test
//    public void translate_to_uuid_username_not_stored_in_context() {
//        final RpslObject object = RpslObject.parse("mntner: TEST-MNT\nauth: SSO username@test.net");
//        when(updateContext.getSsoTranslationResult("username@test.net")).thenReturn("BBBB-1234-CCCC-DDDD");
//
//        final RpslObject result = new SsoTranslator().translateAuthToUuid(updateContext, object);
//
//        assertThat(result, is(RpslObject.parse("mntner: TEST-MNT\nauth: SSO BBBB-1234-CCCC-DDDD")));
//        verify(updateContext).getSsoTranslationResult("username@test.net"); //TODO change after lookup is implemented
//    }
//
//    @Test
//    public void translate_to_username_uuid_stored_in_context_already() {
//        final RpslObject object = RpslObject.parse("mntner: TEST-MNT\nauth: SSO aadd-2132-aaa-fff");
//
//        when(updateContext.getSsoTranslationResult("aadd-2132-aaa-fff")).thenReturn("username@test.net");
//
//        final RpslObject result = new SsoTranslator().translateAuthToUuid(updateContext, object);
//        assertThat(result, is(RpslObject.parse("mntner: TEST-MNT\nauth: SSO username@test.net")));
//    }
//
//    @Test
//    public void populate_not_maintainer_object() {
//        final RpslObject object = RpslObject.parse("aut-num: AS1234");
//        when(update.getSubmittedObject()).thenReturn(object);
//
//        new SsoTranslator().populate(update, updateContext);
//
//        verifyZeroInteractions(updateContext);
//    }
//
//    @Test
//    public void populate_no_sso_auth() {
//        final RpslObject object = RpslObject.parse("mntner: TEST-MNT\nauth: MD5-PW aaff1232431");
//        when(update.getSubmittedObject()).thenReturn(object);
//
//        new SsoTranslator().populate(update, updateContext);
//
//        verifyZeroInteractions(updateContext);
//    }
//
//    @Test
//    public void populate_sso_auth() {
//        final RpslObject object = RpslObject.parse("mntner: TEST-MNT\nauth: SSO user@test.net");
//        when(update.getSubmittedObject()).thenReturn(object);
//
//        new SsoTranslator().populate(update, updateContext);
//
//        verify(updateContext).addSsoTranslationResult("user@test.net", "1234-5678-90AB-DCEF"); //TODO change after lookup is implemented
//    }
}
