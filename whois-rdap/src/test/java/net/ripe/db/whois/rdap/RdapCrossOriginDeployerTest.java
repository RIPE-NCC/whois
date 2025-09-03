package net.ripe.db.whois.rdap;

import jakarta.ws.rs.HttpMethod;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.PreEncodedHttpField;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RdapCrossOriginDeployerTest {

    @Mock
    private Request request;
    @Mock
    private Response response;
    @Mock
    private Callback callback;

    @Mock
    private RdapController rdapController;
    @Mock
    private RdapExceptionMapper rdapExceptionMapper;
    @Mock
    private RdapRequestTypeConverter rdapRequestTypeConverter;

    private RdapCrossOriginDeployer subject;

    @BeforeEach
    public void setup() {
        this.subject = new RdapCrossOriginDeployer(rdapController, rdapExceptionMapper, rdapRequestTypeConverter);
    }

    @Test
    public void no_origin_in_get_request() throws Exception {
        when(request.getHeaders()).thenReturn(HttpFields.EMPTY);

        final HttpFields.Mutable headers = mock(HttpFields.Mutable.class);
        when(response.getHeaders()).thenReturn(headers);

        when(request.getMethod()).thenReturn(HttpMethod.GET);

        subject.handle(request, response, callback);

        verify(headers).put(HttpHeader.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        verify(headers).ensureField(new PreEncodedHttpField(HttpHeader.VARY, HttpHeader.ORIGIN.asString()));

        verifyNoMoreInteractions(request);
        verifyNoMoreInteractions(headers);
    }
}
