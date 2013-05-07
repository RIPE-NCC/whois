package net.ripe.db.whois.api.httpserver;

import com.google.common.collect.Maps;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class JettyConfig {
    private final Map<Audience, Integer> ports = Maps.newHashMap();

    public void setPort(final Audience audience, final int port) {
        ports.put(audience, port);
    }

    public int getPort(final Audience audience) {
        return ports.get(audience);
    }
}
