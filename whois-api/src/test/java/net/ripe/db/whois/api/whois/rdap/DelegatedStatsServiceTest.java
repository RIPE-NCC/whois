package net.ripe.db.whois.api.whois.rdap;

import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.grs.AuthoritativeResource;
import net.ripe.db.whois.common.grs.AuthoritativeResourceData;
import net.ripe.db.whois.common.rpsl.ObjectType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DelegatedStatsServiceTest {
    @Mock AuthoritativeResourceData resourceData;
    @Mock AuthoritativeResource authoritativeResource;

    private DelegatedStatsService subject;

    @Before
    public void setup() {
        subject = new DelegatedStatsService(resourceData, "afrinic.net", "apnic.net", "arin.net", "lacnic.net");
    }

    @Test
    public void getUri_value_found() {
        when(resourceData.getAuthoritativeResource(any(CIString.class))).thenReturn(authoritativeResource);
        when(authoritativeResource.isMaintainedByRir(ObjectType.AUT_NUM, CIString.ciString("AS3546"))).thenReturn(true);

        assertThat(subject.getUriForRedirect("AS3546"), is(not(nullValue())));
        assertThat(subject.getUriForRedirect("AS3546").toString(), is("apnic.net"));
    }

    @Test
    public void getUri_value_not_found() {
        when(resourceData.getAuthoritativeResource(any(CIString.class))).thenReturn(authoritativeResource);
        when(authoritativeResource.isMaintainedByRir(ObjectType.AUT_NUM, CIString.ciString("AS3546"))).thenReturn(false);

        assertThat(subject.getUriForRedirect("AS3546"), is(nullValue()));
    }
}
