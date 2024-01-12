package net.ripe.db.whois.common.rpsl.transform;

import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class FilterEmailFunctionTest {
    FilterEmailFunction subject;

    @BeforeEach
    public void setUp() throws Exception {
        subject = new FilterEmailFunction();
    }

    @Test
    public void apply_no_email_attribute() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "irt: DEV-IRT\n" +
                "auth: MD5-PW $1$YmPozTxJ$s3eGZRVrKVGdSDTeEZJu\n" +
                "source: RIPE"
        );

        final RpslObject response = subject.apply(rpslObject);
        assertThat(response, is(rpslObject));
    }

    @Test
    public void apply_email_attributes() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "mntner: WEIRD-MNT\n" +
                "e-mail: value\n" +
                "e-mail: value\n" +
                "notify: value\n" +
                "notify: value\n" +
                "ref-nfy: value\n" +
                "ref-nfy: value\n" +
                "mnt-nfy: value\n" +
                "mnt-nfy: value\n" +
                "upd-to: value\n" +
                "upd-to: value\n" +
                "source: RIPE\n"
        );

        final RpslObject response = subject.apply(rpslObject);
        assertThat(response.toString(), is("" +
                "mntner:         WEIRD-MNT\n" +
                "source:         RIPE # Filtered\n"));
    }

    @Test
    public void apply_email_attributes_not_filtered() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "mntner:         WEIRD-MNT\n" +
                "source:         RIPE\n"
        );

        final RpslObject response = subject.apply(rpslObject);
        assertThat(response.toString(), is("" +
                "mntner:         WEIRD-MNT\n" +
                "source:         RIPE\n"));
    }

    @Test
    public void apply_filtered_only_once() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "mntner: WEIRD-MNT\n" +
                "source:RIPE # Filtered\n" +
                "e-mail:some@one.com\n"
        );

        final RpslObject response = subject.apply(rpslObject);
        assertThat(response.toString(), is("" +
                "mntner:         WEIRD-MNT\n" +
                "source:         RIPE # Filtered\n"));
    }
}
