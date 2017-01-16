package net.ripe.db.whois.update.dns.zonemaster.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "response")
public abstract class ZonemasterResponse {

    @JsonProperty("jsonrpc")
    private String jsonRpc;

    private String id;

    public String getJsonRpc() {
        return jsonRpc;
    }

    public String getId() {
        return id;
    }

    protected MoreObjects.ToStringHelper toStringHelper() {
        return MoreObjects.toStringHelper(this)
                .add("jsonRpc", jsonRpc)
                .add("id", id);
    }

    @Override
    public String toString() {
        return toStringHelper().toString();
    }
}
