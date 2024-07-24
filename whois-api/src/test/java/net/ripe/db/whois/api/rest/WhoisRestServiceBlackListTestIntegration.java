package net.ripe.db.whois.api.rest;

import jakarta.ws.rs.core.Response;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.httpserver.WhoisBlackListFilter;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

import static net.ripe.db.whois.api.httpserver.JettyBootstrap.BLACK_LIST_JMX_NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@Tag("IntegrationTest")
public class WhoisRestServiceBlackListTestIntegration extends AbstractIntegrationTest {

    private static MBeanServer mBeanServer;

    private static ObjectName mBeanName;

    private static final RpslObject OWNER_MNT = RpslObject.parse("" +
            "mntner:      OWNER-MNT\n" +
            "descr:       Owner Maintainer\n" +
            "admin-c:     TP1-TEST\n" +
            "upd-to:      noreply@ripe.net\n" +
            "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
            "auth:        SSO person@net.net\n" +
            "mnt-by:      OWNER-MNT\n" +
            "source:      TEST");

    @BeforeAll
    public static void beforeClass() throws MalformedObjectNameException {
        final String unsupportedIps = "193.0.0.0 - 193.0.23.255, 2001:67c:2e8::/48";

        System.setProperty("ipranges.untrusted", unsupportedIps);

        mBeanServer = ManagementFactory.getPlatformMBeanServer();
        mBeanName = new ObjectName(BLACK_LIST_JMX_NAME);

        // Ensure the MBean is registered
        if (!mBeanServer.isRegistered(mBeanName)) {
            try {
                MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
                ObjectName name = new ObjectName(BLACK_LIST_JMX_NAME);
                WhoisBlackListFilter mbean = new WhoisBlackListFilter(unsupportedIps);
                mbs.registerMBean(mbean, name);
            } catch (Exception e) {
                //Do Nothing
            }
        }
    }

    @AfterAll
    public static void clear(){ System.clearProperty("ipranges.untrusted"); }

    @BeforeEach
    public void setup() {
        databaseHelper.addObject("person: Test Person\nnic-hdl: TP1-TEST");
        databaseHelper.addObject(OWNER_MNT);
    }

    @Test
    public void add_black_ipv4_then_429_too_many_requests() throws Exception {
        // Add IP to blacklist
        mBeanServer.invoke(mBeanName, "addBlackListAddress", new Object[]{"8.8.8.8"}, new String[]{String.class.getName()});

        final Response response = RestTest.target(getPort(), "whois/test/mntner/OWNER-MNT?clientIp=8.8.8.8")
                .request()
                .get();
        assertThat(HttpStatus.TOO_MANY_REQUESTS_429, is(response.getStatus()));
    }
}
