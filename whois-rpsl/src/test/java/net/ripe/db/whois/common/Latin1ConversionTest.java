package net.ripe.db.whois.common;

import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static net.ripe.db.whois.common.Latin1Conversion.convertAndSubstitute;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class Latin1ConversionTest {

    private final static String SUPPLEMENT = "¡¢£¤¥¦§¨©ª«¬­®¯°±²³´µ¶·¸¹º»¼½¾¿ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿ";

    @Test
    public void convert_ascii_string() {
        final String ascii = "!\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[]^_`abcdefghijklmnopqrstuvwxyz{|}~";

        assertThat(convertAndSubstitute(ascii), is(ascii));
    }

    @Test
    public void convert_supplement_latin1_string() {
        assertThat(convertAndSubstitute(SUPPLEMENT), is(SUPPLEMENT));
    }

    @Test
    public void convert_rpsl_with_supplement() {
        assertThat(
            RpslObject.parse(convertAndSubstitute("person: test\nnic-hdl: TP1-TEST\ndescr: " + SUPPLEMENT)),
            is(RpslObject.parse("person: test\nnic-hdl: TP1-TEST\ndescr: " + SUPPLEMENT))
        );
    }

    @Test
    public void convert_control_characters_string() {
        final String control = new String(new byte[]{0x0, 0x0a}, StandardCharsets.ISO_8859_1);

        assertThat(convertAndSubstitute(control), is("?\n"));
    }

    @Test
    public void convert_non_break_space_string() {
        final String control = new String(new byte[]{(byte)0xa0}, StandardCharsets.ISO_8859_1);

        assertThat(convertAndSubstitute(control), is(" "));
    }

    @Test
    public void convert_non_latin1_string() {
        assertThat(convertAndSubstitute("ΣΔ"), is("??") );
        assertThat(convertAndSubstitute("привет"), is("??????") );
        assertThat(convertAndSubstitute("مرحبا"), is("?????") );
        assertThat(convertAndSubstitute("你好ا"), is("???") );
    }

    @Test
    public void convert_empty_input_string() {
        assertThat(convertAndSubstitute(""), is("") );
    }

    @Test
    public void convert_control_characters_rpsl() {
        assertThat(
            RpslObject.parse(convertAndSubstitute(
                "person: Test\u0000 Person\nnic-hdl: TP1-TEST\nsource: TEST")),
            is(
                RpslObject.parse("person: Test? Person\nnic-hdl: TP1-TEST\nsource: TEST")));
    }

    @Test
    public void convert_non_break_space_rpsl() {
        assertThat(
                RpslObject.parse(convertAndSubstitute(
                "person: Test\u00a0Person\nnic-hdl: TP1-TEST\nsource: TEST")),
            is(
                RpslObject.parse("person: Test Person\nnic-hdl: TP1-TEST\nsource: TEST")));
    }

    @Test
    public void unicode_umlaut_substituted_correctly() {
        assertThat(
                RpslObject.parse(convertAndSubstitute(
                "person: Test P\u00FCrson\nnic-hdl: TP1-TEST\nsource: TEST")),
            is(
                RpslObject.parse("person: Test Pürson\nnic-hdl: TP1-TEST\nsource: TEST")));
    }

    @Test
    public void utf16_umlaut_not_substituted_rpsl() {
        assertThat(
                RpslObject.parse(convertAndSubstitute(
                "person: Test Pürson\nnic-hdl: TP1-TEST\nsource: TEST")),
            is(
                RpslObject.parse("person: Test Pürson\nnic-hdl: TP1-TEST\nsource: TEST")));
    }

}
