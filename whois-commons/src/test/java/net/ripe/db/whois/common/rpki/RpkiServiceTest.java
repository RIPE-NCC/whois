package net.ripe.db.whois.common.rpki;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RpkiServiceTest {

    @Mock RpkiDataProvider rpkiDataProvider;

    @Test
    public void constructorLoadsRoas() {
        when(rpkiDataProvider.loadRoas()).thenReturn(loadRoas());

        final RpkiService rpkiService = new RpkiService(rpkiDataProvider);

        assertThat(rpkiService.findRoas("0.0.0.0"), hasSize(0));
        assertThat(rpkiService.findRoas("::"), hasSize(0));
        assertThat(rpkiService.findRoas("85.204.99.0/24"), hasSize(1));
        assertThat(rpkiService.findRoas("85.204.99.0/24").iterator().next().getPrefix(), is("85.204.99.0/24"));
    }

    // helper methods

    private List<Roa> loadRoas() {
        try {
            return new ObjectMapper().readValue(getClass().getResourceAsStream("/rpki/roas.json"), Roas.class).getRoas();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
