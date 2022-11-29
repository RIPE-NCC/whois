package net.ripe.db.nrtm4;

import net.ripe.db.whois.common.dao.RpslObjectModel;
import net.ripe.db.whois.common.dao.Serial;
import net.ripe.db.whois.common.dao.jdbc.SerialRpslObjectTuple;
import net.ripe.db.whois.common.domain.serials.Operation;
import net.ripe.db.whois.common.rpsl.Dummifier;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


public class DeltaProcessorTest {

    @Test
    void process_empty_tuples_returns_empty_list() {
        final var deltaProcessor = new DeltaProcessor(dummifier);
        final var changes = new ArrayList<SerialRpslObjectTuple>();
        final var result = deltaProcessor.process(changes);
        assertThat(result.size(), is(0));
    }

    @Test
    void process_single_tuple_returns_one_item() {
        final var deltaProcessor = new DeltaProcessor(dummifier);
        final var changes = new ArrayList<SerialRpslObjectTuple>();
        final var serial = new Serial(22, true, 33, 1, Operation.UPDATE);
        final var rpslObject = new RpslObjectModel(44, 55, ObjectType.INETNUM, "1.1.1.1", inetnumObject, System.currentTimeMillis());
        final var tuple = new SerialRpslObjectTuple(serial, rpslObject);
        changes.add(tuple);

        final var result = deltaProcessor.process(changes);

        assertThat(result.size(), is(1));
    }

    private RpslObject inetnumObject = RpslObject.parse("inetnum: 193.0.0.0 - 193.255.255.255\nsource: TEST");

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
