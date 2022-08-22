package net.ripe.db.whois.api.zonemasterservice;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

@JsonInclude(NON_EMPTY)
public class ZonemasterResponseDTO {

    private String id;

    private String method;

    private Params params;

    private String jsonrpc;

    public String getId() {
        return id;
    }

    public String getMethod() {
        return method;
    }

    public String getDomainParam() {
        return params.getDomain();
    }

    private static class Params {
        private String domain;

        private boolean ipv4;

        private boolean ipv6;

        @JsonProperty("client_id")
        private String clientId;

        @JsonProperty("client_version")
        private String clientVersion;


        private final List<Nameservers> nameservers = new ArrayList<>();

        @JsonProperty("ds_info")
        private List<DsInfo> dsInfo = new ArrayList<>();
        private static class Nameservers {
            private String ns;
        }

        private static class DsInfo {

            private int algorithm;

            private String digest;

            @JsonProperty("keytag")
            private String keyTag;

            @JsonProperty("digtype")
            private String digType;
        }

        public String getDomain() {
            return domain;
        }
    }

}


