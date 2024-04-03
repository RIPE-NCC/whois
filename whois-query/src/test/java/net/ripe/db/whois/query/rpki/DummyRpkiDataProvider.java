package net.ripe.db.whois.query.rpki;

import net.ripe.db.whois.common.profiles.WhoisProfile;
import net.ripe.db.whois.common.rpki.Roa;
import net.ripe.db.whois.common.rpki.RpkiDataProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@Profile({WhoisProfile.TEST})
public class DummyRpkiDataProvider implements RpkiDataProvider {

    private List<Roa> roas;

    @Override
    public List<Roa> loadRoas() {
        if (roas == null){
            return Collections.emptyList();
        }
        return roas;
    }

    public void setRoas(final List<Roa> roas) {
        this.roas = roas;
    }
}
