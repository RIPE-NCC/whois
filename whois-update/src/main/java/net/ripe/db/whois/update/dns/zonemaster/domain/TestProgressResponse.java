package net.ripe.db.whois.update.dns.zonemaster.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Entity class for Zonemaster test_progress API method response.
 *
 * @see <a href="https://github.com/dotse/zonemaster-backend/blob/master/docs/API.md">Zonemaster documentation</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TestProgressResponse extends ZonemasterResponse {

    @JsonProperty
    private String result;

    public String getResult() {
        return result;
    }
}
