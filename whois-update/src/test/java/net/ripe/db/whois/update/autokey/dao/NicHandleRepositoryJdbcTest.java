package net.ripe.db.whois.update.autokey.dao;

import net.ripe.db.whois.update.dao.AbstractDaoTest;
import net.ripe.db.whois.update.domain.NicHandle;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

@Transactional
public class NicHandleRepositoryJdbcTest extends AbstractDaoTest {
    @Autowired NicHandleRepository subject;

    @Test
    public void claimSpecified_empty_database() {
        assertThat(subject.claimSpecified(new NicHandle("DW", 0, "RIPE")), is(true));
        assertRows(1);
    }

    @Test
    public void claimSpecified_twice() {
        final NicHandle nicHandle = new NicHandle("DW", 0, "RIPE");
        assertTrue(subject.claimSpecified(nicHandle));
        assertRows(1);

        assertFalse(subject.claimSpecified(nicHandle));
        assertRows(1);
    }

    @Test
    public void claimNextAvailableIndex_empty_database() {
        for (int i = 1; i < 10; i++) {
            assertThat(subject.claimNextAvailableIndex("DW", "RIPE").getIndex(), is(i));
            assertRows(1);
        }
    }

    @Test
    public void claimNextAvailableIndex_empty_database_no_suffix() {
        for (int i = 1; i <= 10; i++) {
            assertThat(subject.claimNextAvailableIndex("DW", null).getIndex(), is(i));
            assertRows(1);
        }
    }

    @Test
    public void claimNextAvailableIndex_different_space() {
        assertThat(subject.claimNextAvailableIndex("DW", "RIPE").getIndex(), is(1));
        assertRows(1);

        assertThat(subject.claimNextAvailableIndex("AB", "RIPE").getIndex(), is(1));
        assertRows(2);
    }

    @Test
    public void claimNextAvailableIndex_different_suffix() {
        assertThat(subject.claimNextAvailableIndex("DW", "ABC").getIndex(), is(1));
        assertRows(1);

        assertThat(subject.claimNextAvailableIndex("DW", "DEF").getIndex(), is(1));
        assertRows(2);
    }

    @Test
    public void claimNextAvailableIndex_closing_gap() {
        subject.createRange("DW", null, 0, 10);
        subject.createRange("DW", null, 12, 20);

        assertThat(subject.claimNextAvailableIndex("DW", "").getIndex(), is(11));
        assertRows(1);

        assertThat(subject.claimNextAvailableIndex("DW", "").getIndex(), is(21));
        assertRows(1);
    }

    @Test
    public void claimNextAvailableIndex_closing_gap_beginning() {
        subject.createRange("DW", null, 2, 10);

        assertThat(subject.claimNextAvailableIndex("DW", "").getIndex(), is(1));
        assertRows(1);

        assertThat(subject.claimNextAvailableIndex("DW", "").getIndex(), is(11));
        assertRows(1);
    }

    @Test
    public void claimNextAvailableIndex_closing_all_gaps() {
        subject.createRange("DW", null, 1, 3);
        subject.createRange("DW", null, 4, 5);
        subject.createRange("DW", null, 6, 10);
        subject.createRange("DW", null, 12, 13);
        subject.createRange("DW", null, 14, 17);
        subject.createRange("DW", null, 20, 21);

        assertThat(subject.claimNextAvailableIndex("DW", "").getIndex(), is(11));
        assertRows(2);

        assertThat(subject.claimNextAvailableIndex("DW", "").getIndex(), is(18));
        assertRows(2);

        assertThat(subject.claimNextAvailableIndex("DW", "").getIndex(), is(19));
        assertRows(1);

        assertThat(subject.claimNextAvailableIndex("DW", "").getIndex(), is(22));
        assertRows(1);
    }


    private void assertRows(final int expectedRows) {
        final List<Map<String, Object>> list = whoisTemplate.queryForList("select * from nic_hdl");
        assertThat(list, hasSize(expectedRows));

        for (final Map<String, Object> objectMap : list) {
            for (final Map.Entry<String, Object> entry : objectMap.entrySet()) {
                assertNotNull(entry.getKey(), entry.getValue());
            }

            final String source = objectMap.get("source").toString();
            if (source.length() > 0) {
                assertThat(source, startsWith("-"));
            }
        }
    }
}
