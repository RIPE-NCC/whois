package net.ripe.db.whois.spec.integration

import net.ripe.db.whois.common.IntegrationTest
import net.ripe.db.whois.spec.domain.SyncUpdate

@org.junit.experimental.categories.Category(IntegrationTest.class)
class IrtIntegrationSpec extends BaseWhoisSourceSpec {

    @Override
    Map<String, String> getFixtures() {
        return [
                "TEST-PN": """\
                    person: some one
                    nic-hdl: TEST-PN
                    mnt-by: TEST-MNT
                    changed: ripe@test.net
                    source: TEST
                """,
                "TEST-MNT": """\
                    mntner: TEST-MNT
                    admin-c: TEST-PN
                    mnt-by: TEST-MNT
                    referral-by: TEST-MNT
                    upd-to: dbtest@ripe.net
                    auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                    source: TEST
                """,
                "PWR-MNT": """\
                    mntner:  RIPE-NCC-HM-MNT
                    descr:   description
                    admin-c: TEST-PN
                    mnt-by:  RIPE-NCC-HM-MNT
                    referral-by: RIPE-NCC-HM-MNT
                    upd-to:  dbtest@ripe.net
                    auth:    MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                    changed: dbtest@ripe.net 20120707
                    source:  TEST
                """,
                "END-MNT": """\
                    mntner:  RIPE-NCC-END-MNT
                    descr:   description
                    admin-c: TEST-PN
                    mnt-by:  RIPE-NCC-END-MNT
                    referral-by: RIPE-NCC-END-MNT
                    upd-to:  dbtest@ripe.net
                    auth:    MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                    changed: dbtest@ripe.net 20120707
                    source:  TEST
                """,
                "ORG1": """\
                    organisation: ORG-TOL1-TEST
                    org-name:     Test Organisation Ltd
                    org-type:     RIR
                    descr:        test org
                    address:      street 5
                    e-mail:       org1@test.com
                    mnt-ref:      TEST-MNT
                    mnt-by:       RIPE-NCC-HM-MNT
                    changed:      dbtest@ripe.net 20120505
                    source:       TEST
                """,
                "ORG2": """\
                    organisation: ORG-TOL2-TEST
                    org-name:     Test Organisation Ltd
                    org-type:     OTHER
                    descr:        test org
                    address:      street 5
                    e-mail:       org1@test.com
                    mnt-ref:      TEST-MNT
                    mnt-by:       TEST-MNT
                    changed:      dbtest@ripe.net 20120505
                    source:       TEST
                """,
                "ORG3": """\
                    organisation: ORG-TOL3-TEST
                    org-name:     Test Organisation Ltd
                    org-type:     DIRECT_ASSIGNMENT
                    descr:        test org
                    address:      street 5
                    e-mail:       org1@test.com
                    mnt-ref:      TEST-MNT
                    mnt-by:       RIPE-NCC-HM-MNT
                    changed:      dbtest@ripe.net 20120505
                    source:       TEST
                """,
                "ORG4": """\
                    organisation: ORG-TOL4-TEST
                    org-name:     Test Organisation Ltd
                    org-type:     IANA
                    descr:        test org
                    address:      street 5
                    e-mail:       org1@test.com
                    mnt-ref:      TEST-MNT
                    mnt-by:       RIPE-NCC-HM-MNT
                    changed:      dbtest@ripe.net 20120505
                    source:       TEST
                """,
                "ORG5": """\
                    organisation: ORG-TOL5-TEST
                    org-name:     Test Organisation Ltd
                    org-type:     LIR
                    descr:        test org
                    address:      street 5
                    e-mail:       org1@test.com
                    mnt-ref:      TEST-MNT
                    mnt-by:       RIPE-NCC-HM-MNT
                    changed:      dbtest@ripe.net 20120505
                    source:       TEST
                """,
                "INET1": """\
                    inetnum: 193.0.0.0 - 193.0.0.255
                    netname: RIPE-NCC
                    descr: description
                    country: DK
                    admin-c: TEST-PN
                    tech-c: TEST-PN
                    status: SUB-ALLOCATED PA
                    mnt-by: TEST-MNT
                    changed: ripe@test.net 20120505
                    source: TEST
                """,
                "INETROOT": """\
                    inetnum: 0.0.0.0 - 255.255.255.255
                    netname: IANA-BLK
                    descr: The whole IPv4 address space
                    country: DK
                    admin-c: TEST-PN
                    tech-c: TEST-PN
                    status: ALLOCATED UNSPECIFIED
                    mnt-by: RIPE-NCC-HM-MNT
                    changed: ripe@test.net 20120505
                    source: TEST
                """
        ]
    }

    def "add irt"() {
      when:
        def response = syncUpdate(new SyncUpdate(data: """\
                irt: irt-IRT1
                address: Street 1
                e-mail: test@ripe.net
                admin-c: TEST-PN
                tech-c: TEST-PN
                auth: MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                mnt-by: TEST-MNT
                changed: test@ripe.net 20120505
                source: TEST
                password: update
                """.stripIndent()))

      then:
        response =~ /SUCCESS/
        response =~ /Create SUCCEEDED: \[irt\] irt-IRT1/
    }

    def "modify irt"() {
      when:
        def response = syncUpdate(new SyncUpdate(data: """\
                irt: irt-IRT1
                address: Street 1
                e-mail: test@ripe.net
                admin-c: TEST-PN
                tech-c: TEST-PN
                auth: MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                mnt-by: TEST-MNT
                changed: test@ripe.net 20120505
                source: TEST
                password: update
                """.stripIndent()))

      then:
        response =~ /SUCCESS/
        response =~ /Create SUCCEEDED: \[irt\] irt-IRT1/

      when:
        def updateResponse = syncUpdate(new SyncUpdate(data: """\
                irt: irt-IRT1
                address: Street 2
                e-mail: test2@ripe.net
                admin-c: TEST-PN
                tech-c: TEST-PN
                auth: MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                mnt-by: TEST-MNT
                changed: test@ripe.net 20120505
                source: TEST
                password: update
                """.stripIndent()))

      then:
        updateResponse =~ /SUCCESS/
        updateResponse =~ /Modify SUCCEEDED: \[irt\] irt-IRT1/
    }

    def "delete irt"() {
      when:
        def response = syncUpdate(new SyncUpdate(data: """\
                irt: irt-IRT1
                address: Street 1
                e-mail: test@ripe.net
                admin-c: TEST-PN
                tech-c: TEST-PN
                auth: MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                mnt-by: TEST-MNT
                changed: test@ripe.net 20120505
                source: TEST
                password: update
                """.stripIndent()))

      then:
        response =~ /SUCCESS/
        response =~ /Create SUCCEEDED: \[irt\] irt-IRT1/

      when:
        def deleteResponse = syncUpdate(new SyncUpdate(data: """\
                irt: irt-IRT1
                address: Street 1
                e-mail: test@ripe.net
                admin-c: TEST-PN
                tech-c: TEST-PN
                auth: MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                mnt-by: TEST-MNT
                changed: test@ripe.net 20120505
                source: TEST
                password: update
                delete: test
                """.stripIndent()))

      then:
        deleteResponse =~ /SUCCESS/
        deleteResponse =~ /Delete SUCCEEDED: \[irt\] irt-IRT1/
    }

    def "add irt with invalid signature and encryption"() {
      when:
        def response = syncUpdate(new SyncUpdate(data: """\
                irt: irt-IRT1
                address: Street 1
                e-mail: test@ripe.net
                signature: PGPKEY-A6D57ECE
                encryption: PGPKEY-A6D57ECE
                admin-c: TEST-PN
                tech-c: TEST-PN
                auth: MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                mnt-by: TEST-MNT
                changed: test@ripe.net 20120505
                source: TEST
                password: update
                """.stripIndent()))

      then:
        response =~ /FAILED/
        response.contains("" +
                "signature:      PGPKEY-A6D57ECE\n" +
                "***Error:   Unknown object referenced PGPKEY-A6D57ECE\n")

        response.contains("" +
                "encryption:     PGPKEY-A6D57ECE\n" +
                "***Error:   Unknown object referenced PGPKEY-A6D57ECE\n")
    }

    def "remove keycert referenced from irt"() {
      when:
        def keycert = """\
                    key-cert:     PGPKEY-28F6CD6C
                    method:       PGP
                    owner:        Ed Shryane <eshryane@ripe.net>
                    fingerpr:     1C40 500A 1DC4 A8D8 D3EA  ABF9 EE99 1EE2 28F6 CD6C
                    certif:       -----BEGIN PGP PUBLIC KEY BLOCK-----
                    certif:       Version: GnuPG v1.4.11 (Darwin)
                    certif:
                    certif:       mQENBE841dMBCAC80IDqJpJC7ch16NEaWvLDM8CslkhiqYk9fgXgUdMNuBsJJ/KV
                    certif:       4oUwzrX+3lNvMPSoW7yRfiokFQ48IhYVZuGlH7DzwsyfS3MniXmw6/vT6JaYPuIF
                    certif:       7TmMHIIxQbzJe+SUrauzJ2J0xQbnKhcfuLkmNO7jiOoKGJWIrO5wUZfd0/4nOoaz
                    certif:       RMokk0Paj6r52ZMni44vV4R0QnuUJRNIIesPDYDkOZGXX1lD9fprTc2DJe8tjAu0
                    certif:       VJz5PpCHwvS9ge22CRTUBSgmf2NBHJwDF+dnvijLuoDFyTuOrSkq0nAt0B9kTUxt
                    certif:       Bsb7mNxlARduo5419hBp08P07LJb4upuVsMPABEBAAG0HkVkIFNocnlhbmUgPGVz
                    certif:       aHJ5YW5lQHJpcGUubmV0PokBOAQTAQIAIgUCTzjV0wIbAwYLCQgHAwIGFQgCCQoL
                    certif:       BBYCAwECHgECF4AACgkQ7pke4ij2zWyUKAf+MmDQnBUUSjDeFvCnNN4JTraMXFUi
                    certif:       Ke2HzVnLvT/Z/XN5W6TIje7u1luTJk/siJJyKYa1ZWQoVOCXruTSge+vP6LxENOX
                    certif:       /sOJ1YxWHJUr3OVOfW2NoKBaUkBBCxi/CSaPti7YPHF0D6rn3GJtoJTnLL4KPnWV
                    certif:       gtja4FtpsgwhiPF/jVmx6/d5Zc/dndDLZZt2sMjh0KDVf7F03hsF/EAauBbxMLvK
                    certif:       yEHMdw7ab5CxeorgWEDaLrR1YwHWHy9cbYC00Mgp1zQR1ok2wN/XZVL7BZYPS/UC
                    certif:       H03bFi3AcN1Vm55QpbU0QJ4qPN8uwYc5VBFSSYRITUCwbB5qBO5kIIBLP7kBDQRP
                    certif:       ONXTAQgA16kMTcjxOtkU8v3sLAIpr2xWwG91BdB2fLV0aUgaZWfexKMnWDu8xpm1
                    certif:       qY+viF+/emdXBc/C7QbFUmhmXCslX5kfD10hkYFTIqc1Axk5Ya8FZtwHFpo0TVTl
                    certif:       sGodZ2gy8334rT9yMH+bZNSlZ+07Fxa7maC1ycxPPL/68+LSBy6wWlAFCwwr7XwN
                    certif:       LGnrBbELgvoi04yMu1EpqAvxZLH1TBgzrFcWzXJjj1JKIB1RGapoDc3m7dvHa3+e
                    certif:       27aQosQnNVNWrHiS67zqWoC963aNuHZBY174yfKPRaN6s5GppC2hMPYGnJV07yah
                    certif:       P0mwRcp4e3AaJIg2SP9CUQJKGPY+mQARAQABiQEfBBgBAgAJBQJPONXTAhsMAAoJ
                    certif:       EO6ZHuIo9s1souEH/ieP9J69j59zfVcN6FimT86JF9CVyB86PGv+naHEyzOrBjml
                    certif:       xBn2TPCNSE5KH8+gENyvYaQ6Wxv4Aki2HnJj5H43LfXPZZ6HNME4FPowoIkumc9q
                    certif:       mndn6WXsgjwT9lc2HQmUgolQObg3JMBRe0rYzVf5N9+eXkc5lR/PpTOHdesP17uM
                    certif:       QqtJs2hKdZKXgKNufSypfQBLXxkhez0KvoZ4PvrLItZTZUjrnRXdObNUgvz5/SVh
                    certif:       4Oqesj+Z36YNFrsYobghzIqOiP4hINsm9mQoshz8YLZe0z7InwcFYHp7HvQWEOyj
                    certif:       kSYadR4aN+CVhYHOsn5nxbiKSFNAWh40q7tDP7I=
                    certif:       =XRho
                    certif:       -----END PGP PUBLIC KEY BLOCK-----
                    notify:       noreply@ripe.net
                    mnt-by:       TEST-MNT
                    changed:      noreply@ripe.net 20120213
                    source:       TEST
                    """.stripIndent()

        def keycertCreate = syncUpdate(new SyncUpdate(data: keycert + "password: update"))
        println "--->" + query("PGPKEY-28F6CD6C")

        def create = syncUpdate(new SyncUpdate(data: """\
                irt: irt-IRT1
                address: Street 1
                e-mail: test@ripe.net
                signature: PGPKEY-28F6CD6C
                encryption: PGPKEY-28F6CD6C
                admin-c: TEST-PN
                tech-c: TEST-PN
                auth: MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                mnt-by: TEST-MNT
                changed: test@ripe.net 20120505
                source: TEST
                password: update
                """.stripIndent()))

      then:
        keycertCreate =~ /SUCCESS/
        create =~ /SUCCESS/

      when:
        def delete = syncUpdate(new SyncUpdate(data: """\
                key-cert:       PGPKEY-28F6CD6C
                method:         PGP
                owner:          Ed Shryane <eshryane@ripe.net>
                fingerpr:       1C40 500A 1DC4 A8D8 D3EA  ABF9 EE99 1EE2 28F6 CD6C
                certif:         -----BEGIN PGP PUBLIC KEY BLOCK-----
                certif:         Version: GnuPG v1.4.11 (Darwin)
                certif:
                certif:         mQENBE841dMBCAC80IDqJpJC7ch16NEaWvLDM8CslkhiqYk9fgXgUdMNuBsJJ/KV
                certif:         4oUwzrX+3lNvMPSoW7yRfiokFQ48IhYVZuGlH7DzwsyfS3MniXmw6/vT6JaYPuIF
                certif:         7TmMHIIxQbzJe+SUrauzJ2J0xQbnKhcfuLkmNO7jiOoKGJWIrO5wUZfd0/4nOoaz
                certif:         RMokk0Paj6r52ZMni44vV4R0QnuUJRNIIesPDYDkOZGXX1lD9fprTc2DJe8tjAu0
                certif:         VJz5PpCHwvS9ge22CRTUBSgmf2NBHJwDF+dnvijLuoDFyTuOrSkq0nAt0B9kTUxt
                certif:         Bsb7mNxlARduo5419hBp08P07LJb4upuVsMPABEBAAG0HkVkIFNocnlhbmUgPGVz
                certif:         aHJ5YW5lQHJpcGUubmV0PokBOAQTAQIAIgUCTzjV0wIbAwYLCQgHAwIGFQgCCQoL
                certif:         BBYCAwECHgECF4AACgkQ7pke4ij2zWyUKAf+MmDQnBUUSjDeFvCnNN4JTraMXFUi
                certif:         Ke2HzVnLvT/Z/XN5W6TIje7u1luTJk/siJJyKYa1ZWQoVOCXruTSge+vP6LxENOX
                certif:         /sOJ1YxWHJUr3OVOfW2NoKBaUkBBCxi/CSaPti7YPHF0D6rn3GJtoJTnLL4KPnWV
                certif:         gtja4FtpsgwhiPF/jVmx6/d5Zc/dndDLZZt2sMjh0KDVf7F03hsF/EAauBbxMLvK
                certif:         yEHMdw7ab5CxeorgWEDaLrR1YwHWHy9cbYC00Mgp1zQR1ok2wN/XZVL7BZYPS/UC
                certif:         H03bFi3AcN1Vm55QpbU0QJ4qPN8uwYc5VBFSSYRITUCwbB5qBO5kIIBLP7kBDQRP
                certif:         ONXTAQgA16kMTcjxOtkU8v3sLAIpr2xWwG91BdB2fLV0aUgaZWfexKMnWDu8xpm1
                certif:         qY+viF+/emdXBc/C7QbFUmhmXCslX5kfD10hkYFTIqc1Axk5Ya8FZtwHFpo0TVTl
                certif:         sGodZ2gy8334rT9yMH+bZNSlZ+07Fxa7maC1ycxPPL/68+LSBy6wWlAFCwwr7XwN
                certif:         LGnrBbELgvoi04yMu1EpqAvxZLH1TBgzrFcWzXJjj1JKIB1RGapoDc3m7dvHa3+e
                certif:         27aQosQnNVNWrHiS67zqWoC963aNuHZBY174yfKPRaN6s5GppC2hMPYGnJV07yah
                certif:         P0mwRcp4e3AaJIg2SP9CUQJKGPY+mQARAQABiQEfBBgBAgAJBQJPONXTAhsMAAoJ
                certif:         EO6ZHuIo9s1souEH/ieP9J69j59zfVcN6FimT86JF9CVyB86PGv+naHEyzOrBjml
                certif:         xBn2TPCNSE5KH8+gENyvYaQ6Wxv4Aki2HnJj5H43LfXPZZ6HNME4FPowoIkumc9q
                certif:         mndn6WXsgjwT9lc2HQmUgolQObg3JMBRe0rYzVf5N9+eXkc5lR/PpTOHdesP17uM
                certif:         QqtJs2hKdZKXgKNufSypfQBLXxkhez0KvoZ4PvrLItZTZUjrnRXdObNUgvz5/SVh
                certif:         4Oqesj+Z36YNFrsYobghzIqOiP4hINsm9mQoshz8YLZe0z7InwcFYHp7HvQWEOyj
                certif:         kSYadR4aN+CVhYHOsn5nxbiKSFNAWh40q7tDP7I=
                certif:         =XRho
                certif:         -----END PGP PUBLIC KEY BLOCK-----
                notify:         noreply@ripe.net
                mnt-by:         TEST-MNT
                changed:        noreply@ripe.net 20120213
                source:         TEST
                password:       update
                delete:         test
                """.stripIndent()))

      then:
        delete =~ /SUCCESS/
    }
}
