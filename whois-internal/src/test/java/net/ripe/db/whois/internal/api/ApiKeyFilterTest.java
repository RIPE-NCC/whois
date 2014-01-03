package net.ripe.db.whois.internal.api;

import com.google.common.collect.Lists;
import net.ripe.db.whois.internal.api.abusec.JdbcApiKeyDao;
import net.ripe.db.whois.internal.api.acl.ApiKeyFilter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ApiKeyFilterTest {
    StringWriter writer;
    @Mock HttpServletRequest request;
    @Mock HttpServletResponse response;
    @Mock FilterChain filterChain;
    @Mock JdbcApiKeyDao apiKeyDao;

    @InjectMocks
    ApiKeyFilter subject;

    @Before
    public void setUp() throws Exception {
        when(apiKeyDao.getUrisForApiKey("KEY")).thenReturn(Lists.newArrayList("/testuri"));
        when(request.getRequestURI()).thenReturn("/testuri");

        writer = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(writer));
    }

    @Test
    public void init() throws Exception {
        subject.init(null);
    }

    @Test
    public void destroy() {
        subject.destroy();
    }

    @Test
    public void key_omitted() throws Exception {
        subject.doFilter(request, response, filterChain);

        assertThat(writer.toString(), is("No apiKey parameter specified"));
        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verifyZeroInteractions(filterChain);
    }

    @Test
    public void key_invalid() throws Exception {
        when(request.getParameter("apiKey")).thenReturn("OOPS");
        subject.doFilter(request, response, filterChain);

        assertThat(writer.toString(), is("Invalid apiKey"));
        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verifyZeroInteractions(filterChain);
    }

    @Test
    public void key_valid() throws Exception {
        when(request.getParameter("apiKey")).thenReturn("KEY");
        subject.doFilter(request, response, filterChain);

        assertThat(writer.toString(), is(""));
        verify(filterChain).doFilter(request, response);
    }
}
