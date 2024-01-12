package net.ripe.db.whois.api.syncupdate;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import net.ripe.db.whois.api.rest.client.RestClientUtils;
import org.springframework.util.FileCopyUtils;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO: split into "insecure" builder for testing only
public class SyncUpdateBuilder {

    private String url;
    private String protocol;
    private String host;
    private Integer port;
    private String source;
    private String data;
    private MultivaluedMap<String, String> headers;
    private boolean help;
    private boolean diff;
    private boolean aNew;
    private boolean redirect;
    private String charset;

    public SyncUpdateBuilder setHeaders(final MultivaluedMap<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public SyncUpdateBuilder setUrl(final String url) {
        this.url = url;
        return this;
    }

    public SyncUpdateBuilder setProtocol(final String protocol) {
        this.protocol = protocol;
        return this;
    }

    public SyncUpdateBuilder setHost(final String host) {
        this.host = host;
        return this;
    }

    public SyncUpdateBuilder setPort(final int port) {
        this.port = port;
        return this;
    }

    public SyncUpdateBuilder setSource(final String source) {
        this.source = source;
        return this;
    }

    public SyncUpdateBuilder setData(final String data) {
        this.data = data;
        return this;
    }

    public SyncUpdateBuilder setHelp(final boolean help) {
        this.help = help;
        return this;
    }

    public SyncUpdateBuilder setDiff(final boolean diff) {
        this.diff = diff;
        return this;
    }

    public SyncUpdateBuilder setNew(final boolean aNew) {
        this.aNew = aNew;
        return this;
    }

    public SyncUpdateBuilder setRedirect(final boolean redirect) {
        this.redirect = redirect;
        return this;
    }

    public SyncUpdateBuilder setCharset(final String charset) {
        this.charset = charset;
        return this;
    }

    public Client build() {
        return new Client(url, protocol, host, port, source, headers, data, help, diff, aNew, redirect, charset);
    }

    public static class Client {

        private static final String HTTP_PROTOCOL = "http";
        private static final String HTTPS_PROTOCOL = "https";

        private static final Joiner.MapJoiner PARAM_JOINER = Joiner.on('&').withKeyValueSeparator("=");
        private static final Pattern CHARSET_PATTERN = Pattern.compile(".*;charset=(.*)");
        private static final Charset DEFAULT_CHARSET = StandardCharsets.ISO_8859_1;

        private final URL url;
        private final MultivaluedMap<String, String> headers;
        private final String data;
        private final boolean isHelp;
        private final boolean isDiff;
        private final boolean isNew;
        private final boolean isRedirect;
        private final Charset charset;

        public Client(
                final String url,
                final String protocol,
                final String host,
                final Integer port,
                final String source,
                final MultivaluedMap<String, String> headers,
                final String data,
                final boolean isHelp,
                final boolean isDiff,
                final boolean isNew,
                final boolean isRedirect,
                final String charset) {
            if (url != null) {
                this.url = getUrl(url);
            } else {
                if (host != null && port != null && source != null) {
                    this.url = getUrl(protocol, host, port, source);
                } else {
                    throw new IllegalStateException("Either (host, port, source) or (url) should not be null");
                }
            }
            this.headers = headers;
            this.data = data;
            this.isHelp = isHelp;
            this.isDiff = isDiff;
            this.isNew = isNew;
            this.isRedirect = isRedirect;
            if (charset != null) {
                try {
                    this.charset = Charset.forName(charset);
                } catch (UnsupportedCharsetException e) {
                    throw new IllegalArgumentException("Unsupported charset " + charset);
                }
            } else {
                this.charset = DEFAULT_CHARSET;
            }
        }

        public String post() {
            try {
                final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                if (HTTPS_PROTOCOL.equalsIgnoreCase(url.getProtocol())) {
                    final HttpsURLConnection httpsConnection = (HttpsURLConnection) connection;
                    httpsConnection.setHostnameVerifier((hostname, session) -> true);
                    httpsConnection.setSSLSocketFactory(RestClientUtils.trustAllSSLContext().getSocketFactory());
                }

                final String body = getBody();
                connection.setRequestProperty(HttpHeaders.CONTENT_LENGTH, Integer.toString(body.length()));
                connection.setRequestProperty(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED + "; charset=" + charset.name());

                setHeaders(connection);

                connection.setDoInput(true);
                connection.setDoOutput(true);

                final Writer writer = new OutputStreamWriter(connection.getOutputStream());
                writer.write(body);
                writer.close();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    throw new IllegalStateException(connection.getResponseMessage());
                }

                return readResponse(connection);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        private String getBody() {
            final Map<String, String> params = Maps.newHashMap();
            if ((data != null) && (data.length() > 0)) {
                params.put("DATA", SyncUpdateUtils.encode(data, charset));
            }
            if (isHelp) {
                params.put("HELP", "yes");
            }
            if (isDiff) {
                params.put("DIFF", "yes");
            }
            if (isNew) {
                params.put("NEW", "yes");
            }
            if (isRedirect) {
                params.put("REDIRECT", "yes");
            }
            return PARAM_JOINER.join(params);
        }

        private static String readResponse(final HttpURLConnection connection) {
            try {
                final InputStream inputStream;

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    inputStream = connection.getInputStream();
                } else {
                    inputStream = connection.getErrorStream();
                }

                final String contentType = connection.getContentType();
                final Matcher matcher = CHARSET_PATTERN.matcher(contentType);
                final String charsetName = matcher.matches() ? matcher.group(1) : Charset.defaultCharset().name();

                final byte[] bytes = FileCopyUtils.copyToByteArray(inputStream);
                return new String(bytes, charsetName);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        private static URL getUrl(final String protocol, final String host, final int port, final String source) {
            return getUrl(String.format("%s://%s%s/whois/syncupdates/%s",
                (protocol != null ? protocol : HTTP_PROTOCOL),
                host,
                (port != 0 ? ":" + port : ""),
                source));
        }

        private static URL getUrl(final String url){
            try {
                return new URL(url);
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException(e);
            }
        }

        private void setHeaders(final HttpURLConnection connection) {
            if (this.headers != null) {
                for (final Map.Entry<String, List<String>> entry : headers.entrySet()) {
                    final String key = entry.getKey();
                    if (connection.getRequestProperty(key) == null) {
                        // don't overwrite existing headers (e.g. content-type, content-length)
                        for (final String value : entry.getValue()) {
                            connection.addRequestProperty(key, value);
                        }
                    }
                }
            }
        }
    }

}
