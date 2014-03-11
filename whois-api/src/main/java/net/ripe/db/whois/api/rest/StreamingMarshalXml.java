package net.ripe.db.whois.api.rest;

import com.google.common.base.Charsets;
import javanet.staxutils.IndentingXMLStreamWriter;
import net.ripe.db.whois.api.rest.domain.AbuseResources;
import net.ripe.db.whois.api.rest.domain.Link;
import net.ripe.db.whois.api.rest.domain.TemplateResources;
import net.ripe.db.whois.api.rest.domain.WhoisResources;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

class StreamingMarshalXml implements StreamingMarshal {
    private static final NewLineEncoder NEW_LINE_ENCODER = new NewLineEncoder();

    private static final JAXBContext context;
    private static final XMLOutputFactory xmlOutputFactory;
    private static Field fEncoder = null;

    static {
        try {
            context = JAXBContext.newInstance(WhoisResources.class, TemplateResources.class, AbuseResources.class);
            xmlOutputFactory = XMLOutputFactory.newFactory();
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

            final XMLStreamWriter xmlStreamWriter = xmlOutputFactory.createXMLStreamWriter(outputStream);

            // dirty hack to avoid stupid XMLStreamWriterImpl not xml-encoding newlines, causing them to disappear on deserialization
            if (fEncoder == null) {
                final Field fEncoder = xmlStreamWriter.getClass().getDeclaredField("fEncoder");
                fEncoder.setAccessible(true);
                this.fEncoder = fEncoder;
            }
            fEncoder.set(xmlStreamWriter, NEW_LINE_ENCODER);

            xmlOut = new IndentingXMLStreamWriter(xmlStreamWriter);

            marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);

        } catch (XMLStreamException | JAXBException | IllegalAccessException | NoSuchFieldException e) {
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
        try {
            marshaller.marshal(t, xmlOut);
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
            xmlOut.close();
        } catch (JAXBException | XMLStreamException e) {
            throw new StreamingException(e);
        }
    }

    static final class NewLineEncoder extends CharsetEncoder {

        NewLineEncoder() {  // should never be called
            super(Charsets.UTF_8, 1, 2);
        }

        @Override
        public boolean canEncode(char c) {
            return c != '\n';
        }

        @Override
        protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out) {
            throw new IllegalStateException("Should never be called!");
        }
    }
}
