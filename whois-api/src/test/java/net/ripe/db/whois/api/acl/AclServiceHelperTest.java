package net.ripe.db.whois.api.acl;

import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class AclServiceHelperTest {

    @Test
    public void testGetDecodedPrefixFromEncodedPrefix() throws Exception {
        String decodedPrefix = "10.0.0.0/32";
        String encodedPrefix = "10.0.0.0%2F32";
        String actualPrefix = AclServiceHelper.getDecodedPrefix(encodedPrefix, StandardCharsets.UTF_8.name());
        assertThat(decodedPrefix, is(actualPrefix));
    }

    @Test
    public void testGetDecodedPrefixFromAlreadyDecodedPrefix() throws Exception {
        String decodedPrefix = "10.0.0.0/32";
        String actualPrefix = AclServiceHelper.getDecodedPrefix(decodedPrefix, StandardCharsets.UTF_8.name());
        assertThat(decodedPrefix, is(actualPrefix));
    }

    @Test
    public void testGetDecodedPrefixWithNullCharacterEncoding() throws Exception {
        String decodedPrefix = "10.0.0.0/32";
        String actualPrefix = AclServiceHelper.getDecodedPrefix(decodedPrefix, null);
        assertThat(decodedPrefix, is(actualPrefix));
    }

    @Test
    public void testGetDecodedPrefixWithUnsupportedCharacterEncoding() throws Exception {
        String decodedPrefix = "10.0.0.0/32";
        String actualPrefix = AclServiceHelper.getDecodedPrefix(decodedPrefix, "adsf");
        assertThat(decodedPrefix, is(actualPrefix));
    }
}
