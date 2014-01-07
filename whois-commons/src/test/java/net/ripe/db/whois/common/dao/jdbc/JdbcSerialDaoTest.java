package net.ripe.db.whois.common.dao.jdbc;

import net.ripe.db.whois.common.domain.serials.SerialEntry;
import net.ripe.db.whois.common.domain.serials.SerialRange;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.support.AbstractDaoTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class JdbcSerialDaoTest extends AbstractDaoTest {
    @Autowired JdbcSerialDao subject;
    @Value("${whois.source}") protected String source;

    @Before
    public void setup() {
        sourceContext.setCurrent(Source.slave(source));
    }

    @After
    public void cleanup() {
        sourceContext.removeCurrentSource();
    }

    @Test
    public void getSerials() {
        databaseHelper.addObject("aut-num:AS4294967207");
        databaseHelper.addObject("person:Denis Walker\nnic-hdl:DW-RIPE");
        databaseHelper.addObject("mntner:DEV-MNT");

        SerialRange range = subject.getSerials();

        assertThat(range.getBegin(), is(1));
        assertThat(range.getEnd(), is(3));
    }

    @Test
    public void getSerialEntryById() {
        RpslObject inetnum = RpslObject.parse("mntner:DEV-MNT");
        databaseHelper.addObject(inetnum);
        SerialEntry entry = subject.getById(1);
        assertThat(entry.getRpslObject(), is(inetnum));
    }

    @Test
    public void getSerialCreateDelete() {
        final RpslObject object = databaseHelper.addObject("aut-num: AS1");
        databaseHelper.deleteObject(object);
        databaseHelper.addObject("aut-num: AS1");

        subject.getById(1);
        subject.getById(2);
        subject.getById(3);
    }
}
