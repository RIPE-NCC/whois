package net.ripe.db.whois.api.mail.dequeue;

import jakarta.mail.internet.ContentType;
import jakarta.mail.internet.MimeMessage;
import net.ripe.db.whois.api.MimeMessageProvider;
import net.ripe.db.whois.api.mail.MailMessage;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.update.domain.ContentWithCredentials;
import net.ripe.db.whois.update.domain.Keyword;
import net.ripe.db.whois.update.domain.PgpCredential;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.domain.X509Credential;
import net.ripe.db.whois.update.log.LoggerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MessageParserTest {
    @Mock MimeMessage mimeMessage;
    @Mock UpdateContext updateContext;
    @Mock LoggerContext loggerContext;
    @Mock DateTimeProvider dateTimeProvider;
    @InjectMocks MessageParser subject;

    @BeforeEach
    public void setUp() throws Exception {
        lenient().when(mimeMessage.getContentType()).thenReturn("text/plain");
        lenient().when(mimeMessage.getContent()).thenReturn("1234");
        lenient().when(dateTimeProvider.getCurrentZonedDateTime()).thenReturn(ZonedDateTime.now(ZoneOffset.UTC));
    }

    @Test
    public void parseKeywords_validOnes() throws Exception {
        final Keyword[] validKeywords = new Keyword[]{Keyword.HELP, Keyword.HOWTO, Keyword.NEW, Keyword.NONE};

        for (Keyword keyword : validKeywords) {
            final String keywordKeyword = keyword.getKeyword();
            if (keywordKeyword == null) {
                continue;
            }
            when(mimeMessage.getSubject()).thenReturn(keywordKeyword);

            final MailMessage message = subject.parse(mimeMessage, updateContext);

            assertThat(keyword.toString(), message.getKeyword(), is(keyword));
            verify(updateContext, never()).addGlobalMessage(any(Message.class));
        }
    }

    @Test
    public void parseKeywords_diff() throws Exception {
        final Keyword keyword = Keyword.DIFF;

        when(mimeMessage.getSubject()).thenReturn(keyword.getKeyword());

        final MailMessage message = subject.parse(mimeMessage, updateContext);

        assertThat(keyword.toString(), message.getKeyword(), is(keyword));
        verify(updateContext, times(1)).addGlobalMessage(UpdateMessages.diffNotSupported());
    }

    @Test
    public void parse_set_delivery_date() throws Exception {
        MimeMessage simpleTextUnsignedMessage = MimeMessageProvider.getMessageSimpleTextUnsigned();

        final MailMessage result = subject.parse(simpleTextUnsignedMessage, updateContext);

        // delivery date in message header Mon, 28 May 2012 00:04:45 +0200
        // Now should be in UTC
        assertThat(result.getDate(), is("Sun May 27 22:04:45 Z 2012"));
    }

    @Test
    public void parse_set_default_date() throws Exception {
        when(mimeMessage.getSubject()).thenReturn("NEW");

        final MailMessage message = subject.parse(mimeMessage, updateContext);

        assertThat(message.getDate().length(), not(is(0)));
        final String timezone = DateTimeFormatter.ofPattern("zzz").format(ZonedDateTime.now(ZoneOffset.UTC));
        assertThat(message.getDate(), containsString(timezone));
        final String year = DateTimeFormatter.ofPattern("yyyy").format(ZonedDateTime.now(ZoneOffset.UTC));
        assertThat(message.getDate(), containsString(year));
    }

    @Test
    public void parseKeywords_mixedCaps() throws Exception {
        when(mimeMessage.getSubject()).thenReturn("nEw");
        final MailMessage message = subject.parse(mimeMessage, updateContext);

        assertThat(message.getKeyword(), is(Keyword.NEW));
        verify(updateContext, never()).addGlobalMessage(any(Message.class));
    }

    @Test
    public void parseSending_stringWithKeyword() throws Exception {
        final String keywordString = "sending my new objects";
        when(mimeMessage.getSubject()).thenReturn(keywordString);
        final MailMessage message = subject.parse(mimeMessage, updateContext);

        assertThat(message.getKeyword(), is(Keyword.NONE));
        verify(updateContext, times(1)).addGlobalMessage(UpdateMessages.invalidKeywordsFound(keywordString));
        verify(updateContext, times(1)).addGlobalMessage(UpdateMessages.allKeywordsIgnored());
    }

    @Test
    public void parseKeywords_keyWords() throws Exception {
        final String keywordString = "KEYWORDS:";
        when(mimeMessage.getSubject()).thenReturn(keywordString);
        final MailMessage message = subject.parse(mimeMessage, updateContext);

        assertThat(message.getKeyword(), is(Keyword.NONE));
        verify(updateContext, times(1)).addGlobalMessage(UpdateMessages.invalidKeywordsFound(keywordString));
        verify(updateContext, times(1)).addGlobalMessage(UpdateMessages.allKeywordsIgnored());
    }

    @Test
    public void parseKeywords_keyWordsAndNew() throws Exception {
        final String keywordString = "KEYWORDS: new";
        when(mimeMessage.getSubject()).thenReturn(keywordString);
        final MailMessage message = subject.parse(mimeMessage, updateContext);

        assertThat(message.getKeyword(), is(Keyword.NONE));
        verify(updateContext, times(1)).addGlobalMessage(UpdateMessages.invalidKeywordsFound(keywordString));
        verify(updateContext, times(1)).addGlobalMessage(UpdateMessages.allKeywordsIgnored());
    }

    @Test
    public void parseKeywords_twoValidKeywords() throws Exception {
        final String keywordString = "new help";
        when(mimeMessage.getSubject()).thenReturn(keywordString);
        final MailMessage message = subject.parse(mimeMessage, updateContext);

        assertThat(message.getKeyword(), is(Keyword.NONE));
        assertThat(message.getContentWithCredentials(), hasSize(1));
        final ContentWithCredentials contentWithCredentials = message.getContentWithCredentials().get(0);
        assertThat(contentWithCredentials.getContent(), containsString("1234"));
        assertThat(contentWithCredentials.getCredentials(), hasSize(0));

        assertThat(message.getUpdateMessage(), is("1234"));

        verify(updateContext, times(1)).addGlobalMessage(UpdateMessages.invalidKeywordsFound(keywordString));
        verify(updateContext, times(1)).addGlobalMessage(UpdateMessages.allKeywordsIgnored());
    }

    @Test
    public void parse_invalid_reply_to() throws Exception {
        final String messageWithInvalidReplyTo = "Reply-To: <respondera: ventas@amusing.cl>";

        MailMessage result = subject.parse(messageWithInvalidReplyTo, updateContext);

        assertThat(result.getReplyTo(), is(emptyString()));
    }

    @Test
    public void parse_missing_reply_to() throws Exception {
        final String messageWithoutReplyTo = "From: minimal@mailclient.org";

        MailMessage result = subject.parse(messageWithoutReplyTo, updateContext);

        assertThat(result.getReplyTo(), is("minimal@mailclient.org"));
        assertThat(result.getFrom(), is("minimal@mailclient.org"));
    }

    @Test
    public void parse_plain_text_unsigned_message() throws Exception {
        final MailMessage message = subject.parse(MimeMessageProvider.getMessageSimpleTextUnsigned(), updateContext);

        assertThat(message.getSubject(), is(""));
        assertThat(message.getFrom(), is("\"foo@test.de\" <bitbucket@ripe.net>"));
        assertThat(message.getId(), is("<20120527220444.GA6565@XXXsource.test.de>"));
        assertThat(message.getReplyTo(), is("\"foo@test.de\" <bitbucket@ripe.net>"));
        assertThat(message.getKeyword(), is(Keyword.NONE));

        final String expectedValue = "" +
                "inetnum: 109.69.68.0 - 109.69.68.7\n" +
                "netname: delete\n" +
                "descr: Description\n" +
                "country: DE\n" +
                "admin-c: T1-RIPE\n" +
                "tech-c: T1-RIPE\n" +
                "status: ASSIGNED PA\n" +
                "mnt-by: TEST-MNT\n" +
                "password: password\n" +
                "source: RIPE\n" +
                "\n" +
                "inetnum: 109.69.68.0 - 109.69.68.7\n" +
                "netname: delete\n" +
                "descr: Description\n" +
                "country: DE\n" +
                "admin-c: T1-RIPE\n" +
                "tech-c: T1-RIPE\n" +
                "status: ASSIGNED PA\n" +
                "mnt-by: TEST-MNT\n" +
                "password: password\n" +
                "source: RIPE\n" +
                "delete: new subnet size\n";

        assertThat(message.getUpdateMessage(), is(expectedValue));

        assertThat(message.getContentWithCredentials(), hasSize(1));
        final ContentWithCredentials contentWithCredentials = message.getContentWithCredentials().get(0);
        assertThat(contentWithCredentials.getContent(), containsString(expectedValue));
        assertThat(contentWithCredentials.getCredentials(), hasSize(0));
    }

    @Test
    public void parse_text_html_message() throws Exception {
        final MailMessage message = subject.parse(MimeMessageProvider.getUpdateMessage("simpleHtmlTextUnsigned.mail"), updateContext);

        assertThat(message.getId(), is("<20120508173357.B3369481C7@ip-10-251-81-156.ec2.internal>"));
        assertThat(message.getReplyTo(), is("bitbucket@ripe.net"));
        assertThat(message.getKeyword(), is(Keyword.NONE));
        assertThat(message.getContentWithCredentials(), hasSize(0));
    }

    @Test
    public void parse_plain_text_utf8_encoded_message() throws Exception {
        final MailMessage message = subject.parse(MimeMessageProvider.getUpdateMessage("simplePlainTextUtf8Encoded.mail"), updateContext);

        assertThat(message.getId(), is("<20120824155310.258DB8F65CA@mx2.bogus.com>"));
        assertThat(message.getReplyTo(), is("test@foo.com"));
        assertThat(message.getKeyword(), is(Keyword.NONE));
        final String expectedValue = "" +
                "inetnum: 10.0.0.0 - 10.0.0.255\n" +
                "status:ASSIGNED PI\n" +
                "mnt-by:T2-MNT\n" +
                "mnt-by:TEST-DBM-MNT\n" +
                "tech-c:VB1-TEST\n" +
                "tech-c:AA1-TEST\n" +
                "notify:test@foo.com\n" +
                "netname:TEST-NETWORK\n" +
                "org:ORG-VBO1-TEST\n" +
                "admin-c:VB1-TEST\n" +
                "admin-c:AA2-TEST\n" +
                "password:password\n" +
                "descr:\u042E\u043D\u0438\u043A\u043E\u0434\u043D\u043E\u0435 \u043E\u043F\u0438\u0441\u0430\u043D\u0438\u0435\n" +
                "country:US\n" +
                "country:BY\n" +
                "country:RU\n" +
                "source:TEST";
        assertThat(message.getUpdateMessage(), is(expectedValue));

        assertThat(message.getContentWithCredentials(), hasSize(1));
        final ContentWithCredentials contentWithCredentials = message.getContentWithCredentials().get(0);
        assertThat(contentWithCredentials.getContent(), containsString("inetnum: 10.0.0.0 - 10.0.0.255"));
        assertThat(contentWithCredentials.getCredentials(), hasSize(0));
    }

    @Test
    public void parse_plain_text_inline_pgp_signature() throws Exception {
        final MailMessage message = subject.parse(MimeMessageProvider.getUpdateMessage("inlinePgpSigned.mail"), updateContext);

        assertThat(message.getId(), is("<ABC593474ADCF6EC3E43BC80@XXX[10.41.147.77]>"));
        assertThat(message.getReplyTo(), is("John Doe <bitbucket@ripe.net>"));
        assertThat(message.getKeyword(), is(Keyword.NONE));
        final String expectedValue =
                "-----BEGIN PGP SIGNED MESSAGE-----\n" +
                "Hash: SHA1\n" +
                "\n" +
                "route:          99.13.0.0/16\n" +
                "descr:          Route\n" +
                "origin:         AS3333\n" +
                "mnt-by:         TEST-DBM-MNT\n" +
                "source:         TEST\n" +
                "delete:         no longer required\n" +
                "-----BEGIN PGP SIGNATURE-----\n" +
                "Version: GnuPG v1\n" +
                "Comment: GPGTools - http://gpgtools.org\n" +
                "\n" +
                "iQEcBAEBAgAGBQJWTIkmAAoJELvMuy1XY5UN2yAH/jrxCDpjg0rPZQJQaF33eyNS\n" +
                "rWXaIcfNO1pOMTIBLsDLHG2b0gEwRMYNsN+/J50Wb2VaXYI/kjRQEZPgJOTZomFX\n" +
                "kt+aWDXSWTE+fsclfoB+vcSraGJFgFTtJM66MHDvKSJ80tWf2VK64Y8MgKmOv6vW\n" +
                "hqxmZePeIr4p4L3w3q3n9GrOkpaPscJtEMTPqqFhft6hsiKTBfUuq/8Inski0LZt\n" +
                "85cWPo4wZP4L2z5OfGsEXnrFAF69AzyXaSwL5B9FsEmQEK/cS09n7mU+fqsKNCAB\n" +
                "npzCZ/Wa9t0alM7BtUZFS7Q2z4BWmSkc75R9U+cv7rafDv8Z2thlvEcHf9fqG0s=\n" +
                "=zw+b\n" +
                "-----END PGP SIGNATURE-----";

        assertThat(message.getUpdateMessage(), is(expectedValue));

        assertThat(message.getContentWithCredentials(), hasSize(1));
        final ContentWithCredentials contentWithCredentials = message.getContentWithCredentials().get(0);
        assertThat(contentWithCredentials.getContent(), containsString(expectedValue));
        assertThat(contentWithCredentials.getCredentials(), hasSize(0));
    }

    @Test
    public void parse_multipart_text_plain_detached_pgp_signature() throws Exception {
        final MailMessage message = subject.parse(MimeMessageProvider.getMessageMultipartPgpSigned(), updateContext);

        assertThat(message.getId(), is("<0C4C4196-55E6-4E8B-BE54-F8A92DEBD1A0@ripe.net>"));
        assertThat(message.getReplyTo(), is("User <user@ripe.net>"));
        assertThat(message.getKeyword(), is(Keyword.NEW));

        final String expectedValue = "" +
                "key-cert:        AUTO-1\n" +
                "method:          X509\n" +
                "owner:           /CN=4a96eecf-9d1c-4e12-8add-5ea5522976d8\n" +
                "fingerpr:        82:7C:C5:40:D1:DB:AE:6A:FA:F8:40:3E:3C:9C:27:7C\n" +
                "certif:          -----BEGIN CERTIFICATE-----\n" +
                "certif:          -----END CERTIFICATE-----\n" +
                "mnt-by:          TEST-DBM-MNT\n" +
                "remarks:         remark\n" +
                "source:          TEST";

        assertThat(message.getUpdateMessage(), is(expectedValue));

        List<ContentWithCredentials> contentWithCredentials = message.getContentWithCredentials();
        assertThat(contentWithCredentials, hasSize(1));
        assertThat(contentWithCredentials.get(0).getContent(), is(expectedValue));
        assertThat(contentWithCredentials.get(0).getCredentials().get(0), is(instanceOf(PgpCredential.class)));
    }

    @Test
    public void parse_multipart_alternative_unsigned() throws Exception {
        final MailMessage message = subject.parse(MimeMessageProvider.getUpdateMessage("multipartAlternativeUnsigned.mail"), updateContext);

        final String expectedValue = "" +
                "password: password\npassword: password\n\n" +
                "as-set:      AS198792:AS-TEST\n" +
                "descr:       Description\n" +
                "tech-c:      P1-RIPE\n" +
                "admin-c:     P1-RIPE\n" +
                "mnt-by:      TEST-MNT\n" +
                "mnt-by:      TEST-MNT\n" +
                "source:      RIPE\n\n\n" +
                "as-set:      AS1:AS-FOO-BOGUS\n" +
                "descr:       Description\n" +
                "tech-c:      P1-RIPE\n" +
                "admin-c:     P1-RIPE\n" +
                "mnt-by:      TEST-MNT\n" +
                "mnt-by:      TEST-MNT\n" +
                "source:      RIPE\n";

        assertThat(message.getUpdateMessage(), is(expectedValue));
        assertThat(message.getId(), is("<CACt+-2p6YfJAgtZLYSf=stuVV7M+6gBvbDLKWdBzXVC+vmxkbg@XXXmail.gmail.com>"));
        assertThat(message.getReplyTo(), is("John Doe <john@doe.com>"));
        assertThat(message.getKeyword(), is(Keyword.NONE));

        assertThat(message.getContentWithCredentials(), hasSize(1));
        final ContentWithCredentials contentWithCredentials = message.getContentWithCredentials().get(0);
        assertThat(contentWithCredentials.getContent(), containsString(expectedValue));
        assertThat(contentWithCredentials.getCredentials(), hasSize(0));
    }

    @Test
    public void parse_multipart_alternative_inline_pgp_signature() throws Exception {
        final MailMessage message = subject.parse(MimeMessageProvider.getMessageMultipartAlternativePgpSigned(), updateContext);

        final String expectedValue =
                "-----BEGIN PGP SIGNED MESSAGE-----\n" +
                "Hash: SHA1\n" +
                "\n" +
                "route:          165.251.168.0/22\n" +
                "descr:          Description\n" +
                "origin:         AS1\n" +
                "notify:         ripe-admin@foo.net\n" +
                "mnt-by:         TEST-MNT\n" +
                "source:         RIPE\n" +
                "-----BEGIN PGP SIGNATURE-----\n" +
                "Version: GnuPG v1\n" +
                "\n" +
                "iQEcBAEBAgAGBQJWTI+jAAoJELvMuy1XY5UNLksH/184MpXD2xGRlEXHaGfvZ3X4\n" +
                "5m9LrlowItjCiSM3a9rdsVAlijxpCnz7IgkdqbU7hIk8HfiUotKUAk0vgxDWTQnG\n" +
                "1wQFvdgi0pjt6y+1kSxCUeVwuiub9St9SyCam1yz7RROFjzdQ654Z5X3vNQnxwog\n" +
                "aO1VT/cRfXoi2GvFHHzMdXVRhtu11OdwdvFxXY+1HXLxc8+OCAYymaRxLGBU9sPA\n" +
                "7A1aKgMxgFOvPrDuRM52KkwxFueqcVU/yi+LH/c2BTuNoQBmunPlu3Lqqq7+R4Wo\n" +
                "8F8NV8aCvn1Jtb6apKhrDUMA/MTOuaD36FKl5KwL0/Ty1OlZCFlw47JwZL31e4I=\n" +
                "=oiZ5\n" +
                "-----END PGP SIGNATURE-----";

        assertThat(message.getUpdateMessage(), is(expectedValue));

        assertThat(message.getId(), is("<D8447F003C9F4CA891CFA626760EBE8E@XXXRaysOfficePC>"));
        assertThat(message.getReplyTo(), is("John Doe <john@doe.net>"));
        assertThat(message.getKeyword(), is(Keyword.NONE));

        assertThat(message.getContentWithCredentials(), hasSize(1));
        final ContentWithCredentials contentWithCredentials = message.getContentWithCredentials().get(0);
        assertThat(contentWithCredentials.getContent(), containsString(expectedValue));
        assertThat(contentWithCredentials.getCredentials(), hasSize(0));
    }

    @Test
    public void parse_multipart_alternative_detached_pgp_signature() throws Exception {
        final MailMessage mailMessage = subject.parse(
                "From: noreply@ripe.net\n" +
                "Content-Type: multipart/signed;\n" +
                "\tboundary=\"Apple-Mail=_8CAC1D90-3ABC-4010-9219-07F34D68A205\";\n" +
                "\tprotocol=\"application/pgp-signature\";\n" +
                "\tmicalg=pgp-sha1\n" +
                "Subject: NEW\n" +
                "Date: Wed, 2 Jan 2013 16:53:25 +0100\n" +
                "Message-Id: <220284EA-D739-4453-BBD2-807C87666F23@ripe.net>\n" +
                "To: test-dbm@ripe.net\n" +
                "Mime-Version: 1.0 (Apple Message framework v1283)\n" +
                "\n" +
                "\n" +
                "--Apple-Mail=_8CAC1D90-3ABC-4010-9219-07F34D68A205\n" +
                "Content-Type: multipart/alternative;\n" +
                "\tboundary=\"Apple-Mail=_40C18EAF-8C7D-479F-9001-D91F1181EEDA\"\n" +
                "\n" +
                "\n" +
                "--Apple-Mail=_40C18EAF-8C7D-479F-9001-D91F1181EEDA\n" +
                "Content-Transfer-Encoding: 7bit\n" +
                "Content-Type: text/plain;\n" +
                "\tcharset=us-ascii\n" +
                "\n" +
                "person:  First Person\n" +
                "address: St James Street\n" +
                "address: Burnley\n" +
                "address: UK\n" +
                "phone:   +44 282 420469\n" +
                "nic-hdl: FP1-TEST\n" +
                "mnt-by:  OWNER-MNT\n" +
                "source:  TEST\n" +
                "\n" +
                "\n" +
                "--Apple-Mail=_40C18EAF-8C7D-479F-9001-D91F1181EEDA\n" +
                "Content-Transfer-Encoding: 7bit\n" +
                "Content-Type: text/html;\n" +
                "\tcharset=us-ascii\n" +
                "\n" +
                "<html><head></head><body style=\"word-wrap: break-word; -webkit-nbsp-mode: space; -webkit-line-break: after-white-space; \">" +
                "<div style=\"font-size: 13px; \"><b>person: &nbsp;First Person</b></div>" +
                "<div style=\"font-size: 13px; \"><b>address: St James Street</b></div>" +
                "<div style=\"font-size: 13px; \"><b>address: Burnley</b></div>" +
                "<div style=\"font-size: 13px; \"><b>address: UK</b></div>" +
                "<div style=\"font-size: 13px; \"><b>phone: &nbsp; +44 282 420469</b></div>" +
                "<div style=\"font-size: 13px; \"><b>nic-hdl: FP1-TEST</b></div>" +
                "<div style=\"font-size: 13px; \"><b>mnt-by: &nbsp;OWNER-MNT</b></div>" +
                "<div style=\"font-size: 13px; \"><b>source: &nbsp;TEST</b></div>" +
                "<div><br></div></body></html>\n" +
                "--Apple-Mail=_40C18EAF-8C7D-479F-9001-D91F1181EEDA--\n" +
                "\n" +
                "--Apple-Mail=_8CAC1D90-3ABC-4010-9219-07F34D68A205\n" +
                "Content-Transfer-Encoding: 7bit\n" +
                "Content-Disposition: attachment;\n" +
                "\tfilename=signature.asc\n" +
                "Content-Type: application/pgp-signature;\n" +
                "\tname=signature.asc\n" +
                "Content-Description: Message signed with OpenPGP using GPGMail\n" +
                "\n" +
                "-----BEGIN PGP SIGNATURE-----\n" +
                "Version: GnuPG v1.4.12 (Darwin)\n" +
                "\n" +
                "iQEcBAEBAgAGBQJQ5Ff1AAoJEO6ZHuIo9s1sQxoIAJYdnvbYjCwRyKgz7sB6/Lmh\n" +
                "Ca7A9FrKuRFXHH2IUM6FIlC8hvFpAlXfkSWtJ03PL4If3od0jL9pwge8hov75+nL\n" +
                "FnhCG2ktb6CfzjoaeumTvzbt5oSbq2itgvaQ15V6Rpb2LIh7yfAcoJ7UgK5X1XEI\n" +
                "OhZvuGy9M49unziI3oF0WwHl4b2bAt/r7/7DNgxlT00pMFqrcI3n00TXEAJphpzH\n" +
                "7Ym5+7PYvTtanxb5x8pMCmgtsKgF5RoHQv4ZBaSS0z00WVivk3cuCugziyTrwI2+\n" +
                "4IkFu75GfD+xKAldd2of09SrFEaOJfXNslq+BZoqc3hGOV+b7vpNARp0s7zsq4E=\n" +
                "=O7qu\n" +
                "-----END PGP SIGNATURE-----\n" +
                "\n" +
                "--Apple-Mail=_8CAC1D90-3ABC-4010-9219-07F34D68A205--", updateContext);

        assertThat(mailMessage.getContentWithCredentials(), hasSize(1));
        final ContentWithCredentials contentWithCredentials = mailMessage.getContentWithCredentials().get(0);
        assertThat(contentWithCredentials.getCredentials(), hasSize(1));
        assertThat(contentWithCredentials.getCredentials().get(0), is(instanceOf(PgpCredential.class)));
        assertThat(contentWithCredentials.getContent(), is("" +
                "person:  First Person\n" +
                "address: St James Street\n" +
                "address: Burnley\n" +
                "address: UK\n" +
                "phone:   +44 282 420469\n" +
                "nic-hdl: FP1-TEST\n" +
                "mnt-by:  OWNER-MNT\n" +
                "source:  TEST\n\n"));
    }

    @Test
    public void parse_multipart_mixed_signed_part() throws Exception {
        final MailMessage mailMessage = subject.parse(
                "To: auto-dbm@ripe.net\n" +
                "From: No Reply <noreply@ripe.net>\n" +
                "Subject: NEW\n" +
                "Message-ID: <56FCE84F.2010807@ripe.net>\n" +
                "Date: Thu, 31 Mar 2016 11:05:19 +0200\n" +
                "User-Agent: Mozilla/5.0 (X11; Linux x86_64; rv:38.0) Gecko/20100101\n" +
                " Thunderbird/38.5.1\n" +
                "MIME-Version: 1.0\n" +
                "Content-Type: multipart/signed; micalg=pgp-sha1;\n" +
                " protocol=\"application/pgp-signature\";\n" +
                " boundary=\"JOqtbv2KmE4lQ7wD2J932c0LrelKPreUg\"\n" +
                "\n" +
                "This is an OpenPGP/MIME signed message (RFC 4880 and 3156)\n" +
                "--JOqtbv2KmE4lQ7wD2J932c0LrelKPreUg\n" +
                "Content-Type: multipart/mixed; boundary=\"SWaLakd0w46TEjGoFnX2jpFn3h7kTqgWh\"\n" +
                "From: No Reply <noreply@ripe.net>\n" +
                "To: auto-dbm@ripe.net\n" +
                "Message-ID: <56FCE84F.2010807@ripe.net>\n" +
                "Subject: NEW\n" +
                "\n" +
                "--SWaLakd0w46TEjGoFnX2jpFn3h7kTqgWh\n" +
                "Content-Type: text/plain; charset=utf-8\n" +
                "Content-Transfer-Encoding: quoted-printable\n" +
                "\n" +
                "person:  First Person\n" +
                "address: St James Street\n" +
                "address: Burnley\n" +
                "address: UK\n" +
                "phone:   +44 282 420469\n" +
                "nic-hdl: FP1-TEST\n" +
                "mnt-by:  OWNER-MNT\n" +
                "source:  TEST\n" +
                "\n" +
                "--SWaLakd0w46TEjGoFnX2jpFn3h7kTqgWh--\n" +
                "\n" +
                "--JOqtbv2KmE4lQ7wD2J932c0LrelKPreUg\n" +
                "Content-Type: application/pgp-signature; name=\"signature.asc\"\n" +
                "Content-Description: OpenPGP digital signature\n" +
                "Content-Disposition: attachment; filename=\"signature.asc\"\n" +
                "\n" +
                "-----BEGIN PGP SIGNATURE-----\n" +
                "Comment: GPGTools - http://gpgtools.org\n" +
                "\n" +
                "iQEcBAEBCAAGBQJW/UFbAAoJELvMuy1XY5UN8sIH/jknae8l8dK4pJnf1CK4fXLq\n" +
                "YedvGzo10gpLKlSu0UnBRapE6aZcPNmYLMJReP/JhbPfRAr7XQCRJKVwOngntmwi\n" +
                "b4p9C+GYOUEUScLbBYe4FS70xPQZBEqDEB+pPEQI7DSRMQs/aF3fSXGHygJA2EGp\n" +
                "QE5e2i0OwL1usnkRb87IXxFoL/LAalHogaU7m+vwJADD5ERqF1jNfiw4B8LQRF8x\n" +
                "Yq5BtICOBOj7sL/JCKL17KrGlcQFWL3StLGf6IghltSSnAVQesKY4k2SzyyCulKL\n" +
                "v3REE7AAtRIih8l+VP4dL09AD1/mWT38D24gjOC+HOHcdGbl7YmE9cHr3xwetgY=\n" +
                "=GA9B\n" +
                "-----END PGP SIGNATURE-----\n" +
                "\n" +
                "--JOqtbv2KmE4lQ7wD2J932c0LrelKPreUg--", updateContext);

        assertThat(mailMessage.getContentWithCredentials(), hasSize(1));
        final ContentWithCredentials contentWithCredentials = mailMessage.getContentWithCredentials().get(0);
        assertThat(contentWithCredentials.getCredentials(), hasSize(1));
        assertThat(contentWithCredentials.getCredentials().get(0), is(instanceOf(PgpCredential.class)));
        assertThat(contentWithCredentials.getContent(), is(
                "Content-Type: multipart/mixed; boundary=\"SWaLakd0w46TEjGoFnX2jpFn3h7kTqgWh\"\n" +
                "From: No Reply <noreply@ripe.net>\n" +
                "To: auto-dbm@ripe.net\n" +
                "Message-ID: <56FCE84F.2010807@ripe.net>\n" +
                "Subject: NEW\n" +
                "\n" +
                "--SWaLakd0w46TEjGoFnX2jpFn3h7kTqgWh\n" +
                "Content-Type: text/plain; charset=utf-8\n" +
                "Content-Transfer-Encoding: quoted-printable\n" +
                "\n" +
                "person:  First Person\n" +
                "address: St James Street\n" +
                "address: Burnley\n" +
                "address: UK\n" +
                "phone:   +44 282 420469\n" +
                "nic-hdl: FP1-TEST\n" +
                "mnt-by:  OWNER-MNT\n" +
                "source:  TEST\n" +
                "\n" +
                "--SWaLakd0w46TEjGoFnX2jpFn3h7kTqgWh--\n"));
    }

    @Test
    public void parse_smime_multipart_text_plain() throws Exception {
        final MailMessage result = subject.parse(
                "From: <Registration.Ripe@company.com>\n" +
                "To: <auto-dbm@ripe.net>\n" +
                "Subject: Bogus\n" +
                "Date: Mon, 20 Aug 2012 10:52:39 +0000\n" +
                "Message-ID: <3723299.113919.1345459955655.JavaMail.trustmail@ss000807>\n" +
                "Accept-Language: de-CH, en-US\n" +
                "Content-Language: de-DE\n" +
                "Content-Type: multipart/signed; protocol=\"application/pkcs7-signature\"; micalg=sha1; \n" +
                "\tboundary=\"----=_Part_113918_874669.1345459955655\"\n" +
                "MIME-Version: 1.0\n" +
                "\n" +
                "------=_Part_113918_874669.1345459955655\n" +
                "Content-Type: text/plain; charset=\"iso-8859-1\"\n" +
                "Content-Transfer-Encoding: 7bit\n" +
                "Content-Language: de-DE\n" +
                "\n" +
                "inetnum:        217.193.127.137 - 217.193.127.137\n" +
                "netname:        NET-NAME\n" +
                "descr:          Bogus\n" +
                "descr:          Address\n" +
                "country:        CH\n" +
                "admin-c:        TEST-RIPE\n" +
                "tech-c:         TEST-RIPE\n" +
                "status:         ASSIGNED PA\n" +
                "mnt-by:         TEST-MNT\n" +
                "source:         RIPE\n" +
                "\n" +
                "mntner: TEST-MNT\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "------=_Part_113918_874669.1345459955655\n" +
                "Content-Type: application/pkcs7-signature; name=smime.p7s; smime-type=signed-data\n" +
                "Content-Transfer-Encoding: base64\n" +
                "Content-Disposition: attachment; filename=\"smime.p7s\"\n" +
                "Content-Description: S/MIME Cryptographic Signature\n" +
                "\n" +
                "MIAGCSqGSIb3DQEHAqCAMIACAQExCzAJBgUrDgMCGgUAMIAGCSqGSIb3DQEHAQAAoIIQ/TCCBVkw\n" +
                "rJme/XmWwocAAAAAAAA=\n" +
                "------=_Part_113918_874669.1345459955655--", updateContext);

        assertThat(result.getId(), is("<3723299.113919.1345459955655.JavaMail.trustmail@ss000807>"));
        assertThat(result.getReplyTo(), is("Registration.Ripe@company.com"));
        assertThat(result.getKeyword(), is(Keyword.NONE));

        List<ContentWithCredentials> contentWithCredentialsList = result.getContentWithCredentials();
        assertThat(contentWithCredentialsList, hasSize(1));
        assertThat(contentWithCredentialsList.get(0).getContent(), is("" +
                "inetnum:        217.193.127.137 - 217.193.127.137\n" +
                "netname:        NET-NAME\n" +
                "descr:          Bogus\n" +
                "descr:          Address\n" +
                "country:        CH\n" +
                "admin-c:        TEST-RIPE\n" +
                "tech-c:         TEST-RIPE\n" +
                "status:         ASSIGNED PA\n" +
                "mnt-by:         TEST-MNT\n" +
                "source:         RIPE\n\n" +
                "mntner: TEST-MNT\n\n\n\n"));
        assertThat(contentWithCredentialsList.get(0).getCredentials(), hasSize(1));
        assertThat(contentWithCredentialsList.get(0).getCredentials().get(0), is(instanceOf(X509Credential.class)));
    }

    @Test
    public void parse_smime_multipart_text_plain_no_headers_in_content() throws Exception {
        final MailMessage result = subject.parse(MimeMessageProvider.getUpdateMessage("multipartPkcs7Signed.mail"), updateContext);

        final String expectedValue = "first line\nsecond line\nthird line\n";
        assertThat(result.getSubject(), is("Bogus"));
        assertThat(result.getFrom(), is("Registration.Ripe@company.com"));
        assertThat(result.getUpdateMessage(), is(expectedValue));

        List<ContentWithCredentials> contentWithCredentialsList = result.getContentWithCredentials();
        assertThat(contentWithCredentialsList, hasSize(1));
        assertThat(contentWithCredentialsList.get(0).getContent(), is(expectedValue));
        assertThat(contentWithCredentialsList.get(0).getCredentials(), hasSize(1));
        assertThat(contentWithCredentialsList.get(0).getCredentials().get(0), is(instanceOf(X509Credential.class)));
    }

    @Test
    public void parse_smime_multipart_alternative() throws Exception {
        final MailMessage mailMessage = subject.parse(
                "Message-ID: <28483859.46585.1352362093823.JavaMail.trustmail@ss000807>\n" +
                "MIME-Version: 1.0\n" +
                "Content-Type: multipart/signed; protocol=\"application/pkcs7-signature\"; micalg=sha1; \n" +
                "\tboundary=\"----=_Part_46584_13090458.1352362093823\"\n" +
                "From: <Registration.Ripe@company.com>\n" +
                "To: <auto-dbm@ripe.net>\n" +
                "Subject: IP-Request for FOO\n" +
                "Date: Thu, 8 Nov 2012 08:08:07 +0000\n" +
                "Accept-Language: de-CH, en-US\n" +
                "Content-Language: de-DE\n" +
                "\n" +
                "------=_Part_46584_13090458.1352362093823\n" +
                "Content-Type: multipart/alternative;\n" +
                "\tboundary=\"_000_54F98DDECB052B4A98783EDEF1B77C4C30649470SG000708corproo_\"\n" +
                "Content-Language: de-DE\n" +
                "\n" +
                "--_000_54F98DDECB052B4A98783EDEF1B77C4C30649470SG000708corproo_\n" +
                "Content-Type: text/plain; charset=\"us-ascii\"\n" +
                "Content-Transfer-Encoding: quoted-printable\n" +
                "\n" +
                "inetnum: 217.193.204.248 - 217.193.204.255\n" +
                "netname: TEST-NET\n" +
                "descr: FOO\n" +
                "descr: BAR\n" +
                "country: CH\n" +
                "admin-c: TEST-RIPE\n" +
                "tech-c: TEST-RIPE\n" +
                "status: ASSIGNED PA\n" +
                "mnt-by: TEST-MNT\n" +
                "source: RIPE\n" +
                "\n" +
                "\n" +
                "--_000_54F98DDECB052B4A98783EDEF1B77C4C30649470SG000708corproo_\n" +
                "Content-Type: text/html; charset=\"us-ascii\"\n" +
                "Content-Transfer-Encoding: quoted-printable\n" +
                "\n" +
                "<html>\n" +
                "<head>\n" +
                "<meta http-equiv=3D\"Content-Type\" content=3D\"text/html; charset=3Dus-ascii\"=\n" +
                ">\n" +
                "<meta name=3D\"Generator\" content=3D\"Microsoft Exchange Server\">\n" +
                "<!-- converted from rtf -->\n" +
                "<style><!-- .EmailQuote { margin-left: 1pt; padding-left: 4pt; border-left:=\n" +
                " #800000 2px solid; } --></style>\n" +
                "</head>\n" +
                "<body>\n" +
                "<font face=3D\"Bogus\" size=3D\"2\"><span style=3D\"font-size:10pt;\">\n" +
                "<div>inetnum: 217.193.204.248 - 217.193.204.255</div>\n" +
                "<div>netname: TEST-NET</div>\n" +
                "<div>descr: FOO</div>\n" +
                "<div>descr: BAR</div>\n" +
                "<div>country: CH</div>\n" +
                "<div>admin-c: TEST-RIPE</div>\n" +
                "<div>tech-c: TEST-RIPE</div>\n" +
                "<div>status: ASSIGNED PA</div>\n" +
                "<div>mnt-by: TEST-MNT</div>\n" +
                "<div>source: RIPE</div>\n" +
                "<div><font face=3D\"Calibri\" size=3D\"2\"><span style=3D\"font-size:11pt;\">&nbs=\n" +
                "p;</span></font></div>\n" +
                "</span></font>\n" +
                "</body>\n" +
                "</html>\n" +
                "\n" +
                "--_000_54F98DDECB052B4A98783EDEF1B77C4C30649470SG000708corproo_--\n" +
                "\n" +
                "------=_Part_46584_13090458.1352362093823\n" +
                "Content-Type: application/pkcs7-signature; name=smime.p7s; smime-type=signed-data\n" +
                "Content-Transfer-Encoding: base64\n" +
                "Content-Disposition: attachment; filename=\"smime.p7s\"\n" +
                "Content-Description: S/MIME Cryptographic Signature\n" +
                "\n" +
                "MIAGCSqGSIb3DQEHAqCAMIACAQExCzAJBgUrDgMCGgUAMIAGCSqGSIb3DQEHAQAAoIIQ/TCCBVkw\n" +
                "NuXtlQgW7sAAAAAAAAA=\n" +
                "------=_Part_46584_13090458.1352362093823--\n",
                        updateContext);

        final List<ContentWithCredentials> contentWithCredentialsList = mailMessage.getContentWithCredentials();
        assertThat(contentWithCredentialsList, hasSize(1));
        assertThat(contentWithCredentialsList.get(0).getContent(), is("" +
                "inetnum: 217.193.204.248 - 217.193.204.255\n" +
                "netname: TEST-NET\n" +
                "descr: FOO\n" +
                "descr: BAR\n" +
                "country: CH\n" +
                "admin-c: TEST-RIPE\n" +
                "tech-c: TEST-RIPE\n" +
                "status: ASSIGNED PA\n" +
                "mnt-by: TEST-MNT\n" +
                "source: RIPE\n\n"));
        assertThat(contentWithCredentialsList.get(0).getCredentials(), hasSize(1));
        assertThat(contentWithCredentialsList.get(0).getCredentials().get(0), is(instanceOf(X509Credential.class)));
    }

    @Test
    public void illegal_charset() throws Exception {
        assertThat(subject.getCharset(new ContentType("text/plain;\n\tcharset=\"_iso-2022-jp$ESC\"")), is(StandardCharsets.ISO_8859_1));
    }

    @Test
    public void parse_signed_message_missing_crc_check() throws Exception {
        final MailMessage message = subject.parse((
                            "To: auto-dbm@ripe.net\n" +
                            "From: No Reply <noreply@ripe.net>\n" +
                            "Date: Thu, 30 Mar 2017 09:00:00 +0100\n" +
                            "MIME-Version: 1.0\n" +
                            "Content-Type: text/plain; charset=windows-1252\n" +
                            "Content-Transfer-Encoding: 7bit\n" +
                            "\n" +
                            "\n" +
                            "-----BEGIN PGP SIGNED MESSAGE-----\n" +
                            "Hash: SHA1\n" +
                            "\n" +
                            "aut-num:        AS3333\n" +
                            "remarks:        +---------------------\n" +
                            "----------------------+\n" +
                            "mnt-by:         TEST-MNT\n" +
                            "source:         TEST\n" +
                            "\n" +
                            "\n" +
                            "-----BEGIN PGP SIGNATURE-----\n" +
                            "Version: GnuPG v2\n" +
                            " \n" +
                            "iEYEARECAAYFAljcrkEACgkQYAnVsDBumXz9bwCfZQAm+4e7bbXztKzgwjGpjBQs\n" +
                            "cYEAn3pN9uN9zhFrph7Co4tZ00aw/7TG\n" +
                            "=NjUf\n" +
                            "-----END PGP SIGNATURE-----\n" +
                            "\n" +
                            "\n" +
                            "\n"),
                updateContext);

        assertThat(message.getContentWithCredentials(), hasSize(1));
        final ContentWithCredentials contentWithCredentials = message.getContentWithCredentials().get(0);
        assertThat(contentWithCredentials.getCredentials(), hasSize(0));
    }


    @Test
    public void parse_failure_message() throws Exception {
        final MailMessage mailMessage = subject.parse(MimeMessageProvider.getUpdateMessage("testParseFailure.mail"), updateContext);

        assertThat(mailMessage.getUpdateMessage(), is(not(emptyString())));
    }

}
