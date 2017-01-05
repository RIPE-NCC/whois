package net.ripe.db.whois.update.dns;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Taken from Zonemaster documentation
 *
 * @see <a href="https://github.com/dotse/zonemaster-backend/blob/master/docs/API.md">Zonemaster documentation</a>
 * <p>
 * <pre>
 * "client_id": A free-form string, optional.
 * "domain": A domain name, required.
 * "profile": A profile name, optional.
 * "client_version": A free-form string, optional.
 * "nameservers": A list of name server objects, optional.
 * "ds_info": A list of DS info objects, optional.
 * "advanced": Deprecated. A boolean, optional.
 * "ipv6": A boolean, optional. (default false)
 * "ipv4": A boolean, optional. (default false)
 * "config": A string, optional. The name of a config profile.
 * "user_ip": A ..., optional.
 * "user_location_info": A ..., optional.
 * "priority": A priority, optional
 * "queue": A queue, optional
 * </pre>
 * <p>
 * <pre>
 *     {
 *        "jsonrpc": "2.0",
 *        "id": 4,
 *        "method": "start_domain_test",
 *        "params": {
 *            "client_id": "Zonemaster Dancer Frontend",
 *            "domain": "zonemaster.net",
 *            "profile": "default_profile",
 *            "client_version": "1.0.1",
 *            "nameservers": [{
 *                "ip": "2001:67c:124c:2007::45",
 *                "ns": "ns3.nic.se"
 *            },{
 *                "ip": "192.93.0.4",
 *                "ns": "ns2.nic.fr"
 *            }],
 *            "ds_info": [],
 *            "advanced": true,
 *            "ipv6": true,
 *            "ipv4": true
 *          }
 *      }
 * </pre>
 */
class ZonemasterStartDomainTestRequest {

    ZonemasterStartDomainTestRequest() {
        json = JsonNodeFactory.instance.objectNode()
                .put("jsonrpc", "2.0")
                .put("id", 4)
                .put("method", "start_domain_test");
        ObjectNode params = JsonNodeFactory.instance.objectNode()
                .put("client_id", "Zonemaster Dancer Frontend")
                .put("domain", "zonemaster.net")
                .put("profile", "default_profile")
                .put("client_version", "1.0.1")
                .put("advanced", true)
                .put("ipv6", true)
                .put("ipv4", true);
        params.putArray("ds_info");
        nameservers = params.putArray("nameservers");
        json.putObject("params").setAll(params);
    }

    ZonemasterStartDomainTestRequest addNameserver(final String ip, final String nameserver) {
        ObjectNode nsNode = JsonNodeFactory.instance.objectNode();
        nsNode.put("ip", ip);
        nsNode.put("ns", nameserver);
        nameservers.add(nsNode);
        return this;
    }

    String asJson() {
        return json.toString();
    }

    public static void main(String[] args) {
        ZonemasterStartDomainTestRequest req = new ZonemasterStartDomainTestRequest();
        req.addNameserver("1.1.1.1", "ns.whereever.ns").addNameserver("2.2.2.2", "buckle.my.shoe");
        System.out.println("request:\n" + req.asJson());
    }

    private ObjectNode json;
    private ArrayNode nameservers;

}
