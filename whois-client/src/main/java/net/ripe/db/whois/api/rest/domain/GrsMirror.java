package net.ripe.db.whois.api.rest.domain;

import javax.xml.bind.annotation.*;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "grs-mirror")
public class GrsMirror {

    @XmlAttribute(name = "id", required = true)
    protected String id;

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

}
