package net.ripe.db.whois.update.dns.zonemaster.domain;

/**
 * Taken from Zonemaster documentation
 *
 * @see <a href="https://github.com/dotse/zonemaster-backend/blob/master/docs/API.md">Zonemaster documentation</a>
 */
public class GetTestResultsRequest implements ZonemasterRequest {

    final Request request;

    public GetTestResultsRequest(String id) {
        this.request = new Request();
        request.setMethod(Request.Method.GET_TEST_RESULTS);
        final Request.Params params = new Request.Params();
        params.setId(id);
        params.setLanguage("en");
    }

    @Override
    public Request getRequest() {
        return request;
    }
}
