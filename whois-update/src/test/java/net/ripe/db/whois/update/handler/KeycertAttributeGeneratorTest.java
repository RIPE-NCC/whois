package net.ripe.db.whois.update.handler;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.ValidationMessages;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContainer;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.keycert.KeyWrapperFactory;
import net.ripe.db.whois.update.keycert.PgpPublicKeyWrapper;
import net.ripe.db.whois.update.keycert.X509CertificateWrapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KeycertAttributeGeneratorTest {
    @Mock private Update update;
    @Mock private UpdateContext updateContext;
    @Mock private KeyWrapperFactory keyWrapperFactory;
    @Mock private Maintainers maintainers;
    @Mock private RpslObjectDao rpslObjectDao;
    @InjectMocks private KeycertAttributeGenerator subject;

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

        validateAttributeType(updatedObject, AttributeType.METHOD, "X509");
        validateAttributeType(updatedObject, AttributeType.FINGERPR, "E7:0F:3B:D4:2F:DD:F5:84:3F:4C:D2:98:78:F3:10:3D");
        validateAttributeType(updatedObject, AttributeType.OWNER, "/C=NL/O=RIPE NCC/OU=Members/CN=zz.example.denis/EMAILADDRESS=denis@ripe.net");
        validateMessages();
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

        validateAttributeType(updatedObject, AttributeType.METHOD, "PGP");
        validateAttributeType(updatedObject, AttributeType.FINGERPR, "884F 8E23 69E5 E6F1 9FB3  63F4 BBCC BB2D 5763 950D");
        validateAttributeType(updatedObject, AttributeType.OWNER, "noreply@ripe.net <noreply@ripe.net>");
        validateMessages();
    }

    @Test
    public void invalid_owner_attribute() {
        RpslObject keycert = RpslObject.parse(
                "key-cert:     PGPKEY-5763950D\n" +
                "owner:        noreply@ripe.net <noreply@ripe.net>\n" +
                "owner:        invalid\n" +
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

        validateAttributeType(updatedObject, AttributeType.METHOD, "PGP");
        validateAttributeType(updatedObject, AttributeType.FINGERPR, "884F 8E23 69E5 E6F1 9FB3  63F4 BBCC BB2D 5763 950D");
        validateAttributeType(updatedObject, AttributeType.OWNER, "noreply@ripe.net <noreply@ripe.net>");
        validateMessages(ValidationMessages.suppliedAttributeReplacedWithGeneratedValue(AttributeType.OWNER));
    }

    @Test
    public void invalid_attribute_is_ignored() {
        RpslObject keycert = RpslObject.parse(
                "key-cert:     PGPKEY-5763950D\n" +
                "invalid-attribute:      invalid\n" +
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

        validateAttributeType(updatedObject, AttributeType.METHOD, "PGP");
        validateAttributeType(updatedObject, AttributeType.FINGERPR, "884F 8E23 69E5 E6F1 9FB3  63F4 BBCC BB2D 5763 950D");
        validateAttributeType(updatedObject, AttributeType.OWNER, "noreply@ripe.net <noreply@ripe.net>");
    }


    @Test
    public void public_key_with_multiple_owners() {
        RpslObject keycert = RpslObject.parse(
                "key-cert:   PGPKEY-5763950D\n" +
                "owner:      invalid\n" +
                "certif:     -----BEGIN PGP PUBLIC KEY BLOCK-----\n" +
                "certif:     Version: GnuPG v1.4.15 (Darwin)\n" +
                "certif:     Comment: GPGTools - http://gpgtools.org\n" +
                "certif:     \n" +
                "certif:     mI0EUqWQnQEEAMKfJ/wK0TuUxgTbDdE5LjDuoDNTNfGr0iw2fNSlsxNME3bpRKW9\n" +
                "certif:     YjgllY7D0wbx4BIdV7MUOaYMDEEPCkApHNwK256Ve2S4WxLCWpyGMiXECs6r4Fj+\n" +
                "certif:     Bkyc2O4bxuXoQocbnKVOiFI0SPpkX1pa0IJleqAq5cz77zK1Ha5OOMErABEBAAG0\n" +
                "certif:     G0FkbWluIFVzZXIgPGFkbWluQHJpcGUubmV0Poi4BBMBAgAiBQJSpZCdAhsDBgsJ\n" +
                "certif:     CAcDAgYVCAIJCgsEFgIDAQIeAQIXgAAKCRDHIn7s6l+7lIpAA/4lExw9e9L2pSsx\n" +
                "certif:     JsDZe0JqukvI6bipuFWa26brAlef+6NtDJjQWvfLAAEoeDBlBchHZ4tpl1Wiyt3J\n" +
                "certif:     kIyeIYDIbb9e/w+romlDlyTQo+8/U1iAdb+TOGzacgbJoykU6OjaGbuMbWn0bu2S\n" +
                "certif:     cmnH08tu0Ydhj04v5yPaSfEYGBF9ULQgSG9zdG1hc3RlciA8aG9zdG1hc3RlckBy\n" +
                "certif:     aXBlLm5ldD6IuAQTAQIAIgUCUqWRMQIbAwYLCQgHAwIGFQgCCQoLBBYCAwECHgEC\n" +
                "certif:     F4AACgkQxyJ+7Opfu5SCzAQAqoRLx76WWa5AcVKUr5BYt2MCPn695iOJ7/Lcxgtq\n" +
                "certif:     wVx8ItIzd0rW5L5iqrreJBSof9W8WfSkHJuwTIzznDzjZZgYaU8eb7zB/g4VAr7b\n" +
                "certif:     ZcfW0b5+IcUI+W1XNdX1ByYsXFkwp/si9QT0VO2rEf+Nxt8zrZ8sygSTWoTWEBWY\n" +
                "certif:     1X24jQRSpZCdAQQAz3jzZHoumwM2tpNQjLdbb/agjeH9zDEb2lMQXSdLx+VdQWeN\n" +
                "certif:     +ywh40Z0beMySXC+4e/pTyliGO5OUly75BiYsowWwLRA17AdR7YNHv+e22mTVf0O\n" +
                "certif:     TkrzUAsU5TAA6ObX6NZWIt3XSCyDOB7m5imOO+vyGwp8p6+6tg2h/oKfedMAEQEA\n" +
                "certif:     AYifBBgBAgAJBQJSpZCdAhsMAAoJEMcifuzqX7uUqBcEAJ4g4bzoRXWJJ6vjuT6w\n" +
                "certif:     UgYCXmEcPSZIwwKPauTj3j7QX46T7u2+yb0qeyK+gFgb4e+iua3KNGb9L82xSEYy\n" +
                "certif:     M8BQ5qhoZnUZjU1DvusYcX7g8Pwe7nntlucKXaMc/1Rgsx2Wpolu59uMLHZ7FUUM\n" +
                "certif:     FUJok98LaNd+xfMg7u+8Fpca\n" +
                "certif:     =tiwc\n" +
                "certif:     -----END PGP PUBLIC KEY BLOCK-----        \n" +
                "mnt-by:       UPD-MNT\n" +
                "notify:       noreply@ripe.net\n" +
                "changed:      noreply@ripe.net 20120213\n" +
                "source:       TEST\n");

        when(keyWrapperFactory.createKeyWrapper(keycert, update, updateContext)).thenReturn(PgpPublicKeyWrapper.parse(keycert));

        final RpslObject updatedObject = subject.generateAttributes(keycert, update, updateContext);

        validateAttributeType(updatedObject, AttributeType.METHOD, "PGP");
        validateAttributeType(updatedObject, AttributeType.FINGERPR, "FBFD 0527 454D 5880 3484  413F C722 7EEC EA5F BB94");
        validateAttributeType(updatedObject, AttributeType.OWNER, "Admin User <admin@ripe.net>", "Hostmaster <hostmaster@ripe.net>");
        validateMessages(ValidationMessages.suppliedAttributeReplacedWithGeneratedValue(AttributeType.OWNER));
    }

    @Test
    public void unknown_certificate() {
        final RpslObject keycert = RpslObject.parse("key-cert: unknown");

        final RpslObject updatedObject = subject.generateAttributes(keycert, update, updateContext);

        assertThat(updatedObject.getAttributes(), hasSize(1));
        validateMessages();
    }

    @Test
    public void person_object_not_updated() {
        final RpslObject person = RpslObject.parse("person: first last\nnic-hdl: FL-TEST");

        final RpslObject updatedObject = subject.generateAttributes(person, update, updateContext);

        assertThat(updatedObject, is(person));
        validateMessages();
    }

    // helper methods

    private void validateAttributeType(final RpslObject rpslObject, final AttributeType attributeType, final String... values) {
        final List attributes = Lists.transform(Arrays.asList(values), new Function<String, RpslAttribute>() {
            @Override
            public RpslAttribute apply(@Nullable String input) {
                return new RpslAttribute(attributeType, input);
            }
        });

        assertThat(rpslObject.findAttributes(attributeType), is(attributes));
    }

    private void validateMessages(final Message... messages) {
        if (messages.length == 0) {
            verify(updateContext, never()).addMessage(any(UpdateContainer.class), any(Message.class));
        } else {
            verify(updateContext, times(messages.length)).addMessage(any(UpdateContainer.class), any(Message.class));
            for (final Message message : messages) {
                verify(updateContext).addMessage(update, message);
            }
        }
    }
}
