package net.ripe.db.whois.update.authentication.credential;


import com.google.common.collect.Sets;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.X509Credential;
import net.ripe.db.whois.update.log.LoggerContext;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.dao.EmptyResultDataAccessException;

import java.security.cert.X509Certificate;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class X509CredentialValidatorTest {

    @Mock private PreparedUpdate update;
    @Mock private UpdateContext updateContext;
    @Mock private RpslObjectDao rpslObjectDao;
    @Mock private X509Credential offeredCredential;
    @Mock private X509Credential knownCredential;
    @Mock private DateTimeProvider dateTimeProvider;
    @Mock private LoggerContext loggerContext;
    @InjectMocks private X509CredentialValidator subject;

    @Before
    public void setup() {
        when(dateTimeProvider.getCurrentDateTime()).thenReturn(LocalDateTime.now());
        when(rpslObjectDao.getByKey(ObjectType.KEY_CERT, "X509-1")).thenAnswer(new Answer<RpslObject>() {
            @Override
            public RpslObject answer(InvocationOnMock invocation) throws Throwable {
                return RpslObject.parse("" +
                        "key-cert:       AUTO-1\n" +
                        "method:         X509\n" +
                        "owner:          /C=NL/ST=Noord-Holland/O=RIPE NCC/OU=DB/CN=Edward Shryane/EMAILADDRESS=eshryane@ripe.net\n" +
                        "fingerpr:       67:92:6C:2A:BC:3F:C7:90:B3:44:CF:CE:AF:1A:29:C2\n" +
                        "certif:         -----BEGIN CERTIFICATE-----\n" +
                        "certif:         MIIDsTCCAxqgAwIBAgICAXwwDQYJKoZIhvcNAQEEBQAwgYUxCzAJBgNVBAYTAk5M\n" +
                        "certif:         MRYwFAYDVQQIEw1Ob29yZC1Ib2xsYW5kMRIwEAYDVQQHEwlBbXN0ZXJkYW0xETAP\n" +
                        "certif:         BgNVBAoTCFJJUEUgTkNDMQwwCgYDVQQLEwNPUFMxDDAKBgNVBAMTA0NBMjEbMBkG\n" +
                        "certif:         CSqGSIb3DQEJARYMb3BzQHJpcGUubmV0MB4XDTExMTIwMTEyMzcyM1oXDTIxMTEy\n" +
                        "certif:         ODEyMzcyM1owgYAxCzAJBgNVBAYTAk5MMRYwFAYDVQQIEw1Ob29yZC1Ib2xsYW5k\n" +
                        "certif:         MREwDwYDVQQKEwhSSVBFIE5DQzELMAkGA1UECxMCREIxFzAVBgNVBAMTDkVkd2Fy\n" +
                        "certif:         ZCBTaHJ5YW5lMSAwHgYJKoZIhvcNAQkBFhFlc2hyeWFuZUByaXBlLm5ldDCBnzAN\n" +
                        "certif:         BgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEAw2zy4QciIZ1iaz3c9YDhvKxXchTCxptv\n" +
                        "certif:         5/A/oAJL0lzw5pFCRP7WgrWx/D7JfRiWgLAle2cBgN4oeho82In52ujcY3oGKKON\n" +
                        "certif:         XvYrIpOEfFaZnBd6o4pUJF5ERU02WS4lO/OJqeJxmGWv35vGHBGGjWaQS8GbETM9\n" +
                        "certif:         lNgqXS9Cl3UCAwEAAaOCATEwggEtMAkGA1UdEwQCMAAwLAYJYIZIAYb4QgENBB8W\n" +
                        "certif:         HU9wZW5TU0wgR2VuZXJhdGVkIENlcnRpZmljYXRlMB0GA1UdDgQWBBTBKJeV7er1\n" +
                        "certif:         y5+EoNVQLGsQ+GP/1zCBsgYDVR0jBIGqMIGngBS+JFXUQVcXFWwDyKV0X07DIMpj\n" +
                        "certif:         2KGBi6SBiDCBhTELMAkGA1UEBhMCTkwxFjAUBgNVBAgTDU5vb3JkLUhvbGxhbmQx\n" +
                        "certif:         EjAQBgNVBAcTCUFtc3RlcmRhbTERMA8GA1UEChMIUklQRSBOQ0MxDDAKBgNVBAsT\n" +
                        "certif:         A09QUzEMMAoGA1UEAxMDQ0EyMRswGQYJKoZIhvcNAQkBFgxvcHNAcmlwZS5uZXSC\n" +
                        "certif:         AQAwHgYDVR0RBBcwFYITZTMtMi5zaW5ndy5yaXBlLm5ldDANBgkqhkiG9w0BAQQF\n" +
                        "certif:         AAOBgQBTkPZ/lYrwA7mR5VV/X+SP7Bj+ZGKz0LudfKGZCCs1zHPGqr7RDtCYBiw1\n" +
                        "certif:         YwoMtlF6aSzgV9MZOZVPZKixCe1dAFShHUUYPctBgsNnanV3sDp9qVQ27Q9HzICo\n" +
                        "certif:         mlPZDYRpwo6Jz9TAdeFWisLWBspnl83R1tQepKTXObjVVCmhsA==\n" +
                        "certif:         -----END CERTIFICATE-----\n" +
                        "mnt-by:         OWNER-MNT\n" +
                        "changed:        noreply@ripe.net 20010101\n" +
                        "source:         TEST\n");
            }
        });
        when(knownCredential.getKeyId()).thenReturn("X509-1");
        when(offeredCredential.verify(any(X509Certificate.class))).thenReturn(true);
    }

    @Test
    public void authentication_success() {
        boolean result = subject.hasValidCredential(update, updateContext, Sets.newHashSet(offeredCredential), knownCredential);

        assertThat(result, is(true));
    }

    @Test
    public void authentication_keycert_not_found() throws Exception {
        when(rpslObjectDao.getByKey(ObjectType.KEY_CERT, "X509-1")).thenThrow(new EmptyResultDataAccessException(1));

        boolean result = subject.hasValidCredential(update, updateContext, Sets.newHashSet(offeredCredential), knownCredential);

        assertThat(result, is(false));
    }

    @Test
    public void authentication_keycert_is_invalid() throws Exception {
        when(rpslObjectDao.getByKey(ObjectType.KEY_CERT, "X509-1")).thenAnswer(new Answer<RpslObject>() {
            @Override
            public RpslObject answer(InvocationOnMock invocation) throws Throwable {
                return RpslObject.parse(
                        "key-cert:       AUTO-1\n" +
                                "method:         X509\n" +
                                "mnt-by:         OWNER-MNT\n" +
                                "changed:        noreply@ripe.net 20010101\n" +
                                "source:         TEST\n");
            }
        });

        boolean result = subject.hasValidCredential(update, updateContext, Sets.newHashSet(offeredCredential), knownCredential);

        assertThat(result, is(false));
    }


    @Test
    public void knownCredentialEqualsAndHashCode() {
        X509Credential first = X509Credential.createKnownCredential("X509-1");
        X509Credential second = X509Credential.createKnownCredential("X509-2");

        assertTrue(first.equals(first));
        assertFalse(first.equals(second));

        assertFalse(first.hashCode() == second.hashCode());
        assertTrue(first.hashCode() == first.hashCode());
    }

    @Test
    public void offeredCredentialEqualsAndHashCode() {
        X509Credential first = X509Credential.createOfferedCredential("signedData1", "signature1");
        X509Credential second = X509Credential.createOfferedCredential("signedData2", "signature2");

        assertTrue(first.equals(first));
        assertFalse(first.equals(second));

        assertFalse(first.hashCode() == second.hashCode());
        assertTrue(first.hashCode() == first.hashCode());
    }

    @Test
    public void offeredAndKnownCredentialsEqualsAndHashCode() {
        X509Credential known = X509Credential.createKnownCredential("X509-1");
        X509Credential offered = X509Credential.createOfferedCredential("signedData", "signature");

        assertFalse(known.equals(offered));
        assertFalse(known.hashCode() == offered.hashCode());
    }

    @Test
    public void knownCredentialIsInvalid() {
        when(dateTimeProvider.getCurrentDateTime()).thenReturn(LocalDateTime.now());
        when(rpslObjectDao.getByKey(ObjectType.KEY_CERT, "X509-1")).thenAnswer(new Answer<RpslObject>() {
            @Override
            public RpslObject answer(InvocationOnMock invocation) throws Throwable {
                return RpslObject.parse("" +
                        "key-cert:       AUTO-1\n" +
                        "method:         X509\n" +
                        "source:         TEST\n");
            }
        });
        when(knownCredential.getKeyId()).thenReturn("X509-1");
        when(offeredCredential.verify(any(X509Certificate.class))).thenReturn(true);

        boolean result = subject.hasValidCredential(update, updateContext, Sets.newHashSet(offeredCredential), knownCredential);

        assertThat(result, is(false));
    }
}
