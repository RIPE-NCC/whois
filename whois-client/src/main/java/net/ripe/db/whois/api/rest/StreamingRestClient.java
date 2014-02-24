package net.ripe.db.whois.api.rest;

import net.ripe.db.whois.api.rest.domain.WhoisObject;
import net.ripe.db.whois.api.rest.domain.WhoisResources;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.EventFilter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

public class StreamingRestClient implements Iterator<WhoisObject>, Closeable {
    private static final JAXBContext jaxbContext;
    private static final Unmarshaller unmarshaller;
    private static final XMLInputFactory xmlInputFactory;
    private static final EventFilter whoisObjectFilter = new WhoisObjectEventFilter();

    static {
        try {
            jaxbContext = JAXBContext.newInstance(WhoisResources.class.getPackage().getName());
            unmarshaller = jaxbContext.createUnmarshaller();
            xmlInputFactory = XMLInputFactory.newFactory();
        } catch (JAXBException e) {
            throw new IllegalStateException(e);
        }
    }

    private final XMLEventReader eventReader;
    private final XMLEventReader filteredReader;
    private final InputStream inputStream;

    public StreamingRestClient(final InputStream inputStream) {
        this.inputStream = inputStream;
        try {
            eventReader = xmlInputFactory.createXMLEventReader(inputStream);
            filteredReader = xmlInputFactory.createFilteredReader(eventReader, whoisObjectFilter);
        } catch (XMLStreamException e) {
            throw new StreamingException(e);
        }
    }

    @Override
    public boolean hasNext() {
        try {
            return filteredReader.peek() != null;
        } catch (XMLStreamException e) {
            throw new StreamingException(e);
        }
    }

    @Override
    public WhoisObject next() {
        try {
            return (WhoisObject) unmarshaller.unmarshal(eventReader);
        } catch (JAXBException e) {
            throw new StreamingException(e);
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        try {
            inputStream.close();
        } catch (IOException e) {
            throw new StreamingException(e);
        }
    }

    public static WhoisResources unMarshalError(final InputStream inputStream) {
        try {
            return (WhoisResources)unmarshaller.unmarshal(inputStream);
        } catch (JAXBException e) {
            throw new StreamingException(e);
        }
    }

    private static class WhoisObjectEventFilter implements EventFilter {
        @Override
        public boolean accept(final XMLEvent event) {
            return event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("object");
        }
    }
}
