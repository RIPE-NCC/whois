package net.ripe.db.whois.api.rest;

import net.ripe.db.whois.api.rest.mapper.WhoisObjectServerMapper;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.update.handler.UpdateRequestHandler;
import net.ripe.db.whois.update.log.LoggerContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

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
        final MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/ripe/mntner/TEST-MNT");
        request.addHeader("name", "value");

        InternalUpdatePerformer.logHttpHeaders(loggerContextMock, request);

        verify(loggerContextMock).log(new Message(Messages.Type.INFO, "Header: name=value"));
        verifyNoMoreInteractions(loggerContextMock);
    }

    @Test
    public void log_http_uri() {
        final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/ripe/mntner/RIPE-DBM-MNT");
        request.setQueryString("password=secret&unfiltered");

        InternalUpdatePerformer.logHttpUri(loggerContextMock, request);

        verify(loggerContextMock).log(new Message(Messages.Type.INFO, "/ripe/mntner/RIPE-DBM-MNT?password=secret&unfiltered"));
        verifyNoMoreInteractions(loggerContextMock);
    }
}
