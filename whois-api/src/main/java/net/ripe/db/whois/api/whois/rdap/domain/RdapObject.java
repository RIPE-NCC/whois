package net.ripe.db.whois.api.whois.rdap.domain;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Lists;

import javax.xml.bind.annotation.*;
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
    "notices",
    "port43"
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
    protected String lang;                                  // TODO: [ES] one lang in RDAP but multiple lang attributes possible in rpslobject
    @XmlElement(required = true)
    protected List<String> rdapConformance;
    protected List<Notice> notices;
    protected String port43;

    public List<Object> getStatus() {
        if (status == null) {
            status = Lists.newArrayList();
        }
        return this.status;
    }

    public List<Entity> getEntitySearchResults() {
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

    public String getPort43() {
        return port43;
    }

    public void setPort43(final String value) {
        this.port43 = value;
    }
}
