package net.ripe.db.whois.api.rdap;

import com.google.common.net.HttpHeaders;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.HttpMethod;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RdapCrossOriginFilterTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    private RdapCrossOriginFilter subject;


    @BeforeEach
    public void setup() {
        this.subject = new RdapCrossOriginFilter();
    }

    @Test
    public void no_origin_in_get_request() throws Exception {
        when(request.getHeader(HttpHeaders.ORIGIN)).thenReturn(null);
        when(request.getMethod()).thenReturn(HttpMethod.GET);

        subject.doFilter(request, response, filterChain);

        verify(response).setHeader(CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "*");
        verifyNoMoreInteractions(response);
    }

}
