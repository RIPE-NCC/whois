package net.ripe.db.whois.update.dns.zonemaster.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TestProgressResponse extends ZonemasterResponse {

    @JsonProperty
    private String result;

    public String getResult() {
        return result;
    }
}
