package net.ripe.db.whois.spec.update


import net.ripe.db.whois.spec.BaseQueryUpdateSpec
import net.ripe.db.whois.spec.domain.AckResponse
import net.ripe.db.whois.spec.domain.Message

@org.junit.jupiter.api.Tag("IntegrationTest")
class PgpSignedMessageSpec extends BaseQueryUpdateSpec {
    @Override
    Map<String, String> getTransients() {
        [
                "PGP-KEYCERT-ONE": """\
                key-cert:     PGPKEY-EBEEB05E
                method:       PGP
                owner:        Test Person1 <noreply1@ripe.net>
                fingerpr:     2A4F DFBE F26C 1951 449E  B450 73BB 96F8 EBEE B05E
                certif:       -----BEGIN PGP PUBLIC KEY BLOCK-----
                certif:       Version: GnuPG/MacGPG2 v2.0.18 (Darwin)
                certif:       Comment: GPGTools - http://gpgtools.org
                certif:
                certif:       mI0EUXAD2gEEAN0pQnR+zoMx1nrwEtqKtzs8uIp6f7zrTWwTyiGM5jWnQW0+2nj+
                certif:       2IBA3Rvdehyz8uUeEw5hd8UbqBb3cKZTTq669q4gaf4iFLcaaLZ3rqo1r9f2Qjg+
                certif:       DdoGjskpGWOA6sJvsdV7jLDGRB2WJpCLM3I9Ckm7d6gvZC33kCWQZ/eFABEBAAG0
                certif:       IFRlc3QgUGVyc29uMSA8bm9yZXBseTFAcmlwZS5uZXQ+iLkEEwECACMFAlFwA9oC
                certif:       GwMHCwkIBwMCAQYVCAIJCgsEFgIDAQIeAQIXgAAKCRBzu5b46+6wXmu3A/40xywR
                certif:       laNR+WZ4/OWrYLifvebXHyrCDjhLcUoM0njZKJeVSHFW8Qmh5Llerk7yDy1hcs5G
                certif:       gvsY3iQ2Uhq2vYhAEQWOZaXTg3Fb4UTzotTPPrLf0m1JXP9a28FI2ZkLrhGv1PlX
                certif:       ansEoXlWgnwPp9PV6HW1GUuIBIAqyanOCKYbmLiNBFFwA9oBBADUd1qRQ1/StzdW
                certif:       xkJlypQ5vedAwk/Oc7zRzv1Jzp96eTIMOpALzmlrKB4+pJELYcYCeFy6zagoqFJa
                certif:       y77IqFfo5V8EXlW9IZj54jiLtz1+G/LSSabOs2d5DdjT5ihmPolyzWepWk5SOaNq
                certif:       LJNgc20FVc02d6bwr9a0RKbgc7UyvwARAQABiJ8EGAECAAkFAlFwA9oCGwwACgkQ
                certif:       c7uW+OvusF5m2QP+LnB/M/VI0/AHycrkZBKoy269jf8F1wkbK7gSe6rVXVFpm06L
                certif:       LhdmBbGrZhapjXb+7QhS0XL4s3tfoDCXjO3FnY2uAfDmfRmxlaMuEajDnFQyQc3T
                certif:       KepilxrKtIkIb31RcHiefTcOSB8a2hKwGLK6QTo/X3bzSJl4NonvAArJITs=
                certif:       =iWqc
                certif:       -----END PGP PUBLIC KEY BLOCK-----
                mnt-by:       TST-MNT
                source:       TEST
                """,
                "PGP-KEYCERT-TWO": """\
                key-cert:     PGPKEY-44AF2B48
                method:       PGP
                owner:        Test Person2 <noreply2@ripe.net>
                fingerpr:     2D23 883D 2FBD C357 E7C6  DFE1 62F4 FD0D 44AF 2B48
                certif:       -----BEGIN PGP PUBLIC KEY BLOCK-----
                certif:       Version: GnuPG/MacGPG2 v2.0.18 (Darwin)
                certif:       Comment: GPGTools - http://gpgtools.org
                certif:
                certif:       mI0EUXAFaAEEAM3KTw+gqe4/L6qaefEIft4kILpAt4TjZbLUqXO44/1j08JiDHP5
                certif:       MT/fm0wGIQR83VrTy2A0SHV77tfUJ1k5TcLNoVouqPlg9oJVbZ1tNfllwU3k0f+Z
                certif:       AMiZYhNzdGpP7efCdOC09Q2KhGhVfKPmco0NFFpoSp/Alq5vsDyqKcvfABEBAAG0
                certif:       IFRlc3QgUGVyc29uMiA8bm9yZXBseTJAcmlwZS5uZXQ+iLkEEwECACMFAlFwBWgC
                certif:       GwMHCwkIBwMCAQYVCAIJCgsEFgIDAQIeAQIXgAAKCRBi9P0NRK8rSOSKA/47d7Kj
                certif:       iuxtvPDELtcYb6Q4QMDnKaRrZkIzCggTLcAzG7pxYdLgfwFBhKUUklRdFf0JVWIl
                certif:       xXXQPcly4oN6HAO9g9tNpN1i5B0cvcJkdbB8Rgvw1Wlbn1bw1Im1B/sRBmFNjle2
                certif:       7O0IIloeKoRIV7PXAyoTofGdGxeCl9B+1oQrpbiNBFFwBWgBBADSQQJFeodvDOd9
                certif:       KmReydL5e9adx5mTAQJvYnhv9o+zKzZpVG89LqgRryLOmZyJ0G+oAjR2XY9FOGZf
                certif:       k5liiMuN8B8aiBF4I4lDTeNbPdt2XBRCLPBjlRHYYWUk3e1ZocdgPbJjJ8UzeCQ+
                certif:       3gSDIOW8x62lLthQ5HMMrIeyCro3gQARAQABiJ8EGAECAAkFAlFwBWgCGwwACgkQ
                certif:       YvT9DUSvK0jRogP/bakd8QjogfLQEZhA344pb6s9nw0H0GA1qaD5ll2+xvvDHyjc
                certif:       PBOC6XqeBQVCkwm94nJlsV3Vn/OTwm2RkmiJ5ncvsyuw8d8xF3P32oaYp6cxXBvc
                certif:       OHYGE/UjM/H2AgL/dTjOlkxhpcZ3xrL9PNJBkqFfgsy3UHcyefjQy67y1DM=
                certif:       =nOlw
                certif:       -----END PGP PUBLIC KEY BLOCK-----
                mnt-by:       TST-MNT2
                source:       TEST
                """,
                "TST-NEW-MNT"    : """\
                mntner:      TST-NEW-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      dbtest@ripe.net
                auth:        PGPKEY-EBEEB05E
                mnt-by:      TST-MNT
                source:      TEST
                """,
                "TEST-DBM-MNT"   : """\
                mntner:       TEST-DBM-MNT
                descr:        Mntner for RIPE DBM objects.
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                auth:         MD5-PW \$1\$tTr8D75J\$ruGCSs6bNrwr25ZrervtR0 # test-dbm
                upd-to:       dbtest@ripe.net
                mnt-by:       TST-MNT
                source:       TEST
                """,
                "IRT-TEST"       : """\
                irt:          IRT-TEST
                address:      Singel 258
                e-mail:       dbtest@ripe.net
                signature:    PGPKEY-EBEEB05E
                encryption:   PGPKEY-44AF2B48
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                auth:         MD5-PW \$1\$12345678\$wza1MMo7nPUnBHNJMOMq3.   # irt
                irt-nfy:      case027-1@ripe.net
                notify:       case027-2@ripe.net
                mnt-by:       TST-MNT
                source:       TEST
                """
        ]
    }

    def "create mntr authorized by PGP-Key"() {
        given:
        syncUpdate(getTransient("PGP-KEYCERT-ONE") + "password:test")
        expect:
        queryObject("-r -T key-cert PGPKEY-EBEEB05E", "key-cert", "PGPKEY-EBEEB05E")
        when:
        def message = send new Message(
                subject: "",
                body: """\
                mntner:      TST-NEW-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      dbtest@ripe.net
                auth:        PGPKEY-EBEEB05E
                mnt-by:      TST-MNT
                source:      TEST

                password: test
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[mntner] TST-NEW-MNT" }

        queryObject("-rBT mntner TST-NEW-MNT", "mntner", "TST-NEW-MNT")
    }

    def "create mntr authorized by a non-existing PGP-Key"() {
        expect:
        queryObjectNotFound("-r -T key-cert PGPKEY-EBEEB05E", "key-cert", "PGPKEY-EBEEB05E")
        when:
        def message = send new Message(
                subject: "",
                body: """\
                mntner:      TST-NEW-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      dbtest@ripe.net
                auth:        PGPKEY-EBEEB05E
                mnt-by:      TST-MNT
                source:      TEST

                password: test
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.failed
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 2, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[mntner] TST-NEW-MNT" }
        ack.errorMessagesFor("Create", "[mntner] TST-NEW-MNT") == ["Unknown object referenced PGPKEY-EBEEB05E"]
        queryObjectNotFound("-rBT mntner TST-NEW-MNT", "mntner", "TST-NEW-MNT")
    }

    def "create mntr authorized by a existing PGP-Key with override"() {
        when:
        def update = syncUpdate(
                """\
                -----BEGIN PGP SIGNED MESSAGE-----
                Hash: SHA1

                mntner:       TEST-DBM-MNT
                descr:        Mntner for RIPE DBM objects.........
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                upd-to:       dbtest@ripe.net
                auth:         MD5-PW \$1\$12345678\$knzUanD5W.zU11AJAAbNw/   # test-dbm
                mnt-by:       TST-MNT
                source:       TEST
                override:     denis,override1


                -----BEGIN PGP SIGNATURE-----
                Version: GnuPG v1.2.4 (GNU/Linux)

                iD8DBQFIRm6CRxqrn9g8P70RAob7AJ9WiCicv9IwVJzf+qRmCM3e102K3QCeKvjW
                9SSmtbtEWAiB8r7PxJN9DYM=
                =XSlL
                -----END PGP SIGNATURE-----

                password: test
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", update)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[mntner] TEST-DBM-MNT" }

        queryObject("-rBT mntner TEST-DBM-MNT", "mntner", "TEST-DBM-MNT")
    }

    def "incorrect pgpkey does not get created"() {
        when:
        def create = syncUpdate("""\
                key-cert:     PGPKEY-EBEEB05E
                method:       PGP
                owner:        Test Person1 <noreply1@ripe.net>
                fingerpr:     2A4F DFBE F26C 1951 449E  B450 73BB 96F8 EBEE B05E
                certif:       -----BEGIN PGP PUBLIC KEY BLOCK-----
                certif:       Version: GnuPG/MacGPG2 v2.0.18 (Darwin)
                certif:       Comment: GPGTools - http://gpgtools.org
                certif:
                certif:       mI0EUXAD2gEEAN0pQnR+zoMx1nrwEtqKtzs8uIp6f7zrTWwTyiGM5jWnQW0+2nj+
                certif:       2IBA3Rvdehyz8uUeEw5hd8UbqBb3cKZTTq669q4gaf4iFLcaaLZ3rqo1r9f2Qjg+
                certif:       DdoGjskpGWOA6sJvsdV7jLDGRB2WJpCLM3I9Ckm7d6gvZC33kCWQZ/eFABEBAAG0
                certif:       IFRlc3QgUGVyc29uMSA8bm9yZXBseTFAcmlwZS5uZXQ+iLkEEwECACMFAlFwA9oC
                certif:       GwMHCwkIBwMCAQYVCAIJCgsEFgIDAQIeAQIXgAAKCRBzu5b46+6wXmu3A/40xywR
                certif:       laNR+WZ4/OWrYLifvebXHyrCDjhLcUoM0njZKJeVSHFW8Qmh5Llerk7yDy1hcs5G
                certif:       gvsY3iQ2Uhq2vYhAEQWOZaXTg3Fb4UTzotTPPrLf0m1JXP9a28FI2ZkLrhGv1PlX
                certif:       ansEoXlWgnwPp9PV6HW1GUuIBIAqyanOCKYbmLiNBFFwA9oBBADUd1qRQ1/StzdW
                certif:       xkJlypQ5vedAwk/Oc7zRzv1Jzp96eTIMOpALzmlrKB4+pJELYcYCeFy6zagoqFJa
                certif:       y77IqFfo5V8EXlW9IZj54jiLtz1+G/LSSabOs2d5DdjT5ihmPolyzWepWk5SOaNq
                certif:       LJNgc20FVc02d6bwr9a0RKbgc7UyvwARAQABiJ8EGAECAAkFAlFwA9oCGwwACgkQ
                certif:       c7uW+OvusF5m2QP+LnB/M/VI0/AHycrkZBKoy269jf8F1wkbK7gSe6rVXVFpm06L
                certif:       LhdmBbGrZhapjXb+7QhS0XL4s3tfoDCXjO3FnY2uAfDmfRmxlaMuEajDnFQyQc3T
                certif:       KepilxrKtIkIb31RcHiefTcOSB8a2hKwGLK6QTo/X3bzSJl4NonvAArJITs==iWqc
                certif:       -----END PGP PUBLIC KEY BLOCK-----
                mnt-by:       TST-MNT
                source:       TEST
                password:     test""".stripIndent(true))

        then:
        def ack = new AckResponse("", create)

        ack.summary.nrFound == 1
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[key-cert] PGPKEY-EBEEB05E" }
        ack.errorMessagesFor("Create", "[key-cert] PGPKEY-EBEEB05E") == ["The supplied object has no key"]

        queryObjectNotFound("-rBT key-cert PGPKEY-EBEEB05E", "key-cert", "PGPKEY-EBEEB05E")
    }

    def "modify mntr authorized by PGP-Key"() {
        given:
        syncUpdate(getTransient("PGP-KEYCERT-ONE") + "password:test")
        syncUpdate(getTransient("TST-NEW-MNT") + "password:test")
        expect:
        queryObject("-r -T key-cert PGPKEY-EBEEB05E", "key-cert", "PGPKEY-EBEEB05E")
        queryObject("-r -T mntner TST-NEW-MNT", "mntner", "TST-NEW-MNT")
        when:
        def message = send new Message(
                subject: "",
                body: """\
                mntner:      TST-NEW-MNT
                descr:       MNTNER description changed
                admin-c:     TP1-TEST
                upd-to:      dbtest@ripe.net
                auth:        PGPKEY-EBEEB05E
                mnt-by:      TST-MNT
                source:      TEST

                password: test
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[mntner] TST-NEW-MNT" }

        queryObject("-rBT mntner TST-NEW-MNT", "mntner", "TST-NEW-MNT")
    }

    def "create mntr authorized by multiple auth values"() {
        given:
        syncUpdate(getTransient("PGP-KEYCERT-ONE") + "password:test")
        syncUpdate(getTransient("PGP-KEYCERT-TWO") + "password:test2")
        expect:
        queryObject("-r -T key-cert PGPKEY-EBEEB05E", "key-cert", "PGPKEY-EBEEB05E")
        queryObject("-r -T key-cert PGPKEY-44AF2B48", "key-cert", "PGPKEY-44AF2B48")
        when:
        def message = send new Message(
                subject: "",
                body: """\
                mntner:      TST-NEW-MNT
                descr:       MNTNER description changed
                admin-c:     TP1-TEST
                upd-to:      dbtest@ripe.net
                auth:        PGPKEY-EBEEB05E
                auth:        PGPKEY-44AF2B48
                mnt-by:      TST-MNT
                source:      TEST

                password: test
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[mntner] TST-NEW-MNT" }

        queryObject("-rBT mntner TST-NEW-MNT", "mntner", "TST-NEW-MNT")
    }

    def "modify mntner using override using pgp key with wrong pgp signature"() {
        given:
        syncUpdate(getTransient("TEST-DBM-MNT") + "password:test")
        expect:
        queryObject("-r -T mntner TEST-DBM-MNT", "mntner", "TEST-DBM-MNT")
        when:
        def update = syncUpdate(
                """\
                -----BEGIN PGP SIGNED MESSAGE-----
                Hash: SHA1

                mntner:       TEST-DBM-MNT
                descr:        Mntner for RIPE DBM objects.........
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                upd-to:       dbtest@ripe.net
                auth:         MD5-PW \$1\$12345678\$knzUanD5W.zU11AJAAbNw/   # test-dbm
                mnt-by:       TST-MNT
                source:       TEST
                override:     denis,override1

                -----BEGIN PGP SIGNATURE-----
                Version: GnuPG v1.2.2 (GNU/Linux)

                iD8DBQE/XcuKsR6P2g2tJhIRAsPRAJ4onZ0f8geuOejI7vBZcNQ7Mx6PWQCeMclN
                w9bxQY7vBu0I/hXyDsf7yPE=
                =kHbd
                -----END PGP SIGNATURE-----

                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", update)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Modify" && it.key == "[mntner] TEST-DBM-MNT" }

        queryObject("-rBT mntner TEST-DBM-MNT", "mntner", "TEST-DBM-MNT")
    }

    def "modify mntner using override using pgp key with right pgp signature"() {
        given:
        syncUpdate(getTransient("TEST-DBM-MNT") + "password:test")
        expect:
        queryObject("-r -T mntner TEST-DBM-MNT", "mntner", "TEST-DBM-MNT")
        when:
        def update = syncUpdate(
                """\
                -----BEGIN PGP SIGNED MESSAGE-----
                Hash: SHA1

                mntner:       TEST-DBM-MNT
                descr:        Mntner for RIPE DBM objects.........
                remarks:      My remarks
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                upd-to:       dbtest@ripe.net
                auth:         MD5-PW \$1\$tTr8D75J\$ruGCSs6bNrwr25ZrervtR0 # test-dbm
                mnt-by:       TST-MNT
                source:       TEST
                override:     denis,override1


                -----BEGIN PGP SIGNATURE-----
                Version: GnuPG v1.2.4 (GNU/Linux)

                iD8DBQFIRm6CRxqrn9g8P70RAob7AJ9WiCicv9IwVJzf+qRmCM3e102K3QCeKvjW
                9SSmtbtEWAiB8r7PxJN9DYM=
                =XSlL
                -----END PGP SIGNATURE-----

                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", update)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Modify" && it.key == "[mntner] TEST-DBM-MNT" }

        query_object_matches("-rBT mntner TEST-DBM-MNT", "mntner", "TEST-DBM-MNT", "remarks:\\s*My remarks")
    }

    def "create irt using pgp key using override"() {
        given:
        syncUpdate(getTransient("PGP-KEYCERT-ONE") + "password:test")
        syncUpdate(getTransient("PGP-KEYCERT-TWO") + "password:test2")
        expect:
        queryObject("-r -T key-cert PGPKEY-EBEEB05E", "key-cert", "PGPKEY-EBEEB05E")
        queryObject("-r -T key-cert PGPKEY-44AF2B48", "key-cert", "PGPKEY-44AF2B48")
        when:
        def update = syncUpdate(
                """\
                irt:                 IRT-REQUEST
                address:             Singel 258
                                     1016 AB  Amsterdam
                                     The Netherlands
                e-mail:              dbtest@ripe.net
                signature:           PGPKEY-EBEEB05E
                encryption:          PGPKEY-44AF2B48
                admin-c:             TP1-TEST
                tech-c:              TP1-TEST
                auth:                MD5-PW \$1\$12345678\$wza1MMo7nPUnBHNJMOMq3.   # irt
                mnt-by:              TST-MNT
                notify:              dbtest@ripe.net
                source:              TEST
                override:            denis,override1
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", update)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[irt] IRT-REQUEST" }

        queryObject("-rBT irt IRT-REQUEST", "irt", "IRT-REQUEST")
    }

    def "modify irt using pgp key using override"() {
        given:
        syncUpdate(getTransient("PGP-KEYCERT-ONE") + "password:test")
        syncUpdate(getTransient("PGP-KEYCERT-TWO") + "password:test2")
        syncUpdate(getTransient("IRT-TEST") + "password:test")
        expect:
        queryObject("-r -T key-cert PGPKEY-EBEEB05E", "key-cert", "PGPKEY-EBEEB05E")
        queryObject("-r -T key-cert PGPKEY-44AF2B48", "key-cert", "PGPKEY-44AF2B48")
        queryObject("-r -T irt IRT-TEST", "irt", "IRT-TEST")
        when:
        def update = syncUpdate(
                """\
                irt:          IRT-TEST
                address:      Singel 258
                e-mail:       dbtest@ripe.net
                signature:    PGPKEY-EBEEB05E
                encryption:   PGPKEY-44AF2B48
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                auth:         MD5-PW \$1\$12345678\$wza1MMo7nPUnBHNJMOMq3.   # irt
                irt-nfy:      case027-1@ripe.net
                notify:       case027-2@ripe.net
                mnt-by:       TST-MNT2
                source:       TEST
                override:     denis,override1
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", update)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Modify" && it.key == "[irt] IRT-TEST" }

        queryObject("-rBT irt IRT-TEST", "irt", "IRT-TEST")
    }

    def "submit a corrupted object"() {
        when:
        def update = syncUpdate(
                """\
                ertif:       efS71fqrHeon3UmZucEeHWq33sn6ZTsjYS0/tcAveIbaWHZfektufYhGBBgRAgAG
                certif:       BQI+Qj3OAAoJEL8f90FHHbvqhWgAnju4Q8Mz6M8zMcwUNqe80HcV6Wg1AKCA4El2
                certif:       Pxy7ewaH1vCBSsdwd6nwxQ==
                certif:       =Nphm
                certif:       -----END PGP PUBLIC KEY BLOCK-----
                mnt-by:       TEST-MNT
                source:       TEST
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", update)

        ack.summary.nrFound == 0
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
    }

    def "delete PGP-Key and modify mntr authorized by the PGP-Key"() {
        given:
        syncUpdate(getTransient("PGP-KEYCERT-ONE") + "password:test")
        syncUpdate(getTransient("TST-NEW-MNT") + "password:test")
        expect:
        queryObject("-r -T key-cert PGPKEY-EBEEB05E", "key-cert", "PGPKEY-EBEEB05E")
        queryObject("-r -T mntner TST-NEW-MNT", "mntner", "TST-NEW-MNT")
        when:
        def update = syncUpdate(
                """\
                key-cert:     PGPKEY-EBEEB05E
                method:       PGP
                owner:        Test Person1 <noreply1@ripe.net>
                fingerpr:     2A4F DFBE F26C 1951 449E  B450 73BB 96F8 EBEE B05E
                certif:       -----BEGIN PGP PUBLIC KEY BLOCK-----
                certif:       Version: GnuPG/MacGPG2 v2.0.18 (Darwin)
                certif:       Comment: GPGTools - http://gpgtools.org
                certif:
                certif:       mI0EUXAD2gEEAN0pQnR+zoMx1nrwEtqKtzs8uIp6f7zrTWwTyiGM5jWnQW0+2nj+
                certif:       2IBA3Rvdehyz8uUeEw5hd8UbqBb3cKZTTq669q4gaf4iFLcaaLZ3rqo1r9f2Qjg+
                certif:       DdoGjskpGWOA6sJvsdV7jLDGRB2WJpCLM3I9Ckm7d6gvZC33kCWQZ/eFABEBAAG0
                certif:       IFRlc3QgUGVyc29uMSA8bm9yZXBseTFAcmlwZS5uZXQ+iLkEEwECACMFAlFwA9oC
                certif:       GwMHCwkIBwMCAQYVCAIJCgsEFgIDAQIeAQIXgAAKCRBzu5b46+6wXmu3A/40xywR
                certif:       laNR+WZ4/OWrYLifvebXHyrCDjhLcUoM0njZKJeVSHFW8Qmh5Llerk7yDy1hcs5G
                certif:       gvsY3iQ2Uhq2vYhAEQWOZaXTg3Fb4UTzotTPPrLf0m1JXP9a28FI2ZkLrhGv1PlX
                certif:       ansEoXlWgnwPp9PV6HW1GUuIBIAqyanOCKYbmLiNBFFwA9oBBADUd1qRQ1/StzdW
                certif:       xkJlypQ5vedAwk/Oc7zRzv1Jzp96eTIMOpALzmlrKB4+pJELYcYCeFy6zagoqFJa
                certif:       y77IqFfo5V8EXlW9IZj54jiLtz1+G/LSSabOs2d5DdjT5ihmPolyzWepWk5SOaNq
                certif:       LJNgc20FVc02d6bwr9a0RKbgc7UyvwARAQABiJ8EGAECAAkFAlFwA9oCGwwACgkQ
                certif:       c7uW+OvusF5m2QP+LnB/M/VI0/AHycrkZBKoy269jf8F1wkbK7gSe6rVXVFpm06L
                certif:       LhdmBbGrZhapjXb+7QhS0XL4s3tfoDCXjO3FnY2uAfDmfRmxlaMuEajDnFQyQc3T
                certif:       KepilxrKtIkIb31RcHiefTcOSB8a2hKwGLK6QTo/X3bzSJl4NonvAArJITs=
                certif:       =iWqc
                certif:       -----END PGP PUBLIC KEY BLOCK-----
                mnt-by:       TST-MNT
                source:       TEST
                delete: test delete

                mntner:      TST-NEW-MNT
                descr:       MNTNER description changed
                admin-c:     TP1-TEST
                upd-to:      dbtest@ripe.net
                auth:        PGPKEY-EBEEB05E
                mnt-by:      TST-MNT
                source:      TEST

                password: test
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", update)

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(1, 0, 0, 1)
        ack.countErrorWarnInfo(1, 1, 0)

        ack.successes.any { it.operation == "Modify" && it.key == "[mntner] TST-NEW-MNT" }
        ack.errors.any { it.operation == "Delete" && it.key == "[key-cert] PGPKEY-EBEEB05E" }

        ack.errorMessagesFor("Delete", "[key-cert] PGPKEY-EBEEB05E") == ["Object [key-cert] PGPKEY-EBEEB05E is referenced from other objects"]
        queryObject("-rBT mntner TST-NEW-MNT", "mntner", "TST-NEW-MNT")
    }

    def "delete key-cert object that differs in generated attributes only"() {
        when:
        def update = syncUpdate(
                """\
                key-cert:     PGPKEY-D8F37DA3
                certif:       -----BEGIN PGP PUBLIC KEY BLOCK-----
                certif:       Version: GnuPG v1.0.7 (GNU/Linux)
                certif:
                certif:       mQGiBD6BsrgRBACJW9zbjuY1fuIAOWGGWRpEzaCfdY/ixvwJ8WpQ0LhhGtdjFmCb
                certif:       S2FAQuwTWcbHPlt+1dYi1aZcPzU8P54CI3yOl3aO7MToe7YkjY9ANzy9WvxRQcSr
                certif:       n/jM50ugKcHTK6ounkpj7mAcUvoeL4WQjLmdAojgQNCWCYlcVXg5i9pRhwCgmYTI
                certif:       vZzJclgpMpORIpuOxiKh2h0D/0o9FYAoYBB11FKnRsOAC399ZdmN4gYel5CFJNoV
                certif:       1pasVdCznqkjxODe/3VyAFSX1RPrVd0kuTxb4qmnMYr0nekznNMg5TYpRg4Uulbz
                certif:       oMoso3mrnrJ7U6doWE143YzGSEzyaD5lzTZ3uacWU9DjzaDAsmhCeLqklfocPfOb
                certif:       F4eZA/wI6F+kcGhRjwmfzh4iDiB54dlLIxHS4/vPgqmVDfXmqYx7o2Eks9S145+z
                certif:       yzks35AtDk7k6JYEB4xGeMZKvf9/V2E6lzo3a8OKqXcI9rHAeHtSdb+cBxCCd6zf
                certif:       cO0eWQvkeHP1yucFDOiDOEpBJjuWcpawVJrG+Gq6m19sEswWxLQkVGVzdCBQZXJz
                certif:       b24gKFRlc3QpIDxkYnRlc3RAcmlwZS5uZXQ+iFkEExECABkFAj6BsrgECwcDAgMV
                certif:       AgMDFgIBAh4BAheAAAoJEJrYZFrY832jW+4AnRXEk4fOtix8ynErwsh68yrss16H
                certif:       AJsEYkdtXwMM69Nd+JWsiEk+48ns3w==
                certif:       =3+2Y
                certif:       -----END PGP PUBLIC KEY BLOCK-----
                mnt-by:       TST-MNT
                source:       TEST
                password:     test

                key-cert:     PGPKEY-D8F37DA3
                certif:       -----BEGIN PGP PUBLIC KEY BLOCK-----
                certif:       Version: GnuPG v1.0.7 (GNU/Linux)
                certif:
                certif:       mQGiBD6BsrgRBACJW9zbjuY1fuIAOWGGWRpEzaCfdY/ixvwJ8WpQ0LhhGtdjFmCb
                certif:       S2FAQuwTWcbHPlt+1dYi1aZcPzU8P54CI3yOl3aO7MToe7YkjY9ANzy9WvxRQcSr
                certif:       n/jM50ugKcHTK6ounkpj7mAcUvoeL4WQjLmdAojgQNCWCYlcVXg5i9pRhwCgmYTI
                certif:       vZzJclgpMpORIpuOxiKh2h0D/0o9FYAoYBB11FKnRsOAC399ZdmN4gYel5CFJNoV
                certif:       1pasVdCznqkjxODe/3VyAFSX1RPrVd0kuTxb4qmnMYr0nekznNMg5TYpRg4Uulbz
                certif:       oMoso3mrnrJ7U6doWE143YzGSEzyaD5lzTZ3uacWU9DjzaDAsmhCeLqklfocPfOb
                certif:       F4eZA/wI6F+kcGhRjwmfzh4iDiB54dlLIxHS4/vPgqmVDfXmqYx7o2Eks9S145+z
                certif:       yzks35AtDk7k6JYEB4xGeMZKvf9/V2E6lzo3a8OKqXcI9rHAeHtSdb+cBxCCd6zf
                certif:       cO0eWQvkeHP1yucFDOiDOEpBJjuWcpawVJrG+Gq6m19sEswWxLQkVGVzdCBQZXJz
                certif:       b24gKFRlc3QpIDxkYnRlc3RAcmlwZS5uZXQ+iFkEExECABkFAj6BsrgECwcDAgMV
                certif:       AgMDFgIBAh4BAheAAAoJEJrYZFrY832jW+4AnRXEk4fOtix8ynErwsh68yrss16H
                certif:       AJsEYkdtXwMM69Nd+JWsiEk+48ns3w==
                certif:       =3+2Y
                certif:       -----END PGP PUBLIC KEY BLOCK-----
                mnt-by:       TST-MNT
                source:       TEST
                delete: test delete

                password: test
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", update)

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(2, 1, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 0)

        ack.successes.any { it.operation == "Create" && it.key == "[key-cert] PGPKEY-D8F37DA3" }
        ack.successes.any { it.operation == "Delete" && it.key == "[key-cert] PGPKEY-D8F37DA3" }

        queryObjectNotFound("-rBT key-cert PGPKEY-D8F37DA3", "key-cert", "PGPKEY-D8F37DA3")
    }

    def "delete key-cert object that does match existing object"() {
        when:
        def update = syncUpdate(
                """\
                key-cert:     PGPKEY-D8F37DA3
                certif:       -----BEGIN PGP PUBLIC KEY BLOCK-----
                certif:       Version: GnuPG v1.0.7 (GNU/Linux)
                certif:
                certif:       mQGiBD6BsrgRBACJW9zbjuY1fuIAOWGGWRpEzaCfdY/ixvwJ8WpQ0LhhGtdjFmCb
                certif:       S2FAQuwTWcbHPlt+1dYi1aZcPzU8P54CI3yOl3aO7MToe7YkjY9ANzy9WvxRQcSr
                certif:       n/jM50ugKcHTK6ounkpj7mAcUvoeL4WQjLmdAojgQNCWCYlcVXg5i9pRhwCgmYTI
                certif:       vZzJclgpMpORIpuOxiKh2h0D/0o9FYAoYBB11FKnRsOAC399ZdmN4gYel5CFJNoV
                certif:       1pasVdCznqkjxODe/3VyAFSX1RPrVd0kuTxb4qmnMYr0nekznNMg5TYpRg4Uulbz
                certif:       oMoso3mrnrJ7U6doWE143YzGSEzyaD5lzTZ3uacWU9DjzaDAsmhCeLqklfocPfOb
                certif:       F4eZA/wI6F+kcGhRjwmfzh4iDiB54dlLIxHS4/vPgqmVDfXmqYx7o2Eks9S145+z
                certif:       yzks35AtDk7k6JYEB4xGeMZKvf9/V2E6lzo3a8OKqXcI9rHAeHtSdb+cBxCCd6zf
                certif:       cO0eWQvkeHP1yucFDOiDOEpBJjuWcpawVJrG+Gq6m19sEswWxLQkVGVzdCBQZXJz
                certif:       b24gKFRlc3QpIDxkYnRlc3RAcmlwZS5uZXQ+iFkEExECABkFAj6BsrgECwcDAgMV
                certif:       AgMDFgIBAh4BAheAAAoJEJrYZFrY832jW+4AnRXEk4fOtix8ynErwsh68yrss16H
                certif:       AJsEYkdtXwMM69Nd+JWsiEk+48ns3w==
                certif:       =3+2Y
                certif:       -----END PGP PUBLIC KEY BLOCK-----
                mnt-by:       TST-MNT
                source:       TEST
                password:     test

                key-cert:       PGPKEY-D8F37DA3
                method:         PGP
                owner:          Test Person (Test) <dbtest@ripe.net>
                fingerpr:       D8AA 8E60 8EDE D810 59B1  3AAF 9AD8 645A D8F3 7DA3
                certif:         -----BEGIN PGP PUBLIC KEY BLOCK-----
                certif:         Version: GnuPG v1.0.7 (GNU/Linux)
                certif:
                certif:         mQGiBD6BsrgRBACJW9zbjuY1fuIAOWGGWRpEzaCfdY/ixvwJ8WpQ0LhhGtdjFmCb
                certif:         S2FAQuwTWcbHPlt+1dYi1aZcPzU8P54CI3yOl3aO7MToe7YkjY9ANzy9WvxRQcSr
                certif:         n/jM50ugKcHTK6ounkpj7mAcUvoeL4WQjLmdAojgQNCWCYlcVXg5i9pRhwCgmYTI
                certif:         vZzJclgpMpORIpuOxiKh2h0D/0o9FYAoYBB11FKnRsOAC399ZdmN4gYel5CFJNoV
                certif:         1pasVdCznqkjxODe/3VyAFSX1RPrVd0kuTxb4qmnMYr0nekznNMg5TYpRg4Uulbz
                certif:         oMoso3mrnrJ7U6doWE143YzGSEzyaD5lzTZ3uacWU9DjzaDAsmhCeLqklfocPfOb
                certif:         F4eZA/wI6F+kcGhRjwmfzh4iDiB54dlLIxHS4/vPgqmVDfXmqYx7o2Eks9S145+z
                certif:         yzks35AtDk7k6JYEB4xGeMZKvf9/V2E6lzo3a8OKqXcI9rHAeHtSdb+cBxCCd6zf
                certif:         cO0eWQvkeHP1yucFDOiDOEpBJjuWcpawVJrG+Gq6m19sEswWxLQkVGVzdCBQZXJz
                certif:         b24gKFRlc3QpIDxkYnRlc3RAcmlwZS5uZXQ+iFkEExECABkFAj6BsrgECwcDAgMV
                certif:         AgMDFgIBAh4BAheAAAoJEJrYZFrY832jW+4AnRXEk4fOtix8ynErwsh68yrss16H
                certif:         AJsEYkdtXwMM69Nd+JWsiEk+48ns3w==
                certif:         =3+2Y
                certif:         -----END PGP PUBLIC KEY BLOCK-----
                mnt-by:         TST-MNT
                source:         TEST
                delete: test delete

                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", update)

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(2, 1, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 0)

        ack.successes.any { it.operation == "Create" && it.key == "[key-cert] PGPKEY-D8F37DA3" }
        ack.successes.any { it.operation == "Delete" && it.key == "[key-cert] PGPKEY-D8F37DA3" }

        queryObjectNotFound("-rBT key-cert PGPKEY-D8F37DA3", "key-cert", "PGPKEY-D8F37DA3")
    }

    def "create PGP-Key with mutiple public keys"() {
        expect:
        queryObjectNotFound("-rBT key-cert PGPKEY-459F13C1", "key-cert", "PGPKEY-459F13C1")
        when:
        def message = send new Message(
                subject: "",
                body: """\
                key-cert:     PGPKEY-459F13C1
                method:       PGP
                owner:        Test User (testing) <dbtest@ripe.net>
                fingerpr:     F127 F439 9286 0A5E 06D0  809B 471A AB9F D83C 3FBD
                certif:       -----BEGIN PGP PUBLIC KEY BLOCK-----
                              Version: GnuPG/MacGPG2 v2.0.18 (Darwin)
                              Comment: GPGTools - http://gpgtools.org
                +
                              mQMuBExtIfMRCACe8xHPWBciPfCkdN+4TNUPW04ahDdlSJk5fmzaFcx8GnJILvzt
                              +0Vbs4HPLUj0yJQz0ZXz+8EPqzs/sqBYZjh5doyGFqXG/Q3oD4Yxru9+msvTQSd4
                              yWQUl+2C/wYcomhHp3pRRbbLPn4/UYxOQtOeoOP5inr9DIPzQ4Ejz744DSSR5SAN
                              AuYhFqN6XxOqQ04RxdlvxMQG3KIVsFGZ5IUBaY3ZplO/ul65nYgyE9jujaBBF1O6
                              OvEIUIHE0CwHReCsNSDZgS89lWfMDKFBcmfCP7hGfHE2sOWcTGVC38aaPjFQe76c
                              WIXp191xySc71qVUeuKMWBLWLwSX+jZCFxWDAQDzDYJSwjdDvtGvF1X2ddcsylki
                              /pK7uz5E4AuVzxoaSQf/WfSpc6VfWPIXkbPvstGq8sxx4cClylur11bpQ8ZsqzSL
                              EBOK2v+f2wnRGJAXy+Jq3Ur+mQhfZJiq+sM4gCCcVl9/uKnQrGWYKnZ8E9v3q0ot
                              O9i0/23HX9oFK8A11q6eJpXF89Y1cUwHVmAGaDoqMEc00vHYSZc+TMfzDR1PW9AD
                              O8wzkL1FYzSX8/5iV73PPpy1K4QCecKt6ejXg85WpEbl4HCBXotcQLL9J0+NFbaX
                              bypy0fvVTYQyHSbY5m5UhHXd7VHFo86UvUP6mym+UjfULMvScKf9DpjWqNEcjdoG
                              D1tMCguMV9bgM/vG6K35U5DOeA8JdTIy7jPNHDsVywf6A+pawnU/rDhUvlUIea6U
                              mbW08vZ9DyUAtCtb2/BnZ20FDBCqp3OdIfjg9XLnQrI8GdQlfVDDoEQxy+9QhjM3
                              JFB0apZIjvrMfdALq5+ywH9I/4xn7GPiIg2LDkFMtFCVA1ON6HNJz1flrjELt1cf
                              t3z8aevkAdBctsppzSZjdGigJcBoQunhE2E/JHG7hcun6LAIYyZXec5KbJjHUJq+
                              2YcM6u2T2gsE3FR/fIIh4JJ/Q2zI41R3m4Ao64XU0DtIYwHUo+6JcKi4d7aIiObA
                              8mmdLQesyamdA/E0taUu1htRY64yeWicgrBtKbY40/GKiW+4h5HIUjyl6rJW9mvC
                              LrROR1BHVG9vbHMgUHJvamVjdCBUZWFtIChPZmZpY2lhbCBPcGVuUEdQIEtleSkg
                              PGdwZ3Rvb2xzLW9yZ0BsaXN0cy5ncGd0b29scy5vcmc+iIMEExEIACsCGwMFCQlm
                              AYAGCwkIBwMCBhUIAgkKCwQWAgMBAh4BAheABQJNNNmvAhkBAAoJEHbXjwUA0CbE
                              ROkA/1HxARfN23sZi9s+7Si1YlSsVCiokXUbFKSAwGb4W+osAP4n62nfLY7i1n1f
                              lY+FspLmP7BRIoTGRNNpmqED0BQHC4kCIgQTAQIADAUCTToVKQWDCJkOSgAKCRDa
                              hwwTRqlXsIGLEACApX2L9d/A4HLewtw0xradGEr5THysBrE3wMxjeXBBF2/0jVsW
                              qrTjpLZE/Jc6xH8qK+X1j6zP6nQrHenWJnSMM3cGsqqW2hjB5+iMNqAgm0I0sOAG
                              NX8N9EOpy/r8/IV5w/3maoPOfEdjIcQrrsFu9EPk3i2dEMWDY8p+zAhuNcit5v6L
                              3dNFQxA34hWfTGsditdojefTkDTpcgc+yeatKHWU7LVj9NEwsU13M5j5m4c1/Vmj
                              kf9z9ZIY53LyjpFYOIFO3q8ZTWMSbc1N166d0vTGkBAcoN9ADhBHL27sB2vxDV5B
                              lb9uAK8CIZSU4+eb+02T73os2JdtTPYHQwUpq8mSanZG0hyOsNm5AKkrIQcgPdyQ
                              V06uhPuOdd7aosF9UXRSoMLaAyphL8h2zCyWOfiSnrVhCIWmqLIP62DhFPInf3yB
                              vmV90DeIFJS03tcEC3JIXzBid1bHEQBu703ij7n3NTBiIk5vO9PdKa73sxAopI0i
                              egECcMeyue07si7EyVMR2lHyLSVcZWPRBv1jepsQLryOXB2KwpQCuoqg3MN8jMLy
                              PUDUbUXmoesQc6jZXCmd+wQKQMY92331CoQXlOen+G+OhAcCJ6tZ57gLrEVVuFL1
                              1+QiqDauEEa3/soyPZC/QgsvsOK2PJmFaMfXEtZSNpz8HqrR9bO/NEzCKIhMBBMR
                              AgAMBQJNcC31BYMIYvV+AAoJEN0ckHpQ/p0yVa8AoMumOpcCY40ZoUTuZkeh0Ilg
                              zUBHAKC18+sUZhnR0ZqhEFNUXjOa+ElHfohMBBMRAgAMBQJNcDSLBYMIYu7oAAoJ
                              ECo4IGJOjjyFAi0An3FcwwJDq/yPCLryj1rmnwb3Z1T6AJwLGwSnIRJSlrn5+YIR
                              7XdDitDhDohGBBARAgAGBQJNcH14AAoJEDbt/tCleo76Ab8AoLjiCT/OvkRYSpoj
                              mufWBkXZXMrXAJ4z4ygpUU9YF/QpFeMHT+ZmV0VNPIhMBBIRAgAMBQJNcKEABYMI
                              YoJzAAoJEOrAwtx5kch1huEAn28v73FgzL2l80nEMh7YAVFJ/NqlAKCNbvedx1VI
                              naSaLJisyB3+Ma/7iIhMBBIRAgAMBQJNcKHABYMIYoGzAAoJEKbkO86DXeH17MMA
                              oL0Q9AFJmUMOPX/vcKngroyrGCNxAKCHnjDjVp5vjRvJ+eNkV2hEuBFrO4kCIgQS
                              AQIADAUCTXC2VQWDCGJtHgAKCRBk9yqkNJ4jA3D/D/91ELWv5VhYM2uMWnmR0a5X
                              GRK4ZMmeNNCwiL2C6fiUb8la9cUTVBG0CEsE0EoxoAqIaMraBkBNJRw5gjKLbS9R
                              6S72FMIluVJr2Du5P8K8VhzNUSo3rfDUfcE4CNnrIkxWX3/YFJOnjAjP/rdXKSFa
                              jdsr/oe+IrlpPI/ZZe8stkQ9S9t5eII77prtwqgxf9T4/ZHHYDLGzitw2FZOw1Ox
                              ttDSeQX0S9N+fbCgWLSRwYSvAf17Lw+y+a3q4CdALFccXQJSRcI5f1o8Z1TOcgz0
                              5hU2q/8JbBzbDKT8XydX1eqGHxRUsVZG201vDGeLm8YxE36oPn8tSKC5N1lJvXb/
                              GiCh5NtWDpKsTMGNi5kLvQxNXKvUOSwcRkeQmzyyExD8ufuelx0TqVxUEA3dYtSw
                              5RB2hrmeyiCmMJlC22N2tcTA55rm+M+sqs1J5w5o+glYvs8wOory9xa4sBeSiah1
                              meOqg0Ji2Tdj8ArmAWf2yAaQ7C2aTyoe26U76uAAnluJnBIVUH9gjeBGOrAd0hC8
                              rwepwVB78Lm2BmbPDz+WoCOJDGOGkEZvf5tTQhnIYmb1mYYbCVvg4Fgws7bdZKn3
                              yeCBnIxiLt9L0Pk2XAEhJ6/s1PL7aYgwTepg/bcNHKEHCkvD94bvUK6u9jVvA/zB
                              i+/rNjzU6uy0EzLtJj6894kCIgQTAQIADAUCTXFHwQWDCGHbsgAKCRBaVLu4eCJe
                              CGbZD/9rSmmxKmecbAmOfoyuPBskvDvA1MBN+tksN8ZCjQyuItA6SNRk2rEdSC8g
                              NvY3v/g+q6+HnzWSbgXqN4ia4mrur6uL2UX08qBYglnDDN5hzCKDEtC7Uhc+hjOQ
                              8J7XhiJPT7KR1BI51i82ANYFoKOxDJ8MwTmosEyvXjltYV5kVhOyJ6PJKzItUyGO
                              PMq6Xl5/BRyQqdrkpO1QHA3L3mUnQG7c2Ud0tvky+UmwfLWammGTyRlUMoMey5Zj
                              pyEDgk6yxuHLgWtjxnQ/ouGhsXImRlnzYf5ktdaFUwlheB0afETwsI4IR49KLnT6
                              v7VEZcsmxDkyIUOPQZjAm+vJbBUB8WbpwGTX7D+pERm/ISCYr0yCKfQVDKE1+2RT
                              p0FUwT5zFH2llnSB+LkKtI3NLwaUSyE5JCCeq7nkWHFwrG5/fyO2Z5N4XHmjaXqY
                              xMerwIFmk4fM9iEhicsDZXqcMFRS7mkAvtbMXjPwm0xxjx8CH4xuVXTEs234Pm+k
                              QDWjPmE//9J+YlIfgY2rdCGOaTD7+SGJwr4YO8r3/eJJG81Hslf1hDLVixMfcia3
                              0npgGqj0zmEp6hWQIP99DIdWO4fYtw378xpEw0V8Xlc01+HNiM9rm6drR1Q+J57m
                              urtMCpdronrR6z9WjKWWJY31lBkdktcdwqseEgREX+pJwBw+B4hkBBMRCAAMBQJN
                              OhVVBYMImQ4eAAoJEPAEkOHBTWtrqhgBAIwJy1CV/uuY2gGFY+2iY6VsGgXRbCz2
                              TzA8CMIKk8VxAP4z2LJnzEo1Kk1qf/g4mSgW9WTRiI4TFXdZwCoF3jjqHokCIgQS
                              AQgADAUCTXCXgQWDCGKL8gAKCRAy5nrarOTt1gIXD/9C7w5I6CoJ2FE2IkJP8WRp
                              ffIM1rU0sBBPh37Lwio2iajSPLT5e+6KcBmoMXXO348qkfOUcu2WZqEkEA/nBVyx
                              wuofHHnh+k+p2KNULYy5afjuBDu7H/nmH9I4b9esRqLE6Y/EzTB8v540AUCEvw0O
                              ZWZfRWeRlxEznl3PuWMCutnhssm4B76jkZvXgyczZxDxxOpwMoQhoXkO50Q6VgPP
                              R3k4vuRqTqllmvByjxm4B7pbETF82p/S8UGo8sfcan5d3bVEikwQf3nELJopax/x
                              +R7ZbQGoTFESNY+zUlxZ1zWQmhPan8AlOyyh7FUTtFC7qcjYyMwpgZtgbHETtvlc
                              kV8DWa8PxxLccTf7ksQfX6hAv9z2hrw9hymm5Agu3h5PXGafE56CntWJYfq7gri+
                              Qt2gtJLe6D8wv6ZnuBdeW3u/vP6dmZIvQ0DTgael4ksKoEoh0hig8QPD7zExSUf5
                              3Iafp/p4thPh0aYL/gcQL6M+lGzAoq8jDA8rGa0MOhZlkEtXOJpA/UtxqtPCbABU
                              2+4885n11vwNeV+SnranRIQ5o5W/Ams3AXAU8zetmwrI2iLsEsc1ku8lrBHTHV+I
                              wZFHmbZXa2sQqkkAV8dynjt/hqf85t81OTuNCgCtj0dZ4iDpaHcrvMYPCiSweSoA
                              zPs7tNYZOKq4s+2IlMD5BLRNR1BHTWFpbCBQcm9qZWN0IFRlYW0gKE9mZmljaWFs
                              IE9wZW5QR1AgS2V5KSA8Z3BnbWFpbC1kZXZlbEBsaXN0cy5ncGdtYWlsLm9yZz6I
                              gAQTEQgAKAUCTG0h8wIbAwUJCWYBgAYLCQgHAwIGFQgCCQoLBBYCAwECHgECF4AA
                              CgkQdtePBQDQJsRU3AEAvFkGuZWss09WEOzgE3CdoURSTTgevd26vkRlJg/GYioB
                              AOB3Dm3ZeDcQhsz8MwP/YgYvzLLKmkVl2VX/y9WK9vbaiEYEExECAAYFAkxtIrUA
                              CgkQ3RyQelD+nTI0xgCggSIUHv5TjEOGyxptCqwvA/SXtZoAoNVlvx7HgKHbLJi6
                              +G8kNvmB6dSOiGQEExEIAAwFAkzbnyEFgwj3hFIACgkQ1nmLjHgqz5CzRQD/QaMA
                              oY/13mcxqIddEv9PZy/XmVbqzUR1WIJGoMOfyggA/A4hfhDgWqIW5O8bEWH4pCwy
                              esNYwxCGEXBO6BqHVlf6iQIiBBMBAgAMBQJNBJQ8BYMIzo83AAoJENqHDBNGqVew
                              TJsQALe+NvsBS6q3gXa6vKpHHyF3GvvWNhoMRo1Vm18JErGjoCcOZk0CYJqAeS4j
                              zwGCUQVo905WP0sOu8dJXHaa46MqFa3xE9quAdbpTLeXWED2HGbM2lz5Ff8ryoa7
                              KvT5QWfIUSoc2nKtmzSt/VHEbtRsLHDEy8YXPHCSsxS2Zs1RETUeETKj/TkjdiAy
                              J1vL/Fnr17JKxpDsAKnCV94S3NrOT+3sNO/5XPWQgvfhtS3dhdGRN31TUrXw6R6T
                              Ev30BKJu2jvgK+L+Dqm6B9bp73VEElnKpBfMS7EzdV5hsPGJVuii8U0aZNPKgjiq
                              4QZ5M9aFePbBrl/85S0TGYT6gRK6c1pjjYZlGDdyxZW/gJmlQFASyStwTbE0Fi6/
                              n0vjYxo1cWvlfEDOQKotshR/6BfZdYFIAx39/TD5m4I2r2E70kcE44hs3Tb+TAB2
                              EStTZN6UvnJLRAqCzf4irmf4jlMbr7qQG2xz7ML8jwPRB12iPGMOG2nCFjhMORCe
                              qWmMjrwcd5zims8wko99G5L2U10eHYjkaXNmbArFJ2EnITUuzUbxtJDVhmDk0yy2
                              c7XjOhgdLctprD9UCa9ZXvCzEd94l+mTj8CrhJiRz9pSJPa5bSUrStM9ZcoeMt4y
                              hgUEwBKldRTKrRcBiVMYEizGQ84ibh986/x63FnAjZbTGM+ZiEwEExECAAwFAk1w
                              NIsFgwhi7ugACgkQKjggYk6OPIWr0gCg5BkbQezK5qpO/Sr+neaV/H6kncUAn1c4
                              Xc2gBCte6Lfp9G60Pwscp94jiEYEEBECAAYFAk1wfXgACgkQNu3+0KV6jvpDQwCf
                              dqoMsEUGdwISXIZKDpcYVuPceg8Aniywf6kIsMLQ3WclYihbRTVX4NU0iEsEEhEC
                              AAwFAk1wocAFgwhigbMACgkQpuQ7zoNd4fUlNACWMySpYkExmFMZp1JaR7uPDAzV
                              twCgng1qfRDisPnlp3AUC8Un1jS4V2KITAQSEQIADAUCTXChAAWDCGKCcwAKCRDq
                              wMLceZHIdRPYAKCem3cB/mvri3ENTD93CMHw4VfU8wCgxSUymzQ2rM4DCwP0ho0f
                              rWtzYoOJAiIEEgECAAwFAk1wtlUFgwhibR4ACgkQZPcqpDSeIwODNg//caMbnHFV
                              KitU4ldRWjn7ZRlay8f/d8sepl03YeQN62HHL74iQ65ugLCrYdIoftdCcmRs2x12
                              jHRtTGQFtyxhQfF39Du4MHlnn6tsXbPsl8shsT9hAW2kijMcOVcRkoZIl10eH6PZ
                              7vZLueytlao+H1Biq6yNLXQh9Ki3RN/shXqvOTjmFSw6l97fiXq1kSI6tsPL69ur
                              yajv4c/Y4yb8e2iqRyH1PQOjLuBQfssBPiMF9/biZxAfNaWOg5rujueP6Ir3igNd
                              rTZ6Y2791uTk36uLcCrWuEPIE2kdfggXdYJv8Od9srJrQ3sOSSewQfDAMsNG2L5D
                              HAvVjjBJ6M5CLY4Lh0nOZmePwMqBi2YmjJkfAj23pI+w877PHVIp1OAjpzzoZJOm
                              vY1opMsLbb/XtryspHwfADk6ZZqUujEdl9erwnrMi63gDAylihwIibp6Oato268B
                              eM9ObJrRpdEgsPcCWDN7ScHBISDrX/GakAabOYrrIsE5TMsgZUo3MsNpnWY1QmlN
                              b2F872bm91ayzU8OQ3LhrR4dDjab9mCxw2ZoFwwvkpg7NO2PrC4/NFBRqNIIY/2F
                              jeLbICMCqiNRZ2Tro856pehue7sLRZ6fEAuT3iCdZATA3qEw/Qxj5T9eI/BvZe2H
                              w1gI57t2zQLetPqdr1DA63t5ASMzmS/DBzWJAiIEEwECAAwFAk1xR8EFgwhh27IA
                              CgkQWlS7uHgiXghfOBAArDlrNEVxwSdC+ipQa1Hh5mRDmNUzo7oGb3rQuMeuQl6x
                              l/hbPFaqlSeXFLvZSkEDbG4OqS7MNeoqqoGqlLV8pwOiTqGWtFrwf/E+CIIR10iY
                              a2RphA4wvkvU9SGoXYxMe7WGg7ShUV2GrorlivBh4ibpBJaBuUkXRr32GUDSk8D6
                              Ix6MP6W4n/QfCJOdxaR0ouMt4+Xw5c+4XgeF5QiVgct09dYuvp+98wBMxIvP1x3z
                              pCLzzqmGSr/0zbsLiTxv68A+tbAl7AvrKq22ljtc/QUX7xDxjdx/ZUfMapLLaZXP
                              aO1/XpPMO0VpaQLXFuoF54YzG6r/p2un4tmqjyeeSf15CQZ25uwPNW4/2ZoxoJ/b
                              Xovk5oZ7exB8POzT8AXKqDShAEstGmNKD9MFmeLNHE/P0rbg1SOi6WlzpxySh2Z5
                              e4hZ68P7evVPPmrHQqXFFGOigTKD7+0hC5oJI4+ckRIjKNFoZcq08dKMMUubdJHI
                              HiWG4uax+ZNIflp3DsmHK0X6azsECdwjyxoORFK1jouhDiLc1n4SJF5RZ+VQ4ilF
                              e2hjxYnVzD7HwAeRIwlDxCCzqasYop5W1MFFy8FhytZ2n+BumZcybWOow1FA+Pkr
                              rOfl9UmO1jbZIBMr0B2dTmeia3kzmdDtxDk3Q47r4a/QXBDGUcZS8gTZpFhIrzmJ
                              AiIEEgEIAAwFAk1wl4EFgwhii/IACgkQMuZ62qzk7dY2KA/9HCIFPgwDFa9/2zpN
                              vKyBrOrbgtOCMz2JprsOw+f3KjyGB858BO3+NmtJQ/D1H9aRfy1PW8KwCc/a9PlT
                              6f89FuwTv1hfCSveeOUHEP0u3UIJwJMB1aQc3YXNAlTfVoEAOtusL7AbXwSj52Ut
                              uAEvqdaM5FF5Erog+VUyVnUlVWU2kUzQn0ib9KVC28izKzjGEiCr0EtR3B927nvn
                              iWIh/FvqjIAYWVKGk300jzStf1FJcYOfJhAqBKZ6t1nJE6smtd8ZTA+86FTe4Cij
                              +52LsKRFAxDFBBMpTr5Ne6Z9QpVt+ItIE0ahLD0rcYFuRKfo1g7IL3r/i+JuDIOj
                              V++ixMY39iBYot4O5AOswuAaixvqPrBkF6sOtMCxNLr3p4BlXaerpiVYQENgodnF
                              cUKI0nE0g/FSO8USboxFVWH66YR0DMyWFFldSgRRcvY5bdyFUpax5gTGVYhMQV94
                              g3qje77TzirvBQyP8vqzlLdOaMu5FSWNsXwwYQiJSufcI6b/1wP7+PMkEfvsOO66
                              e9v1C0264k09FzldNET29D8WEh+ffBpoJCOzU513ZHpy6vvGdfKzs5E0DCkD+gFv
                              nkiLm9kHrsPVYAn9n5Ts+WlfdUAx5pfwhwmFn9pSCK+Ti03EjRL5kC6mEoGfWA4r
                              hVtqyvzEVgGyj8zTdYqDfSDvSS25Ag0ETG0h8xAIAJRp6DW+NamD4fxmZ14lg6LK
                              tqw18nyZj5MhWRxT/O8xzRwox6W67GKL30lY4SiqZZlRYwh9mYtNyR9Ix9Mw73p8
                              oZqq0pDCHXIPC1EjgbnxlMEYtX1CnUCwn/Izhatk/b1OPOiDvQ/CCpKc9vWJpPNy
                              q4w92Hjv3jwgYV1+e+kA/HYrRUipTUJ7blnNoGxfpp+WbJr9O5EeOBkyLHMP8sGE
                              AGJQeB7cWMKQGZG00XsbA7W7fW/NQVJAfGs5AAtjDCROnAcILeWhe+YhkYpaxWUJ
                              WDTRJQEJPibu0DirhUQwhbijSenLomSQjw62D0mvqC/ILxhcjz5ndrc5vPdwSVcA
                              BREIAIWDzQ55WG4TG46879v8BiC8CMyKlSSuidrXADWJvJShxVY7EigSYoGLDBK1
                              0XYLTejFFDaqN0Bk5i87jagr3D8tVj1lviQivi8zjF54qY17Vi8MNefslWMEMSGA
                              MGn0Kd/Ap6jDnOasB6Z2B7C1aQO5bv+KAPgoduqFCAr3R9Lhkp5evoRMnNPbduBM
                              h0u/ioo4TXSy1QqXgzeH3V2RXpsacSpw3IvPPLikVkN/l2qv5SuO9lWL2Cci1ii1
                              ocIRisXicCO3M0rW4pLaNrqCYQ6nsP84kttnpcbxKeludUPWWVfhJcY5ooNvrPQ7
                              CKUfvAQzjR/FylgfUD34gJ+wsoKIZwQYEQgADwUCTG0h8wIbDAUJCWYBgAAKCRB2
                              148FANAmxIFkAP9WDPMOIaC27XuriVk9ZTA29qbSPbuHAbfJBUDJUQaUFgEAv4hc
                              G8hEwsWbB0FD01ZPjkkMaPY/bpECewaPNuwnTlyYjQRQzxa0AQQAueohXcwatNmn
                              OymxQ8WjOU1i2zYorXfoi+Yc2PJI1KWnyKctNOqE5T7pt2lBiwsk6HOUXCgf9Fd0
                              Ki5EnrherdWCKfGqlh/DDk8q47cwezqGQFmC150j/7+pwG8rg9ZM14PrERa8oVmI
                              E+yKNTt61duj4raFA5K7YutsOdwsWQEAEQEAAbQyREIgVGVzdCAoUlNBIGtleSBm
                              b3IgREIgdGVzdGluZykgPGRidGVzdEByaXBlLm5ldD6IuQQTAQIAIwUCUM8WtAIb
                              LwcLCQgHAwIBBhUIAgkKCwQWAgMBAh4BAheAAAoJEHLE5sNFnxPAs+QD/ja92NEp
                              DfNCYVhyOzMLLkv8wuWPiBiNmQ4kgJW50szPdzVm11rklpS74qcVnrrh6RUvEslj
                              pygfU31vFYo1LMpqgTHhaFXw4caTbF1KQkcrzzt/hRikDjTzknHCSDOWPPEDF8t/
                              UZC5I3Dd5jRvdPYYOBdFDBxKXjF25+2jTMRF
                              =4TVE
                              -----END PGP PUBLIC KEY BLOCK-----
                mnt-by:       TST-MNT
                source:       TEST

                password: test
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.failed
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 2, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[key-cert] PGPKEY-459F13C1" }
        ack.errorMessagesFor("Create", "[key-cert] PGPKEY-459F13C1") == ["The supplied object has multiple keys"]
        queryObjectNotFound("-rBT key-cert PGPKEY-459F13C1", "key-cert", "PGPKEY-459F13C1")
    }
}
