package net.ripe.db.whois.update.authentication.credential;

import com.google.common.collect.Sets;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.update.domain.Operation;
import net.ripe.db.whois.update.domain.Paragraph;
import net.ripe.db.whois.update.domain.PgpCredential;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.log.LoggerContext;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PgpCredentialValidatorTest {
    @Mock private PreparedUpdate preparedUpdate;
    @Mock private Update update;
    @Mock private UpdateContext updateContext;
    @Mock private RpslObjectDao rpslObjectDao;
    @Mock private DateTimeProvider dateTimeProvider;
    @Mock private LoggerContext loggerContext;
    @InjectMocks private PgpCredentialValidator subject;

    private static final RpslObject KEYCERT_OBJECT = RpslObject.parse("" +
            "key-cert:       PGPKEY-5763950D\n" +
            "method:         PGP\n" +
            "owner:          noreply@ripe.net <noreply@ripe.net>\n" +
            "fingerpr:       884F 8E23 69E5 E6F1 9FB3  63F4 BBCC BB2D 5763 950D\n" +
            "certif:         -----BEGIN PGP PUBLIC KEY BLOCK-----\n" +
            "certif:         Version: GnuPG v1\n" +
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
            "source:         RIPE\n");

    @Test
    public void authenticateExistingRpslObject() {
        when(dateTimeProvider.getCurrentDateTime()).thenReturn(LocalDateTime.now());
        when(preparedUpdate.getUpdate()).thenReturn(update);

        final String message =
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
                "source:         RIPE\n" +
                "delete:         reason\n" +
                "-----BEGIN PGP SIGNATURE-----\n" +
                "Version: GnuPG v1\n" +
                "Comment: GPGTools - http://gpgtools.org\n" +
                "\n" +
                "iQEcBAEBAgAGBQJWTc5TAAoJELvMuy1XY5UNorkIAJsWhjbTcPBLCtug50Hkp0ty\n" +
                "6hMMVWfIS92fGFzpUKkS3fTnUXkTwsaF0+KQRSHEa6pobMXsP5MCl0SPJaVY4FTz\n" +
                "CtlpTHQ1avld/o281Y44wGmN/JFcGml8cnpY9/wseNS2OogemJ1ZQdd9Y4zNuCNX\n" +
                "YS5y2jXLQyuLEzmhg423+b4IqeVZBHdWX43tituzk5phy9U2ZuVAnxLQWvNt0QZC\n" +
                "v6g0Rig345U3rn0aRCAAFz6C/Al1QbRt5dsH3vQ/lQfiCBoR0A1x9ttsUkB7oCdJ\n" +
                "P4eeAXVVIZIqCPKBmNo2fRoDJW5Ly1YEAIASp1pjh0h/kDfJwPQc+mqOQ1CRwgQ=\n" +
                "=KPdC\n" +
                "-----END PGP SIGNATURE-----";

        final PgpCredential offeredCredential = PgpCredential.createOfferedCredential(message);
        final PgpCredential knownCredential = PgpCredential.createKnownCredential("PGPKEY-5763950D");

        when(rpslObjectDao.getByKey(ObjectType.KEY_CERT, KEYCERT_OBJECT.getKey().toString())).thenReturn(KEYCERT_OBJECT);
        when(preparedUpdate.getUpdate()).thenReturn(createUpdate());
        assertThat(subject.hasValidCredential(preparedUpdate, updateContext, Sets.newHashSet(offeredCredential), knownCredential, null), is(true));
        verify(loggerContext).logString(any(Update.class), anyString(), anyString());
    }

    @Test
    public void authenticateExistingRpslObjectGreekEncoding() throws Exception {
        when(dateTimeProvider.getCurrentDateTime()).thenReturn(LocalDateTime.now());
        when(preparedUpdate.getUpdate()).thenReturn(update);

        final String message =
                "-----BEGIN PGP SIGNED MESSAGE-----\n" +
                "Hash: SHA1\n" +
                "\n" +
                "person:     Test Person\n" +
                "address:    ακρόπολη\n" +
                "phone:      +30 123 411141\n" +
                "fax-no:     +30 123 411140\n" +
                "nic-hdl:    TP1-TEST\n" +
                "mnt-by:     UPD-MNT\n" +
                "source:     TEST\n" +
                "-----BEGIN PGP SIGNATURE-----\n" +
                "Version: GnuPG v1\n" +
                "Comment: GPGTools - http://gpgtools.org\n" +
                "\n" +
                "iQEcBAEBAgAGBQJWTa/LAAoJELvMuy1XY5UNpukIAIOAgObWVSNbyOklWgIGjH6q\n" +
                "XeJY6LhysQCQTEbD+AMJhd5rQQbN4jnK+MwaTneCwKuEwppzcWzthd0ZFdWFk+gR\n" +
                "N5PYtfHcWn46a0jW6pA1EUxGf+8V8vVSM0hnDJnK/rHv7F+aCo/4uYTID6AGit/Q\n" +
                "/lsURfe58oQZ88N8rJcA+WVDkEdEpqsRErqWi0J5TE7h0lnm7xLyGOF8OQBs/kqg\n" +
                "QypGr+WzELhLkrL7nBCiL00wylYR4pD1P2VzNSrezgcsprgrCGpzurXaXVi7nWgs\n" +
                "//7jgTneD5yXfR4iWV6/JKps9zksPLR33bcB0rWLdnzdXRGO83FbORtdGOfVjqE=\n" +
                "=0RVp\n" +
                "-----END PGP SIGNATURE-----";

        final PgpCredential offeredCredential = PgpCredential.createOfferedCredential(message, Charset.forName("ISO-8859-7"));
        final PgpCredential knownCredential = PgpCredential.createKnownCredential("PGPKEY-5763950D");

        when(rpslObjectDao.getByKey(ObjectType.KEY_CERT, KEYCERT_OBJECT.getKey().toString())).thenReturn(KEYCERT_OBJECT);
        when(preparedUpdate.getUpdate()).thenReturn(createUpdate());
        assertThat(subject.hasValidCredential(preparedUpdate, updateContext, Sets.newHashSet(offeredCredential), knownCredential, null), is(true));
        verify(loggerContext).logString(any(Update.class), anyString(), anyString());
    }


    @Test
    public void setsEffectiveCredential() {
        when(dateTimeProvider.getCurrentDateTime()).thenReturn(LocalDateTime.now());
        when(preparedUpdate.getUpdate()).thenReturn(update);

        final String message =
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
                        "source:         RIPE\n" +
                        "delete:         reason\n" +
                        "-----BEGIN PGP SIGNATURE-----\n" +
                        "Version: GnuPG v1\n" +
                        "Comment: GPGTools - http://gpgtools.org\n" +
                        "\n" +
                        "iQEcBAEBAgAGBQJWTc5TAAoJELvMuy1XY5UNorkIAJsWhjbTcPBLCtug50Hkp0ty\n" +
                        "6hMMVWfIS92fGFzpUKkS3fTnUXkTwsaF0+KQRSHEa6pobMXsP5MCl0SPJaVY4FTz\n" +
                        "CtlpTHQ1avld/o281Y44wGmN/JFcGml8cnpY9/wseNS2OogemJ1ZQdd9Y4zNuCNX\n" +
                        "YS5y2jXLQyuLEzmhg423+b4IqeVZBHdWX43tituzk5phy9U2ZuVAnxLQWvNt0QZC\n" +
                        "v6g0Rig345U3rn0aRCAAFz6C/Al1QbRt5dsH3vQ/lQfiCBoR0A1x9ttsUkB7oCdJ\n" +
                        "P4eeAXVVIZIqCPKBmNo2fRoDJW5Ly1YEAIASp1pjh0h/kDfJwPQc+mqOQ1CRwgQ=\n" +
                        "=KPdC\n" +
                        "-----END PGP SIGNATURE-----";

        final PgpCredential offeredCredential = PgpCredential.createOfferedCredential(message, Charset.forName("ISO-8859-7"));
        final PgpCredential knownCredential = PgpCredential.createKnownCredential("PGPKEY-5763950D");
        final Update update = createUpdate();
        when(preparedUpdate.getUpdate()).thenReturn(update);
        when(rpslObjectDao.getByKey(ObjectType.KEY_CERT, KEYCERT_OBJECT.getKey().toString())).thenReturn(KEYCERT_OBJECT);

        subject.hasValidCredential(preparedUpdate, updateContext, Sets.newHashSet(offeredCredential), knownCredential, null);

        assertThat(update.getEffectiveCredential(), Is.is(knownCredential.getKeyId()));
        assertThat(update.getEffectiveCredentialType(), Is.is(Update.EffectiveCredentialType.PGP));
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

        final PgpCredential knownCredential = PgpCredential.createKnownCredential("PGPKEY-5763950D");
        when(rpslObjectDao.getByKey(ObjectType.KEY_CERT, KEYCERT_OBJECT.getKey().toString())).thenReturn(KEYCERT_OBJECT);
        assertThat(subject.hasValidCredential(preparedUpdate, updateContext, Sets.newHashSet(offeredCredential), knownCredential, null), is(false));
    }

    @Test
    public void authenticateKeycertNotFound() throws Exception {
        final String message =
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
                "source:         RIPE\n" +
                "delete:         reason\n" +
                "-----BEGIN PGP SIGNATURE-----\n" +
                "Version: GnuPG v1\n" +
                "Comment: GPGTools - http://gpgtools.org\n" +
                "\n" +
                "iQEcBAEBAgAGBQJWTc9CAAoJELvMuy1XY5UNl2oH/2i2JeUvWsrOF/FAxJsvCUMG\n" +
                "4KqGWJKb1/Sdgp3NJbtrqoZo54+vdI3f0Oqb6q5nspTQJntfA0uq08GFbwcOHA5T\n" +
                "9fgyh0BVE/OFNUY336r+Gr8Sf9sfLWVgAIUNhe5hyUeoqSD+zcp0buYE/HFLp4Yh\n" +
                "EVoOSHSGdSpRtkErE3eMYEjYCle7zJhC3MDnauWYYHQzZVJEvQ8mnEi3fZd+OetL\n" +
                "XnngoRCUa3z3J8tFx0GYNNxP+YA24+gJf+BuJqxX86N0Nua8ZZqzR+AkbAffvXwv\n" +
                "HR3C5Mn2WOXspKRfdevzV34k4x/SzqFa1qGoF62AKmY5pLMX8XSvU4WV4zjOEDU=\n" +
                "=UgWC\n" +
                "-----END PGP SIGNATURE-----";

        final PgpCredential offeredCredential = PgpCredential.createOfferedCredential(message);
        final PgpCredential knownCredential = PgpCredential.createKnownCredential("PGPKEY-5763950D");

        when(rpslObjectDao.getByKey(ObjectType.KEY_CERT, KEYCERT_OBJECT.getKey().toString())).thenThrow(new EmptyResultDataAccessException(1));
        assertThat(subject.hasValidCredential(preparedUpdate, updateContext, Sets.newHashSet(offeredCredential), knownCredential, null), is(false));
    }

    @Test
    public void authenticateKnownCredentialIsInvalid() {
        final String message =
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
                "source:         RIPE\n" +
                "delete:         reason\n" +
                "-----BEGIN PGP SIGNATURE-----\n" +
                "Version: GnuPG v1\n" +
                "Comment: GPGTools - http://gpgtools.org\n" +
                "\n" +
                "iQEcBAEBAgAGBQJWTdB6AAoJELvMuy1XY5UN96QIAJjssbJrKuJvWmgq/iTYjHYz\n" +
                "iq3qJGprdxM0G+OErsydLmG5R7BhW6aLmV/H07Ap84T9KUniGm+sWgAHJdC0Eb/n\n" +
                "cf+8sC7hy5Fkmwabam52Ncr+c4mEuR7Tt2s0bfm2x+IgFb/doMoJjCydYk5FQKL/\n" +
                "QzhgR3whwhftsrSgj2MAD2VDVo7HsvD1Otp/hVLISdnPQuR6i9wRp7gipn+QSdp/\n" +
                "PBK/UzdvUGItTM/wCoekEcwh41plq7VepL6rbA2NXe9x65rvMprEhL0ulQVP8YXZ\n" +
                "9LF/bGg8YGezpktt8iFnLKsOCek74mSdVi70xTNsc4sq9mNfdBXrsZstNHvDJ+E=\n" +
                "=Pr87\n" +
                "-----END PGP SIGNATURE-----";

        final PgpCredential offeredCredential = PgpCredential.createOfferedCredential(message);
        final PgpCredential knownCredential = PgpCredential.createKnownCredential("PGPKEY-5763950D");

        final RpslObject emptyKeycertObject = new RpslObjectBuilder(KEYCERT_OBJECT).removeAttributeType(AttributeType.CERTIF).get();
        when(rpslObjectDao.getByKey(ObjectType.KEY_CERT, emptyKeycertObject.getKey().toString())).thenReturn(emptyKeycertObject);
        assertThat(subject.hasValidCredential(preparedUpdate, updateContext, Sets.newHashSet(offeredCredential), knownCredential, null), is(false));
    }

    @Test
    public void knownCredentialEqualsAndHashCode() {
        final PgpCredential first = PgpCredential.createKnownCredential("PGPKEY-AAAAAAAA");
        final PgpCredential second = PgpCredential.createKnownCredential("PGPKEY-BBBBBBBB");

        assertThat(first, equalTo(first));
        assertThat(first, not(equalTo(second)));

        assertThat((first.hashCode() == second.hashCode()), is(false));
        assertThat((first.hashCode() == first.hashCode()), is(true));
    }

    @Test
    public void offeredCredentialEqualsAndHashCode() {
        final PgpCredential first = PgpCredential.createOfferedCredential("signedData1", "signature1", StandardCharsets.ISO_8859_1);
        final PgpCredential second = PgpCredential.createOfferedCredential("signedData2", "signature2", StandardCharsets.ISO_8859_1);

        assertThat(first, equalTo(first));
        assertThat(first, not(equalTo(second)));

        assertThat((first.hashCode() == second.hashCode()), is(false));
        assertThat((first.hashCode() == first.hashCode()), is(true));
    }

    @Test
    public void offeredAndKnownCredentialsEqualsAndHashCode() {
        final PgpCredential known = PgpCredential.createKnownCredential("X509-1");
        final PgpCredential offered = PgpCredential.createOfferedCredential("signedData", "signature", StandardCharsets.ISO_8859_1);

        assertThat(known, not(equalTo(offered)));
        assertThat((known.hashCode() == offered.hashCode()), is(false));
    }

    @Test
    public void knownCredentialIsInvalid() {
        when(preparedUpdate.getUpdate()).thenReturn(update);

        final String message =
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
                "source:         RIPE\n" +
                "delete:         reason\n" +
                "-----BEGIN PGP SIGNATURE-----\n" +
                "Version: GnuPG v1\n" +
                "Comment: GPGTools - http://gpgtools.org\n" +
                "\n" +
                "iQEcBAEBAgAGBQJWTf5ZAAoJELvMuy1XY5UNfeEIAIfqwKn4DjyZRGm9NUyXZ3nX\n" +
                "5OY1YuH+d7IXicw5fADSzCED1Kg411TYUphCIM+TUGY8per8MiR1UV9OF+i4tRFc\n" +
                "aeuNEFY+ZkCQaSp6qhlYFVPqiboKAd2sg2NKoa/aTwk+IEX1RrVxXURNvKgoVqQ5\n" +
                "4ChIw2yZvuWoZk5qnZQ/ztl0axi7ZngUACNX7pN2LpaIzAT2FrA1FADmHmktmAt0\n" +
                "eWS70m/fOTCN2DEw46irBgjqiY/rQcClxUX596OlV/dQ1l/Gmo4wDwKPxJ9dzHS5\n" +
                "y2aPt9iULKozTlOZDWDAXxsjxqHFUHxSC5aSAiU9qRP6uzdN5dil2Y0LLh+7hB4=\n" +
                "=J7hj\n" +
                "-----END PGP SIGNATURE-----";

        final PgpCredential offeredCredential = PgpCredential.createOfferedCredential(message);
        final PgpCredential knownCredential = PgpCredential.createKnownCredential("PGPKEY-5763950D");

        final RpslObject keycertObject = RpslObject.parse("key-cert: PGPKEY-5763950D");
        when(rpslObjectDao.getByKey(ObjectType.KEY_CERT, keycertObject.getKey().toString())).thenReturn(keycertObject);

        assertThat(subject.hasValidCredential(preparedUpdate, updateContext, Sets.newHashSet(offeredCredential), knownCredential, null), is(false));

        verify(loggerContext).logString(any(Update.class), anyString(), anyString());
    }

    private Update createUpdate() {
        final Paragraph paragraph = new Paragraph(" ");
        final RpslObject submittedObject = new RpslObject(Arrays.asList(new RpslAttribute(AttributeType.ORGANISATION, CIString.ciString("org-1"))));

        return new Update(paragraph, Operation.DELETE, Arrays.asList(" "), submittedObject);
    }
}
