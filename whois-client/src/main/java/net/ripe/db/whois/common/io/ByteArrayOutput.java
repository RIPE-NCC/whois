package net.ripe.db.whois.common.io;

import java.io.OutputStream;
import java.util.Arrays;

/**
 * Like ByteArrayOutputStream, but without synchronized access.
 */
public final class ByteArrayOutput extends OutputStream {
    private byte buf[];
    private int count;

    public ByteArrayOutput() {
        this(128);
    }

    public ByteArrayOutput(int initialCapacity) {
        buf = new byte[initialCapacity];
    }

    public void write(int b) {
        int newcount = count + 1;
        if (newcount > buf.length) {
            buf = Arrays.copyOf(buf, Math.max(buf.length << 1, newcount));
        }
        buf[count] = (byte) b;
        count = newcount;
    }

    public void write(byte b[]) {
        write(b, 0, b.length);
    }

    public void write(byte b[], int off, int len) {
        if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        int newcount = count + len;
        if (newcount > buf.length) {
            buf = Arrays.copyOf(buf, Math.max(buf.length << 1, newcount));
        }
        System.arraycopy(b, off, buf, count, len);
        count = newcount;
    }

    public void reset() {
        count = 0;
    }

    public byte[] toByteArray() {
        return Arrays.copyOf(buf, count);
    }

    public int size() {
        return count;
    }
}
