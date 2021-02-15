package net.ripe.db.whois.common.grs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.ripe.db.whois.common.rpsl.ObjectType;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static org.junit.Assert.assertTrue;

public class AuthoritativeResourceJsonLoaderTest {

    static Logger logger = LoggerFactory.getLogger(AuthoritativeResourceJsonLoaderTest.class);

    @Test
    public void load() throws IOException {
        final AuthoritativeResource authoritativeResource = new AuthoritativeResourceJsonLoader(logger).load(
                new ObjectMapper().readValue(IOUtils.toString(getClass().getResourceAsStream("/grs/rirstats.json"), Charset.defaultCharset()), JsonNode.class)
        );

        assertTrue(authoritativeResource.isMaintainedInRirSpace(ObjectType.AUT_NUM, ciString("AS7")));
        assertTrue(authoritativeResource.isMaintainedInRirSpace(ObjectType.AUT_NUM, ciString("AS1877")));
        assertTrue(authoritativeResource.isMaintainedInRirSpace(ObjectType.AUT_NUM, ciString("AS2849")));

        assertTrue(authoritativeResource.isMaintainedInRirSpace(ObjectType.INETNUM, ciString("2.0.0.0-2.15.255.255")));
        assertTrue(authoritativeResource.isMaintainedInRirSpace(ObjectType.INETNUM, ciString("2.56.168.0-2.56.171.255")));
        assertTrue(authoritativeResource.isMaintainedInRirSpace(ObjectType.INETNUM, ciString("5.44.248.0-5.44.255.255")));
        assertTrue(authoritativeResource.isMaintainedInRirSpace(ObjectType.INETNUM, ciString("13.116.0.0-13.123.255.255")));

        assertTrue(authoritativeResource.isMaintainedInRirSpace(ObjectType.INET6NUM, ciString("2001:600::/32")));
        assertTrue(authoritativeResource.isMaintainedInRirSpace(ObjectType.INET6NUM, ciString("2001:678::/48")));
        assertTrue(authoritativeResource.isMaintainedInRirSpace(ObjectType.INET6NUM, ciString("2001:678:1::/48")));
        assertTrue(authoritativeResource.isMaintainedInRirSpace(ObjectType.INET6NUM, ciString("2001:601::/32")));
    }

}
