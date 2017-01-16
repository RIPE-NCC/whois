package net.ripe.db.whois.update.dns.zonemaster.domain;

/**
 * Taken from Zonemaster documentation
 *
 * @see <a href="https://github.com/dotse/zonemaster-backend/blob/master/docs/API.md">Zonemaster documentation</a>
 */
public class TestProgressRequest implements ZonemasterRequest {

    final Request request;

    public TestProgressRequest(final String id) {
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
