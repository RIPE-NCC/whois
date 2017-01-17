package net.ripe.db.whois.update.dns.zonemaster.domain;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

public class StartDomainTestResponse extends ZonemasterResponse {

    @JsonProperty
    private String result;

    /**
     * Percentage complete for Start Domain Test in progress
     * @return
     */
    public String getResult() {
        return result;
    }

    @Override
    protected MoreObjects.ToStringHelper toStringHelper() {
        return super.toStringHelper()
                .add("result", result);
    }

    public static class Error {
        private String message;
        private int code;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                .add("message", message)
                .add("code", code)
                .toString();
        }
    }
}
