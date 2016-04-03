package net.ripe.db.whois.common.io;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ByteArrayOutputTest {
    private ByteArrayOutput subject;
    private byte[] buffer = "test".getBytes();

    @Before
    public void setUp() throws Exception {
        subject = new ByteArrayOutput();
    }

    @Test
    public void write_byte() {
        for (byte b : buffer) {
            subject.write(b);
        }

        assertThat(subject.toByteArray(), is(buffer));
    }

    @Test
    public void write_byte_too_small_buffer() {
        subject = new ByteArrayOutput(0);
        for (byte b : buffer) {
            subject.write(b);
        }

        assertThat(subject.toByteArray(), is(buffer));
    }

    @Test
    public void write_bytes() {
        subject.write(buffer);

        assertThat(subject.toByteArray(), is(buffer));
    }

    @Test
    public void write_bytes_too_small_buffer() {
        subject = new ByteArrayOutput(0);
        subject.write(buffer);

        assertThat(subject.toByteArray(), is(buffer));
    }

    @Test
    public void write_bytes_offset_complete() {
        subject.write(buffer, 0, buffer.length);

        assertThat(subject.toByteArray(), is(buffer));
    }

    @Test
    public void write_bytes_offset_empty_length() {
        subject.write(buffer, 0, 0);

        assertThat(subject.toByteArray(), is(new byte[0]));
    }

    @Test
    public void write_bytes_offset_partial() {
        subject.write(buffer, 2, buffer.length - 2);

        assertThat(subject.toByteArray(), is(new byte[]{'s', 't'}));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void write_bytes_offset_out_of_bounds() {
        subject.write(buffer, 2, buffer.length);
    }

    @Test
    public void reset() {
        subject.write(buffer);

        subject.reset();
        assertThat(subject.toByteArray(), is(new byte[]{}));

        subject.write(buffer);
        assertThat(subject.toByteArray(), is(buffer));
    }

    @Test
    public void size() {
        subject.write(buffer);

        assertThat(subject.size(), is(buffer.length));
    }
}
