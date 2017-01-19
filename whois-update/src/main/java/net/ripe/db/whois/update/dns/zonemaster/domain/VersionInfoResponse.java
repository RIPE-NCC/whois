package net.ripe.db.whois.update.dns.zonemaster.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.MoreObjects;

import java.util.Map;

/**
 * Entity class for Zonemaster version_info API method response.
 *
 * @see <a href="https://github.com/dotse/zonemaster-backend/blob/master/docs/API.md">Zonemaster documentation</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class VersionInfoResponse extends ZonemasterResponse {

    private Map<String, String> result;

    public Map<String, String> getResult() {
        return result;
    }

    @Override
    protected MoreObjects.ToStringHelper toStringHelper() {
        return super.toStringHelper()
            .add("result", result);
    }
}
