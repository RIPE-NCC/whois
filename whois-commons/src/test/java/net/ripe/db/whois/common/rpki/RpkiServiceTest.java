package net.ripe.db.whois.common.rpki;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

public class RpkiServiceTest {

    @Test
    public void testConstruct() {
        new RpkiService(new RpkiDataProvider() {
            @Override
            public List<Roa> loadRoas() {
                try {
                    return new ObjectMapper().readValue(getClass().getResourceAsStream("/rpki/roas.json"), Roas.class).getRoas();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
