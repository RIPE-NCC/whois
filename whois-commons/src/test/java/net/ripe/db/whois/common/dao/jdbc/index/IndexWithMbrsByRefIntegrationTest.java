package net.ripe.db.whois.common.dao.jdbc.index;


import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

@Tag("IntegrationTest")
public class IndexWithMbrsByRefIntegrationTest extends IndexIntegrationTestBase {
    private IndexStrategy subject;

    @BeforeEach
    public void setUp() throws Exception {
        subject = IndexStrategies.get(AttributeType.MBRS_BY_REF);
    }

    @Test
    public void add_and_find_any() {
        final RpslObject asSet = RpslObject.parse("" +
                "as-set:          AS-TEST\n" +
                "mbrs-by-ref:     ANY\n" +
                "source:          RIPE");

        databaseHelper.addObject(asSet);

        final List<RpslObjectInfo> results = subject.findInIndex(whoisTemplate, "ANY");
        assertThat(results, hasSize(1));
    }

    @Test
    public void add_and_find_ref() {
        databaseHelper.addObject("mntner: DEV-MNT");

        final RpslObject asSet = RpslObject.parse("" +
                "as-set:          AS-TEST\n" +
                "mbrs-by-ref:     DEV-MNT\n" +
                "source:          RIPE");

        databaseHelper.addObject(asSet);

        final List<RpslObjectInfo> results = subject.findInIndex(whoisTemplate, "DEV-MNT");
        assertThat(results, hasSize(1));
    }

    @Test
    public void not_found_in_index() throws Exception {
        final List<RpslObjectInfo> results = subject.findInIndex(whoisTemplate, "MBRS");

        assertThat(results, hasSize(0));
    }

    @Test
    public void not_found_in_index_any() throws Exception {
        final List<RpslObjectInfo> results = subject.findInIndex(whoisTemplate, "ANY");

        assertThat(results, hasSize(0));
    }
}
