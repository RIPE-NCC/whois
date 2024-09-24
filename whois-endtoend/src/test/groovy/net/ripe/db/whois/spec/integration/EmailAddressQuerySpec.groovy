package net.ripe.db.whois.spec.integration

@org.junit.jupiter.api.Tag("IntegrationTest")
class EmailAddressQuerySpec extends BaseWhoisSourceSpec {
    @Override
    Map<String, String> getFixtures() {
        [
                "TEST-PN": """\
                person:         Test Person
                address:        St James Street
                address:        Burnley
                address:        UK
                phone:          +44 282 420469
                nic-hdl:        TP1-TEST
                mnt-by:         TST-MNT
                source:         TEST
                """,
                "TST-MNT": """\
                mntner:         TST-MNT
                descr:          MNTNER for test
                admin-c:        TP1-TEST
                upd-to:         dbtest@ripe.net
                auth:           MD5-PW \$1\$d9fKeTr2\$Si7YudNf4rUGmR71n/cqk/  #test
                source:         TEST
                """
        ]
    }

    // abuse-mailbox

    def "abuse-mailbox"() {
      given:
        databaseHelper.addObject("""\
                role:           Test Role
                address:        Amsterdam
                e-mail:         dbtest@ripe.net
                abuse-mailbox:  Abuse Mailbox <abuse-mailbox@ripe.net>
                nic-hdl:        TR1-TEST
                mnt-by:         TST-MNT
                source:         TEST
                """.stripIndent(true))
      when:
        def response = query "abuse-mailbox@ripe.net"
      then:
        response.contains("%ERROR:101: no entries found")
    }

    def "abuse-mailbox inverse query"() {
      given:
        databaseHelper.addObject("""\
                role:           Test Role
                address:        Amsterdam
                e-mail:         dbtest@ripe.net
                abuse-mailbox:  Abuse Mailbox <abuse-mailbox@ripe.net>
                nic-hdl:        TR1-TEST
                mnt-by:         TST-MNT
                source:         TEST
                """.stripIndent(true))
      when:
        def response = query "-i abuse-mailbox abuse-mailbox@ripe.net"
      then:
        def expectedResult = """\
            role:           Test Role
            address:        Amsterdam
            abuse-mailbox:  Abuse Mailbox <abuse-mailbox@ripe.net>
            nic-hdl:        TR1-TEST
            mnt-by:         TST-MNT
            source:         TEST # Filtered
            """.stripIndent(true)

        response.contains(expectedResult)
    }

    //e-mail

    def "e-mail"() {
      given:
        databaseHelper.addObject("""\
                role:           Test Role
                address:        Amsterdam
                e-mail:         Test User <dbtest@ripe.net>
                nic-hdl:        TR1-TEST
                mnt-by:         TST-MNT
                source:         TEST
                """.stripIndent(true))
      when:
        def response = query "dbtest@ripe.net"
      then:
        def expectedResult = """\
            role:           Test Role
            address:        Amsterdam
            nic-hdl:        TR1-TEST
            mnt-by:         TST-MNT
            source:         TEST # Filtered
            """.stripIndent(true)

        response.contains(expectedResult)
    }

    def "e-mail inverse query"() {
      given:
        databaseHelper.addObject("""\
                role:           Test Role
                address:        Amsterdam
                e-mail:         Test User <dbtest@ripe.net>
                nic-hdl:        TR1-TEST
                mnt-by:         TST-MNT
                source:         TEST
                """.stripIndent(true))
      when:
        def response = query "-i e-mail dbtest@ripe.net"
      then:
        def expectedResult = """\
            role:           Test Role
            address:        Amsterdam
            nic-hdl:        TR1-TEST
            mnt-by:         TST-MNT
            source:         TEST # Filtered
            """.stripIndent(true)

        response.contains(expectedResult)
    }

    //irt-nfy

    def "irt-nfy"() {
      given:
        databaseHelper.addObject("""\
                irt:       irt-IRT1
                address:   Amsterdam
                e-mail:    dbtest@ripe.net
                admin-c:   TP1-TEST
                tech-c:    TP1-TEST
                auth:      MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                irt-nfy:   IRT Notification <irt@ripe.net>
                mnt-by:    TST-MNT
                source:    TEST
                """.stripIndent(true))
      when:
        def response = query "irt@ripe.net"
      then:
        response.contains("%ERROR:101: no entries found")
    }

    def "irt-nfy inverse query"() {
      given:
        databaseHelper.addObject("""\
                irt:       irt-IRT1
                address:   Amsterdam
                e-mail:    dbtest@ripe.net
                admin-c:   TP1-TEST
                tech-c:    TP1-TEST
                auth:      MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                irt-nfy:   IRT Notification <irt@ripe.net>
                mnt-by:    TST-MNT
                source:    TEST
                """.stripIndent(true))
      when:
        def response = query "-i irt-nfy irt@ripe.net"
      then:
        def expectedResult = """\
            irt:            irt-IRT1
            address:        Amsterdam
            admin-c:        TP1-TEST
            tech-c:         TP1-TEST
            auth:           MD5-PW # Filtered
            irt-nfy:        IRT Notification <irt@ripe.net>
            mnt-by:         TST-MNT
            source:         TEST # Filtered
            """.stripIndent(true)

        response.contains(expectedResult)
    }

    //mnt-nfy

    def "mnt-nfy"() {
      given:
        databaseHelper.updateObject("""\
                mntner:         TST-MNT
                descr:          MNTNER for test
                admin-c:        TP1-TEST
                upd-to:         dbtest@ripe.net
                mnt-nfy:        Notify Maintainer <mntnfy@ripe.net>
                auth:           MD5-PW \$1\$d9fKeTr2\$Si7YudNf4rUGmR71n/cqk/  #test
                source:         TEST
                """.stripIndent(true))
      when:
        def response = query "mntnfy@ripe.net"
      then:
        response.contains("%ERROR:101: no entries found")
    }

    def "mnt-nfy inverse query"() {
      given:
        databaseHelper.updateObject("""\
                mntner:         TST-MNT
                descr:          MNTNER for test
                admin-c:        TP1-TEST
                upd-to:         dbtest@ripe.net
                mnt-nfy:        Notify Maintainer <mntnfy@ripe.net>
                auth:           MD5-PW \$1\$d9fKeTr2\$Si7YudNf4rUGmR71n/cqk/  #test
                source:         TEST
                """.stripIndent(true))
      when:
        def response = query "-i mnt-nfy mntnfy@ripe.net"
      then:
        def expectedResult = """\
            mntner:         TST-MNT
            descr:          MNTNER for test
            admin-c:        TP1-TEST
            auth:           MD5-PW # Filtered
            source:         TEST # Filtered
            """.stripIndent(true)

        response.contains(expectedResult)
    }

    //notify

    def "notify"() {
      given:
        databaseHelper.addObject("""\
                role:           Test Role
                address:        Amsterdam
                e-mail:         Test User <dbtest@ripe.net>
                notify:         Notify User <notify@ripe.net>
                nic-hdl:        TR1-TEST
                mnt-by:         TST-MNT
                source:         TEST
                """.stripIndent(true))
      when:
        def response = query "notify@ripe.net"
      then:
        response.contains("%ERROR:101: no entries found")
    }

    def "notify inverse query"() {
      given:
        databaseHelper.addObject("""\
                role:           Test Role
                address:        Amsterdam
                e-mail:         Test User <dbtest@ripe.net>
                notify:         Notify User <notify@ripe.net>
                nic-hdl:        TR1-TEST
                mnt-by:         TST-MNT
                source:         TEST
                """.stripIndent(true))
      when:
        def response = query "-i notify notify@ripe.net"
      then:
        def expectedResult = """\
            role:           Test Role
            address:        Amsterdam
            nic-hdl:        TR1-TEST
            mnt-by:         TST-MNT
            source:         TEST # Filtered
            """.stripIndent(true)

        response.contains(expectedResult)
    }

    //ref-nfy

    def "ref-nfy"() {
      given:
        databaseHelper.addObject("""\
                organisation:   ORG-TO1-TEST
                org-name:       Test Organisation
                org-type:       OTHER
                address:        Amsterdam
                e-mail:         dbtest@ripe.net
                ref-nfy:        References Notify <refnfy@ripe.net>
                mnt-ref:        TST-MNT
                mnt-by:         TST-MNT
                source:         TEST
                """.stripIndent(true))
      when:
        def response = query "refnfy@ripe.net"
      then:
        response.contains("%ERROR:101: no entries found")
    }

    def "ref-nfy inverse query"() {
      given:
        databaseHelper.addObject("""\
                organisation:   ORG-TO1-TEST
                org-name:       Test Organisation
                org-type:       OTHER
                address:        Amsterdam
                e-mail:         dbtest@ripe.net
                ref-nfy:        References Notify <refnfy@ripe.net>
                mnt-ref:        TST-MNT
                mnt-by:         TST-MNT
                source:         TEST
                """.stripIndent(true))
      when:
        def response = query "-i ref-nfy refnfy@ripe.net"
      then:
        def expectedResult = """\
            organisation:   ORG-TO1-TEST
            org-name:       Test Organisation
            org-type:       OTHER
            address:        Amsterdam
            mnt-ref:        TST-MNT
            mnt-by:         TST-MNT
            source:         TEST # Filtered
            """.stripIndent(true)

        response.contains(expectedResult)
    }

    //upd-to

    def "upd-to"() {
      given:
        databaseHelper.updateObject("""\
                mntner:         TST-MNT
                descr:          MNTNER for test
                admin-c:        TP1-TEST
                upd-to:        Update To Maintainer <updto@ripe.net>
                auth:           MD5-PW \$1\$d9fKeTr2\$Si7YudNf4rUGmR71n/cqk/  #test
                source:         TEST
                """.stripIndent(true))
      when:
        def response = query "updto@ripe.net"
      then:
        response.contains("%ERROR:101: no entries found")
    }

    def "upd-to inverse query"() {
      given:
        databaseHelper.updateObject("""\
                mntner:         TST-MNT
                descr:          MNTNER for test
                admin-c:        TP1-TEST
                upd-to:        Update To Maintainer <updto@ripe.net>
                auth:           MD5-PW \$1\$d9fKeTr2\$Si7YudNf4rUGmR71n/cqk/  #test
                source:         TEST
                """.stripIndent(true))
      when:
        def response = query "-i upd-to updto@ripe.net"
      then:
        def expectedResult = """\
            mntner:         TST-MNT
            descr:          MNTNER for test
            admin-c:        TP1-TEST
            auth:           MD5-PW # Filtered
            source:         TEST # Filtered
            """.stripIndent(true)

        response.contains(expectedResult)
    }


}
