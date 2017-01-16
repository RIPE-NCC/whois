package net.ripe.db.whois.update.dns.zonemaster;

/**
 * Taken from Zonemaster documentation
 *
 * @see <a href="https://github.com/dotse/zonemaster-backend/blob/master/docs/API.md">Zonemaster documentation</a>
 */
class GetTestResultsRequest extends ZonemasterRequestSupport {

    final Request request;

    GetTestResultsRequest(String id) {
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

    public static void main(String[] args) {
        GetTestResultsRequest req = new GetTestResultsRequest("i ij i ij oo");
        System.out.println("request:\n" + req.getRequest());
    }
}
