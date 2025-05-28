package net.ripe.db.whois.update.dns;

import net.ripe.db.whois.update.domain.Update;
import org.junit.jupiter.api.Test;

import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(MockitoExtension.class)
public class DnsCheckRequestTest {
    @Mock
    Update update;

    @Test
    public void equals_null() {
        assertThat(new DnsCheckRequest(update, "domain", "glue").equals(null), is(false));
    }

    @Test
    public void equals_other_class() {
        assertThat(new DnsCheckRequest(update, "domain", "glue").equals(""), is(false));
    }

    @Test
    public void equals_same_instance() {
        final DnsCheckRequest dnsCheckRequest = new DnsCheckRequest(update, "domain", "glue");
        assertThat(dnsCheckRequest.equals(dnsCheckRequest), is(true));
    }

    @Test
    public void equals_equal() {
        final DnsCheckRequest dnsCheckRequest1 = new DnsCheckRequest(update, "domain", "glue");
        final DnsCheckRequest dnsCheckRequest2 = new DnsCheckRequest(update, "domain", "glue");
        assertThat(dnsCheckRequest1.equals(dnsCheckRequest2), is(true));
        assertThat(dnsCheckRequest1.hashCode(), is(dnsCheckRequest2.hashCode()));
    }

    @Test
    public void equals_not_equal_domain() {
        final DnsCheckRequest dnsCheckRequest1 = new DnsCheckRequest(update, "domain", "glue");
        final DnsCheckRequest dnsCheckRequest2 = new DnsCheckRequest(update, "DOMAIN", "glue");
        assertThat(dnsCheckRequest1.equals(dnsCheckRequest2), is(false));
    }

    @Test
    public void equals_not_equal_glue() {
        final DnsCheckRequest dnsCheckRequest1 = new DnsCheckRequest(update, "domain", "glue");
        final DnsCheckRequest dnsCheckRequest2 = new DnsCheckRequest(update, "domain", "GLUE");
        assertThat(dnsCheckRequest1.equals(dnsCheckRequest2), is(false));
    }

    @Test
    public void accessors() {
        final DnsCheckRequest dnsCheckRequest = new DnsCheckRequest(update, "domain", "glue");
        assertThat(dnsCheckRequest.getDomain(), is("domain"));
        assertThat(dnsCheckRequest.getGlue(), is("glue"));
        assertThat(dnsCheckRequest.toString(), is("domain glue"));


    }
}
