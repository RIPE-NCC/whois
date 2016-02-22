package net.ripe.db.whois;

import net.ripe.db.whois.common.rpsl.RpslObject;

public class RpslObjectFixtures {

    public static final RpslObject TEST_OBJECT = RpslObject.parse("" +
            "mntner:        TESTING-MNT\n" +
            "descr:         Test maintainer\n" +
            "admin-c:       TP1-TEST\n" +
            "upd-to:        upd-to@ripe.net\n" +
            "mnt-nfy:       mnt-nfy@ripe.net\n" +
            "auth:          MD5-PW $1$EmukTVYX$Z6fWZT8EAzHoOJTQI6jFJ1  # 123\n" +
            "mnt-by:        OWNER-MNT\n" +
            "mnt-by:        TESTING-MNT\n" +
            "created:       2010-11-12T13:14:15Z\n" +
            "source:        TEST\n");

    public static final RpslObject PAULETH_PALTHEN = RpslObject.parse("" +
            "person:    Pauleth Palthen\n" +
            "address:   Singel 258\n" +
            "phone:     +31-1234567890\n" +
            "e-mail:    noreply@ripe.net\n" +
            "mnt-by:    OWNER-MNT\n" +
            "nic-hdl:   PP1-TEST\n" +
            "remarks:   remark\n" +
            "source:    TEST\n");

    public static final RpslObject OWNER_MNT = RpslObject.parse("" +
            "mntner:      OWNER-MNT\n" +
            "descr:       Owner Maintainer\n" +
            "admin-c:     TP1-TEST\n" +
            "upd-to:      noreply@ripe.net\n" +
            "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
            "auth:        SSO person@net.net\n" +
            "mnt-by:      OWNER-MNT\n" +
            "source:      TEST");

    public static final RpslObject TEST_PERSON = RpslObject.parse("" +
            "person:    Test Person\n" +
            "address:   Singel 258\n" +
            "phone:     +31 6 12345678\n" +
            "nic-hdl:   TP1-TEST\n" +
            "mnt-by:    OWNER-MNT\n" +
            "source:    TEST\n");

    public static final RpslObject KEYCERT = RpslObject.parse(
            "key-cert:       PGPKEY-A8D16B70\n" +
                    "method:         PGP\n" +
                    "owner:          Test Person5 <noreply5@ripe.net>\n" +
                    "fingerpr:       D079 99F1 92D5 41B6 E7BC  6578 9175 DB8D A8D1 6B70\n" +
                    "certif:         -----BEGIN PGP PUBLIC KEY BLOCK-----\n" +
                    "certif:         Version: GnuPG/MacGPG2 v2.0.18 (Darwin)\n" +
                    "certif:         Comment: GPGTools - http://gpgtools.org\n" +
                    "certif:\n" +
                    "certif:         mI0EUXD+mAEEAKbIL2qj82+7FXCFJntqY50bdNI4NrhrMfGiBMpwmuuFscvI+iT7\n" +
                    "certif:         /9AcLaAefKrKCqrM/MYLOx6b6UnIyBFqu/JVdl4aAtUpEUc4YV6jjvKa/29lAwi4\n" +
                    "certif:         /gepCwBFL1b5hV7pxnM13ZOODrf10FMjq4Y0EXP4CFVf0wi0ryqIftatABEBAAG0\n" +
                    "certif:         IFRlc3QgUGVyc29uNSA8bm9yZXBseTVAcmlwZS5uZXQ+iLkEEwECACMFAlFw/pgC\n" +
                    "certif:         GwMHCwkIBwMCAQYVCAIJCgsEFgIDAQIeAQIXgAAKCRCRdduNqNFrcDArBACPPxu+\n" +
                    "certif:         mdBqhEysSeQR1DrL02X4mwR3kkCElva4Yx91/TaZf0NC/Xa3Zr4mUKQgNW+Bp3j8\n" +
                    "certif:         QN05jY4nqVYjkiFW6U9TMnGyFcwVzQdEJzgvjeZsuANd1RxdL8E4N0l4J+lYf5WN\n" +
                    "certif:         5wfDgHE1aRm1QC5h1bOqThZ4/hFmddffL3TI77iNBFFw/pgBBACsA5ZBIZ7Ax2oA\n" +
                    "certif:         XGPHooWP6K5d9Q5MDDyoyI6eyhVGhRFY0W40/9EdCCnzv3NNC5rnkXNrUpct3WVl\n" +
                    "certif:         NpZybSVUqMkuOyAwgxqfe8k/EPpi6IfRUZeft/Hfpby7ycVUSWPxb4AmzNvR4lUp\n" +
                    "certif:         wLfAJSETsrffOCo1hhGgT7Qg2SMLswARAQABiJ8EGAECAAkFAlFw/pgCGwwACgkQ\n" +
                    "certif:         kXXbjajRa3AecwP7BbunGw89R2u8C+sw+chO3gyWr+klccxZ2g2RiGOMKWEVQXUM\n" +
                    "certif:         Ru0OLzbKfGajl1RO4oo6aTLQAKwi7RoQO31mf699Nadt8nLnI3anVT3tcdI/HXNM\n" +
                    "certif:         0qCy3r2tct/P63LCn+uIT4WvBjCp3gxPok3FdJf6iRon/J5lMNe3M3VjJoM=\n" +
                    "certif:         =DgsR\n" +
                    "certif:         -----END PGP PUBLIC KEY BLOCK-----\n" +
                    "mnt-by:         OWNER-MNT\n" +
                    "source:         TEST");

}
