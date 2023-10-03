package net.ripe.db.whois.api.rdap.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hazelcast.com.jayway.jsonpath.JsonPath;
import com.hazelcast.org.apache.calcite.runtime.Resources;
import jakarta.ws.rs.DefaultValue;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

import java.io.Serializable;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Redaction", propOrder = {
        "name",
        "reason",
        "prePath",
        "method"
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Redaction implements Serializable {

    private Description name;

    private Description reason;

    private String prePath;

    private String method;

    public Redaction() {
        // required no-arg constructor
    }
    public Redaction(final Description name, final Description reason){
        this.name = name;
        this.reason = reason;
        this.method = "removal";
    }

    public Description getName() {
        return name;
    }

    public Description getReason() {
        return reason;
    }

    public String getMethod() {
        return method;
    }

    public String getPrePath() {
        return prePath;
    }

    public void setPrePath(final String prePath) {
        this.prePath = prePath;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "description"
    })
    public static class Description implements Serializable {

        private String description;

        public Description() {
            // required no-arg constructor
        }

        public Description(final String description){
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
