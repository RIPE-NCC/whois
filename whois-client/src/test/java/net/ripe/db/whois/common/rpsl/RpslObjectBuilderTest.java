package net.ripe.db.whois.common.rpsl;

import org.junit.Test;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

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

    @Test
    public void should_convert_character_encoding_utf8__into_latin1() {
        final RpslObject subject = new RpslObjectBuilder("" +
                "person:  Person\n" +
                "e-mail:  noreply@ripe.net\n" +
                "mnt-by:  OWNER-MNT\n" +
                "nic-hdl: PP1-TEST\n" +
                "remarks: test \u03A3 and \u00DF characters\n").get();

        assertThat(RpslObjectTest.convertToString(subject.getValuesForAttribute(AttributeType.REMARKS)), contains("test ? and \u00DF characters"));
        assertThat(subject.toString(), is("" +
                "person:         Person\n" +
                "e-mail:         noreply@ripe.net\n" +
                "mnt-by:         OWNER-MNT\n" +
                "nic-hdl:        PP1-TEST\n" +
                "remarks:        test ? and \u00DF characters\n"));
    }

    @Test
    public void should_convert_character_encoding_utf8__into_latin1_also() {
        final RpslObject subject = new RpslObjectBuilder()
                .append(new RpslAttribute(AttributeType.PERSON, "Person"))
                .append(new RpslAttribute(AttributeType.E_MAIL, "noreply@ripe.net"))
                .append(new RpslAttribute(AttributeType.MNT_BY, "OWNER-MNT"))
                .append(new RpslAttribute(AttributeType.NIC_HDL, "PP1-TEST"))
                .append(new RpslAttribute(AttributeType.REMARKS, "test \u03A3 and \u00DF characters"))
                .get();

        assertThat(RpslObjectTest.convertToString(subject.getValuesForAttribute(AttributeType.REMARKS)), contains("test ? and \u00DF characters"));
        assertThat(subject.toString(), is("" +
                "person:         Person\n" +
                "e-mail:         noreply@ripe.net\n" +
                "mnt-by:         OWNER-MNT\n" +
                "nic-hdl:        PP1-TEST\n" +
                "remarks:        test ? and \u00DF characters\n"));
    }
}
