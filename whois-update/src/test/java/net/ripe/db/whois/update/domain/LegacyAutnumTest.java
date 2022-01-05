package net.ripe.db.whois.update.domain;

import com.google.common.collect.Lists;
import net.ripe.db.whois.update.dao.LegacyAutnumDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LegacyAutnumTest {

    @Mock LegacyAutnumDao legacyAutnumDao;
    @InjectMocks LegacyAutnum subject;

    @BeforeEach
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
