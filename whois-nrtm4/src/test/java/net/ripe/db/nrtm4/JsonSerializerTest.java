package net.ripe.db.nrtm4;

import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


public class JsonSerializerTest {

    @Test
    void payload_processor_can_serialize_empty_array() {
        final var payloads = new RpslObject[] {};
        final var payloadProcessor = new JsonSerializer();
        assertThat(payloadProcessor.process(payloads), is("[]"));
    }

    @Test
    void payload_processor_can_serialize_delta() {
        final var payloads = new DeltaChange[] {
            DeltaChange.addModify(1, RpslObject.parse(inetnumObjectBytes))
        };
        final var payloadProcessor = new JsonSerializer();
        assertThat(payloadProcessor.process(payloads), is("[{\"action\":\"add_modify\",\"object\":\"inetnum:        193.0.0.0 - 193.255.255.255\\nsource:         TEST\\n\"}]"));
    }

    @Test
    void payload_processor_can_serialize_delta_with_extended_latin1() {
        final var payloads = new DeltaChange[] {
            DeltaChange.addModify(1, RpslObject.parse(orgObjectBytes))
        };
        final var payloadProcessor = new JsonSerializer();
        assertThat(payloadProcessor.process(payloads), is("[{\"action\":\"add_modify\",\"object\":\"organisation:   ORG-XYZ99-RIPE\\norg-name:       XYZ B.V.\\norg-type:       OTHER\\naddress:        XYZ B.V.\\naddress:        ÅçÅçstraße 999\\naddress:        Zürich\\naddress:        NETHERLANDS\\nphone:          +31709876543\\nfax-no:         +31703456789\\nmnt-by:         XYZ-MNT\\nmnt-ref:        PQR-MNT\\nabuse-c:        XYZ-RIPE\\ncreated:        2018-01-01T00:00:00Z\\nlast-modified:  2019-12-24T00:00:00Z\\nsource:         TEST\\n\"}]"));
    }

    // TODO: check that the 'expected' strings are correct wrt escaping line feeds!
    // TODO: should object_type be lower? it is in the RFC but it's not specced, it's only in examples.
    // TODO: test multiline attribute
    private final byte[] inetnumObjectBytes = "inetnum: 193.0.0.0 - 193.255.255.255\nsource: TEST".getBytes(StandardCharsets.ISO_8859_1);
    private final byte[] orgObjectBytes = "organisation:    ORG-XYZ99-RIPE\norg-name:        XYZ B.V.\norg-type:        OTHER\naddress:         XYZ B.V.\naddress:         ÅçÅçstraße 999\naddress:         Zürich\naddress:         NETHERLANDS\nphone:           +31709876543\nfax-no:          +31703456789\nmnt-by:          XYZ-MNT\nmnt-ref:         PQR-MNT\nabuse-c:         XYZ-RIPE\ncreated:         2018-01-01T00:00:00Z\nlast-modified:   2019-12-24T00:00:00Z\nsource:          TEST\n".getBytes(StandardCharsets.ISO_8859_1);

}
