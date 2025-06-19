package net.ripe.db.whois.rdap.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "remark", propOrder = {
    "description"
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Remark implements Serializable {
    protected List<String> description;

    public Remark(final List<String> description) {
        this.description = description;
    }

    public Remark() {
        // required no-arg constructor
    }

    public List<String> getDescription() {
        return description;
    }

}
