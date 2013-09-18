package net.ripe.db.whois.api.whois;

import net.ripe.db.whois.api.whois.domain.*;

import net.ripe.db.whois.api.whois.domain.Link;
import net.ripe.db.whois.api.whois.domain.WhoisResources;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.OutputStream;

class StreamingMarshalXml implements StreamingMarshal {

    private static JAXBContext context;
    private static XMLOutputFactory xmlOutputFactory;

    public StreamingMarshalXml() {
        try {
            this.context = JAXBContext.newInstance(
                    Attribute.class,
                    Attributes.class,
                    Flag.class,
                    Flags.class,
                    GeolocationAttributes.class,
                    GrsMirror.class,
                    GrsSource.class,
                    GrsSources.class,
                    InverseAttribute.class,
                    InverseAttributes.class,
                    Language.class,
                    Link.class,
                    Location.class,
                    Parameters.class,
                    PrimaryKey.class,
                    QueryStrings.class,
                    Source.class,
                    Sources.class,
                    Template.class,
                    TemplateAttribute.class,
                    TemplateAttributes.class,
                    TemplateResources.class,
                    Templates.class,
                    TypeFilter.class,
                    TypeFilters.class,
                    WhoisModify.class,
                    WhoisObject.class,
                    WhoisObjects.class,
                    WhoisResources.class,
                    WhoisTag.class,
                    WhoisTags.class,
                    WhoisVersion.class,
                    WhoisVersions.class);
            xmlOutputFactory = XMLOutputFactory.newFactory();
        } catch (JAXBException e) {
            throw new IllegalStateException(e);
        }
    }

    private XMLStreamWriter xmlOut;

    @Override
    public void open(final OutputStream outputStream, String root) {
        try {
            xmlOut = xmlOutputFactory.createXMLStreamWriter(outputStream);
            xmlOut.writeStartDocument();
            xmlOut.writeStartElement(root);
            // TODO: this is ugly, should come from package info instead (which is the case with no streaming)
            xmlOut.writeNamespace("xlink", Link.XLINK_URI);
        } catch (XMLStreamException e) {
            throw new StreamingException(e);
        }
    }

    @Override
    public void start(final String name) {
        try {
            xmlOut.writeStartElement(name);
        } catch (XMLStreamException e) {
            throw new StreamingException(e);
        }
    }

    @Override
    public void end() {
        try {
            xmlOut.writeEndElement();
        } catch (XMLStreamException e) {
            throw new StreamingException(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void write(final String name, final T t) {
        JAXBElement<T> element = new JAXBElement<>(QName.valueOf(name), (Class<T>) t.getClass(), t);

        try {
            final Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            marshaller.marshal(element, xmlOut);
        } catch (JAXBException e) {
            throw new StreamingException(e);
        }
    }

    @Override
    public void close() {
        try {
            xmlOut.writeEndDocument();
            xmlOut.close();
        } catch (XMLStreamException e) {
            throw new StreamingException(e);
        }
    }
}
