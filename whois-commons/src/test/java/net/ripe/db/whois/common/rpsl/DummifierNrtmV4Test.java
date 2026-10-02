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
        final RpslObject routeObject = RpslObject.parse("""
                route:          10/8
                remarks:        remarks for test1
                remarks:        remarks for test2
                descr:          descr for test1
                descr:          descr for test2
                origin:         AS3333
                source:         TEST""");

        final RpslObject dummifiedRouteObject = subject.dummify(routeObject);

        assertThat(dummifiedRouteObject.toString(), is("""
                route:          10/8
                remarks:        Dummified
                descr:          Dummified
                origin:         AS3333
                source:         TEST
                remarks:        ****************************
                remarks:        * THIS OBJECT IS MODIFIED
                remarks:        * Please note that all data that is generally regarded as personal
                remarks:        * data has been removed from this object.
                remarks:        * To view the original object, please query the RIPE Database at:
                remarks:        * http://www.ripe.net/whois
                remarks:        ****************************
                """));
    }

    @Test
    public void dummify_organisation_object() {
        final RpslObject organisation = RpslObject.parse("""
                organisation: ORG-TO1-TEST
                org-name:     Test Organisation
                reg-nr:       1234567890
                source:       TEST
                """);

        final RpslObject dummifiedOrganisationObject = subject.dummify(organisation);

        assertThat(dummifiedOrganisationObject.toString(), is("""
                organisation:   ORG-TO1-TEST
                org-name:       Test Organisation
                reg-nr:         1234567890
                source:         TEST
                remarks:        ****************************
                remarks:        * THIS OBJECT IS MODIFIED
                remarks:        * Please note that all data that is generally regarded as personal
                remarks:        * data has been removed from this object.
                remarks:        * To view the original object, please query the RIPE Database at:
                remarks:        * http://www.ripe.net/whois
                remarks:        ****************************
                """));
    }


    @Test
    public void dummify_keycert_object() {
        final RpslObject keyCert = RpslObject.parse("""
                        key-cert:        AUTO-1
                        method:          X509
                        owner:           /CN=4a96eecf-9d1c-4e12-8add-5ea5522976d8
                        fingerpr:        82:7C:C5:40:D1:DB:AE:6A:FA:F8:40:3E:3C:9C:27:7C
                        certif:          -----BEGIN CERTIFICATE-----
                        certif:          -----END CERTIFICATE-----
                        mnt-by:          TEST-DBM-MNT
                        source:          TEST""");

        final RpslObject dummifiedKeycertObject = subject.dummify(keyCert);

        assertThat(dummifiedKeycertObject.toString(), is("""
                        key-cert:       AUTO-1
                        method:         X509
                        owner:          /CN=4a96eecf-9d1c-4e12-8add-5ea5522976d8
                        fingerpr:       82:7C:C5:40:D1:DB:AE:6A:FA:F8:40:3E:3C:9C:27:7C
                        certif:         Dummified
                        mnt-by:         TEST-DBM-MNT
                        source:         TEST
                        remarks:        ****************************
                        remarks:        * THIS OBJECT IS MODIFIED
                        remarks:        * Please note that all data that is generally regarded as personal
                        remarks:        * data has been removed from this object.
                        remarks:        * To view the original object, please query the RIPE Database at:
                        remarks:        * http://www.ripe.net/whois
                        remarks:        ****************************
                        """));
    }
}
