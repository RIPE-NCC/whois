package net.ripe.db.whois.update.dns.zonemaster.domain;

/**
 * Entity class for Zonemaster version_info API method request.
 *
 * @see <a href="https://github.com/dotse/zonemaster-backend/blob/master/docs/API.md">Zonemaster documentation</a>
 */
public class VersionInfoRequest extends ZonemasterRequest {

    public VersionInfoRequest() {
        setMethod(ZonemasterRequest.Method.VERSION_INFO);
    }
}
