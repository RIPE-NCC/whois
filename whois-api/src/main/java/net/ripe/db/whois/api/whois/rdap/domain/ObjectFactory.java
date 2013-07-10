package net.ripe.db.whois.api.whois.rdap.domain;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

@XmlRegistry
public class ObjectFactory {

    private final static QName _Nameserver_QNAME = new QName("", "nameserver");
    private final static QName _Remark_QNAME = new QName("", "remark");
    private final static QName _Event_QNAME = new QName("", "event");
    private final static QName _Entity_QNAME = new QName("", "entity");
    private final static QName _Link_QNAME = new QName("", "link");
    private final static QName _Notice_QNAME = new QName("", "notice");

    public ObjectFactory() {
    }

    public Entity createEntity() {
        return new Entity();
    }

    public Ip createIp() {
        return new Ip();
    }

    public Autnum createAutnum() {
        return new Autnum();
    }

    public Event createEvent() {
        return new Event();
    }

    public Link createLink() {
        return new Link();
    }

    public Nameserver createNameserver() {
        return new Nameserver();
    }

    public Remark createRemark() {
        return new Remark();
    }

    public Notice createNotice() {
        return new Notice();
    }

    public RdapObject createRdapObject() {
        return new RdapObject();
    }

    public Domain createDomain() {
        return new Domain();
    }

    public Nameserver.IpAddresses createNameserverIpAddresses() {
        return new Nameserver.IpAddresses();
    }

    @XmlElementDecl(namespace = "", name = "nameserver")
    public JAXBElement<Nameserver> createNameserver(Nameserver value) {
        return new JAXBElement<Nameserver>(_Nameserver_QNAME, Nameserver.class, null, value);
    }

    @XmlElementDecl(namespace = "", name = "remark")
    public JAXBElement<Remark> createRemark(Remark value) {
        return new JAXBElement<Remark>(_Remark_QNAME, Remark.class, null, value);
    }

    @XmlElementDecl(namespace = "", name = "event")
    public JAXBElement<Event> createEvent(Event value) {
        return new JAXBElement<Event>(_Event_QNAME, Event.class, null, value);
    }

    @XmlElementDecl(namespace = "", name = "entity")
    public JAXBElement<Entity> createEntity(Entity value) {
        return new JAXBElement<Entity>(_Entity_QNAME, Entity.class, null, value);
    }

    @XmlElementDecl(namespace = "", name = "link")
    public JAXBElement<Link> createLink(Link value) {
        return new JAXBElement<Link>(_Link_QNAME, Link.class, null, value);
    }

    @XmlElementDecl(namespace = "", name = "notice")
    public JAXBElement<Notice> createNotice(Notice value) {
        return new JAXBElement<Notice>(_Notice_QNAME, Notice.class, null, value);
    }
}
