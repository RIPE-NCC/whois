package net.ripe.db.whois.common.domain;

import org.junit.Test;

import static net.ripe.db.whois.common.domain.CIString.ciSet;
import static net.ripe.db.whois.common.domain.CIString.ciString;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class CIStringTest {
    @Test
    public void create_null() {
        assertNull(ciString(null));
    }

    @Test
    public void equals_ci() {
        assertThat(ciString("ABC"), is(ciString("ABC")));
        assertThat(ciString("ABC"), is(ciString("abc")));
        assertThat(ciString("ABC"), is(ciString("aBc")));
    }

    @Test
    public void hashCode_ci() {
        assertThat(ciString("ABC").hashCode(), is(ciString("ABC").hashCode()));
        assertThat(ciString("ABC").hashCode(), is(ciString("abc").hashCode()));
        assertThat(ciString("ABC").hashCode(), is(ciString("aBc").hashCode()));
    }

    @Test
    public void ciset() {
        assertThat(ciSet("a", "b"), is(ciSet("A", "b")));
        assertThat(ciSet("a", "b").contains(ciString("A")), is(true));
        assertThat(ciSet("a", "b").contains(ciString("c")), is(false));
    }

    @Test
    public void tostring() {
        assertThat(ciString("SoMe WeIrd CasIng").toString(), is("SoMe WeIrd CasIng"));
    }

    @Test
    public void compare() {
        assertThat(ciString("abc").compareTo(ciString("ABC")), is(0));
        assertThat(ciString("abc").compareTo(ciString("AbC")), is(0));
        assertThat(ciString("abc").compareTo(ciString("def")), lessThan(0));
        assertThat(ciString("def").compareTo(ciString("ABC")), greaterThan(0));
        assertThat(ciString("DEF").compareTo(ciString("abc")), greaterThan(0));
    }

    @Test
    public void subsequence() {
        assertThat(ciString("abcdef").subSequence(1, 3), is((CharSequence) "bc"));
        assertThat(ciString("ABCDEF").subSequence(1, 3), is((CharSequence) "BC"));
    }

    @Test
    public void charAt() {
        assertThat(ciString("abcdef").charAt(1), is('b'));
        assertThat(ciString("ABCDEF").charAt(1), is('B'));
    }

    @Test
    public void length() {
        assertThat(ciString("").length(), is(0));
        assertThat(ciString("abcdef").length(), is(6));
    }

    @Test
    public void startsWith() {
        assertThat(ciString("").startsWith(ciString("")), is(true));
        assertThat(ciString("ab").startsWith(ciString("AB")), is(true));
        assertThat(ciString("abcdef").startsWith(ciString("AB")), is(true));
        assertThat(ciString("ABCDEF").startsWith(ciString("aB")), is(true));

        assertThat(ciString("ABCDEF").startsWith(ciString("def")), is(false));
    }

    @Test
    public void append() {
        assertThat(ciString("").append(ciString("")), is(ciString("")));
        assertThat(ciString("a").append(ciString("b")), is(ciString("ab")));
        assertThat(ciString("a").append(ciString("b")), is(ciString("AB")));
    }

    @Test
    public void toInt() {
        assertThat(ciString("1").toInt(), is(1));
        assertThat(ciString("312").toInt(), is(312));
    }

    @Test(expected = NumberFormatException.class)
    public void toInt_empty() {
        ciString("").toInt();
    }

    @Test(expected = NumberFormatException.class)
    public void toInt_invalid() {
        ciString("a").toInt();
    }
}
