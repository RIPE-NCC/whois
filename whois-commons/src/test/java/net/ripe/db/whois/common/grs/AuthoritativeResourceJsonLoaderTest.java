package net.ripe.db.whois.common.grs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.ripe.db.whois.common.rpsl.ObjectType;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import static net.ripe.db.whois.common.domain.CIString.ciString;

public class AuthoritativeResourceJsonLoaderTest {

    static Logger logger = LoggerFactory.getLogger(AuthoritativeResourceJsonLoaderTest.class);

    @Test
    public void load() throws IOException {
        final AuthoritativeResource authoritativeResource = new AuthoritativeResourceJsonLoader(logger).load(
                new ObjectMapper().readValue(IOUtils.toString(getClass().getResourceAsStream("/grs/rirstats.json"), Charset.defaultCharset()), JsonNode.class)
        );

        assertThat(authoritativeResource.isMaintainedInRirSpace(ObjectType.AUT_NUM, ciString("AS7")), is(true));
        assertThat(authoritativeResource.isMaintainedInRirSpace(ObjectType.AUT_NUM, ciString("AS1877")), is(true));
        assertThat(authoritativeResource.isMaintainedInRirSpace(ObjectType.AUT_NUM, ciString("AS2849")), is(true));

        assertThat(authoritativeResource.isMaintainedInRirSpace(ObjectType.INETNUM, ciString("2.0.0.0-2.15.255.255")), is(true));
        assertThat(authoritativeResource.isMaintainedInRirSpace(ObjectType.INETNUM, ciString("2.56.168.0-2.56.171.255")), is(true));
        assertThat(authoritativeResource.isMaintainedInRirSpace(ObjectType.INETNUM, ciString("5.44.248.0-5.44.255.255")), is(true));
        assertThat(authoritativeResource.isMaintainedInRirSpace(ObjectType.INETNUM, ciString("13.116.0.0-13.123.255.255")), is(true));

        assertThat(authoritativeResource.isMaintainedInRirSpace(ObjectType.INET6NUM, ciString("2001:600::/32")), is(true));
        assertThat(authoritativeResource.isMaintainedInRirSpace(ObjectType.INET6NUM, ciString("2001:678::/48")), is(true));
        assertThat(authoritativeResource.isMaintainedInRirSpace(ObjectType.INET6NUM, ciString("2001:678:1::/48")), is(true));
        assertThat(authoritativeResource.isMaintainedInRirSpace(ObjectType.INET6NUM, ciString("2001:601::/32")), is(true));
    }

}
