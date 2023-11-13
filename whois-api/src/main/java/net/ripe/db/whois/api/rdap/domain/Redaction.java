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
        "postPath",
        "pathLang",
        "method"
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Redaction implements Serializable {
    @XmlElement(required = true)
    private Description name;

    private Description reason;

    private String prePath;
    private String postPath;
    private String pathLang;

    private String method;

    public Redaction() {
        // required no-arg constructor
    }
    private Redaction(final String name, final String prePath, final String postPath, final String pathLang, final String reason, final String method){
        this.name = new Description(name);
        this.prePath = prePath;
        this.postPath = postPath;
        this.pathLang = pathLang;
        this.reason = new Description(reason);
        this.method = method;
    }

    public Description getName() {
        return name;
    }

    public Description getReason() {
        return reason;
    }

    public String getPostPath() {
        return postPath;
    }

    public String getMethod() {
        return method;
    }

    public String getPrePath() {
        return prePath;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Redaction redaction = (Redaction) o;
        return name.equals(redaction.name) && reason.equals(redaction.reason) && Objects.equals(prePath, redaction.prePath) && Objects.equals(postPath, redaction.postPath) && Objects.equals(pathLang, redaction.pathLang) && method.equals(redaction.method);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, reason, prePath, postPath, pathLang, method);
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

    public static Redaction getRedactionByRemoval(final String name, final String prePath, final String reason) {
        return  new Redaction(name, prePath, null, null, reason, "removal");
    }

    public static Redaction getRedactionByPartialValue(final String name, final String postPath, final String reason) {
        return  new Redaction(name, null, postPath, "jsonpath", reason, "partialValue");
    }

}
