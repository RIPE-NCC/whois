package net.ripe.db.whois.common.io;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ByteArrayInputTest {
    private ByteArrayInput subject;
    private byte[] buffer = "test".getBytes();

    @Test
    public void test_read() {
        subject = new ByteArrayInput(buffer);

        assertThat(subject.available(), is(buffer.length));

        final StringBuilder result = new StringBuilder();
        for (int c = subject.read(); c != -1; c = subject.read()) {
            result.append((char)c);
        }

        assertThat(result.toString().getBytes(), is(buffer));
        assertThat(subject.available(), is(0));
    }

    @Test
    public void test_read_from_offest() {
        subject = new ByteArrayInput(buffer, 2, buffer.length);

        assertThat(subject.available(), is(buffer.length - 2));
        assertThat(subject.read(), is((int)'s'));
        assertThat(subject.read(), is((int)'t'));
    }

    @Test
    public void test_peek() {
        subject = new ByteArrayInput(buffer);

        assertThat(subject.peek(), is((int)'t'));
        assertThat(subject.peek(), is((int)'t'));

        assertThat(subject.read(), is((int)'t'));
        assertThat(subject.peek(), is((int)'e'));
        assertThat(subject.peek(), is((int)'e'));
    }

    @Test
    public void test_mark() {
        subject = new ByteArrayInput(buffer);

        subject.mark(1000);
        assertThat(subject.read(), is((int)'t'));
        assertThat(subject.read(), is((int)'e'));

        subject.reset();
        assertThat(subject.read(), is((int)'t'));
        assertThat(subject.read(), is((int)'e'));
    }
}
