package net.ripe.db.whois.update.dns.zonemaster.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Taken from Zonemaster documentation
 *
 * @see <a href="https://github.com/dotse/zonemaster-backend/blob/master/docs/API.md">Zonemaster documentation</a>
 */
public class TestProgressRequest extends ZonemasterRequest {

    @JsonProperty
    private String params;

    public TestProgressRequest(final String id) {
        super.setMethod(ZonemasterRequest.Method.TEST_PROGRESS);
        this.params = id;
    }
}
