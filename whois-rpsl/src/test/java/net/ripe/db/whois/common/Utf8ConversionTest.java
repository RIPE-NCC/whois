package net.ripe.db.whois.common;

import net.ripe.db.whois.common.rpsl.RpslAttribute;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class Utf8ConversionTest {

    @Test
    public void convert_utf8_rpsl_string() {
        final String utf8String = """
                person:     Test Unicode
                address:    ?test
                address:    ΣΔ Street
                address:    привет Lane
                address:    مرحبا Road
                address:    你好ا Avenue
                e-mail:     example@xn--80adxhks.ru
                nic-hdl:    UNICODE-TEST
                mnt-by:     UPD-MNT
                source:     TEST
                """;
        final String rpslObjectUtfEscaped = """
                person:     Test Unicode
                address:    \u0000test
                address:    \u03A3\u0394 Street
                address:    \u043F\u0440\u0438\u0432\u0435\u0442 Lane
                address:    \u0645\u0631\u062D\u0628\u0627 Road
                address:    \u4F60\u597D\u0627 Avenue
                e-mail:     example@xn--80adxhks.ru
                nic-hdl:    UNICODE-TEST
                mnt-by:     UPD-MNT
                source:     TEST
                """;
        assertThat(Utf8Conversion.convertString(utf8String), is(Utf8Conversion.convertString(rpslObjectUtfEscaped)));
        assertThat(Utf8Conversion.createUtf8Attribute(new RpslAttribute("address", "?test")), is(Utf8Conversion.createUtf8Attribute(new RpslAttribute("address", "\u0000test"))));
        assertThat(Utf8Conversion.createUtf8Attribute(new RpslAttribute("address", "ΣΔ Street")), is(Utf8Conversion.createUtf8Attribute(new RpslAttribute("address", "\u03A3\u0394 Street"))));
        assertThat(Utf8Conversion.createUtf8Attribute(new RpslAttribute("address", "привет Lane")), is(Utf8Conversion.createUtf8Attribute(new RpslAttribute("address", "\u043F\u0440\u0438\u0432\u0435\u0442 Lane"))));
        assertThat(Utf8Conversion.createUtf8Attribute(new RpslAttribute("address", "你好ا Avenue")), is(Utf8Conversion.createUtf8Attribute(new RpslAttribute("address", "\u4F60\u597D\u0627 Avenue"))));
    }

}
