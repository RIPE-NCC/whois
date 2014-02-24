package net.ripe.db.whois.api.rest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RestServiceHelperTest {
    @Mock private HttpServletRequest request;

    @Test
    public void getRequestUrl_url_only() {
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://test.net"));

        final String result = RestServiceHelper.getRequestURL(request);

        assertThat(result, is("http://test.net"));
    }

    @Test
    public void getRequestUrl_url_and_querystring() {
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://test.net"));
        when(request.getQueryString()).thenReturn("flags=list-versions");

        final String result = RestServiceHelper.getRequestURL(request);

        assertThat(result, is("http://test.net?flags=list-versions"));
    }
}
