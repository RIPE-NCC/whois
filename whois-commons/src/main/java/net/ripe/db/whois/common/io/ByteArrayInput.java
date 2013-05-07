package net.ripe.db.whois.common.io;

import java.io.InputStream;

/**
 * Like ByteArrayInputStream, but without synchronized access.
 */
public final class ByteArrayInput extends InputStream {
    private byte buf[];
    private int pos;
    private int mark = 0;
    private int count;

    @SuppressWarnings("PMD.ArrayIsStoredDirectly") // We just want to read the byte array using an inputstream
    public ByteArrayInput(final byte buf[]) {
        this.buf = buf;
        this.pos = 0;
        this.count = buf.length;
    }

    @SuppressWarnings("PMD.ArrayIsStoredDirectly") // We just want to read the byte array using an inputstream
    public ByteArrayInput(final byte buf[], final int offset, final int length) {
        this.buf = buf;
        this.pos = offset;
        this.count = Math.min(offset + length, buf.length);
        this.mark = offset;
    }

    public int read() {
        return (pos < count) ? (buf[pos++] & 0xff) : -1;
    }

    public int available() {
        return count - pos;
    }

    public void mark(final int readAheadLimit) {
        mark = pos;
    }

    public void reset() {
        pos = mark;
    }

    public int peek() {
        mark(1);
        int next = read();
        reset();
        return next;
    }
}
