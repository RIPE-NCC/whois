package net.ripe.db.whois.nrtm;

import joptsimple.OptionException;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public class QueryTest {

    public static final String SOURCE = "RIPE";
    public static final String NONAUTH_SOURCE = "RIPE-NONAUTH";

    @Test
    public void null_argument() {
        assertThrows(NullPointerException.class, () -> {
            new Query(SOURCE, null);
        });

    }

    @Test
    public void empty() {
        assertThrows(NrtmException.class, () -> {
            new Query(SOURCE, "");
        });
    }

    @Test
    public void all_args() {
        assertThrows(NrtmException.class, () -> {
            new Query(SOURCE, "-q -g -k");
        });
    }

    @Test
    public void flag_k_no_flag_g() {
        assertThrows(NrtmException.class, () -> {
            new Query(SOURCE, "-k");
        });
    }

    @Test
    public void flag_q_no_arg() {
        assertThrows(OptionException.class, () -> {
            new Query(SOURCE, "-q");
        });
    }

    @Test
    public void flag_q_unknown() {
        assertThrows(NrtmException.class, () -> {
            new Query(SOURCE, "-q foo");
        });
    }

    @Test
    public void flag_q_source() {
        Query subject = new Query(SOURCE, "-q SouRCEs");

        assertThat(subject.isInfoQuery(), is(true));
        assertThat(subject.getQueryOption(), is(Query.QueryArgument.SOURCES));
    }

    @Test
    public void flag_q_version() {
        Query subject = new Query(SOURCE, "-q veRsION");

        assertThat(subject.isInfoQuery(), is(true));
        assertThat(subject.getQueryOption(), is(Query.QueryArgument.VERSION));
    }

    @Test
    public void flag_g_wrong_source() {
        try {
            new Query(SOURCE, NONAUTH_SOURCE, "-g FOO");
            fail("No exception thrown");
        } catch (NrtmException e) {
            assertThat(e.getMessage(), containsString("ERROR:403"));
        }
    }

    @Test
    public void flag_g_wrong_version() {
        try {
            new Query(SOURCE, "-g RIPE:0");
            fail("No exception thrown");
        } catch (NrtmException e) {
            assertThat(e.getMessage(), containsString("ERROR:406"));
        }
    }

    @Test
    public void flag_g_wrong_element_coun() {
        try {
            new Query(SOURCE, "-g RIPE");
            fail("No exception thrown");
        } catch (NrtmException e) {
            assertThat(e.getMessage(), containsString("ERROR:405"));
        }
    }

    @Test
    public void flag_g_wrong_first_nan() {
        try {
            new Query(SOURCE, "-g RIPE:3:FOO");
            fail("No exception thrown");
        } catch (NrtmException e) {
            assertThat(e.getMessage(), containsString("ERROR:405"));
        }
    }

    @Test
    public void flag_g_wrong_second_nan() {
        try {
            new Query(SOURCE, "-g RIPE:3:1-FOO");
            fail("No exception thrown");
        } catch (NrtmException e) {
            assertThat(e.getMessage(), containsString("ERROR:405"));
        }
    }

    @Test
    public void flag_g_wrong_second_before_first() {
        try {
            new Query(SOURCE, "-g RIPE:3:2-1");
            fail("No exception thrown");
        } catch (NrtmException e) {
            assertThat(e.getMessage(), containsString("ERROR:405"));
        }
    }

    @Test
    public void flag_g_wrong_first_empty_tries_negative() {
        try {
            new Query(SOURCE, "-g RIPE:3:-1");
            fail("No exception thrown");
        } catch (NrtmException e) {
            assertThat(e.getMessage(), containsString("ERROR:405"));
        }
    }

    @Test
    public void flag_g_ok() {
        Query subject = new Query(SOURCE, "-g RIPE:3:0-" + Integer.MAX_VALUE);

        assertThat(subject.isMirrorQuery(), is(true));
        assertThat(subject.isKeepalive(), is(false));

        assertThat(subject.getSerialBegin(), is(0));
        assertThat(subject.getSerialEnd(), is(Integer.MAX_VALUE));
    }

    @Test
    public void flag_g_ok_with_flag_k() {
        Query subject = new Query(SOURCE, "-k -g RIPE:3:0-" + Integer.MAX_VALUE);

        assertThat(subject.isMirrorQuery(), is(true));
        assertThat(subject.isKeepalive(), is(true));
    }

    @Test
    public void flag_g_end_is_LAST() {
        Query subject = new Query(SOURCE, "-g RIPE:3:0-LaSt");

        assertThat(subject.isMirrorQuery(), is(true));

        assertThat(subject.getSerialBegin(), is(0));
        assertThat(subject.getSerialEnd(), is(-1));
    }
}
