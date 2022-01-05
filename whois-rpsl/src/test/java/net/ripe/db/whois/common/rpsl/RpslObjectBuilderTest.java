package net.ripe.db.whois.common.rpsl;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class RpslObjectBuilderTest {

    @Test
    public void remove_attribute_type_no_match() {
        assertThat(
            new RpslObjectBuilder(
                "mntner: OWNER-MNT\n" +
                "descr: test\n" +
                "source: TEST").removeAttributeType(AttributeType.REMARKS).get(),
            is(RpslObject.parse(
                "mntner: OWNER-MNT\n" +
                "descr: test\n" +
                "source: TEST")));
    }

    @Test
    public void remove_attribute_type_single_match() {
        assertThat(
            new RpslObjectBuilder(
                "mntner: OWNER-MNT\n" +
                "remarks: first\n" +
                "descr: test\n" +
                "source: TEST").removeAttributeType(AttributeType.REMARKS).get(),
            is(RpslObject.parse(
                "mntner: OWNER-MNT\n" +
                "descr: test\n" +
                "source: TEST")));
    }

    @Test
    public void remove_attribute_type_multiple_matches() {
        assertThat(
            new RpslObjectBuilder(
                "mntner: OWNER-MNT\n" +
                "remarks: first\n" +
                "descr: test\n" +
                "remarks: second\n" +
                "source: TEST").removeAttributeType(AttributeType.REMARKS).get(),
            is(RpslObject.parse(
                "mntner: OWNER-MNT\n" +
                "descr: test\n" +
                "source: TEST")));
    }
}
