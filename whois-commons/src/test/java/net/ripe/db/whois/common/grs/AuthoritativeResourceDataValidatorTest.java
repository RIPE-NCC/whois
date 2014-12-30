package net.ripe.db.whois.common.grs;

import com.google.common.base.Splitter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Scanner;

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuthoritativeResourceDataValidatorTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthoritativeResourceDataValidatorTest.class);

    @Mock AuthoritativeResourceData authoritativeResourceData;
    AuthoritativeResourceDataValidator subject;

    @Before
    public void setUp() throws Exception {
        subject = new AuthoritativeResourceDataValidator(new String[] {"GRS1","GRS2","GRS3"}, authoritativeResourceData);

        final AuthoritativeResource resourceDataGrs1 = AuthoritativeResource.loadFromScanner(LOGGER, "GRS1", new Scanner("" +
                "GRS1||ipv4|192.0.0.0|3||allocated|\n" +
                "GRS1||ipv4|193.0.0.10|6||allocated|\n" +
                "GRS1||ipv4|10.0.0.0|1||allocated|\n" +
                "GRS1||ipv6|::0|0||allocated|\n" +
                "GRS1||asn|1|3||assigned|\n"));

        final AuthoritativeResource resourceDataGrs2 = AuthoritativeResource.loadFromScanner(LOGGER, "GRS2", new Scanner("" +
                "GRS2||ipv4|192.0.0.1|4||allocated|\n" +
                "GRS2||ipv4|11.0.0.0|16777216||allocated|\n" +
                "GRS2||ipv4|10.0.0.0|1||allocated|\n" +
                "GRS2||ipv6|::0|0||allocated|\n" +
                "GRS2||asn|1|2||assigned|\n" +
                "GRS2||asn|10|2||assigned|\n"));

        final AuthoritativeResource resourceDataGrs3 = AuthoritativeResource.loadFromScanner(LOGGER, "GRS3", new Scanner("" +
                "GRS3||ipv4|193.0.0.0|12||allocated|\n" +
                "GRS3||ipv4|10.0.0.1|1||allocated|\n" +
                "GRS3||ipv6|::0|0||allocated|\n" +
                "GRS3||asn|1|1||assigned|\n" +
                "GRS3||asn|10|1||assigned|\n" +
                "GRS3||asn|12|1||assigned|\n"));

        when(authoritativeResourceData.getAuthoritativeResource(ciString("GRS1"))).thenReturn(resourceDataGrs1);
        when(authoritativeResourceData.getAuthoritativeResource(ciString("GRS2"))).thenReturn(resourceDataGrs2);
        when(authoritativeResourceData.getAuthoritativeResource(ciString("GRS3"))).thenReturn(resourceDataGrs3);
    }

@Test
    public void checkOverlaps() throws IOException {
        final StringWriter writer = new StringWriter();
        subject.checkOverlaps(writer);

        final String output = writer.getBuffer().toString();
        LOGGER.debug("overlaps:\n{}", output);

        final List<String> overlaps = Splitter.on("\n").splitToList(output);

        assertThat(overlaps, hasSize(11)); // Splitter generates one empty element after last newline
        assertThat(overlaps, containsInAnyOrder(
                "GRS1	GRS2	aut-num     	AS1-AS2",
                "GRS1	GRS2	inetnum     	10.0.0.0-10.0.0.0",
                "GRS1	GRS2	inetnum     	192.0.0.1-192.0.0.2",
                "GRS1	GRS2	inet6num    	::/0",
                "GRS1	GRS3	aut-num     	AS1-AS1",
                "GRS1	GRS3	inetnum     	193.0.0.10-193.0.0.11",
                "GRS1	GRS3	inet6num    	::/0",
                "GRS2	GRS3	aut-num     	AS10-AS10",
                "GRS2	GRS3	aut-num     	AS1-AS1",
                "GRS2	GRS3	inet6num    	::/0",
                ""));
    }
}
