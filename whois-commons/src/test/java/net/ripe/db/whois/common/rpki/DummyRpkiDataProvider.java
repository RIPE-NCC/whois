package net.ripe.db.whois.common.rpki;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.ripe.db.whois.common.profiles.WhoisProfile;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@Profile({WhoisProfile.TEST})
public class DummyRpkiDataProvider implements RpkiDataProvider {

    private List<Roa> loadedRoas = null;

    @Override
    public List<Roa> loadRoas() {
        if (loadedRoas != null) {
            return loadedRoas;
        }

        try {
            return loadRoas(new ObjectMapper().readValue(getClass().getResourceAsStream("/rpki/roas.json"), Roas.class).getRoas());
        } catch (IOException e){
            throw new IllegalStateException(e);
        }
    }

    public List<Roa> loadRoas(final List<Roa> roas) {
        if (loadedRoas == null) {
            loadedRoas = roas;
        }
        return loadedRoas;
    }

}
