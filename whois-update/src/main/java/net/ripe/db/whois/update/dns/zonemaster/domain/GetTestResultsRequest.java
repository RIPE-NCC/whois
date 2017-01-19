package net.ripe.db.whois.update.dns.zonemaster.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.google.common.base.MoreObjects;

/**
 * Entity class for Zonemaster get_test_results API method request.
 *
 * @see <a href="https://github.com/dotse/zonemaster-backend/blob/master/docs/API.md">Zonemaster documentation</a>
 */

public class GetTestResultsRequest extends ZonemasterRequest {

    private static final String LANGUAGE = "en";

    @JsonProperty
    final GetTestResultsRequest.Params params;

    public GetTestResultsRequest(final String id) {
        super.setMethod(ZonemasterRequest.Method.GET_TEST_RESULTS);
        this.params = new GetTestResultsRequest.Params(id, LANGUAGE);
    }

    @Override
    protected MoreObjects.ToStringHelper toStringHelper() {
        return super.toStringHelper()
            .add("params", params);
    }

    @JsonRootName("params")
    public static class Params {
        @JsonProperty
        private String id;
        @JsonProperty
        private String language;

        public Params(final String id, final String language) {
            this.id = id;
            this.language = language;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("language", language)
                .toString();
        }
    }
}
