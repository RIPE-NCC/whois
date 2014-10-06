package net.ripe.db.whois.common.support;

import org.springframework.stereotype.Component;

@Component
public class TelnetWhoisClientSupplier {

    public TelnetWhoisClient get(String host) {
        return new TelnetWhoisClient(host);
    }
    public TelnetWhoisClient get(int port) {
        return new TelnetWhoisClient(port);
    }
    public TelnetWhoisClient get(String host, int port) {
        return new TelnetWhoisClient(host, port);
    }
}
