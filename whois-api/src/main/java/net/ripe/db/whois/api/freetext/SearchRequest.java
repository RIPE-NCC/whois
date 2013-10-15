package net.ripe.db.whois.api.freetext;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

final class SearchRequest {
    private static final String PARAM_ROWS = "rows";
    private static final String PARAM_START = "start";
    private static final String PARAM_QUERY = "q";
    private static final String PARAM_HIGHLIGHT = "hl";
    private static final String PARAM_HIGHLIGHT_PRE = "hl.simple.pre";
    private static final String PARAM_HIGHLIGHT_POST = "hl.simple.post";
    private static final String PARAM_RESPONSE_WRITER_TYPE = "wt";
    private static final String PARAM_FACET = "facet";

    private static final Splitter PARAM_SPLITTER = Splitter.on('&');
    private static final Splitter VALUE_SPLITTER = Splitter.on('=');

    private final Map<String, String> params;

    private SearchRequest(final Map<String, String> params) {
        this.params = Collections.unmodifiableMap(params);
    }

    public static SearchRequest parse(final String query) {
        if (StringUtils.isEmpty(query)) {
            throw new IllegalArgumentException("No query parameter.");
        }

        final Map<String, String> params = Maps.newHashMap();

        for (String param : PARAM_SPLITTER.split(query)) {
            final Iterator<String> keyValueIterator = VALUE_SPLITTER.split(param).iterator();
            if (keyValueIterator.hasNext()) {
                final String key = keyValueIterator.next().trim();
                if (keyValueIterator.hasNext()) {
                    try {
                        String value = URLDecoder.decode(keyValueIterator.next(), Charset.defaultCharset().name());
                        value = value.replaceAll("[/]", "\\\\/");
                        params.put(key, value);
                    } catch (UnsupportedEncodingException e) {
                        throw new IllegalArgumentException(String.format("Unsupported encoding: %s", Charset.defaultCharset().name()));
                    }
                }
            }
        }

        return new SearchRequest(params);
    }

    public String getQuery() {
        return getStringValue(PARAM_QUERY, null);
    }

    public boolean isHighlight() {
        return getBooleanValue(PARAM_HIGHLIGHT, false);
    }

    public String getHighlightPre() {
        return getStringValue(PARAM_HIGHLIGHT_PRE, "<b>");
    }

    public String getHighlightPost() {
        return getStringValue(PARAM_HIGHLIGHT_POST, "</b>");
    }

    public boolean isFacet() {
        return getBooleanValue(PARAM_FACET, false);
    }

    public int getStart() {
        return getIntValue(PARAM_START, 0);
    }

    public int getRows() {
        return getIntValue(PARAM_ROWS, 10);
    }

    public String getFormat() {
        return getStringValue(PARAM_RESPONSE_WRITER_TYPE, "xml");
    }

    public Map<String, String> getParams() {
        return params;
    }

    private String getStringValue(final String key, final String defaultValue) {
        return params.containsKey(key) ? params.get(key) : defaultValue;
    }

    private boolean getBooleanValue(final String key, final boolean defaultValue) {
        return params.containsKey(key) ? "true".equals(params.get(key)) : defaultValue;
    }

    private int getIntValue(final String key, final int defaultValue) {
        try {
            return params.containsKey(key) ? Integer.parseInt(params.get(key)) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
