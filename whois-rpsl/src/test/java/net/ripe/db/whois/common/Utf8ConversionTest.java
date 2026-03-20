package net.ripe.db.whois.common;

import net.ripe.db.whois.common.rpsl.RpslAttribute;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class Utf8ConversionTest {

    @Test
    public void convert_utf8_attributes_string() {
        assertThat(Utf8Conversion.createUtf8Attribute(new RpslAttribute("address", "\u03A3\u0394 Street")), is(Utf8Conversion.createUtf8Attribute(new RpslAttribute("address", "ΣΔ Street"))));
        assertThat(Utf8Conversion.createUtf8Attribute(new RpslAttribute("address", "\u043F\u0440\u0438\u0432\u0435\u0442 Lane")),is(Utf8Conversion.createUtf8Attribute(new RpslAttribute("address", "привет Lane"))));
        assertThat(Utf8Conversion.createUtf8Attribute(new RpslAttribute("address", "\u4F60\u597D\u0627 Avenue")), is(Utf8Conversion.createUtf8Attribute(new RpslAttribute("address", "你好ا Avenue"))));
    }

    @Test
    public void sanitise_utf8_attributes_string() {
        assertThat(Utf8Conversion.createUtf8Attribute(new RpslAttribute("address", "\u0000test")), is(Utf8Conversion.createUtf8Attribute(new RpslAttribute("address", "?test"))));
        assertThat(Utf8Conversion.createUtf8Attribute(new RpslAttribute("address", "\u0001\u0002 \u0003\u0004 Street")), is(Utf8Conversion.createUtf8Attribute(new RpslAttribute("address", "?? ?? Street"))));
        assertThat(Utf8Conversion.createUtf8Attribute(new RpslAttribute("address", "\u0005\u0006 \u0007\u0008 Street")), is(Utf8Conversion.createUtf8Attribute(new RpslAttribute("address", "?? ?? Street"))));
        assertThat(Utf8Conversion.createUtf8Attribute(new RpslAttribute("address", "\u0009test")), is(Utf8Conversion.createUtf8Attribute(new RpslAttribute("address", "\ttest"))));


        assertThat(Utf8Conversion.createUtf8Attribute(new RpslAttribute("address", "\u001b\u001f \u007f\u0084 Street")), is(Utf8Conversion.createUtf8Attribute(new RpslAttribute("address", "?? ?? Street"))));
        assertThat(Utf8Conversion.createUtf8Attribute(new RpslAttribute("address", "\u0085\u008c \u0090\u00a0 Street")), is(Utf8Conversion.createUtf8Attribute(new RpslAttribute("address", "\n? ?  Street "))));
    }

}
