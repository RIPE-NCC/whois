package net.ripe.db.whois.api.rest;

import net.ripe.db.whois.common.ClockDateTimeProvider;
import net.ripe.db.whois.common.TestDateTimeProvider;
import net.ripe.db.whois.update.keycert.X509CertificateTestUtil;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;

import static net.ripe.db.whois.api.rest.ClientCertificateExtractor.HEADER_SSL_CLIENT_CERT;
import static net.ripe.db.whois.api.rest.ClientCertificateExtractor.HEADER_SSL_CLIENT_VERIFY;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ClientCertificateExtractorTest {

    static String cert;

    static {
        try {
            cert = X509CertificateTestUtil.asPem(X509CertificateTestUtil.generate("test-cn", new ClockDateTimeProvider()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testValidCertificate() {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(HEADER_SSL_CLIENT_CERT)).thenReturn(cert);
        when(request.getHeader(HEADER_SSL_CLIENT_VERIFY)).thenReturn("GENEROUS");

        assertThat(ClientCertificateExtractor.getClientCertificate(request, new ClockDateTimeProvider()).isPresent(), is(true));
    }

    @Test
    public void testInvalidVerifyHeader() {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(HEADER_SSL_CLIENT_CERT)).thenReturn(cert);
        when(request.getHeader(HEADER_SSL_CLIENT_VERIFY)).thenReturn("NOT-ACCEPTED");

        assertThat(ClientCertificateExtractor.getClientCertificate(request, new ClockDateTimeProvider()).isPresent(), is(false));
    }

    @Test
    public void testNoCertificate() {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(HEADER_SSL_CLIENT_VERIFY)).thenReturn("GENEROUS");

        assertThat(ClientCertificateExtractor.getClientCertificate(request, new ClockDateTimeProvider()).isPresent(), is(false));
    }

    @Test
    public void testCertificateExpired() {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(HEADER_SSL_CLIENT_CERT)).thenReturn(cert);
        when(request.getHeader(HEADER_SSL_CLIENT_VERIFY)).thenReturn("GENEROUS");

        final TestDateTimeProvider testDateTimeProvider = new TestDateTimeProvider();
        testDateTimeProvider.setTime(LocalDateTime.now().plusYears(5));

        assertThat(ClientCertificateExtractor.getClientCertificate(request, testDateTimeProvider).isPresent(), is(false));
    }

    @Test
    public void testCertificateNotYetValid() {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(HEADER_SSL_CLIENT_CERT)).thenReturn(cert);
        when(request.getHeader(HEADER_SSL_CLIENT_VERIFY)).thenReturn("GENEROUS");

        final TestDateTimeProvider testDateTimeProvider = new TestDateTimeProvider();
        testDateTimeProvider.setTime(LocalDateTime.now().minusMonths(1));

        assertThat(ClientCertificateExtractor.getClientCertificate(request, testDateTimeProvider).isPresent(), is(false));
    }

}
