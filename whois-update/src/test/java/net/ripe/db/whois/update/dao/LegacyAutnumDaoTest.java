package net.ripe.db.whois.update.dao;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.domain.CIString;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class LegacyAutnumDaoTest extends AbstractUpdateDaoTest  {
    @Autowired LegacyAutnumDao subject;

    @Test
    public void storeAndRead() {
        subject.store(Lists.newArrayList("325", "675", "1058"));

        final List<CIString> list = subject.readLegacyAutnums();
        assertThat(Iterables.elementsEqual(list, CIString.ciSet("AS325", "AS675", "AS1058")), is(Boolean.TRUE));
    }
}
