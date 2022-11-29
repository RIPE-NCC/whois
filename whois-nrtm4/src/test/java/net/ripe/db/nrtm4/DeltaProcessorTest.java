package net.ripe.db.nrtm4;

import net.ripe.db.whois.common.domain.serials.Operation;
import net.ripe.db.whois.common.domain.serials.SerialEntry;
import net.ripe.db.whois.common.rpsl.Dummifier;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


public class DeltaProcessorTest {

    @Test
    void process_empty_tuples_returns_empty_list() {
        final var deltaProcessor = new DeltaProcessor(dummifier);
        final var changes = new ArrayList<SerialEntry>();
        final var result = deltaProcessor.process(changes);
        assertThat(result.size(), is(0));
    }

    @Test
    void process_single_tuple_returns_one_item() {
        final var deltaProcessor = new DeltaProcessor(dummifier);
        final var changes = List.of(
            new SerialEntry(22, Operation.UPDATE, true, 101, inetnumObjectBytes)
        );
        final var result = deltaProcessor.process(changes);
        assertThat(result.size(), is(1));
    }

    private RpslObject inetnumObject = RpslObject.parse("inetnum: 193.0.0.0 - 193.255.255.255\nsource: TEST");
    private byte[] inetnumObjectBytes = "inetnum: 193.0.0.0 - 193.255.255.255\nsource: TEST".getBytes(StandardCharsets.ISO_8859_1);

    private Dummifier dummifier = new Dummifier() {

        @Override
        public RpslObject dummify(final int version, final RpslObject rpslObject) {
            return rpslObject;
        }

        @Override
        public boolean isAllowed(final int version, final RpslObject rpslObject) {
            return true;
        }
    };

}
