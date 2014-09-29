package net.ripe.db.whois.update.handler;

import org.aspectj.lang.annotation.After;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.AssertThrows;

import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(MockitoJUnitRunner.class)
public class CharacterSetConversionTest {

    @Test
    public void testStraightforwardLatin1Text() {
        assertThat(CharacterSetConversion.isConvertableIntoLatin1(" !\":#$%&'()*+,-./"), is(true) );
        assertThat(CharacterSetConversion.isConvertableIntoLatin1("0123456789"), is(true) );
        assertThat(CharacterSetConversion.isConvertableIntoLatin1(":;<=>?@"), is(true) );
        assertThat(CharacterSetConversion.isConvertableIntoLatin1("[\\]^_`"), is(true) );
        assertThat(CharacterSetConversion.isConvertableIntoLatin1("{|}~"), is(true) );
        assertThat(CharacterSetConversion.isConvertableIntoLatin1("abcdefghijklmnopqrstuvwzyz"), is(true) );
        assertThat(CharacterSetConversion.isConvertableIntoLatin1("ABCDEFGHIJKLMNOPQRSTUVWXYZ"), is(true) );
    }

    @Test
    public void testStrangeLatin1Text() {
        assertThat(CharacterSetConversion.isConvertableIntoLatin1("£ © ß ä û ö ü é è Ç"), is(true) );
    }

    @Test
    public void testNonLatin() {
        assertThat(CharacterSetConversion.isConvertableIntoLatin1("привет"), is(false) );
        assertThat(CharacterSetConversion.isConvertableIntoLatin1("مرحبا"), is(false) );
        assertThat(CharacterSetConversion.isConvertableIntoLatin1("你好ا"), is(false) );
    }

}
