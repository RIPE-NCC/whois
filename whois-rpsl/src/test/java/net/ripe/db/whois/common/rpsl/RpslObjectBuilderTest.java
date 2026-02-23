package net.ripe.db.whois.common.rpsl;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

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
    public void test_utf8_characters_utf8_parsed() {

        final byte[] utf8Input = """
        person:     Test Person3
        address:    Flughafenstra\\u00DFe 109\\u20AC/a😀
        address:    Münchenß, Germany€
        phone:      +49 282 411141
        fax-no:     +49 282 411140
        nic-hdl:    TP3-TEST
        changed:    dbtest@ripe.net 20120101
        mnt-by:     UPD-MNT
        source:     TEST
        password: update
        """.getBytes(StandardCharsets.UTF_8);

        //latin1 parsing mangled output
        assertThat(
                new RpslObjectBuilder(utf8Input).get(),
                is(not(RpslObject.parse(
                        """
                                person:     Test Person3
                                address:    Flughafenstra\\u00DFe 109\\u20AC/a😀
                                address:    Münchenß, Germany€
                                phone:      +49 282 411141
                                fax-no:     +49 282 411140
                                nic-hdl:    TP3-TEST
                                changed:    dbtest@ripe.net 20120101
                                mnt-by:     UPD-MNT
                                source:     TEST
                                password: update
                                """))));

        //utf8 parsing correct output
        assertThat(
                new RpslObjectBuilder(new String(utf8Input)).get(),
                is(RpslObject.parse(
                        """
                                person:     Test Person3
                                address:    Flughafenstra\\u00DFe 109\\u20AC/a😀
                                address:    Münchenß, Germany€
                                phone:      +49 282 411141
                                fax-no:     +49 282 411140
                                nic-hdl:    TP3-TEST
                                changed:    dbtest@ripe.net 20120101
                                mnt-by:     UPD-MNT
                                source:     TEST
                                password: update
                                """)));
    }

    @Test
    public void test_latin1_characters_utf8_parsed() {

        final byte[] justLatinInput = """
        person:     Test Person3
        address:    Flughafenstra\\u00DFe 109\\u20AC/a
        address:    Münchenß, Germany
        phone:      +49 282 411141
        fax-no:     +49 282 411140
        nic-hdl:    TP3-TEST
        changed:    dbtest@ripe.net 20120101
        mnt-by:     UPD-MNT
        source:     TEST
        password: update
        """.getBytes(StandardCharsets.ISO_8859_1);

        //latin1 parsing correct output
        assertThat(
                new RpslObjectBuilder(justLatinInput).get(),
                is(RpslObject.parse(
                        """
                                person:     Test Person3
                                address:    Flughafenstra\\u00DFe 109\\u20AC/a
                                address:    Münchenß, Germany
                                phone:      +49 282 411141
                                fax-no:     +49 282 411140
                                nic-hdl:    TP3-TEST
                                changed:    dbtest@ripe.net 20120101
                                mnt-by:     UPD-MNT
                                source:     TEST
                                password: update
                                """)));

        //utf8 parsing correct output
        assertThat(
                new RpslObjectBuilder(new String(justLatinInput, StandardCharsets.ISO_8859_1)).get(),
                is(RpslObject.parse(
                        """
                                person:     Test Person3
                                address:    Flughafenstra\\u00DFe 109\\u20AC/a
                                address:    Münchenß, Germany
                                phone:      +49 282 411141
                                fax-no:     +49 282 411140
                                nic-hdl:    TP3-TEST
                                changed:    dbtest@ripe.net 20120101
                                mnt-by:     UPD-MNT
                                source:     TEST
                                password: update
                                """)));
    }
}
