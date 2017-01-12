package net.ripe.db.whois.update.dns.zonemaster;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Map;

public class VersionInfoResponse {

    public String getJsonrpc() {
        return jsonrpc;
    }

    public void setJsonrpc(final String jsonrpc) {
        this.jsonrpc = jsonrpc;
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
                .append("jsonrpc", jsonrpc)
                .append("id", id)
                .append("result", result)
                .toString();
    }

    private String jsonrpc;
    private String id;
    private Map<String, String> result;
}
