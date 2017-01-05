package net.ripe.db.whois.update.dns;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Taken from Zonemaster documentation
 *
 * @see <a href="https://github.com/dotse/zonemaster-backend/blob/master/docs/API.md">Zonemaster documentation</a>
 */
class ZonemasterGetTestResultsRequest {

    ZonemasterGetTestResultsRequest(String id) {
        JsonNodeFactory factory = JsonNodeFactory.instance;
        json = factory.objectNode()
                .put("jsonrpc", "2.0")
                .put("id", 6)
                .put("method", "get_test_results");
        json.putObject("params").put("id", id).put("language", "en");
    }

    String asJson() {
        return json.toString();
    }

    public static void main(String[] args) {
        ZonemasterGetTestResultsRequest req = new ZonemasterGetTestResultsRequest("i ij i ij oo");
        System.out.println("request:\n" + req.asJson());
    }

    private ObjectNode json;

}
