package net.ripe.db.whois.api.whois.rdap;

import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.grs.AuthoritativeResource;
import net.ripe.db.whois.common.grs.AuthoritativeResourceData;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.query.query.Query;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.util.StringValueResolver;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DelegatedStatsServiceTest {
    @Mock AuthoritativeResourceData authoritativeResourceData;
    @Mock AuthoritativeResource authoritativeResourceOne;
    @Mock AuthoritativeResource authoritativeResourceTwo;
    @Mock StringValueResolver valueResolver;

    private DelegatedStatsService subject;

    @Before
    public void setup() {
        when(authoritativeResourceOne.isMaintainedInRirSpace(any(ObjectType.class), any(CIString.class))).thenReturn(Boolean.FALSE);
        when(authoritativeResourceTwo.isMaintainedInRirSpace(any(ObjectType.class), any(CIString.class))).thenReturn(Boolean.FALSE);
        when(authoritativeResourceData.getAuthoritativeResource(CIString.ciString("one"))).thenReturn(authoritativeResourceOne);
        when(authoritativeResourceData.getAuthoritativeResource(CIString.ciString("two"))).thenReturn(authoritativeResourceTwo);

        when(valueResolver.resolveStringValue("${rdap.redirect.one:}")).thenReturn("one.net");
        when(valueResolver.resolveStringValue("${rdap.redirect.two:}")).thenReturn("two.net");

        subject = new DelegatedStatsService("one,two", authoritativeResourceData);
        subject.setEmbeddedValueResolver(valueResolver);
        subject.init();
    }

    @Test
    public void getUri_value_found() {
        when(authoritativeResourceOne.isMaintainedInRirSpace(ObjectType.AUT_NUM, CIString.ciString("AS3546"))).thenReturn(true);

        assertThat(subject.getUriForRedirect("/rdap/autnum/3546", Query.parse("-T aut-num AS3546")).toString(), is("one.net/autnum/3546"));
    }

    @Test
    public void getUri_value_not_found() {
        try {
            subject.getUriForRedirect("/rdap/autnum/3546", Query.parse("-T aut-num AS3546"));
            fail();
        } catch (WebApplicationException expected) {
            assertThat(expected.getResponse().getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
        }
    }
}
