package net.ripe.db.whois.nrtm;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import joptsimple.OptionException;

public class QueryTest {

    public static final String SOURCE = "RIPE";

    @Test(expected = NullPointerException.class)
    public void null_argument() {
        new Query(SOURCE, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void empty() {
        new Query(SOURCE, "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void all_args() {
        new Query(SOURCE, "-q -g -k");
    }

    @Test(expected = IllegalArgumentException.class)
    public void flag_k_no_flag_g() {
        new Query(SOURCE, "-k");
    }

    @Test(expected = OptionException.class)
    public void flag_q_no_arg() {
        new Query(SOURCE, "-q");
    }

    @Test(expected = IllegalArgumentException.class)
    public void flag_q_unknown() {
        new Query(SOURCE, "-q foo");
    }

    @Test
    public void flag_q_source() {
        Query subject = new Query(SOURCE, "-q SouRCEs");

        assertTrue(subject.isInfoQuery());
        assertThat(subject.getQueryOption(), is(Query.QueryArgument.SOURCES));
    }

    @Test
    public void flag_q_version() {
        Query subject = new Query(SOURCE, "-q veRsION");

        assertTrue(subject.isInfoQuery());
        assertThat(subject.getQueryOption(), is(Query.QueryArgument.VERSION));
    }

    @Test
    public void flag_g_wrong_source() {
        try {
            new Query(SOURCE, "-g FOO");
            fail("No exception thrown");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("ERROR:403"));
        }
    }

    @Test
    public void flag_g_wrong_version() {
        try {
            new Query(SOURCE, "-g RIPE:0");
            fail("No exception thrown");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("ERROR:406"));
        }
    }

    @Test
    public void flag_g_wrong_element_coun() {
        try {
            new Query(SOURCE, "-g RIPE");
            fail("No exception thrown");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("ERROR:405"));
        }
    }

    @Test
    public void flag_g_wrong_first_nan() {
        try {
            new Query(SOURCE, "-g RIPE:3:FOO");
            fail("No exception thrown");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("ERROR:405"));
        }
    }

    @Test
    public void flag_g_wrong_second_nan() {
        try {
            new Query(SOURCE, "-g RIPE:3:1-FOO");
            fail("No exception thrown");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("ERROR:405"));
        }
    }

    @Test
    public void flag_g_wrong_second_before_first() {
        try {
            new Query(SOURCE, "-g RIPE:3:2-1");
            fail("No exception thrown");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("ERROR:405"));
        }
    }

    @Test
    public void flag_g_wrong_first_empty_tries_negative() {
        try {
            new Query(SOURCE, "-g RIPE:3:-1");
            fail("No exception thrown");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("ERROR:405"));
        }
    }

    @Test
    public void flag_g_ok() {
        Query subject = new Query(SOURCE, "-g RIPE:3:0-" + Integer.MAX_VALUE);

        assertTrue(subject.isMirrorQuery());
        assertFalse(subject.isKeepalive());

        assertThat(subject.getSerialBegin(), is(0));
        assertThat(subject.getSerialEnd(), is(Integer.MAX_VALUE));
    }

    @Test
    public void flag_g_ok_with_flag_k() {
        Query subject = new Query(SOURCE, "-k -g RIPE:3:0-" + Integer.MAX_VALUE);

        assertTrue(subject.isMirrorQuery());
        assertTrue(subject.isKeepalive());
    }

    @Test
    public void flag_g_end_is_LAST() {
        Query subject = new Query(SOURCE, "-g RIPE:3:0-LaSt");

        assertTrue(subject.isMirrorQuery());

        assertThat(subject.getSerialBegin(), is(0));
        assertThat(subject.getSerialEnd(), is(-1));
    }
}
