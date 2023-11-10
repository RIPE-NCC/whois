package net.ripe.db.whois.api.rdap.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Lists;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;

import java.io.Serializable;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "rdapObject", propOrder = {
    "networks",
    "network",
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

    protected Ip network;
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

    @XmlTransient
    @JsonIgnore
    private List<RpslAttribute> redactedRpslAttrs;

    protected List<Redaction> redacted;

    public List<Object> getStatus() {
        if (status == null) {
            status = Lists.newArrayList();
        }
        return this.status;
    }

    public List<RpslAttribute> getRedactedRpslAttrs() {
        if(this.redactedRpslAttrs == null) {
            this.redactedRpslAttrs = Lists.newArrayList();
        }

        return redactedRpslAttrs;
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

    public List<Ip> getNetworks() {
        if (networks == null) {
            networks = Lists.newArrayList();
        }
        return networks;
    }

    public Ip getNetwork() {
        return network;
    }

    public List<Autnum> getAutnums() {
        if (autnums == null) {
            autnums = Lists.newArrayList();
        }
        return autnums;
    }

    public List<Redaction> getRedacted() {
        if (redacted == null) {
            redacted = Lists.newArrayList();
        }
        return redacted;
    }

    public void setLang(final String value) {
        this.lang = value;
    }

    public void setStatus(List<Object> status) {
        this.status = status;
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

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(final Integer errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorTitle() {
        return errorTitle;
    }

    public void setErrorTitle(final String errorTitle) {
        this.errorTitle = errorTitle;
    }

    public List<String> getDescription() {
        if (errorDescription == null) {
            errorDescription = Lists.newArrayList();
        }
        return this.errorDescription;
    }

    public void setDescription(final List<String> description) {
        this.errorDescription = description;
    }

    public String getObjectClassName() {
        return objectClassName;
    }

    public void setObjectClassName(final String value) {
        this.objectClassName = value;
    }

    public void setAutnums(final List<Autnum> autnums){
        this.autnums = autnums;
    }

    public void setNetworks(final List<Ip> networks) {
        this.networks = networks;
    }

    public void setNetwork(final Ip network) {
        this.network = network;
    }
}
