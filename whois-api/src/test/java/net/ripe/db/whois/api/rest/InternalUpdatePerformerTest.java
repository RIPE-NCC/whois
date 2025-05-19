package net.ripe.db.whois.api.rest;

import com.google.common.collect.Lists;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Response;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectServerMapper;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.sso.AuthServiceClientException;
import net.ripe.db.whois.common.sso.SsoTokenTranslator;
import net.ripe.db.whois.common.sso.UserSession;
import net.ripe.db.whois.common.credentials.Credential;
import net.ripe.db.whois.update.domain.Operation;
import net.ripe.db.whois.update.domain.Origin;
import net.ripe.db.whois.common.credentials.OverrideCredential;
import net.ripe.db.whois.common.credentials.PasswordCredential;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.handler.UpdateRequestHandler;
import net.ripe.db.whois.update.log.LoggerContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
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
    public void create_update_with_override_no_passwords() {
        final RpslObject object = RpslObject.parse(
                "aut-num: AS123\n" +
                "mnt-by: TEST-MNT\n" +
                "source: TEST");

        final Update update = subject.createUpdate(updateContextMock, object, Collections.EMPTY_LIST, "no reason", "override");

        assertThat(update.getCredentials().all(), contains((Credential) OverrideCredential.parse("override")));
        assertThat(update.getDeleteReasons(), contains("no reason"));
        assertThat(update.getOperation(), is(Operation.DELETE));
        assertThat(update.getParagraph().getContent(), is(
                "aut-num:        AS123\n" +
                "mnt-by:         TEST-MNT\n" +
                "source:         TEST\n"));
        assertThat(update.getSubmittedObject(), is(object));
        assertThat(update.getType(), is(ObjectType.AUT_NUM));
    }

    @Test
    public void create_update_with_override_and_password_no_delete_reason() {
        final RpslObject object = RpslObject.parse(
                "mntner: TEST-MNT\n" +
                "mnt-by: TEST-MNT\n" +
                "source: TEST");

        final Update update = subject.createUpdate(updateContextMock, object, Collections.singletonList("password"), null, "override");

        assertThat(update.getCredentials().all(), containsInAnyOrder((Credential) OverrideCredential.parse("override"), (Credential) new PasswordCredential("password")));
        assertThat(update.getDeleteReasons(), is(nullValue()));
        assertThat(update.getOperation(), is(Operation.UNSPECIFIED));
        assertThat(update.getParagraph().getContent(), is(
                "mntner:         TEST-MNT\n" +
                "mnt-by:         TEST-MNT\n" +
                "source:         TEST\n"));
        assertThat(update.getSubmittedObject(), is(object));
        assertThat(update.getType(), is(ObjectType.MNTNER));
    }

    @Test
    public void create_update_no_override() {
        final RpslObject object = RpslObject.parse(
                "role: Test Role\n" +
                "nic-hdl: TP-TEST\n" +
                "mnt-by: TEST-MNT\n" +
                "source: TEST");

        final Update update = subject.createUpdate(updateContextMock, object, Collections.singletonList("password"), "no reason", null);

        assertThat(update.getCredentials().all(), contains((Credential) new PasswordCredential("password")));
        assertThat(update.getDeleteReasons(), contains("no reason"));
        assertThat(update.getParagraph().getContent(), is(
                "role:           Test Role\n" +
                "nic-hdl:        TP-TEST\n" +
                "mnt-by:         TEST-MNT\n" +
                "source:         TEST\n"));
        assertThat(update.getSubmittedObject(), is(object));
        assertThat(update.getType(), is(ObjectType.ROLE));
    }

    @Test
    public void create_update_passwords_only() {
        final RpslObject object = RpslObject.parse(
                "person: Test Person\n" +
                "nic-hdl: TP1-TEST\n" +
                "mnt-by: TEST-MNT\n" +
                "source: TEST");

        final Update update = subject.createUpdate(updateContextMock, object, Lists.newArrayList("password1", "password2"), null, null);

        assertThat(update.getCredentials().all(), containsInAnyOrder((Credential) new PasswordCredential("password1"), (Credential) new PasswordCredential("password2")));
        assertThat(update.getDeleteReasons(), is(nullValue()));
        assertThat(update.getParagraph().getContent(), is(
                "person:         Test Person\n" +
                "nic-hdl:        TP1-TEST\n" +
                "mnt-by:         TEST-MNT\n" +
                "source:         TEST\n"));
        assertThat(update.getSubmittedObject(), is(object));
        assertThat(update.getType(), is(ObjectType.PERSON));
    }

    @Test
    public void create_origin() {
        when(requestMock.getRemoteAddr()).thenReturn("127.0.0.1");
        when(dateTimeProviderMock.getCurrentDateTime()).thenReturn(ZonedDateTime.parse("2146-01-31T06:49:37.888+00:00").toLocalDateTime());

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

        verifyNoMoreInteractions(ssoTokenTranslatorMock);
        verifyNoMoreInteractions(loggerContextMock);

        subject.setSsoSessionToContext(updateContextMock, null);

        verifyNoMoreInteractions(ssoTokenTranslatorMock);
        verifyNoMoreInteractions(loggerContextMock);
    }

    @Test
    public void setSsoSessionToContext_successful_sso_translation() {
        final UserSession userSession = new UserSession("offereduuid","test@ripe.net", "Test User", true, "2033-01-30T16:38:27.369+11:00");
        when(ssoTokenTranslatorMock.translateSsoToken("test-token")).thenReturn(userSession);

        subject.setSsoSessionToContext(updateContextMock, "test-token");

        verify(ssoTokenTranslatorMock).translateSsoToken("test-token");
        verifyNoMoreInteractions(loggerContextMock);
        verify(updateContextMock).setUserSession(userSession);
    }

    @Test
    public void setSsoSessionToContext_exception_is_logged() {
        when(ssoTokenTranslatorMock.translateSsoToken("test-token")).thenThrow(new AuthServiceClientException(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), "exception"));

        try {
            subject.setSsoSessionToContext(updateContextMock, "test-token");
        } catch (AuthServiceClientException e) {
            verify(ssoTokenTranslatorMock.translateSsoToken("test-token"));
            verify(loggerContextMock).log(new Message(Messages.Type.ERROR, "exception"));
            verify(updateContextMock).addGlobalMessage(RestMessages.ssoAuthIgnored());
        }
    }
}
