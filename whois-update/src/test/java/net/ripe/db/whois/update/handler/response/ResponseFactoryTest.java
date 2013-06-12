package net.ripe.db.whois.update.handler.response;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectMessages;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.*;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ResponseFactoryTest {
    public static final String SKIPPED_PARAGRAPH = "" +
            "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "The following paragraph(s) do not look like objects\n" +
            "and were NOT PROCESSED:\n";


    public static final String WARNING = "***Warning: ";
    public static final String ERROR = "***Error: ";

    @Mock DateTimeProvider dateTimeProvider;
    @Mock UpdateContext updateContext;
    @InjectMocks ResponseFactory subject;

    private Origin origin;
    private List<UpdateResult> updateResults;
    private List<Paragraph> ignoredParagraphs;

    @Before
    public void setUp() throws Exception {
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

        when(dateTimeProvider.getCurrentDateTime()).thenReturn(new LocalDateTime(0, DateTimeZone.UTC));
        when(updateContext.printGlobalMessages()).thenReturn("");

        subject.setVersion("1.2.3");
        subject.setSource("TEST");
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
                "http://www.ripe.net/db/support/db-terms-conditions.pdf\n" +
                "\n" +
                "For assistance or clarification please contact:\n" +
                "RIPE Database Administration <ripe-dbm@ripe.net>\n"));

        assertVersion(response);
    }

    @Test
    public void getAck_no_errors() {
        final RpslObject rpslObject = RpslObject.parse("mntner: DEV-ROOT-MNT");

        final Update update = new Update(new Paragraph(rpslObject.toString()), Operation.DELETE, Lists.<String>newArrayList(), rpslObject);
        updateResults.add(new UpdateResult(update, rpslObject, rpslObject, Action.DELETE, UpdateStatus.SUCCESS, new ObjectMessages(), 0));

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
                "http://www.ripe.net/db/support/db-terms-conditions.pdf\n" +
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
                "http://www.ripe.net/db/support/db-terms-conditions.pdf\n" +
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
                "changed:       test@test.com 20120504\n" +
                "changed:       test@test.com 20120529\n" +
                "source:        RIPE\n" +
                "delete:        no longer required\n";

        final Paragraph paragraph = new Paragraph(rpslObjectString);
        final RpslObject rpslObject = RpslObject.parse(rpslObjectString);
        final Update update = new Update(paragraph, Operation.DELETE, Arrays.asList("no longer required"), rpslObject);

        final ObjectMessages objectMessages = new ObjectMessages();

        objectMessages.addMessage(rpslObject.findAttribute(AttributeType.SOURCE), UpdateMessages.unrecognizedSource("RIPE"));

        updateResults.add(new UpdateResult(update, rpslObject, rpslObject, Action.DELETE, UpdateStatus.FAILED, objectMessages, 0));

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
                "changed:        test@test.com 20120504\n" +
                "changed:        test@test.com 20120529\n" +
                "source:         RIPE\n" +
                "***Error:   Unrecognized source: RIPE\n" +
                "delete:         no longer required\n" +
                "\n" +
                "\n" +
                "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "\n" +
                "The RIPE Database is subject to Terms and Conditions:\n" +
                "http://www.ripe.net/db/support/db-terms-conditions.pdf\n" +
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
                "RIPE Database FAQ is available at\n" +
                "\n" +
                "http://www.ripe.net/data-tools/db/faq\n" +
                "\n" +
                "RPSL RFCs are available at\n" +
                "\n" +
                "ftp://ftp.ripe.net/rfc/rfc2622.txt\n" +
                "ftp://ftp.ripe.net/rfc/rfc2725.txt\n" +
                "ftp://ftp.ripe.net/rfc/rfc4012.txt\n" +
                "\n" +
                "The RIPE Database is subject to Terms and Conditions:\n" +
                "http://www.ripe.net/db/support/db-terms-conditions.pdf\n" +
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
        notification.add(Notification.Type.SUCCESS, create1);
        notification.add(Notification.Type.SUCCESS, create2);


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
    public void notification_success_filter_auth() {
        final RpslObject object = RpslObject.parse("" +
                "mntner: DEV-MNT\n" +
                "auth: MD5-PW $1$YmPozTxJ$s3eGZRVrKVGdSDTeEZJu//\n" +
                "source: RIPE"
        );

        final Update update = new Update(new Paragraph(object.toString()), Operation.UNSPECIFIED, Lists.<String>newArrayList(), object);
        final PreparedUpdate create = new PreparedUpdate(update, null, object, Action.CREATE);

        final Notification notification = new Notification("notify@me.com");
        notification.add(Notification.Type.SUCCESS, create);


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
        notification.add(Notification.Type.SUCCESS_REFERENCE, create1);
        notification.add(Notification.Type.SUCCESS_REFERENCE, create2);

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
        notification.add(Notification.Type.FAILED_AUTHENTICATION, create1);
        notification.add(Notification.Type.FAILED_AUTHENTICATION, create2);

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
        assertThat(message, containsString("" +
                "This is to notify you of changes in RIPE Database or\n" +
                "object authorisation failures.\n" +
                "\n" +
                "This message is auto-generated.\n" +
                "Please DO NOT reply to this message.\n" +
                "\n" +
                "If you do not understand why we sent you this message,\n" +
                "or for assistance or clarification please contact:\n" +
                "RIPE Database Administration <ripe-dbm@ripe.net>\n" +
                "\n" +
                "Change requested from:"));

        assertThat(message, containsString("" +
                "The RIPE Database is subject to Terms and Conditions:\n" +
                "http://www.ripe.net/db/support/db-terms-conditions.pdf\n" +
                "\n" +
                "For assistance or clarification please contact:\n" +
                "RIPE Database Administration <ripe-dbm@ripe.net>"));

        assertVersion(message);
    }

    private void assertVersion(final String response) {
        assertThat(response, containsString("" +
                "\n" +
                "The RIPE Database is subject to Terms and Conditions:\n" +
                "http://www.ripe.net/db/support/db-terms-conditions.pdf\n" +
                "\n" +
                "For assistance or clarification please contact:\n" +
                "RIPE Database Administration <ripe-dbm@ripe.net>\n" +
                "\n" +
                "Generated by RIPE WHOIS Update version 1.2.3 on UNDEFINED\n" +
                "Handled mail update (TEST, 1970-01-01 00:00:00)\n"));
    }
}
