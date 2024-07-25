package net.ripe.db.whois.api.httpserver.dos;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


public class WhoisQueryDoSFilterTest {

    private WhoisQueryDoSFilter subject;

    @BeforeEach
    public void setUp() {
        this.subject = new WhoisQueryDoSFilter("50");
    }

    @Test
    public void checkWhitelist() {
        subject.setWhitelist("127.0.0.1,::1,193.0.0.0 - 193.0.23.255,2001:67c:2e8::/48,10.0.0.0 - 10.255.255.255");

        assertThat(subject.checkWhitelist("193.0.20.230"), is(true));
        assertThat(subject.checkWhitelist("2001:067c:02e8:0000:0000:0000:0000:0000"), is(true));
    }
}
