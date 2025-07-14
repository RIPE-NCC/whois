package net.ripe.db.whois.api.log;

import net.ripe.db.whois.api.SecureRestTest;
import net.ripe.db.whois.api.httpserver.AbstractHttpsIntegrationTest;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;

@Tag("IntegrationTest")
public class JettyRequestNonTrustedHttpsTestIntegration extends AbstractHttpsIntegrationTest {

    private static final RpslObject OWNER_MNT = RpslObject.parse("" +
            "mntner:        OWNER-MNT\n" +
            "descr:         Owner Maintainer\n" +
            "admin-c:       TP1-TEST\n" +
            "upd-to:        noreply@ripe.net\n" +
            "auth:          MD5-PW $1$GUKzBg/F$PoCZBbhTNxCKM3K9VF8y60\n" + // #team-red4321
            "mnt-by:        OWNER-MNT\n" +
            "source:        TEST");

    private static final RpslObject TEST_PERSON = RpslObject.parse("" +
            "person:        Test Person\n" +
            "address:       Singel 258\n" +
            "phone:         +31 6 12345678\n" +
            "nic-hdl:       TP1-TEST\n" +
            "mnt-by:        OWNER-MNT\n" +
            "source:        TEST");

    private final StringWriter stringWriter = new StringWriter();

    @BeforeEach
    public void setup() {
        databaseHelper.addObjects(OWNER_MNT, TEST_PERSON);
        addLog4jAppender();
    }

    @BeforeAll
    public static void beforeClass() {
        System.setProperty("ipranges.trusted", "193.0.20.1");
    }

    @AfterAll
    public static void clearProperty() {
        System.clearProperty("ipranges.trusted");
    }

    @AfterEach
    public void tearDown() {
        removeLog4jAppender();
    }


    @Test
    public void log_request_ignore_client_ip_non_trusted_source() {
        SecureRestTest.target(getSecurePort(), "whois/test/person/TP1-TEST?clientIp=10.20.30.40")
                .request()
                .get(WhoisResources.class);

        assertThat(getRequestLog(), startsWith("127.0.0.1"));
    }
}
