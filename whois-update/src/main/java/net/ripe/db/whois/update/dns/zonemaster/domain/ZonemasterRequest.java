package net.ripe.db.whois.update.dns.zonemaster.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

/**
 * Base entity class for Zonemaster API method requests.
 *
 * @see <a href="https://github.com/dotse/zonemaster-backend/blob/master/docs/API.md">Zonemaster documentation</a>
 */
public abstract class ZonemasterRequest {

    @JsonProperty("jsonrpc")
    private String jsonRpc = "2.0";
    @JsonProperty
    private int id;
    @JsonProperty
    private String method;

    protected void setMethod(final ZonemasterRequest.Method method) {
        this.method = method.getMethod();
        this.id = method.getId();
    }

    protected MoreObjects.ToStringHelper toStringHelper() {
        return MoreObjects.toStringHelper(this)
                .add("jsonRpc", jsonRpc)
                .add("method", method)
                .add("id", id);
    }

    @Override
    public String toString() {
        return toStringHelper().toString();
    }

    public enum Method {
        VERSION_INFO("version_info", 1),
        GET_NS_IPS("get_ns_ips", 2),
        GET_DATA_FROM_PARENT_ZONE("get_data_from_parent_zone", 3),
        START_DOMAIN_TEST("start_domain_test", 4),
        TEST_PROGRESS("test_progress", 5),
        GET_TEST_RESULTS("get_test_results", 6),
        GET_TEST_HISTORY("get_test_history", 7);

        private String method;
        private int id;


        Method(final String method, final int id) {
            this.method = method;
            this.id = id;
        }

        public String getMethod() {
            return method;
        }
        public int getId() {
            return id;
        }
    }
}
