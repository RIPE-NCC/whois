package net.ripe.db.whois.update.keycert;

import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.UpdateContainer;
import net.ripe.db.whois.update.domain.UpdateContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class KeyWrapperFactoryTest {
    @Mock UpdateContainer updateContainer;
    @Mock UpdateContext updateContext;

    @InjectMocks KeyWrapperFactory subject;

    @Test
    public void createKeyWrapper_invalid_pgp_key() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "key-cert:     PGPKEY-28F6CD6C\n" +
                "method:       PGP\n" +
                "owner:        DFN-CERT (2003), ENCRYPTION Key\n" +
                "fingerpr:     1C40 500A 1DC4 A8D8 D3EA  ABF9 EE99 1EE2 28F6 CD6C\n" +
                "certif:       -----BEGIN PGP PUBLIC KEY BLOCK-----\n" +
                "certif:       Version: GnuPG v1.4.11 (Darwin)\n" +
                "certif:\n" +
                "certif:       -----END PGP PUBLIC KEY BLOCK-----\n" +
                "mnt-by:       UPD-MNT\n" +
                "notify:       eshryane@ripe.net\n" +
                "changed:      eshryane@ripe.net 20120213\n" +
                "source:       TEST\n"
        );

        final KeyWrapper keyWrapper = subject.createKeyWrapper(rpslObject, updateContainer, updateContext);
        assertNull(keyWrapper);
    }

    @Test
    public void createKeyWrapper_invalid_x509_key() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "key-cert:        AUTO-1\n" +
                "method:          X509\n" +
                "owner:           /CN=4a96eecf-9d1c-4e12-8add-5ea5522976d8\n" +
                "fingerpr:        82:7C:C5:40:D1:DB:AE:6A:FA:F8:40:3E:3C:9C:27:7C\n" +
                "certif:          -----BEGIN CERTIFICATE-----\n" +
                "certif:          -----END CERTIFICATE-----\n" +
                "mnt-by:          UPD-MNT\n" +
                "remarks:         remark\n" +
                "changed:         noreply@ripe.net 20121001\n" +
                "source:          TEST\n"
        );

        final KeyWrapper keyWrapper = subject.createKeyWrapper(rpslObject, updateContainer, updateContext);
        assertNull(keyWrapper);
    }

    @Test
    public void createKeyWrapper_pgp_key() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "key-cert:     X509-1\n" +
                "method:       X509\n" +
                "owner:        /C=NL/O=RIPE NCC/OU=Members/CN=zz.example.denis/Email=denis@ripe.net\n" +
                "fingerpr:     E7:0F:3B:D4:2F:DD:F5:84:3F:4C:D2:98:78:F3:10:3D\n" +
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
                "source:       TEST\n"
        );

        final KeyWrapper keyWrapper = subject.createKeyWrapper(rpslObject, updateContainer, updateContext);
        assertTrue(keyWrapper instanceof X509CertificateWrapper);
    }

    @Test
    public void createKeyWrapper_x509_key() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "key-cert:       PGPKEY-81CCF97D\n" +
                "method:         PGP\n" +
                "owner:          Unknown <unread@ripe.net>\n" +
                "fingerpr:       EDDF 375A B830 D1BB 26E5  ED3B 76CA 91EF 81CC F97D\n" +
                "certif:         -----BEGIN PGP PUBLIC KEY BLOCK-----\n" +
                "certif:         Version: GnuPG v1.4.12 (Darwin)\n" +
                "certif:         Comment: GPGTools - http://gpgtools.org\n" +
                "certif:\n" +
                "certif:         mQENBFC0yvUBCACn2JKwa5e8Sj3QknEnD5ypvmzNWwYbDhLjmD06wuZxt7Wpgm4+\n" +
                "certif:         yO68swuow09jsrh2DAl2nKQ7YaODEipis0d4H2i0mSswlsC7xbmpx3dRP/yOu4WH\n" +
                "certif:         2kZciQYxC1NY9J3CNIZxgw6zcghJhtm+LT7OzPS8s3qp+w5nj+vKY09A+BK8yHBN\n" +
                "certif:         E+VPeLOAi+D97s+Da/UZWkZxFJHdV+cAzQ05ARqXKXeadfFdbkx0Eq2R0RZm9R+L\n" +
                "certif:         A9tPUhtw5wk1gFMsN7c5NKwTUQ/0HTTgA5eyKMnTKAdwhIY5/VDxUd1YprnK+Ebd\n" +
                "certif:         YNZh+L39kqoUL6lqeu0dUzYp2Ll7R2IURaXNABEBAAG0I25vcmVwbHlAcmlwZS5u\n" +
                "certif:         ZXQgPG5vcmVwbHlAcmlwZS5uZXQ+iQE4BBMBAgAiBQJQtMr1AhsDBgsJCAcDAgYV\n" +
                "certif:         CAIJCgsEFgIDAQIeAQIXgAAKCRC7zLstV2OVDdjSCACYAyyWr83Df/zzOWGP+qMF\n" +
                "certif:         Vukj8xhaM5f5MGb9FjMKClo6ezT4hLjQ8hfxAAZxndwAXoz46RbDUsAe/aBwdwKB\n" +
                "certif:         0owcacoaxUd0i+gVEn7CBHPVUfNIuNemcrf1N7aqBkpBLf+NINZ2+3c3t14k1BGe\n" +
                "certif:         xCInxEqHnq4zbUmunCNYjHoKbUj6Aq7janyC7W1MIIAcOY9/PvWQyf3VnERQImgt\n" +
                "certif:         0fhiekCr6tRbANJ4qFoJQSM/ACoVkpDvb5PHZuZXf/v+XB1DV7gZHjJeZA+Jto5Z\n" +
                "certif:         xrmS5E+HEHVBO8RsBOWDlmWCcZ4k9olxp7/z++mADXPprmLaK8vjQmiC2q/KOTVA\n" +
                "certif:         uQENBFC0yvUBCADTYI6i4baHAkeY2lR2rebpTu1nRHbIET20II8/ZmZDK8E2Lwyv\n" +
                "certif:         eWold6pAWDq9E23J9xAWL4QUQRQ4V+28+lknMySXbU3uFLXGAs6W9PrZXGcmy/12\n" +
                "certif:         pZ+82hHckh+jN9xUTtF89NK/wHh09SAxDa/ST/z/Dj0k3pQWzgBdi36jwEFtHhck\n" +
                "certif:         xFwGst5Cv8SLvA9/DaP75m9VDJsmsSwh/6JqMUb+hY71Dr7oxlIFLdsREsFVzVec\n" +
                "certif:         YHsKINlZKh60dA/Br+CC7fClBycEsR4Z7akw9cPLWIGnjvw2+nq9miE005QLqRy4\n" +
                "certif:         dsrwydbMGplaE/mZc0d2WnNyiCBXAHB5UhmZABEBAAGJAR8EGAECAAkFAlC0yvUC\n" +
                "certif:         GwwACgkQu8y7LVdjlQ1GMAgAgUohj4q3mAJPR6d5pJ8Ig5E3QK87z3lIpgxHbYR4\n" +
                "certif:         HNaR0NIV/GAt/uca11DtIdj3kBAj69QSPqNVRqaZja3NyhNWQM4OPDWKIUZfolF3\n" +
                "certif:         eY2q58kEhxhz3JKJt4z45TnFY2GFGqYwFPQ94z1S9FOJCifL/dLpwPBSKucCac9y\n" +
                "certif:         6KiKfjEehZ4VqmtM/SvN23GiI/OOdlHL/xnU4NgZ90GHmmQFfdUiX36jWK99LBqC\n" +
                "certif:         RNW8V2MV+rElPVRHev+nw7vgCM0ewXZwQB/bBLbBrayx8LzGtMvAo4kDJ1kpQpip\n" +
                "certif:         a/bmKCK6E+Z9aph5uoke8bKoybIoQ2K3OQ4Mh8yiI+AjiQ==\n" +
                "certif:         =HQmg\n" +
                "certif:         -----END PGP PUBLIC KEY BLOCK-----\n" +
                "mnt-by:         UPD-MNT\n" +
                "notify:         noreply@ripe.net\n" +
                "changed:        noreply@ripe.net 20120213\n" +
                "source:         TEST\n"
        );

        final KeyWrapper keyWrapper = subject.createKeyWrapper(rpslObject, updateContainer, updateContext);
        assertTrue(keyWrapper instanceof PgpPublicKeyWrapper);
    }
}
