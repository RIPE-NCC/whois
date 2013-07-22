package net.ripe.db.whois.api.whois.rdap;

import net.ripe.db.whois.common.domain.CIString;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DelegatedStatsServiceTest {
    @Mock DelegatedStatsDao statsDao;

    private DelegatedStatsService subject;

    @Before
    public void setup() {
        subject = new DelegatedStatsService(statsDao, "afrinic.net", "apnic.net", "arin.net", "lacnic.net");
    }

    @Test
    public void getUri_value_found() {
        when(statsDao.findSourceForResource("AS3546")).thenReturn(CIString.ciString("afrinic"));

        assertThat(subject.getUriForRedirect("AS3546"), is(not(nullValue())));
        assertThat(subject.getUriForRedirect("AS3546").toString(), is("afrinic.net"));
    }

    @Test
    public void getUri_value_not_found() {
        when(statsDao.findSourceForResource("AS3546")).thenReturn(null);

        assertThat(subject.getUriForRedirect("AS3546"), is(nullValue()));
    }
}
