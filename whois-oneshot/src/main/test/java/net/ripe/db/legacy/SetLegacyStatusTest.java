package net.ripe.db.legacy;

import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;

/*
    (1) input line: 137.191.192.0/20,/21,/22,/23,/24
        should map to: 137.191.192.0 - 137.191.222.255 [7936]
            137.191.192/20      137.191.192.0 - 137.191.207.255 [4096]
            137.191.208/21      137.191.208.0 - 137.191.215.255 [2048]
            137.191.216/22      137.191.216.0 - 137.191.219.255 [1024]
            137.191.220/23      137.191.220.0 - 137.191.221.255 [512]
            137.191.222/24      137.191.222.0 - 137.191.222.255 [256]
    (2) input line: 128.141.0.0/16,/16
        should map to: 128.141.0.0 - 128.142.255.255
 */
public class SetLegacyStatusTest {
    private SetLegacyStatus setLegacyStatus = new SetLegacyStatus(null, "", "", true);

    @Test
    public void shouldGiveRpslObjectForCsl() throws Exception {
        final RpslObject rpslObject = setLegacyStatus.lookupTopLevelIpv4ResourceFromCsl("137.191.192.0/20,/21,/22,/23,/24");
        assertThat(rpslObject, notNullValue());
        assertThat(rpslObject.getValueForAttribute(AttributeType.INETNUM).toString(), is("137.191.192.0 - 137.191.222.255"));
    }

    @Test
    public void shouldNotGiveIpIntervalForCsl() throws Exception {
        final RpslObject rpslObject = setLegacyStatus.lookupTopLevelIpv4ResourceFromCsl("128.141.0.0/16,/17");
        assertThat(rpslObject, nullValue());
    }

    @Test
    public void shouldGiveIntervalForInetnum() throws Exception {
        final RpslObject rpslObject = setLegacyStatus.lookupTopLevelIp4Resource("128.130.0.0/15");
        assertThat(rpslObject, notNullValue());
        assertThat(rpslObject.getValueForAttribute(AttributeType.INETNUM).toString(), is("128.130.0.0 - 128.131.255.255"));
    }
}
