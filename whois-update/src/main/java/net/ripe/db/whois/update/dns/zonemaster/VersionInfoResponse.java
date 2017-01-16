package net.ripe.db.whois.update.dns.zonemaster;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Map;

public class VersionInfoResponse {

    @JsonProperty("jsonRpc")
    private String jsonRpc;
    private String id;
    private Map<String, String> result;

    public String getJsonRpc() {
        return jsonRpc;
    }

    public void setJsonRpc(final String jsonRpc) {
        this.jsonRpc = jsonRpc;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public Map<String, String> getResult() {
        return result;
    }

    public void setResult(final Map<String, String> result) {
        this.result = result;
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("jsonrpc", jsonRpc)
                .append("id", id)
                .append("result", result)
                .toString();
    }

}
