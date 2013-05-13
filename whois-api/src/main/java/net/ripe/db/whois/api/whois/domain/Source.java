package net.ripe.db.whois.api.whois.domain;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "grsMirror"
})
@XmlRootElement(name = "source")
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Source {

    @XmlElement(name = "grs-mirror")
    protected List<GrsMirror> grsMirror;
    @XmlAttribute(required = true)
    protected String name;
    @XmlAttribute(required = true)
    protected String id;

    public Source(final String id) {
        this.id = id;
    }

    public Source() {}

    public List<GrsMirror> getGrsMirror() {
        if (grsMirror == null) {
            grsMirror = new ArrayList<GrsMirror>();
        }
        return this.grsMirror;
    }

    public String getId() {
        return id;
    }

    public Source setId(String value) {
        this.id = value;
        return this;
    }

    public String getName() {
        return name;
    }

    public Source setName(String value) {
        this.name = value;
        return this;
    }
}
