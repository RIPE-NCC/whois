package net.ripe.db.whois.common.rpsl.attrs;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ChangedTest {

    @Test
    public void empty() {
        assertThrows(AttributeParseException.class, () -> {
            Changed.parse("");
        });

    }

    @Test
    public void no_email() {
        assertThrows(AttributeParseException.class, () -> {
            Changed.parse("20010101");
        });
    }

    @Test
    public void invalid_date() {
        assertThrows(AttributeParseException.class, () -> {
            Changed.parse("a@a.a 13131313");
        });

    }

    @Test
    public void too_long() {
        assertThrows(AttributeParseException.class, () -> {
            Changed.parse("abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz 20010101");

        });
    }

    @Test
    public void no_date() {
        final Changed subject = Changed.parse("foo@provider.com");

        assertThat(subject.getEmail(), is("foo@provider.com"));
        assertThat(subject.getDateString(), is(nullValue()));
        assertThat(subject.getDate(), is(nullValue()));
        assertThat(subject.toString(), is("foo@provider.com"));
    }

    @Test
    public void short_with_date() {
        final Changed subject = Changed.parse("a@a.a 20010101");

        assertThat(subject.getEmail(), is("a@a.a"));
        assertThat(subject.getDateString(), is("20010101"));
        assertThat(subject.getDate(), is(LocalDate.of(2001, 1, 1)));
        assertThat(subject.toString(), is("a@a.a 20010101"));
    }

    @Test
    public void mixedUpDateAndEmail() {
        assertThrows(AttributeParseException.class, () -> {
            Changed.parse("20130112 b.was@infbud.pl");
        });
    }

    @Test
    public void long_email_date() {
        final Changed subject = Changed.parse("'anthingcan1242go!@(&)#^!(&@#^21here\"@0.2345678901234567890123456789012345678901 20010101");

        assertThat(subject.getEmail(), is("'anthingcan1242go!@(&)#^!(&@#^21here\"@0.2345678901234567890123456789012345678901"));
        assertThat(subject.toString(), is("'anthingcan1242go!@(&)#^!(&@#^21here\"@0.2345678901234567890123456789012345678901 20010101"));
    }
}
