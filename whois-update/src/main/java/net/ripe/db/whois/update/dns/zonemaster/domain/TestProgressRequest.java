package net.ripe.db.whois.update.dns.zonemaster.domain;

/**
 * Taken from Zonemaster documentation
 *
 * @see <a href="https://github.com/dotse/zonemaster-backend/blob/master/docs/API.md">Zonemaster documentation</a>
 */
public class TestProgressRequest extends ZonemasterRequest {

    public TestProgressRequest(final String id) {
        super.setMethod(ZonemasterRequest.Method.TEST_PROGRESS);
        final ZonemasterRequest.Params params = new ZonemasterRequest.Params();
        params.setId(id);
        super.setParams(params);
    }
}
