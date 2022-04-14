package net.ripe.db.whois.update.dns.zonemaster.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.google.common.base.MoreObjects;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.attrs.AttributeParseException;
import net.ripe.db.whois.common.rpsl.attrs.DsRdata;
import net.ripe.db.whois.update.dns.DnsCheckRequest;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Entity class for Zonemaster start_domain_test API method request.
 *
 * @see <a href="https://github.com/dotse/zonemaster-backend/blob/master/docs/API.md">Zonemaster documentation</a>
 */
public  class StartDomainTestRequest extends ZonemasterRequest {

    private static Splitter SPACE_SPLITTER = Splitter.on(' ').omitEmptyStrings().trimResults();


    @JsonProperty
    private Params params;

    public StartDomainTestRequest(final DnsCheckRequest dnsCheckRequest) {
        setMethod(StartDomainTestRequest.Method.START_DOMAIN_TEST);

        final StartDomainTestRequest.Params params = new StartDomainTestRequest.Params();
        params.setDsInfos(Collections.emptyList());
        params.setNameservers(Collections.emptyList());
        params.setDomain(dnsCheckRequest.getDomain());

        final RpslObject rpslObject = dnsCheckRequest.getUpdate().getSubmittedObject();

        if (rpslObject.containsAttribute(AttributeType.NSERVER)) {
            params.setNameservers(parseNameservers(rpslObject.getValuesForAttribute(AttributeType.NSERVER)));
        }

        if (rpslObject.containsAttribute(AttributeType.DS_RDATA)) {
            params.setDsInfos(parseDsRdata(rpslObject.getValuesForAttribute(AttributeType.DS_RDATA)));
        }


        this.params = params;
    }

    // TODO: [ES] replace STRING_SPLITTER with NServer.parse()
    private List<StartDomainTestRequest.Nameserver> parseNameservers(final Set<CIString> nserverValues) {
        final List<StartDomainTestRequest.Nameserver> nameservers = Lists.newArrayList();
        for (CIString nserverValue : nserverValues) {
            final List<String> splits = SPACE_SPLITTER.splitToList(nserverValue.toString().trim());
            nameservers.add(new StartDomainTestRequest.Nameserver(splits.get(0), (splits.size() > 1) ? splits.get(1) : null));
        }
        return nameservers;
    }

    private List<StartDomainTestRequest.DsInfo> parseDsRdata(final Set<CIString> dsRdataValues) {
        final List<StartDomainTestRequest.DsInfo> dsInfos = Lists.newArrayList();
        for (CIString dsRdataValue : dsRdataValues) {
            try {
                final DsRdata dsRdata = DsRdata.parse(dsRdataValue);
                dsInfos.add(new StartDomainTestRequest.DsInfo(dsRdata.getKeytag(), dsRdata.getAlgorithm(), dsRdata.getDigestType(), dsRdata.getDigestHexString()));
            } catch (AttributeParseException e) {
                // this should not happen: ds-rdata attributes have already been validated
                throw new IllegalArgumentException("invalid dsRdata " + dsRdataValue + ": " + e.getMessage());
            }
        }
        return dsInfos;
    }

    @Override
    protected MoreObjects.ToStringHelper toStringHelper() {
        return super.toStringHelper()
                .add("params", params);
    }

    @JsonRootName("params")
    public static class Params {
        @JsonProperty("client_id")
        private String clientId = "Whois";
        @JsonProperty
        private String domain;
        @JsonProperty("client_version")
        private String clientVersion = "1.0";
        @JsonProperty
        private String id;
        @JsonProperty
        private String language;
        @JsonProperty("nameservers")
        private List<Nameserver> nameservers;
        @JsonProperty("ds_info")
        private List<DsInfo> dsInfos;
        @JsonProperty
        private boolean ipv4 = true;
        @JsonProperty
        private boolean ipv6 = true;

        public void setNameservers(final List<Nameserver> nameservers) {
            this.nameservers = nameservers;
        }

        public void setDsInfos(final List<DsInfo> dsInfos) {
            this.dsInfos = dsInfos;
        }

        public void setId(final String id) {
            this.id = id;
        }

        public void setLanguage(final String language) {
            this.language = language;
        }

        public void setDomain(final String domain) {
            this.domain = domain;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("nameservers", nameservers)
                    .add("dsinfos", dsInfos)
                    .add("id", id)
                    .add("language", language)
                    .toString();
        }
    }

    public static class Nameserver {
        @JsonProperty
        private String ip;
        @JsonProperty("ns")
        private String nameserver;

        public Nameserver(final String nameserver, final String ip) {
            this.nameserver = nameserver;
            this.ip = ip;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("ip", ip)
                    .add("ns", nameserver)
                    .toString();
        }
    }

    public static class DsInfo {
        @JsonProperty("keytag")
        private int keyTag;
        @JsonProperty
        private int algorithm;
        @JsonProperty("digtype")
        private int digestType;
        @JsonProperty
        private String digest;

        public DsInfo(final int keyTag, final int algorithm, final int digestType, final String digest) {
            this.keyTag = keyTag;
            this.algorithm = algorithm;
            this.digestType = digestType;
            this.digest = digest;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .toString();
        }
    }
}
