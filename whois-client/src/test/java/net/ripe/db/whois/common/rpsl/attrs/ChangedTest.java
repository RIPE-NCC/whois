package net.ripe.db.whois.common.rpsl.attrs;

import org.joda.time.LocalDate;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class ChangedTest {

    @Test(expected = AttributeParseException.class)
    public void empty() {
        Changed.parse("");
    }

    @Test(expected = AttributeParseException.class)
    public void no_email() {
        Changed.parse("20010101");
    }

    @Test(expected = AttributeParseException.class)
    public void invalid_date() {
        Changed.parse("a@a.a 13131313");
    }

    @Test(expected = AttributeParseException.class)
    public void too_long() {
        Changed.parse("abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz 20010101");
    }

    @Test
    public void no_date() {
        final Changed subject = Changed.parse("foo@provider.com");

        assertThat(subject.getEmail(), is("foo@provider.com"));
        assertNull(subject.getDateString());
        assertNull(subject.getDate());
        assertThat(subject.toString(), is("foo@provider.com"));
    }

    @Test
    public void short_with_date() {
        final Changed subject = Changed.parse("a@a.a 20010101");

        assertThat(subject.getEmail(), is("a@a.a"));
        assertThat(subject.getDateString(), is("20010101"));
        assertThat(subject.getDate(), is(new LocalDate(2001, 1, 1)));
        assertThat(subject.toString(), is("a@a.a 20010101"));
    }

    @Test(expected = AttributeParseException.class)
    public void mixedUpDateAndEmail() {
        Changed subject = Changed.parse("20130112 b.was@infbud.pl");
    }

    @Test
    public void long_email_date() {
        final Changed subject = Changed.parse("'anthingcan1242go!@(&)#^!(&@#^21here\"@0.2345678901234567890123456789012345678901 20010101");

        assertThat(subject.getEmail(), is("'anthingcan1242go!@(&)#^!(&@#^21here\"@0.2345678901234567890123456789012345678901"));
        assertThat(subject.toString(), is("'anthingcan1242go!@(&)#^!(&@#^21here\"@0.2345678901234567890123456789012345678901 20010101"));
    }
}
