package net.ripe.db.whois.update.dns.zonemaster;

/**
 * Taken from Zonemaster documentation
 *
 * @see <a href="https://github.com/dotse/zonemaster-backend/blob/master/docs/API.md">Zonemaster documentation</a>
 */
class TestProgressRequest extends ZonemasterRequestSupport {

    final Request request;

    TestProgressRequest(final String id) {
        this.request = new Request();
        this.request.setMethod(Request.Method.TEST_PROGRESS);
        final Request.Params params = new Request.Params();
        params.setId(id);
    }

    @Override
    public Request getRequest() {
        return request;
    }
}
