package net.ripe.db.whois.update.dns.zonemaster.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

import javax.annotation.Nullable;

/**
 * Base entity class for Zonemaster API method responses.
 *
 * @see <a href="https://github.com/dotse/zonemaster-backend/blob/master/docs/API.md">Zonemaster documentation</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class ZonemasterResponse {

    @JsonProperty("jsonrpc")
    private String jsonRpc;
    @JsonProperty
    private String id;
    @JsonProperty
    private Error error;

    public String getJsonRpc() {
        return jsonRpc;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public Error getError() {
        return error;
    }

    protected MoreObjects.ToStringHelper toStringHelper() {
        return MoreObjects.toStringHelper(this)
                .add("jsonRpc", jsonRpc)
                .add("id", id)
                .add("error", error);
    }

    @Override
    public String toString() {
        return toStringHelper().toString();
    }

    public static class Error {
        private String message;
        private int code;

        private String data;

        public String getMessage() {
            return message;
        }

        public int getCode() {
            return code;
        }

        public String getData() {
            return data;
        }
        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("message", message)
                    .add("code", code)
                    .add("data", data)
                    .toString();
        }
    }

}
