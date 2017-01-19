package net.ripe.db.whois.update.dns.zonemaster.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

/**
 * Entity class for Zonemaster test_progress API method request.
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

    @Override
    protected MoreObjects.ToStringHelper toStringHelper() {
        return super.toStringHelper()
            .add("params", params);
    }
}
