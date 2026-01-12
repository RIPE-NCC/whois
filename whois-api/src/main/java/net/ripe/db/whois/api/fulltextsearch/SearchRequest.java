package net.ripe.db.whois.api.fulltextsearch;

import com.google.common.base.MoreObjects;
import net.ripe.db.whois.common.Validate;

import javax.annotation.concurrent.Immutable;

@Immutable
public final class SearchRequest {

    protected static final String XML_FORMAT = "xml";
    protected static final String JSON_FORMAT = "json";

    private final int rows;
    private final int start;
    private final String query;
    private final boolean highlight;
    private final String highlightPre;
    private final String highlightPost;
    private final boolean facet;
    private final String format;

    private SearchRequest(
                final int rows,
                final int start,
                final String query,
                final boolean highlight,
                final String highlightPre,
                final String highlightPost,
                final boolean facet,
                final String format) {
        Validate.notNull(query, "No query parameter.");
        Validate.isTrue(!query.isEmpty(), "Invalid query");
        Validate.isTrue(XML_FORMAT.equals(format) || JSON_FORMAT.equals(format), "invalid format " + format);
        Validate.notNull(highlightPre, "no highlight.pre parameter");
        Validate.notNull(highlightPost, "no highlight.post parameter");
        this.rows = rows;
        this.start = start;
        this.query = query;
        this.highlight = highlight;
        this.highlightPre = highlightPre;
        this.highlightPost = highlightPost;
        this.facet = facet;
        this.format = format;
    }

    public String getQuery() {
        return this.query;
    }

    public boolean isHighlight() {
        return this.highlight;
    }

    public String getHighlightPre() {
        return this.highlightPre;
    }

    public String getHighlightPost() {
        return this.highlightPost;
    }

    public boolean isFacet() {
        return this.facet;
    }

    public int getStart() {
        return this.start;
    }

    public int getRows() {
        return this.rows;
    }

    public String getFormat() {
        return this.format;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(SearchRequest.class)
                .add("rows", rows)
                .add("start", start)
                .add("query", query)
                .add("highlight", highlight)
                .add("highlightPre", highlightPre)
                .add("highlightPost", highlightPost)
                .add("facet", facet)
                .add("format", format)
                .toString();
    }

    public static class SearchRequestBuilder {

        private int rows;
        private int start;
        private String query;
        private boolean highlight;
        private String highlightPre;
        private String highlightPost;
        private String format;
        private boolean facet;

        public SearchRequestBuilder setRows(final String rows) {
            this.rows = getIntValue(rows);
            return this;
        }

        public SearchRequestBuilder setStart(final String start) {
            this.start = getIntValue(start);
            return this;
        }

        public SearchRequestBuilder setQuery(final String query) {
            this.query = query;
            return this;
        }

        public SearchRequestBuilder setHighlight(final String highlight) {
            this.highlight = getBooleanValue(highlight);
            return this;
        }

        public SearchRequestBuilder setHighlightPre(final String highlightPre) {
            this.highlightPre = highlightPre;
            return this;
        }

        public SearchRequestBuilder setHighlightPost(final String highlightPost) {
            this.highlightPost = highlightPost;
            return this;
        }

        public SearchRequestBuilder setFormat(final String format) {
            this.format = format;
            return this;
        }

        public SearchRequestBuilder setFacet(final String facet) {
            this.facet = getBooleanValue(facet);
            return this;
        }

        public SearchRequest build() {
            return new SearchRequest(
                            rows,
                            start,
                            query,
                            highlight,
                            highlightPre,
                            highlightPost,
                            facet,
                            format);
        }

        // helper methods

        private int getIntValue(final String value) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(value + " is not a number");
            }
        }

        private boolean getBooleanValue(final String value) {
            return "true".equalsIgnoreCase(value);
        }
    }
}
