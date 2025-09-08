package net.ripe.db.whois.spec.update


import net.ripe.db.whois.common.rpsl.ObjectType
import net.ripe.db.whois.spec.BaseQueryUpdateSpec

@org.junit.jupiter.api.Tag("IntegrationTest")
class OutOfRegionSpec extends BaseQueryUpdateSpec {

    @Override
    Map<String, String> getFixtures() {
        [
                "TEST-PN": """\
                person:  Test Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: TP1-TEST
                mnt-by:  OWNER-MNT
                source:  TEST
                """,
                "AS222 - AS333": """\
                as-block:       AS222 - AS333
                descr:          RIPE NCC ASN block
                mnt-by:         RIPE-NCC-HM-MNT
                mnt-lower:      LIR-MNT
                source:         TEST
                """,
                "OUT-OF-REGION-INETNUM": """\
                inetnum:         213.152.64.0 - 213.152.95.255
                netname:         NON-RIPE-NCC-MANAGED-ADDRESS-BLOCK
                admin-c:         TP1-TEST
                tech-c:          TP1-TEST
                status:          ALLOCATED UNSPECIFIED
                mnt-by:          RIPE-NCC-HM-MNT
                mnt-lower:       RIPE-NCC-HM-MNT
                created:         2014-11-07T14:15:06Z
                last-modified:   2015-10-29T15:18:51Z
                source:          TEST
                """,
                "IN-REGION-INET6NUM": """\
                inet6num:     2001:600::/25
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                mnt-by:          RIPE-NCC-HM-MNT
                status:       ALLOCATED-BY-RIR
                source:       TEST
                """,
        ]
    }

    @Override
    Map<String, String> getTransients() {
        [
                "OUT-OF-REGION-AUTNUM": """\
                aut-num:        AS252
                as-name:        End-User-1
                descr:          description
                status:         OTHER
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-LIR1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         LIR-MNT
                mnt-by:         RIPE-NCC-HM-MNT
                source:         TEST-NONAUTH
                """,
                "IN-REGION-AUTNUM": """\
                aut-num:        AS251
                as-name:        End-User-1
                descr:          description
                status:         OTHER
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-LIR1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         LIR-MNT
                mnt-by:         RIPE-NCC-HM-MNT
                source:         TEST
                """,
                "IN-REGION-INETNUM": """\
                inetnum:     10.2.0.0 - 10.2.255.255
                netname:     ReallyAmazingNetname
                country:     NL
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                org:         ORG-LIR1-TEST
                status:      ALLOCATED PA
                mnt-by:      OWNER-MNT
                mnt-lower:   OWNER2-MNT
                mnt-routes:  OWNER3-MNT
                mnt-routes:  RIPE-NCC-HM-MNT
                source:      TEST
                """,
                "OUT-OF-REGION-ROUTE": """\
                route:          213.152.64.0/24
                descr:          A route
                origin:         AS252
                mnt-by:         LIR-MNT
                mnt-by:         RIPE-NCC-HM-MNT
                created:        2002-05-21T15:33:55Z
                last-modified:  2009-10-15T09:32:17Z
                source:         TEST-NONAUTH
                """,
                "IN-REGION-ROUTE": """\
                route:          10.1.0.0/16
                descr:          A route
                origin:         AS252
                mnt-by:         LIR-MNT
                mnt-by:         RIPE-NCC-HM-MNT
                created:        2002-05-21T15:33:55Z
                last-modified:  2009-10-15T09:32:17Z
                source:         TEST
                """,
                "OUT-OF-REGION-ROUTE6": """\
                route6:         2001:400::/24
                descr:          A route
                origin:         AS252
                mnt-by:         LIR-MNT
                mnt-by:         RIPE-NCC-HM-MNT
                created:        2002-05-21T15:33:55Z
                last-modified:  2009-10-15T09:32:17Z
                source:         TEST-NONAUTH
                """,
                "IN-REGION-ROUTE6": """\
                route6:         2001:600::/25
                descr:          A route
                origin:         AS252
                mnt-by:         LIR-MNT
                mnt-by:         RIPE-NCC-HM-MNT
                created:        2002-05-21T15:33:55Z
                last-modified:  2009-10-15T09:32:17Z
                source:         TEST
                """,
        ]
    }

    @Override
    List<String> getAuthoritativeResources() {
        [
                "::/3",
                "2000::/16",
                "2001::/22",
                "2001:600::/23",
                "2001:800::/21",
                "2001:1000::/20",
                "2001:2000::/19",
                "2001:4000::/18",
                "2001:8000::/17",
                "2002::/15",
                "2004::/14",
                "2008::/13",
                "2010::/12",
                "2020::/11",
                "2040::/10",
                "2080::/9",
                "2100::/8",
                "2200::/7",
                "2400::/6",
                "2800::/5",
                "3000::/4",
                "4000::/2",
                "8000::/1",
                "0.0.0.0/1",
                "128.0.0.0/2",
                "192.0.0.0/4",
                "208.0.0.0/6",
                "212.0.0.0/8",
                "213.0.0.0/9",
                "213.128.0.0/12",
                "213.144.0.0/13",
                "213.152.0.0/18",
                "213.152.96.0/19",
                "213.152.128.0/17",
                "213.153.0.0/16",
                "213.154.0.0/15",
                "213.156.0.0/14",
                "213.160.0.0/11",
                "213.192.0.0/10",
                "214.0.0.0/7",
                "216.0.0.0/5",
                "224.0.0.0/3"
        ]
    }

    def "not create out of region aut-num"() {
      when:
          def ack = syncUpdateWithResponse("""
                aut-num:        AS252
                as-name:        End-User-1
                descr:          description
                status:         OTHER
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-LIR1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         LIR-MNT
                source:         TEST

                password:   lir
                password:   owner3
                """.stripIndent(true)
        )

      then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 2, 0)

      ack.errors.any { it.operation == "Create" && it.key == "[aut-num] AS252" }
      ack.errorMessagesFor("Create", "[aut-num] AS252") == [
              "Cannot create out of region aut-num objects"
      ]
      ack.warningMessagesFor("Create", "[aut-num] AS252") ==
              ["Supplied attribute 'source' has been replaced with a generated value"]

      queryObjectNotFound("-rBG -T aut-num AS252", "aut-num", "AS252")
    }

    def "modify out of region aut-num"() {
        given:
        dbfixture(getTransient("OUT-OF-REGION-AUTNUM"))
        when:
        def ack = syncUpdateWithResponse("""
                aut-num:        AS252
                as-name:        End-User-1
                descr:          description2
                status:         OTHER
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-LIR1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         LIR-MNT
                mnt-by:         RIPE-NCC-HM-MNT
                source:         TEST-NONAUTH

                password:   lir
                password:   owner3
                """.stripIndent(true)
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)

        queryObject("-rBG -T aut-num AS252", "aut-num", "AS252")
    }

    def "modify out of region aut-num, wrong source"() {
        given:
        dbfixture(getTransient("OUT-OF-REGION-AUTNUM"))
        when:
        def ack = syncUpdateWithResponse("""
                aut-num:        AS252
                as-name:        End-User-1
                descr:          description2
                status:         OTHER
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-LIR1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         LIR-MNT
                mnt-by:         RIPE-NCC-HM-MNT
                source:         TEST

                password:   lir
                password:   owner3
                """.stripIndent(true)
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.warningSuccessMessagesFor("Modify", "[aut-num] AS252") ==
                ["Supplied attribute 'source' has been replaced with a generated value"]

        when:
        def autnum = restLookup(ObjectType.AUT_NUM, "AS252", "update");

        then:
        hasAttribute(autnum, "source", "TEST-NONAUTH", null);
    }

    def "modify out of region aut-num, rs maintainer"() {
        given:
        dbfixture(getTransient("OUT-OF-REGION-AUTNUM"))
        when:
        def ack = syncUpdateWithResponse("""
                aut-num:        AS252
                as-name:        End-User-1
                descr:          description2
                status:         OTHER
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-LIR1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         LIR-MNT
                mnt-by:         RIPE-NCC-HM-MNT
                source:         TEST-NONAUTH

                password:   hm
                """.stripIndent(true)
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)

        queryObject("-rBG -T aut-num AS252", "aut-num", "AS252")
    }

    def "modify out of region aut-num, wrong source, rs maintainer"() {
        given:
        dbfixture(getTransient("OUT-OF-REGION-AUTNUM"))
        when:
        def ack = syncUpdateWithResponse("""
                aut-num:        AS252
                as-name:        End-User-1
                descr:          description2
                status:         OTHER
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-LIR1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         LIR-MNT
                mnt-by:         RIPE-NCC-HM-MNT
                source:         TEST

                password:   hm
                """.stripIndent(true)
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.warningSuccessMessagesFor("Modify", "[aut-num] AS252") ==
                ["Supplied attribute 'source' has been replaced with a generated value"]

        when:
        def autnum = restLookup(ObjectType.AUT_NUM, "AS252", "update");

        then:
        hasAttribute(autnum, "source", "TEST-NONAUTH", null);
    }

    def "modify out of region aut-num, wrong source, using override"() {
        given:
        dbfixture(getTransient("OUT-OF-REGION-AUTNUM"))
        when:
        def ack = syncUpdateWithResponse("""
                aut-num:        AS252
                as-name:        End-User-1
                descr:          description2
                status:         OTHER
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-LIR1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         LIR-MNT
                source:         TEST
                override:       denis,override1
                """.stripIndent(true)
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 1)
        ack.warningSuccessMessagesFor("Modify", "[aut-num] AS252") ==
                ["Supplied attribute 'source' has been replaced with a generated value",
                 "You cannot add or remove a RIPE NCC maintainer"]

        when:
        def autnum = restLookup(ObjectType.AUT_NUM, "AS252", "update");

        then:
        hasAttribute(autnum, "source", "TEST-NONAUTH", null);
    }

    def "modify in region aut-num, nonauth source, rs maintainer"() {
        given:
        dbfixture(getTransient("IN-REGION-AUTNUM"))
        when:
        def ack = syncUpdateWithResponse("""
                aut-num:        AS251
                as-name:        End-User-1
                descr:          description2
                status:         OTHER
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-LIR1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         LIR-MNT
                mnt-by:         RIPE-NCC-HM-MNT
                source:         TEST-NONAUTH

                password:   hm
                """.stripIndent(true)
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 3, 0)
        ack.warningSuccessMessagesFor("Modify", "[aut-num] AS251") ==
                ["Supplied attribute 'status' has been replaced with a generated value",
                 "Supplied attribute 'source' has been replaced with a generated value"]
    }

    def "modify in region aut-num, nonauth source, using override"() {
        given:
        dbfixture(getTransient("IN-REGION-AUTNUM"))
        when:
        def ack = syncUpdateWithResponse("""
                aut-num:        AS251
                as-name:        End-User-1
                descr:          description
                status:         OTHER
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-LIR1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         LIR-MNT
                source:         TEST-NONAUTH
                override:       denis,override1
                """.stripIndent(true)
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 3, 1)
        ack.warningSuccessMessagesFor("Modify", "[aut-num] AS251") ==
                ["Supplied attribute 'status' has been replaced with a generated value",
                 "Supplied attribute 'source' has been replaced with a generated value",
                 "You cannot add or remove a RIPE NCC maintainer"]
    }

    def "modify out of region aut-num, using override"() {
        given:
        dbfixture(getTransient("OUT-OF-REGION-AUTNUM"))
        when:
        def ack = syncUpdateWithResponse("""
                aut-num:        AS252
                as-name:        End-User-1
                descr:          description2
                status:         OTHER
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-LIR1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         LIR-MNT
                source:         TEST-NONAUTH
                override:       denis,override1

                password:   lir
                password:   owner3
                """.stripIndent(true)
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 1)

        queryObject("-rBG -T aut-num AS252", "aut-num", "AS252")
    }

    def "modify in region aut-num with nonauth source"() {
        given:
        dbfixture(getTransient("IN-REGION-AUTNUM"))
        when:
        def ack = syncUpdateWithResponse("""
                aut-num:        AS251
                as-name:        End-User-1
                descr:          description
                status:         OTHER
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-LIR1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         LIR-MNT
                mnt-by:         RIPE-NCC-HM-MNT
                source:         TEST-NONAUTH

                password:   lir
                password:   owner3
                """.stripIndent(true)
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 3, 0)

        ack.warningSuccessMessagesFor("Modify", "[aut-num] AS251") ==
                ["Supplied attribute 'status' has been replaced with a generated value",
                 "Supplied attribute 'source' has been replaced with a generated value"]

        when:
        def autnum = restLookup(ObjectType.AUT_NUM, "AS251", "update");

        then:
        hasAttribute(autnum, "source", "TEST", null);
    }

    def "create in region aut-num with nonauth source"() {
        when:
        def ack = syncUpdateWithResponse("""
                aut-num:        AS251
                as-name:        End-User-1
                descr:          description
                status:         OTHER
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-LIR1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         LIR-MNT
                source:         TEST-NONAUTH

                password:   lir
                password:   owner3
                """.stripIndent(true)
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 3, 0)

        ack.warningSuccessMessagesFor("Create", "[aut-num] AS251") ==
                ["Supplied attribute 'status' has been replaced with a generated value",
                 "Supplied attribute 'source' has been replaced with a generated value"]

        when:
        def autnum = restLookup(ObjectType.AUT_NUM, "AS251", "update");

        then:
        hasAttribute(autnum, "source", "TEST", null);
    }

    def "not create out of region aut-num, nonauth source"() {
        when:
        def ack = syncUpdateWithResponse("""
                aut-num:        AS252
                as-name:        End-User-1
                descr:          description
                status:         OTHER
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-LIR1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         LIR-MNT
                source:         TEST-NONAUTH

                password:   lir
                password:   owner3
                """.stripIndent(true)
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)

        ack.errors.any { it.operation == "Create" && it.key == "[aut-num] AS252" }
        ack.errorMessagesFor("Create", "[aut-num] AS252") == [
                "Cannot create out of region aut-num objects"
        ]

        queryObjectNotFound("-rBG -T aut-num AS252", "aut-num", "AS252")
    }

    def "create out of region aut-num, with override"() {
        when:
        def ack = syncUpdateWithResponse("""
                aut-num:        AS252
                as-name:        End-User-1
                descr:          description
                status:         OTHER
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-LIR1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         LIR-MNT
                source:         TEST
                override:       denis,override1

                password:   lir
                password:   owner3
                
                """.stripIndent(true)
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 1)

        ack.successes.any { it.operation == "Create" && it.key == "[aut-num] AS252" }
        ack.warningSuccessMessagesFor("Create", "[aut-num] AS252") ==
                ["Supplied attribute 'source' has been replaced with a generated value"]

        when:
        def autnum = restLookup(ObjectType.AUT_NUM, "AS252", "update");

        then:
        hasAttribute(autnum, "source", "TEST-NONAUTH", null);
    }

    def "create out of region aut-num, rs-maintainer"() {
        when:
        def ack = syncUpdateWithResponse("""
                aut-num:        AS252
                as-name:        End-User-1
                descr:          description
                status:         OTHER
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-LIR1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-HM-MNT
                source:         TEST

                password:   hm
                password:   lir
                password:   owner3
                
                """.stripIndent(true)
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)

        ack.successes.any { it.operation == "Create" && it.key == "[aut-num] AS252" }
        ack.warningSuccessMessagesFor("Create", "[aut-num] AS252") ==
                ["Supplied attribute 'source' has been replaced with a generated value"]

        when:
        def autnum = restLookup(ObjectType.AUT_NUM, "AS252", "update");

        then:
        hasAttribute(autnum, "source", "TEST-NONAUTH", null);
    }

    def "create out of region aut-num with nonauth source, rs-maintainer"() {
        when:
        def ack = syncUpdateWithResponse("""
                aut-num:        AS252
                as-name:        End-User-1
                descr:          description
                status:         OTHER
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-LIR1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-HM-MNT
                source:         TEST-NONAUTH

                password:   hm
                password:   lir
                password:   owner3
                
                """.stripIndent(true)
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)

        ack.successes.any { it.operation == "Create" && it.key == "[aut-num] AS252" }

        when:
        def autnum = restLookup(ObjectType.AUT_NUM, "AS252", "update");

        then:
        hasAttribute(autnum, "source", "TEST-NONAUTH", null);
    }

    def "create out of region aut-num with nonauth source, with override"() {
        when:
        def ack = syncUpdateWithResponse("""
                aut-num:        AS252
                as-name:        End-User-1
                descr:          description
                status:         OTHER
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-LIR1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         LIR-MNT
                source:         TEST-NONAUTH
                override:       denis,override1

                password:   lir
                password:   owner3
                
                """.stripIndent(true)
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)

        ack.successes.any { it.operation == "Create" && it.key == "[aut-num] AS252" }

        when:
        def autnum = restLookup(ObjectType.AUT_NUM, "AS252", "update");

        then:
        hasAttribute(autnum, "source", "TEST-NONAUTH", null);
    }

    def "not create inetnum with nonauth source"() {
        when:
        def ack = syncUpdateWithResponse("""
                inetnum:     10.1.0.0 - 10.1.255.255
                netname:     invalid-net
                country:     NL
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                status:      ASSIGNED PA
                mnt-by:      OWNER-MNT
                source:      TEST-NONAUTH

                password:   owner
                password:   owner2
                
                """.stripIndent(true)
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)

        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 10.1.0.0 - 10.1.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 10.1.0.0 - 10.1.255.255") == [
                "Source TEST-NONAUTH is not allowed for inetnum objects"
        ]

        queryObjectNotFound("-r -T inetnum 10.1.0.0 - 10.1.255.255", "inetnum", "10.1.0.0 - 10.1.255.255")
    }

    def "not create out of region route"() {
        when:
        def ack = syncUpdateWithResponse("""
                route:          213.152.64.0/24
                descr:          A route
                origin:         AS252
                mnt-by:         LIR-MNT
                created:        2002-05-21T15:33:55Z
                last-modified:  2009-10-15T09:32:17Z
                source:         TEST-NONAUTH
                
                password: lir                
                """.stripIndent(true)
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(2, 1, 0)

        ack.errors.any { it.operation == "Create" && it.key == "[route] 213.152.64.0/24AS252" }
        ack.errorMessagesFor("Create", "[route] 213.152.64.0/24AS252") == [
                "Authorisation for [inetnum] 213.152.64.0 - 213.152.95.255 failed using \"mnt-lower:\" not authenticated by: RIPE-NCC-HM-MNT",
                "Cannot create out of region route objects"
        ]

        queryObjectNotFound("-rGBT route 213.152.64.0/24", "route", "213.152.64.0/24")
    }

    def "create out of region route, rs maintainer"() {
        when:
        def ack = syncUpdateWithResponse("""
                route:          213.152.64.0/24
                descr:          A route
                origin:         AS252
                mnt-by:         LIR-MNT
                mnt-by:         RIPE-NCC-HM-MNT
                source:         TEST-NONAUTH
                
                password: hm                
                """.stripIndent(true)
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)

        queryObject("-rGBT route 213.152.64.0/24", "route", "213.152.64.0/24")
    }

    def "create out of region route, using override"() {
        when:
        def ack = syncUpdateWithResponse("""
                route:          213.152.64.0/24
                descr:          A route
                origin:         AS252
                mnt-by:         LIR-MNT
                source:         TEST-NONAUTH
                override:       denis,override1
                """.stripIndent(true)
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)

        queryObject("-rGBT route 213.152.64.0/24", "route", "213.152.64.0/24")
    }

    def "create in region route, wrong source"() {
        when:
        def ack = syncUpdateWithResponse("""
                route:          10.1.0.0/16
                descr:          A route
                origin:         AS252
                mnt-by:         LIR-MNT
                source:         TEST-NONAUTH
                
                password: lir                
                password: owner3                
                """.stripIndent(true)
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.warningSuccessMessagesFor("Create", "[route] 10.1.0.0/16AS252") ==
            [ "Supplied attribute 'source' has been replaced with a generated value" ]

        queryObject("-rGBT route 10.1.0.0/16", "route", "10.1.0.0/16")
    }

    def "create in region route, wrong source, rs maintainer"() {
        given:
        dbfixture(getTransient("IN-REGION-INETNUM"))
        when:
        def ack = syncUpdateWithResponse("""
                route:          10.2.0.0/16
                descr:          A route
                origin:         AS252
                mnt-by:         LIR-MNT
                mnt-by:         RIPE-NCC-HM-MNT
                source:         TEST-NONAUTH
                
                password: hm                
                """.stripIndent(true)
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.warningSuccessMessagesFor("Create", "[route] 10.2.0.0/16AS252") ==
            [ "Supplied attribute 'source' has been replaced with a generated value" ]

        queryObject("-rGBT route 10.2.0.0/16", "route", "10.2.0.0/16")
    }

    def "create in region route, wrong source, using override"() {
        when:
        def ack = syncUpdateWithResponse("""
                route:          10.1.0.0/16
                descr:          A route
                origin:         AS252
                mnt-by:         LIR-MNT
                source:         TEST-NONAUTH
                override:       denis,override1
                """.stripIndent(true)
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 1)
        ack.warningSuccessMessagesFor("Create", "[route] 10.1.0.0/16AS252") ==
                ["Supplied attribute 'source' has been replaced with a generated value"]

        queryObject("-rGBT route 10.1.0.0/16", "route", "10.1.0.0/16")
    }

    def "not create out of region route, wrong source"() {
        when:
        def ack = syncUpdateWithResponse("""
                route:          213.152.64.0/24
                descr:          A route
                origin:         AS252
                mnt-by:         LIR-MNT
                created:        2002-05-21T15:33:55Z
                last-modified:  2009-10-15T09:32:17Z
                source:         TEST
                
                password: lir                
                """.stripIndent(true)
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(2, 2, 0)

        ack.errors.any { it.operation == "Create" && it.key == "[route] 213.152.64.0/24AS252" }
        ack.errorMessagesFor("Create", "[route] 213.152.64.0/24AS252") == [
                "Authorisation for [inetnum] 213.152.64.0 - 213.152.95.255 failed using \"mnt-lower:\" not authenticated by: RIPE-NCC-HM-MNT",
                "Cannot create out of region route objects"
        ]
        ack.warningMessagesFor("Create", "[route] 213.152.64.0/24AS252") ==
                ["Supplied attribute 'source' has been replaced with a generated value"]

        queryObjectNotFound("-rGBT route 213.152.64.0/24", "route", "213.152.64.0/24")
    }

    def "create out of region route, rs maintainer, wrong source"() {
        when:
        def ack = syncUpdateWithResponse("""
                route:          213.152.64.0/24
                descr:          A route
                origin:         AS252
                mnt-by:         LIR-MNT
                mnt-by:         RIPE-NCC-HM-MNT
                source:         TEST
                
                password: hm                
                """.stripIndent(true)
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.warningSuccessMessagesFor("Create", "[route] 213.152.64.0/24AS252") ==
                ["Supplied attribute 'source' has been replaced with a generated value"]

        queryObject("-rGBT route 213.152.64.0/24", "route", "213.152.64.0/24")
    }

    def "create out of region route, using override, with wrong source"() {
        when:
        def ack = syncUpdateWithResponse("""
                route:          213.152.64.0/24
                descr:          A route
                origin:         AS252
                mnt-by:         LIR-MNT
                source:         TEST
                override:       denis,override1
                """.stripIndent(true)
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 1)
        ack.warningSuccessMessagesFor("Create", "[route] 213.152.64.0/24AS252") ==
                ["Supplied attribute 'source' has been replaced with a generated value"]

        queryObject("-rGBT route 213.152.64.0/24", "route", "213.152.64.0/24")
    }

    def "modify out of region route"() {
        given:
        dbfixture(getTransient("OUT-OF-REGION-ROUTE"))
        when:
        def ack = syncUpdateWithResponse("""
                route:          213.152.64.0/24
                descr:          A route
                descr:          and another descr
                origin:         AS252
                mnt-by:         LIR-MNT
                mnt-by:         RIPE-NCC-HM-MNT
                source:         TEST-NONAUTH
                
                password: lir                
                """.stripIndent(true)
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)

        queryObject("-rGBT route 213.152.64.0/24", "route", "213.152.64.0/24")
    }

    def "modify out of region route, rs maintainer"() {
        given:
        dbfixture(getTransient("OUT-OF-REGION-ROUTE"))
        when:
        def ack = syncUpdateWithResponse("""
                route:          213.152.64.0/24
                descr:          A route
                descr:          and another descr
                origin:         AS252
                mnt-by:         LIR-MNT
                mnt-by:         RIPE-NCC-HM-MNT
                source:         TEST-NONAUTH
                
                password: hm                
                """.stripIndent(true)
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)

        queryObject("-rGBT route 213.152.64.0/24", "route", "213.152.64.0/24")
    }

    def "modify out of region route, with override"() {
        given:
        dbfixture(getTransient("OUT-OF-REGION-ROUTE"))
        when:
        def ack = syncUpdateWithResponse("""
                route:          213.152.64.0/24
                descr:          A route
                descr:          and another descr
                origin:         AS252
                mnt-by:         LIR-MNT
                source:         TEST-NONAUTH
                override:       denis,override1
                """.stripIndent(true)
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 1)

        queryObject("-rGBT route 213.152.64.0/24", "route", "213.152.64.0/24")
    }

    def "modify in region route, wrong source"() {
        given:
        dbfixture(getTransient("IN-REGION-ROUTE"))
        when:
        def ack = syncUpdateWithResponse("""
                route:          10.1.0.0/16
                descr:          A route
                descr:          and another descr
                origin:         AS252
                mnt-by:         LIR-MNT
                mnt-by:         RIPE-NCC-HM-MNT
                source:         TEST-NONAUTH
                
                password: lir                
                """.stripIndent(true)
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.warningSuccessMessagesFor("Modify", "[route] 10.1.0.0/16AS252") ==
                ["Supplied attribute 'source' has been replaced with a generated value"]

        queryObject("-rGBT route 10.1.0.0/16", "route", "10.1.0.0/16")
    }

    def "modify in region route, rs maintainer, with wrong source"() {
        given:
        dbfixture(getTransient("IN-REGION-ROUTE"))
        when:
        def ack = syncUpdateWithResponse("""
                route:          10.1.0.0/16
                descr:          A route
                descr:          and another descr
                origin:         AS252
                mnt-by:         LIR-MNT
                mnt-by:         RIPE-NCC-HM-MNT
                source:         TEST-NONAUTH
                
                password: hm                
                """.stripIndent(true)
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.warningSuccessMessagesFor("Modify", "[route] 10.1.0.0/16AS252") ==
                ["Supplied attribute 'source' has been replaced with a generated value"]

        queryObject("-rGBT route 10.1.0.0/16", "route", "10.1.0.0/16")
    }

    def "modify in region route, wrong source, using override"() {
        given:
        dbfixture(getTransient("IN-REGION-ROUTE"))
        when:
        def ack = syncUpdateWithResponse("""
                route:          10.1.0.0/16
                descr:          A route
                descr:          and another descr
                origin:         AS252
                mnt-by:         LIR-MNT
                source:         TEST-NONAUTH
                override:       denis,override1
                """.stripIndent(true)
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 1)
        ack.warningSuccessMessagesFor("Modify", "[route] 10.1.0.0/16AS252") ==
                ["Supplied attribute 'source' has been replaced with a generated value",
                 "You cannot add or remove a RIPE NCC maintainer"]

        queryObject("-rGBT route 10.1.0.0/16", "route", "10.1.0.0/16")
    }

    def "modify out of region route, wrong source"() {
        given:
        dbfixture(getTransient("OUT-OF-REGION-ROUTE"))
        when:
        def ack = syncUpdateWithResponse("""
                route:          213.152.64.0/24
                descr:          A route
                descr:          and another descr
                origin:         AS252
                mnt-by:         LIR-MNT
                mnt-by:         RIPE-NCC-HM-MNT
                source:         TEST
                
                password: lir                
                """.stripIndent(true)
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.warningSuccessMessagesFor("Modify", "[route] 213.152.64.0/24AS252") ==
                ["Supplied attribute 'source' has been replaced with a generated value"]

        queryObject("-rGBT route 213.152.64.0/24", "route", "213.152.64.0/24")
    }

    def "modify out of region route, rs maintainer, wrong source"() {
        given:
        dbfixture(getTransient("OUT-OF-REGION-ROUTE"))
        when:
        def ack = syncUpdateWithResponse("""
                route:          213.152.64.0/24
                descr:          A route
                descr:          and another descr
                origin:         AS252
                mnt-by:         LIR-MNT
                mnt-by:         RIPE-NCC-HM-MNT
                source:         TEST
                
                password: hm                
                """.stripIndent(true)
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.warningSuccessMessagesFor("Modify", "[route] 213.152.64.0/24AS252") ==
                ["Supplied attribute 'source' has been replaced with a generated value"]

        queryObject("-rGBT route 213.152.64.0/24", "route", "213.152.64.0/24")
    }

    def "modify out of region route, with override, wrong source"() {
        given:
        dbfixture(getTransient("OUT-OF-REGION-ROUTE"))
        when:
        def ack = syncUpdateWithResponse("""
                route:          213.152.64.0/24
                descr:          A route
                descr:          and another descr
                origin:         AS252
                mnt-by:         LIR-MNT
                source:         TEST
                override:       denis,override1
                """.stripIndent(true)
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 1)
        ack.warningSuccessMessagesFor("Modify", "[route] 213.152.64.0/24AS252") ==
                ["Supplied attribute 'source' has been replaced with a generated value",
                 "You cannot add or remove a RIPE NCC maintainer"]

        queryObject("-rGBT route 213.152.64.0/24", "route", "213.152.64.0/24")
    }

    def "not create out of region route6"() {
        when:
        def ack = syncUpdateWithResponse("""
                route6:         2001:400::/24
                descr:          A route
                origin:         AS252
                mnt-by:         LIR-MNT
                source:         TEST-NONAUTH
                
                password: lir                
                """.stripIndent(true)
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(2, 1, 0)

        ack.errors.any { it.operation == "Create" && it.key == "[route6] 2001:400::/24AS252" }
        ack.errorMessagesFor("Create", "[route6] 2001:400::/24AS252") == [
                "Authorisation for [inet6num] ::/0 failed using \"mnt-by:\" not authenticated by: RIPE-NCC-HM-MNT",
                "Cannot create out of region route6 objects"
        ]

        queryObjectNotFound("-rGBT route6 2001:400::/24", "route6", "2001:400::/24")
    }

    def "create out of region route6, rs maintainer"() {
        when:
        def ack = syncUpdateWithResponse("""
                route6:         2001:400::/24
                descr:          A route
                origin:         AS252
                mnt-by:         LIR-MNT
                mnt-by:         RIPE-NCC-HM-MNT
                source:         TEST-NONAUTH
                
                password: hm                
                """.stripIndent(true)
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)

        queryObject("-rGBT route6 2001:400::/24", "route6", "2001:400::/24")
    }

    def "create out of region route6, using override"() {
        when:
        def ack = syncUpdateWithResponse("""
                route6:         2001:400::/24
                descr:          A route
                origin:         AS252
                mnt-by:         LIR-MNT
                source:         TEST-NONAUTH
                override:       denis,override1
                """.stripIndent(true)
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)

        queryObject("-rGBT route6 2001:400::/24", "route6", "2001:400::/24")
    }

    def "create in region route6, wrong source"() {
        when:
        def ack = syncUpdateWithResponse("""
                route6:         2001:600::/25
                descr:          A route
                origin:         AS252
                mnt-by:         LIR-MNT
                source:         TEST-NONAUTH
                
                password: lir                
                """.stripIndent(true)
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)

        ack.warningSuccessMessagesFor("Create", "[route6] 2001:600::/25AS252") == [
                "Supplied attribute 'source' has been replaced with a generated value"
        ]

        queryObject("-rGBT route6 2001:600::/25", "route6", "2001:600::/25")
    }

    def "create in region route6, rs maintainer, wrong source"() {
        when:
        def ack = syncUpdateWithResponse("""
                route6:         2001:600::/25
                descr:          A route
                origin:         AS252
                mnt-by:         LIR-MNT
                mnt-by:         RIPE-NCC-HM-MNT
                source:         TEST-NONAUTH
                
                password: hm                
                """.stripIndent(true)
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)

        ack.warningSuccessMessagesFor("Create", "[route6] 2001:600::/25AS252") == [
                "Supplied attribute 'source' has been replaced with a generated value"
        ]

        queryObject("-rGBT route6 2001:600::/25", "route6", "2001:600::/25")
    }

    def "create in region route6, using override, wrong source"() {
        when:
        def ack = syncUpdateWithResponse("""
                route6:         2001:600::/25
                descr:          A route
                origin:         AS252
                mnt-by:         LIR-MNT
                source:         TEST-NONAUTH
                override:       denis,override1
                """.stripIndent(true)
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 1)

        ack.warningSuccessMessagesFor("Create", "[route6] 2001:600::/25AS252") == [
                "Supplied attribute 'source' has been replaced with a generated value"
        ]

        queryObject("-rGBT route6 2001:600::/25", "route6", "2001:600::/25")
    }

    def "not create out of region route6, wrong source"() {
        when:
        def ack = syncUpdateWithResponse("""
                route6:         2001:400::/24
                descr:          A route
                origin:         AS252
                mnt-by:         LIR-MNT
                source:         TEST
                
                password: lir                
                """.stripIndent(true)
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(2, 2, 0)

        ack.errors.any { it.operation == "Create" && it.key == "[route6] 2001:400::/24AS252" }
        ack.errorMessagesFor("Create", "[route6] 2001:400::/24AS252") == [
                "Authorisation for [inet6num] ::/0 failed using \"mnt-by:\" not authenticated by: RIPE-NCC-HM-MNT",
                "Cannot create out of region route6 objects"
        ]
        ack.warningMessagesFor("Create", "[route6] 2001:400::/24AS252") == [
                "Supplied attribute 'source' has been replaced with a generated value"
        ]

        queryObjectNotFound("-rGBT route6 2001:400::/24", "route6", "2001:400::/24")
    }

    def "create out of region route6, rs maintainer, with wrong source"() {
        when:
        def ack = syncUpdateWithResponse("""
                route6:         2001:400::/24
                descr:          A route
                origin:         AS252
                mnt-by:         LIR-MNT
                mnt-by:         RIPE-NCC-HM-MNT
                source:         TEST
                
                password: hm                
                """.stripIndent(true)
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)

        ack.warningSuccessMessagesFor("Create", "[route6] 2001:400::/24AS252") == [
                "Supplied attribute 'source' has been replaced with a generated value"
        ]

        queryObject("-rGBT route6 2001:400::/24", "route6", "2001:400::/24")
    }

    def "create out of region route6, using override, with wrong source"() {
        when:
        def ack = syncUpdateWithResponse("""
                route6:         2001:400::/24
                descr:          A route
                origin:         AS252
                mnt-by:         LIR-MNT
                mnt-by:         RIPE-NCC-HM-MNT
                source:         TEST
                override:       denis,override1
                """.stripIndent(true)
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 1)

        ack.warningSuccessMessagesFor("Create", "[route6] 2001:400::/24AS252") == [
                "Supplied attribute 'source' has been replaced with a generated value",
                 "You cannot add or remove a RIPE NCC maintainer"
        ]

        queryObject("-rGBT route6 2001:400::/24", "route6", "2001:400::/24")
    }

    def "modify out of region route6"() {
        given:
        dbfixture(getTransient("OUT-OF-REGION-ROUTE6"))
        when:
        def ack = syncUpdateWithResponse("""
                route6:         2001:400::/24
                descr:          A route
                descr:          another
                origin:         AS252
                mnt-by:         LIR-MNT
                mnt-by:         RIPE-NCC-HM-MNT
                source:         TEST-NONAUTH
                
                password: lir                
                """.stripIndent(true)
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)

        queryObject("-rGBT route6 2001:400::/24", "route6", "2001:400::/24")
    }

    def "modify out of region route6, rs maintainer"() {
        given:
        dbfixture(getTransient("OUT-OF-REGION-ROUTE6"))
        when:
        def ack = syncUpdateWithResponse("""
                route6:         2001:400::/24
                descr:          A route
                descr:          another
                origin:         AS252
                mnt-by:         LIR-MNT
                mnt-by:         RIPE-NCC-HM-MNT
                source:         TEST-NONAUTH
                
                password: hm                
                """.stripIndent(true)
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)

        queryObject("-rGBT route6 2001:400::/24", "route6", "2001:400::/24")
    }

    def "modify out of region route6, using override"() {
        given:
        dbfixture(getTransient("OUT-OF-REGION-ROUTE6"))
        when:
        def ack = syncUpdateWithResponse("""
                route6:         2001:400::/24
                descr:          A route
                descr:          another
                origin:         AS252
                mnt-by:         LIR-MNT
                mnt-by:         RIPE-NCC-HM-MNT
                source:         TEST-NONAUTH
                override:       denis,override1
                """.stripIndent(true)
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)

        queryObject("-rGBT route6 2001:400::/24", "route6", "2001:400::/24")
    }

    def "modify in region route6, wrong source"() {
        given:
        dbfixture(getTransient("IN-REGION-ROUTE6"))
        when:
        def ack = syncUpdateWithResponse("""
                route6:         2001:600::/25
                descr:          A route
                descr:          another
                origin:         AS252
                mnt-by:         LIR-MNT
                mnt-by:         RIPE-NCC-HM-MNT
                source:         TEST-NONAUTH
                
                password: lir                
                """.stripIndent(true)
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.warningSuccessMessagesFor("Modify", "[route6] 2001:600::/25AS252") == [
                "Supplied attribute 'source' has been replaced with a generated value"
        ]

        queryObject("-rGBT route6 2001:600::/25", "route6", "2001:600::/25")
    }

    def "modify in region route6, rs maintainer, wrong source"() {
        given:
        dbfixture(getTransient("IN-REGION-ROUTE6"))
        when:
        def ack = syncUpdateWithResponse("""
                route6:         2001:600::/25
                descr:          A route
                descr:          another
                origin:         AS252
                mnt-by:         LIR-MNT
                mnt-by:         RIPE-NCC-HM-MNT
                source:         TEST-NONAUTH
                
                password: hm                
                """.stripIndent(true)
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.warningSuccessMessagesFor("Modify", "[route6] 2001:600::/25AS252") == [
                "Supplied attribute 'source' has been replaced with a generated value"
        ]

        queryObject("-rGBT route6 2001:600::/25", "route6", "2001:600::/25")
    }

    def "modify in region route6, using override, wrong source"() {
        given:
        dbfixture(getTransient("IN-REGION-ROUTE6"))
        when:
        def ack = syncUpdateWithResponse("""
                route6:         2001:600::/25
                descr:          A route
                descr:          another
                origin:         AS252
                mnt-by:         LIR-MNT
                mnt-by:         RIPE-NCC-HM-MNT
                source:         TEST-NONAUTH
                override:       denis,override1
                """.stripIndent(true)
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 1)
        ack.warningSuccessMessagesFor("Modify", "[route6] 2001:600::/25AS252") == [
                "Supplied attribute 'source' has been replaced with a generated value"
        ]

        queryObject("-rGBT route6 2001:600::/25", "route6", "2001:600::/25")
    }

    def "modify out of region route6, wrong source"() {
        given:
        dbfixture(getTransient("OUT-OF-REGION-ROUTE6"))
        when:
        def ack = syncUpdateWithResponse("""
                route6:         2001:400::/24
                descr:          A route
                descr:          another
                origin:         AS252
                mnt-by:         LIR-MNT
                mnt-by:         RIPE-NCC-HM-MNT
                source:         TEST
                
                password: lir                
                """.stripIndent(true)
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.warningSuccessMessagesFor("Modify", "[route6] 2001:400::/24") == [
                "Supplied attribute 'source' has been replaced with a generated value"
        ]

        queryObject("-rGBT route6 2001:400::/24", "route6", "2001:400::/24")
        restLookup(ObjectType.ROUTE6, "2001:400::/24AS252", "update");
    }

    def "modify out of region route6, rs maintainer, wrong source"() {
        given:
        dbfixture(getTransient("OUT-OF-REGION-ROUTE6"))
        when:
        def ack = syncUpdateWithResponse("""
                route6:         2001:400::/24
                descr:          A route
                descr:          another
                origin:         AS252
                mnt-by:         LIR-MNT
                mnt-by:         RIPE-NCC-HM-MNT
                source:         TEST
                
                password: hm                
                """.stripIndent(true)
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.warningSuccessMessagesFor("Modify", "[route6] 2001:400::/24AS252") == [
                "Supplied attribute 'source' has been replaced with a generated value"
        ]

        queryObject("-rGBT route6 2001:400::/24", "route6", "2001:400::/24")
    }

    def "modify out of region route6, using override, wrong source"() {
        given:
        dbfixture(getTransient("OUT-OF-REGION-ROUTE6"))
        when:
        def ack = syncUpdateWithResponse("""
                route6:         2001:400::/24
                descr:          A route
                descr:          another
                origin:         AS252
                mnt-by:         LIR-MNT
                mnt-by:         RIPE-NCC-HM-MNT
                source:         TEST
                override:       denis,override1
                """.stripIndent(true)
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 1)
        ack.warningSuccessMessagesFor("Modify", "[route6] 2001:400::/24AS252") == [
                "Supplied attribute 'source' has been replaced with a generated value"
        ]

        queryObject("-rGBT route6 2001:400::/24", "route6", "2001:400::/24")
    }

}
