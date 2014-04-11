package net.ripe.db.whois.update.domain;

import com.google.common.collect.Lists;
import net.ripe.db.whois.update.dao.LegacyAutnumDao;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LegacyAutnumTest {

    @Mock LegacyAutnumDao legacyAutnumDao;
    @InjectMocks LegacyAutnum subject;

    @Before
    public void setup() {
        when(legacyAutnumDao.load()).thenReturn(Lists.newArrayList(ciString("AS102")));
        subject.init();
    }

    @Test
    public void contains() {
        assertThat(subject.contains(ciString("AS100")), is(false));
        assertThat(subject.contains(ciString("AS102")), is(true));
    }
}
