package net.ripe.db.whois.common.rpsl;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import org.apache.commons.lang.Validate;

import java.util.ArrayList;
import java.util.List;

// TODO: [AH] shovel RpslObjectFilter & other data-changing methods from RpslObject over here
public class RpslObjectBuilder {
    private List<RpslAttribute> attributes;

    public RpslObjectBuilder() {
        this.attributes = Lists.newArrayList();
    }

    public RpslObjectBuilder(List<RpslAttribute> attributes) {
        this.attributes = attributes;
    }

    public RpslObjectBuilder(RpslObject rpslObject) {
        this.attributes = Lists.newArrayList(rpslObject.getAttributes());
    }

    public RpslObjectBuilder(String input) {
        this(getAttributes(input));
    }

    public RpslObjectBuilder(byte[] input) {
        this(getAttributes(input));
    }

    public RpslObject get() {
        return new RpslObject(attributes);
    }

    public List<RpslAttribute> getAttributes() {
        return attributes;
    }

    static List<RpslAttribute> getAttributes(String input) {
        return getAttributes(input.getBytes(Charsets.UTF_8));
    }

    static List<RpslAttribute> getAttributes(byte[] buf) {
        Validate.notNull(buf, "Object can not be null");

        final List<RpslAttribute> newAttributes = new ArrayList<>(32);

        int pos = 0;
        while (pos < buf.length) {
            int start = pos;

            boolean readKey = false;
            for (; pos < buf.length; pos++) {
                int c = buf[pos] & 0xff;

                if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '-' || c == ':' || c == '*' || c == ' ')) {
                    throw new IllegalArgumentException("Read illegal character in key: '" + (char) c + "'");
                }

                if (c == ':') {
                    readKey = true;
                    break;
                }
            }

            if (!readKey) throw new IllegalArgumentException("No key found");
            if (start == pos) throw new IllegalArgumentException("Read zero sized key");

            final String key = new String(buf, start, pos - start, Charsets.UTF_8);

            // skip over ':' and continue reading the attribute value
            start = ++pos;
            int stop = pos;

            processStream:
            for (; pos < buf.length; ) {
                int c = buf[pos++] & 0xff;

                if (c == '\r') {
                    continue;
                }

                if (c == '\n') {
                    int next = (pos < buf.length) ? buf[pos] & 0xff : -1;

                    switch (next) {
                        case ' ':
                        case '\t':
                        case '+':
                            break;
                        default:
                            break processStream;
                    }
                }

                stop = pos;
            }

            final String value = new String(buf, start, stop - start, Charsets.UTF_8);
            newAttributes.add(new RpslAttribute(key, value));
        }

        return newAttributes;
    }
}
