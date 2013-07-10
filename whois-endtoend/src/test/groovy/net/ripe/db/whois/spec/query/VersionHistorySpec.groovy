package net.ripe.db.whois.spec.query

import net.ripe.db.whois.spec.BaseSpec
import net.ripe.db.whois.spec.BasicFixtures
import spec.domain.AckResponse

class VersionHistorySpec extends BaseSpec {

    @Override
    Map<String, String> getBasicFixtures() {
        return BasicFixtures.permanentFixtures
    }

    @Override
    Map<String, String> getFixtures() {
        [
            "ORG-FO1": """\
                organisation:    ORG-FO1-TEST
                org-type:        other
                org-name:        First Org
                org:             ORG-FO1-TEST
                address:         RIPE NCC
                                 Singel 258
                                 1016 AB Amsterdam
                                 Netherlands
                e-mail:          dbtest@ripe.net
                mnt-ref:         owner3-mnt
                mnt-by:          owner2-mnt
                changed:         denis@ripe.net 20121016
                source:          TEST
                """,
            "OLDMNTNER": """\
                mntner:         OLD-MNT
                descr:          description
                admin-c:        TP1-TEST
                mnt-by:         OLD-MNT
                referral-by:    OLD-MNT
                upd-to:         updto_cre@ripe.net
                auth:           MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                auth:           CRYPT-PW QQtm3kRqZXWAu
                changed:        dbtest@ripe.net 20120707
                source:         TEST
                """
        ]
    }

    @Override
    Map<String, String> getTransients() {
        [
            "RIR-ALLOC-20": """\
                inet6num:     2001::/20
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                status:       ALLOCATED-BY-RIR
                changed:      dbtest@ripe.net 20130101
                source:       TEST
                """,
            "ALLOC-PA": """\
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
            "ASS-END": """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
            "PN-FF": """\
                person:  fred fred
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: ff1-TEST
                mnt-by:  OWNER-MNT
                changed: dbtest@ripe.net 20120101
                source:  TEST
                """,
            "RL-FR": """\
                role:    First Role
                address: St James Street
                address: Burnley
                address: UK
                e-mail:  dbtest@ripe.net
                nic-hdl: FR1-TEST
                mnt-by:  OWNER-MNT
                changed: dbtest@ripe.net 20121016
                source:  TEST
                """,
            "SELF-MNT": """\
                mntner:  SELF-MNT
                descr:   description
                admin-c: TP1-TEST
                mnt-by:  SELF-MNT
                referral-by: SELF-MNT
                upd-to:  updto_cre@ripe.net
                auth:    MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                changed: dbtest@ripe.net 20120707
                source:  TEST
                """,
            "PAUL": """\
                mntner:  PAUL
                descr:   description
                admin-c: TP1-TEST
                mnt-by:  PAUL
                referral-by: PAUL
                upd-to:  updto_cre@ripe.net
                auth:    MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                changed: dbtest@ripe.net 20120707
                source:  TEST
                """,
            "AS1000": """\
                aut-num:     AS1000
                as-name:     TEST-AS
                descr:       Testing Authorisation code
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                mnt-by:      LIR-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """,
            "AS2000": """\
                aut-num:     AS2000
                as-name:     TEST-AS
                descr:       Testing Authorisation code
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                mnt-by:      LIR-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """,
        ]
    }

    def "query --list-versions"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "override: override1")

      expect:
        // "RIR-ALLOC-20"
        queryObject("-rBG -T inet6num 2001::/20", "inet6num", "2001::/20")

      and:
        queryLineMatches("--list-versions 2001::/20", "^% Version history for INET6NUM object \"2001::/20\"")
        queryLineMatches("--list-versions 2001::/20", "^1\\s*[0-9-]+\\s*[0-9:]+\\s*ADD/UPD")
        ! queryLineMatches("--list-versions 2001::/20", "^2\\s*[0-9-]+\\s*[0-9:]+\\s*ADD/UPD")
    }

    def "query --list-versions, 2 versions"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "override: override1")

      expect:
        // "RIR-ALLOC-20"
        queryObject("-rBG -T inet6num 2001::/20", "inet6num", "2001::/20")

      when:
        def message = syncUpdate("""\
                inet6num:     2001::/20
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                status:       ALLOCATED-BY-RIR
                remarks:      version 2
                changed:      dbtest@ripe.net 20130101
                source:       TEST
                override:  override1

                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Modify" && it.key == "[inet6num] 2001::/20" }

        queryLineMatches("--list-versions 2001::/20", "^% Version history for INET6NUM object \"2001::/20\"")
        queryLineMatches("--list-versions 2001::/20", "^1\\s*[0-9-]+\\s*[0-9:]+\\s*ADD/UPD")
        queryLineMatches("--list-versions 2001::/20", "^2\\s*[0-9-]+\\s*[0-9:]+\\s*ADD/UPD")
        ! queryLineMatches("--list-versions 2001::/20", "^3\\s*[0-9-]+\\s*[0-9:]+\\s*ADD/UPD")
    }

    def "query --show-version 2, 2 versions"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "override: override1")

      expect:
        // "RIR-ALLOC-20"
        queryObject("-rBG -T inet6num 2001::/20", "inet6num", "2001::/20")

      when:
        def message = syncUpdate("""\
                inet6num:     2001::/20
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                status:       ALLOCATED-BY-RIR
                remarks:      version 2
                changed:      dbtest@ripe.net 20130101
                source:       TEST
                override:  override1

                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Modify" && it.key == "[inet6num] 2001::/20" }

        queryLineMatches("--show-version 2 2001::/20", "^% Version 2 \\(current version\\) of object \"2001::/20\"")
        queryLineMatches("--show-version 2 2001::/20", "^% This version was a UPDATE operation on ")
        queryLineMatches("--show-version 2 2001::/20", "^remarks:\\s*version 2")
    }

    def "query --list-versions, 3 versions"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "override: override1")

      expect:
        // "ALLOC-PA"
        queryObject("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      when:
        def message = syncUpdate("""\
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                remarks:      version 2
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                override:  override1

                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                remarks:      version 3
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                override:  override1

                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(2, 0, 2, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 2)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.0.0 - 192.169.255.255" }

        queryLineMatches("--list-versions 192.168.0.0 - 192.169.255.255", "^% Version history for INETNUM object \"192.168.0.0 - 192.169.255.255\"")
        queryLineMatches("--list-versions 192.168.0.0 - 192.169.255.255", "^1\\s*[0-9-]+\\s*[0-9:]+\\s*ADD/UPD")
        queryLineMatches("--list-versions 192.168.0.0 - 192.169.255.255", "^2\\s*[0-9-]+\\s*[0-9:]+\\s*ADD/UPD")
        queryLineMatches("--list-versions 192.168.0.0 - 192.169.255.255", "^3\\s*[0-9-]+\\s*[0-9:]+\\s*ADD/UPD")
        ! queryLineMatches("--list-versions 192.168.0.0 - 192.169.255.255", "^4\\s*[0-9-]+\\s*[0-9:]+\\s*ADD/UPD")
    }

    def "query --show-version 3, 3 versions"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "override: override1")

      expect:
        // "ALLOC-PA"
        queryObject("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      when:
        def message = syncUpdate("""\
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                remarks:      version 2
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                override:  override1

                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                remarks:      version 3
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                override:  override1

                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(2, 0, 2, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 2)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.0.0 - 192.169.255.255" }

        queryLineMatches("--show-version 3 192.168.0.0 - 192.169.255.255", "^% Version 3 \\(current version\\) of object \"192.168.0.0 - 192.169.255.255\"")
        queryLineMatches("--show-version 3 192.168.0.0 - 192.169.255.255", "^% This version was a UPDATE operation on ")
        queryLineMatches("--show-version 3 192.168.0.0 - 192.169.255.255", "^remarks:\\s*version 3")
    }

    def "query --show-version 2, 3 versions"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "override: override1")

      expect:
        // "ALLOC-PA"
        queryObject("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      when:
        def message = syncUpdate("""\
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                remarks:      version 2
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                override:  override1

                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                remarks:      version 3
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                override:  override1

                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(2, 0, 2, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 2)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.0.0 - 192.169.255.255" }

        queryLineMatches("--show-version 2 192.168.0.0 - 192.169.255.255", "^% Version 2 of object \"192.168.0.0 - 192.169.255.255\"")
        queryLineMatches("--show-version 2 192.168.0.0 - 192.169.255.255", "^% This version was a UPDATE operation on ")
        queryLineMatches("--show-version 2 192.168.0.0 - 192.169.255.255", "^remarks:\\s*version 2")
    }

    def "query --show-version 1, 3 versions"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "override: override1")

      expect:
        // "ALLOC-PA"
        queryObject("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      when:
        def message = syncUpdate("""\
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                remarks:      version 2
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                override:  override1

                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                remarks:      version 3
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                override:  override1

                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(2, 0, 2, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 2)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.0.0 - 192.169.255.255" }

        queryLineMatches("--show-version 1 192.168.0.0 - 192.169.255.255", "^% Version 1 of object \"192.168.0.0 - 192.169.255.255\"")
        queryLineMatches("--show-version 1 192.168.0.0 - 192.169.255.255", "^% This version was a UPDATE operation on ")
    }

    def "query --list-versions, 4 versions, deleted"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "override: override1")

      expect:
        // "ALLOC-PA"
        queryObject("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      when:
        def message = syncUpdate("""\
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                remarks:      version 2
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                override:  override1

                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                remarks:      version 3
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                override:  override1

                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                remarks:      version 3
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                delete:       comment
                override:  override1

                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 3
        ack.summary.assertSuccess(3, 0, 2, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 3)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.0.0 - 192.169.255.255" }
        ack.successes.any { it.operation == "Delete" && it.key == "[inetnum] 192.168.0.0 - 192.169.255.255" }

        queryObjectNotFound("192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

        queryLineMatches("--list-versions 192.168.0.0 - 192.169.255.255", "^% Version history for INETNUM object \"192.168.0.0 - 192.169.255.255\"")
        queryLineMatches("--list-versions 192.168.0.0 - 192.169.255.255", "% This object was deleted on")
        ! queryLineMatches("--list-versions 192.168.0.0 - 192.169.255.255", "^1\\s*[0-9-]+\\s*[0-9:]+\\s*ADD/UPD")
    }

    def "query --list-versions, 4 versions, deleted, re-created with only 1 version of new object"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "override: override1")

      expect:
        // "ALLOC-PA"
        queryObject("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      when:
        def message = syncUpdate("""\
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                remarks:      version 2
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                override:  override1

                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                remarks:      version 2
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                delete:       comment
                override:  override1

                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                remarks:      version 1
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                override:  override1

                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 3
        ack.summary.assertSuccess(3, 1, 1, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 3)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.0.0 - 192.169.255.255" }
        ack.successes.any { it.operation == "Delete" && it.key == "[inetnum] 192.168.0.0 - 192.169.255.255" }
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.169.255.255" }

        query_object_matches("-GBr 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255", "version 1")

        queryLineMatches("--list-versions 192.168.0.0 - 192.169.255.255", "^% Version history for INETNUM object \"192.168.0.0 - 192.169.255.255\"")
        queryLineMatches("--list-versions 192.168.0.0 - 192.169.255.255", "^1\\s*[0-9-]+\\s*[0-9:]+\\s*ADD/UPD")
        ! queryLineMatches("--list-versions 192.168.0.0 - 192.169.255.255", "^2\\s*[0-9-]+\\s*[0-9:]+\\s*ADD/UPD")
    }

    def "query --list-versions, 2 versions, person object"() {
      given:
        syncUpdate(getTransient("PN-FF") + "override: override1")

      expect:
        // "PN-FF"
        queryObject("-rBG -T person ff1-test", "person", "fred fred")

      when:
        def message = syncUpdate("""\
                person:  fred fred
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: ff1-TEST
                mnt-by:  OWNER-MNT
                remarks: version 2
                changed: dbtest@ripe.net 20120101
                source:  TEST
                override:  override1
                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Modify" && it.key == "[person] ff1-TEST   fred fred" }

        query_object_matches("-GBr ff1-test", "person", "Fred Fred", "version 2")

        queryLineMatches("--list-versions ff1-test", "^% Version history for PERSON object \"ff1-test\"")
        queryLineMatches("--list-versions ff1-test", "% History not available for PERSON and ROLE objects")
        ! queryLineMatches("--list-versions ff1-test", "^1\\s*[0-9-]+\\s*[0-9:]+\\s*ADD/UPD")
    }

    def "query --list-versions, 2 versions, role object"() {
      given:
        syncUpdate(getTransient("RL-FR") + "override: override1")

      expect:
        // "RL-FR"
        queryObject("-rBG -T role FR1-TEST", "role", "First Role")

      when:
        def message = syncUpdate("""\
                role:    First Role
                address: St James Street
                address: Burnley
                address: UK
                e-mail:  dbtest@ripe.net
                nic-hdl: FR1-TEST
                mnt-by:  OWNER-MNT
                remarks: version 2
                changed: dbtest@ripe.net 20121016
                source:  TEST
                override:  override1

                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Modify" && it.key == "[role] FR1-TEST   First Role" }

        query_object_matches("-GBr FR1-TEST", "role", "First Role", "version 2")

        queryLineMatches("--list-versions FR1-TEST", "^% Version history for ROLE object \"FR1-TEST\"")
        queryLineMatches("--list-versions FR1-TEST", "% History not available for PERSON and ROLE objects")
        ! queryLineMatches("--list-versions FR1-TEST", "^1\\s*[0-9-]+\\s*[0-9:]+\\s*ADD/UPD")
    }

    def "query --list-versions, 2 versions, organisation object"() {
      when:
        def message = syncUpdate("""\
                organisation:    auto-1
                org-type:        other
                org-name:        First Version Org
                org:             ORG-FVO1-TEST
                address:         RIPE NCC
                                 Singel 258
                                 1016 AB Amsterdam
                                 Netherlands
                e-mail:          dbtest@ripe.net
                mnt-ref:         owner3-mnt
                mnt-by:          owner2-mnt
                changed:         denis@ripe.net 20121016
                source:          TEST
                override:  override1

                organisation:    ORG-FVO1-TEST
                org-type:        other
                org-name:        First Org
                org:             ORG-FVO1-TEST
                address:         RIPE NCC
                                 Singel 258
                                 1016 AB Amsterdam
                                 Netherlands
                e-mail:          dbtest@ripe.net
                mnt-ref:         owner3-mnt
                mnt-by:          owner2-mnt
                remarks: version 2
                changed:         denis@ripe.net 20121016
                source:          TEST
                override:  override1

                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(2, 1, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 2)
        ack.successes.any { it.operation == "Modify" && it.key == "[organisation] ORG-FVO1-TEST" }

        query_object_matches("-GBr ORG-FVO1-TEST", "organisation", "ORG-FVO1-TEST", "version 2")

        queryLineMatches("--list-versions ORG-FVO1-TEST", "^% Version history for ORGANISATION object \"ORG-FVO1-TEST\"")
        queryLineMatches("--list-versions ORG-FVO1-TEST", "^2\\s*[0-9-]+\\s*[0-9:]+\\s*ADD/UPD")
        ! queryLineMatches("--list-versions ORG-FVO1-TEST", "^3\\s*[0-9-]+\\s*[0-9:]+\\s*ADD/UPD")
    }

    def "query --list-versions, 2 versions, mntner object"() {
      given:
        syncUpdate(getTransient("SELF-MNT") + "override: override1")

      expect:
        // "SELF-MNT"
        queryObject("-rBG -T mntner SELF-MNT", "mntner", "SELF-MNT")

      when:
        def message = syncUpdate("""\
                mntner:  SELF-MNT
                descr:   description
                admin-c: TP1-TEST
                mnt-by:  SELF-MNT
                referral-by: SELF-MNT
                upd-to:  updto_cre@ripe.net
                auth:    MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                remarks: version 2
                changed: dbtest@ripe.net 20120707
                source:  TEST
                override:  override1

                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Modify" && it.key == "[mntner] SELF-MNT" }

        query_object_matches("-GBr SELF-MNT", "mntner", "SELF-MNT", "version 2")

        queryLineMatches("--list-versions SELF-MNT", "^% Version history for MNTNER object \"SELF-MNT\"")
        queryLineMatches("--list-versions SELF-MNT", "^2\\s*[0-9-]+\\s*[0-9:]+\\s*ADD/UPD")
        ! queryLineMatches("--list-versions SELF-MNT", "^3\\s*[0-9-]+\\s*[0-9:]+\\s*ADD/UPD")
    }

    def "query --list-versions, 2 versions, mntner object with name PAUL"() {
      given:
        syncUpdate(getTransient("PAUL") + "override: override1")

      expect:
        // "SELF-MNT"
        queryObject("-rBG -T mntner PAUL", "mntner", "PAUL")

      when:
        def message = syncUpdate("""\
                mntner:  PAUL
                descr:   description
                admin-c: TP1-TEST
                mnt-by:  PAUL
                referral-by: PAUL
                upd-to:  updto_cre@ripe.net
                auth:    MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                remarks: version 2
                changed: dbtest@ripe.net 20120707
                source:  TEST
                override:  override1

                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Modify" && it.key == "[mntner] PAUL" }

        query_object_matches("-GBr PAUL", "mntner", "PAUL", "version 2")

        queryLineMatches("--list-versions PAUL", "^% Version history for MNTNER object \"PAUL\"")
        queryLineMatches("--list-versions PAUL", "^2\\s*[0-9-]+\\s*[0-9:]+\\s*ADD/UPD")
        ! queryLineMatches("--list-versions PAUL", "^3\\s*[0-9-]+\\s*[0-9:]+\\s*ADD/UPD")
    }

    def "query help"() {
      when:
        def qqq = query("help")

      then:
        qqq =~ /(?m)^%\s*--list-versions/
    }

    def "query --list-versions, 2 exact matching route, 2 versions of each"() {
      given:
        syncUpdate(getTransient("ASS-END") + "override: override1")
        syncUpdate(getTransient("AS1000") + "override: override1")
        syncUpdate(getTransient("AS2000") + "override: override1")

      expect:
        // "ASS-END"
        queryObject("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
        // "AS1000"
        queryObject("-rBG -T aut-num AS1000", "aut-num", "AS1000")
        // "AS2000"
        queryObject("-rBG -T aut-num AS2000", "aut-num", "AS2000")

      when:
        def message = syncUpdate("""\
                route:          192.168.200.0/24
                descr:          Route
                origin:         AS1000
                mnt-by:         LIR-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST
                override:  override1

                route:          192.168.200.0/24
                descr:          Route
                origin:         AS1000
                mnt-by:         LIR-MNT
                remarks:        version 2
                changed:        noreply@ripe.net 20120101
                source:         TEST
                override:  override1

                route:          192.168.200.0/24
                descr:          Route
                origin:         AS2000
                mnt-by:         LIR-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST
                override:  override1

                route:          192.168.200.0/24
                descr:          Route
                origin:         AS2000
                mnt-by:         LIR-MNT
                remarks:        version 2
                changed:        noreply@ripe.net 20120101
                source:         TEST
                override:  override1

                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 4
        ack.summary.assertSuccess(4, 2, 2, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 4)
        ack.successes.any { it.operation == "Create" && it.key == "[route] 192.168.200.0/24AS1000" }
        ack.successes.any { it.operation == "Modify" && it.key == "[route] 192.168.200.0/24AS1000" }

        query_object_matches("-GBr -T route 192.168.200.0/24", "route", "192.168.200.0/24", "version 2")

        queryLineMatches("--list-versions 192.168.200.0/24AS1000", "^% Version history for ROUTE object \"192.168.200.0/24AS1000\"")
        queryLineMatches("--list-versions 192.168.200.0/24AS1000", "^2\\s*[0-9-]+\\s*[0-9:]+\\s*ADD/UPD")
        ! queryLineMatches("--list-versions 192.168.200.0/24AS1000", "^3\\s*[0-9-]+\\s*[0-9:]+\\s*ADD/UPD")

        queryLineMatches("--list-versions 192.168.200.0/24AS2000", "^% Version history for ROUTE object \"192.168.200.0/24AS2000\"")
        queryLineMatches("--list-versions 192.168.200.0/24AS2000", "^2\\s*[0-9-]+\\s*[0-9:]+\\s*ADD/UPD")
        ! queryLineMatches("--list-versions 192.168.200.0/24AS2000", "^3\\s*[0-9-]+\\s*[0-9:]+\\s*ADD/UPD")
    }

    def "query --show-version for 2 exact matching route, 2 & 3 versions exist"() {
      given:
        syncUpdate(getTransient("ASS-END") + "override: override1")
        syncUpdate(getTransient("AS1000") + "override: override1")
        syncUpdate(getTransient("AS2000") + "override: override1")

      expect:
        // "ASS-END"
        queryObject("-rBG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
        // "AS1000"
        queryObject("-rBG -T aut-num AS1000", "aut-num", "AS1000")
        // "AS2000"
        queryObject("-rBG -T aut-num AS2000", "aut-num", "AS2000")

      when:
        def message = syncUpdate("""\
                route:          192.168.200.0/24
                descr:          Route
                origin:         AS1000
                mnt-by:         LIR-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST
                override:  override1

                route:          192.168.200.0/24
                descr:          Route
                origin:         AS1000
                mnt-by:         LIR-MNT
                remarks:        version 2
                changed:        noreply@ripe.net 20120101
                source:         TEST
                override:  override1

                route:          192.168.200.0/24
                descr:          Route
                origin:         AS2000
                mnt-by:         LIR-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST
                override:  override1

                route:          192.168.200.0/24
                descr:          Route
                origin:         AS2000
                mnt-by:         LIR-MNT
                remarks:        version 2
                changed:        noreply@ripe.net 20120101
                source:         TEST
                override:  override1

                route:          192.168.200.0/24
                descr:          Route
                origin:         AS2000
                mnt-by:         LIR-MNT
                remarks:        version 3
                changed:        noreply@ripe.net 20120101
                source:         TEST
                override:  override1

                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 5
        ack.summary.assertSuccess(5, 2, 3, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 5)
        ack.successes.any { it.operation == "Create" && it.key == "[route] 192.168.200.0/24AS1000" }
        ack.successes.any { it.operation == "Modify" && it.key == "[route] 192.168.200.0/24AS1000" }

        query_object_matches("-GBr -T route 192.168.200.0/24", "route", "192.168.200.0/24", "version 2")
        query_object_matches("-GBr -T route 192.168.200.0/24", "route", "192.168.200.0/24", "version 3")

        queryLineMatches("--show-version 2 192.168.200.0/24AS1000", "^% Version 2 \\(current version\\) of object \"192.168.200.0/24AS1000\"")
        queryLineMatches("--show-version 2 192.168.200.0/24AS1000", "^% This version was a UPDATE operation on ")
        queryLineMatches("--show-version 2 192.168.200.0/24AS1000", "^remarks:\\s*version 2")

        queryLineMatches("--show-version 2 192.168.200.0/24AS2000", "^% Version 2 of object \"192.168.200.0/24AS2000\"")
        queryLineMatches("--show-version 2 192.168.200.0/24AS2000", "^% This version was a UPDATE operation on ")
        queryLineMatches("--show-version 2 192.168.200.0/24AS2000", "^remarks:\\s*version 2")
    }

    def "query --version-list, 3 versions, no space in range"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "override: override1")

      expect:
        // "ALLOC-PA"
        queryObject("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      when:
        def message = syncUpdate("""\
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                remarks:      version 2
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                override:  override1

                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                remarks:      version 3
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                override:  override1

                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(2, 0, 2, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 2)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.0.0 - 192.169.255.255" }

        queryLineMatches("--list-versions 192.168.0.0-192.169.255.255", "^% Version history for INETNUM object \"192.168.0.0 - 192.169.255.255\"")
        queryLineMatches("--list-versions 192.168.0.0-192.169.255.255", "^1\\s*[0-9-]+\\s*[0-9:]+\\s*ADD/UPD")
        queryLineMatches("--list-versions 192.168.0.0-192.169.255.255", "^2\\s*[0-9-]+\\s*[0-9:]+\\s*ADD/UPD")
        queryLineMatches("--list-versions 192.168.0.0-192.169.255.255", "^3\\s*[0-9-]+\\s*[0-9:]+\\s*ADD/UPD")
        ! queryLineMatches("--list-versions 192.168.0.0 - 192.169.255.255", "^4\\s*[0-9-]+\\s*[0-9:]+\\s*ADD/UPD")
    }

    def "query --show-version 5, 2 versions"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "override: override1")

      expect:
        // "RIR-ALLOC-20"
        queryObject("-rBG -T inet6num 2001::/20", "inet6num", "2001::/20")

      when:
        def message = syncUpdate("""\
                inet6num:     2001::/20
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                status:       ALLOCATED-BY-RIR
                remarks:      version 2
                changed:      dbtest@ripe.net 20130101
                source:       TEST
                override:  override1

                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Modify" && it.key == "[inet6num] 2001::/20" }

        queryLineMatches("--show-version 5 2001::/20", "^%ERROR:117: version cannot exceed 2 for this object")
        ! queryLineMatches("--show-version 5 2001::/20", "^% Version 5 of object \"2001::/20\"")
    }

    def "query --show-version 0, 2 versions"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "override: override1")

      expect:
        // "RIR-ALLOC-20"
        queryObject("-rBG -T inet6num 2001::/20", "inet6num", "2001::/20")

      when:
        def message = syncUpdate("""\
                inet6num:     2001::/20
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                status:       ALLOCATED-BY-RIR
                remarks:      version 2
                changed:      dbtest@ripe.net 20130101
                source:       TEST
                override:  override1

                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Modify" && it.key == "[inet6num] 2001::/20" }

        queryLineMatches("--show-version 0 2001::/20", "^% version flag number must be greater than 0")
        ! queryLineMatches("--show-version 0 2001::/20", "^% Version 0 of object \"2001::/20\"")
        queryLineMatches("--show-version 0 2001::/20", "^%ERROR:111: invalid option supplied")
    }

    def "query --show-version -2, 2 versions"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "override: override1")

      expect:
        // "RIR-ALLOC-20"
        queryObject("-rBG -T inet6num 2001::/20", "inet6num", "2001::/20")

      when:
        def message = syncUpdate("""\
                inet6num:     2001::/20
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                status:       ALLOCATED-BY-RIR
                remarks:      version 2
                changed:      dbtest@ripe.net 20130101
                source:       TEST
                override:  override1

                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Modify" && it.key == "[inet6num] 2001::/20" }

        queryLineMatches("--show-version -2 2001::/20", "^% version flag number must be greater than 0")
        ! queryLineMatches("--show-version -2 2001::/20", "^% Version -2 of object \"2001::/20\"")
        queryLineMatches("--show-version -2 2001::/20", "^%ERROR:111: invalid option supplied")
    }

    def "query --show-version 1.5, 2 versions"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "override: override1")

      expect:
        // "RIR-ALLOC-20"
        queryObject("-rBG -T inet6num 2001::/20", "inet6num", "2001::/20")

      when:
        def message = syncUpdate("""\
                inet6num:     2001::/20
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                status:       ALLOCATED-BY-RIR
                remarks:      version 2
                changed:      dbtest@ripe.net 20130101
                source:       TEST
                override:  override1

                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Modify" && it.key == "[inet6num] 2001::/20" }

        ! queryLineMatches("--show-version 1.5 2001::/20", "^% Version 1.5 of object \"2001::/20\"")
        queryLineMatches("--show-version 1.5 2001::/20", "^%ERROR:111: invalid option supplied")
    }

    def "query --show-version fred, 2 versions"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "override: override1")

      expect:
        // "RIR-ALLOC-20"
        queryObject("-rBG -T inet6num 2001::/20", "inet6num", "2001::/20")

      when:
        def message = syncUpdate("""\
                inet6num:     2001::/20
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                status:       ALLOCATED-BY-RIR
                remarks:      version 2
                changed:      dbtest@ripe.net 20130101
                source:       TEST
                override:  override1

                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Modify" && it.key == "[inet6num] 2001::/20" }

        queryLineMatches("--show-version fred 2001::/20", "^%ERROR:111: invalid option supplied")
    }

    def "query --show-version 2 and --list-versions, 2 versions"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "override: override1")

      expect:
        // "RIR-ALLOC-20"
        queryObject("-rBG -T inet6num 2001::/20", "inet6num", "2001::/20")

      when:
        def message = syncUpdate("""\
                inet6num:     2001::/20
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                status:       ALLOCATED-BY-RIR
                remarks:      version 2
                changed:      dbtest@ripe.net 20130101
                source:       TEST
                override:  override1

                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Modify" && it.key == "[inet6num] 2001::/20" }

        queryLineMatches("--list-versions --show-version 2 2001::/20", "^%ERROR:109: invalid combination of flags passed")
        queryLineMatches("--list-versions --show-version 2 2001::/20", "^% The flags \"--list-versions\" and \"--show-version\" cannot be used together.") ||
        queryLineMatches("--list-versions --show-version 2 2001::/20", "^% The flags \"--show-version\" and \"--list-versions\" cannot be used together.")
    }

    def "query --show-version 2 and -b, 2 versions"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "override: override1")

      expect:
        // "RIR-ALLOC-20"
        queryObject("-rBG -T inet6num 2001::/20", "inet6num", "2001::/20")

      when:
        def message = syncUpdate("""\
                inet6num:     2001::/20
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                status:       ALLOCATED-BY-RIR
                remarks:      version 2
                changed:      dbtest@ripe.net 20130101
                source:       TEST
                override:  override1

                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Modify" && it.key == "[inet6num] 2001::/20" }

        queryLineMatches("-b --show-version 2 2001::/20", "^%ERROR:109: invalid combination of flags passed")
    }

    def "query --list-versions and -m, 2 versions"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "override: override1")

      expect:
        // "RIR-ALLOC-20"
        queryObject("-rBG -T inet6num 2001::/20", "inet6num", "2001::/20")

      when:
        def message = syncUpdate("""\
                inet6num:     2001::/20
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                status:       ALLOCATED-BY-RIR
                remarks:      version 2
                changed:      dbtest@ripe.net 20130101
                source:       TEST
                override:  override1

                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Modify" && it.key == "[inet6num] 2001::/20" }

        queryLineMatches("-m --list-versions 2001::/20", "^%ERROR:109: invalid combination of flags passed")
    }

    def "query --list-versions and -k, 2 versions"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "override: override1")

      expect:
        // "RIR-ALLOC-20"
        queryObject("-rBG -T inet6num 2001::/20", "inet6num", "2001::/20")

      when:
        def message = syncUpdate("""\
                inet6num:     2001::/20
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                status:       ALLOCATED-BY-RIR
                remarks:      version 2
                changed:      dbtest@ripe.net 20130101
                source:       TEST
                override:  override1

                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Modify" && it.key == "[inet6num] 2001::/20" }

        queryLineMatches("-k --show-version 1 2001::/20\n\n--show-version 2 2001::/20\n\n-k", "^% Version 1 of object \"2001::/20\"")
        queryLineMatches("-k --show-version 1 2001::/20\n\n--show-version 2 2001::/20\n\n-k", "^% Version 2 \\(current version\\) of object \"2001::/20\"")
//        queryLineMatches("-k -rBG 2001::/20\n\n--show-version 1 2001::/20\n\n--show-version 2 2001::/20\n\n-k", "^% Version 1 of object \"2001::/20\"")
    }

    def "query --show-version 2 and -F, 2 versions"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "override: override1")

      expect:
        // "RIR-ALLOC-20"
        queryObject("-rBG -T inet6num 2001::/20", "inet6num", "2001::/20")

      when:
        def message = syncUpdate("""\
                inet6num:     2001::/20
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                status:       ALLOCATED-BY-RIR
                remarks:      version 2
                changed:      dbtest@ripe.net 20130101
                source:       TEST
                override:  override1

                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Modify" && it.key == "[inet6num] 2001::/20" }

        queryLineMatches("-F --show-version 2 2001::/20", "^%ERROR:109: invalid combination of flags passed")
    }

    def "query --show-version 2 and -K, 2 versions"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "override: override1")

      expect:
        // "RIR-ALLOC-20"
        queryObject("-rBG -T inet6num 2001::/20", "inet6num", "2001::/20")

      when:
        def message = syncUpdate("""\
                inet6num:     2001::/20
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                status:       ALLOCATED-BY-RIR
                remarks:      version 2
                changed:      dbtest@ripe.net 20130101
                source:       TEST
                override:  override1

                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Modify" && it.key == "[inet6num] 2001::/20" }

        queryLineMatches("-K --show-version 2 2001::/20", "^%ERROR:109: invalid combination of flags passed")
    }

    def "query --show-version 2 and -T route6, 2 versions"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "override: override1")

      expect:
        // "RIR-ALLOC-20"
        queryObject("-rBG -T inet6num 2001::/20", "inet6num", "2001::/20")

      when:
        def message = syncUpdate("""\
                inet6num:     2001::/20
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                status:       ALLOCATED-BY-RIR
                remarks:      version 2
                changed:      dbtest@ripe.net 20130101
                source:       TEST
                override:  override1

                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Modify" && it.key == "[inet6num] 2001::/20" }

        queryLineMatches("-T route6 --show-version 2 2001::/20", "^%ERROR:109: invalid combination of flags passed")
    }

    def "query --show-version 2 and -V client-tag, 2 versions"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "override: override1")

      expect:
        // "RIR-ALLOC-20"
        queryObject("-rBG -T inet6num 2001::/20", "inet6num", "2001::/20")

      when:
        def message = syncUpdate("""\
                inet6num:     2001::/20
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                status:       ALLOCATED-BY-RIR
                remarks:      version 2
                changed:      dbtest@ripe.net 20130101
                source:       TEST
                override:  override1

                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Modify" && it.key == "[inet6num] 2001::/20" }

        queryLineMatches("-V fred --show-version 2 2001::/20", "^% Version 2 \\(current version\\) of object \"2001::/20\"")
        queryLineMatches("-V fred --show-version 2 2001::/20", "^% This version was a UPDATE operation on ")
        queryLineMatches("-V fred --show-version 2 2001::/20", "^remarks:\\s*version 2")
    }

    def "query --show-version 2, crypt-pw should be hidden (extra version 1 created by loader)"() {
        when:
            def message = syncUpdate("""\
                mntner:         OLD-MNT
                descr:          description
                admin-c:        TP1-TEST
                mnt-by:         OLD-MNT
                referral-by:    OLD-MNT
                upd-to:         updto_cre@ripe.net
                auth:           MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                changed:        dbtest@ripe.net 20120707
                source:         TEST
                override: override1

                """.stripIndent()
            )

        then:
            def ack = new AckResponse("", message)

            ack.summary.nrFound == 1
            ack.summary.assertSuccess(1, 0, 1, 0, 0)
            ack.summary.assertErrors(0, 0, 0, 0)

            ack.countErrorWarnInfo(0, 0, 1)
            ack.successes.any { it.operation == "Modify" && it.key == "[mntner] OLD-MNT" }

            queryLineMatches("--show-version 2 OLD-MNT", "^% Version 2 of object \"OLD-MNT\"")
            queryLineMatches("--show-version 2 OLD-MNT", "^auth:\\s+CRYPT-PW # Filtered")
            queryLineMatches("--show-version 2 OLD-MNT", "^auth:\\s+MD5-PW # Filtered")
    }

    def "query --dif-versions 2:3, 3 versions"() {
        given:
        syncUpdate(getTransient("ALLOC-PA") + "override: override1")

        expect:
        // "ALLOC-PA"
        queryObject("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

        when:
        def message = syncUpdate("""\
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                remarks:      version 2
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                override:  override1

                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                remarks:      version 3
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                override:  override1

                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(2, 0, 2, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 2)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.0.0 - 192.169.255.255" }

        queryLineMatches("--show-version 1 192.168.0.0 - 192.169.255.255", "^% Version 1 of object \"192.168.0.0 - 192.169.255.255\"")
        queryLineMatches("--show-version 1 192.168.0.0 - 192.169.255.255", "^% This version was a UPDATE operation on ")
        queryLineMatches("--diff-versions 2:3 192.168.0.0 - 192.169.255.255", "-remarks:\\s*version 2")
        queryLineMatches("--diff-versions 2:3 192.168.0.0 - 192.169.255.255", "\\+remarks:\\s*version 3")
    }

    def "query --dif-versions 3:2, 3 versions"() {
        given:
        syncUpdate(getTransient("ALLOC-PA") + "override: override1")

        expect:
        // "ALLOC-PA"
        queryObject("-rBG -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

        when:
        def message = syncUpdate("""\
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                remarks:      version 2
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                override:  override1

                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                remarks:      version 3
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                override:  override1

                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(2, 0, 2, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 2)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.0.0 - 192.169.255.255" }

        queryLineMatches("--show-version 1 192.168.0.0 - 192.169.255.255", "^% Version 1 of object \"192.168.0.0 - 192.169.255.255\"")
        queryLineMatches("--show-version 1 192.168.0.0 - 192.169.255.255", "^% This version was a UPDATE operation on ")
        queryLineMatches("--diff-versions 3:2 192.168.0.0 - 192.169.255.255", "\\+remarks:\\s*version 2")
        queryLineMatches("--diff-versions 3:2 192.168.0.0 - 192.169.255.255", "-remarks:\\s*version 3")
    }

}
