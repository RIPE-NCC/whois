package net.ripe.db.whois.common;

import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class Latin1ConversionTest {

    private final static String SUPPLEMENT = "¡¢£¤¥¦§¨©ª«¬®¯°±²³´µ¶·¸¹º»¼½¾¿ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿ";

    @Test
    public void convert_ascii_string() {
        final String ascii = "!\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[]^_`abcdefghijklmnopqrstuvwxyz{|}~";

        assertThat(Latin1Conversion.convertString(ascii), is(ascii));
    }

    @Test
    public void convert_supplement_latin1_string() {
        assertThat(Latin1Conversion.convertString(SUPPLEMENT), is(SUPPLEMENT));
    }

    @Test
    public void convert_rpsl_with_supplement() {
        assertThat(
            Latin1Conversion.convert("person: test\nnic-hdl: TP1-TEST\ndescr: " + SUPPLEMENT).getRpslObject(),
            is(RpslObject.parse("person: test\nnic-hdl: TP1-TEST\ndescr: " + SUPPLEMENT))
        );
    }

    @Test
    public void convert_control_characters_string() {
        final String control = new String(new byte[]{0x0, 0x0a}, StandardCharsets.ISO_8859_1);

        assertThat(Latin1Conversion.convertString(control), is("?\n"));
    }

    @Test
    public void convert_non_break_space_string() {
        final String control = new String(new byte[]{(byte)0xa0}, StandardCharsets.ISO_8859_1);

        assertThat(Latin1Conversion.convertString(control), is(" "));
    }

    @Test
    public void convert_silent_hyphen_string() {
        final String control = new String(new byte[]{(byte)0xad}, StandardCharsets.ISO_8859_1);

        assertThat(Latin1Conversion.convertString(control), is("-"));
    }

    @Test
    public void convert_non_latin1_string() {
        assertThat(Latin1Conversion.convertString("ΣΔ"), is("??") );
        assertThat(Latin1Conversion.convertString("привет"), is("??????") );
        assertThat(Latin1Conversion.convertString("مرحبا"), is("?????") );
        assertThat(Latin1Conversion.convertString("你好ا"), is("???") );
    }

    @Test
    public void convert_empty_input_string() {
        assertThat(Latin1Conversion.convertString(""), is("") );
    }

    @Test
    public void convert_control_characters_rpsl() {
        assertThat(
            Latin1Conversion.convert(
                "person: Test\u0000 Person\nnic-hdl: TP1-TEST\nsource: TEST").getRpslObject(),
            is(
                RpslObject.parse("person: Test? Person\nnic-hdl: TP1-TEST\nsource: TEST")));
    }

    @Test
    public void convert_non_break_space_rpsl() {
        assertThat(
            Latin1Conversion.convert(
                "person: Test\u00a0Person\nnic-hdl: TP1-TEST\nsource: TEST").getRpslObject(),
            is(
                RpslObject.parse("person: Test Person\nnic-hdl: TP1-TEST\nsource: TEST")));
    }

    @Test
    public void unicode_umlaut_substituted_correctly() {
        assertThat(
            Latin1Conversion.convert(
                "person: Test P\u00FCrson\nnic-hdl: TP1-TEST\nsource: TEST").getRpslObject(),
            is(
                RpslObject.parse("person: Test Pürson\nnic-hdl: TP1-TEST\nsource: TEST")));
    }

    @Test
    public void utf16_umlaut_not_substituted_rpsl() {
        assertThat(
            Latin1Conversion.convert(
                "person: Test Pürson\nnic-hdl: TP1-TEST\nsource: TEST").getRpslObject(),
            is(
                RpslObject.parse("person: Test Pürson\nnic-hdl: TP1-TEST\nsource: TEST")));
    }

}
