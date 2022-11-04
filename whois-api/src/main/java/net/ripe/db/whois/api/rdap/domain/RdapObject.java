package net.ripe.db.whois.api.rdap.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Lists;

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
    "networks",
    "autnums",
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
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RdapObject implements Serializable {
    protected List<Ip> networks;
    protected List<Autnum> autnums;
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
    protected String objectClassName;

    protected Integer errorCode;
    @XmlElement(name = "title")
    protected String errorTitle;
    @XmlElement(name = "description")
    protected List<String> errorDescription;

    public final List<Object> getStatus() {
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

    public final List<Remark> getRemarks() {
        if (remarks == null) {
            remarks = Lists.newArrayList();
        }
        return this.remarks;
    }

    public final List<Link> getLinks() {
        if (links == null) {
            links = Lists.newArrayList();
        }
        return this.links;
    }

    public final List<Event> getEvents() {
        if (events == null) {
            events = Lists.newArrayList();
        }
        return this.events;
    }

    public final String getLang() {
        return lang;
    }

    public final List<Ip> getNetworks() {
        if (networks == null) {
            networks = Lists.newArrayList();
        }
        return networks;
    }

    public final List<Autnum> getAutnums() {
        if (autnums == null) {
            autnums = Lists.newArrayList();
        }
        return autnums;
    }

    public final void setLang(final String value) {
        this.lang = value;
    }

    public final void setStatus(List<Object> status) {
        this.status = status;
    }

    public final List<String> getRdapConformance() {
        if (rdapConformance == null) {
            rdapConformance = Lists.newArrayList();
        }
        return this.rdapConformance;
    }

    public final List<Notice> getNotices() {
        if (notices == null) {
            notices = Lists.newArrayList();
        }
        return this.notices;
    }

    public final String getPort43() {
        return port43;
    }

    public final void setPort43(final String value) {
        this.port43 = value;
    }

    public final Integer getErrorCode() {
        return errorCode;
    }

    public final void setErrorCode(final Integer errorCode) {
        this.errorCode = errorCode;
    }

    public final String getErrorTitle() {
        return errorTitle;
    }

    public final void setErrorTitle(final String errorTitle) {
        this.errorTitle = errorTitle;
    }

    public final List<String> getDescription() {
        if (errorDescription == null) {
            errorDescription = Lists.newArrayList();
        }
        return this.errorDescription;
    }

    public final void setDescription(final List<String> description) {
        this.errorDescription = description;
    }

    public final String getObjectClassName() {
        return objectClassName;
    }

    public final void setObjectClassName(final String value) {
        this.objectClassName = value;
    }

    public final void setAutnums(final List<Autnum> autnums){
        this.autnums = autnums;
    }

    public final void setIpv4(List<Ip> ipv4Networks) {
        if (networks == null) {
            networks = Lists.newArrayList();
        }
        networks.addAll(ipv4Networks);
    }

    public final void setIpv6(List<Ip> ipv6Networks) {
        if (networks == null) {
            networks = Lists.newArrayList();
        }
        networks.addAll(ipv6Networks);
    }
}
