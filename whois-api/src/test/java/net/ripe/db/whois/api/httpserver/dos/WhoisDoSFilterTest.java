package net.ripe.db.whois.api.httpserver.dos;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


public class WhoisDoSFilterTest {

    private WhoisDoSFilter subject;

    @Test
    public void testWhitelist() {
        subject = new WhoisDoSFilter();
        subject.setWhitelist("127.0.0.1,::1,193.0.0.0 - 193.0.23.255,2001:67c:2e8::/48,10.0.0.0 - 10.255.255.255");

        assertThat(subject.checkWhitelist("193.0.20.230"), is(true));
        assertThat(subject.checkWhitelist("2001:067c:02e8:0000:0000:0000:0000:0000"), is(true));
    }
}
