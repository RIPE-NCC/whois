package net.ripe.db.whois.update.handler.response;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.ApplicationVersion;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.apiKey.OAuthSession;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectMessages;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.sso.UserSession;
import net.ripe.db.whois.update.domain.Ack;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.Notification;
import net.ripe.db.whois.update.domain.Operation;
import net.ripe.db.whois.update.domain.Origin;
import net.ripe.db.whois.update.domain.Paragraph;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.ResponseMessage;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.domain.UpdateResult;
import net.ripe.db.whois.update.domain.UpdateStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static net.ripe.db.whois.common.support.StringMatchesRegexp.stringMatchesRegexp;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ResponseFactoryTest {
    public static final String SKIPPED_PARAGRAPH = "" +
            "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "The following paragraph(s) do not look like objects\n" +
            "and were NOT PROCESSED:\n";


    public static final String WARNING = "***Warning: ";
    public static final String ERROR = "***Error: ";

    @Mock DateTimeProvider dateTimeProvider;
    @Mock UpdateContext updateContext;
    @Mock ApplicationVersion applicationVersion;
    @InjectMocks ResponseFactory subject;

    private Origin origin;
    private List<UpdateResult> updateResults;
    private List<Paragraph> ignoredParagraphs;

    @BeforeEach
    public void setUp() throws Exception {
        System.setProperty("instance.name", "10.0.0.0");
        origin = new Origin() {
            @Override
            public boolean isDefaultOverride() {
                return false;
            }

            @Override
            public boolean allowAdminOperations() {
                return false;
            }

            @Override
            public String getId() {
                return "<E1CCB659-9446-41B0-8E4A-5548104F94A9@ripe.net>";
            }

            @Override
            public String getFrom() {
                return "Andre Kampert <cac37ak@ripe.net>";
            }

            @Override
            public String getResponseHeader() {
                return "" +
                        ">  From:       Andre Kampert <cac37ak@ripe.net>\n" +
                        ">  Subject:    delete route 194.39.132.0/24\n" +
                        ">  Date:       Thu, 14 Jun 2012 10:04:42 +0200\n" +
                        ">  Reply-To:   cac37ak@ripe.net\n" +
                        ">  Message-ID: <E1CCB659-9446-41B0-8E4A-5548104F94A9@ripe.net>";
            }

            @Override
            public String getNotificationHeader() {
                return "";
            }

            @Override
            public String getName() {
                return "mail update";
            }
        };

        updateResults = Lists.newArrayList();
        ignoredParagraphs = Lists.newArrayList();

        when(dateTimeProvider.getCurrentDateTime()).thenReturn(LocalDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC));
        lenient().when(updateContext.printGlobalMessages()).thenReturn("");
        lenient().when(updateContext.getUserSession()).thenReturn(new UserSession("offereduuid","test@ripe.net", "Test User", true,"2033-01-30T16:38:27.369+11:00"));
        when(applicationVersion.getVersion()).thenReturn("1.2.3");

        ReflectionTestUtils.setField(subject, "source", "TEST");
    }

    @AfterEach
    public void after() {
        System.clearProperty("instance.name");
    }

    @Test
    public void getException() {
        final String response = subject.createExceptionResponse(updateContext, origin);

        assertThat(response, containsString("" +
                ">  From:       Andre Kampert <cac37ak@ripe.net>\n" +
                ">  Subject:    delete route 194.39.132.0/24\n" +
                ">  Date:       Thu, 14 Jun 2012 10:04:42 +0200\n" +
                ">  Reply-To:   cac37ak@ripe.net\n" +
                ">  Message-ID: <E1CCB659-9446-41B0-8E4A-5548104F94A9@ripe.net>\n" +
                "\n" +
                "Internal software error\n" +
                "\n" +
                "\n" +
                "The RIPE Database is subject to Terms and Conditions:\n" +
                "https://docs.db.ripe.net/terms-conditions.html\n" +
                "\n" +
                "For assistance or clarification please contact:\n" +
                "RIPE Database Administration <ripe-dbm@ripe.net>\n"));

        assertVersion(response);
    }

    @Test
    public void getAck_no_errors() {
        final RpslObject rpslObject = RpslObject.parse("mntner: DEV-ROOT-MNT");

        updateResults.add(new UpdateResult(rpslObject, rpslObject, Action.DELETE, UpdateStatus.SUCCESS, new ObjectMessages(), 0, false));

        final Ack ack = new Ack(updateResults, ignoredParagraphs);

        final String response = subject.createAckResponse(updateContext, origin, ack);
        assertThat(response, containsString("Number of objects found:                   1"));
        assertThat(response, containsString("Number of objects processed successfully:  1"));
        assertThat(response, containsString("Delete:         1"));
        assertThat(response, containsString("Number of objects processed with errors:   0"));
        assertThat(response, containsString("Delete:         0"));
        assertThat(response, not(containsString(WARNING)));
        assertThat(response, not(containsString(ERROR)));
        assertThat(response, not(containsString(SKIPPED_PARAGRAPH)));

        assertThat(response, containsString("" +
                ">  From:       Andre Kampert <cac37ak@ripe.net>\n" +
                ">  Subject:    delete route 194.39.132.0/24\n" +
                ">  Date:       Thu, 14 Jun 2012 10:04:42 +0200\n" +
                ">  Reply-To:   cac37ak@ripe.net\n" +
                ">  Message-ID: <E1CCB659-9446-41B0-8E4A-5548104F94A9@ripe.net>\n" +
                "\n" +
                "SUMMARY OF UPDATE:\n" +
                "\n" +
                "Number of objects found:                   1\n" +
                "Number of objects processed successfully:  1\n" +
                "  Create:         0\n" +
                "  Modify:         0\n" +
                "  Delete:         1\n" +
                "  No Operation:   0\n" +
                "Number of objects processed with errors:   0\n" +
                "  Create:         0\n" +
                "  Modify:         0\n" +
                "  Delete:         0\n" +
                "\n" +
                "DETAILED EXPLANATION:\n" +
                "\n" +
                "\n" +
                "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "The following object(s) were processed SUCCESSFULLY:\n" +
                "\n" +
                "---\n" +
                "Delete SUCCEEDED: [mntner] DEV-ROOT-MNT\n" +
                "\n" +
                "\n" +
                "\n" +
                "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "\n" +
                "The RIPE Database is subject to Terms and Conditions:\n" +
                "https://docs.db.ripe.net/terms-conditions.html\n" +
                "\n" +
                "For assistance or clarification please contact:\n" +
                "RIPE Database Administration <ripe-dbm@ripe.net>\n\n"));

        assertVersion(response);
    }

    @Test
    public void getAck_skipped_paragraphs() {
        final Paragraph paragraph1 = new Paragraph("paragraph 1");
        final Paragraph paragraph2 = new Paragraph("paragraph 2");

        ignoredParagraphs.add(paragraph1);
        ignoredParagraphs.add(paragraph2);

        final Ack ack = new Ack(updateResults, ignoredParagraphs);

        final String response = subject.createAckResponse(updateContext, origin, ack);

        assertThat(response, containsString(SKIPPED_PARAGRAPH));
        assertThat(response, containsString(paragraph1.getContent()));
        assertThat(response, containsString(paragraph2.getContent()));

        assertThat(response, containsString("" +
                ">  From:       Andre Kampert <cac37ak@ripe.net>\n" +
                ">  Subject:    delete route 194.39.132.0/24\n" +
                ">  Date:       Thu, 14 Jun 2012 10:04:42 +0200\n" +
                ">  Reply-To:   cac37ak@ripe.net\n" +
                ">  Message-ID: <E1CCB659-9446-41B0-8E4A-5548104F94A9@ripe.net>\n" +
                "\n" +
                "SUMMARY OF UPDATE:\n" +
                "\n" +
                "Number of objects found:                   0\n" +
                "Number of objects processed successfully:  0\n" +
                "  Create:         0\n" +
                "  Modify:         0\n" +
                "  Delete:         0\n" +
                "  No Operation:   0\n" +
                "Number of objects processed with errors:   0\n" +
                "  Create:         0\n" +
                "  Modify:         0\n" +
                "  Delete:         0\n" +
                "\n" +
                "DETAILED EXPLANATION:\n" +
                "\n" +
                "\n" +
                "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "The following paragraph(s) do not look like objects\n" +
                "and were NOT PROCESSED:\n" +
                "\n" +
                "paragraph 1\n" +
                "\n" +
                "paragraph 2\n" +
                "\n" +
                "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "\n" +
                "The RIPE Database is subject to Terms and Conditions:\n" +
                "https://docs.db.ripe.net/terms-conditions.html\n" +
                "\n" +
                "For assistance or clarification please contact:\n" +
                "RIPE Database Administration <ripe-dbm@ripe.net>\n"));

        assertVersion(response);
    }

    @Test
    public void getAck_errors() {
        final String rpslObjectString = "" +
                "route:         194.39.132.0/24\n" +
                "descr:         Description\n" +
                "origin:        AS1\n" +
                "notify:        notify@test.com\n" +
                "mnt-by:        TEST-MNT\n" +
                "source:        RIPE\n" +
                "delete:        no longer required\n";

        final RpslObject rpslObject = RpslObject.parse(rpslObjectString);
        final ObjectMessages objectMessages = new ObjectMessages();
        objectMessages.addMessage(rpslObject.findAttribute(AttributeType.SOURCE), UpdateMessages.unrecognizedSource("RIPE"));
        updateResults.add(new UpdateResult(rpslObject, rpslObject, Action.DELETE, UpdateStatus.FAILED, objectMessages, 0, false));
        final Ack ack = new Ack(updateResults, ignoredParagraphs);

        final String response = subject.createAckResponse(updateContext, origin, ack);

        assertThat(response, not(containsString(SKIPPED_PARAGRAPH)));
        assertThat(response, containsString(ERROR));

        assertThat(response, containsString("" +
                ">  From:       Andre Kampert <cac37ak@ripe.net>\n" +
                ">  Subject:    delete route 194.39.132.0/24\n" +
                ">  Date:       Thu, 14 Jun 2012 10:04:42 +0200\n" +
                ">  Reply-To:   cac37ak@ripe.net\n" +
                ">  Message-ID: <E1CCB659-9446-41B0-8E4A-5548104F94A9@ripe.net>\n" +
                "\n" +
                "SUMMARY OF UPDATE:\n" +
                "\n" +
                "Number of objects found:                   1\n" +
                "Number of objects processed successfully:  0\n" +
                "  Create:         0\n" +
                "  Modify:         0\n" +
                "  Delete:         0\n" +
                "  No Operation:   0\n" +
                "Number of objects processed with errors:   1\n" +
                "  Create:         0\n" +
                "  Modify:         0\n" +
                "  Delete:         1\n" +
                "\n" +
                "DETAILED EXPLANATION:\n" +
                "\n" +
                "\n" +
                "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "The following object(s) were found to have ERRORS:\n" +
                "\n" +
                "---\n" +
                "Delete FAILED: [route] 194.39.132.0/24AS1\n" +
                "\n" +
                "route:          194.39.132.0/24\n" +
                "descr:          Description\n" +
                "origin:         AS1\n" +
                "notify:         notify@test.com\n" +
                "mnt-by:         TEST-MNT\n" +
                "source:         RIPE\n" +
                "***Error:   Unrecognized source: RIPE\n" +
                "delete:         no longer required\n" +
                "\n" +
                "\n" +
                "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "\n" +
                "The RIPE Database is subject to Terms and Conditions:\n" +
                "https://docs.db.ripe.net/terms-conditions.html\n" +
                "\n" +
                "For assistance or clarification please contact:\n" +
                "RIPE Database Administration <ripe-dbm@ripe.net>\n"));

        assertVersion(response);
    }

    @Test
    public void createHelpResponse() {
        final String response = subject.createHelpResponse(updateContext, origin);

        assertThat(response, containsString("" +
                ">  From:       Andre Kampert <cac37ak@ripe.net>\n" +
                ">  Subject:    delete route 194.39.132.0/24\n" +
                ">  Date:       Thu, 14 Jun 2012 10:04:42 +0200\n" +
                ">  Reply-To:   cac37ak@ripe.net\n" +
                ">  Message-ID: <E1CCB659-9446-41B0-8E4A-5548104F94A9@ripe.net>\n" +
                "\n" +
                "You have requested Help information from the RIPE NCC Database,\n" +
                "therefore the body of your message has been ignored.\n" +
                "\n" +
                "RIPE Database documentation is available at\n" +
                "\n" +
                "http://www.ripe.net/data-tools/support/documentation\n" +
                "\n" +
                "RPSL RFCs are available at\n" +
                "\n" +
                "ftp://ftp.ripe.net/rfc/rfc2622.txt\n" +
                "ftp://ftp.ripe.net/rfc/rfc2725.txt\n" +
                "ftp://ftp.ripe.net/rfc/rfc4012.txt\n" +
                "\n" +
                "The RIPE Database is subject to Terms and Conditions:\n" +
                "https://docs.db.ripe.net/terms-conditions.html\n" +
                "\n" +
                "For assistance or clarification please contact:\n" +
                "RIPE Database Administration <ripe-dbm@ripe.net>\n"));

        assertVersion(response);
    }

    @Test
    public void notification_success() {
        final RpslObject object1 = RpslObject.parse("mntner: DEV-ROOT1-MNT");
        final Update update1 = new Update(new Paragraph(object1.toString()), Operation.UNSPECIFIED, Lists.<String>newArrayList(), object1);
        final PreparedUpdate create1 = new PreparedUpdate(update1, null, object1, Action.CREATE);

        final RpslObject object2 = RpslObject.parse("mntner: DEV-ROOT2-MNT");
        final Update update2 = new Update(new Paragraph(object2.toString()), Operation.UNSPECIFIED, Lists.<String>newArrayList(), object2);
        final PreparedUpdate create2 = new PreparedUpdate(update2, null, object2, Action.CREATE);

        final Notification notification = new Notification("notify@me.com");
        notification.add(Notification.Type.SUCCESS, create1, updateContext);
        notification.add(Notification.Type.SUCCESS, create2, updateContext);


        final ResponseMessage responseMessage = subject.createNotification(updateContext, origin, notification);

        assertNotification(responseMessage);

        assertThat(responseMessage.getSubject(), is("Notification of RIPE Database changes"));

        assertThat(responseMessage.getMessage(), containsString("" +
                "Some object(s) in RIPE Database that you either\n" +
                "maintain or you are listed in as to-be-notified have\n" +
                "been added, deleted or changed.\n"));

        assertThat(responseMessage.getMessage(), containsString("" +
                "---\n" +
                "OBJECT BELOW CREATED:\n" +
                "\n" +
                "mntner:         DEV-ROOT1-MNT\n" +
                "\n" +
                "---\n" +
                "OBJECT BELOW CREATED:\n" +
                "\n" +
                "mntner:         DEV-ROOT2-MNT\n"));
    }

    @Test
    public void notification_success_with_user_in_the_session() {
        when(updateContext.getUserSession()).thenReturn(new UserSession("offereduuid","test@ripe.net", "Test User", true,"2033-01-30T16:38:27.369+11:00"));

        final RpslObject object1 = RpslObject.parse("mntner: DEV-ROOT1-MNT");
        final Update update1 = new Update(new Paragraph(object1.toString()), Operation.UNSPECIFIED, Lists.<String>newArrayList(), object1);
        final PreparedUpdate create1 = new PreparedUpdate(update1, null, object1, Action.CREATE);

        final Notification notification = new Notification("notify@me.com");
        notification.add(Notification.Type.SUCCESS, create1, updateContext);

        final ResponseMessage responseMessage = subject.createNotification(updateContext, origin, notification);

        assertNotification(responseMessage);

        assertThat(responseMessage.getMessage(), containsString("You can reply to this message to contact the person who made this change.\n"));
    }

    @Test
    public void notification_success_with_effective_sso_credentials() {

        when(updateContext.getUserSession()).thenReturn(new UserSession("offereduuid","test@ripe.net", "Test User", true,"2033-01-30T16:38:27.369+11:00"));

        final RpslObject object1 = RpslObject.parse("mntner: DEV-ROOT1-MNT");
        final Update update1 = new Update(new Paragraph(object1.toString()), Operation.UNSPECIFIED, Lists.<String>newArrayList(), object1);
        final PreparedUpdate create1 = new PreparedUpdate(update1, null, object1, Action.CREATE);
        update1.setEffectiveCredential("test@ripe.net", Update.EffectiveCredentialType.SSO);


        final Notification notification = new Notification("notify@me.com");
        notification.add(Notification.Type.SUCCESS, create1, updateContext);

        final ResponseMessage responseMessage = subject.createNotification(updateContext, origin, notification);

        assertNotification(responseMessage);

        assertThat(responseMessage.getMessage(), containsString("" +
                "---\n" +
                "OBJECT BELOW CREATED:\n" +
                "\n" +
                "mntner:         DEV-ROOT1-MNT\n" +
                "\n" +
                "Changed by SSO account: test@ripe.net\n"+
                "\n" ));

    }

    @Test
    public void notification_success_with_effective_pgp_credentials() {

        final RpslObject object1 = RpslObject.parse("mntner: DEV-ROOT1-MNT");
        final Update update1 = new Update(new Paragraph(object1.toString()), Operation.UNSPECIFIED, Lists.<String>newArrayList(), object1);
        final PreparedUpdate create1 = new PreparedUpdate(update1, null, object1, Action.CREATE);
        update1.setEffectiveCredential("PGP-KEY-123", Update.EffectiveCredentialType.PGP);


        final Notification notification = new Notification("notify@me.com");
        notification.add(Notification.Type.SUCCESS, create1, updateContext);

        final ResponseMessage responseMessage = subject.createNotification(updateContext, origin, notification);

        assertNotification(responseMessage);

        assertThat(responseMessage.getMessage(), containsString("" +
                "---\n" +
                "OBJECT BELOW CREATED:\n" +
                "\n" +
                "mntner:         DEV-ROOT1-MNT\n" +
                "\n" +
                "Changed by PGP-KEY-123. You can find contact details for this key here:\n" +
                "https://apps.db.ripe.net/db-web-ui/query?source=RIPE&searchtext=PGP-KEY-123&types=key-cert\n"+
                "\n" ));

    }

    @Test
    public void notification_success_with_effective_apiKey_credentials() {

        final RpslObject object1 = RpslObject.parse("mntner: DEV-ROOT1-MNT");
        final Update update1 = new Update(new Paragraph(object1.toString()), Operation.UNSPECIFIED, Lists.<String>newArrayList(), object1);
        final PreparedUpdate create1 = new PreparedUpdate(update1, null, object1, Action.CREATE);
        update1.setEffectiveCredential("test@ripe.net (f60ee0fc)", Update.EffectiveCredentialType.APIKEY);


        final Notification notification = new Notification("notify@me.com");
        notification.add(Notification.Type.SUCCESS, create1, updateContext);

        final ResponseMessage responseMessage = subject.createNotification(updateContext, origin, notification);

        assertNotification(responseMessage);

        assertThat(responseMessage.getMessage(), containsString("" +
                "---\n" +
                "OBJECT BELOW CREATED:\n" +
                "\n" +
                "mntner:         DEV-ROOT1-MNT\n" +
                "\n" +
                "Changed by SSO account using API Key id: test@ripe.net (f60ee0fc)\n"+
                "\n" ));

    }

    @Test
    public void notification_success_with_effective_password_credentials() {

        final RpslObject object1 = RpslObject.parse("mntner: DEV-ROOT1-MNT");
        final Update update1 = new Update(new Paragraph(object1.toString()), Operation.UNSPECIFIED, Lists.newArrayList(), object1);
        final PreparedUpdate create1 = new PreparedUpdate(update1, null, object1, Action.CREATE);
        update1.setEffectiveCredential("MD5-PW", Update.EffectiveCredentialType.PASSWORD);

        final Notification notification = new Notification("notify@me.com");
        notification.add(Notification.Type.SUCCESS, create1, updateContext);

        final ResponseMessage responseMessage = subject.createNotification(updateContext, origin, notification);

        assertNotification(responseMessage);

        assertThat(responseMessage.getMessage(), containsString("" +
                "---\n" +
                "OBJECT BELOW CREATED:\n" +
                "\n" +
                "mntner:         DEV-ROOT1-MNT\n" +
                "\n" +
                "Changed by password.\n" +
                "\n" ));
    }

    @Test
    public void notification_success_with_effective_x509_credentials() {

        final RpslObject object1 = RpslObject.parse("mntner: DEV-ROOT1-MNT");
        final Update update1 = new Update(new Paragraph(object1.toString()), Operation.UNSPECIFIED, Lists.newArrayList(), object1);
        final PreparedUpdate create1 = new PreparedUpdate(update1, null, object1, Action.CREATE);
        update1.setEffectiveCredential("X509-1", Update.EffectiveCredentialType.X509);

        final Notification notification = new Notification("notify@me.com");
        notification.add(Notification.Type.SUCCESS, create1, updateContext);

        final ResponseMessage responseMessage = subject.createNotification(updateContext, origin, notification);

        assertNotification(responseMessage);

        assertThat(responseMessage.getMessage(), containsString("" +
                "---\n" +
                "OBJECT BELOW CREATED:\n" +
                "\n" +
                "mntner:         DEV-ROOT1-MNT\n" +
                "\n" +
                "Changed by X509-1. You can find contact details for this key here:\n" +
                "https://apps.db.ripe.net/db-web-ui/query?source=RIPE&searchtext=X509-1&types=key-cert\n"+
                "\n" ));
    }

    @Test
    public void notification_success_filter_auth() {
        final RpslObject object = RpslObject.parse("" +
                "mntner: DEV-MNT\n" +
                "auth: MD5-PW $1$YmPozTxJ$s3eGZRVrKVGdSDTeEZJu//\n" +
                "source: RIPE"
        );

        final Update update = new Update(new Paragraph(object.toString()), Operation.UNSPECIFIED, Lists.<String>newArrayList(), object);
        final PreparedUpdate create = new PreparedUpdate(update, null, object, Action.CREATE);

        final Notification notification = new Notification("notify@me.com");
        notification.add(Notification.Type.SUCCESS, create, updateContext);


        final ResponseMessage responseMessage = subject.createNotification(updateContext, origin, notification);

        assertNotification(responseMessage);

        assertThat(responseMessage.getSubject(), is("Notification of RIPE Database changes"));

        assertThat(responseMessage.getMessage(), containsString("" +
                "Some object(s) in RIPE Database that you either\n" +
                "maintain or you are listed in as to-be-notified have\n" +
                "been added, deleted or changed.\n"));

        assertThat(responseMessage.getMessage(), containsString("" +
                "---\n" +
                "OBJECT BELOW CREATED:\n" +
                "\n" +
                "mntner:         DEV-MNT\n" +
                "auth:           MD5-PW # Filtered\n" +
                "source:         RIPE # Filtered\n" +
                ""));
    }

    @Test
    public void notification_success_reference() {
        final RpslObject object1 = RpslObject.parse("mntner: DEV-ROOT1-MNT");
        final Update update1 = new Update(new Paragraph(object1.toString()), Operation.UNSPECIFIED, Lists.<String>newArrayList(), object1);
        final PreparedUpdate create1 = new PreparedUpdate(update1, null, object1, Action.CREATE);

        final RpslObject object2 = RpslObject.parse("mntner: DEV-ROOT2-MNT");
        final Update update2 = new Update(new Paragraph(object2.toString()), Operation.UNSPECIFIED, Lists.<String>newArrayList(), object2);
        final PreparedUpdate create2 = new PreparedUpdate(update2, null, object2, Action.CREATE);

        final Notification notification = new Notification("notify@me.com");
        notification.add(Notification.Type.SUCCESS_REFERENCE, create1, updateContext);
        notification.add(Notification.Type.SUCCESS_REFERENCE, create2, updateContext);

        final ResponseMessage responseMessage = subject.createNotification(updateContext, origin, notification);
        assertNotification(responseMessage);

        assertThat(responseMessage.getSubject(), is("Notification of RIPE Database changes"));

        assertThat(responseMessage.getMessage(), containsString("" +
                "Some object(s) in RIPE Database added references to\n" +
                "objects you are listed in as to-be-notified."));

        assertThat(responseMessage.getMessage(), containsString("" +
                "---\n" +
                "OBJECT BELOW CREATED:\n" +
                "\n" +
                "mntner:         DEV-ROOT1-MNT\n" +
                "\n" +
                "---\n" +
                "OBJECT BELOW CREATED:\n" +
                "\n" +
                "mntner:         DEV-ROOT2-MNT"));
    }

    @Test
    public void notification_auth_failed() {
        final RpslObject object1 = RpslObject.parse("mntner: DEV-ROOT1-MNT");
        final Update update1 = new Update(new Paragraph(object1.toString()), Operation.UNSPECIFIED, Lists.<String>newArrayList(), object1);
        final PreparedUpdate create1 = new PreparedUpdate(update1, null, object1, Action.CREATE);

        final RpslObject object2 = RpslObject.parse("mntner: DEV-ROOT2-MNT");
        final Update update2 = new Update(new Paragraph(object2.toString()), Operation.UNSPECIFIED, Lists.<String>newArrayList(), object2);
        final PreparedUpdate create2 = new PreparedUpdate(update2, null, object2, Action.CREATE);

        final Notification notification = new Notification("notify@me.com");
        notification.add(Notification.Type.FAILED_AUTHENTICATION, create1, updateContext);
        notification.add(Notification.Type.FAILED_AUTHENTICATION, create2, updateContext);

        final ResponseMessage responseMessage = subject.createNotification(updateContext, origin, notification);
        assertNotification(responseMessage);

        assertThat(responseMessage.getSubject(), is("RIPE Database updates, auth error notification"));

        assertThat(responseMessage.getMessage(), containsString("" +
                "Some objects in which you are referenced as a\n" +
                "maintainer were requested to be changed, but *failed*\n" +
                "the proper authorisation for any of the referenced\n" +
                "maintainers."));

        assertThat(responseMessage.getMessage(), containsString("" +
                "---\n" +
                "CREATE REQUESTED FOR:\n" +
                "\n" +
                "mntner:         DEV-ROOT1-MNT\n" +
                "\n" +
                "---\n" +
                "CREATE REQUESTED FOR:\n" +
                "\n" +
                "mntner:         DEV-ROOT2-MNT"));
    }

    private void assertNotification(final ResponseMessage responseMessage) {
        final String message = responseMessage.getMessage();
        String replayOrNotMessage = (updateContext.getUserSession() != null) ? "You can reply to this message to contact the person who made this change.\n" : "Please DO NOT reply to this message.\n";
                assertThat(message, containsString("" +
                "This is to notify you of changes in RIPE Database or\n" +
                "object authorisation failures.\n" +
                "\n" +
                "This message is auto-generated.\n" +
                replayOrNotMessage +
                "\n" +
                "If you do not understand why we sent you this message,\n" +
                "or for assistance or clarification please visit https://www.ripe.net/s/notify.\n" +
                "\n" +
                "Change requested from:"));

        assertThat(message, containsString("" +
                "The RIPE Database is subject to Terms and Conditions:\n" +
                "https://docs.db.ripe.net/terms-conditions.html\n" +
                "\n" +
                "For assistance or clarification please visit https://www.ripe.net/s/notify."));
    }

    private void assertVersion(final String response) {
        assertThat(response, stringMatchesRegexp("(?is).*" +
                "The RIPE Database is subject to Terms and Conditions:\n" +
                "https://docs.db.ripe.net/terms-conditions.html\n" +
                "\n" +
                "For assistance or clarification please contact:\n" +
                "RIPE Database Administration <ripe-dbm@ripe.net>\n" +
                "\n" +
                "Generated by RIPE WHOIS Update version 1.2.3 on .*\n" +
                "Handled mail update \\(TEST, 1970-01-01T00:00:00Z\\)\n" +
                ".*"));
    }
}
