package net.ripe.db.whois.common.rpki;

import net.ripe.db.whois.common.profiles.WhoisProfile;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@Profile({WhoisProfile.TEST})
public class DummyRpkiService extends RpkiService {

    public DummyRpkiService(final DummyRpkiDataProvider rpkiDataProvider) {
        super(rpkiDataProvider);
    }

    @Override
    public Set<Roa> findRoas(String prefix) {
        super.loadRoas();
        return super.findRoas(prefix);
    }
}
