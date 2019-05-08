package net.ripe.db.whois.common;

import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.Ignore;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class Latin1ConversionTest {

    @Test
    public void convert_ascii_string() {
        final String ascii = "!\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[]^_`abcdefghijklmnopqrstuvwxyz{|}~";

        assertThat(Latin1Conversion.convert(ascii), is(ascii));
    }

    @Test
    public void convert_supplment_latin1_string() {
        final String supplement = "¡¢£¤¥¦§¨©ª«¬­®¯°±²³´µ¶·¸¹º»¼½¾¿ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿ";

        assertThat(Latin1Conversion.convert(supplement), is(supplement));
    }

    @Test
    public void convert_control_characters_string() {
        final String control = new String(new byte[]{0x0, 0x0a}, StandardCharsets.ISO_8859_1);

        assertThat(Latin1Conversion.convert(control), is("?\n"));
    }

    @Test
    public void convert_non_break_space_string() {
        final String control = new String(new byte[]{(byte)0xa0}, StandardCharsets.ISO_8859_1);

        assertThat(Latin1Conversion.convert(control), is(" "));
    }

    @Test
    public void convert_non_latin1_string() {
        assertThat(Latin1Conversion.convert("ΣΔ"), is("??") );
        assertThat(Latin1Conversion.convert("привет"), is("??????") );
        assertThat(Latin1Conversion.convert("مرحبا"), is("?????") );
        assertThat(Latin1Conversion.convert("你好ا"), is("???") );
    }

    @Test
    public void convert_empty_input_string() {
        assertThat(Latin1Conversion.convert(""), is("") );
    }

    @Test
    public void convert_control_characters_rpsl() {
        assertThat(
            Latin1Conversion.convert(
                RpslObject.parse("person: Test\u0000 Person\nnic-hdl: TP1-TEST\nsource: TEST")),
            is(
                RpslObject.parse("person: Test? Person\nnic-hdl: TP1-TEST\nsource: TEST")));
    }

    @Test
    public void convert_non_break_space_rpsl() {
        assertThat(
            Latin1Conversion.convert(
                RpslObject.parse("person: Test\u00a0Person\nnic-hdl: TP1-TEST\nsource: TEST")),
            is(
                RpslObject.parse("person: Test Person\nnic-hdl: TP1-TEST\nsource: TEST")));
    }

    // TODO: [ES] UTF-8 u-umlaut is converted to '?' not latin-1 umlaut
    @Ignore
    @Test
    public void utf8_umlaut_not_substituted_rpsl() {
        assertThat(
            Latin1Conversion.convert(
                RpslObject.parse("person: Test P\uC3BCrson\nnic-hdl: TP1-TEST\nsource: TEST")),
            is(
                RpslObject.parse("person: Test Pürson\nnic-hdl: TP1-TEST\nsource: TEST")));
    }

    @Test
    public void utf16_umlaut_not_substituted_rpsl() {
        assertThat(
            Latin1Conversion.convert(
                RpslObject.parse("person: Test Pürson\nnic-hdl: TP1-TEST\nsource: TEST")),
            is(
                RpslObject.parse("person: Test Pürson\nnic-hdl: TP1-TEST\nsource: TEST")));
    }

}
