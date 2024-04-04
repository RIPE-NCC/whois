package net.ripe.db.whois.common.rpki;

import java.util.List;

public interface RpkiDataProvider {
    List<Roa> loadRoas();
}
