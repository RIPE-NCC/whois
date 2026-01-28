package net.ripe.db.whois.rdap.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import java.io.Serializable;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "publicIds", propOrder = {
        "type",
        "identifier"
})
@XmlRootElement
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class PublicIds implements Serializable {

    private String type;

    private String identifier;
    
    public PublicIds() {
        // required for deserialization
    }

    public PublicIds(String type, String identifier) {
        this.type = type;
        this.identifier = identifier;
    }

    public String getType() {
        return type;
    }

    public String getIdentifier() {
        return identifier;
    }
}
