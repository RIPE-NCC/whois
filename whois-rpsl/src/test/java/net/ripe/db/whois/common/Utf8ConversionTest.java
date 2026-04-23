package net.ripe.db.whois.common;

import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class Utf8ConversionTest {

    @Test
    public void normalise_decomposed_unicode_form() {
        // Java considers the r-caron codepoint U+0159 and decomposed U+0072U+030C to be the same, so we must also check string length to be sure the decomposed form is normalised
        assertThat(Utf8Conversion.createUtf8Attribute(new RpslAttribute(AttributeType.DESCR, "\u0072\u030C")).getValue().length(), is(1));
        assertThat(Utf8Conversion.createUtf8Attribute(new RpslAttribute(AttributeType.DESCR, "Ond\u0072\u030Cej Caletka")).getValue(), is("Ond\u0159ej Caletka"));
    }

    @Test
    public void convert_utf8_attributes_string() {
        assertThat(Utf8Conversion.createUtf8Attribute(new RpslAttribute("address", "\u03A3\u0394 Street")), is(Utf8Conversion.createUtf8Attribute(new RpslAttribute("address", "ΣΔ Street"))));
        assertThat(Utf8Conversion.createUtf8Attribute(new RpslAttribute("address", "\u043F\u0440\u0438\u0432\u0435\u0442 Lane")),is(Utf8Conversion.createUtf8Attribute(new RpslAttribute("address", "привет Lane"))));
        assertThat(Utf8Conversion.createUtf8Attribute(new RpslAttribute("address", "\u4F60\u597D\u0627 Avenue")), is(Utf8Conversion.createUtf8Attribute(new RpslAttribute("address", "你好ا Avenue"))));
    }

    @Test
    public void sanitise_utf8_control_string() {
        assertThat(Utf8Conversion.createUtf8Attribute(new RpslAttribute("address", "\u0000test")), is(Utf8Conversion.createUtf8Attribute(new RpslAttribute("address", "?test"))));
        assertThat(Utf8Conversion.createUtf8Attribute(new RpslAttribute("address", "\u0001\u0002 \u0003\u0004 Street")), is(Utf8Conversion.createUtf8Attribute(new RpslAttribute("address", "?? ?? Street"))));
        assertThat(Utf8Conversion.createUtf8Attribute(new RpslAttribute("address", "\u0005\u0006 \u0007\u0008 Street")), is(Utf8Conversion.createUtf8Attribute(new RpslAttribute("address", "?? ?? Street"))));
        assertThat(Utf8Conversion.createUtf8Attribute(new RpslAttribute("address", "\u0009test")), is(Utf8Conversion.createUtf8Attribute(new RpslAttribute("address", "\ttest"))));

        assertThat(Utf8Conversion.createUtf8Attribute(new RpslAttribute("address", "\u001b\u001f \u007f\u0084 Street")), is(Utf8Conversion.createUtf8Attribute(new RpslAttribute("address", "?? ?? Street"))));
        assertThat(Utf8Conversion.createUtf8Attribute(new RpslAttribute("address", "\u0085\u008c \u0090\u00a0 Street")), is(Utf8Conversion.createUtf8Attribute(new RpslAttribute("address", "\n? ?  Street "))));
    }

    @Test
    public void invisible_characters_already_handled(){
        // Variation Selectors: U+FE00 – U+FE0F
       for (int cp = 0xFE00; cp <= 0xFE0F; cp++) {
            final String escapedEncoding = String.format("\\u%04X", cp);
            assertThat(Utf8Conversion.createUtf8Attribute(new RpslAttribute("address", escapedEncoding + "test")), is(Utf8Conversion.createUtf8Attribute(new RpslAttribute("address", "?test"))));
        }

        // Variation Selectors Supplement: U+E0100 – U+E01EF
        for (int cp = 0xE0100; cp <= 0xE01EF; cp++) {
            final String invisibleString = new String(Character.toChars(cp)); // needs to go in surrogates because 5 hex
            assertThat(Utf8Conversion.createUtf8Attribute(new RpslAttribute("address", invisibleString + "test")), is(Utf8Conversion.createUtf8Attribute(new RpslAttribute("address", "?test"))));
        }

        // Try directly with E0100 surrogates
        assertThat(Utf8Conversion.createUtf8Attribute(new RpslAttribute("address", "\uDB40\uDD00test")), is(Utf8Conversion.createUtf8Attribute(new RpslAttribute("address", "?test"))));
    }

}
