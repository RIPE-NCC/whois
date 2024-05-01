package net.ripe.db.whois.common.rpki;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.ripe.db.whois.common.profiles.WhoisProfile;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
@Profile({WhoisProfile.TEST})
public class DummyRpkiDataProvider implements RpkiDataProvider {

    private List<Roa> loadedRoas;

    @Override
    public List<Roa> loadRoas() {
        if (!loadedRoas.isEmpty()){
            return loadedRoas;
        }

        try {
            return new ObjectMapper().readValue(getClass().getResourceAsStream("/rpki/roas.json"), Roas.class).getRoas();
        } catch (IOException ex){
            /* Do Nothing*/
        }
        return Collections.emptyList();
    }

    public void loadCustomRoas(final List<Roa> roas){
        if (loadedRoas.isEmpty()){
            this.loadedRoas = roas;
        }
        loadedRoas.addAll(roas);
    }
}
