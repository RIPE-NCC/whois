package net.ripe.db.whois.update.dns.zonemaster.domain;


import com.google.common.base.MoreObjects;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "response")
public class StartDomainTestResponse extends ZonemasterResponse {

    private String result;
    private Error error;

    /**
     * Percentage complete for Start Domain Test in progress
     * @return
     */
    public String getResult() {
        return result;
    }

    @Nullable
    public Error getError() {
        return error;
    }

    @Override
    protected MoreObjects.ToStringHelper toStringHelper() {
        return super.toStringHelper()
                .add("result", result)
                .add("error", error);
    }

    public static class Error {
        private String message;
        private int code;

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                .add("message", message)
                .add("code", code)
                .toString();
        }
    }
}
