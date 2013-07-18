package net.ripe.db.whois.api.whois.rdap.domain;

import com.google.common.collect.Lists;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "rdapObject", propOrder = {
    "status",
    "entities",
    "remarks",
    "links",
    "events",
    "lang",
    "rdapConformance",
    "notices"
})
@XmlSeeAlso({
    Nameserver.class,
    Entity.class,
    Ip.class,
    Autnum.class,
    Domain.class
})
@XmlRootElement
@JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY)
public class RdapObject implements Serializable {
    protected List<Object> status;
    protected List<Entity> entities;
    protected List<Remark> remarks;
    protected List<Link> links;
    protected List<Event> events;
    protected String lang;
    @XmlElement(required = true)
    protected List<String> rdapConformance;
    protected List<Notice> notices;

    public List<Object> getStatus() {
        if (status == null) {
            status = Lists.newArrayList();
        }
        return this.status;
    }

    public List<Entity> getEntities() {
        if (entities == null) {
            entities = Lists.newArrayList();
        }
        return this.entities;
    }

    public List<Remark> getRemarks() {
        if (remarks == null) {
            remarks = Lists.newArrayList();
        }
        return this.remarks;
    }

    public List<Link> getLinks() {
        if (links == null) {
            links = Lists.newArrayList();
        }
        return this.links;
    }

    public List<Event> getEvents() {
        if (events == null) {
            events = Lists.newArrayList();
        }
        return this.events;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(final String value) {
        this.lang = value;
    }

    public List<String> getRdapConformance() {
        if (rdapConformance == null) {
            rdapConformance = Lists.newArrayList();
        }
        return this.rdapConformance;
    }

    public List<Notice> getNotices() {
        if (notices == null) {
            notices = Lists.newArrayList();
        }
        return this.notices;
    }
}
