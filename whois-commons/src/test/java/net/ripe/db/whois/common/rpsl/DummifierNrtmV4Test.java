package net.ripe.db.whois.common.rpsl;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


@ExtendWith(MockitoExtension.class)
public class DummifierNrtmV4Test {

    DummifierNrtmV4 subject;

    @BeforeEach
    public void setUp() {
        subject = new DummifierNrtmV4(
                Map.of(AttributeType.AUTH.toString(), "PGP PGP-111",
                        AttributeType.TECH_C.toString(), "CREW-RIPE",
                        AttributeType.ADMIN_C.toString(), "CREW-RIPE")
        );
    }

    @Test
    public void dummify_remarks_descr() {
        final RpslObject routeObject = RpslObject.parse(
                "route:          10/8\n" +
                "remarks:        remarks for test1\n" +
                "remarks:        remarks for test2\n" +
                "descr:          descr for test1\n" +
                "descr:          descr for test2\n" +
                "origin:         AS3333\n" +
                "source:         TEST");

        final RpslObject dummifiedRouteObject = subject.dummify(routeObject);

        assertThat(dummifiedRouteObject.toString(), is(
                "route:          10/8\n" +
                "remarks:        Dummified\n" +
                "descr:          Dummified\n" +
                "origin:         AS3333\n" +
                "source:         TEST\n" +
                "remarks:        ****************************\n" +
                "remarks:        * THIS OBJECT IS MODIFIED\n" +
                "remarks:        * Please note that all data that is generally regarded as personal\n" +
                "remarks:        * data has been removed from this object.\n" +
                "remarks:        * To view the original object, please query the RIPE Database at:\n" +
                "remarks:        * http://www.ripe.net/whois\n" +
                "remarks:        ****************************\n"));
    }

    @Test
    public void dummify_keycert_object() {
        final RpslObject keyCert = RpslObject.parse(
                "key-cert:        AUTO-1\n" +
                        "method:          X509\n" +
                        "owner:           /CN=4a96eecf-9d1c-4e12-8add-5ea5522976d8\n" +
                        "fingerpr:        82:7C:C5:40:D1:DB:AE:6A:FA:F8:40:3E:3C:9C:27:7C\n" +
                        "certif:          -----BEGIN CERTIFICATE-----\n" +
                        "certif:          -----END CERTIFICATE-----\n" +
                        "mnt-by:          TEST-DBM-MNT\n" +
                        "source:          TEST");

        final RpslObject dummifiedRouteObject = subject.dummify(keyCert);

        assertThat(dummifiedRouteObject.toString(), is(
                "key-cert:       AUTO-1\n" +
                        "method:         X509\n" +
                        "owner:          /CN=4a96eecf-9d1c-4e12-8add-5ea5522976d8\n" +
                        "fingerpr:       82:7C:C5:40:D1:DB:AE:6A:FA:F8:40:3E:3C:9C:27:7C\n" +
                        "certif:         Dummified\n" +
                        "mnt-by:         TEST-DBM-MNT\n" +
                        "source:         TEST\n" +
                        "remarks:        ****************************\n" +
                        "remarks:        * THIS OBJECT IS MODIFIED\n" +
                        "remarks:        * Please note that all data that is generally regarded as personal\n" +
                        "remarks:        * data has been removed from this object.\n" +
                        "remarks:        * To view the original object, please query the RIPE Database at:\n" +
                        "remarks:        * http://www.ripe.net/whois\n" +
                        "remarks:        ****************************\n"));
    }
}
