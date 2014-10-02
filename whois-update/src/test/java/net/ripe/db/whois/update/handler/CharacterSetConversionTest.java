package net.ripe.db.whois.update.handler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class CharacterSetConversionTest {

    @Test
    public void should_convert_ascci() {
        assertThat(CharacterSetConversion.isConvertableIntoLatin1(" !\":#$%&'()*+,-./"), is(true) );
        assertThat(CharacterSetConversion.isConvertableIntoLatin1("0123456789"), is(true) );
        assertThat(CharacterSetConversion.isConvertableIntoLatin1(":;<=>?@"), is(true) );
        assertThat(CharacterSetConversion.isConvertableIntoLatin1("[\\]^_`"), is(true) );
        assertThat(CharacterSetConversion.isConvertableIntoLatin1("{|}~"), is(true) );
        assertThat(CharacterSetConversion.isConvertableIntoLatin1("abcdefghijklmnopqrstuvwzyz"), is(true) );
        assertThat(CharacterSetConversion.isConvertableIntoLatin1("ABCDEFGHIJKLMNOPQRSTUVWXYZ"), is(true) );
    }

    @Test
    public void should_convert_latin1() {
        assertThat(CharacterSetConversion.isConvertableIntoLatin1(" ¡¢£¤¥¦§¨©ª«¬­®¯°±²³´µ¶·¸¹º»¼½¾¿ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿ"), is(true));
    }

    @Test
    public void should_not_convert_other_charsets() {
        assertThat(CharacterSetConversion.isConvertableIntoLatin1("привет"), is(false) );
        assertThat(CharacterSetConversion.isConvertableIntoLatin1("مرحبا"), is(false) );
        assertThat(CharacterSetConversion.isConvertableIntoLatin1("你好ا"), is(false) );
    }

    @Test
    public void should_not_choke_on_empty_input() {
        assertThat(CharacterSetConversion.isConvertableIntoLatin1(""), is(true) );
        assertThat(CharacterSetConversion.isConvertableIntoLatin1(null), is(true) );
    }

}
