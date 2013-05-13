package net.ripe.db.whois.common.collect;

import java.util.List;

public interface ProxyLoader<P, R> {
    void load(List<P> proxy, List<R> result);
}
