package net.ripe.db.whois.query.rpki;

import java.util.List;

public interface RpkiDataProvider {
    List<Roa> loadRoas();
}
