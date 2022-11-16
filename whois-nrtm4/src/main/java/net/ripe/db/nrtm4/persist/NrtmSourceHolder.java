package net.ripe.db.nrtm4.persist;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;


@Component
public class NrtmSourceHolder {

    private final NrtmSource source;
    private final NrtmSource nonauthSource;

    private static final Map<String, NrtmSource> map = new HashMap<>();

    NrtmSourceHolder(
            @Value("whois.source") final String source,
            @Value("whois.nonauth.source") final String nonauthSource
    ) {
        this.source = new NrtmSource(source);
        this.nonauthSource = new NrtmSource(nonauthSource);
        map.put(source, this.source);
        map.put(nonauthSource, this.nonauthSource);
    }

    public NrtmSource getSource() {
        return source;
    }

    public NrtmSource getNonauthSource() {
        return nonauthSource;
    }

    public static NrtmSource valueOf(final String str) {
        if (map.containsKey(str)) {
            return map.get(str);
        }
        throw new IllegalArgumentException("No NRTM source with key " + str);
    }
}
