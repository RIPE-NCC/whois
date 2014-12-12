package net.ripe.db.whois.common.sso;


import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SsoHelperTest {
    @Mock private AuthTranslator authTranslator;

    @Test
    public void translate_no_auth_attribute() {
        final RpslObject object = RpslObject.parse("" +
                "person: Test Person\n" +
                "nic-hdl: TP1.TEST\n" +
                "mnt-by: TEST-MNT\n" +
                "source: TEST");
        final RpslObject result = SsoHelper.translateAuth(
                object,
                authTranslator);

        assertThat(result, is(object));
    }

    @Test
    public void translate_no_sso_auth_attribute() {
        final RpslObject object = RpslObject.parse("" +
                "mntner: TEST-MNT\n" +
                "mnt-by: TEST-MNT\n" +
                "auth: MD5-PW aadf\n");

        final RpslObject result = SsoHelper.translateAuth(object, authTranslator);

        assertThat(result, is(object));
    }

    @Test
    public void translate_sso_auth_attribute() {
        final RpslAttribute attribute = new RpslAttribute(AttributeType.AUTH, "SSO test@test.net");
        final RpslAttribute translated = new RpslAttribute(AttributeType.AUTH, "SSO bbbb-aaaa-cccc-dddd");
        when(authTranslator.translate("SSO", "test@test.net", attribute)).thenReturn(translated);

        final RpslObject object = RpslObject.parse("" +
                "mntner: TEST-MNT\n" +
                "mnt-by: TEST-MNT\n" +
                "auth: SSO test@test.net\n");

        final RpslObject result = SsoHelper.translateAuth(object, authTranslator);

        assertThat(result.toString(), is("" +
                "mntner:         TEST-MNT\n" +
                "mnt-by:         TEST-MNT\n" +
                "auth:           SSO bbbb-aaaa-cccc-dddd\n"));
    }

    @Test
    public void translate_many_sso_attributes() {
        final RpslAttribute attribute = new RpslAttribute(AttributeType.AUTH, "SSO test@test.net");
        final RpslAttribute attribute2 = new RpslAttribute(AttributeType.AUTH, "SSO another@test.net");
        final RpslAttribute translated1 = new RpslAttribute(AttributeType.AUTH, "SSO bbbb-aaaa-cccc-dddd");
        final RpslAttribute translated2 = new RpslAttribute(AttributeType.AUTH, "SSO eeee-ffff-eeee-ffff");
        when(authTranslator.translate("SSO", "test@test.net", attribute)).thenReturn(translated1);
        when(authTranslator.translate("SSO", "another@test.net", attribute2)).thenReturn(translated2);

        final RpslObject object = RpslObject.parse("" +
                "mntner: TEST-MNT\n" +
                "mnt-by: TEST-MNT\n" +
                "auth: SSO test@test.net\n" +
                "auth: SSO another@test.net\n");

        final RpslObject result = SsoHelper.translateAuth(object, authTranslator);

        assertThat(result.toString(), is("" +
                "mntner:         TEST-MNT\n" +
                "mnt-by:         TEST-MNT\n" +
                "auth:           SSO bbbb-aaaa-cccc-dddd\n" +
                "auth:           SSO eeee-ffff-eeee-ffff\n"));
    }
}
