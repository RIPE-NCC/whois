package net.ripe.db.whois.update.dns;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DnsCheckRequestTest {
    @Test
    public void equals_null() {
        assertThat(new DnsCheckRequest("domain", "glue").equals(null), is(false));
    }

    @Test
    public void equals_other_class() {
        assertThat(new DnsCheckRequest("domain", "glue").equals(""), is(false));
    }

    @Test
    public void equals_same_instance() {
        final DnsCheckRequest dnsCheckRequest = new DnsCheckRequest("domain", "glue");
        assertThat(dnsCheckRequest.equals(dnsCheckRequest), is(true));
    }

    @Test
    public void equals_equal() {
        final DnsCheckRequest dnsCheckRequest1 = new DnsCheckRequest("domain", "glue");
        final DnsCheckRequest dnsCheckRequest2 = new DnsCheckRequest("domain", "glue");
        assertThat(dnsCheckRequest1.equals(dnsCheckRequest2), is(true));
        assertThat(dnsCheckRequest1.hashCode(), is(dnsCheckRequest2.hashCode()));
    }

    @Test
    public void equals_not_equal_domain() {
        final DnsCheckRequest dnsCheckRequest1 = new DnsCheckRequest("domain", "glue");
        final DnsCheckRequest dnsCheckRequest2 = new DnsCheckRequest("DOMAIN", "glue");
        assertThat(dnsCheckRequest1.equals(dnsCheckRequest2), is(false));
    }

    @Test
    public void equals_not_equal_glue() {
        final DnsCheckRequest dnsCheckRequest1 = new DnsCheckRequest("domain", "glue");
        final DnsCheckRequest dnsCheckRequest2 = new DnsCheckRequest("domain", "GLUE");
        assertThat(dnsCheckRequest1.equals(dnsCheckRequest2), is(false));
    }

    @Test
    public void accessors() {
        final DnsCheckRequest dnsCheckRequest = new DnsCheckRequest("domain", "glue");
        assertThat(dnsCheckRequest.getDomain(), is("domain"));
        assertThat(dnsCheckRequest.getGlue(), is("glue"));
        assertThat(dnsCheckRequest.toString(), is("domain glue"));


    }
}
