package net.ripe.db.whois.api.rdap.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "remark", propOrder = {
    "description"
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Remark implements Serializable {
    protected List<String> description;

    protected List<String> remarks;

    public Remark(final List<String> description, final List<String> remarks) {
        this.description = description;
        this.remarks = remarks;
    }

    public Remark() {
        // required no-arg constructor
    }

    public List<String> getDescription() {
        return description;
    }

    public List<String> getRemarks() {
        return remarks;
    }
}
