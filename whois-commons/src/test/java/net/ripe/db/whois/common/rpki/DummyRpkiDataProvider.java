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

    private List<Roa> loadedRoas = loadFromResource();

    @Override
    public List<Roa> loadRoas() {
        return loadedRoas;
    }

    public void loadRoas(final List<Roa> roas){
        this.loadedRoas = roas;
    }

    private static List<Roa> loadFromResource(){
        try {
            return new ObjectMapper().readValue(DummyRpkiDataProvider.class.getResourceAsStream("/rpki/roas.json"), Roas.class).getRoas();
        } catch (IOException ex){
            throw new IllegalStateException(ex);
        }
    }

}
