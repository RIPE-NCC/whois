package net.ripe.db.whois.api.whois.rdap.domain;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "remark", propOrder = {
    "description"
})
@JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY)
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
