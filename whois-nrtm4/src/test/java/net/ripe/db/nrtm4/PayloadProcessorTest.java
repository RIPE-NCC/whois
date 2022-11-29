package net.ripe.db.nrtm4;

import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


public class PayloadProcessorTest {

    @Test
    void payload_processor_can_serialize_empty_array() {
        final var payloads = new RpslObject[] {};
        final var payloadProcessor = new PayloadProcessor(payloads);
        assertThat(payloadProcessor.getJson(), is("[]"));
    }

    @Test
    void payload_processor_can_serialize_simple_inetnum() {
        final var payloads = new RpslObject[] {
            RpslObject.parse(inetnumObjectBytes)
        };
        final var payloadProcessor = new PayloadProcessor(payloads);
        assertThat(payloadProcessor.getJson(), is("[\"inetnum:        193.0.0.0 - 193.255.255.255\\nsource:         TEST\\n\"]"));
    }

    @Test
    void payload_processor_can_serialize_delta() {
        final var payloads = new DeltaChange[] {
            new DeltaChange(DeltaChange.Action.ADD_MODIFY, ObjectType.INETNUM, null, RpslObject.parse(inetnumObjectBytes))
        };
        final var payloadProcessor = new PayloadProcessor(payloads);
        assertThat(payloadProcessor.getJson(), is("[{\"action\":\"add_modify\",\"object\":\"inetnum:        193.0.0.0 - 193.255.255.255\\nsource:         TEST\\n\",\"object_class\":\"INETNUM\"}]"));
    }

    // TODO: check that the 'expected' strings are correct wrt escaping line feeds!
    // TODO: should object_type be lower? it is in the RFC but it's not specced, it's only in examples.
    // TODO: test multiline
    // TODO: test extended Latin1 characters and other non-ASCII input
    private final byte[] inetnumObjectBytes = "inetnum: 193.0.0.0 - 193.255.255.255\nsource: TEST".getBytes(StandardCharsets.ISO_8859_1);

}
