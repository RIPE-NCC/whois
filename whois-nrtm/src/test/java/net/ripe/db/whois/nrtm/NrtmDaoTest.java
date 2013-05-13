package net.ripe.db.whois.nrtm;

import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.nrtm.NrtmDao.SerialRange;
import net.ripe.db.whois.nrtm.integration.AbstractNrtmIntegrationBase;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class NrtmDaoTest extends AbstractNrtmIntegrationBase {
    @Autowired NrtmDao subject;

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
        NrtmDao.SerialEntry entry = subject.getById(1);
        assertThat(entry.getRpslObject(), is(inetnum));
    }

    @Test
    public void getSerialCreateDelete() {
        final RpslObject object = databaseHelper.addObject("aut-num: AS1");
        databaseHelper.removeObject(object.getObjectId(), "aut-num: AS1");
        databaseHelper.addObject("aut-num: AS1");

        subject.getById(1);
        subject.getById(2);
        subject.getById(3);
    }
}
