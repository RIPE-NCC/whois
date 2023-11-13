package net.ripe.db.whois.api.rest.client;

import com.google.common.collect.Lists;
import com.google.common.net.UrlEscapers;
import net.ripe.db.whois.api.rest.mapper.AttributeMapper;
import net.ripe.db.whois.api.rest.mapper.DirtyClientAttributeMapper;
import net.ripe.db.whois.api.rest.mapper.FormattedClientAttributeMapper;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectMapper;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

public class RestClientUtils {
    private RestClientUtils() {
        // do not instantiate
    }

    // encode a query parameter
    // DO NOT use to encode form parameters
    public static final String encode(final String param) {
        return UrlEscapers.urlPathSegmentEscaper().escape(param);
    }

    // encode a list of query parameters
    public static final List<String> encode(final List<String> params) {
        return Lists.transform(params, input -> encode(input));
    }

    public static RestClient createRestClient(final String restApiUrl, final String source) {
        final RestClient restClient = new RestClient(restApiUrl, source);
        restClient.setWhoisObjectMapper(
                new WhoisObjectMapper(
                        restApiUrl,
                        new AttributeMapper[]{
                                new FormattedClientAttributeMapper(),
                                new DirtyClientAttributeMapper()
                        }));
        return restClient;
    }

    // Trust all server-side certificates (including self-signed)
    public static SSLContext trustAllSSLContext() {
        try {
            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(final X509Certificate[] arg0, final String arg1) throws CertificateException {
                    }
                    @Override
                    public void checkServerTrusted(final X509Certificate[] arg0, final String arg1) throws CertificateException {
                    }
                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
            }}, new java.security.SecureRandom());
            return sslContext;
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new IllegalStateException(e);
        }
    }

}
