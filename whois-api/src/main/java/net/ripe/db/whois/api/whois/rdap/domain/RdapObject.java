package net.ripe.db.whois.api.whois.rdap.domain;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.ArrayList;
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
            status = new ArrayList<>();
        }
        return this.status;
    }

    public List<Entity> getEntities() {
        if (entities == null) {
            entities = new ArrayList<>();
        }
        return this.entities;
    }

    public List<Remark> getRemarks() {
        if (remarks == null) {
            remarks = new ArrayList<>();
        }
        return this.remarks;
    }

    public List<Link> getLinks() {
        if (links == null) {
            links = new ArrayList<>();
        }
        return this.links;
    }

    public List<Event> getEvents() {
        if (events == null) {
            events = new ArrayList<>();
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
            rdapConformance = new ArrayList<>();
        }
        return this.rdapConformance;
    }

    public List<Notice> getNotices() {
        if (notices == null) {
            notices = new ArrayList<>();
        }
        return this.notices;
    }
}
