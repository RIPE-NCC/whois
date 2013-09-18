package net.ripe.db.whois.api.whois.domain;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each
 * Java content interface and Java element interface
 * generated in the net.ripe.db.whois.update.net.ripe.db.whois.update.api.whois.domain package.
 * <p>An ObjectFactory allows you to programatically
 * construct new instances of the Java representation
 * for XML content. The Java representation of XML
 * content can consist of schema derived interfaces
 * and classes representing the binding of schema
 * type definitions, element declarations and model
 * groups.  Factory methods for each of these are
 * provided in this class.
 *
 */
@XmlRegistry
public class ObjectFactory {

    private static final QName _Link_QNAME = new QName("", "link");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: net.ripe.db.whois.update.net.ripe.db.whois.update.api.whois.domain
     *
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link WhoisResources }
     *
     */
    public WhoisResources createWhoisResources() {
        return new WhoisResources();
    }

    /**
     * Create an instance of {@link WhoisObject }
     *
     */
    public WhoisObject createObject() {
        return new WhoisObject();
    }

    /**
     * Create an instance of {@link GrsMirror }
     *
     */
    public GrsMirror createGrsMirror() {
        return new GrsMirror();
    }

    /**
     * Create an instance of {@link Attribute }
     *
     */
    public Attribute createAttribute() {
        return new Attribute();
    }

    /**
     * Create an instance of {@link Parameters }
     *
     */
    public Parameters createParameters() {
        return new Parameters();
    }

    /**
     * Create an instance of {@link Source }
     *
     */
    public Source createSource() {
        return new Source();
    }

    /**
     * Create an instance of {@link TypeFilter }
     *
     */
    public TypeFilter createTypeFilter() {
        return new TypeFilter();
    }

    /**
     * Create an instance of {@link InverseAttribute }
     *
     */
    public InverseAttribute createInverseAttribute() {
        return new InverseAttribute();
    }

    /**
     * Create an instance of {@link Flag }
     *
     */
    public Flag createFlag() {
        return new Flag();
    }

    /**
     * Create an instance of {@link QueryString }
     *
     */
    public QueryString createQueryString() {
        return new QueryString();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link java.lang.Object }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "", name = "link")
    public JAXBElement<java.lang.Object> createLink(java.lang.Object value) {
        return new JAXBElement<>(_Link_QNAME, java.lang.Object.class, null, value);
    }

}
