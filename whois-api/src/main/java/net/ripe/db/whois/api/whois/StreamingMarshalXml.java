package net.ripe.db.whois.api.whois;

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
    private static Marshaller marshaller;
    private static XMLOutputFactory xmlOutputFactory;

    static {
        try {
            final JAXBContext context = JAXBContext.newInstance(WhoisResources.class.getPackage().getName());
            marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);

            xmlOutputFactory = XMLOutputFactory.newFactory();
        } catch (JAXBException e) {
            throw new IllegalStateException("Initializing", e);
        }
    }

    private XMLStreamWriter xmlOut;

    @Override
    public void open(final OutputStream outputStream, final String... parentElementNames) {
        try {
            xmlOut = xmlOutputFactory.createXMLStreamWriter(outputStream);
            xmlOut.writeStartDocument();

            for (final String parentElementName : parentElementNames) {
                xmlOut.writeStartElement(parentElementName);
            }
        } catch (XMLStreamException e) {
            throw new RuntimeException("Open", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void write(final String name, final T t) {
        try {
            JAXBElement<T> element = new JAXBElement<T>(QName.valueOf(name), (Class<T>) t.getClass(), t);
            marshaller.marshal(element, xmlOut);
        } catch (JAXBException e) {
            throw new RuntimeException("Write", e);
        }
    }

    @Override
    public void close() {
        try {
            xmlOut.writeEndDocument();
            xmlOut.close();
        } catch (XMLStreamException e) {
            throw new RuntimeException("Close", e);
        }
    }
}
