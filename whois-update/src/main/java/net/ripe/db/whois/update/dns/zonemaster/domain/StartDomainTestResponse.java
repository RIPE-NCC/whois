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
}
