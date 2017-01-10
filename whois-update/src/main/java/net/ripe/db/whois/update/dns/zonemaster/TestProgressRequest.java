package net.ripe.db.whois.update.dns.zonemaster;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Taken from Zonemaster documentation
 *
 * @see <a href="https://github.com/dotse/zonemaster-backend/blob/master/docs/API.md">Zonemaster documentation</a>
 */
class TestProgressRequest {

    TestProgressRequest(String id) {
        JsonNodeFactory factory = JsonNodeFactory.instance;
        json = factory.objectNode()
                .put("jsonrpc", "2.0")
                .put("id", 5)
                .put("method", "test_progress")
                .put("params", id);
    }

    String asJson() {
        return json.toString();
    }

    public static void main(String[] args) {
        TestProgressRequest req = new TestProgressRequest("old macdonald had a farm");
        System.out.println("request:\n" + req.asJson());
    }

    private ObjectNode json;

}
