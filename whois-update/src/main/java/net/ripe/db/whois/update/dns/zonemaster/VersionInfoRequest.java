package net.ripe.db.whois.update.dns.zonemaster;

/**
 * Taken from Zonemaster documentation
 *
 * @see <a href="https://github.com/dotse/zonemaster-backend/blob/master/docs/API.md">Zonemaster documentation</a>
 */
class VersionInfoRequest extends ZonemasterRequestSupport {

    private final Request request;

    VersionInfoRequest() {
        this.request = new Request();
        this.request.setMethod(Request.Method.VERSION_INFO);
    }

    @Override
    public Request getRequest() {
        return this.request;
    }

}
