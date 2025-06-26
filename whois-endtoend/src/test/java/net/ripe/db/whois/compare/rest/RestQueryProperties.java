package net.ripe.db.whois.compare.rest;

import net.ripe.db.whois.compare.common.ComparisonExecutorConfig;

import jakarta.ws.rs.core.MediaType;

class RestQueryProperties {

    public enum Source {RIPE, TEST}

    private Source source;
    private MediaType mediaType;

    public RestQueryProperties(final String query) {
        determineQueryProps(query);
    }

    public int getPortFromConfiguration(final ComparisonExecutorConfig config){
        if (source == Source.TEST){
            return config.getTestPort();
        }
        return config.getRipePort();
    }

    private void determineQueryProps(final String query) {
        final String lcQuery = query.toLowerCase();
        if (lcQuery.startsWith("whois/test/") || lcQuery.contains("source=test")) {
            this.source = Source.TEST;
        } else if (lcQuery.startsWith("whois/ripe/") || lcQuery.contains("source=ripe")) {
            this.source = Source.RIPE;
        } else if (lcQuery.startsWith("whois/metadata")) {
            this.source = Source.RIPE;
        } else {
            throw new IllegalArgumentException("Cannot determine Source from query");
        }

        this.mediaType = lcQuery.contains(".json") ? MediaType.APPLICATION_JSON_TYPE :
                            lcQuery.contains(".txt") ? MediaType.TEXT_PLAIN_TYPE :
                            MediaType.APPLICATION_XML_TYPE;
     }

    public Source getSource() {
        return source;
    }

    public MediaType getMediaType() {
        return mediaType;
    }
}
