package net.ripe.db.whois.update.authentication.credential;

import com.google.common.base.Charsets;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.update.domain.PgpCredential;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.log.LoggerContext;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.dao.EmptyResultDataAccessException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PgpCredentialValidatorTest {
    @Mock private PreparedUpdate update;
    @Mock private UpdateContext updateContext;
    @Mock private RpslObjectDao rpslObjectDao;
    @Mock private DateTimeProvider dateTimeProvider;
    @Mock private LoggerContext loggerContext;
    @InjectMocks private PgpCredentialValidator subject;
    private RpslObject keycertObject = RpslObject.parse("" +
            "key-cert:       PGPKEY-67ABAB48\n" +
            "method:         PGP\n" +
            "owner:          Test Person <noreply@ripe.net>\n" +
            "fingerpr:       0FF6 4721 FAF7 D971 A04E  897A B15F D4B6 67AB AB48\n" +
            "certif:         -----BEGIN PGP PUBLIC KEY BLOCK-----\n" +
            "certif:         Version: GnuPG v1.4.13 (Darwin)\n" +
            "certif:         Comment: GPGTools - http://gpgtools.org\n" +
            "certif:         \n" +
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
            "notify:         noreply@ripe.net\n" +
            "mnt-by:         TEST-MNT\n" +
            "changed:        noreply@ripe.net 20070917\n" +
            "source:         RIPE\n");

    @Before
    public void setup() {
        when(dateTimeProvider.getCurrentDateTime()).thenReturn(LocalDateTime.now());
    }

    @Test
    public void authenticateExistingRpslObject() throws Exception {
        final String message = "" +
                "-----BEGIN PGP SIGNED MESSAGE-----\n" +
                "Hash: SHA1\n" +
                "\n" +
                "inetnum:        213.168.127.96 - 213.168.127.10\n" +
                "netname:        NETNAME\n" +
                "descr:          Description\n" +
                "country:        DE\n" +
                "admin-c:        TEST-RIPE\n" +
                "tech-c:         TEST-RIPE\n" +
                "status:         ASSIGNED PA\n" +
                "mnt-by:         TEST-MNT\n" +
                "mnt-lower:      TEST-MNT\n" +
                "changed:        foo@test.de 20090129\n" +
                "source:         RIPE\n" +
                "delete:         reason\n" +
                "-----BEGIN PGP SIGNATURE-----\n" +
                "Version: GnuPG v1.4.13 (Darwin)\n" +
                "Comment: GPGTools - http://gpgtools.org\n" +
                "\n" +
                "iQEcBAEBAgAGBQJRb/OqAAoJELvMuy1XY5UNovsH+QHj69vbPg7dLw49TY4Giqff\n" +
                "+W56/Z67iedP8dRJkhbSt4MzSBJaJi7VPrJDZjw7qXFmBDgHWHyDVCI6fJKUXFHA\n" +
                "d41lS0cs/4kA2olfhjfXB3djUAPyUuldT7nNyuZFHi/oTAjao7siCktPLgn5ZN0E\n" +
                "/IUpBvI5KNgMLpABfR8s5H8htTxGtaiOqVpfH7pc2VsxbC5PDUzjOOfb0nOmsegn\n" +
                "PAhZ3TnMmO78rBsuILfkYqaR9s9E+M7R7nMIPBchALaoUKYyzUxt1Ic/WHn1ECcN\n" +
                "hQvAyyc1xARDiOcJYziYChDxFX6zDCYIadDnIhfw3590KDNyl9ZjjkycK6NvlqA=\n" +
                "=tOGn\n" +
                "-----END PGP SIGNATURE-----";

        final PgpCredential offeredCredential = PgpCredential.createOfferedCredential(message);
        final PgpCredential knownCredential = PgpCredential.createKnownCredential("PGPKEY-67ABAB48");


        when(rpslObjectDao.getByKey(ObjectType.KEY_CERT, keycertObject.getKey().toString())).thenReturn(keycertObject);

        assertThat(subject.hasValidCredential(update, updateContext, Sets.newHashSet(offeredCredential), knownCredential), is(true));
        verify(loggerContext).logString(any(Update.class), anyString(), anyString());
    }

    @Test
    public void authenticateSignatureVerifyFailed() throws Exception {
        final PgpCredential offeredCredential = PgpCredential.createOfferedCredential("" +
                "-----BEGIN PGP SIGNED MESSAGE-----\n" +
                "Hash: SHA1\n\n" +
                "inetnum:        213.168.127.96 - 213.168.127.103\n" +
                "-----BEGIN PGP SIGNATURE-----\n" +
                "Version: GnuPG v1.4.11 (MingW32) - WinPT 1.4.3\n" +
                "Charset: UTF-8\n" +
                "Comment: GnuPT 2.7.2\n\n" +
                "iEYEARECAAYFAk+r2BgACgkQesVNFxdpXQoLPQCgq4dt/+PymmQZ8/AX+0HJfbGL\n" +
                "LEwAn2zxSKmMKSLZVbRLxwgVhDQGn+5o\n" +
                "=g9vN\n" +
                "-----END PGP SIGNATURE-----\n");

        final PgpCredential knownCredential = PgpCredential.createKnownCredential("PGPKEY-67ABAB48");

        when(rpslObjectDao.getByKey(ObjectType.KEY_CERT, keycertObject.getKey().toString())).thenReturn(keycertObject);

        boolean result = subject.hasValidCredential(update, updateContext, Sets.newHashSet(offeredCredential), knownCredential);

        assertThat(result, is(false));
    }

    @Test
    public void authenticateKeycertNotFound() throws Exception {
        final String message = "" +
                "-----BEGIN PGP SIGNED MESSAGE-----\n" +
                "Hash: SHA1\n" +
                "\n" +
                "inetnum:        213.168.127.96 - 213.168.127.10\n" +
                "netname:        NETNAME\n" +
                "descr:          Description\n" +
                "country:        DE\n" +
                "admin-c:        TEST-RIPE\n" +
                "tech-c:         TEST-RIPE\n" +
                "status:         ASSIGNED PA\n" +
                "mnt-by:         TEST-MNT\n" +
                "mnt-lower:      TEST-MNT\n" +
                "changed:        foo@test.de 20090129\n" +
                "source:         RIPE\n" +
                "delete:         reason\n" +
                "-----BEGIN PGP SIGNATURE-----\n" +
                "Version: GnuPG v1.4.13 (Darwin)\n" +
                "Comment: GPGTools - http://gpgtools.org\n" +
                "\n" +
                "iQEcBAEBAgAGBQJRb/OqAAoJELvMuy1XY5UNovsH+QHj69vbPg7dLw49TY4Giqff\n" +
                "+W56/Z67iedP8dRJkhbSt4MzSBJaJi7VPrJDZjw7qXFmBDgHWHyDVCI6fJKUXFHA\n" +
                "d41lS0cs/4kA2olfhjfXB3djUAPyUuldT7nNyuZFHi/oTAjao7siCktPLgn5ZN0E\n" +
                "/IUpBvI5KNgMLpABfR8s5H8htTxGtaiOqVpfH7pc2VsxbC5PDUzjOOfb0nOmsegn\n" +
                "PAhZ3TnMmO78rBsuILfkYqaR9s9E+M7R7nMIPBchALaoUKYyzUxt1Ic/WHn1ECcN\n" +
                "hQvAyyc1xARDiOcJYziYChDxFX6zDCYIadDnIhfw3590KDNyl9ZjjkycK6NvlqA=\n" +
                "=tOGn\n" +
                "-----END PGP SIGNATURE-----";

        final PgpCredential offeredCredential = PgpCredential.createOfferedCredential(message);
        final PgpCredential knownCredential = PgpCredential.createKnownCredential("PGPKEY-67ABAB48");

        when(rpslObjectDao.getByKey(ObjectType.KEY_CERT, keycertObject.getKey().toString())).thenThrow(new EmptyResultDataAccessException(1));

        boolean result = subject.hasValidCredential(update, updateContext, Sets.newHashSet(offeredCredential), knownCredential);

        assertThat(result, is(false));
    }

    @Test
    public void authenticateKnownCredentialIsInvalid() {
        final String message = "" +
                "-----BEGIN PGP SIGNED MESSAGE-----\n" +
                "Hash: SHA1\n" +
                "\n" +
                "inetnum:        213.168.127.96 - 213.168.127.10\n" +
                "netname:        NETNAME\n" +
                "descr:          Description\n" +
                "country:        DE\n" +
                "admin-c:        TEST-RIPE\n" +
                "tech-c:         TEST-RIPE\n" +
                "status:         ASSIGNED PA\n" +
                "mnt-by:         TEST-MNT\n" +
                "mnt-lower:      TEST-MNT\n" +
                "changed:        foo@test.de 20090129\n" +
                "source:         RIPE\n" +
                "delete:         reason\n" +
                "-----BEGIN PGP SIGNATURE-----\n" +
                "Version: GnuPG v1.4.13 (Darwin)\n" +
                "Comment: GPGTools - http://gpgtools.org\n" +
                "\n" +
                "iQEcBAEBAgAGBQJRb/OqAAoJELvMuy1XY5UNovsH+QHj69vbPg7dLw49TY4Giqff\n" +
                "+W56/Z67iedP8dRJkhbSt4MzSBJaJi7VPrJDZjw7qXFmBDgHWHyDVCI6fJKUXFHA\n" +
                "d41lS0cs/4kA2olfhjfXB3djUAPyUuldT7nNyuZFHi/oTAjao7siCktPLgn5ZN0E\n" +
                "/IUpBvI5KNgMLpABfR8s5H8htTxGtaiOqVpfH7pc2VsxbC5PDUzjOOfb0nOmsegn\n" +
                "PAhZ3TnMmO78rBsuILfkYqaR9s9E+M7R7nMIPBchALaoUKYyzUxt1Ic/WHn1ECcN\n" +
                "hQvAyyc1xARDiOcJYziYChDxFX6zDCYIadDnIhfw3590KDNyl9ZjjkycK6NvlqA=\n" +
                "=tOGn\n" +
                "-----END PGP SIGNATURE-----";

        final PgpCredential offeredCredential = PgpCredential.createOfferedCredential(message);
        final PgpCredential knownCredential = PgpCredential.createKnownCredential("PGPKEY-67ABAB48");

        keycertObject = new RpslObjectBuilder(keycertObject).removeAttributeType(AttributeType.CERTIF).get();

        when(rpslObjectDao.getByKey(ObjectType.KEY_CERT, keycertObject.getKey().toString())).thenReturn(keycertObject);

        boolean result = subject.hasValidCredential(update, updateContext, Sets.newHashSet(offeredCredential), knownCredential);

        assertThat(result, is(false));
    }

    @Test
    public void knownCredentialEqualsAndHashCode() {
        PgpCredential first = PgpCredential.createKnownCredential("PGPKEY-AAAAAAAA");
        PgpCredential second = PgpCredential.createKnownCredential("PGPKEY-BBBBBBBB");

        assertTrue(first.equals(first));
        assertFalse(first.equals(second));

        assertFalse(first.hashCode() == second.hashCode());
        assertTrue(first.hashCode() == first.hashCode());
    }

    @Test
    public void offeredCredentialEqualsAndHashCode() {
        PgpCredential first = PgpCredential.createOfferedCredential("signedData1", "signature1", Charsets.ISO_8859_1);
        PgpCredential second = PgpCredential.createOfferedCredential("signedData2", "signature2", Charsets.ISO_8859_1);

        assertTrue(first.equals(first));
        assertFalse(first.equals(second));

        assertFalse(first.hashCode() == second.hashCode());
        assertTrue(first.hashCode() == first.hashCode());
    }

    @Test
    public void offeredAndKnownCredentialsEqualsAndHashCode() {
        PgpCredential known = PgpCredential.createKnownCredential("X509-1");
        PgpCredential offered = PgpCredential.createOfferedCredential("signedData", "signature", Charsets.ISO_8859_1);

        assertFalse(known.equals(offered));
        assertFalse(known.hashCode() == offered.hashCode());
    }

    @Test
    public void knownCredentialIsInvalid() {
        final String message = "" +
                "-----BEGIN PGP SIGNED MESSAGE-----\n" +
                "Hash: SHA1\n" +
                "\n" +
                "inetnum:        213.168.127.96 - 213.168.127.10\n" +
                "netname:        NETNAME\n" +
                "descr:          Description\n" +
                "country:        DE\n" +
                "admin-c:        TEST-RIPE\n" +
                "tech-c:         TEST-RIPE\n" +
                "status:         ASSIGNED PA\n" +
                "mnt-by:         TEST-MNT\n" +
                "mnt-lower:      TEST-MNT\n" +
                "changed:        foo@test.de 20090129\n" +
                "source:         RIPE\n" +
                "delete:         reason\n" +
                "-----BEGIN PGP SIGNATURE-----\n" +
                "Version: GnuPG v1.4.13 (Darwin)\n" +
                "Comment: GPGTools - http://gpgtools.org\n" +
                "\n" +
                "iQEcBAEBAgAGBQJRb/OqAAoJELvMuy1XY5UNovsH+QHj69vbPg7dLw49TY4Giqff\n" +
                "+W56/Z67iedP8dRJkhbSt4MzSBJaJi7VPrJDZjw7qXFmBDgHWHyDVCI6fJKUXFHA\n" +
                "d41lS0cs/4kA2olfhjfXB3djUAPyUuldT7nNyuZFHi/oTAjao7siCktPLgn5ZN0E\n" +
                "/IUpBvI5KNgMLpABfR8s5H8htTxGtaiOqVpfH7pc2VsxbC5PDUzjOOfb0nOmsegn\n" +
                "PAhZ3TnMmO78rBsuILfkYqaR9s9E+M7R7nMIPBchALaoUKYyzUxt1Ic/WHn1ECcN\n" +
                "hQvAyyc1xARDiOcJYziYChDxFX6zDCYIadDnIhfw3590KDNyl9ZjjkycK6NvlqA=\n" +
                "=tOGn\n" +
                "-----END PGP SIGNATURE-----";

        final PgpCredential offeredCredential = PgpCredential.createOfferedCredential(message);
        final PgpCredential knownCredential = PgpCredential.createKnownCredential("PGPKEY-67ABAB48");

        RpslObject keycertObject = RpslObject.parse("key-cert: PGPKEY-67ABAB48");
        when(rpslObjectDao.getByKey(ObjectType.KEY_CERT, keycertObject.getKey().toString())).thenReturn(keycertObject);

        assertThat(subject.hasValidCredential(update, updateContext, Sets.newHashSet(offeredCredential), knownCredential), is(false));
        verify(loggerContext).logString(any(Update.class), anyString(), anyString());
    }
}
