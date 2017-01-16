package net.ripe.db.whois.update.dns.zonemaster.domain;


import javax.annotation.Nullable;

public class StartDomainTestResponse {

    private String jsonrpc;
    private String id;
    private String result;
    private Error error;

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

    /**
     * Percentage complete for Start Domain Test in progress
     * @return
     */
    public String getResult() {
        return result;
    }

    public void setResult(final String result) {
        this.result = result;
    }

    @Nullable
    public Error getError() {
        return error;
    }

    static class Error {
        private String message;
        private int code;
    }
}
