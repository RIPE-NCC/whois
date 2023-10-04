package net.ripe.db.whois.api.rdap.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

import java.io.Serializable;
import java.util.Objects;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Redaction", propOrder = {
        "name",
        "reason",
        "prePath",
        "method"
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Redaction implements Serializable {
    @XmlElement(required = true)
    private Description name;

    private Description reason;

    @XmlElement(required = true)
    private String prePath;

    private String method;

    public Redaction() {
        // required no-arg constructor
    }
    public Redaction(final Description name, final String prePath, final Description reason){
        this.name = name;
        this.prePath = prePath;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Redaction redaction = (Redaction) o;
        return name.description.equals(redaction.name.description) && Objects.equals(reason.description,
                redaction.reason.description) && Objects.equals(prePath,
                redaction.prePath) && Objects.equals(method, redaction.method);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, reason, prePath, method);
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
