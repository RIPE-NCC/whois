package net.ripe.db.whois.api.httpserver;

import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertTrue;

public class JettyBootstrapTest {

    @Test
    public void testWhitelist() {
        TestDosFilter dosFilter = new TestDosFilter();
        dosFilter.setWhitelist("127.0.0.1,::1,193.0.0.0 - 193.0.23.255,2001:67c:2e8::/48,10.0.0.0 - 10.255.255.255");

        assertTrue(dosFilter.checkWhitelist("193.0.20.230"));
        assertTrue(dosFilter.checkWhitelist("2001:067c:02e8:0000:0000:0000:0000:0000"));
    }

    class TestDosFilter extends WhoisDoSFilter {

        @Override
        public boolean checkWhitelist(String candidate) {
            return super.checkWhitelist(candidate);
        }
    }
}
