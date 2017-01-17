package net.ripe.db.whois.update.dns.zonemaster.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TestProgressResponse extends ZonemasterResponse {

    @JsonProperty
    private String result;

    public String getResult() {
        return result;
    }
}
