package net.ripe.db.whois.api.rest;

import net.ripe.db.whois.api.rest.mapper.WhoisObjectServerMapper;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.update.handler.UpdateRequestHandler;
import net.ripe.db.whois.update.log.LoggerContext;
import org.apache.commons.collections.iterators.IteratorEnumeration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Enumeration;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InternalUpdatePerformerTest {

    @Mock
    private UpdateRequestHandler updateRequestHandlerMock;
    @Mock
    private DateTimeProvider dateTimeProviderMock;
    @Mock
    private WhoisObjectServerMapper whoisObjectMapperMock;
    @Mock
    private LoggerContext loggerContextMock;
    @InjectMocks
    private InternalUpdatePerformer subject;

    @Test
    public void log_http_headers() {
        HttpServletRequest requestMock = mock(HttpServletRequest.class);
        when(requestMock.getHeaderNames()).thenReturn(enumerate("name"));
        when(requestMock.getHeaders("name")).thenReturn(enumerate("value"));

        InternalUpdatePerformer.logHttpHeaders(loggerContextMock, requestMock);

        verify(loggerContextMock).log(new Message(Messages.Type.INFO, "Header: name=value"));
        verifyNoMoreInteractions(loggerContextMock);
    }

    @Test
    public void log_cookies() {
        HttpServletRequest requestMock = mock(HttpServletRequest.class);
        when(requestMock.getCookies()).thenReturn(new Cookie[]{new Cookie("name", "value")});

        InternalUpdatePerformer.logCookies(loggerContextMock, requestMock);

        verify(loggerContextMock).log(new Message(Messages.Type.INFO, "Cookie: name=value"));
        verifyNoMoreInteractions(loggerContextMock);
    }

    private Enumeration<String> enumerate(final String... values) {
        return new IteratorEnumeration(Arrays.asList(values).iterator());
    }
}
