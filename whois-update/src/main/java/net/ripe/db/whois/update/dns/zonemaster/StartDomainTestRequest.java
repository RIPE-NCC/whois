package net.ripe.db.whois.update.dns.zonemaster;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.dns.DnsCheckRequest;

import java.util.Set;

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
class StartDomainTestRequest extends ZonemasterRequestSupport {

    StartDomainTestRequest(final DnsCheckRequest dnsCheckRequest) {
        init(dnsCheckRequest);
    }

    private void init(final DnsCheckRequest dnsCheckRequest) {
        json = JsonNodeFactory.instance.objectNode()
                .put("jsonrpc", "2.0")
                .put("id", 4)
                .put("method", "start_domain_test");
        ObjectNode params = JsonNodeFactory.instance.objectNode()
                .put("domain", dnsCheckRequest.getDomain())
                .put("config", "predelegation_config") // Special flag for ns.ripe.net
                .put("ipv6", true)
                .put("ipv4", true);
        dsRdataArray = params.putArray("ds_info");
        nameservers = params.putArray("nameservers");
        json.putObject("params").setAll(params);

        RpslObject rpslObject = dnsCheckRequest.getUpdate().getSubmittedObject();

        if (rpslObject.containsAttribute(AttributeType.NSERVER)) {
            parseNameservers(rpslObject.getValuesForAttribute(AttributeType.DS_RDATA));
        }

        if (rpslObject.containsAttribute(AttributeType.DS_RDATA)) {
            parseDsRdata(rpslObject.getValuesForAttribute(AttributeType.DS_RDATA));
        }
    }

    private void parseNameservers(final Set<CIString> nserverValues) {
        for (CIString nserverValue : nserverValues) {
            String cleanNs = nserverValue.toString().trim();
            String[] splits = cleanNs.split(" ");
            if (splits.length > 1) {
                // contains both name and ip address
                addNameserver(splits[0], splits[1]);
            } else {
                addNameserver(splits[0]);
            }
        }
    }

    private void parseDsRdata(final Set<CIString> dsRdata) {
        for (CIString dsRdataLine : dsRdata) {
            String[] dsParts = dsRdataLine.toString().trim().split(" ");
            if (dsParts.length == 4) {
                ObjectNode dsRdataNode = JsonNodeFactory.instance.objectNode();
                dsRdataNode.put("keytag", dsParts[0])
                        .put("algorithm", dsParts[1])
                        .put("digtype", dsParts[2])
                        .put("digest", dsParts[3]);
                dsRdataArray.add(dsRdataNode);
            } else {
                // doesn't look like good dsRdata. now what?
            }
        }
    }

    private void addNameserver(final String nameserver) {
        addNameserver(nameserver, null);
    }

    private void addNameserver(final String nameserver, final String ip) {
        ObjectNode nsNode = JsonNodeFactory.instance.objectNode();
        nsNode.put("ns", nameserver);
        if (ip != null) {
            nsNode.put("ip", ip);
        }
        nameservers.add(nsNode);
    }

    public ObjectNode json() {
        return json;
    }

    private ObjectNode json;
    private ArrayNode nameservers;
    private ArrayNode dsRdataArray;

}
