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

    @Override
    public List<Roa> loadRoas() {
        try {
            return new ObjectMapper().readValue(getClass().getResourceAsStream("/rpki/roas.json"), Roas.class).getRoas();
        } catch (IOException ex){
            /* Do Nothing*/
        }
        return Collections.emptyList();
    }

}
