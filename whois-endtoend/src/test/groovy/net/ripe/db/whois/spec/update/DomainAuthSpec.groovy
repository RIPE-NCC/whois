package net.ripe.db.whois.spec.update


import net.ripe.db.whois.spec.BaseQueryUpdateSpec
import net.ripe.db.whois.spec.domain.AckResponse
import org.junit.jupiter.api.Tag

@Tag("IntegrationTest")
class DomainAuthSpec extends BaseQueryUpdateSpec {

    @Override
    Map<String, String> getTransients() {
        [
            "AS0 - AS4294967295": """\
                as-block:       AS0 - AS4294967295
                descr:          Full ASN range
                mnt-by:         RIPE-DBM-MNT
                mnt-lower:      RIPE-NCC-HM-MNT
                source:         TEST
                """,
            "ALLOC-PA": """\
                inetnum:      193.0.0.0 - 193.255.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                source:       TEST
                """,
            "ALLOC-PA-LOW": """\
                inetnum:      193.0.0.0 - 193.255.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                source:       TEST
                """,
            "ALLOC-PA-LOW-DOM": """\
                inetnum:      193.0.0.0 - 193.255.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                mnt-domains:  LIR2-MNT
                source:       TEST
                """,
            "ALLOC-PA-LOW-DOM-R": """\
                inetnum:      193.0.0.0 - 193.255.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                mnt-domains:  LIR2-MNT
                mnt-routes:   LIR3-MNT
                source:       TEST
                """,
            "ASSIGN-PA-LOW-DOM": """\
                inetnum:      193.0.0.0 - 193.0.0.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       OWNER-MNT
                mnt-lower:    LIR-MNT
                mnt-domains:  LIR2-MNT
                source:       TEST
                """,
            "ALLOC-PA-LOW-R": """\
                inetnum:      193.0.0.0 - 193.255.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                mnt-routes:   LIR3-MNT
                source:       TEST
                """,
            "ALLOC-PA-R": """\
                inetnum:      193.0.0.0 - 193.255.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-routes:   LIR3-MNT
                source:       TEST
                """,
            "ALLOC-U": """\
                inetnum:      192.0.0.0 - 192.255.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED Unspecified
                mnt-by:       RIPE-NCC-HM-MNT
                source:       TEST
                """,
            "ALLOC-PA1": """\
                inetnum:      192.0.0.0 - 192.0.0.0
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       LIR-MNT
                source:       TEST
                """,
            "ALLOC-PA2": """\
                inetnum:      192.0.0.0 - 192.0.0.1
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       LIR-MNT
                source:       TEST
                """,
            "ALLOC-DOMAIN": """\
                domain:         193.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         DOMAIN-MNT
                source:         TEST
                """,
            "ASSIGN-DOMAIN": """\
                domain:         0.0.193.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         DOMAIN-MNT
                source:         TEST
                """,
            "OVERLAP-DOMAIN": """\
                domain:         128-191.0.0.193.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         DOMAIN-MNT
                source:         TEST
                """,
            "ALLOC6-PA-LOW-DOM-R": """\
                inet6num:     2001::/16
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED-BY-RIR
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                mnt-domains:  LIR2-MNT
                mnt-routes:   LIR3-MNT
                source:       TEST
                """,
            "ALLOC6-PA-NO-DOM-R": """\
                inet6num:     2a03:3460::/32
                netname:      CZ-POSITION-20140915
                descr:        Position s.r.o
                country:      CZ
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED-BY-RIR
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                source:       TEST
                """,            "ASSIGN6-DOMAIN": """\
                domain:         0.e.0.0.c.7.6.0.1.0.0.2.ip6.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         owner-MNT
                source:         TEST
                """,
            "ENUM-ROOT": """\
                domain:         e164.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         owner-MNT
                source:         TEST
                """,
            "ENUM-UK": """\
                domain:         4.4.e164.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        ns1.nic.uk
                nserver:        ns3.nic.uk
                mnt-by:         owner-MNT
                mnt-by:         RIPE-GII-MNT
                source:         TEST
                """,
    ]}

    def "create reverse domain, ripe space, exact match inetnum with mnt-domains, domains pw supplied"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-LOW-DOM-R") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 193.0.0.0 - 193.255.255.255", "inetnum", "193.0.0.0 - 193.255.255.255")

      expect:
        queryObjectNotFound("-rGBT domain 193.in-addr.arpa", "domain", "193.in-addr.arpa")

      when:
        def message = syncUpdate("""\
                domain:         193.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         owner-MNT
                source:         TEST

                password:   lir2
                password:   owner
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[domain] 193.in-addr.arpa" }

        queryObject("-rGBT domain 193.in-addr.arpa", "domain", "193.in-addr.arpa")
    }

    def "create reverse domain, single IP, ripe space, exact match inetnum with mnt-domains, domains pw supplied"() {
        given:
        syncUpdate(getTransient("ALLOC-U") + "override: denis,override1")
        syncUpdate(getTransient("ALLOC-PA1") + "override: denis,override1")

        expect:
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        queryObject("-r -T inetnum 192.0.0.0 - 192.0.0.0", "inetnum", "192.0.0.0 - 192.0.0.0")
        queryObjectNotFound("-rGBT domain 0.0.0.192.in-addr.arpa", "domain", "0.0.0.192.in-addr.arpa")

        when:
        def message = syncUpdate("""\
                domain:         0.0.0.192.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         owner-MNT
                source:         TEST

                password:   lir
                password:   owner
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[domain] 0.0.0.192.in-addr.arpa" }

        queryObject("-rGBT domain 0.0.0.192.in-addr.arpa", "domain", "0.0.0.192.in-addr.arpa")
    }

    def "create reverse domain, range of 2 IP, ripe space, exact match inetnum with mnt-domains, domains pw supplied"() {
        given:
        syncUpdate(getTransient("ALLOC-U") + "override: denis,override1")
        syncUpdate(getTransient("ALLOC-PA2") + "override: denis,override1")

        expect:
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        queryObject("-r -T inetnum 192.0.0.0 - 192.0.0.1", "inetnum", "192.0.0.0 - 192.0.0.1")
        queryObjectNotFound("-rGBT domain 0-1.0.0.192.in-addr.arpa", "domain", "0-1.0.0.192.in-addr.arpa")

        when:
        def message = syncUpdate("""\
                domain:         0-1.0.0.192.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         owner-MNT
                source:         TEST

                password:   lir
                password:   owner
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[domain] 0-1.0.0.192.in-addr.arpa" }

        queryObject("-rGBT domain 0-1.0.0.192.in-addr.arpa", "domain", "0-1.0.0.192.in-addr.arpa")
    }

    def "create reverse domain, ripe space, exact match inetnum with mnt-domains, routes pw supplied"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-LOW-DOM-R") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 193.0.0.0 - 193.255.255.255", "inetnum", "193.0.0.0 - 193.255.255.255")

      expect:
        queryObjectNotFound("-rGBT domain 193.in-addr.arpa", "domain", "193.in-addr.arpa")

      when:
        def message = syncUpdate("""\
                domain:         193.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         owner-MNT
                source:         TEST

                password:   lir3
                password:   owner
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[domain] 193.in-addr.arpa" }
        ack.errorMessagesFor("Create", "[domain] 193.in-addr.arpa") ==
                ["Authorisation for [inetnum] 193.0.0.0 - 193.255.255.255 failed using \"mnt-domains:\" not authenticated by: LIR2-MNT"]

        queryObjectNotFound("-rGBT domain 193.in-addr.arpa", "domain", "193.in-addr.arpa")
    }

    def "create reverse domain, ripe space, exact match inetnum with mnt-domains, -by pw supplied"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-LOW-DOM-R") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 193.0.0.0 - 193.255.255.255", "inetnum", "193.0.0.0 - 193.255.255.255")

      expect:
        queryObjectNotFound("-rGBT domain 193.in-addr.arpa", "domain", "193.in-addr.arpa")

      when:
        def message = syncUpdate("""\
                domain:         193.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         owner-MNT
                source:         TEST

                password:   hm
                password:   owner
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[domain] 193.in-addr.arpa" }
        ack.errorMessagesFor("Create", "[domain] 193.in-addr.arpa") ==
                ["Authorisation for [inetnum] 193.0.0.0 - 193.255.255.255 failed using \"mnt-domains:\" not authenticated by: LIR2-MNT"]

        queryObjectNotFound("-rGBT domain 193.in-addr.arpa", "domain", "193.in-addr.arpa")
    }

    def "create reverse domain, ripe space, exact match inetnum with mnt-lower no mnt-domains, lower pw supplied"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-LOW-R") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 193.0.0.0 - 193.255.255.255", "inetnum", "193.0.0.0 - 193.255.255.255")

      expect:
        queryObjectNotFound("-rGBT domain 193.in-addr.arpa", "domain", "193.in-addr.arpa")

      when:
        def message = syncUpdate("""\
                domain:         193.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         owner-MNT
                source:         TEST

                password:   lir
                password:   owner
               """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[domain] 193.in-addr.arpa" }
        ack.errorMessagesFor("Create", "[domain] 193.in-addr.arpa") ==
                ["Authorisation for [inetnum] 193.0.0.0 - 193.255.255.255 failed using \"mnt-by:\" not authenticated by: RIPE-NCC-HM-MNT"]

        queryObjectNotFound("-rGBT domain 193.in-addr.arpa", "domain", "193.in-addr.arpa")
    }

    def "create reverse domain, ripe space, exact match inetnum with mnt-lower no mnt-domains, routes pw supplied"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-LOW-R") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 193.0.0.0 - 193.255.255.255", "inetnum", "193.0.0.0 - 193.255.255.255")

      expect:
        queryObjectNotFound("-rGBT domain 193.in-addr.arpa", "domain", "193.in-addr.arpa")

      when:
        def message = syncUpdate("""\
                domain:         193.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         owner-MNT
                source:         TEST

                password:   lir3
                password:   owner
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[domain] 193.in-addr.arpa" }
        ack.errorMessagesFor("Create", "[domain] 193.in-addr.arpa") ==
                ["Authorisation for [inetnum] 193.0.0.0 - 193.255.255.255 failed using \"mnt-by:\" not authenticated by: RIPE-NCC-HM-MNT"]

        queryObjectNotFound("-rGBT domain 193.in-addr.arpa", "domain", "193.in-addr.arpa")
    }

    def "create reverse domain, ripe space, exact match inetnum with mnt-lower no mnt-domains, -by pw supplied"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-LOW-R") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 193.0.0.0 - 193.255.255.255", "inetnum", "193.0.0.0 - 193.255.255.255")

      expect:
        queryObjectNotFound("-rGBT domain 193.in-addr.arpa", "domain", "193.in-addr.arpa")

      when:
        def message = syncUpdate("""\
                domain:         193.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         owner-MNT
                source:         TEST

                password:   hm
                password:   owner
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[domain] 193.in-addr.arpa" }

        queryObject("-rGBT domain 193.in-addr.arpa", "domain", "193.in-addr.arpa")
    }

    def "create reverse domain, ripe space, exact match inetnum with no mnt-lower no mnt-domains, -by pw supplied"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-R") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 193.0.0.0 - 193.255.255.255", "inetnum", "193.0.0.0 - 193.255.255.255")

      expect:
        queryObjectNotFound("-rGBT domain 193.in-addr.arpa", "domain", "193.in-addr.arpa")

      when:
        def message = syncUpdate("""\
                domain:         193.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         owner-MNT
                source:         TEST

                password:   hm
                password:   owner
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[domain] 193.in-addr.arpa" }

        queryObject("-rGBT domain 193.in-addr.arpa", "domain", "193.in-addr.arpa")
    }

    def "create reverse domain, ripe space, exact match inetnum with no mnt-lower no mnt-domains, routes pw supplied"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-R") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 193.0.0.0 - 193.255.255.255", "inetnum", "193.0.0.0 - 193.255.255.255")

      expect:
        queryObjectNotFound("-rGBT domain 193.in-addr.arpa", "domain", "193.in-addr.arpa")

      when:
        def message = syncUpdate("""\
                domain:         193.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         owner-MNT
                source:         TEST

                password:   lir3
                password:   owner
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[domain] 193.in-addr.arpa" }
        ack.errorMessagesFor("Create", "[domain] 193.in-addr.arpa") ==
                ["Authorisation for [inetnum] 193.0.0.0 - 193.255.255.255 failed using \"mnt-by:\" not authenticated by: RIPE-NCC-HM-MNT"]

        queryObjectNotFound("-rGBT domain 193.in-addr.arpa", "domain", "193.in-addr.arpa")
    }

    def "create reverse domain, ripe space, less specific inetnum with mnt-domains, domains pw supplied"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-LOW-DOM-R") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 193.0.0.0 - 193.255.255.255", "inetnum", "193.0.0.0 - 193.255.255.255")

      expect:
        queryObjectNotFound("-rGBT domain 0.0.193.in-addr.arpa", "domain", "0.0.193.in-addr.arpa")

      when:
        def message = syncUpdate("""\
                domain:         0.0.193.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         owner-MNT
                source:         TEST

                password:   lir2
                password:   owner
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[domain] 0.0.193.in-addr.arpa" }

        queryObject("-rGBT domain 0.0.193.in-addr.arpa", "domain", "0.0.193.in-addr.arpa")
    }

    def "create reverse domain, ripe space, less specific inetnum with mnt-domains, routes pw supplied"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-LOW-DOM-R") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 193.0.0.0 - 193.255.255.255", "inetnum", "193.0.0.0 - 193.255.255.255")

      expect:
        queryObjectNotFound("-rGBT domain 0.0.193.in-addr.arpa", "domain", "0.0.193.in-addr.arpa")

      when:
        def message = syncUpdate("""\
                domain:         0.0.193.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         owner-MNT
                source:         TEST

                password:   lir3
                password:   owner
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[domain] 0.0.193.in-addr.arpa" }
        ack.errorMessagesFor("Create", "[domain] 0.0.193.in-addr.arpa") ==
                ["Authorisation for [inetnum] 193.0.0.0 - 193.255.255.255 failed using \"mnt-domains:\" not authenticated by: LIR2-MNT"]

        queryObjectNotFound("-rGBT domain 0.0.193.in-addr.arpa", "domain", "0.0.193.in-addr.arpa")
    }

    def "create reverse domain, ripe space, less specific inetnum with mnt-domains, -by pw supplied"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-LOW-DOM-R") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 193.0.0.0 - 193.255.255.255", "inetnum", "193.0.0.0 - 193.255.255.255")

      expect:
        queryObjectNotFound("-rGBT domain 0.0.193.in-addr.arpa", "domain", "0.0.193.in-addr.arpa")

      when:
        def message = syncUpdate("""\
                domain:         0.0.193.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         owner-MNT
                source:         TEST

                password:   hm
                password:   owner
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[domain] 0.0.193.in-addr.arpa" }
        ack.errorMessagesFor("Create", "[domain] 0.0.193.in-addr.arpa") ==
                ["Authorisation for [inetnum] 193.0.0.0 - 193.255.255.255 failed using \"mnt-domains:\" not authenticated by: LIR2-MNT"]

        queryObjectNotFound("-rGBT domain 0.0.193.in-addr.arpa", "domain", "0.0.193.in-addr.arpa")
    }

    def "create reverse domain, ripe space, less specific inetnum with mnt-lower no mnt-domains, lower pw supplied"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-LOW-R") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 193.0.0.0 - 193.255.255.255", "inetnum", "193.0.0.0 - 193.255.255.255")

      expect:
        queryObjectNotFound("-rGBT domain 0.0.193.in-addr.arpa", "domain", "0.0.193.in-addr.arpa")

      when:
        def message = syncUpdate("""\
                domain:         0.0.193.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         owner-MNT
                source:         TEST

                password:   lir
                password:   owner
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[domain] 0.0.193.in-addr.arpa" }

        queryObject("-rGBT domain 0.0.193.in-addr.arpa", "domain", "0.0.193.in-addr.arpa")
    }

    def "create reverse domain, ripe space, less specific inetnum with mnt-lower no mnt-domains, routes pw supplied"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-LOW-R") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 193.0.0.0 - 193.255.255.255", "inetnum", "193.0.0.0 - 193.255.255.255")

      expect:
        queryObjectNotFound("-rGBT domain 0.0.193.in-addr.arpa", "domain", "0.0.193.in-addr.arpa")

      when:
        def message = syncUpdate("""\
                domain:         0.0.193.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         OWNER-MNT
                source:         TEST

                password:   lir3
                password:   owner
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[domain] 0.0.193.in-addr.arpa" }
        ack.errorMessagesFor("Create", "[domain] 0.0.193.in-addr.arpa") ==
                ["Authorisation for [inetnum] 193.0.0.0 - 193.255.255.255 failed using \"mnt-lower:\" not authenticated by: LIR-MNT"]

        queryObjectNotFound("-rGBT domain 0.0.193.in-addr.arpa", "domain", "0.0.193.in-addr.arpa")
    }

    def "create reverse domain, ripe space, less specific inetnum with mnt-lower no mnt-domains, -by pw supplied"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-LOW-R") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 193.0.0.0 - 193.255.255.255", "inetnum", "193.0.0.0 - 193.255.255.255")

      expect:
        queryObjectNotFound("-rGBT domain 0.0.193.in-addr.arpa", "domain", "0.0.193.in-addr.arpa")

      when:
        def message = syncUpdate("""\
                domain:         0.0.193.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         owner-MNT
                source:         TEST

                password:   hm
                password:   owner
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[domain] 0.0.193.in-addr.arpa" }
        ack.errorMessagesFor("Create", "[domain] 0.0.193.in-addr.arpa") ==
                ["Authorisation for [inetnum] 193.0.0.0 - 193.255.255.255 failed using \"mnt-lower:\" not authenticated by: LIR-MNT"]

        queryObjectNotFound("-rGBT domain 0.0.193.in-addr.arpa", "domain", "0.0.193.in-addr.arpa")
    }

    def "create reverse domain, ripe space, less specific inetnum with no mnt-lower no mnt-domains, -by pw supplied"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-R") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 193.0.0.0 - 193.255.255.255", "inetnum", "193.0.0.0 - 193.255.255.255")

      expect:
        queryObjectNotFound("-rGBT domain 0.0.193.in-addr.arpa", "domain", "0.0.193.in-addr.arpa")

      when:
        def message = syncUpdate("""\
                domain:         0.0.193.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         owner-MNT
                source:         TEST

                password:   hm
                password:   owner
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[domain] 0.0.193.in-addr.arpa" }

        queryObject("-rGBT domain 0.0.193.in-addr.arpa", "domain", "0.0.193.in-addr.arpa")
    }

    def "create reverse domain, ripe space, less specific inetnum with no mnt-lower no mnt-domains, routes pw supplied"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-R") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 193.0.0.0 - 193.255.255.255", "inetnum", "193.0.0.0 - 193.255.255.255")

      expect:
        queryObjectNotFound("-rGBT domain 0.0.193.in-addr.arpa", "domain", "0.0.193.in-addr.arpa")

      when:
        def message = syncUpdate("""\
                domain:         0.0.193.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         owner-MNT
                source:         TEST

                password:   lir3
                password:   owner
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[domain] 0.0.193.in-addr.arpa" }
        ack.errorMessagesFor("Create", "[domain] 0.0.193.in-addr.arpa") ==
                ["Authorisation for [inetnum] 193.0.0.0 - 193.255.255.255 failed using \"mnt-by:\" not authenticated by: RIPE-NCC-HM-MNT"]

        queryObjectNotFound("-rGBT domain 0.0.193.in-addr.arpa", "domain", "0.0.193.in-addr.arpa")
    }

    def "create reverse domain, ripe space, smaller than /24, override"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-R") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 193.0.0.0 - 193.255.255.255", "inetnum", "193.0.0.0 - 193.255.255.255")

      expect:
        queryObjectNotFound("-rGBT domain 1.0.0.193.in-addr.arpa", "domain", "1.0.0.193.in-addr.arpa")

      when:
        def message = syncUpdate("""\
                domain:         1.0.0.193.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         owner-MNT
                source:         TEST
                override:  denis,override1

                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[domain] 1.0.0.193.in-addr.arpa" }
        ack.infoSuccessMessagesFor("Create", "[domain] 1.0.0.193.in-addr.arpa") == [
              "Authorisation override used"]

        queryObject("-rGBT domain 1.0.0.193.in-addr.arpa", "domain", "1.0.0.193.in-addr.arpa")
    }

    def "create reverse domain, ripe space, range smaller than /24, override"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-R") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 193.0.0.0 - 193.255.255.255", "inetnum", "193.0.0.0 - 193.255.255.255")

      expect:
        queryObjectNotFound("-rGBT domain 1-2.0.0.193.in-addr.arpa", "domain", "1-2.0.0.193.in-addr.arpa")

      when:
        def message = syncUpdate("""\
                domain:         1-2.0.0.193.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         owner-MNT
                source:         TEST
                override:   denis,override1

                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[domain] 1-2.0.0.193.in-addr.arpa" }
        ack.infoSuccessMessagesFor("Create", "[domain] 1-2.0.0.193.in-addr.arpa") == [
                "Authorisation override used"]

        queryObject("-rGBT domain 1-2.0.0.193.in-addr.arpa", "domain", "1-2.0.0.193.in-addr.arpa")
    }

    def "create reverse domain, ripe space, reversed range smaller than /24, override"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-R") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 193.0.0.0 - 193.255.255.255", "inetnum", "193.0.0.0 - 193.255.255.255")

      expect:
        queryObjectNotFound("-rGBT domain 2-1.0.0.193.in-addr.arpa", "domain", "2-1.0.0.193.in-addr.arpa")

      when:
        def message = syncUpdate("""\
                domain:         2-1.0.0.193.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         owner-MNT
                source:         TEST
                override:   denis,override1

                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[domain] 2-1.0.0.193.in-addr.arpa" }
        ack.errorMessagesFor("Create", "[domain] 2-1.0.0.193.in-addr.arpa") ==
                ["Syntax error in 2-1.0.0.193.in-addr.arpa"]

        queryObjectNotFound("-rGBT domain 2-1.0.0.193.in-addr.arpa", "domain", "2-1.0.0.193.in-addr.arpa")
    }

    def "create reverse domain, ripe space, negative range smaller than /24, override"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-R") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 193.0.0.0 - 193.255.255.255", "inetnum", "193.0.0.0 - 193.255.255.255")

      expect:
        queryObjectNotFound("-rGBT domain -1-2.0.0.193.in-addr.arpa", "domain", "-1-2.0.0.193.in-addr.arpa")

      when:
        def message = syncUpdate("""\
                domain:         -1-2.0.0.193.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         owner-MNT
                source:         TEST
                override:   denis,override1

                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[domain] -1-2.0.0.193.in-addr.arpa" }
        ack.errorMessagesFor("Create", "[domain] -1-2.0.0.193.in-addr.arpa") ==
                ["Syntax error in -1-2.0.0.193.in-addr.arpa"]

        queryObjectNotFound("-rGBT domain -1-2.0.0.193.in-addr.arpa", "domain", "-1-2.0.0.193.in-addr.arpa")
    }

    def "create reverse domain, ripe space, 0-255 range, smaller than /24, override"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-R") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 193.0.0.0 - 193.255.255.255", "inetnum", "193.0.0.0 - 193.255.255.255")

      expect:
        queryObjectNotFound("-rGBT domain 0-255.0.0.193.in-addr.arpa", "domain", "0-255.0.0.193.in-addr.arpa")

      when:
        def message = syncUpdate("""\
                domain:         0-255.0.0.193.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         owner-MNT
                source:         TEST
                override:   denis,override1

                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[domain] 0-255.0.0.193.in-addr.arpa" }
        ack.errorMessagesFor("Create", "[domain] 0-255.0.0.193.in-addr.arpa") ==
                ["Syntax error in 0-255.0.0.193.in-addr.arpa"]

        queryObjectNotFound("-rGBT domain 0-255.0.0.193.in-addr.arpa", "domain", "0-255.0.0.193.in-addr.arpa")
    }

    def "create reverse domain, ripe space, 1-256 range, smaller than /24, override"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-R") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 193.0.0.0 - 193.255.255.255", "inetnum", "193.0.0.0 - 193.255.255.255")

      expect:
        queryObjectNotFound("-rGBT domain 1-256.0.0.193.in-addr.arpa", "domain", "1-256.0.0.193.in-addr.arpa")

      when:
        def message = syncUpdate("""\
                domain:         1-256.0.0.193.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         owner-MNT
                source:         TEST
                override:   denis,override1

                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[domain] 1-256.0.0.193.in-addr.arpa" }
        ack.errorMessagesFor("Create", "[domain] 1-256.0.0.193.in-addr.arpa") ==
                ["Syntax error in 1-256.0.0.193.in-addr.arpa"]

        queryObjectNotFound("-rGBT domain 1-256.0.0.193.in-addr.arpa", "domain", "1-256.0.0.193.in-addr.arpa")
    }

    def "create reverse domain, ripe space, 01-03 range, smaller than /24, override"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-R") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 193.0.0.0 - 193.255.255.255", "inetnum", "193.0.0.0 - 193.255.255.255")

      expect:
        queryObjectNotFound("-rGBT domain 01-03.0.0.193.in-addr.arpa", "domain", "01-03.0.0.193.in-addr.arpa")

      when:
        def message = syncUpdate("""\
                domain:         01-03.0.0.193.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         owner-MNT
                source:         TEST
                override:   denis,override1

                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[domain] 01-03.0.0.193.in-addr.arpa" }
        ack.errorMessagesFor("Create", "[domain] 01-03.0.0.193.in-addr.arpa") ==
                ["Syntax error in 01-03.0.0.193.in-addr.arpa"]

        queryObjectNotFound("-rGBT domain 01-03.0.0.193.in-addr.arpa", "domain", "01-03.0.0.193.in-addr.arpa")
    }

    def "create reverse domain, ripe space, range equals /32, override"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-LOW") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 193.0.0.0 - 193.255.255.255", "inetnum", "193.0.0.0 - 193.255.255.255")

      expect:
        queryObjectNotFound("-rGBT domain 2-2.0.0.193.in-addr.arpa", "domain", "2-2.0.0.193.in-addr.arpa")

      when:
        def message = syncUpdate("""\
                domain:         2-2.0.0.193.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         owner-MNT
                source:         TEST
                override:   denis,override1

                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[domain] 2-2.0.0.193.in-addr.arpa" }
        ack.infoSuccessMessagesFor("Create", "[domain] 2-2.0.0.193.in-addr.arpa") == [
              "Authorisation override used"]

        queryObject("-rGBT domain 2-2.0.0.193.in-addr.arpa", "domain", "2-2.0.0.193.in-addr.arpa")
    }

    def "create reverse domain, ripe space, ip6.arpa suffix"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-LOW") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 193.0.0.0 - 193.255.255.255", "inetnum", "193.0.0.0 - 193.255.255.255")

      expect:
        queryObjectNotFound("-rGBT domain 0.0.193.ip6.arpa", "domain", "0.0.193.ip6.arpa")

      when:
        def message = syncUpdate("""\
                domain:         0.0.193.ip6.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         owner-MNT
                source:         TEST
                override:   denis,override1

                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[domain] 0.0.193.ip6.arpa" }
        ack.errorMessagesFor("Create", "[domain] 0.0.193.ip6.arpa") ==
                ["Syntax error in 0.0.193.ip6.arpa"]

        queryObjectNotFound("-rGBT domain 0.0.193.ip6.arpa", "domain", "0.0.193.ip6.arpa")
    }

    def "create reverse domain, ripe space, trailing dot"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-LOW") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 193.0.0.0 - 193.255.255.255", "inetnum", "193.0.0.0 - 193.255.255.255")

      expect:
        queryObjectNotFound("-rGBT domain 193.in-addr.arpa", "domain", "193.in-addr.arpa")

      when:
        def message = syncUpdate("""\
                domain:         193.in-addr.arpa.
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         LIR-MNT
                source:         TEST

                password:   lir
                password:   hm
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[domain] 193.in-addr.arpa" }
        ack.infoSuccessMessagesFor("Create", "[domain] 193.in-addr.arpa") == [
                "Value 193.in-addr.arpa. converted to 193.in-addr.arpa"]

        queryObject("-rGBT domain 193.in-addr.arpa", "domain", "193.in-addr.arpa")
    }

    def "create reverse domain, ripe space, leading dot"() {
      expect:
        queryObjectNotFound("-rGBT domain .193.in-addr.arpa", "domain", ".193.in-addr.arpa")

      when:
        dnsStubbedResponse(".193.in-addr.arpa",
                "Host name .193.in-addr.arpa is illegal (syntax error at ) The hostname is not syntactially correct according to RFC 952.  A common error is to begin the hostname with a non-letter (a-z) or use invalid characters (only a-z, 0-9 and - are allowed).",
                ".193.in-addr.arpa is not a valid name for a zone.")

        def message = syncUpdate("""\
                domain:         .193.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         LIR-MNT
                source:         TEST

                password:   lir
                password:   hm
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[domain] .193.in-addr.arpa" }

        queryObjectNotFound("-rGBT domain .193.in-addr.arpa", "domain", ".193.in-addr.arpa")
    }

    def "create reverse domain, non ripe space"() {
      expect:
        queryObjectNotFound("-rGBT domain 1.99.in-addr.arpa", "domain", "1.99.in-addr.arpa")

      when:
        dnsStubbedResponse("1.99.in-addr.arpa",
                "No name servers found at child. No name servers could be found at the child. This usually means that the child is not configured to answer queries about the zone.",
                "Fatal error in delegation for zone 1.99.in-addr.arpa. No name servers found at child or at parent. No further testing can be performed.")

        def message = syncUpdate("""\
                domain:         1.99.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         LIR-MNT
                source:         TEST

                password:   lir
                password:   hm
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(2, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[domain] 1.99.in-addr.arpa" }
        ack.errorMessagesFor("Create", "[domain] 1.99.in-addr.arpa") == [
                "No name servers found at child. No name servers could be found at the child. This usually means that the child is not configured to answer queries about the zone.",
                "Fatal error in delegation for zone 1.99.in-addr.arpa. No name servers found at child or at parent. No further testing can be performed."
        ]

        queryObjectNotFound("-rGBT domain 1.99.in-addr.arpa", "domain", "1.99.in-addr.arpa")
    }

    def "create reverse domain, dash in 4th octet"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-LOW") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 193.0.0.0 - 193.255.255.255", "inetnum", "193.0.0.0 - 193.255.255.255")

      expect:
        queryObjectNotFound("-rGBT domain 192-255.148.201.193.in-addr.arpa", "domain", "192-255.148.201.193.in-addr.arpa")

      when:
        def message = syncUpdate("""\
                domain:         192-255.148.201.193.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        ns1.nl-ix.net
                nserver:        ns2.nl-ix.net
                mnt-by:         LIR-MNT
                source:         TEST

                password:   lir
                password:   hm
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[domain] 192-255.148.201.193.in-addr.arpa" }

        queryObject("-rGBT domain 192-255.148.201.193.in-addr.arpa", "domain", "192-255.148.201.193.in-addr.arpa")
    }

    def "create reverse domain, dash in 4th octet, trailing dot"() {
      given:

      expect:
        queryObjectNotFound("-rGBT domain 192-255.148.201.193.in-addr.arpa", "domain", "192-255.148.201.193.in-addr.arpa")

      when:
        def message = syncUpdate("""\
                domain:         192-255.148.201.193.in-addr.arpa.
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        ns1.nl-ix.net
                nserver:        ns2.nl-ix.net
                mnt-by:         LIR-MNT
                source:         TEST

                password:   lir
                password:   hm
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[domain] 192-255.148.201.193.in-addr.arpa" }
        ack.infoSuccessMessagesFor("Create", "[domain] 192-255.148.201.193.in-addr.arpa") == [
                "Value 192-255.148.201.193.in-addr.arpa. converted to 192-255.148.201.193.in-addr.arpa"]

        queryObject("-rGBT domain 192-255.148.201.193.in-addr.arpa", "domain", "192-255.148.201.193.in-addr.arpa")
    }

    def "create reverse domain, dash in 3rd octet"() {
      given:

      expect:
        queryObjectNotFound("-rGBT domain 2.1-3.1.193.in-addr.arpa", "domain", "2.1-3.1.193.in-addr.arpa")

      when:
        def message = syncUpdate("""\
                domain:         2.1-3.1.193.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         LIR-MNT
                source:         TEST

                password:   lir
                password:   hm
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[domain] 2.1-3.1.193.in-addr.arpa" }
//        ack.errorMessagesFor("Create", "[domain] 2.1-3.1.193.in-addr.arpa") ==
//              ["Syntax error in 2.1-3.1.193.in-addr.arpa"] TODO need an ack.errorMessagesForMatching

        queryObjectNotFound("-rGBT domain 2.1-3.1.193.in-addr.arpa", "domain", "2.1-3.1.193.in-addr.arpa")
    }

    def "create reverse domain, dash in 3rd octet, no 4th octet"() {
      given:

      expect:
        queryObjectNotFound("-rGBT domain 148-149.201.193.in-addr.arpa", "domain", "148-149.201.193.in-addr.arpa")

      when:
        def message = syncUpdate("""\
                domain:         148-149.201.193.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        ns1.nl-ix.net
                nserver:        ns2.nl-ix.net
                mnt-by:         LIR-MNT
                source:         TEST

                password:   lir
                password:   hm
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[domain] 148-149.201.193.in-addr.arpa" }
//        ack.errorMessagesFor("Create", "[domain] 148-149.201.193.in-addr.arpa") == //TODO - need an ack.errorMessagesForMatches so I can pick out the error message bit I want
//              ["Syntax error in 148-149.201.193.in-addr.arpa"]

        queryObjectNotFound("-rGBT domain 148-149.201.193.in-addr.arpa", "domain", "148-149.201.193.in-addr.arpa")
    }

    def "create reverse domain, dash in 2nd octet"() {
      given:

      expect:
        queryObjectNotFound("-rGBT domain 201-202.193.in-addr.arpa", "domain", "201-202.193.in-addr.arpa")

      when:
        def message = syncUpdate("""\
                domain:         201-202.193.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        ns1.nl-ix.net
                nserver:        ns2.nl-ix.net
                mnt-by:         LIR-MNT
                source:         TEST

                password:   lir
                password:   hm
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[domain] 201-202.193.in-addr.arpa" }
//        ack.errorMessagesFor("Create", "[domain] 201-202.193.in-addr.arpa") ==    // TODO
//              ["Syntax error in 201-202.193.in-addr.arpa"]

        queryObjectNotFound("-rGBT domain 201-202.193.in-addr.arpa", "domain", "201-202.193.in-addr.arpa")
    }

    def "create reverse domain, dash in 1st octet"() {
      given:

      expect:
        queryObjectNotFound("-rGBT domain 1-193.in-addr.arpa", "domain", "1-193.in-addr.arpa")

      when:
        def message = syncUpdate("""\
                domain:         1-193.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         LIR-MNT
                source:         TEST

                password:   lir
                password:   hm
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[domain] 1-193.in-addr.arpa" }
//        ack.errorMessagesFor("Create", "[domain] 1-193.in-addr.arpa") ==      // TODO
//              ["Syntax error in 1-193.in-addr.arpa"]

        queryObjectNotFound("-rGBT domain 1-193.in-addr.arpa", "domain", "1-193.in-addr.arpa")
    }

    def "delete reverse domain"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-LOW") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 193.0.0.0 - 193.255.255.255", "inetnum", "193.0.0.0 - 193.255.255.255")
        syncUpdate(getTransient("ALLOC-DOMAIN") + "override: denis,override1")
        queryObject("-r -T domain 193.in-addr.arpa", "domain", "193.in-addr.arpa")

      expect:
        queryObject("-rGBT domain 193.in-addr.arpa", "domain", "193.in-addr.arpa")

      when:
        def message = syncUpdate("""\
                domain:         193.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         DOMAIN-MNT
                source:         TEST
                delete:  testing delete

                password:   mb-dom
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Delete" && it.key == "[domain] 193.in-addr.arpa" }

        queryObjectNotFound("-rGBT domain 193.in-addr.arpa", "domain", "193.in-addr.arpa")
    }

    def "delete reverse domain, override"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-LOW") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 193.0.0.0 - 193.255.255.255", "inetnum", "193.0.0.0 - 193.255.255.255")
        syncUpdate(getTransient("ALLOC-DOMAIN") + "override: denis,override1")
        queryObject("-r -T domain 193.in-addr.arpa", "domain", "193.in-addr.arpa")

      expect:
        queryObject("-rGBT domain 193.in-addr.arpa", "domain", "193.in-addr.arpa")

      when:
        def message = syncUpdate("""\
                domain:         193.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         DOMAIN-MNT
                source:         TEST
                delete:  testing delete
                override:   denis,override1

                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Delete" && it.key == "[domain] 193.in-addr.arpa" }
        ack.infoSuccessMessagesFor("Delete", "[domain] 193.in-addr.arpa") == [
                "Authorisation override used"]

        queryObjectNotFound("-rGBT domain 193.in-addr.arpa", "domain", "193.in-addr.arpa")
    }

    def "delete reverse domain, using parent mnt-domains"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-LOW-DOM") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 193.0.0.0 - 193.255.255.255", "inetnum", "193.0.0.0 - 193.255.255.255")
        syncUpdate(getTransient("ASSIGN-DOMAIN") + "override: denis,override1")
        queryObject("-r -T domain 0.0.193.in-addr.arpa", "domain", "0.0.193.in-addr.arpa")

      expect:

      when:
        def message = syncUpdate("""\
                domain:         0.0.193.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         DOMAIN-MNT
                source:         TEST
                delete:  testing delete

                password:   lir2
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 0, 1)
        ack.countErrorWarnInfo(3, 0, 0)
        ack.errors.any { it.operation == "Delete" && it.key == "[domain] 0.0.193.in-addr.arpa" }
        ack.errorMessagesFor("Delete", "[domain] 0.0.193.in-addr.arpa") ==
                ["Authorisation for [domain] 0.0.193.in-addr.arpa failed using \"mnt-by:\" not authenticated by: DOMAIN-MNT",
                        "Authorisation for [inetnum] 193.0.0.0 - 193.255.255.255 failed using \"mnt-lower:\" not authenticated by: LIR-MNT",
                        "Authorisation for [inetnum] 193.0.0.0 - 193.255.255.255 failed using \"mnt-by:\" not authenticated by: RIPE-NCC-HM-MNT"
                ]

        queryObject("-rGBT domain 0.0.193.in-addr.arpa", "domain", "0.0.193.in-addr.arpa")
    }

    def "delete reverse domain, using parent mnt-routes"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-LOW-DOM-R") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 193.0.0.0 - 193.255.255.255", "inetnum", "193.0.0.0 - 193.255.255.255")
        syncUpdate(getTransient("ASSIGN-DOMAIN") + "override: denis,override1")
        queryObject("-r -T domain 0.0.193.in-addr.arpa", "domain", "0.0.193.in-addr.arpa")

      expect:

      when:
        def message = syncUpdate("""\
                domain:         0.0.193.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         DOMAIN-MNT
                source:         TEST
                delete:  testing delete

                password:   lir3
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 0, 1)
        ack.countErrorWarnInfo(3, 0, 0)
        ack.errors.any { it.operation == "Delete" && it.key == "[domain] 0.0.193.in-addr.arpa" }
        ack.errorMessagesFor("Delete", "[domain] 0.0.193.in-addr.arpa") ==
                ["Authorisation for [domain] 0.0.193.in-addr.arpa failed using \"mnt-by:\" not authenticated by: DOMAIN-MNT",
                        "Authorisation for [inetnum] 193.0.0.0 - 193.255.255.255 failed using \"mnt-lower:\" not authenticated by: LIR-MNT",
                        "Authorisation for [inetnum] 193.0.0.0 - 193.255.255.255 failed using \"mnt-by:\" not authenticated by: RIPE-NCC-HM-MNT"
                ]

        queryObject("-rGBT domain 0.0.193.in-addr.arpa", "domain", "0.0.193.in-addr.arpa")
    }

    def "delete reverse domain, using parent mnt-lower"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-LOW-DOM") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 193.0.0.0 - 193.255.255.255", "inetnum", "193.0.0.0 - 193.255.255.255")
        syncUpdate(getTransient("ASSIGN-DOMAIN") + "override: denis,override1")
        queryObject("-r -T domain 0.0.193.in-addr.arpa", "domain", "0.0.193.in-addr.arpa")

      expect:

      when:
        def message = syncUpdate("""\
                domain:         0.0.193.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         DOMAIN-MNT
                source:         TEST
                delete:  testing delete

                password:   lir
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Delete" && it.key == "[domain] 0.0.193.in-addr.arpa" }

        queryObjectNotFound("-rGBT domain 0.0.193.in-addr.arpa", "domain", "0.0.193.in-addr.arpa")
    }

    def "delete reverse domain, using parent mnt-by"() {
        given:
        syncUpdate(getTransient("ALLOC-PA-LOW-DOM") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 193.0.0.0 - 193.255.255.255", "inetnum", "193.0.0.0 - 193.255.255.255")
        syncUpdate(getTransient("ASSIGN-DOMAIN") + "override: denis,override1")
        queryObject("-r -T domain 0.0.193.in-addr.arpa", "domain", "0.0.193.in-addr.arpa")

        expect:

        when:
        def message = syncUpdate("""\
                domain:         0.0.193.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         DOMAIN-MNT
                source:         TEST
                delete:  testing delete

                password:   hm
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Delete" && it.key == "[domain] 0.0.193.in-addr.arpa" }

        queryObjectNotFound("-rGBT domain 0.0.193.in-addr.arpa", "domain", "0.0.193.in-addr.arpa")
    }

    def "modify reverse domain, add remarks using mnt-by"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-LOW-DOM") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 193.0.0.0 - 193.255.255.255", "inetnum", "193.0.0.0 - 193.255.255.255")
        syncUpdate(getTransient("ASSIGN-DOMAIN") + "override: denis,override1")
        queryObject("-r -T domain 0.0.193.in-addr.arpa", "domain", "0.0.193.in-addr.arpa")

      expect:

      when:
        def message = syncUpdate("""\
                domain:         0.0.193.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         DOMAIN-MNT
                remarks:        just added
                source:         TEST

                password:   mb-dom
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[domain] 0.0.193.in-addr.arpa" }

        query_object_matches("-rGBT domain 0.0.193.in-addr.arpa", "domain", "0.0.193.in-addr.arpa", "just added")
    }

    def "modify reverse domain, add remarks using parent mnt-lower"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-LOW-DOM") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 193.0.0.0 - 193.255.255.255", "inetnum", "193.0.0.0 - 193.255.255.255")
        syncUpdate(getTransient("ASSIGN-DOMAIN") + "override: denis,override1")
        queryObject("-r -T domain 0.0.193.in-addr.arpa", "domain", "0.0.193.in-addr.arpa")

      expect:

      when:
        def message = syncUpdate("""\
                domain:         0.0.193.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         DOMAIN-MNT
                remarks:        just added
                source:         TEST

                password:   lir
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)
        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[domain] 0.0.193.in-addr.arpa" }
        ack.errorMessagesFor("Modify", "[domain] 0.0.193.in-addr.arpa") ==
                ["Authorisation for [domain] 0.0.193.in-addr.arpa failed using \"mnt-by:\" not authenticated by: DOMAIN-MNT"]

        query_object_not_matches("-rGBT domain 0.0.193.in-addr.arpa", "domain", "0.0.193.in-addr.arpa", "just added")
    }

    def "create child reverse domain, parent exists"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-LOW") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 193.0.0.0 - 193.255.255.255", "inetnum", "193.0.0.0 - 193.255.255.255")
        syncUpdate(getTransient("ALLOC-DOMAIN") + "override: denis,override1")
        queryObject("-r -T domain 193.in-addr.arpa", "domain", "193.in-addr.arpa")

      expect:
        queryObjectNotFound("-r -T domain 0.0.193.in-addr.arpa", "domain", "0.0.193.in-addr.arpa")

      when:
        def message = syncUpdate("""\
                domain:         0.0.193.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         DOMAIN-MNT
                remarks:        just added
                source:         TEST

                password:   mb-dom
                password:   lir
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[domain] 0.0.193.in-addr.arpa" }
        ack.errorMessagesFor("Create", "[domain] 0.0.193.in-addr.arpa") ==
                ["Existing less specific domain object found 193.0.0.0/8"]

        queryObjectNotFound("-rGBT domain 0.0.193.in-addr.arpa", "domain", "0.0.193.in-addr.arpa")
    }

    def "create parent reverse domain, child exists"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-LOW") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 193.0.0.0 - 193.255.255.255", "inetnum", "193.0.0.0 - 193.255.255.255")
        syncUpdate(getTransient("ASSIGN-DOMAIN") + "override: denis,override1")
        queryObject("-r -T domain 0.0.193.in-addr.arpa", "domain", "0.0.193.in-addr.arpa")

      expect:
        queryObjectNotFound("-r -T domain 193.in-addr.arpa", "domain", "193.in-addr.arpa")

      when:
        def message = syncUpdate("""\
                domain:         193.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         DOMAIN-MNT
                remarks:        just added
                source:         TEST

                password:   mb-dom
                password:   hm
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[domain] 193.in-addr.arpa" }
        ack.errorMessagesFor("Create", "[domain] 193.in-addr.arpa") ==
                ["Existing more specific domain object found 193.0.0.0/24"]

        queryObjectNotFound("-rGBT domain 193.in-addr.arpa", "domain", "193.in-addr.arpa")
    }

    def "create reverse domain, upper case"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-LOW-DOM-R") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 193.0.0.0 - 193.255.255.255", "inetnum", "193.0.0.0 - 193.255.255.255")

      expect:
        queryObjectNotFound("-rGBT domain 193.in-addr.arpa", "domain", "193.in-addr.arpa")

      when:
        def message = syncUpdate("""\
                domain:         193.IN-ADDR.ARpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         owner-MNT
                source:         TEST

                password:   lir2
                password:   owner
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[domain] 193.IN-ADDR.ARpa" }

        queryObject("-rGBT domain 193.in-addr.arpa", "domain", "193.IN-ADDR.ARpa")
    }

    def "create reverse domain, upper case attr"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-LOW-DOM-R") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 193.0.0.0 - 193.255.255.255", "inetnum", "193.0.0.0 - 193.255.255.255")

      expect:
        queryObjectNotFound("-rGBT domain 193.in-addr.arpa", "domain", "193.in-addr.arpa")

      when:
        def message = syncUpdate("""\
                Domain:         193.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         owner-MNT
                source:         TEST

                password:   lir2
                password:   owner
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[domain] 193.in-addr.arpa" }

        queryObject("-rGBT domain 193.in-addr.arpa", "domain", "193.in-addr.arpa")
    }

    def "create reverse domain, trailing dot on nserver"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-LOW-DOM-R") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 193.0.0.0 - 193.255.255.255", "inetnum", "193.0.0.0 - 193.255.255.255")

      expect:
        queryObjectNotFound("-rGBT domain 193.in-addr.arpa", "domain", "193.in-addr.arpa")

      when:
        def message = syncUpdate("""\
                domain:         193.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net.
                nserver:        ns3.nic.fr.
                mnt-by:         owner-MNT
                source:         TEST

                password:   lir2
                password:   owner
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 2)
        ack.successes.any { it.operation == "Create" && it.key == "[domain] 193.in-addr.arpa" }
        ack.infoSuccessMessagesFor("Create", "[domain] 193.in-addr.arpa") == [
                "Value pri.authdns.ripe.net. converted to pri.authdns.ripe.net",
                "Value ns3.nic.fr. converted to ns3.nic.fr"
        ]

        query_object_not_matches("-rGBT domain 193.in-addr.arpa", "domain", "193.in-addr.arpa", "pri.authdns.ripe.net\\.")
        query_object_not_matches("-rGBT domain 193.in-addr.arpa", "domain", "193.in-addr.arpa", "ns3.nic.fr\\.")
    }

    def "modify reverse domain, change nserver"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-LOW-DOM-R") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 193.0.0.0 - 193.255.255.255", "inetnum", "193.0.0.0 - 193.255.255.255")
        syncUpdate(getTransient("ALLOC-DOMAIN") + "override: denis,override1")
        queryObject("-r -T domain 193.in-addr.arpa", "domain", "193.in-addr.arpa")

      expect:

      when:
        def message = syncUpdate("""\
                domain:         193.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        tinnie.arin.net
                mnt-by:         DOMAIN-MNT
                source:         TEST

                password:   mb-dom
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[domain] 193.in-addr.arpa" }

        query_object_matches("-rGBT domain 193.in-addr.arpa", "domain", "193.in-addr.arpa", "tinnie.arin.net")
        query_object_not_matches("-rGBT domain 193.in-addr.arpa", "domain", "193.in-addr.arpa", "ns3.nic.fr")
    }

    def "create reverse domain, only 1 nserver"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-LOW-DOM-R") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 193.0.0.0 - 193.255.255.255", "inetnum", "193.0.0.0 - 193.255.255.255")

      expect:
        queryObjectNotFound("-rGBT domain 193.in-addr.arpa", "domain", "193.in-addr.arpa")

      when:
        dnsStubbedResponse("193.in-addr.arpa",
                "Too few name servers (1). Only one name server was found for the zone. You should always have at least two name servers for a zone to be able to handle transient connectivity problems.",
                "Too few IPv4 name servers (1). Only one IPv4 name server was found for the zone. You should always have at least two IPv4 name servers for a zone to be able to handle transient connectivity problems.")

        def message = syncUpdate("""\
                domain:         193.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                mnt-by:         owner-MNT
                source:         TEST

                password:   lir2
                password:   owner
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(2, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[domain] 193.in-addr.arpa" }
        ack.errorMessagesFor("Create", "[domain] 193.in-addr.arpa") == [
                "Too few name servers (1). Only one name server was found for the zone. You should always have at least two name servers for a zone to be able to handle transient connectivity problems.",
                "Too few IPv4 name servers (1). Only one IPv4 name server was found for the zone. You should always have at least two IPv4 name servers for a zone to be able to handle transient connectivity problems."
        ]

        queryObjectNotFound("-rGBT domain 193.in-addr.arpa", "domain", "193.in-addr.arpa")
    }

    def "create reverse domain, ripe IPv6 space, less specific inet6num with mnt-domains, domains pw supplied"() {
      given:
        syncUpdate(getTransient("ALLOC6-PA-LOW-DOM-R") + "password: hm\npassword: owner3")
        queryObject("-r -T inet6num 2001::/16", "inet6num", "2001::/16")

      expect:
        queryObjectNotFound("-rGBT domain 0.e.0.0.c.7.6.0.1.0.0.2.ip6.arpa", "domain", "0.e.0.0.c.7.6.0.1.0.0.2.ip6.arpa")

      when:
        def message = syncUpdate("""\
                domain:         0.e.0.0.c.7.6.0.1.0.0.2.ip6.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         owner-MNT
                source:         TEST

                password:   lir2
                password:   owner
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[domain] 0.e.0.0.c.7.6.0.1.0.0.2.ip6.arpa" }

        queryObject("-rGBT domain 0.e.0.0.c.7.6.0.1.0.0.2.ip6.arpa", "domain", "0.e.0.0.c.7.6.0.1.0.0.2.ip6.arpa")
    }

    def "create reverse domain, ripe IPv6 space, exact match inet6num with mnt-lower no mnt-domains, lower pw supplied"() {
        given:
        syncUpdate(getTransient("ALLOC6-PA-NO-DOM-R") + "password: hm\npassword: owner3")
        queryObject("-r -T inet6num 2a03:3460::/32", "inet6num", "2a03:3460::/32")

        expect:
        queryObjectNotFound("-rGBT domain 0.6.4.3.3.0.a.2.ip6.arpa", "domain", "0.6.4.3.3.0.a.2.ip6.arpa")

        when:
        def message = syncUpdate("""\
                domain:         0.6.4.3.3.0.a.2.ip6.arpa
                descr:          Reverse delegation for 2a03:3460::
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         owner-MNT
                source:         TEST

                password:   owner
                password:   lir
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[domain] 0.6.4.3.3.0.a.2.ip6.arpa" }
        ack.errorMessagesFor("Create", "[domain] 0.6.4.3.3.0.a.2.ip6.arpa") ==
                ["Authorisation for [inet6num] 2a03:3460::/32 failed using \"mnt-by:\" not authenticated by: RIPE-NCC-HM-MNT"]

        queryObjectNotFound("-rGBT domain 0.6.4.3.3.0.a.2.ip6.arpa", "domain", "0.6.4.3.3.0.a.2.ip6.arpa")
    }

    def "create reverse domain, ripe IPv6 space, in-addra.arpa suffix"() {
      given:
        syncUpdate(getTransient("ALLOC6-PA-LOW-DOM-R") + "password: hm\npassword: owner3")
        queryObject("-r -T inet6num 2001::/16", "inet6num", "2001::/16")

      expect:
        queryObjectNotFound("-rGBT domain 0.e.0.0.c.7.6.0.1.0.0.2.ip6.arpa", "domain", "0.e.0.0.c.7.6.0.1.0.0.2.ip6.arpa")

      when:
        def message = syncUpdate("""\
                domain:         0.e.0.0.c.7.6.0.1.0.0.2.in-addra.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         owner-MNT
                source:         TEST
                override: denis,override1

                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[domain] 0.e.0.0.c.7.6.0.1.0.0.2.in-addra.arpa" }
        ack.errorMessagesFor("Create", "[domain] 0.e.0.0.c.7.6.0.1.0.0.2.in-addra.arpa") ==
                ["Syntax error in 0.e.0.0.c.7.6.0.1.0.0.2.in-addra.arpa"]

        queryObjectNotFound("-rGBT domain 0.e.0.0.c.7.6.0.1.0.0.2.in-addra.arpa", "domain", "0.e.0.0.c.7.6.0.1.0.0.2.in-addra.arpa")
    }

    def "modify reverse domain, ripe IPv6 space, less specific inet6num with mnt-domains, domains pw supplied"() {
      given:
        syncUpdate(getTransient("ALLOC6-PA-LOW-DOM-R") + "password: hm\npassword: owner3")
        queryObject("-r -T inet6num 2001::/16", "inet6num", "2001::/16")
        syncUpdate(getTransient("ASSIGN6-DOMAIN") + "override: denis,override1")
        queryObject("-r -T domain 0.e.0.0.c.7.6.0.1.0.0.2.ip6.arpa", "domain", "0.e.0.0.c.7.6.0.1.0.0.2.ip6.arpa")

      expect:

      when:
        def message = syncUpdate("""\
                domain:         0.e.0.0.c.7.6.0.1.0.0.2.ip6.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                remarks:        just added
                mnt-by:         owner-MNT
                source:         TEST

                password:   owner
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[domain] 0.e.0.0.c.7.6.0.1.0.0.2.ip6.arpa" }

        query_object_matches("-rGBT domain 0.e.0.0.c.7.6.0.1.0.0.2.ip6.arpa", "domain", "0.e.0.0.c.7.6.0.1.0.0.2.ip6.arpa", "just added")
    }

    def "delete reverse domain, ripe IPv6 space, less specific inet6num with mnt-domains, domains pw supplied"() {
      given:
        syncUpdate(getTransient("ALLOC6-PA-LOW-DOM-R") + "password: hm\npassword: owner3")
        queryObject("-r -T inet6num 2001::/16", "inet6num", "2001::/16")
        syncUpdate(getTransient("ASSIGN6-DOMAIN") + "override: denis,override1")
        queryObject("-r -T domain 0.e.0.0.c.7.6.0.1.0.0.2.ip6.arpa", "domain", "0.e.0.0.c.7.6.0.1.0.0.2.ip6.arpa")

      expect:

      when:
        def message = syncUpdate("""\
                domain:         0.e.0.0.c.7.6.0.1.0.0.2.ip6.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         owner-MNT
                source:         TEST
                delete:  testing

                password:   owner
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Delete" && it.key == "[domain] 0.e.0.0.c.7.6.0.1.0.0.2.ip6.arpa" }

        queryObjectNotFound("-rGBT domain 0.e.0.0.c.7.6.0.1.0.0.2.ip6.arpa", "domain", "0.e.0.0.c.7.6.0.1.0.0.2.ip6.arpa")
    }

    def "create reverse domain, ripe IPv6 space with dash"() {
      given:
        syncUpdate(getTransient("ALLOC6-PA-LOW-DOM-R") + "password: hm\npassword: owner3")
        queryObject("-r -T inet6num 2001::/16", "inet6num", "2001::/16")

      expect:
        queryObjectNotFound("-rGBT domain 0.e.0.0.c.7.6.0.1.0.0.2.ip6.arpa", "domain", "0.e.0.0.c.7.6.0.1.0.0.2.ip6.arpa")

      when:
        def message = syncUpdate("""\
                domain:         0-1.e.0.0.c.7.6.0.1.0.0.2.ip6.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         owner-MNT
                source:         TEST

                password:   lir2
                password:   owner
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[domain] 0-1.e.0.0.c.7.6.0.1.0.0.2.ip6.arpa" }
//        ack.errorMessagesFor("Create", "[domain] 0-1.e.0.0.c.7.6.0.1.0.0.2.ip6.arpa") ==
//              ["Syntax error in 0-1.e.0.0.c.7.6.0.1.0.0.2.ip6.arpa"] TODO - ack.errorMessagesForMatches

        queryObjectNotFound("-rGBT domain 0.e.0.0.c.7.6.0.1.0.0.2.ip6.arpa", "domain", "0.e.0.0.c.7.6.0.1.0.0.2.ip6.arpa")
    }

    def "create reverse domain, ripe IPv6 space, /128"() {
      given:
        syncUpdate(getTransient("ALLOC6-PA-LOW-DOM-R") + "password: hm\npassword: owner3")
        queryObject("-r -T inet6num 2001::/16", "inet6num", "2001::/16")

      expect:
        queryObjectNotFound("-rGBT domain 0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.1.0.0.2.ip6.arpa", "domain", "0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.1.0.0.2.ip6.arpa")

      when:
        def message = syncUpdate("""\
                domain:         0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.1.0.0.2.ip6.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         owner-MNT
                source:         TEST
                override:   denis,override1

                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[domain] 0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.1.0.0.2.ip6.arpa" }
        ack.infoSuccessMessagesFor("Create", "[domain] 0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.1.0.0.2.ip6.arpa") == [
                "Authorisation override used"]

        queryObject("-rGBT domain 0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.1.0.0.2.ip6.arpa", "domain", "0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.1.0.0.2.ip6.arpa")
    }

    def "create reverse domain, dnssec errors"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-LOW-DOM-R") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 193.0.0.0 - 193.255.255.255", "inetnum", "193.0.0.0 - 193.255.255.255")

      expect:
        queryObjectNotFound("-rGBT domain 193.in-addr.arpa", "domain", "193.in-addr.arpa")

      when:
        dnsStubbedResponse("0.0.193.in-addr.arpa", "The zone 0.0.193.in-addr.arpa has published DS records, but none of them work.")

        def message = syncUpdate("""\
                domain:         0.0.193.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                ds-rdata:       52151  1  1  13ee60f7499a70e5aadaf05828e7fc59e8e70bc1
                mnt-by:         owner-MNT
                source:         TEST

                password:   lir2
                password:   owner
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[domain] 0.0.193.in-addr.arpa" }
        ack.errorMessagesFor("Create", "[domain] 0.0.193.in-addr.arpa") == [
                "The zone 0.0.193.in-addr.arpa has published DS records, but none of them work."
        ]

        queryObjectNotFound("-rGBT domain 0.0.193.in-addr.arpa", "domain", "0.0.193.in-addr.arpa")
    }

    def "create reverse domain, dnssec"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-LOW-DOM-R") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 193.0.0.0 - 193.255.255.255", "inetnum", "193.0.0.0 - 193.255.255.255")

      expect:
        queryObjectNotFound("-rGBT domain 193.in-addr.arpa", "domain", "193.in-addr.arpa")

      when:
        def message = syncUpdate("""\
                domain:         0.0.193.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                ds-rdata:       17881 5 1 2e58131e5fe28ec965a7b8e4efb52d0a028d7a78
                ds-rdata:       17881 5 2 8c6265733a73e5588bfac516a4fcfbe1103a544b95f254cb67a21e474079547e
                mnt-by:         owner-MNT
                source:         TEST

                password:   lir2
                password:   owner
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[domain] 0.0.193.in-addr.arpa" }

        queryObject("-rGBT domain 0.0.193.in-addr.arpa", "domain", "0.0.193.in-addr.arpa")
    }

    def "create reverse domain, dnssec with cont lines"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-LOW-DOM-R") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 193.0.0.0 - 193.255.255.255", "inetnum", "193.0.0.0 - 193.255.255.255")

      expect:
        queryObjectNotFound("-rGBT domain 193.in-addr.arpa", "domain", "193.in-addr.arpa")

      when:
        def message = syncUpdate("""\
                domain:         0.0.193.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                ds-rdata:       17881
                +5
                 1
                         2e58131e5fe28ec965a
                +7b8e4efb52d0a028d7a78
                ds-rdata:       17881 5 2 8c6265733a73e5588bfac516a4fcfbe1103a544b95f254cb67a21e474079547e
                mnt-by:         owner-MNT
                source:         TEST

                password:   lir2
                password:   owner
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[domain] 0.0.193.in-addr.arpa" }
        ack.infoSuccessMessagesFor("Create", "[domain] 0.0.193.in-addr.arpa") == [
                "Value 17881 5 1 2e58131e5fe28ec965a 7b8e4efb52d0a028d7a78 converted to 17881 5 1 2e58131e5fe28ec965a7b8e4efb52d0a028d7a78"
        ]

        queryObject("-rGBT domain 0.0.193.in-addr.arpa", "domain", "0.0.193.in-addr.arpa")
    }

    def "create reverse domain, invalid glue"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-LOW-DOM-R") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 193.0.0.0 - 193.255.255.255", "inetnum", "193.0.0.0 - 193.255.255.255")

      expect:
        queryObjectNotFound("-rGBT domain 193.in-addr.arpa", "domain", "193.in-addr.arpa")

      when:
        def message = syncUpdate("""\
                domain:         0.0.193.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net 81.20.133.177
                nserver:        ns3.nic.fr 2001:600::/128
                mnt-by:         owner-MNT
                source:         TEST
                override:   denis,override1

                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(2, 0, 2)
        ack.errors.any { it.operation == "Create" && it.key == "[domain] 0.0.193.in-addr.arpa" }
        ack.errorMessagesFor("Create", "[domain] 0.0.193.in-addr.arpa") ==
                ["Glue records only allowed if hostname ends with 0.0.193.in-addr.arpa",
                        "Glue records only allowed if hostname ends with 0.0.193.in-addr.arpa"
                ]
        ack.infoMessagesFor("Create", "[domain] 0.0.193.in-addr.arpa") ==
                ["Value ns3.nic.fr 2001:600::/128 converted to ns3.nic.fr 2001:600::",
                        "Authorisation override used"
                ]

        queryObjectNotFound("-rGBT domain 0.0.193.in-addr.arpa", "domain", "0.0.193.in-addr.arpa")
    }

    def "create reverse domain, invalid glue 2"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-LOW-DOM-R") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 193.0.0.0 - 193.255.255.255", "inetnum", "193.0.0.0 - 193.255.255.255")

      expect:
        queryObjectNotFound("-rGBT domain 193.in-addr.arpa", "domain", "193.in-addr.arpa")

      when:
        def message = syncUpdate("""\
                domain:         0.0.193.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net.0.0.193.in-addr.arpa 81.20.133.177.in-addr.arpa
                nserver:        ns3.nic.fr0.0.193.in-addr.arpa 2001:600::/32.ip6.arpa
                mnt-by:         owner-MNT
                source:         TEST
                override:   denis,override1

                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(2, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[domain] 0.0.193.in-addr.arpa" }
        ack.errorMessagesFor("Create", "[domain] 0.0.193.in-addr.arpa") ==
                ["Syntax error in pri.authdns.ripe.net.0.0.193.in-addr.arpa 81.20.133.177.in-addr.arpa",
                        "Syntax error in ns3.nic.fr0.0.193.in-addr.arpa 2001:600::/32.ip6.arpa"
                ]

        queryObjectNotFound("-rGBT domain 0.0.193.in-addr.arpa", "domain", "0.0.193.in-addr.arpa")
    }

    def "create reverse domain, invalid glue 3"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-LOW-DOM-R") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 193.0.0.0 - 193.255.255.255", "inetnum", "193.0.0.0 - 193.255.255.255")

      expect:
        queryObjectNotFound("-rGBT domain 193.in-addr.arpa", "domain", "193.in-addr.arpa")

      when:
        def message = syncUpdate("""\
                domain:         0.0.193.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net.0.0.193.in-addr.arpa 81.20.133/24
                nserver:        ns3.nic.fr0.0.193.in-addr.arpa 2001:600::/32
                mnt-by:         owner-MNT
                source:         TEST
                override:   denis,override1

                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(2, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[domain] 0.0.193.in-addr.arpa" }
        ack.errorMessagesFor("Create", "[domain] 0.0.193.in-addr.arpa") ==
                ["Syntax error in pri.authdns.ripe.net.0.0.193.in-addr.arpa 81.20.133/24",
                        "Syntax error in ns3.nic.fr0.0.193.in-addr.arpa 2001:600::/32"
                ]

        queryObjectNotFound("-rGBT domain 0.0.193.in-addr.arpa", "domain", "0.0.193.in-addr.arpa")
    }

    def "create reverse domain, valid glue"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-LOW-DOM-R") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 193.0.0.0 - 193.255.255.255", "inetnum", "193.0.0.0 - 193.255.255.255")

      expect:
        queryObjectNotFound("-rGBT domain 193.in-addr.arpa", "domain", "193.in-addr.arpa")

      when:
        def message = syncUpdate("""\
                domain:         0.0.193.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net.0.0.193.in-addr.arpa 81.20.133.177
                nserver:        ns3.nic.fr.0.0.193.in-addr.arpa 2001:600::1
                mnt-by:         owner-MNT
                source:         TEST
                override:   denis,override1

                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[domain] 0.0.193.in-addr.arpa" }
        ack.infoSuccessMessagesFor("Create", "[domain] 0.0.193.in-addr.arpa") == [
                "Authorisation override used"]

        queryObject("-rGBT domain 0.0.193.in-addr.arpa", "domain", "0.0.193.in-addr.arpa")
    }

    def "create reverse domain, valid glue, IPv6 and IPv4 addresses, same host"() {
        given:
        syncUpdate(getTransient("ALLOC-PA-LOW-DOM-R") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 193.0.0.0 - 193.255.255.255", "inetnum", "193.0.0.0 - 193.255.255.255")

        expect:
        queryObjectNotFound("-rGBT domain 193.in-addr.arpa", "domain", "193.in-addr.arpa")

        when:
        def message = syncUpdate("""\
                domain:         0.0.193.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net.0.0.193.in-addr.arpa 81.20.133.177
                nserver:        pri.authdns.ripe.net.0.0.193.in-addr.arpa 2001:600::1
                mnt-by:         owner-MNT
                source:         TEST
                password:   owner
                password:   lir2

                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[domain] 0.0.193.in-addr.arpa" }

        queryObject("-rGBT domain 0.0.193.in-addr.arpa", "domain", "0.0.193.in-addr.arpa")
    }

    def "create forward domain"() {
      expect:
        queryObjectNotFound("-rGBT domain mydomain.net", "domain", "mydomain.net")

      when:
        dnsStubbedResponse("mydomain.net",
                "No name servers found at child. No name servers could be found at the child. This usually means that the child is not configured to answer queries about the zone.",
                "Fatal error in delegation for zone mydomain.net. No name servers found at child or at parent. No further testing can be performed.")

        def message = syncUpdate("""\
                domain:         mydomain.net
                descr:          forward domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        sec3.apnic.net
                mnt-by:         LIR-MNT
                source:         TEST

                password:   lir
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[domain] mydomain.net" }
        ack.errorMessagesFor("Create", "[domain] mydomain.net") == [
                "Syntax error in mydomain.net"
        ]

        queryObjectNotFound("-rGBT domain mydomain.net", "domain", "mydomain.net")
    }

    def "create forward domain, override"() {
      given:

      expect:
        queryObjectNotFound("-rGBT domain mydomain.net", "domain", "mydomain.net")

      when:
        def message = syncUpdate("""\
                domain:         mydomain.net
                descr:          forward domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        ns1.some.net
                nserver:        ns2.some.net
                mnt-by:         LIR-MNT
                source:         TEST
                override:    denis,override1

                password:   lir
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[domain] mydomain.net" }
        ack.errorMessagesFor("Create", "[domain] mydomain.net") ==
                ["Syntax error in mydomain.net"]

        queryObjectNotFound("-rGBT domain mydomain.net", "domain", "mydomain.net")
    }

    def "create root enum domain"() {
      given:

      expect:
        queryObjectNotFound("-rGBT domain e164.arpa", "domain", "e164.arpa")

      when:
        def message = syncUpdate("""\
                domain:         e164.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         RIPE-GII-MNT
                source:         TEST

                password:       gii
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[domain] e164.arpa" }
        ack.errorMessagesFor("Create", "[domain] e164.arpa") ==
                ["Syntax error in e164.arpa"]

        queryObjectNotFound("-rGBT domain e164.arpa", "domain", "e164.arpa")
    }

    def "create UK enum domain, GII password"() {
      given:

      expect:
        queryObjectNotFound("-rGBT domain 4.4.e164.arpa", "domain", "4.4.e164.arpa")

      when:
        def message = syncUpdate("""\
                domain:         4.4.e164.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        ns1.nic.uk
                nserver:        ns3.nic.uk
                mnt-by:         owner-MNT
                mnt-by:         RIPE-GII-MNT
                source:         TEST

                password:       gii
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[domain] 4.4.e164.arpa" }

        queryObject("-rGBT domain 4.4.e164.arpa", "domain", "4.4.e164.arpa")
    }

    def "create UK enum domain, user password"() {
      given:
        syncUpdate(getTransient("ALLOC-PA-LOW-DOM-R") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 193.0.0.0 - 193.255.255.255", "inetnum", "193.0.0.0 - 193.255.255.255")

      expect:
        queryObjectNotFound("-rGBT domain 4.4.e164.arpa", "domain", "4.4.e164.arpa")

      when:
        def message = syncUpdate("""\
                domain:         4.4.e164.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        ns1.nic.uk
                nserver:        ns3.nic.uk
                mnt-by:         owner-MNT
                mnt-by:         RIPE-GII-MNT
                source:         TEST

                password:       owner
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[domain] 4.4.e164.arpa" }
        ack.errorMessagesFor("Create", "[domain] 4.4.e164.arpa") ==
                ["Creating enum domain requires administrative authorisation"]

        queryObjectNotFound("-rGBT domain 4.4.e164.arpa", "domain", "4.4.e164.arpa")
    }

    def "create UK enum domain, override"() {
      given:

      expect:
        queryObjectNotFound("-rGBT domain 4.4.e164.arpa", "domain", "4.4.e164.arpa")

      when:
        def message = syncUpdate("""\
                domain:         4.4.e164.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        ns1.nic.uk
                nserver:        ns3.nic.uk
                mnt-by:         owner-MNT
                mnt-by:         RIPE-GII-MNT
                source:         TEST
                override:    denis,override1

                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[domain] 4.4.e164.arpa" }
        ack.infoSuccessMessagesFor("Create", "[domain] 4.4.e164.arpa") == [
                "Authorisation override used"]

        queryObject("-rGBT domain 4.4.e164.arpa", "domain", "4.4.e164.arpa")
    }

    def "modify UK enum domain, GII password"() {
      given:
        syncUpdate(getTransient("ENUM-UK") + "password: gii")
        queryObject("-r -T domain 4.4.e164.arpa", "domain", "4.4.e164.arpa")

      expect:

      when:
        def message = syncUpdate("""\
                domain:         4.4.e164.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        ns1.nic.uk
                nserver:        ns3.nic.uk
                mnt-by:         owner-MNT
                mnt-by:         RIPE-GII-MNT
                remarks:        just added
                source:         TEST

                password:       gii
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[domain] 4.4.e164.arpa" }

        query_object_matches("-rGBT domain 4.4.e164.arpa", "domain", "4.4.e164.arpa", "just added")
    }

    def "modify UK enum domain, override"() {
      given:
        syncUpdate(getTransient("ENUM-UK") + "password: gii")
        queryObject("-r -T domain 4.4.e164.arpa", "domain", "4.4.e164.arpa")

      expect:

      when:
        def message = syncUpdate("""\
                domain:         4.4.e164.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        ns1.nic.uk
                nserver:        ns3.nic.uk
                mnt-by:         owner-MNT
                mnt-by:         RIPE-GII-MNT
                remarks:        just added
                source:         TEST
                override:   denis,override1

                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Modify" && it.key == "[domain] 4.4.e164.arpa" }
        ack.infoSuccessMessagesFor("Modify", "[domain] 4.4.e164.arpa") == [
                "Authorisation override used"]

        query_object_matches("-rGBT domain 4.4.e164.arpa", "domain", "4.4.e164.arpa", "just added")
    }

    def "modify UK enum domain, user password"() {
      given:
        syncUpdate(getTransient("ENUM-UK") + "password: gii")
        queryObject("-r -T domain 4.4.e164.arpa", "domain", "4.4.e164.arpa")

      expect:

      when:
        def message = syncUpdate("""\
                domain:         4.4.e164.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        ns1.nic.uk
                nserver:        ns3.nic.uk
                mnt-by:         owner-MNT
                mnt-by:         RIPE-GII-MNT
                remarks:        just added
                source:         TEST

                password:       owner
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[domain] 4.4.e164.arpa" }

        query_object_matches("-rGBT domain 4.4.e164.arpa", "domain", "4.4.e164.arpa", "just added")
    }

    def "delete UK enum domain, user password"() {
      given:
        syncUpdate(getTransient("ENUM-UK") + "password: gii")
        queryObject("-r -T domain 4.4.e164.arpa", "domain", "4.4.e164.arpa")

      expect:

      when:
        def message = syncUpdate("""\
                domain:         4.4.e164.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        ns1.nic.uk
                nserver:        ns3.nic.uk
                mnt-by:         owner-MNT
                mnt-by:         RIPE-GII-MNT
                source:         TEST
                delete:  test ing

                password:       owner
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Delete" && it.key == "[domain] 4.4.e164.arpa" }

        queryObjectNotFound("-rGBT domain 4.4.e164.arpa", "domain", "4.4.e164.arpa")
    }

    def "delete UK enum domain, GII password"() {
      given:
        syncUpdate(getTransient("ENUM-UK") + "password: gii")
        queryObject("-r -T domain 4.4.e164.arpa", "domain", "4.4.e164.arpa")

      expect:

      when:
        def message = syncUpdate("""\
                domain:         4.4.e164.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        ns1.nic.uk
                nserver:        ns3.nic.uk
                mnt-by:         owner-MNT
                mnt-by:         RIPE-GII-MNT
                source:         TEST
                delete:  test ing

                password:       gii
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Delete" && it.key == "[domain] 4.4.e164.arpa" }

        queryObjectNotFound("-rGBT domain 4.4.e164.arpa", "domain", "4.4.e164.arpa")
    }

    def "create overlapping domain"() {
        given:
        syncUpdate(getTransient("ALLOC-PA-LOW-DOM-R") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 193.0.0.0 - 193.255.255.255", "inetnum", "193.0.0.0 - 193.255.255.255")
        syncUpdate(getTransient("OVERLAP-DOMAIN") + "override: denis,override1")
        ipTreeUpdater.rebuild();

        when:
        def message = syncUpdate("""\
                Domain:         191-193.0.0.193.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         owner-MNT
                source:         TEST

                password:   lir2
                password:   owner
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[domain] 191-193.0.0.193.in-addr.arpa" }
        ack.errorMessagesFor("Create", "[domain] 191-193.0.0.193.in-addr.arpa") ==
                ["This domain overlaps with 128-191.0.0.193.in-addr.arpa"]

        queryObjectNotFound("-rGBT domain 191-193.0.0.193.in-addr.arpa", "domain", "191-193.0.0.193.in-addr.arpa")
    }

}
