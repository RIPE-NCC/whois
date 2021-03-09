package net.ripe.db.whois.nrtm.integration;

import com.amazonaws.proprot.Header;
import com.amazonaws.proprot.ProxyProtocol;
import com.amazonaws.proprot.ProxyProtocolSpec.AddressFamily;
import com.amazonaws.proprot.ProxyProtocolSpec.Command;
import com.amazonaws.proprot.ProxyProtocolSpec.TransportProtocol;
import com.amazonaws.util.IOUtils;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.dao.jdbc.DatabaseHelper;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.nrtm.NrtmServer;
import net.ripe.db.whois.query.acl.IpResourceConfiguration;
import net.ripe.db.whois.query.support.TestPersonalObjectAccounting;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@Category(IntegrationTest.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class NrtmProxyProtocolTestIntegration extends AbstractNrtmIntegrationBase {

    private static final RpslObject mntner = RpslObject.parse("" +
            "mntner: TEST-MNT\n" +
            "source: TEST");

    @Autowired
    private IpResourceConfiguration ipResourceConfiguration;
    @Autowired
    private TestPersonalObjectAccounting testPersonalObjectAccounting;

    @BeforeClass
    public static void beforeClass() {
        DatabaseHelper.addGrsDatabases("1-GRS");
        System.setProperty("nrtm.update.interval", "1");
        System.setProperty("nrtm.enabled", "true");
        System.setProperty("nrtm.import.sources", "1-GRS");
        System.setProperty("nrtm.import.enabled", "true");
        System.setProperty("proxy.protocol.enabled", "true");
    }

    @AfterClass
    public static void afterClass() {
        System.clearProperty("proxy.protocol.enabled");
    }

    @Before
    public void before() throws InterruptedException {
        databaseHelper.addObject(mntner);
        ipResourceConfiguration.reload();
        testPersonalObjectAccounting.resetAccounting();

        nrtmServer.start();
    }

    @After
    public void reset() {
        databaseHelper.getAclTemplate().update("DELETE FROM acl_denied");
        databaseHelper.getAclTemplate().update("DELETE FROM acl_event");

        nrtmServer.stop(true);
    }

    @Test
    public void acl_blocked() throws Exception {
        final InetAddress clientIp = InetAddress.getByName("1.2.3.4");
        databaseHelper.insertAclIpDenied("1.2.3.4/32");
        ipResourceConfiguration.reload();

        try (final Socket socket = new Socket("localhost", NrtmServer.getPort());
             final InputStream in = socket.getInputStream();
             final OutputStream out = socket.getOutputStream();) {

            out.write(createProxyProtocolHeader(clientIp));
            out.write("-g TEST:3:1-LAST\r\n".getBytes(StandardCharsets.ISO_8859_1));

            assertThat(IOUtils.toString(in).contains("TEST-MNT"), is(false));
        }


    }

    private byte[] createProxyProtocolHeader(final InetAddress clientIp) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        final Header header = new Header();
        header.setCommand(Command.PROXY);
        header.setAddressFamily(clientIp instanceof Inet4Address ? AddressFamily.AF_INET : AddressFamily.AF_INET6);
        header.setTransportProtocol(TransportProtocol.STREAM);
        header.setSrcAddress(clientIp.getAddress());
        header.setDstAddress(clientIp instanceof Inet4Address? InetAddress.getLocalHost().getAddress() : Inet6Address.getByName("::1").getAddress());
        header.setSrcPort(23);
        header.setDstPort(23);

        ProxyProtocol proxyProtocol = new ProxyProtocol();
        proxyProtocol.write(header, out);

        return out.toByteArray();
    }

}
