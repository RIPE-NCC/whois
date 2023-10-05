package net.ripe.db.whois.api.rdap.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

import java.io.Serializable;
import java.util.Objects;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "redaction", propOrder = {
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
    public Redaction(final String name, final String prePath, final String reason){
        this.name = new Description(name);
        this.prePath = prePath;
        this.reason = new Description(reason);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Redaction redaction = (Redaction) o;
        return name.description.equals(redaction.name.description) &&
                reason.description.equals(redaction.reason.description) &&
                prePath.equals(redaction.prePath) &&
                method.equals(redaction.method);
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

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Description description1 = (Description) o;
            return description.equals(description1.description);
        }

        @Override
        public int hashCode() {
            return Objects.hash(description);
        }


    }

}
