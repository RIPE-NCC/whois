package net.ripe.db.whois.api.rest.domain;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Lists;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "grsMirror"
})
@XmlRootElement(name = "source")
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Source {

    @XmlElement(name = "grs-mirror")
    private List<GrsMirror> grsMirror = Lists.newArrayList();
    @XmlAttribute(required = true)
    private String name;
    @XmlAttribute(required = true)
    private String id;

    public Source(final String id) {
        this.id = id;
    }

    public Source() {
        // default no-arg constructor
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
