package net.ripe.db.whois.update.dns.zonemaster.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.MoreObjects;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VersionInfoResponse extends ZonemasterResponse {

    private Map<String, String> result;

    public Map<String, String> getResult() {
        return result;
    }

    public void setResult(final Map<String, String> result) {
        this.result = result;
    }

    @Override
    protected MoreObjects.ToStringHelper toStringHelper() {
        return super.toStringHelper()
            .add("result", result);
    }
}
