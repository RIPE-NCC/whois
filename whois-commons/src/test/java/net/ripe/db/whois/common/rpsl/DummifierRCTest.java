package net.ripe.db.whois.common.rpsl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class DummifierRCTest {

    private DummifierRC subject;

    @BeforeEach
    public void setup() {
        subject = new DummifierRC();
    }

    @Test
    public void dummify_organisation() {
        final RpslObject before = RpslObject.parse(
            "organisation:   ORG-RIEN1-TEST\n" +
            "org-name:       Reseaux IP Europeens Network Coordination Centre (RIPE NCC)\n" +
            "org-type:       LIR\n" +
            "descr:          RIPE NCC Operations\n" +
            "address:        Singel 256\n" +
            "address:        Amsterdam\n" +
            "address:        Netherlands\n" +
            "phone:          +31205552222\n" +
            "fax-no:         +31205552266\n" +
            "admin-c:        AC1-TEST\n" +
            "admin-c:        AC2-TEST\n" +
            "abuse-c:        AR1-TEST\n" +
            "mnt-ref:        TEST-MNT\n" +
            "mnt-by:         TEST-MNT\n" +
            "created:        1970-01-01T00:00:00Z\n" +
            "last-modified:  2020-09-22T09:00:00Z\n" +
            "source:         TEST");

        final RpslObject after = subject.dummify(3, before);

        assertThat(after.toString(), is(
            "organisation:   ORG-RIEN1-TEST\n" +
            "org-name:       Romeo India Echo November\n" +
            "org-type:       LIR\n" +
            "descr:          ***\n" +
            "address:        ***\n" +
            "address:        ***\n" +
            "address:        Netherlands\n" +
            "phone:          +31205......\n" +
            "fax-no:         +31205......\n" +
            "admin-c:        AC1-TEST\n" +
            "admin-c:        AC2-TEST\n" +
            "abuse-c:        AR1-TEST\n" +
            "mnt-ref:        TEST-MNT\n" +
            "mnt-by:         TEST-MNT\n" +
            "created:        1970-01-01T00:00:00Z\n" +
            "last-modified:  2020-09-22T09:00:00Z\n" +
            "source:         TEST\n"));
    }

    @Test
    public void dummify_role() {
        final RpslObject before = RpslObject.parse(
            "role:           Abuse Contact\n" +
            "descr:          RIPE NCC Operations\n" +
            "address:        Singel 256\n" +
            "address:        Amsterdam\n" +
            "address:        Netherlands\n" +
            "e-mail:         abuse@ripe.net\n" +
            "nic-hdl:        AC1-TEST\n" +
            "remarks:        Abuse\n" +
            "+Contact\n" +
            "\tRole\n" +
            "abuse-mailbox:  abuse@ripe.net\n" +
            "mnt-by:         TEST-MNT\n" +
            "created:        1970-01-01T00:00:00Z\n" +
            "last-modified:  2020-09-22T09:00:00Z\n" +
            "source:         TEST");

        final RpslObject after = subject.dummify(3, before);

        assertThat(after.toString(), is(
            "role:           Abuse Contact\n" +
            "descr:          ***\n" +
            "address:        Singel 256\n" +
            "address:        Amsterdam\n" +
            "address:        Netherlands\n" +
            "e-mail:         ***@ripe.net\n" +
            "nic-hdl:        AC1-TEST\n" +
            "remarks:        ***\n" +
            "abuse-mailbox:  abuse@ripe.net\n" +
            "mnt-by:         TEST-MNT\n" +
            "created:        1970-01-01T00:00:00Z\n" +
            "last-modified:  2020-09-22T09:00:00Z\n" +
            "source:         TEST\n"));
    }

    @Test
    public void dummify_aut_num() {
        final RpslObject before = RpslObject.parse(
            "aut-num:        AS3333\n" +
            "descr:          RIPE NCC Operations\n" +
            "as-name:        RIPE_NCC_AS_NAME\n" +
            "admin-c:        AC1-TEST\n" +
            "tech-c:         TC1-TEST\n" +
            "notify:         test@ripe.net\n" +
            "notify:         test@ripe.net, noreply@ripe.net\n" +
            "notify:         Test User <test@ripe.net>\n" +
            "notify:         Invalid invalid@ripe.net>\n" +
            "mnt-by:         TEST-MNT\n" +
            "created:        1970-01-01T00:00:00Z\n" +
            "last-modified:  2020-09-22T09:00:00Z\n" +
            "source:         TEST");

        final RpslObject after = subject.dummify(3, before);

        assertThat(after.toString(), is(
            "aut-num:        AS3333\n" +
            "descr:          ***\n" +
            "as-name:        RIPE_NCC_AS_NAME\n" +
            "admin-c:        AC1-TEST\n" +
            "tech-c:         TC1-TEST\n" +
            "notify:         ***@ripe.net\n" +
            "notify:         ***\n" +
            "notify:         ***@ripe.net\n" +
            "notify:         ***\n" +
            "mnt-by:         TEST-MNT\n" +
            "created:        1970-01-01T00:00:00Z\n" +
            "last-modified:  2020-09-22T09:00:00Z\n" +
            "source:         TEST\n"));
    }

    @Test
    public void dummify_keycert() {
        final RpslObject before = RpslObject.parse(
            "key-cert:       PGPKEY-57639544\n" +
            "method:         PGP\n" +
            "owner:          Test Person <noreply@ripe.net>\n" +
            "fingerpr:       2A4F DFBE F26C 1951 449E  B450 73BB 96F8 5763 9544\n" +
            "certif:         -----BEGIN PGP PUBLIC KEY BLOCK-----\n" +
            "certif:         -----END PGP PUBLIC KEY BLOCK-----\n" +
            "notify:         noreply@ripe.net\n" +
            "mnt-by:         TEST-MNT\n" +
            "source:         TEST\n");

        final RpslObject after = subject.dummify(3, before);

        assertThat(after.toString(), is(
            "key-cert:       PGPKEY-57639544\n" +
            "method:         PGP\n" +
            "owner:          ***\n" +
            "fingerpr:       2A4F DFBE F26C 1951 449E  B450 73BB 96F8 5763 9544\n" +
            "certif:         -----BEGIN PGP PUBLIC KEY BLOCK-----\n" +
            "certif:         -----END PGP PUBLIC KEY BLOCK-----\n" +
            "notify:         ***@ripe.net\n" +
            "mnt-by:         TEST-MNT\n" +
            "source:         TEST\n"));
    }
}
