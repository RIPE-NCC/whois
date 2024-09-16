package net.ripe.db.whois.query.integration;

import com.amazonaws.proprot.Header;
import com.amazonaws.proprot.ProxyProtocol;
import com.amazonaws.proprot.ProxyProtocolSpec.AddressFamily;
import com.amazonaws.proprot.ProxyProtocolSpec.Command;
import com.amazonaws.proprot.ProxyProtocolSpec.TransportProtocol;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.QueryServer;
import net.ripe.db.whois.query.support.AbstractQueryIntegrationTest;
import net.ripe.db.whois.query.support.TestPersonalObjectAccounting;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@Tag("IntegrationTest")
public class WhoisProxyProtocolTestIntegration extends AbstractQueryIntegrationTest {

    @Autowired
    TestPersonalObjectAccounting testPersonalObjectAccounting;

    @BeforeAll
    public static void setSpringProfile() {
        System.setProperty("proxy.protocol.enabled", "true");
    }

    @AfterAll
    public static void resetSpringProfile() {
        System.clearProperty("proxy.protocol.enabled");
    }

    @BeforeEach
    public void startupWhoisServer() {
        databaseHelper.clearAclLimits();
        ipResourceConfiguration.reload();
        testPersonalObjectAccounting.resetAccounting();

        final RpslObject person = RpslObject.parse("person: ADM-TEST\naddress: address\nphone: +312342343\nmnt-by:RIPE-NCC-HM-MNT\nadmin-c: ADM-TEST\nnic-hdl: ADM-TEST\nsource: TEST");
        final RpslObject mntner = RpslObject.parse("mntner: RIPE-NCC-HM-MNT\nmnt-by: RIPE-NCC-HM-MNT\ndescr: description\nadmin-c: ADM-TEST\nsource: TEST");
        databaseHelper.addObjects(Lists.newArrayList(person, mntner));

        ipTreeUpdater.rebuild();
        queryServer.start();
    }

    @AfterEach
    public void shutdownWhoisServer() {
        queryServer.stop(true);
    }

    @Test
    public void test_ipv4_client_ip() throws UnknownHostException {
        InetAddress clientIp = InetAddress.getByName("12.34.56.78");

        send(clientIp, "ADM-TEST");
        assertThat(testPersonalObjectAccounting.getQueriedPersonalObjects(clientIp), is(1));
    }

    @Test
    public void test_ipv6_client_ip() throws UnknownHostException {
        InetAddress clientIp = InetAddress.getByName("ee80:aa00:bb00:cc00::");

        send(clientIp, "ADM-TEST");
        assertThat(testPersonalObjectAccounting.getQueriedPersonalObjects(clientIp), is(1));
    }

    private String send(final InetAddress clientIp, final String query) {
        try (final Socket socket = new Socket("localhost", queryServer.getPort());
             final OutputStream out = socket.getOutputStream();
             final InputStream in = socket.getInputStream()) {

            out.write(createProxyProtocolHeader(clientIp));
            out.write((query + "\r\n").getBytes(StandardCharsets.ISO_8859_1));
            out.flush();

            return IOUtils.toString(new InputStreamReader(in, StandardCharsets.ISO_8859_1));
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    private byte[] createProxyProtocolHeader(final InetAddress clientIp) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        final Header header = new Header();
        header.setCommand(Command.PROXY);
        header.setAddressFamily(clientIp instanceof Inet4Address? AddressFamily.AF_INET : AddressFamily.AF_INET6);
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
