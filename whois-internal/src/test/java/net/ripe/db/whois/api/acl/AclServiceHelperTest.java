package net.ripe.db.whois.api.acl;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class AclServiceHelperTest {

    @Test
    public void testGetDecodedPrefixFromEncodedPrefix() throws Exception {
        assertThat(AclServiceHelper.decode("10.0.0.0%2F32"), is("10.0.0.0/32"));
    }

    @Test
    public void testGetDecodedPrefixFromAlreadyDecodedPrefix() throws Exception {
        assertThat(AclServiceHelper.decode("10.0.0.0/32"), is("10.0.0.0/32"));
    }
}
