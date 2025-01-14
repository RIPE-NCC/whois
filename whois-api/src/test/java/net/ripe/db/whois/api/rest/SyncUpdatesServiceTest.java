package net.ripe.db.whois.api.rest;

import com.google.common.collect.Iterators;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.Response;
import net.ripe.db.whois.api.UpdatesParser;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.domain.IpRanges;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.common.sso.AuthServiceClientException;
import net.ripe.db.whois.common.sso.SsoTokenTranslator;
import net.ripe.db.whois.common.sso.UserSession;
import net.ripe.db.whois.update.domain.Keyword;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateRequest;
import net.ripe.db.whois.update.domain.UpdateResponse;
import net.ripe.db.whois.update.domain.UpdateStatus;
import net.ripe.db.whois.update.handler.UpdateRequestHandler;
import net.ripe.db.whois.update.log.LoggerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.Collections;

import static jakarta.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SyncUpdatesServiceTest {

    @Mock HttpServletRequest request;
    @Mock HttpServletResponse response;
    @Mock DateTimeProvider dateTimeProvider;
    @Mock UpdateRequestHandler messageHandler;
    @Mock IpRanges ipRanges;
    @Mock UpdatesParser updatesParser;
    @Mock LoggerContext loggerContext;
    @Mock SourceContext sourceContext;
    @Mock SsoTokenTranslator ssoTokenTranslator;

    @InjectMocks SyncUpdatesService subject;

    @BeforeEach
    public void setUp() {
        lenient().when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        lenient().when(request.getHeaderNames()).thenReturn(Iterators.asEnumeration(Collections.emptyIterator()));
        lenient().when(sourceContext.getCurrentSource()).thenReturn(Source.master("TEST"));
    }

    @Test
    public void handle_no_parameters() {
        when(messageHandler.handle(any(UpdateRequest.class), any(UpdateContext.class))).thenReturn(new UpdateResponse(UpdateStatus.SUCCESS, "OK"));

        final String data = null;
        final String help = null;
        final String nnew = null;
        final String diff = null;
        final String redirect = null;
        final String source = "test";
        final String contentType = "UTF-8";
        final String ssoToken = null;

        final Response response = subject.doGet(request, source, data, help, nnew, diff, redirect, contentType, null, ssoToken);

        assertThat(response.getStatus(), is(HttpURLConnection.HTTP_OK));
        assertThat(response.getEntity().toString(), is("OK"));
    }

    @Test
    public void handle_only_new_parameter() {
        final String data = null;
        final String help = null;
        final String nnew = "YES";
        final String diff = null;
        final String redirect = null;
        final String source = "test";
        final String contentType = "UTF-8";
        final String ssoToken = null;

        final Response response = subject.doGet(request, source, data, help, nnew, diff, redirect, contentType, null, ssoToken);

        assertThat(response.getStatus(), is(HttpURLConnection.HTTP_BAD_REQUEST));
        assertThat(response.getEntity().toString(), is("DATA parameter is missing"));
    }

    @Test
    public void handle_only_diff_parameter() {
        final String data = null;
        final String help = null;
        final String nnew = null;
        final String diff = "YES";
        final String redirect = null;
        final String source = "test";
        final String contentType = "UTF-8";
        final String ssoToken = null;

        final Response response = subject.doGet(request, source, data, help, nnew, diff, redirect, contentType, null, ssoToken);

        assertThat(response.getStatus(), is(HttpURLConnection.HTTP_BAD_REQUEST));
        assertThat(response.getEntity().toString(), is("the DIFF method is not actually supported by the Syncupdates interface"));
    }

    @Test
    public void handle_only_data_parameter() {
        when(messageHandler.handle(any(UpdateRequest.class), any(UpdateContext.class))).thenReturn(new UpdateResponse(UpdateStatus.SUCCESS, "OK"));

        final String data = "person";
        final String help = null;
        final String nnew = null;
        final String diff = null;
        final String redirect = null;
        final String source = "test";
        final String contentType = "UTF-8";
        final String ssoToken = null;

        final Response response = subject.doGet(request, source, data, help, nnew, diff, redirect, contentType, null, ssoToken);

        assertThat(response.getStatus(), is(HttpURLConnection.HTTP_OK));
        assertThat(response.getEntity().toString(), is("OK"));
    }

    @Test
    public void handle_unauthorised() {
        final String data = "person";
        final String help = null;
        final String nnew = null;
        final String diff = null;
        final String redirect = null;
        final String source = "test";
        final String contentType = "UTF-8";
        final String ssoToken = null;

        when(messageHandler.handle(any(UpdateRequest.class), any(UpdateContext.class))).thenReturn(new UpdateResponse(UpdateStatus.FAILED_AUTHENTICATION, "FAILED"));
        final Response response = subject.doGet(request, source, data, help, nnew, diff, redirect, contentType, null, ssoToken);

        assertThat(response.getStatus(), is(HttpURLConnection.HTTP_UNAUTHORIZED));
        assertThat(response.getEntity().toString(), is("FAILED"));
    }

    @Test
    public void handle_diff_and_data_parameters() {
        final String data = "lkajkafa";
        final String help = null;
        final String nnew = null;
        final String diff = "YES";
        final String redirect = null;
        final String source = "test";
        final String contentType = "UTF-8";
        final String ssoToken = null;

        final Response response = subject.doGet(request, source, data, help, nnew, diff, redirect, contentType, null, ssoToken);

        assertThat(response.getStatus(), is(HttpURLConnection.HTTP_BAD_REQUEST));
        assertThat(response.getEntity().toString(), is("the DIFF method is not actually supported by the Syncupdates interface"));
    }

    @Test
    public void throw_illegal_argument_exception() {
        try {
            final String data = "person";
            final String help = null;
            final String nnew = null;
            final String diff = null;
            final String redirect = null;
            final String source = "test";
            final String contentType = "UTF-8";
            final String ssoToken = null;

            doThrow(new IllegalArgumentException("some message")).
                    when(messageHandler).handle(any(UpdateRequest.class), any(UpdateContext.class));

            subject.doGet(request, source, data, help, nnew, diff, redirect, contentType, null,  ssoToken);
            fail();
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), is("some message"));
        }
    }

    @Test
    public void throw_runtime_exception() {
        try {
            final String data = "person";
            final String help = null;
            final String nnew = null;
            final String diff = null;
            final String redirect = null;
            final String source = "test";
            final String contentType = "UTF-8";
            final String ssoToken = null;

            doThrow(new RuntimeException("some message", new IllegalStateException("some message"))).
                    when(messageHandler).handle(any(UpdateRequest.class), any(UpdateContext.class));

            subject.doGet(request, source, data, help, nnew, diff, redirect, contentType, null, ssoToken);
            fail();
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), is("some message"));
        }
    }

    @Test
    public void handle_invalid_encoding() {
        when(messageHandler.handle(any(UpdateRequest.class), any(UpdateContext.class))).thenReturn(new UpdateResponse(UpdateStatus.SUCCESS, "OK"));

        final String data = "person";
        final String help = null;
        final String nnew = null;
        final String diff = null;
        final String redirect = null;
        final String source = "test";
        final String contentType = "text/plain; charset=RGRFE";
        final String ssoToken = null;

        final Response response = subject.doGet(request, source, data, help, nnew, diff, redirect, contentType, null, ssoToken);

        assertThat(response.getStatus(), is(HttpURLConnection.HTTP_OK));
        assertThat(response.getEntity().toString(), is("OK"));
    }

    @Test
    public void handle_invalid_content_type() {
        when(messageHandler.handle(any(UpdateRequest.class), any(UpdateContext.class))).thenReturn(new UpdateResponse(UpdateStatus.SUCCESS, "OK"));

        final String data = "person";
        final String help = null;
        final String nnew = null;
        final String diff = null;
        final String redirect = null;
        final String source = "test";
        final String contentType = "invalid";
        final String ssoToken = null;

        final Response response = subject.doGet(request, source, data, help, nnew, diff, redirect, contentType, null, ssoToken);

        assertThat(response.getStatus(), is(HttpURLConnection.HTTP_OK));
        assertThat(response.getEntity().toString(), is("OK"));
    }

    @Test
    public void handle_redirect_allowed() {
        when(messageHandler.handle(any(UpdateRequest.class), any(UpdateContext.class))).thenReturn(new UpdateResponse(UpdateStatus.SUCCESS, "OK"));

        final String data = "person";
        final String help = null;
        final String nnew = null;
        final String diff = null;
        final String redirect = "YES";
        final String source = "test";
        final String contentType = "UTF-8";
        final String ssoToken = null;

        final Response response = subject.doGet(request, source, data, help, nnew, diff, redirect, contentType, null, ssoToken);

        assertThat(response.getStatus(), is(HttpURLConnection.HTTP_OK));
        assertThat(response.getEntity().toString(), is("OK"));
    }

    @Test
    public void handle_redirect_is_ignored() {
        when(messageHandler.handle(any(UpdateRequest.class), any(UpdateContext.class))).thenReturn(new UpdateResponse(UpdateStatus.SUCCESS, "OK"));

        final String data = "person";
        final String help = null;
        final String nnew = null;
        final String diff = null;
        final String redirect = "YES";
        final String source = "test";
        final String contentType = "UTF-8";
        final String ssoToken = null;

        final Response response = subject.doGet(request, source, data, help, nnew, diff, redirect, contentType, null, ssoToken);

        assertThat(response.getStatus(), is(HttpURLConnection.HTTP_OK));
        assertThat(response.getEntity().toString(), is("OK"));
    }

    @Test
    public void handle_multipart_post() {
        when(messageHandler.handle(any(UpdateRequest.class), any(UpdateContext.class))).thenReturn(new UpdateResponse(UpdateStatus.SUCCESS, "OK"));
        when(ssoTokenTranslator.translateSsoToken("valid-token")).thenReturn(new UserSession("offereduuid","test@ripe.net", "Test User", true, "2033-01-30T16:38:27.369+11:00"));

        final String data = "person:   Ed Shryane\n" +
                "address:  Ripe NCC Singel 258\n" +
                "phone:    +31-61238-2827\n" +
                "nic-hdl:  ES222-RIPE\n" +
                "mnt-by:   TEST-DBM-MNT\n" +
                "source:   test\n" +
                "remarks:  something\n" +
                "\n" +
                "password: password";
        final String help = null;
        final String nnew = null;
        final String diff = null;
        final String redirect = null;
        final String source = "test";
        final String ssoToken = "valid-token";
        final String contentType = "charset=\"latin1\"";

        subject.doMultipartPost(request, source, data, help, nnew, diff, redirect, null, contentType, ssoToken);

        verify(messageHandler).handle(
                argThat(updateRequest -> {
                        assertThat(updateRequest.getKeyword(), is(Keyword.NONE));
                        return true;
                    }),
                argThat(updateContext -> {
                        assertThat(updateContext.getUserSession().getUsername(), is("test@ripe.net"));
                        return true;
                    }));
    }

    @Test
    public void handle_multipart_post_invalid_sso_token() {
        when(messageHandler.handle(any(UpdateRequest.class), any(UpdateContext.class))).thenReturn(new UpdateResponse(UpdateStatus.SUCCESS, "OK"));
        when(ssoTokenTranslator.translateSsoToken("invalid-token")).thenThrow(new AuthServiceClientException(UNAUTHORIZED.getStatusCode(),"Unknown RIPE NCC Access token: invalid-token"));

        final String data = "person:   Ed Shryane\n" +
                "address:  Ripe NCC Singel 258\n" +
                "phone:    +31-61238-2827\n" +
                "nic-hdl:  ES222-RIPE\n" +
                "mnt-by:   TEST-DBM-MNT\n" +
                "source:   test\n" +
                "remarks:  something\n" +
                "\n" +
                "password: password";
        final String help = null;
        final String nnew = null;
        final String diff = null;
        final String redirect = null;
        final String source = "test";
        final String ssoToken = "invalid-token";
        final String contentType = "charset=\"latin1\"";

        subject.doMultipartPost(request, source, data, help, nnew, diff, redirect, null, contentType, ssoToken);

        verify(messageHandler).handle(
                argThat(updateRequest -> {
                    assertThat(updateRequest.getKeyword(), is(Keyword.NONE));
                    return true;
                }),
                argThat(updateContext -> {
                    assertThat(updateContext.getUserSession(), is(nullValue()));
                    return true;
                }));
    }

    @Test
    public void log_callback() throws IOException {
        final String message = "message";
        final OutputStream outputStream = mock(OutputStream.class);

        final SyncUpdatesService.SyncUpdateLogCallback logCallback = subject.new SyncUpdateLogCallback(message);
        logCallback.log(outputStream);

        verify(outputStream).write(message.getBytes());
    }

    @Test
    public void request_to_string() {
        SyncUpdatesService.Request request = new SyncUpdatesService.Request.RequestBuilder()
                .setData("person: name\naddress: Singel 258")
                .setNew("no")
                .setRemoteAddress("127.0.0.1")
                .setSource("RIPE")
                .build();

        assertThat(request.toString(), containsString("127.0.0.1"));
        assertThat(request.toString(), containsString("DATA=\n\nperson: name\naddress: Singel 258"));
        assertThat(request.toString(), containsString("NEW=no"));
    }
}
