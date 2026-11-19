package net.ripe.db.whois.common.rpki;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

public class RpkiServiceTest {

    private RpkiService rpkiService;

    @BeforeEach
    public void setup() {
        this.rpkiService = new RpkiService(new RpkiDataProvider() {
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

    @Test
    public void ipv4() {
        assertThat(rpkiService.findRoas("94.127.92.0/24"), contains(new Roa("AS42187", 24, "94.127.92.0/24", "ripe")));
    }

    @Test
    public void ipv6() {
        assertThat(rpkiService.findRoas("2400:AC40:8b7::/48"), contains(new Roa("AS136255", 48, "2400:AC40:8B7::/48", "apnic")));
    }

    @Test
    public void asn() {
        assertThat(rpkiService.findRoas("as42187"), contains(new Roa("AS42187", 24, "94.127.92.0/24", "ripe")));
    }

}
