package net.ripe.db.whois.api.rest;

import javanet.staxutils.IndentingXMLStreamWriter;
import javanet.staxutils.io.StAXStreamWriter;
import net.ripe.db.whois.api.rest.domain.AbuseResources;
import net.ripe.db.whois.api.rest.domain.Link;
import net.ripe.db.whois.api.rest.domain.TemplateResources;
import net.ripe.db.whois.api.rest.domain.WhoisResources;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.OutputStream;

class StreamingMarshalXml implements StreamingMarshal {
    private static final JAXBContext context;

    static {
        try {
            context = JAXBContext.newInstance(WhoisResources.class, TemplateResources.class, AbuseResources.class);
        } catch (JAXBException e) {
            throw new IllegalStateException(e);
        }
    }

    private final XMLStreamWriter xmlOut;
    private final Marshaller marshaller;
    private final String root;

    StreamingMarshalXml(final OutputStream outputStream, String root) {
        try {
            this.root = root;

            xmlOut = new IndentingXMLStreamWriter(new StAXStreamWriter(outputStream));

            marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);

        } catch (JAXBException e) {
            throw new StreamingException(e);
        }
    }

    @Override
    public void open() {
        try {
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

    @Override
    public <T> void singleton(T t) {
        try {
            marshaller.marshal(t, xmlOut);
        } catch (JAXBException e) {
            throw new StreamingException(e);
        }
    }
}
