package net.ripe.db.whois.api.mail.dequeue;

import com.google.common.base.Charsets;
import net.ripe.db.whois.api.MimeMessageProvider;
import net.ripe.db.whois.api.mail.MailMessage;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.update.domain.*;
import net.ripe.db.whois.update.log.LoggerContext;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.mail.MessagingException;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MessageParserTest {
    @Mock MimeMessage mimeMessage;
    @Mock UpdateContext updateContext;
    @Mock LoggerContext loggerContext;
    @InjectMocks MessageParser subject;

    @Before
    public void setUp() throws Exception {
        when(mimeMessage.getContentType()).thenReturn("text/plain");
        when(mimeMessage.getContent()).thenReturn("1234");
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

        assertThat(result.getDate(), is("Mon, 28 May 2012 00:04:45 +0200"));
    }

    @Test
    public void parse_set_default_date() throws Exception {
        when(mimeMessage.getSubject()).thenReturn("NEW");

        final MailMessage message = subject.parse(mimeMessage, updateContext);

        assertThat(message.getDate().length(), not(is(0)));
        final String timezone = DateTimeFormat.forPattern("zzz").print(new DateTime());
        assertThat(message.getDate(), containsString(timezone));
        final String year = DateTimeFormat.forPattern("yyyy").print(new DateTime());
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
        MimeMessage messageWithInvalidReplyTo = new MimeMessage(null, new ByteArrayInputStream("Reply-To: <respondera: ventas@amusing.cl>".getBytes()));

        MailMessage result = subject.parse(messageWithInvalidReplyTo, updateContext);

        assertTrue(StringUtils.isBlank(result.getReplyTo()));
    }

    @Test
    public void parse_missing_reply_to() throws Exception {
        MimeMessage messageWithoutReplyTo = new MimeMessage(null, new ByteArrayInputStream("From: minimal@mailclient.org".getBytes()));

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
                "changed: changed@ripe.net 20120528\n" +
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
                "changed: changed@ripe.net 20120528\n" +
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
                "changed:test@foo.com 20120824\n" +
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
        final String expectedValue = "" +
                "-----BEGIN PGP SIGNED MESSAGE-----\n" +
                "Hash: SHA1\n" +
                "\n" +
                "route:        194.39.132.0/24\n" +
                "descr:        Description\n" +
                "origin:       AS12510\n" +
                "notify:       foo@bar.com\n" +
                "mnt-by:       BOGUS-MNT\n" +
                "mnt-by:       T8-MNT\n" +
                "changed:      nm@bogus.com 20120504\n" +
                "changed:      nm@bogus.com 20120529\n" +
                "source:       RIPE\n" +
                "delete:       no longer required for AS12510\n" +
                "-----BEGIN PGP SIGNATURE-----\n" +
                "Version: GnuPG v1.4.9 (SunOS)\n" +
                "\n" +
                "iEYEARECAAYFAk/FbSMACgkQsAWoDcAb7KJmJgCfe2PjxUFIeHycZ85jteosU1ez\n" +
                "kL0An3ypg8F75jlPyTYIUuiCQEcP/9sz\n" +
                "=j7tD\n" +
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
                "changed:         noreply@ripe.net 20121001\n" +
                "source:          TEST";

        assertThat(message.getUpdateMessage(), is(expectedValue));

        List<ContentWithCredentials> contentWithCredentials = message.getContentWithCredentials();
        assertThat(contentWithCredentials, hasSize(1));
        assertThat(contentWithCredentials.get(0).getContent(), is(expectedValue));
        assertTrue(contentWithCredentials.get(0).getCredentials().get(0) instanceof PgpCredential);
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
                "changed:     test@bogus.fr 20120531\n" +
                "source:      RIPE\n\n\n" +
                "as-set:      AS1:AS-FOO-BOGUS\n" +
                "descr:       Description\n" +
                "tech-c:      P1-RIPE\n" +
                "admin-c:     P1-RIPE\n" +
                "mnt-by:      TEST-MNT\n" +
                "mnt-by:      TEST-MNT\n" +
                "changed:     test@bogus.fr 20120531\n" +
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

        final String expectedValue = "" +
                "-----BEGIN PGP SIGNED MESSAGE-----\n" +
                "\n" +
                "route:          165.251.168.0/22\n" +
                "descr:          Description\n" +
                "origin:         AS1\n" +
                "notify:         ripe-admin@foo.net\n" +
                "mnt-by:         TEST-MNT\n" +
                "changed:        john@doe.net 20120530\n" +
                "source:         RIPE\n" +
                "\n" +
                "-----BEGIN PGP SIGNATURE-----\n" +
                "Version: PGP 6.5.8\n" +
                "\n" +
                "iQCVAwUBT8amRPVKpQwG/7ZlAQFM0gP+N3d2N2IivRzte0o6bvU3nqN84yGC4l3r\n" +
                "zeZKi7dvU3R2betF8IElvL4x/bpBPAHXQWO+QaYMg3Yz6HCBKLJwMFgyWbmcJtD0\n" +
                "zL1HUOJmGyNv/eFjNSMgfpeZEsPZ3R+Pz9gSjEAW5aAj1wLdpXvVK9rYOQPc3TVc\n" +
                "z2xhfX4BqpQ=\n" +
                "=ODtr\n" +
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
        final MimeMessage message = getMessage("" +
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
                "changed: denis@ripe.net 20121016\n" +
                "source:  TEST\n" +
                "\n" +
                "\n" +
                "--Apple-Mail=_40C18EAF-8C7D-479F-9001-D91F1181EEDA\n" +
                "Content-Transfer-Encoding: 7bit\n" +
                "Content-Type: text/html;\n" +
                "\tcharset=us-ascii\n" +
                "\n" +
                "<html><head></head><body style=\"word-wrap: break-word; -webkit-nbsp-mode: space; -webkit-line-break: after-white-space; \"><div style=\"font-size: 13px; \"><b>person: &nbsp;First Person</b></div><div style=\"font-size: 13px; \"><b>address: St James Street</b></div><div style=\"font-size: 13px; \"><b>address: Burnley</b></div><div style=\"font-size: 13px; \"><b>address: UK</b></div><div style=\"font-size: 13px; \"><b>phone: &nbsp; +44 282 420469</b></div><div style=\"font-size: 13px; \"><b>nic-hdl: FP1-TEST</b></div><div style=\"font-size: 13px; \"><b>mnt-by: &nbsp;OWNER-MNT</b></div><div style=\"font-size: 13px; \"><b>changed: <a href=\"mailto:denis@ripe.net\">denis@ripe.net</a> 20121016</b></div><div style=\"font-size: 13px; \"><b>source: &nbsp;TEST</b></div><div><br></div></body></html>\n" +
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
                "--Apple-Mail=_8CAC1D90-3ABC-4010-9219-07F34D68A205--");

        final MailMessage mailMessage = subject.parse(message, updateContext);

        assertThat(mailMessage.getContentWithCredentials(), hasSize(1));
        final ContentWithCredentials contentWithCredentials = mailMessage.getContentWithCredentials().get(0);
        assertThat(contentWithCredentials.getCredentials(), hasSize(1));
        assertTrue(contentWithCredentials.getCredentials().get(0) instanceof PgpCredential);
        assertThat(contentWithCredentials.getContent(), is("" +
                "person:  First Person\n" +
                "address: St James Street\n" +
                "address: Burnley\n" +
                "address: UK\n" +
                "phone:   +44 282 420469\n" +
                "nic-hdl: FP1-TEST\n" +
                "mnt-by:  OWNER-MNT\n" +
                "changed: denis@ripe.net 20121016\n" +
                "source:  TEST\n\n"));
    }

    @Test
    public void parse_smime_multipart_text_plain() throws Exception {
        final MimeMessage message = getMessage("" +
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
                "changed:        email@foobar.net 20120312\n" +
                "source:         RIPE\n" +
                "\n" +
                "changed: email@foobar.net\n" +
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
                "ggNBoAMCAQICECYdlHUPbJ2C1O/M47kPYTowDQYJKoZIhvcNAQEFBQAwZDELMAkGA1UEBhMCY2gx\n" +
                "ETAPBgNVBAoTCFN3aXNzY29tMSUwIwYDVQQLExxEaWdpdGFsIENlcnRpZmljYXRlIFNlcnZpY2Vz\n" +
                "MRswGQYDVQQDExJTd2lzc2NvbSBSb290IENBIDEwHhcNMDYwMjIzMDk1MzEyWhcNMTYwMjIzMDk1\n" +
                "MzEyWjBlMQswCQYDVQQGEwJjaDERMA8GA1UEChMIU3dpc3Njb20xJTAjBgNVBAsTHERpZ2l0YWwg\n" +
                "Q2VydGlmaWNhdGUgU2VydmljZXMxHDAaBgNVBAMTE1N3aXNzY29tIFJ1YmluIENBIDEwggEiMA0G\n" +
                "CSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDGWTjNZ38DiwRxLNBue9D2Bn85oUpE1lIpRjp2qm3P\n" +
                "BE+K9/smk2OvKBJ9PlLsROK4t3Hsc7De+BkG/f3eyyR9+4Acvx36nv4/74rS3+GNTGIFBIhcpjyY\n" +
                "s7w+TJNHAG7Pdok7hLUst6PwCU16YNcRdUIbDBWknzVs6hqsyYJRlzzYGEs9UuwvpQJUXE/gCxc0\n" +
                "x5zUyCFtZZjuQ32v+8ID7KWNvdp8HlOv8EEC/XrR/NoFq4CX5fHaJQf7YwMbkeM3/55za0cD8rDt\n" +
                "S4fRekIfeRJM5W+5vCEXzjJ1e+UoBrLzYxjZlgwEA/UXut1ulQHlKB88qDTJ+lLVg1JuAUGTAgMB\n" +
                "AAGjggEEMIIBADBABggrBgEFBQcBAQQ0MDIwMAYIKwYBBQUHMAKGJGh0dHA6Ly93d3cuc3dpc3Nk\n" +
                "aWdpY2VydC5jaC9kb3dubG9hZDASBgNVHRMBAf8ECDAGAQH/AgEAMBMGA1UdIAQMMAowCAYGYIV0\n" +
                "AVMEMEMGA1UdHwQ8MDowOKA2oDSGMmh0dHA6Ly93d3cuc3dpc3NkaWdpY2VydC5jaC9kb3dubG9h\n" +
                "ZC9zZGNzLXJvb3QuY3JsMA4GA1UdDwEB/wQEAwIBBjAdBgNVHQ4EFgQULcKno2M+P4NHq0gzNoGF\n" +
                "99TprMAwHwYDVR0jBBgwFoAUAyUv3m+CATpcLNwroWm1Z9SM0/0wDQYJKoZIhvcNAQEFBQADggIB\n" +
                "ALXafISxFtQcm29o2r5cMUTRoUroEcdITEFjKa8ZQZf63JpHZYWQrziklNcvJOt5TmWtUsUc9KQK\n" +
                "y8AQN6m4AJR+Ol/7U8+3IFaqyRt9Bp38iaPv3AZSHU4Uf83UhW6zI/SlvXZxuKMkFP+l08eJCIFo\n" +
                "HHdHVUi1fzN9B15s3qFnSlCsNHWB3A5j81P8bKc7QdpIj4pUte6QTZd4ESchKEiUNzzqawVfVCW8\n" +
                "Btmq5xMv2r1cn34pBAjBGF/p3AE30DH5he4Yqn3xhnvBMOhHGbkoeaR5XXQKpvMmFSYo9eH2Yj6h\n" +
                "mQ/1R27eqdjaR9eGjbqJCz5HU2kiSgceI2YZgafoFjhlrPuq/7V4ZVKJbf7/eKqRWVuUh7/6wUsJ\n" +
                "lqiXtEIjM0R7hD0kjQzvOCAthV3byvB/V5VIzEViKRp0DQbNRGhog9nGP13xE4FF6xTMxFP7urip\n" +
                "elCDWc9Yc/pp50Vi7f0isynOC3BRjM0WXrHJumu83zDxzxNYUV12XIg6CL/SasRJpEheTsc9LcBU\n" +
                "3pxcsVQfxAl4SQG6nbdLUc8EwpCZs0fGxnRSstJ6394tIk8VqI1Dr/B8/a709t2rOptea4MgpEo7\n" +
                "BikJvbyRP8DPobdmQu4R66QOFdhlfZUWvxTZJvv0wXaKycYyNO8e3lpAnU2gQ4WJjnG9I9zynDLr\n" +
                "MIIFvzCCBKegAwIBAgIQGm9r2E0WLw04jBcdooo7hTANBgkqhkiG9w0BAQUFADBlMQswCQYDVQQG\n" +
                "EwJjaDERMA8GA1UEChMIU3dpc3Njb20xJTAjBgNVBAsTHERpZ2l0YWwgQ2VydGlmaWNhdGUgU2Vy\n" +
                "dmljZXMxHDAaBgNVBAMTE1N3aXNzY29tIFJ1YmluIENBIDEwHhcNMTEwODEyMTI1NDUyWhcNMTQw\n" +
                "ODEyMTI1NDUyWjCBpTENMAsGA1UEBwwEQmVybjELMAkGA1UECAwCQkUxDTALBgNVBAsMBFNDSVMx\n" +
                "IDAeBgNVBAoMF1N3aXNzY29tIElUIFNlcnZpY2VzIEFHMQswCQYDVQQGEwJDSDEaMBgGA1UEAwwR\n" +
                "UmVnaXN0cmF0aW9uIFJpcGUxLTArBgkqhkiG9w0BCQEWHnJlZ2lzdHJhdGlvbi5yaXBlQHN3aXNz\n" +
                "Y29tLmNvbTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAID/R0iVboEhBcA9loz1DW2z\n" +
                "2ZAb2mlDoKGSW6wXK/Eu77AvRYWwjwqUBaRA1bLcoLiHt/q6f6b1jOc5YZqdMJ+s09gVcmYmvjMr\n" +
                "G6FCW/hM+/JjgovowwZbZcmQONJDYjAGL8YU23IFFy1mK0B0ZyYA2LeFGkHmqX1dhXk4zm34/XXs\n" +
                "9GZylRBAOH4qa2KviV+2VdufVl12u14aD6KaogCZhkAPCW8MTdZ2ZGNOu984KQMie4yHeFBM05/S\n" +
                "vMNs7ND9mPx67r1iJM4samc7KJx3VYa75427Hfr4Y9F6q9MRAB5wMirx1a3TS880NpWF5UoZpCNJ\n" +
                "Gr1L3Y/shbEGRJ8CAwEAAaOCAigwggIkMH8GCCsGAQUFBwEBBHMwcTAuBggrBgEFBQcwAYYiaHR0\n" +
                "cDovL29jc3Auc3dpc3NkaWdpY2VydC5jaC9ydWJpbjA/BggrBgEFBQcwAoYzaHR0cDovL3d3dy5z\n" +
                "d2lzc2RpZ2ljZXJ0LmNoL2Rvd25sb2FkL3NkY3MtcnViaW4uY3J0MB8GA1UdIwQYMBaAFC3Cp6Nj\n" +
                "Pj+DR6tIMzaBhffU6azAMEgGA1UdIARBMD8wPQYGYIV0AVMEMDMwMQYIKwYBBQUHAgEWJWh0dHA6\n" +
                "Ly93d3cuc3dpc3NkaWdpY2VydC5jaC9kb2N1bWVudHMwgbwGA1UdHwSBtDCBsTB0oHKgcIZubGRh\n" +
                "cDovL2xkYXAuc3dpc3NkaWdpY2VydC5jaC9DTj1Td2lzc2NvbSBSdWJpbiBDQSAxLGRjPXJ1Ymlu\n" +
                "LGRjPXN3aXNzZGlnaWNlcnQsZGM9Y2g/Y2VydGlmaWNhdGVSZXZvY2F0aW9uTGlzdD8wOaA3oDWG\n" +
                "M2h0dHA6Ly93d3cuc3dpc3NkaWdpY2VydC5jaC9kb3dubG9hZC9zZGNzLXJ1YmluLmNybDAdBgNV\n" +
                "HSUEFjAUBggrBgEFBQcDAgYIKwYBBQUHAwQwDgYDVR0PAQH/BAQDAgSwMCkGA1UdEQQiMCCBHnJl\n" +
                "Z2lzdHJhdGlvbi5yaXBlQHN3aXNzY29tLmNvbTAdBgNVHQ4EFgQUI/zu8XKWjoVtr4oMzM1hkaP/\n" +
                "BCswDQYJKoZIhvcNAQEFBQADggEBAHaM6iCWIZJDYTBy+3k2ER4RhXTk1ksPIEg6a6Sp0UBNXDyI\n" +
                "Z3CfeDOheNuHC95psPryyuvFQUhzki/LYIYxYobwqsaO3CwbO07iABWmCOcEpuJSA8FdZ/BwnEGS\n" +
                "EIv3/a/ve/nRC4dXz1WL9r7Y/UBFD0+m2LSDme1Awmsk0ri0RQYlEXkTb3zAklPHKclb1yXRzmmN\n" +
                "i122nPl0Ax3GhfqFJY9gUmsb28QVDvLHnax/C+IsxQhIAi9zmgrTixGKOEUSECYKN9v1ug60yeqi\n" +
                "SFIKTXI2iZp6WY74HGQRGk5NRmnVFx5/69yFW27gk3vkuxHOsEJlDVOr8vAwgb5CYXowggXZMIID\n" +
                "waADAgECAhBcC4VcC+dZQd9XzD9/nag2MA0GCSqGSIb3DQEBBQUAMGQxCzAJBgNVBAYTAmNoMREw\n" +
                "DwYDVQQKEwhTd2lzc2NvbTElMCMGA1UECxMcRGlnaXRhbCBDZXJ0aWZpY2F0ZSBTZXJ2aWNlczEb\n" +
                "MBkGA1UEAxMSU3dpc3Njb20gUm9vdCBDQSAxMB4XDTA1MDgxODEyMDYyMFoXDTI1MDgxODIyMDYy\n" +
                "MFowZDELMAkGA1UEBhMCY2gxETAPBgNVBAoTCFN3aXNzY29tMSUwIwYDVQQLExxEaWdpdGFsIENl\n" +
                "cnRpZmljYXRlIFNlcnZpY2VzMRswGQYDVQQDExJTd2lzc2NvbSBSb290IENBIDEwggIiMA0GCSqG\n" +
                "SIb3DQEBAQUAA4ICDwAwggIKAoICAQDQubCoDNm7PyH4G9Uzk4AWZSB1sj2bYG1GyIwxbxfD+pps\n" +
                "Vu08xZFXw82rlkmQKhlLHqNtV93xK2IodUVeqtZb+gsl2KEW+RzELuaVKmfM0CluPIU0OGFJsQCf\n" +
                "1jpxX01tzl+5qeSJf2pS+sqb8typ+Z2ZRz9OKV+0po1dewuZEQMD/ufb26P/HaXNkB4BHzWwfwDb\n" +
                "kG/GfnvR7np6p6oMV2+kbcUTO7Cl2e0yHLReZ4tU3HOH5dMXfGZQcl3UGljB2c/YiQJvp0m0Nl3Q\n" +
                "pN4HLLZ1tyiR1pe+KPWYHupbJsm9sJdz2q6RJutowfk5FdZnSwptT8vPsORCcYxTeefu4dsdoG4d\n" +
                "jBp3NVwWHitTHzSL0Wz88mcHevWt7daaq6GxS+HMN1/9f81NrrgfnEP5KlhVQ0W8ls1wDvzJ42a6\n" +
                "To07gcsVZHu5lOhdM1KFcS5PjqIGEVHJ48uhbjEIZAzC0jz1NujX0A54IyCRySQqZSlbIvchzoNe\n" +
                "pPPeS9Noj0Z1XIMJbilrxHCM9Z3XIC//RtIrOMIvdRw9ftql7x5ghWlC08z4Y/4eQzmFprZjQRCz\n" +
                "cx680/rKfRZH4qfV0KOKCgiWYlZuNNvZArkwdeME0uePwrARQAqs1XECYosxvt3GI1gxQkMtdPnG\n" +
                "nqaKD+n+v4PmQ1ckuu9GNKrXEgE47QIDAQABo4GGMIGDMA4GA1UdDwEB/wQEAwIBhjAdBgNVHSEE\n" +
                "FjAUMBIGB2CFdAFTAAEGB2CFdAFTAAEwEgYDVR0TAQH/BAgwBgEB/wIBBzAfBgNVHSMEGDAWgBQD\n" +
                "JS/eb4IBOlws3CuhabVn1IzT/TAdBgNVHQ4EFgQUAyUv3m+CATpcLNwroWm1Z9SM0/0wDQYJKoZI\n" +
                "hvcNAQEFBQADggIBADUQy+ymBA0ND83A26uo8oiXDN+TL018QFYxeuukD2DNevO+wyeOAz6k3RLv\n" +
                "fh50Bjw/MfIce5ExIbTw0GyX1OmXsiRWHlbDNb2IBQ9bEBpk4ceCMPkyrZ5QLOd4BdAxsVqYinVO\n" +
                "kFxqFCrgUkeCYOYe2oGx+xQLWvGf0pW6PtAb1hUdo76G1dsPwElkuy5QGUvSJPjdHgdW0DiglXAg\n" +
                "dozX3R7en3HEI++DE1yjJBVNKUA8asSp2LemRKUN9OCddx5AcCb82tk25HnktT+8m2W+uxGWz9vG\n" +
                "KDk6CM5HW1NaxZn+Xand70zUxqWtAuaMBxIebwPRb6Cj8ym9EsdQorB/iKmZd5qxwKU5Llx8aeIs\n" +
                "sOo3aqThWuH1UOWD76W7KojnjNv9bV6XGah+ZnVrceq/scdvoPSOpOw0UVuMJgNwoXfVARJXADXb\n" +
                "I94OiiiZ/bEQb0v/OC1gTiyc62e1rUnuSx+sr/sNkFpmYHBdqs141CTuyEGgkwGSnGqe/LkkxbMV\n" +
                "gn6+rpUr67HA2uMBYAteaayEVmG+cRf+HRMP/saHRen+MqAaDROklFVxpRaLusqJsLLH/I/YVLWT\n" +
                "Yp3Oz1n7PRjOKss1FYJd/1QiW3FS+7fJ/mCbAEFk8Koq7LZCQ86JZoHIi585VAMl0xY1joTQX/ow\n" +
                "GvWabPQOU/k6W9EcMYIDTzCCA0sCAQEweTBlMQswCQYDVQQGEwJjaDERMA8GA1UEChMIU3dpc3Nj\n" +
                "b20xJTAjBgNVBAsTHERpZ2l0YWwgQ2VydGlmaWNhdGUgU2VydmljZXMxHDAaBgNVBAMTE1N3aXNz\n" +
                "Y29tIFJ1YmluIENBIDECEBpva9hNFi8NOIwXHaKKO4UwCQYFKw4DAhoFAKCCAaswGAYJKoZIhvcN\n" +
                "AQkDMQsGCSqGSIb3DQEHATAcBgkqhkiG9w0BCQUxDxcNMTIwODIwMTA1MjM1WjAjBgkqhkiG9w0B\n" +
                "CQQxFgQUZIBQXsg/8X8GMBoZ1TakgRgWyz4wNAYJKoZIhvcNAQkPMScwJTAKBggqhkiG9w0DBzAO\n" +
                "BggqhkiG9w0DAgICAIAwBwYFKw4DAgcwgYgGCSsGAQQBgjcQBDF7MHkwZTELMAkGA1UEBhMCY2gx\n" +
                "ETAPBgNVBAoTCFN3aXNzY29tMSUwIwYDVQQLExxEaWdpdGFsIENlcnRpZmljYXRlIFNlcnZpY2Vz\n" +
                "MRwwGgYDVQQDExNTd2lzc2NvbSBSdWJpbiBDQSAxAhAab2vYTRYvDTiMFx2iijuFMIGKBgsqhkiG\n" +
                "9w0BCRACCzF7MHkwZTELMAkGA1UEBhMCY2gxETAPBgNVBAoTCFN3aXNzY29tMSUwIwYDVQQLExxE\n" +
                "aWdpdGFsIENlcnRpZmljYXRlIFNlcnZpY2VzMRwwGgYDVQQDExNTd2lzc2NvbSBSdWJpbiBDQSAx\n" +
                "AhAab2vYTRYvDTiMFx2iijuFMA0GCSqGSIb3DQEBAQUABIIBAC13K/ZrP6tmTtSLshOjv0txup1u\n" +
                "VFpHvP5i1nU2ly/SR9BkB8MnN0bpMAvb/QM5VydRST9IFT9m4pdHRFpbfx+N8LGm7TBt9T2e3/mL\n" +
                "cj5lZrZUoNOlOeoMODR3HSoPl9NYPvCUKirP4H0jgFb9fxZSWIBvIg6CMbAyF2rE9aRMHKa13xte\n" +
                "WqgO1Ml0AIDQ0BnVPyLMF1BPtu4e4VCXWDbnwf714TBkS+Qx/8YkmzTEd5hkVA3A11b5JChrcRex\n" +
                "E362Cd8e5dhRKLV9FrbGZ3UPpnFcmuuQPjYCRP/eqDJzIfyoeJwEY4WrmZqGrTQNWz27t7Ov6214\n" +
                "rJme/XmWwocAAAAAAAA=\n" +
                "------=_Part_113918_874669.1345459955655--");

        final MailMessage result = subject.parse(message, updateContext);

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
                "changed:        email@foobar.net 20120312\n" +
                "source:         RIPE\n\n" +
                "changed: email@foobar.net\n\n\n\n"));
        assertThat(contentWithCredentialsList.get(0).getCredentials(), hasSize(1));
        assertTrue(contentWithCredentialsList.get(0).getCredentials().get(0) instanceof X509Credential);
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
        assertTrue(contentWithCredentialsList.get(0).getCredentials().get(0) instanceof X509Credential);
    }

    @Test
    public void parse_smime_multipart_alternative() throws Exception {
        final MimeMessage input = getMessage("" +
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
                "changed: email@foobar.net\n" +
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
                "<div>changed: email@foobar.net</div>\n" +
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
                "------=_Part_46584_13090458.1352362093823--\n");

        final MailMessage message = subject.parse(input, updateContext);

        List<ContentWithCredentials> contentWithCredentialsList = message.getContentWithCredentials();
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
                "changed: email@foobar.net\n" +
                "source: RIPE\n\n"));
        assertThat(contentWithCredentialsList.get(0).getCredentials(), hasSize(1));
        assertTrue(contentWithCredentialsList.get(0).getCredentials().get(0) instanceof X509Credential);
    }

    @Test
    public void illegal_charset() throws Exception {
        assertThat(subject.getCharset(new ContentType("text/plain;\n\tcharset=\"_iso-2022-jp$ESC\"")), is(Charsets.ISO_8859_1));
    }

    private MimeMessage getMessage(final String message) throws MessagingException, IOException {
        return new MimeMessage(null, new ByteArrayInputStream(message.getBytes()));
    }
}
