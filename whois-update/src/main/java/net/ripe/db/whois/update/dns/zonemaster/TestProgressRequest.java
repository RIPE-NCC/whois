package net.ripe.db.whois.update.dns.zonemaster;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Taken from Zonemaster documentation
 *
 * @see <a href="https://github.com/dotse/zonemaster-backend/blob/master/docs/API.md">Zonemaster documentation</a>
 */
class TestProgressRequest extends ZonemasterRequestSupport {

    TestProgressRequest(final String id) {
        JsonNodeFactory factory = JsonNodeFactory.instance;
        json = factory.objectNode()
                .put("jsonrpc", "2.0")
                .put("id", 5)
                .put("method", "test_progress")
                .put("params", id);
    }

    public ObjectNode json() {
        return json;
    }

    private ObjectNode json;

}
