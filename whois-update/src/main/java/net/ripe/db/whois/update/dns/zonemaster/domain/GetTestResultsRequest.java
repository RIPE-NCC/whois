package net.ripe.db.whois.update.dns.zonemaster.domain;

/**
 * Taken from Zonemaster documentation
 *
 * @see <a href="https://github.com/dotse/zonemaster-backend/blob/master/docs/API.md">Zonemaster documentation</a>
 */
public class GetTestResultsRequest extends ZonemasterRequest {

    private static final String LANGUAGE = "en";

    public GetTestResultsRequest(final String id) {
        super.setMethod(ZonemasterRequest.Method.GET_TEST_RESULTS);
        final ZonemasterRequest.Params params = new ZonemasterRequest.Params();
        params.setId(id);
        params.setLanguage(LANGUAGE);
        super.setParams(params);
    }
}
