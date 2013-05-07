package spec.integration

import net.ripe.db.whois.common.IntegrationTest
import net.ripe.db.whois.common.domain.CIString
import net.ripe.db.whois.common.domain.Tag
import net.ripe.db.whois.common.rpsl.ObjectType
import org.joda.time.LocalDateTime
import spec.domain.SyncUpdate

@org.junit.experimental.categories.Category(IntegrationTest.class)
class UnrefCleanupIntegrationSpec extends BaseSpec {

    @Override
    Map<String, String> getFixtures() {
        return [
                "UPD-MNT": """\
            mntner: UPD-MNT
            descr: description
            admin-c: TEST-RIPE
            mnt-by: UPD-MNT
            referral-by: ADMIN-MNT
            upd-to: dbtest@ripe.net
            auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            changed: dbtest@ripe.net 20120707
            source: TEST
            """,
                "ADMIN-MNT": """\
            mntner: ADMIN-MNT
            descr: description
            admin-c: TEST-RIPE
            mnt-by: ADMIN-MNT
            referral-by: ADMIN-MNT
            upd-to: dbtest@ripe.net
            auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            changed: dbtest@ripe.net 20120707
            source: TEST
            """,
                "TEST-RIPE": """\
            person:  Admin Person
            address: Admin Road
            address: Town
            address: UK
            phone:   +44 282 411141
            nic-hdl: TEST-RIPE
            mnt-by:  UPD-MNT
            mnt-by:  ADMIN-MNT
            changed: dbtest@ripe.net 20120101
            source:  TEST
            """
        ]
    }

    def "cleanup none"() {
      when:
        setTime(new LocalDateTime().plusDays(100))
        unrefCleanup()

      then:
        objectExists(ObjectType.MNTNER, "UPD-MNT")
        objectExists(ObjectType.MNTNER, "ADMIN-MNT")
        objectExists(ObjectType.PERSON, "TEST-RIPE")
    }

    def "unref cleanup person never referenced, created today"() {
      when:
        def response = syncUpdate(new SyncUpdate(data: """\
            person:         Unref Person
            address:        Admin Road
            address:        Town
            address:        UK
            phone:          +44 282 411141
            nic-hdl:        UNRF-RIPE
            mnt-by:         ADMIN-MNT
            changed:        dbtest@ripe.net 20120101
            source:         TEST
            password:       update
            """.stripIndent()))

      then:
        response.contains("SUCCESS")
        objectExists(ObjectType.PERSON, "UNRF-RIPE")

      when:
        unrefCleanup()

      then:
        objectExists(ObjectType.PERSON, "UNRF-RIPE")

      when:
        def tags = tagsDao.getTagsOfType(CIString.ciString("unref"))

      then:
        tags.size() == 1
        tags.contains(new Tag(CIString.ciString("unref"), rpslObjectDao.findByKey(ObjectType.PERSON, "UNRF-RIPE").objectId, "90"))
    }

    def "unref cleanup person never referenced, multiple days"() {
      when:
        def response = syncUpdate(new SyncUpdate(data: """\
            person:         Unref Person
            address:        Admin Road
            address:        Town
            address:        UK
            phone:          +44 282 411141
            nic-hdl:        UNRF-RIPE
            mnt-by:         ADMIN-MNT
            changed:        dbtest@ripe.net 20120101
            source:         TEST
            password:       update
            """.stripIndent()))

      then:
        response.contains("SUCCESS")
        objectExists(ObjectType.PERSON, "UNRF-RIPE")

      when:
        setTime(new LocalDateTime().plusDays(10))
        unrefCleanup()

      then:
        objectExists(ObjectType.PERSON, "UNRF-RIPE")
        def tags = tagsDao.getTagsOfType(CIString.ciString("unref"))
        tags.size() == 1
        tags.contains(new Tag(CIString.ciString("unref"), rpslObjectDao.findByKey(ObjectType.PERSON, "UNRF-RIPE").objectId, "80"))

      when:
        setTime(new LocalDateTime().plusDays(20))
        unrefCleanup()

      then:
        objectExists(ObjectType.PERSON, "UNRF-RIPE")
        def tags2 = tagsDao.getTagsOfType(CIString.ciString("unref"))
        tags2.size() == 1
        tags2.contains(new Tag(CIString.ciString("unref"), rpslObjectDao.findByKey(ObjectType.PERSON, "UNRF-RIPE").objectId, "70"))

      when:
        setTime(new LocalDateTime().plusDays(100))
        unrefCleanup()

      then:
        !objectExists(ObjectType.PERSON, "UNRF-RIPE")
        tagsDao.getTagsOfType(CIString.ciString("unref")).isEmpty()
    }

    def "unref cleanup person never referenced, created > 90 days ago"() {
      when:
        def response = syncUpdate(new SyncUpdate(data: """\
            person:         Unref Person
            address:        Admin Road
            address:        Town
            address:        UK
            phone:          +44 282 411141
            nic-hdl:        UNRF-RIPE
            mnt-by:         ADMIN-MNT
            changed:        dbtest@ripe.net 20120101
            source:         TEST
            password:       update
            """.stripIndent()))

      then:
        response.contains("SUCCESS")
        objectExists(ObjectType.PERSON, "UNRF-RIPE")

      when:
        setTime(new LocalDateTime().plusDays(100))
        unrefCleanup()

      then:
        !objectExists(ObjectType.PERSON, "UNRF-RIPE")

      when:
        def tags = tagsDao.getTagsOfType(CIString.ciString("unref"))

      then:
        tags.isEmpty()
    }

    def "cleanup currently unreferenced maintainer with self reference"() {
      when:
        def response = syncUpdate(new SyncUpdate(data: """\
            mntner: UNREF-MNT
            descr: description
            admin-c: TEST-RIPE
            mnt-by: UNREF-MNT
            referral-by: ADMIN-MNT
            upd-to: dbtest@ripe.net
            auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            changed: dbtest@ripe.net 20120707
            source: TEST
            password: update
            """.stripIndent()))

      then:
        response.contains("SUCCESS")
        objectExists(ObjectType.MNTNER, "UNREF-MNT")

      when:
        setTime(new LocalDateTime().plusDays(100))
        unrefCleanup()

      then:
        !objectExists(ObjectType.MNTNER, "UNREF-MNT")
        tagsDao.getTagsOfType(CIString.ciString("unref")).isEmpty()
    }

    def "cleanup person referenced by unreferenced role"() {
      when:
        def response = syncUpdate(new SyncUpdate(data: """\
            password:       update

            role:           Test Admin
            address:        Address
            phone:          +44 282 411141
            fax-no:         +44 282 411140
            e-mail:         admin@test.com
            admin-c:        RLRF-RIPE
            tech-c:         RLRF-RIPE
            nic-hdl:        RL-TEST
            mnt-by:         ADMIN-MNT
            notify:         admin@test.com
            changed:        admin@test.com 20120505
            abuse-mailbox:  admin@test.com
            source:         TEST

            person:         Unref Person
            address:        Admin Road
            phone:          +44 282 411141
            nic-hdl:        RLRF-RIPE
            mnt-by:         ADMIN-MNT
            changed:        dbtest@ripe.net 20120101
            source:         TEST

            """.stripIndent()))

      then:
        response.contains("SUCCESS")
        objectExists(ObjectType.ROLE, "RL-TEST")
        objectExists(ObjectType.PERSON, "RLRF-RIPE")

      when:
        unrefCleanup()

      then:
        def tags = tagsDao.getTagsOfType(CIString.ciString("unref"))
        tags.size() == 1
        tags.contains(new Tag(CIString.ciString("unref"), rpslObjectDao.findByKey(ObjectType.ROLE, "RL-TEST").objectId, "90"))

      when:
        setTime(new LocalDateTime().plusDays(100))
        unrefCleanup()

      then:
        !objectExists(ObjectType.ROLE, "RL-TEST")
        objectExists(ObjectType.PERSON, "RLRF-RIPE")
        tagsDao.getTagsOfType(CIString.ciString("unref")).isEmpty()

      when:
        setTime(new LocalDateTime().plusDays(101))
        unrefCleanup()

      then:
        objectExists(ObjectType.PERSON, "RLRF-RIPE")
        def tags2 = tagsDao.getTagsOfType(CIString.ciString("unref"))
        tags2.size() == 1
        tags2.get(0).value == "89"
    }

    def "cleanup person referenced by as-set"() {
      when:
        def response = syncUpdate(new SyncUpdate(data: """\
            password:       update

            person:         Unref Person
            address:        Admin Road
            phone:          +44 282 411141
            nic-hdl:        REFR-RIPE
            mnt-by:         ADMIN-MNT
            changed:        dbtest@ripe.net 20120101
            source:         TEST

            as-set:         AS-TEST
            descr:          test as-set
            members:        AS1
            tech-c:         REFR-RIPE
            admin-c:        REFR-RIPE
            mnt-by:         ADMIN-MNT
            changed:        dbtest@ripe.net 20120101
            source:         TEST

            """.stripIndent()))

      then:
        response.contains("SUCCESS")
        objectExists(ObjectType.AS_SET, "AS-TEST")
        objectExists(ObjectType.PERSON, "REFR-RIPE")

      when:
        setTime(new LocalDateTime().plusDays(100))
        unrefCleanup()

      then:
        objectExists(ObjectType.AS_SET, "AS-TEST")
        objectExists(ObjectType.PERSON, "REFR-RIPE")
        tagsDao.getTagsOfType(CIString.ciString("unref")).isEmpty()

      when:
        setTime(new LocalDateTime())
        def deleteResponse = syncUpdate(new SyncUpdate(data: """\
            as-set:         AS-TEST
            descr:          test as-set
            members:        AS1
            tech-c:         REFR-RIPE
            admin-c:        REFR-RIPE
            mnt-by:         ADMIN-MNT
            changed:        dbtest@ripe.net 20120101
            source:         TEST
            delete:         reason
            password:       update

            """.stripIndent()))

      then:
        deleteResponse.contains("SUCCESS")
        !objectExists(ObjectType.AS_SET, "AS-TEST")
        objectExists(ObjectType.PERSON, "REFR-RIPE")

      when:
        setTime(new LocalDateTime().plusDays(50))
        unrefCleanup()

      then:
        objectExists(ObjectType.PERSON, "REFR-RIPE")
        def tags = tagsDao.getTagsOfType(CIString.ciString("unref"))
        tags.size() == 1
        tags[0].value == "40"

      when:
        setTime(new LocalDateTime().plusDays(100))
        unrefCleanup()

      then:
        !objectExists(ObjectType.PERSON, "REFR-RIPE")
        tagsDao.getTagsOfType(CIString.ciString("unref")).isEmpty()
    }

    def "do not cleanup person referenced by as-set deleted 10 days ago"() {
      given:
        def now = new LocalDateTime()

      when:
        syncUpdate(new SyncUpdate(data: """\
            person:         Unref Person
            address:        Admin Road
            phone:          +44 282 411141
            nic-hdl:        REFR-RIPE
            mnt-by:         ADMIN-MNT
            changed:        dbtest@ripe.net 20120101
            source:         TEST
            password:       update
            """.stripIndent()))

        setTime(now.plusDays(80))

        def asTestResponse = syncUpdate(new SyncUpdate(data: """\
            as-set:         AS-TEST
            descr:          test as-set
            members:        AS1
            tech-c:         REFR-RIPE
            admin-c:        REFR-RIPE
            mnt-by:         ADMIN-MNT
            changed:        dbtest@ripe.net 20120101
            source:         TEST
            password:       update
            """.stripIndent()))

        setTime(now.plusDays(82))

        def asTest2Response = syncUpdate(new SyncUpdate(data: """\
            as-set:         AS-TEST2
            descr:          test as-set
            members:        AS1
            tech-c:         REFR-RIPE
            admin-c:        REFR-RIPE
            mnt-by:         ADMIN-MNT
            changed:        dbtest@ripe.net 20120101
            source:         TEST
            password:       update
            """.stripIndent()))

      then:
        asTestResponse.contains("SUCCESS")
        asTest2Response.contains("SUCCESS")
        objectExists(ObjectType.AS_SET, "AS-TEST")
        objectExists(ObjectType.AS_SET, "AS-TEST2")
        objectExists(ObjectType.PERSON, "REFR-RIPE")

      when:
        def deleteResponse1 = syncUpdate(new SyncUpdate(data: """\
            as-set:         AS-TEST
            descr:          test as-set
            members:        AS1
            tech-c:         REFR-RIPE
            admin-c:        REFR-RIPE
            mnt-by:         ADMIN-MNT
            changed:        dbtest@ripe.net 20120101
            source:         TEST
            delete:         reason
            password:       update
            """.stripIndent()))

        setTime(now.plusDays(82))

        def deleteResponse2 = syncUpdate(new SyncUpdate(data: """\
            as-set:         AS-TEST2
            descr:          test as-set
            members:        AS1
            tech-c:         REFR-RIPE
            admin-c:        REFR-RIPE
            mnt-by:         ADMIN-MNT
            changed:        dbtest@ripe.net 20120101
            source:         TEST
            delete:         reason
            password:       update
            """.stripIndent()))

      then:
        deleteResponse1.contains("SUCCESS")
        deleteResponse2.contains("SUCCESS")
        !objectExists(ObjectType.AS_SET, "AS-TEST")
        !objectExists(ObjectType.AS_SET, "AS-TEST2")
        objectExists(ObjectType.PERSON, "REFR-RIPE")

      when:
        setTime(now.plusDays(100))
        unrefCleanup()

      then:
        objectExists(ObjectType.PERSON, "REFR-RIPE")

        def daysUntilDelete = 90 - (100 - 82)
        def object = rpslObjectDao.findByKey(ObjectType.PERSON, "REFR-RIPE")
        getTags(object.getObjectId()).get(0).getValue() == daysUntilDelete.toString()

    }

    def "cleanup unreferenced keycert"() {
      when:
        def createKeyCertResponse = syncUpdate(new SyncUpdate(data: """\
            key-cert:     PGPKEY-28F6CD6C
            method:       PGP
            owner:        DFN-CERT (2003), ENCRYPTION Key
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
            mnt-by:       UPD-MNT
            notify:       noreply@ripe.net
            changed:      noreply@ripe.net 20120213
            source:       TEST
            password:     update
            """.stripIndent()))

      then:
        createKeyCertResponse.contains("SUCCESS")
        objectExists(ObjectType.KEY_CERT, "PGPKEY-28F6CD6C")

      when:
        setTime(new LocalDateTime().plusDays(100))
        unrefCleanup()

      then:
        objectExists(ObjectType.MNTNER, "UPD-MNT")
        objectExists(ObjectType.MNTNER, "ADMIN-MNT")
        objectExists(ObjectType.PERSON, "TEST-RIPE")
        !objectExists(ObjectType.KEY_CERT, "PGPKEY-28F6CD6C")
    }

    def "cleanup keycert referenced by maintainer"() {
      when:
        def createKeyCertResponse = syncUpdate(new SyncUpdate(data: """\
            key-cert:     PGPKEY-28F6CD6C
            method:       PGP
            owner:        DFN-CERT (2003), ENCRYPTION Key
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
            mnt-by:       UPD-MNT
            notify:       noreply@ripe.net
            changed:      noreply@ripe.net 20120213
            source:       TEST


            mntner: UPD-MNT
            descr: description
            admin-c: TEST-RIPE
            mnt-by: UPD-MNT
            referral-by: ADMIN-MNT
            upd-to: dbtest@ripe.net
            auth:   PGPKEY-28F6CD6C
            changed: dbtest@ripe.net 20120707
            source: TEST


            password:     update
            """.stripIndent()))

      then:
        createKeyCertResponse.contains("SUCCESS")
        objectExists(ObjectType.KEY_CERT, "PGPKEY-28F6CD6C")

      when:
        unrefCleanup()

      then:
        objectExists(ObjectType.MNTNER, "UPD-MNT")
        objectExists(ObjectType.MNTNER, "ADMIN-MNT")
        objectExists(ObjectType.PERSON, "TEST-RIPE")
        objectExists(ObjectType.KEY_CERT, "PGPKEY-28F6CD6C")
    }

    def "tagging uses latest change for number of days unreferenced"() {
      given:
        def now = new LocalDateTime()

      when:
        def initUpdate = syncUpdate(new SyncUpdate(data: """\
            mntner:         TEST-MNT
            descr:          description
            mnt-by:         TEST-MNT
            admin-c:        TEST-RIPE
            upd-to:         test@ripe.net
            auth:           MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            referral-by:    TEST-MNT
            changed:        test@ripe.net 20120404
            source:         TEST
            password:       update
            """.stripIndent()))

        setTime(now.plusDays(30))

        def update1 = syncUpdate(new SyncUpdate(data: """\
            mntner:         TEST-MNT
            descr:          descriptions
            mnt-by:         TEST-MNT
            admin-c:        TEST-RIPE
            upd-to:         test@ripe.net
            auth:           MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            referral-by:    TEST-MNT
            changed:        test@ripe.net 20120404
            source:         TEST
            password:       update
            """.stripIndent()))

        setTime(now.plusDays(40))

        def update2 = syncUpdate(new SyncUpdate(data: """\
            mntner:         TEST-MNT
            descr:          description
            mnt-by:         TEST-MNT
            admin-c:        TEST-RIPE
            upd-to:         test@ripe.net
            auth:           MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            referral-by:    TEST-MNT
            changed:        test@ripe.net 20120404
            source:         TEST
            password:       update
            """.stripIndent()))

      then:
        initUpdate.contains("SUCCESS")
        update1.contains("SUCCESS")
        update2.contains("SUCCESS")
        objectExists(ObjectType.MNTNER, "TEST-MNT")

      when:
        setTime(now.plusDays(60))
        unrefCleanup()

      then:
        objectExists(ObjectType.MNTNER, "TEST-MNT")

        def daysUntilDelete = 90 - (60 - 40)
        def object = rpslObjectDao.findByKey(ObjectType.MNTNER, "TEST-MNT")
        getTags(object.getObjectId()).get(0).getValue() == daysUntilDelete.toString()
    }

    def "tagging unmodified unreferenced objects"() {
      given:
        def now = new LocalDateTime()

      when:
        def initUpdate = syncUpdate(new SyncUpdate(data: """\
            mntner:         TEST-MNT
            descr:          description
            mnt-by:         TEST-MNT
            admin-c:        TEST-RIPE
            upd-to:         test@ripe.net
            auth:           MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            referral-by:    TEST-MNT
            changed:        test@ripe.net 20120404
            source:         TEST
            password:       update
            """.stripIndent()))

      then:
        initUpdate.contains("SUCCESS")
        objectExists(ObjectType.MNTNER, "TEST-MNT")

      when:
        setTime(now.plusDays(87))
        unrefCleanup()

      then:
        objectExists(ObjectType.MNTNER, "TEST-MNT")

        def daysUntilDelete = 90 - 87
        def object = rpslObjectDao.findByKey(ObjectType.MNTNER, "TEST-MNT")
        getTags(object.getObjectId()).get(0).getValue() == daysUntilDelete.toString()
    }
}
