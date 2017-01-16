package net.ripe.db.whois.update.dns.zonemaster.domain;

import com.google.common.base.MoreObjects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "request")
public class Request {

    @XmlElement(name = "jsonrpc")
    private String jsonRpc = "2.0";

    @XmlElement
    private int id;

    @XmlElement
    private String method;

    @XmlElement
    private Params params;

    public void setParams(final Params params) {
        this.params = params;
    }

    public void setMethod(final Request.Method method) {
        this.method = method.getMethod();
        this.id = method.getId();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("params", params)
            .add("method", method)
            .add("id", id)
            .toString();
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "params")
    public static class Params {

        @XmlElement(name = "client_id")
        private String clientId = "Zonemaster Dancer Frontend";

        @XmlElement
        private String domain;

        @XmlElement
        private String profile = "default_profile";

        @XmlElement(name = "client_version")
        private String clientVersion = "1.0.1";

        @XmlElement
        private String config;

        @XmlElement
        private String id;

        @XmlElement
        private String language;

        @XmlElements({@XmlElement(name = "nameservers", type = Nameserver.class)})
        private List<Nameserver> nameservers;

        @XmlElements({@XmlElement(name = "ds_info", type = DsInfo.class)})
        private List<DsInfo> dsInfos;

        @XmlElement
        private boolean advanced = true;

        @XmlElement
        private boolean ipv4 = true;

        @XmlElement
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

        public void setConfig(final String config) {
            this.config = config;
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

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "nameserver")
    public static class Nameserver {

        @XmlElement(name = "ip")
        private String ip;

        @XmlElement(name = "ns")
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

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "dsinfo")
    public static class DsInfo {
        @XmlElement(name = "keytag")
        private String keyTag;

        @XmlElement
        private String algorithm;

        @XmlElement(name = "digtype")
        private String digestType;

        @XmlElement
        private String digest;

        public DsInfo(final String keyTag, final String algorithm, final String digestType, final String digest) {
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

    public enum Method {
        VERSION_INFO("version_info", 1),
        GET_NS_IPS("get_ns_ips", 2),
        GET_DATA_FROM_PARENT_ZONE("get_data_from_parent_zone", 3),
        START_DOMAIN_TEST("start_domain_test", 4),
        TEST_PROGRESS("test_progress", 5),
        GET_TEST_RESULTS("get_test_result", 6),
        GET_TEST_HISTORY("get_test_history", 7);

        private String method;
        private int id;

        Method(final String method, final int id) {
            this.method = method;
            this.id = id;
        }

        public String getMethod() {
            return method;
        }

        public int getId() {
            return id;
        }
    }
}
