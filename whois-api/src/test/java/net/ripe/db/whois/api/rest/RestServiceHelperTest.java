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
    public void getRequestUrl() {
        assertThat(getRequestURL("http://test.net", null), is("http://test.net"));
        assertThat(getRequestURL("http://test.net", "flags=list-versions"), is("http://test.net?flags=list-versions"));
        assertThat(getRequestURL("http://test.net", "password=abc"), is("http://test.net"));
        assertThat(getRequestURL("http://test.net", "password=abc&password=xyz"), is("http://test.net"));
        assertThat(getRequestURL("http://test.net", "param=one&password=xyz"), is("http://test.net?param=one"));
        assertThat(getRequestURL("http://test.net", "password=xyz&param=one"), is("http://test.net?param=one"));
        assertThat(getRequestURL("http://test.net", "password=abc&param=one&password=xyz"), is("http://test.net?param=one"));
        assertThat(getRequestURL("http://test.net", "param=one&password=abc&param=two"), is("http://test.net?param=one&param=two"));
        assertThat(getRequestURL("http://test.net", "param=one&password=abc&param=two&password=xyz"), is("http://test.net?param=one&param=two"));
        assertThat(getRequestURL("http://test.net", "password=abc&param=one&password=xyz&param=two"), is("http://test.net?param=one&param=two"));
        assertThat(getRequestURL("http://test.net", "password=aaa&password=bbb&param=one&password=ccc&param=two"), is("http://test.net?param=one&param=two"));
    }

    // helper methods

    private String getRequestURL(final String url, final String queryString) {
        when(request.getRequestURL()).thenReturn(new StringBuffer(url));
        when(request.getQueryString()).thenReturn(queryString);

        return RestServiceHelper.getRequestURL(request);
    }
}
