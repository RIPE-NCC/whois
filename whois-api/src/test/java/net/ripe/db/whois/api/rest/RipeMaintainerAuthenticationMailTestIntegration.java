package net.ripe.db.whois.api.rest;

import com.google.common.collect.Lists;
import jakarta.mail.internet.MimeMessage;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.MailUpdatesTestSupport;
import net.ripe.db.whois.common.domain.IpRanges;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.mail.MailSenderStub;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

@Tag("IntegrationTest")
public class RipeMaintainerAuthenticationMailTestIntegration extends AbstractIntegrationTest {
    //regardless the trusted range, email is never trusted.

    @Autowired IpRanges ipRanges;
    @Autowired MailUpdatesTestSupport mailUpdatesTestSupport;
    @Autowired MailSenderStub mailSenderStub;

    private static final String RPSL_PERSON_WITH_RIPE_MAINTAINER = "" +
            "person:    TEST Person\n" +
            "address:   Singel 258\n" +
            "e-mail:    ppalse@ripe.net\n" +
            "phone:     +311234567\n" +
            "mnt-by:    RIPE-NCC-HM-MNT\n" +
            "nic-hdl:   AUTO-1\n" +
            "source:    TEST\n" +
            "password:  emptypassword";

    @BeforeEach
    public void setup() throws Exception {
        databaseHelper.addObjects(Lists.newArrayList(
                RpslObject.parse(
                        "mntner:  RIPE-NCC-HM-MNT\n" +
                        "descr:   description\n" +
                        "admin-c: TEST-RIPE\n" +
                        "mnt-by:  RIPE-NCC-HM-MNT\n" +
                        "upd-to:  dbtest@ripe.net\n" +
                        "auth:    MD5-PW $1$/7f2XnzQ$p5ddbI7SXq4z4yNrObFS/0 # emptypassword" +
                        "source:  TEST"),
                RpslObject.parse(
                        "person:  Admin Person\n" +
                        "address: Admin Road\n" +
                        "address: Town\n" +
                        "address: UK\n" +
                        "phone:   +44 282 411141\n" +
                        "nic-hdl: TEST-RIPE\n" +
                        "mnt-by:  TST-MNT\n" +
                        "source:  TEST"),
                RpslObject.parse("" +
                        "mntner:  TST-MNT\n" +
                        "descr:   description\n" +
                        "admin-c: TEST-RIPE\n" +
                        "mnt-by:  TST-MNT\n" +
                        "upd-to:  dbtest@ripe.net\n" +
                        "auth:    MD5-PW $1$fU9ZMQN9$QQtm3kRqZXWAuLpeOiLN7. # update\n" +
                        "source:  TEST")));
    }

    @Test
    public void mail_update_from_outside_ripe_network() throws Exception {
        ipRanges.setTrusted("53.67.0.1");

        final String response = mailUpdatesTestSupport.insert("NEW", RPSL_PERSON_WITH_RIPE_MAINTAINER);

        final MimeMessage message = mailSenderStub.getMessage(response);

        assertThat(message.getContent().toString(), containsString("" +
                "***Error:   Authentication by RIPE NCC maintainers only allowed from within the\n" +
                "            RIPE NCC network"));
    }

    @Test
    public void mail_update_from_within_ripe_network() throws Exception {
        ipRanges.setTrusted("127.0.0.1", "::1");

        final String response = mailUpdatesTestSupport.insert("NEW", RPSL_PERSON_WITH_RIPE_MAINTAINER);

        final MimeMessage message = mailSenderStub.getMessage(response);

        assertThat(message.getContent().toString(), containsString("" +
                "***Error:   Authentication by RIPE NCC maintainers only allowed from within the\n" +
                "            RIPE NCC network"));
    }
}
