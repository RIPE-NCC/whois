package net.ripe.db.whois.nrtm.client;

import net.ripe.db.whois.common.domain.CIString;

class NrtmSource {
    private final CIString name;
    private final CIString originSource;
    private final String host;
    private final int port;

    public NrtmSource(final CIString name, final CIString originSource, final String host, final int port) {
        this.name = name;
        this.originSource = originSource;
        this.host = host;
        this.port = port;
    }

    public CIString getName() {
        return name;
    }

    public CIString getOriginSource() {
        return originSource;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
