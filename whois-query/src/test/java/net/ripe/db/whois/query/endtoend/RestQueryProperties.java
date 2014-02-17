package net.ripe.db.whois.query.endtoend;

import javax.ws.rs.core.MediaType;

class RestQueryProperties {

    public enum Source {RIPE, TEST}
    public enum QueryType {LOOKUP, SEARCH}

    private Source source;
    private QueryType queryType;
    private MediaType mediaType;

    public RestQueryProperties(final String query) {
        determineQueryProps(query);
    }

    public int getPortFromConfiguration(final RestExecutorConfiguration config){
        if (source == Source.TEST){
            return config.getTestPort();
        }
        return config.getRipePort();
    }

    private void determineQueryProps(final String query) {
        if (query.toLowerCase().startsWith("whois/test/") || query.toLowerCase().contains("source=test")) {
            this.source = Source.TEST;
        } else if (query.toLowerCase().startsWith("whois/ripe/") || query.toLowerCase().contains("source=ripe")) {
            this.source = Source.RIPE;
        } else {
            throw new IllegalArgumentException("Cannot determine Source from query");
        }

        if (query.toLowerCase().startsWith("whois/search?")){
            this.queryType = QueryType.SEARCH;
        } else if (query.toLowerCase().startsWith("whois/ripe/") || query.toLowerCase().contains("whois/test/")) {
            this.queryType = QueryType.LOOKUP;
        } else {
            throw new IllegalArgumentException("Cannot determine QueryType from query");
        }

        if (query.toLowerCase().contains(".json")){
            this.mediaType = MediaType.APPLICATION_JSON_TYPE;
        } else {
            this.mediaType = MediaType.APPLICATION_XML_TYPE;
        }
     }

    public Source getSource() {
        return source;
    }
    public QueryType getQueryType() {
        return queryType;
    }

    public MediaType getMediaType() {
        return mediaType;
    }
}
