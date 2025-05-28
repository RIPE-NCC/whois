package net.ripe.db.whois.api.rdap;

import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.grs.AuthoritativeResource;
import net.ripe.db.whois.common.grs.AuthoritativeResourceData;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.query.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.StringValueResolver;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RdapDelegatedStatsServiceTest {
    @Mock AuthoritativeResourceData authoritativeResourceData;
    @Mock AuthoritativeResource authoritativeResourceOne;
    @Mock AuthoritativeResource authoritativeResourceTwo;
    @Mock StringValueResolver valueResolver;

    private DelegatedStatsService subject;

    @BeforeEach
    public void setup() {
        when(authoritativeResourceOne.isMaintainedInRirSpace(any(ObjectType.class), any(CIString.class))).thenReturn(Boolean.FALSE);
        lenient().when(authoritativeResourceTwo.isMaintainedInRirSpace(any(ObjectType.class), any(CIString.class))).thenReturn(Boolean.FALSE);
        when(authoritativeResourceData.getAuthoritativeResource(CIString.ciString("one"))).thenReturn(authoritativeResourceOne);
        lenient().when(authoritativeResourceData.getAuthoritativeResource(CIString.ciString("two"))).thenReturn(authoritativeResourceTwo);

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
