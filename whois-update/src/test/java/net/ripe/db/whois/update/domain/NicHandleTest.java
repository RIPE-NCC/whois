package net.ripe.db.whois.update.domain;

import net.ripe.db.whois.common.domain.CIString;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static net.ripe.db.whois.common.domain.CIString.ciSet;
import static net.ripe.db.whois.common.domain.CIString.ciString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class NicHandleTest {
    CIString source;
    Set<CIString> countryCodes;

    @Before
    public void setUp() throws Exception {
        source = ciString("RIPE");
        countryCodes = ciSet("NL", "EN");
    }

    @Test
    public void parse_auto() {
        try {
            NicHandle.parse("AUTO-1", source, countryCodes);
            fail("AUTO- should not be supported as NIC-HDL");
        } catch (NicHandleParseException e) {
            assertThat(e.getMessage(), is("Primary key generation request cannot be parsed as NIC-HDL: AUTO-1"));
        }
    }

    @Test
    public void parse_auto_lowercase() {
        try {
            NicHandle.parse("auto-1", source, countryCodes);
            fail("AUTO- should not be supported as NIC-HDL");
        } catch (NicHandleParseException e) {
            assertThat(e.getMessage(), is("Primary key generation request cannot be parsed as NIC-HDL: auto-1"));
        }
    }

    @Test
    public void parse_empty() {
        try {
            NicHandle.parse("", source, countryCodes);
            fail("Empty should not be supported as NIC-HDL");
        } catch (NicHandleParseException e) {
            assertThat(e.getMessage(), is("Invalid NIC-HDL: "));
        }
    }

    @Test
    public void parse_character_space_only() {
        final NicHandle nicHandle = NicHandle.parse("DW", source, countryCodes);

        assertThat(nicHandle.getSpace(), is("DW"));
        assertThat(nicHandle.getIndex(), is(0));
        assertNull(nicHandle.getSuffix());
        assertThat(nicHandle.toString(), is("DW"));
    }

    @Test(expected = NicHandleParseException.class)
    public void parse_space_too_long() {
        NicHandle.parse("SPACE", source, countryCodes);
    }

    @Test(expected = NicHandleParseException.class)
    public void parse_suffix_too_long() {
        NicHandle.parse("DW-VERYLONGSUFFIX", source, countryCodes);
    }

    @Test
    public void parse_character_space_and_index_only() {
        final NicHandle nicHandle = NicHandle.parse("DW123  ", source, countryCodes);

        assertThat(nicHandle.getSpace(), is("DW"));
        assertThat(nicHandle.getIndex(), is(123));
        assertNull(nicHandle.getSuffix());
        assertThat(nicHandle.toString(), is("DW123"));
    }

    @Test
    public void parse_character_space_and_suffix_only() {
        final NicHandle nicHandle = NicHandle.parse("DW-RIPE", source, countryCodes);

        assertThat(nicHandle.getSpace(), is("DW"));
        assertThat(nicHandle.getIndex(), is(0));
        assertThat(nicHandle.getSuffix(), is("RIPE"));
        assertThat(nicHandle.toString(), is("DW-RIPE"));
    }

    @Test
    public void parse_suffix_source() {
        final NicHandle nicHandle = NicHandle.parse("DW123-" + source, source, countryCodes);

        assertThat(nicHandle.getSpace(), is("DW"));
        assertThat(nicHandle.getIndex(), is(123));
        assertThat(nicHandle.getSuffix(), is(source.toString()));
        assertThat(nicHandle.toString(), is("DW123-RIPE"));
    }

    @Test
    public void parse_suffix_countryCode() {
        final CIString countryCode = countryCodes.iterator().next();
        final NicHandle nicHandle = NicHandle.parse("AB12-" + countryCode, source, countryCodes);

        assertThat(nicHandle.getSpace(), is("AB"));
        assertThat(nicHandle.getIndex(), is(12));
        assertThat(nicHandle.getSuffix(), is(countryCode.toString()));
    }

    @Test
    public void parse_suffix_predefined_lowercase() {
        final NicHandle nicHandle = NicHandle.parse("dw-apnic", source, countryCodes);

        assertThat(nicHandle.getSpace(), is("dw"));
        assertThat(nicHandle.getIndex(), is(0));
        assertThat(nicHandle.getSuffix(), is("apnic"));
    }

    @Test(expected = NicHandleParseException.class)
    public void parse_suffix_invalid() {
        NicHandle.parse("DW-SOMETHING", source, countryCodes);
    }

    @Test
    public void equal_null() {
        final NicHandle nicHandle = NicHandle.parse("DW", source, countryCodes);
        assertFalse(nicHandle.equals(null));
    }

    @Test
    public void equal_otherClass() {
        final NicHandle nicHandle = NicHandle.parse("DW", source, countryCodes);
        assertFalse(nicHandle.equals(""));
    }

    @Test
    public void equal_self() {
        final NicHandle nicHandle = NicHandle.parse("DW", source, countryCodes);
        assertTrue(nicHandle.equals(nicHandle));
    }

    @Test
    public void equal_same() {
        final NicHandle nicHandle = NicHandle.parse("DW", source, countryCodes);
        assertTrue(nicHandle.equals(NicHandle.parse("DW", source, countryCodes)));
    }

    @Test
    public void equal_different() {
        final NicHandle nicHandle = NicHandle.parse("DW", source, countryCodes);
        assertFalse(nicHandle.equals(NicHandle.parse("AB", source, countryCodes)));
    }

    @Test
    public void hashCode_check() {
        NicHandle.parse("DW", source, countryCodes).hashCode();
    }
}
