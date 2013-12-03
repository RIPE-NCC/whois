package net.ripe.db.whois.api.syncupdate;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import org.springframework.util.FileCopyUtils;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SyncUpdateBuilder {
    private String url;
    private String host;
    private Integer port;
    private String source;
    private String data;
    private boolean help;
    private boolean diff;
    private boolean aNew;
    private boolean redirect;

    public SyncUpdateBuilder setUrl(String url) {
        this.url = url;
        return this;
    }

    public SyncUpdateBuilder setHost(String host) {
        this.host = host;
        return this;
    }

    public SyncUpdateBuilder setPort(int port) {
        this.port = port;
        return this;
    }

    public SyncUpdateBuilder setSource(String source) {
        this.source = source;
        return this;
    }

    public SyncUpdateBuilder setData(String data) {
        this.data = data;
        return this;
    }

    public SyncUpdateBuilder setHelp(boolean help) {
        this.help = help;
        return this;
    }

    public SyncUpdateBuilder setDiff(boolean diff) {
        this.diff = diff;
        return this;
    }

    public SyncUpdateBuilder setNew(boolean aNew) {
        this.aNew = aNew;
        return this;
    }

    public SyncUpdateBuilder setRedirect(boolean redirect) {
        this.redirect = redirect;
        return this;
    }

    public Client build() {
        if (url != null) {
            return new Client(url, data, help, diff, aNew, redirect);
        }

        if (host != null && port != null && source != null) {
            return new Client(host, port, source, data, help, diff, aNew, redirect);
        }

        throw new IllegalStateException("Either (host, port, source) or (url) should not be null");
    }

    public static class Client {

        private static final Joiner.MapJoiner PARAM_JOINER = Joiner.on('&').withKeyValueSeparator("=");
        private static final Pattern CHARSET_PATTERN = Pattern.compile(".*;charset=(.*)");
        private static final String CHARSET = "ISO-8859-1";

        private final URL url;
        private final String data;
        private final boolean isHelp;
        private final boolean isDiff;
        private final boolean isNew;
        private final boolean isRedirect;

        public Client(final String host, final int port, final String source, final String data, final boolean isHelp, final boolean isDiff, final boolean isNew, final boolean isRedirect) {
            this.url = getUrl(host, port, source);
            this.data = data;
            this.isHelp = isHelp;
            this.isDiff = isDiff;
            this.isNew = isNew;
            this.isRedirect = isRedirect;
        }

        public Client(final String url, final String data, final boolean isHelp, final boolean isDiff, final boolean isNew, final boolean isRedirect) {
            this.url = getUrl(url);
            this.data = data;
            this.isHelp = isHelp;
            this.isDiff = isDiff;
            this.isNew = isNew;
            this.isRedirect = isRedirect;
       }

        public String post() {
            try {
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                final String body = getBody();
                connection.setRequestProperty(HttpHeaders.CONTENT_LENGTH, Integer.toString(body.length()));
                connection.setRequestProperty(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED + "; charset=" + CHARSET);

                connection.setDoInput(true);
                connection.setDoOutput(true);

                Writer writer = new OutputStreamWriter(connection.getOutputStream());
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
                params.put("DATA", encode(data));
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

        private static String encode(final String data) {
            try {
                return URLEncoder.encode(data, CHARSET);
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException(e);
            }
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

        private static URL getUrl(final String host, final int port, final String source) {
            return getUrl(String.format("http://%s%s/whois/syncupdates/%s", host, (port != 0 ? ":" + Integer.toString(port) : ""), source));
        }

        private static URL getUrl(final String url){
            try {
                return new URL(url);
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

}