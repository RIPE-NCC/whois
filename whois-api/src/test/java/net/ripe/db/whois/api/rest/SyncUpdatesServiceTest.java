package net.ripe.db.whois.api.rest;

import com.google.common.collect.Iterators;
import net.ripe.db.whois.api.UpdatesParser;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.domain.IpRanges;
import net.ripe.db.whois.common.ip.Interval;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.update.domain.Keyword;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateRequest;
import net.ripe.db.whois.update.domain.UpdateResponse;
import net.ripe.db.whois.update.domain.UpdateStatus;
import net.ripe.db.whois.update.handler.UpdateRequestHandler;
import net.ripe.db.whois.update.log.LoggerContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SyncUpdatesServiceTest {

    @Mock HttpServletRequest request;
    @Mock HttpServletResponse response;
    @Mock DateTimeProvider dateTimeProvider;
    @Mock UpdateRequestHandler messageHandler;
    @Mock IpRanges ipRanges;
    @Mock UpdatesParser updatesParser;
    @Mock LoggerContext loggerContext;
    @Mock SourceContext sourceContext;

    @InjectMocks SyncUpdatesService subject;

    @Before
    public void setUp() throws Exception {
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeaderNames()).thenReturn(Iterators.asEnumeration(Iterators.<String>emptyIterator()));
        when(messageHandler.handle(any(UpdateRequest.class), any(UpdateContext.class))).thenReturn(new UpdateResponse(UpdateStatus.SUCCESS, "OK"));
        when(sourceContext.getCurrentSource()).thenReturn(Source.master("TEST"));
    }

    @Test
    public void handle_no_parameters() throws Exception {
        final String data = null;
        final String help = null;
        final String nnew = null;
        final String diff = null;
        final String redirect = null;
        final String source = "test";
        final String contentType = "UTF-8";

        final Response response = subject.doGet(request, source, data, help, nnew, diff, redirect, contentType);

        assertThat(response.getStatus(), is(HttpURLConnection.HTTP_BAD_REQUEST));
        assertThat(response.getEntity().toString(), is("Invalid request"));
    }

    @Test
    public void handle_only_new_parameter() throws Exception {
        final String data = null;
        final String help = null;
        final String nnew = "YES";
        final String diff = null;
        final String redirect = null;
        final String source = "test";
        final String contentType = "UTF-8";

        final Response response = subject.doGet(request, source, data, help, nnew, diff, redirect, contentType);

        assertThat(response.getStatus(), is(HttpURLConnection.HTTP_BAD_REQUEST));
        assertThat(response.getEntity().toString(), is("DATA parameter is missing"));
    }

    @Test
    public void handle_only_diff_parameter() throws Exception {
        final String data = null;
        final String help = null;
        final String nnew = null;
        final String diff = "YES";
        final String redirect = null;
        final String source = "test";
        final String contentType = "UTF-8";

        final Response response = subject.doGet(request, source, data, help, nnew, diff, redirect, contentType);

        assertThat(response.getStatus(), is(HttpURLConnection.HTTP_BAD_REQUEST));
        assertThat(response.getEntity().toString(), is("the DIFF method is not actually supported by the Syncupdates interface"));
    }

    @Test
    public void handle_only_data_parameter() throws Exception {
        final String data = "person";
        final String help = null;
        final String nnew = null;
        final String diff = null;
        final String redirect = null;
        final String source = "test";
        final String contentType = "UTF-8";

        final Response response = subject.doGet(request, source, data, help, nnew, diff, redirect, contentType);

        assertThat(response.getStatus(), is(HttpURLConnection.HTTP_OK));
        assertThat(response.getEntity().toString(), is("OK"));
    }

    @Test
    public void handle_unauthorised() throws Exception {
        final String data = "person";
        final String help = null;
        final String nnew = null;
        final String diff = null;
        final String redirect = null;
        final String source = "test";
        final String contentType = "UTF-8";

        when(messageHandler.handle(any(UpdateRequest.class), any(UpdateContext.class))).thenReturn(new UpdateResponse(UpdateStatus.FAILED_AUTHENTICATION, "FAILED"));
        final Response response = subject.doGet(request, source, data, help, nnew, diff, redirect, contentType);

        assertThat(response.getStatus(), is(HttpURLConnection.HTTP_UNAUTHORIZED));
        assertThat(response.getEntity().toString(), is("FAILED"));
    }

    @Test
    public void handle_diff_and_data_parameters() throws Exception {
        final String data = "lkajkafa";
        final String help = null;
        final String nnew = null;
        final String diff = "YES";
        final String redirect = null;
        final String source = "test";
        final String contentType = "UTF-8";

        final Response response = subject.doGet(request, source, data, help, nnew, diff, redirect, contentType);

        assertThat(response.getStatus(), is(HttpURLConnection.HTTP_BAD_REQUEST));
        assertThat(response.getEntity().toString(), is("the DIFF method is not actually supported by the Syncupdates interface"));
    }

    @Test
    public void throw_illegal_argument_exception() throws Exception {
        try {
            final String data = "person";
            final String help = null;
            final String nnew = null;
            final String diff = null;
            final String redirect = null;
            final String source = "test";
            final String contentType = "UTF-8";

            doThrow(new IllegalArgumentException("some message")).
                    when(messageHandler).handle(any(UpdateRequest.class), any(UpdateContext.class));

            subject.doGet(request, source, data, help, nnew, diff, redirect, contentType);
            fail();
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), is("some message"));
        }
    }

    @Test
    public void throw_runtime_exception() throws Exception {
        try {
            final String data = "person";
            final String help = null;
            final String nnew = null;
            final String diff = null;
            final String redirect = null;
            final String source = "test";
            final String contentType = "UTF-8";

            doThrow(new RuntimeException("some message", new IllegalStateException("some message"))).
                    when(messageHandler).handle(any(UpdateRequest.class), any(UpdateContext.class));

            subject.doGet(request, source, data, help, nnew, diff, redirect, contentType);
            fail();
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), is("some message"));
        }
    }

    @Test
    public void handle_invalid_encoding() throws Exception {
        final String data = "person";
        final String help = null;
        final String nnew = null;
        final String diff = null;
        final String redirect = null;
        final String source = "test";
        final String contentType = "text/plain; charset=RGRFE";

        final Response response = subject.doGet(request, source, data, help, nnew, diff, redirect, contentType);

        assertThat(response.getStatus(), is(HttpURLConnection.HTTP_OK));
        assertThat(response.getEntity().toString(), is("OK"));
    }

    @Test
    public void content_type_and_content_length_in_response() throws Exception {
        final String data = "person";
        final String help = null;
        final String nnew = null;
        final String diff = null;
        final String redirect = null;
        final String source = "test";
        final String contentType = "text/plain; charset=US-ASCII";

        final Response response = subject.doGet(request, source, data, help, nnew, diff, redirect, contentType);

        List<Object> contentLengthResponse = response.getMetadata().get(HttpHeaders.CONTENT_TYPE);
        assertThat(contentLengthResponse.size(), is(1));
        assertThat(contentLengthResponse.get(0).toString(), is("text/plain"));
        List<Object> contentTypeResponse = response.getMetadata().get(HttpHeaders.CONTENT_LENGTH);
        assertThat(contentTypeResponse.size(), is(1));
        assertThat(contentTypeResponse.get(0).toString(), is("2"));
    }

    @Test
    public void handle_invalid_content_type() throws Exception {
        final String data = "person";
        final String help = null;
        final String nnew = null;
        final String diff = null;
        final String redirect = null;
        final String source = "test";
        final String contentType = "invalid";

        final Response response = subject.doGet(request, source, data, help, nnew, diff, redirect, contentType);

        assertThat(response.getStatus(), is(HttpURLConnection.HTTP_OK));
        assertThat(response.getEntity().toString(), is("OK"));
        List<Object> contentLengthResponse = response.getMetadata().get(HttpHeaders.CONTENT_TYPE);
        assertThat(contentLengthResponse.size(), is(1));
        assertThat(contentLengthResponse.get(0).toString(), is("text/plain"));
    }

    @Test
    public void handle_redirect_allowed() throws Exception {
        final String data = "person";
        final String help = null;
        final String nnew = null;
        final String diff = null;
        final String redirect = "YES";
        final String source = "test";
        final String contentType = "UTF-8";

        when(ipRanges.isTrusted(any(Interval.class))).thenReturn(true);
        final Response response = subject.doGet(request, source, data, help, nnew, diff, redirect, contentType);

        assertThat(response.getStatus(), is(HttpURLConnection.HTTP_OK));
        assertThat(response.getEntity().toString(), is("OK"));
    }

    @Test
    public void handle_redirect_not_allowed() throws Exception {
        final String data = "person";
        final String help = null;
        final String nnew = null;
        final String diff = null;
        final String redirect = "YES";
        final String source = "test";
        final String contentType = "UTF-8";

        final Response response = subject.doGet(request, source, data, help, nnew, diff, redirect, contentType);

        assertThat(response.getStatus(), is(HttpURLConnection.HTTP_FORBIDDEN));
        assertThat(response.getEntity().toString(), is("Not allowed to disable notifications: 127.0.0.1"));
    }


    @Test
    public void handle_multipart_post() throws Exception {
        final String data = "person:   Ed Shryane\n" +
                "address:  Ripe NCC Singel 258\n" +
                "phone:    +31-61238-2827\n" +
                "nic-hdl:  ES222-RIPE\n" +
                "mnt-by:   TEST-DBM-MNT\n" +
                "changed:  eshryane@ripe.net 20120829\n" +
                "source:   test\n" +
                "remarks:  something\n" +
                "override: password";
        final String help = null;
        final String nnew = null;
        final String diff = null;
        final String redirect = null;
        final String source = "test";

        subject.doMultipartPost(request, source, data, help, nnew, diff, redirect);

        verify(messageHandler).handle(argThat(new ArgumentMatcher<UpdateRequest>() {
            @Override
            public boolean matches(final Object argument) {
                UpdateRequest updateRequest = (UpdateRequest) argument;
                assertThat(updateRequest.getKeyword(), is(Keyword.NONE));
                assertThat(updateRequest.getUpdateMessage(), is(data));
                return true;
            }
        }), any(UpdateContext.class));
    }


    @Test
    public void log_callback() throws Exception {
        final String message = "message";
        final OutputStream outputStream = mock(OutputStream.class);

        final SyncUpdatesService.SyncUpdateLogCallback logCallback = subject.new SyncUpdateLogCallback(message);
        logCallback.log(outputStream);

        verify(outputStream).write(message.getBytes());
    }

    @Test
    public void request_to_string() throws Exception {
        SyncUpdatesService.Request request = subject.new Request("person: name\naddress: Singel 258", "no", null, null, null, "127.0.0.1", "RIPE");

        assertThat(request.toString(), containsString("127.0.0.1"));
        assertThat(request.toString(), containsString("DATA=\n\nperson: name\naddress: Singel 258"));
        assertThat(request.toString(), containsString("NEW=no"));
    }
}
