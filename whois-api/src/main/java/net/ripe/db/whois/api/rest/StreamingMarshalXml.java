package net.ripe.db.whois.api.rest;

import com.google.common.collect.ImmutableList;
import com.sun.xml.bind.marshaller.CharacterEscapeHandler;
import javanet.staxutils.events.NamespaceEvent;
import javanet.staxutils.io.XMLWriterUtils;
import net.ripe.db.whois.api.rest.client.StreamingException;
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
import javax.xml.stream.events.Namespace;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class StreamingMarshalXml implements StreamingMarshal {
    private static final List<Namespace> NAMESPACES = ImmutableList.<Namespace>of(new NamespaceEvent("xlink", Link.XLINK_URI));
    private static final NewlineEscapeHandler NEWLINE_ESCAPE_HANDLER = new NewlineEscapeHandler();

    private static final JAXBContext context;

    static {
        try {
            context = JAXBContext.newInstance(WhoisResources.class, TemplateResources.class, AbuseResources.class);
        } catch (JAXBException e) {
            throw new IllegalStateException(e);
        }
    }

    private final NamespaceSuppressingOutputStream xmlOut;
    private final Marshaller marshaller;
    private final String root;

    StreamingMarshalXml(final OutputStream outputStream, String root) {
        try {
            this.root = root;

            xmlOut = new NamespaceSuppressingOutputStream(outputStream);

            marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(CharacterEscapeHandler.class.getName(), NEWLINE_ESCAPE_HANDLER);

        } catch (JAXBException e) {
            throw new StreamingException(e);
        }
    }

    @Override
    public void open() {
        try {
            XMLWriterUtils.writeStartDocument("1.0", "UTF-8", xmlOut);
            xmlOut.write('\n');
            XMLWriterUtils.writeStartElement(QName.valueOf(root), null, NAMESPACES.iterator(), xmlOut);
            xmlOut.write('\n');
        } catch (XMLStreamException | IOException e) {
            throw new StreamingException(e);
        }
    }

    @Override
    public void start(final String name) {
        try {
            XMLWriterUtils.writeStartElement(QName.valueOf(name), null, null, xmlOut);
            xmlOut.write('\n');
        } catch (XMLStreamException | IOException e) {
            throw new StreamingException(e);
        }
    }

    @Override
    public void end(final String name) {
        try {
            XMLWriterUtils.writeEndElement(QName.valueOf(name), xmlOut);
            xmlOut.write('\n');
        } catch (IOException e) {
            throw new StreamingException(e);
        }
    }

    @Override
    public <T> void write(final String name, final T t) {
        JAXBElement<T> element = new JAXBElement<>(QName.valueOf(name), (Class<T>) t.getClass(), t);

        try {
            xmlOut.suppressNamespacesOnNextLine();
            marshaller.marshal(element, xmlOut);
            xmlOut.write('\n');
        } catch (JAXBException | IOException e) {
            throw new StreamingException(e);
        }
    }

    @Override
    public <T> void writeArray(T t) {
        write("object", t);
    }

    @Override
    public <T> void startArray(final String name) {
        // deliberately not implemented
    }

    public <T> void endArray() {
        // deliberately not implemented
    }

    @Override
    public void close() {
        try {
            XMLWriterUtils.writeEndDocument(xmlOut);
            xmlOut.close();
        } catch (IOException e) {
            throw new StreamingException(e);
        }
    }

    @Override
    public <T> void singleton(T t) {
        try {
            XMLWriterUtils.writeStartDocument("1.0", "UTF-8", xmlOut);
            xmlOut.write('\n');
            marshaller.marshal(t, xmlOut);
            xmlOut.close();
        } catch (JAXBException | IOException e) {
            throw new StreamingException(e);
        }
    }

    static final class NamespaceSuppressingOutputStream extends OutputStreamWriter {
        private static final Pattern NAMESPACE_BE_GONE = Pattern.compile("(?i)\\s+xmlns:[0-9a-z]+\\s*=\\s*\"[^\"]+\"");
        private boolean suppress = false;
        private StringBuilder captured;

        public NamespaceSuppressingOutputStream(OutputStream out) {
            super(out);
        }

        public void suppressNamespacesOnNextLine() {
            captured = new StringBuilder(128);
            suppress = true;
        }

        private void foundLine() throws IOException {
            suppress = false;
            final Matcher matcher = NAMESPACE_BE_GONE.matcher(captured.toString());
            final String res = matcher.replaceAll("");
            super.write(res);
        }

        @Override
        public void write(int c) throws IOException {
            if (suppress) {
                captured.append((char) c);
                if (c == '\n') {
                    foundLine();
                }
            } else {
                super.write(c);
            }
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            if (suppress) {
                final int end = off + len;
                for (int i = off; i < end; i++) {
                    if (cbuf[i] == '\n') {
                        i++;
                        captured.append(cbuf, off, i - off);
                        foundLine();

                        final int remainingLength = end - i;
                        if (remainingLength > 0) {
                            super.write(cbuf, i, remainingLength);
                        }
                        return;
                    }
                }
                captured.append(cbuf, off, len);
            } else {
                super.write(cbuf, off, len);
            }
        }

        @Override
        public void write(String str, int off, int len) throws IOException {
            if (suppress) {
                final int end = off + len;
                for (int i = off; i < end; i++) {
                    if (str.charAt(i) == '\n') {
                        i++;
                        captured.append(str, off, i - off);
                        foundLine();

                        final int remainingLength = end - i;
                        if (remainingLength > 0) {
                            super.write(str, i, remainingLength);
                        }
                        return;
                    }
                }
                captured.append(str, off, len);
            } else {
                super.write(str, off, len);
            }
        }
    }

    static final class NewlineEscapeHandler implements CharacterEscapeHandler {

        public void escape(char[] ch, int start, int length, boolean isAttVal, Writer out) throws IOException {
            final int end = start + length;

            for (int i = start; i < end; i++) {
                char c = ch[i];

                if (c == '&' || c == '<' || c == '>' || c == '\r' || (isAttVal && (c == '\n' || c == '\"'))) {

                    if (i > start) {
                        out.write(ch, start, i - start);
                    }

                    start = i + 1;

                    switch (ch[i]) {
                        case '&':
                            out.write("&amp;");
                            break;
                        case '<':
                            out.write("&lt;");
                            break;
                        case '>':
                            out.write("&gt;");
                            break;
                        case '\"':
                            out.write("&quot;");
                            break;
                        case '\n':
                            out.write("&#xA;");
                            break;
                    }
                }
            }

            if (end > start) {
                out.write(ch, start, end - start);
            }
        }
    }

}
