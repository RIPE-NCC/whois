package net.ripe.db.whois.update.dns.zonemaster.domain;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.dns.DnsCheckRequest;

import java.util.List;
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
public  class StartDomainTestRequest extends ZonemasterRequest {

    private static Splitter SPACE_SPLITTER = Splitter.on(' ').omitEmptyStrings().trimResults();

    public StartDomainTestRequest(final DnsCheckRequest dnsCheckRequest) {
        super.setMethod(ZonemasterRequest.Method.START_DOMAIN_TEST);

        final ZonemasterRequest.Params params = new ZonemasterRequest.Params();
        params.setDomain(dnsCheckRequest.getDomain());
        params.setConfig("predelegation_config");           // Special flag for ns.ripe.net

        final RpslObject rpslObject = dnsCheckRequest.getUpdate().getSubmittedObject();

        if (rpslObject.containsAttribute(AttributeType.NSERVER)) {
            params.setNameservers(parseNameservers(rpslObject.getValuesForAttribute(AttributeType.DS_RDATA)));      // TODO: is this the correct attribute type?
        }

        if (rpslObject.containsAttribute(AttributeType.DS_RDATA)) {
            params.setDsInfos(parseDsRdata(rpslObject.getValuesForAttribute(AttributeType.DS_RDATA)));
        }

        super.setParams(params);
    }

    private List<ZonemasterRequest.Nameserver> parseNameservers(final Set<CIString> nserverValues) {
        final List<ZonemasterRequest.Nameserver> nameservers = Lists.newArrayList();
        for (CIString nserverValue : nserverValues) {
            final List<String> splits = SPACE_SPLITTER.splitToList(nserverValue.toString().trim());
            nameservers.add(new ZonemasterRequest.Nameserver(splits.get(0), (splits.size() > 1) ? splits.get(1) : null));
        }
        return nameservers;
    }

    private List<ZonemasterRequest.DsInfo> parseDsRdata(final Set<CIString> dsRdata) {
        final List<ZonemasterRequest.DsInfo> dsInfos = Lists.newArrayList();
        for (CIString dsRdataLine : dsRdata) {
            final List<String> dsParts = SPACE_SPLITTER.splitToList(dsRdataLine.toString().trim());
            if (dsParts.size() == 4) {
                dsInfos.add(new ZonemasterRequest.DsInfo(dsParts.get(0), dsParts.get(1), dsParts.get(2), dsParts.get(3)));
            } else {
                // TODO: doesn't look like good dsRdata. now what?
                throw new IllegalArgumentException("invalid dsRdata: " + dsRdataLine);
            }
        }
        return dsInfos;
    }
}
