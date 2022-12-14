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
import static org.hamcrest.Matchers.nullValue;


public class DeltaTransformerTest {

    @Test
    void process_empty_rows_returns_empty_list() {
        final var deltaProcessor = new DeltaTransformer(dummifier);
        final var changes = new ArrayList<SerialEntry>();
        final var result = deltaProcessor.toDeltaChange(changes);
        assertThat(result.size(), is(0));
    }

    @Test
    void process_single_row_returns_one_item() {
        final var deltaProcessor = new DeltaTransformer(dummifier);
        final var changes = List.of(
            new SerialEntry(22, Operation.UPDATE, true, 101, inetnumObjectBytes, "193.0.0.0 - 193.255.255.255")
        );
        final var result = deltaProcessor.toDeltaChange(changes);
        assertThat(result.size(), is(1));
        final var change = result.get(0);
        assertThat(change.getPrimaryKey(), is(nullValue()));
        assertThat(change.getAction(), is(DeltaChange.Action.ADD_MODIFY));
    }

    @Test
    void process_single_deleted_row_returns_one_item() {
        final var deltaProcessor = new DeltaTransformer(dummifier);
        final var changes = List.of(
            new SerialEntry(22, Operation.DELETE, false, 101, inetnumObjectBytes, "193.0.0.0 - 193.255.255.255")
        );
        final var result = deltaProcessor.toDeltaChange(changes);
        assertThat(result.size(), is(1));
        final var change = result.get(0);
        assertThat(change.getPrimaryKey(), is("193.0.0.0 - 193.255.255.255"));
        assertThat(change.getAction(), is(DeltaChange.Action.DELETE));
    }

    private final byte[] inetnumObjectBytes = "inetnum: 193.0.0.0 - 193.255.255.255\nsource: TEST".getBytes(StandardCharsets.ISO_8859_1);

    private final Dummifier dummifier = new Dummifier() {

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
