package net.ripe.db.whois.api.rest;

import com.google.common.collect.Lists;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectServerMapper;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.sso.CrowdClientException;
import net.ripe.db.whois.common.sso.SsoTokenTranslator;
import net.ripe.db.whois.common.sso.UserSession;
import net.ripe.db.whois.update.domain.Origin;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.handler.UpdateRequestHandler;
import net.ripe.db.whois.update.log.LoggerContext;
import org.apache.log4j.helpers.NullEnumeration;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Hashtable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InternalUpdatePerformerTest {

    @Mock private UpdateRequestHandler updateRequestHandlerMock;
    @Mock private DateTimeProvider dateTimeProviderMock;
    @Mock private WhoisObjectServerMapper whoisObjectMapperMock;
    @Mock private LoggerContext loggerContextMock;
    @Mock private SsoTokenTranslator ssoTokenTranslatorMock;
    @Mock private HttpServletRequest requestMock;
    @Mock private UpdateContext updateContextMock;
    @InjectMocks private InternalUpdatePerformer subject;

    @Test
    public void logHttpHeaders_header() {
        final MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/ripe/mntner/TEST-MNT");
        request.addHeader("name", "value");

        subject.logHttpHeaders(loggerContextMock, request);

        verify(loggerContextMock).log(new Message(Messages.Type.INFO, "Header: name=value"));
        verifyNoMoreInteractions(loggerContextMock);
    }

    @Test
    public void logHttpHeaders_headers() {
        final Hashtable hashTable = new Hashtable();
        hashTable.put("header1", "value1");
        hashTable.put("header2", "value2");
        hashTable.put("header3", "value3");

        when(requestMock.getHeaderNames()).thenReturn(hashTable.keys());

        when(requestMock.getHeaders("header1")).thenReturn(Collections.enumeration(Collections.singleton("value1")));
        when(requestMock.getHeaders("header2")).thenReturn(Collections.enumeration(Collections.singleton("value2")));
        when(requestMock.getHeaders("header3")).thenReturn(Collections.enumeration(Collections.singleton("value3")));

        subject.logHttpHeaders(loggerContextMock, requestMock);

        verify(loggerContextMock).log(new Message(Messages.Type.INFO, String.format("Header: header1=value1")));
        verify(loggerContextMock).log(new Message(Messages.Type.INFO, String.format("Header: header2=value2")));
        verify(loggerContextMock).log(new Message(Messages.Type.INFO, String.format("Header: header3=value3")));
    }

    @Test
    public void logHttpHeaders_no_headers() {
        when(requestMock.getHeaderNames()).thenReturn(NullEnumeration.getInstance());

        subject.logHttpHeaders(loggerContextMock, requestMock);

        verifyZeroInteractions(loggerContextMock);
    }

    @Test
    public void logHttpUri_no_querystring() {
        when(requestMock.getQueryString()).thenReturn("");
        when(requestMock.getRequestURI()).thenReturn("www.test.net");

        subject.logHttpUri(loggerContextMock, requestMock);

        verify(loggerContextMock).log(new Message(Messages.Type.INFO, "www.test.net"));
    }

    @Test
    public void log_http_uri() {
        final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/ripe/mntner/RIPE-DBM-MNT");
        request.setQueryString("password=secret&unfiltered");

        InternalUpdatePerformer.logHttpUri(loggerContextMock, request);

        verify(loggerContextMock).log(new Message(Messages.Type.INFO, "/ripe/mntner/RIPE-DBM-MNT?password=secret&unfiltered"));
        verifyNoMoreInteractions(loggerContextMock);
    }

    @Test
    public void createContent_no_passwords() {
        final RpslObject object = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "mnt-by: TEST-MNT\n" +
                "source: TEST");

        final String content = subject.createContent(object, Collections.EMPTY_LIST, "no reason", "override");

        assertThat(content, is("" +
                "aut-num:        AS123\n" +
                "mnt-by:         TEST-MNT\n" +
                "source:         TEST\n" +
                "delete: no reason\n" +
                "\n" +
                "override: override\n\n"));
    }

    @Test
    public void createContent_no_deleteReason() {
        final RpslObject object = RpslObject.parse("" +
                "mntner: TEST-MNT\n" +
                "mnt-by: TEST-MNT\n" +
                "source: TEST");

        final String content = subject.createContent(object, Collections.singletonList("password"), null, "override");

        assertThat(content, is("" +
                "mntner:         TEST-MNT\n" +
                "mnt-by:         TEST-MNT\n" +
                "source:         TEST\n" +
                "password: password\n" +
                "override: override\n\n"));
    }

    @Test
    public void createContent_no_override() {
        final RpslObject object = RpslObject.parse("" +
                "role: Test Role\n" +
                "nic-hdl: TP-TEST\n" +
                "mnt-by: TEST-MNT\n" +
                "source: TEST");

        final String content = subject.createContent(object, Collections.singletonList("password"), "no reason", null);

        assertThat(content, is("" +
                "role:           Test Role\n" +
                "nic-hdl:        TP-TEST\n" +
                "mnt-by:         TEST-MNT\n" +
                "source:         TEST\n" +
                "delete: no reason\n" +
                "\n" +
                "password: password\n"));
    }

    @Test
    public void createContent_passwordsOnly() {
        final RpslObject object = RpslObject.parse("" +
                "person: Test Person\n" +
                "nic-hdl: TP1-TEST\n" +
                "mnt-by: TEST-MNT\n" +
                "source: TEST");

        final String content = subject.createContent(object, Lists.newArrayList("password1", "password2"), null, null);

        assertThat(content, is("" +
                "person:         Test Person\n" +
                "nic-hdl:        TP1-TEST\n" +
                "mnt-by:         TEST-MNT\n" +
                "source:         TEST\n" +
                "password: password1\n" +
                "password: password2\n"));
    }

    @Test
    public void createOrigin() {
        when(requestMock.getRemoteAddr()).thenReturn("127.0.0.1");
        when(dateTimeProviderMock.getCurrentDateTime()).thenReturn(new LocalDateTime(5556667777888l, DateTimeZone.UTC));

        final Origin origin = subject.createOrigin(requestMock);

        assertThat(origin.getFrom(), is("127.0.0.1"));
        assertThat(origin.getId(), is("127.0.0.1"));
        assertThat(origin.getName(), is("rest api"));
        assertThat(origin.getNotificationHeader(), containsString("" +
                "- From-Host: 127.0.0.1\n" +
                " - Date/Time: Mon Jan 31 06:49:37"));
        assertThat(origin.getResponseHeader(), containsString("" +
                "- From-Host: 127.0.0.1\n" +
                " - Date/Time: Mon Jan 31 06:49:37"));
    }

    @Test
    public void setSsoSessionToContext_no_sso_token() {
        subject.setSsoSessionToContext(updateContextMock, "");

        verifyZeroInteractions(ssoTokenTranslatorMock);
        verifyZeroInteractions(loggerContextMock);

        subject.setSsoSessionToContext(updateContextMock, null);

        verifyZeroInteractions(ssoTokenTranslatorMock);
        verifyZeroInteractions(loggerContextMock);
    }

    @Test
    public void setSsoSessionToContext_successful_sso_translation() {
        final UserSession userSession = new UserSession("test-user", true);
        when(ssoTokenTranslatorMock.translateSsoToken("test-token")).thenReturn(userSession);

        subject.setSsoSessionToContext(updateContextMock, "test-token");

        verify(ssoTokenTranslatorMock).translateSsoToken("test-token");
        verifyZeroInteractions(loggerContextMock);
        verify(updateContextMock).setUserSession(userSession);
    }

    @Test
    public void setSsoSessionToContext_exception_is_logged() {
        when(ssoTokenTranslatorMock.translateSsoToken("test-token")).thenThrow(new CrowdClientException("exception"));

        try {
            subject.setSsoSessionToContext(updateContextMock, "test-token");
        } catch (CrowdClientException e) {
            verify(ssoTokenTranslatorMock.translateSsoToken("test-token"));
            verify(loggerContextMock).log(new Message(Messages.Type.ERROR, "exception"));
            verify(updateContextMock).addGlobalMessage(RestMessages.ssoAuthIgnored());
        }
    }
}
