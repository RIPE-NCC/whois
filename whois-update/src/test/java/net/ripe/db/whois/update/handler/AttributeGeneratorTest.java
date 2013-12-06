package net.ripe.db.whois.update.handler;

import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.keycert.KeyWrapperFactory;
import net.ripe.db.whois.update.keycert.PgpPublicKeyWrapper;
import net.ripe.db.whois.update.keycert.X509CertificateWrapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AttributeGeneratorTest {
    @Mock Update update;
    @Mock UpdateContext updateContext;
    @Mock KeyWrapperFactory keyWrapperFactory;
    @InjectMocks AttributeGenerator subject;

    @Test
    public void generate_attributes_for_x509_certificate() {
        final RpslObject keycert = RpslObject.parse(
                "key-cert:     X509-1\n" +
                "certif:       -----BEGIN CERTIFICATE-----\n" +
                "certif:       MIIC8DCCAlmgAwIBAgICBIQwDQYJKoZIhvcNAQEEBQAwXjELMAkGA1UEBhMCTkwx\n" +
                "certif:       ETAPBgNVBAoTCFJJUEUgTkNDMR0wGwYDVQQLExRSSVBFIE5DQyBMSVIgTmV0d29y\n" +
                "certif:       azEdMBsGA1UEAxMUUklQRSBOQ0MgTElSIFJvb3QgQ0EwHhcNMDQwOTI3MTI1NDAx\n" +
                "certif:       WhcNMDUwOTI3MTI1NDAxWjBsMQswCQYDVQQGEwJOTDERMA8GA1UEChMIUklQRSBO\n" +
                "certif:       Q0MxEDAOBgNVBAsTB01lbWJlcnMxGTAXBgNVBAMTEHp6LmV4YW1wbGUuZGVuaXMx\n" +
                "certif:       HTAbBgkqhkiG9w0BCQEWDmRlbmlzQHJpcGUubmV0MFwwDQYJKoZIhvcNAQEBBQAD\n" +
                "certif:       SwAwSAJBAKdZEYY0pCb5updB808+y8CjNsnraQ/3sBL3/184TqD4AE/TSOdZJ2oU\n" +
                "certif:       HmEpfm6ECkbHOJ1NtMwRjAbkk/rWiBMCAwEAAaOB8jCB7zAJBgNVHRMEAjAAMBEG\n" +
                "certif:       CWCGSAGG+EIBAQQEAwIFoDALBgNVHQ8EBAMCBeAwGgYJYIZIAYb4QgENBA0WC1JJ\n" +
                "certif:       UEUgTkNDIENBMB0GA1UdDgQWBBQk0+qAmXPImzyVTOGARwNPHAX0GTCBhgYDVR0j\n" +
                "certif:       BH8wfYAUI8r2d8CnSt174cfhUw2KNga3Px6hYqRgMF4xCzAJBgNVBAYTAk5MMREw\n" +
                "certif:       DwYDVQQKEwhSSVBFIE5DQzEdMBsGA1UECxMUUklQRSBOQ0MgTElSIE5ldHdvcmsx\n" +
                "certif:       HTAbBgNVBAMTFFJJUEUgTkNDIExJUiBSb290IENBggEAMA0GCSqGSIb3DQEBBAUA\n" +
                "certif:       A4GBAAxojauJHRm3XtPfOCe4B5iz8uVt/EeKhM4gjHGJrUbkAlftLJYUe2Vx8HcH\n" +
                "certif:       O4b+9E098Rt6MfFF+1dYNz7/NgiIpR7BlmdWzPCyhfgxJxTM9m9B7B/6noDU+aaf\n" +
                "certif:       w0L5DyjKGe0dbjMKtaDdgQhxj8aBHNnQVbS9Oqhvmc65XgNi\n" +
                "certif:       -----END CERTIFICATE-----\n" +
                "mnt-by:       UPD-MNT\n" +
                "changed:      noreply@ripe.net 20040927\n" +
                "source:       TEST\n");

        when(keyWrapperFactory.createKeyWrapper(keycert, update, updateContext)).thenReturn(X509CertificateWrapper.parse(keycert));

        final RpslObject updatedObject = subject.generateAttributes(keycert, update, updateContext);

        assertThat(updatedObject.findAttribute(AttributeType.METHOD).getCleanValue().toString(), is("X509"));
        assertThat(updatedObject.findAttribute(AttributeType.FINGERPR).getCleanValue().toString(), is("E7:0F:3B:D4:2F:DD:F5:84:3F:4C:D2:98:78:F3:10:3D"));
        assertThat(updatedObject.findAttribute(AttributeType.OWNER).getCleanValue().toString(), is("/C=NL/O=RIPE NCC/OU=Members/CN=zz.example.denis/EMAILADDRESS=denis@ripe.net"));
    }

    @Test
    public void generate_attributes_for_pgp_certificate() throws Exception {
        final RpslObject keycert = RpslObject.parse(
                "key-cert:     PGPKEY-5763950D\n" +
                "certif:       -----BEGIN PGP PUBLIC KEY BLOCK-----\n" +
                "certif:       Version: GnuPG v1.4.12 (Darwin)\n" +
                "certif:       \n" +
                "certif:       mQENBFC0yvUBCACn2JKwa5e8Sj3QknEnD5ypvmzNWwYbDhLjmD06wuZxt7Wpgm4+\n" +
                "certif:       yO68swuow09jsrh2DAl2nKQ7YaODEipis0d4H2i0mSswlsC7xbmpx3dRP/yOu4WH\n" +
                "certif:       2kZciQYxC1NY9J3CNIZxgw6zcghJhtm+LT7OzPS8s3qp+w5nj+vKY09A+BK8yHBN\n" +
                "certif:       E+VPeLOAi+D97s+Da/UZWkZxFJHdV+cAzQ05ARqXKXeadfFdbkx0Eq2R0RZm9R+L\n" +
                "certif:       A9tPUhtw5wk1gFMsN7c5NKwTUQ/0HTTgA5eyKMnTKAdwhIY5/VDxUd1YprnK+Ebd\n" +
                "certif:       YNZh+L39kqoUL6lqeu0dUzYp2Ll7R2IURaXNABEBAAG0I25vcmVwbHlAcmlwZS5u\n" +
                "certif:       ZXQgPG5vcmVwbHlAcmlwZS5uZXQ+iQE4BBMBAgAiBQJQtMr1AhsDBgsJCAcDAgYV\n" +
                "certif:       CAIJCgsEFgIDAQIeAQIXgAAKCRC7zLstV2OVDdjSCACYAyyWr83Df/zzOWGP+qMF\n" +
                "certif:       Vukj8xhaM5f5MGb9FjMKClo6ezT4hLjQ8hfxAAZxndwAXoz46RbDUsAe/aBwdwKB\n" +
                "certif:       0owcacoaxUd0i+gVEn7CBHPVUfNIuNemcrf1N7aqBkpBLf+NINZ2+3c3t14k1BGe\n" +
                "certif:       xCInxEqHnq4zbUmunCNYjHoKbUj6Aq7janyC7W1MIIAcOY9/PvWQyf3VnERQImgt\n" +
                "certif:       0fhiekCr6tRbANJ4qFoJQSM/ACoVkpDvb5PHZuZXf/v+XB1DV7gZHjJeZA+Jto5Z\n" +
                "certif:       xrmS5E+HEHVBO8RsBOWDlmWCcZ4k9olxp7/z++mADXPprmLaK8vjQmiC2q/KOTVA\n" +
                "certif:       uQENBFC0yvUBCADTYI6i4baHAkeY2lR2rebpTu1nRHbIET20II8/ZmZDK8E2Lwyv\n" +
                "certif:       eWold6pAWDq9E23J9xAWL4QUQRQ4V+28+lknMySXbU3uFLXGAs6W9PrZXGcmy/12\n" +
                "certif:       pZ+82hHckh+jN9xUTtF89NK/wHh09SAxDa/ST/z/Dj0k3pQWzgBdi36jwEFtHhck\n" +
                "certif:       xFwGst5Cv8SLvA9/DaP75m9VDJsmsSwh/6JqMUb+hY71Dr7oxlIFLdsREsFVzVec\n" +
                "certif:       YHsKINlZKh60dA/Br+CC7fClBycEsR4Z7akw9cPLWIGnjvw2+nq9miE005QLqRy4\n" +
                "certif:       dsrwydbMGplaE/mZc0d2WnNyiCBXAHB5UhmZABEBAAGJAR8EGAECAAkFAlC0yvUC\n" +
                "certif:       GwwACgkQu8y7LVdjlQ1GMAgAgUohj4q3mAJPR6d5pJ8Ig5E3QK87z3lIpgxHbYR4\n" +
                "certif:       HNaR0NIV/GAt/uca11DtIdj3kBAj69QSPqNVRqaZja3NyhNWQM4OPDWKIUZfolF3\n" +
                "certif:       eY2q58kEhxhz3JKJt4z45TnFY2GFGqYwFPQ94z1S9FOJCifL/dLpwPBSKucCac9y\n" +
                "certif:       6KiKfjEehZ4VqmtM/SvN23GiI/OOdlHL/xnU4NgZ90GHmmQFfdUiX36jWK99LBqC\n" +
                "certif:       RNW8V2MV+rElPVRHev+nw7vgCM0ewXZwQB/bBLbBrayx8LzGtMvAo4kDJ1kpQpip\n" +
                "certif:       a/bmKCK6E+Z9aph5uoke8bKoybIoQ2K3OQ4Mh8yiI+AjiQ==\n" +
                "certif:       =HQmg\n" +
                "certif:       -----END PGP PUBLIC KEY BLOCK-----" +
                "mnt-by:       UPD-MNT\n" +
                "notify:       noreply@ripe.net\n" +
                "changed:      noreply@ripe.net 20120213\n" +
                "source:       TEST\n");

        when(keyWrapperFactory.createKeyWrapper(keycert, update, updateContext)).thenReturn(PgpPublicKeyWrapper.parse(keycert));

        final RpslObject updatedObject = subject.generateAttributes(keycert, update, updateContext);

        assertThat(updatedObject.findAttribute(AttributeType.METHOD).getCleanValue().toString(), is("PGP"));
        assertThat(updatedObject.findAttribute(AttributeType.FINGERPR).getValue(), is("884F 8E23 69E5 E6F1 9FB3  63F4 BBCC BB2D 5763 950D"));
        assertThat(updatedObject.findAttribute(AttributeType.OWNER).getCleanValue().toString(), is("noreply@ripe.net <noreply@ripe.net>"));
    }

    @Test
    public void multiple_owner_attributes() {
        RpslObject keycert = RpslObject.parse(
                "key-cert:     PGPKEY-5763950D\n" +
                "owner:        one\n" +
                "owner:        two\n" +
                "certif:       -----BEGIN PGP PUBLIC KEY BLOCK-----\n" +
                "certif:       Version: GnuPG v1.4.12 (Darwin)\n" +
                "certif:       \n" +
                "certif:       mQENBFC0yvUBCACn2JKwa5e8Sj3QknEnD5ypvmzNWwYbDhLjmD06wuZxt7Wpgm4+\n" +
                "certif:       yO68swuow09jsrh2DAl2nKQ7YaODEipis0d4H2i0mSswlsC7xbmpx3dRP/yOu4WH\n" +
                "certif:       2kZciQYxC1NY9J3CNIZxgw6zcghJhtm+LT7OzPS8s3qp+w5nj+vKY09A+BK8yHBN\n" +
                "certif:       E+VPeLOAi+D97s+Da/UZWkZxFJHdV+cAzQ05ARqXKXeadfFdbkx0Eq2R0RZm9R+L\n" +
                "certif:       A9tPUhtw5wk1gFMsN7c5NKwTUQ/0HTTgA5eyKMnTKAdwhIY5/VDxUd1YprnK+Ebd\n" +
                "certif:       YNZh+L39kqoUL6lqeu0dUzYp2Ll7R2IURaXNABEBAAG0I25vcmVwbHlAcmlwZS5u\n" +
                "certif:       ZXQgPG5vcmVwbHlAcmlwZS5uZXQ+iQE4BBMBAgAiBQJQtMr1AhsDBgsJCAcDAgYV\n" +
                "certif:       CAIJCgsEFgIDAQIeAQIXgAAKCRC7zLstV2OVDdjSCACYAyyWr83Df/zzOWGP+qMF\n" +
                "certif:       Vukj8xhaM5f5MGb9FjMKClo6ezT4hLjQ8hfxAAZxndwAXoz46RbDUsAe/aBwdwKB\n" +
                "certif:       0owcacoaxUd0i+gVEn7CBHPVUfNIuNemcrf1N7aqBkpBLf+NINZ2+3c3t14k1BGe\n" +
                "certif:       xCInxEqHnq4zbUmunCNYjHoKbUj6Aq7janyC7W1MIIAcOY9/PvWQyf3VnERQImgt\n" +
                "certif:       0fhiekCr6tRbANJ4qFoJQSM/ACoVkpDvb5PHZuZXf/v+XB1DV7gZHjJeZA+Jto5Z\n" +
                "certif:       xrmS5E+HEHVBO8RsBOWDlmWCcZ4k9olxp7/z++mADXPprmLaK8vjQmiC2q/KOTVA\n" +
                "certif:       uQENBFC0yvUBCADTYI6i4baHAkeY2lR2rebpTu1nRHbIET20II8/ZmZDK8E2Lwyv\n" +
                "certif:       eWold6pAWDq9E23J9xAWL4QUQRQ4V+28+lknMySXbU3uFLXGAs6W9PrZXGcmy/12\n" +
                "certif:       pZ+82hHckh+jN9xUTtF89NK/wHh09SAxDa/ST/z/Dj0k3pQWzgBdi36jwEFtHhck\n" +
                "certif:       xFwGst5Cv8SLvA9/DaP75m9VDJsmsSwh/6JqMUb+hY71Dr7oxlIFLdsREsFVzVec\n" +
                "certif:       YHsKINlZKh60dA/Br+CC7fClBycEsR4Z7akw9cPLWIGnjvw2+nq9miE005QLqRy4\n" +
                "certif:       dsrwydbMGplaE/mZc0d2WnNyiCBXAHB5UhmZABEBAAGJAR8EGAECAAkFAlC0yvUC\n" +
                "certif:       GwwACgkQu8y7LVdjlQ1GMAgAgUohj4q3mAJPR6d5pJ8Ig5E3QK87z3lIpgxHbYR4\n" +
                "certif:       HNaR0NIV/GAt/uca11DtIdj3kBAj69QSPqNVRqaZja3NyhNWQM4OPDWKIUZfolF3\n" +
                "certif:       eY2q58kEhxhz3JKJt4z45TnFY2GFGqYwFPQ94z1S9FOJCifL/dLpwPBSKucCac9y\n" +
                "certif:       6KiKfjEehZ4VqmtM/SvN23GiI/OOdlHL/xnU4NgZ90GHmmQFfdUiX36jWK99LBqC\n" +
                "certif:       RNW8V2MV+rElPVRHev+nw7vgCM0ewXZwQB/bBLbBrayx8LzGtMvAo4kDJ1kpQpip\n" +
                "certif:       a/bmKCK6E+Z9aph5uoke8bKoybIoQ2K3OQ4Mh8yiI+AjiQ==\n" +
                "certif:       =HQmg\n" +
                "certif:       -----END PGP PUBLIC KEY BLOCK-----\n" +
                "mnt-by:       UPD-MNT\n" +
                "notify:       noreply@ripe.net\n" +
                "changed:      noreply@ripe.net 20120213\n" +
                "source:       TEST\n");
        when(keyWrapperFactory.createKeyWrapper(keycert, update, updateContext)).thenReturn(PgpPublicKeyWrapper.parse(keycert));

        final RpslObject updatedObject = subject.generateAttributes(keycert, update, updateContext);

        assertThat(updatedObject.findAttribute(AttributeType.METHOD).getCleanValue().toString(), is("PGP"));
        assertThat(updatedObject.findAttribute(AttributeType.FINGERPR).getValue(), is("884F 8E23 69E5 E6F1 9FB3  63F4 BBCC BB2D 5763 950D"));
        assertThat(updatedObject.findAttribute(AttributeType.OWNER).getCleanValue().toString(), is("noreply@ripe.net <noreply@ripe.net>"));
    }

    @Test
    public void unknown_certificate() {
        final RpslObject keycert = RpslObject.parse("key-cert: unknown");

        final RpslObject updatedObject = subject.generateAttributes(keycert, update, updateContext);

        assertThat(updatedObject.getAttributes().size(), is(1));
    }

    @Test
    public void person_object_not_updated() {
        final RpslObject person = RpslObject.parse("person: first last\nnic-hdl: FL-TEST");

        final RpslObject updatedObject = subject.generateAttributes(person, update, updateContext);

        assertThat(updatedObject, is(person));
    }
}
