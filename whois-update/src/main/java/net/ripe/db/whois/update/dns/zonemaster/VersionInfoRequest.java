package net.ripe.db.whois.update.dns.zonemaster;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Taken from Zonemaster documentation
 *
 * @see <a href="https://github.com/dotse/zonemaster-backend/blob/master/docs/API.md">Zonemaster documentation</a>
 */
class VersionInfoRequest implements ZonemasterRequest {

    VersionInfoRequest() {
        JsonNodeFactory factory = JsonNodeFactory.instance;
        json = factory.objectNode()
                .put("jsonrpc", "2.0")
                .put("id", 1)
                .put("method", "version_info");
    }

    public String asJson() {
        return json.toString();
    }

    private ObjectNode json;

}
