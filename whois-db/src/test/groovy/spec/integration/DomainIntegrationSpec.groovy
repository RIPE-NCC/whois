package spec.integration

import net.ripe.db.whois.common.IntegrationTest
import spec.domain.SyncUpdate

@org.junit.experimental.categories.Category(IntegrationTest.class)
class DomainIntegrationSpec extends BaseSpec {

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
                "DOMAIN-MNT": """\
                    mntner: DOMAIN-MNT
                    admin-c: TEST-PN
                    mnt-by: DOMAIN-MNT
                    referral-by: DOMAIN-MNT
                    upd-to: dbtest@ripe.net
                    auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN6. # don't know, get a password that is known but not update
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
                "ENUM-MNT": """\
                    mntner:  RIPE-NCC-MNT
                    descr:   description
                    admin-c: TEST-PN
                    mnt-by:  RIPE-NCC-MNT
                    referral-by: RIPE-NCC-MNT
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
                "INET2": """\
                    inetnum: 10.0.0.0 - 10.0.0.255
                    netname: RIPE-NCC
                    descr: description
                    country: DK
                    admin-c: TEST-PN
                    tech-c: TEST-PN
                    status: SUB-ALLOCATED PA
                    mnt-by: TEST-MNT
                    mnt-domains: DOMAIN-MNT
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
                """,

                "IRT": """\
                    irt: irt-IRT1
                    address: Street 1
                    e-mail: test@ripe.net
                    admin-c: TEST-PN
                    tech-c: TEST-PN
                    auth: MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                    mnt-by: TEST-MNT
                    changed: test@ripe.net 20120505
                    source: TEST
                """
        ]
    }

    def "create enum domain without ENUM MAINTAINER"() {
      when:
        def insertResponse = syncUpdate new SyncUpdate(data: """\
                domain:          2.1.2.1.5.5.5.2.0.2.1.e164.arpa
                descr:           enum domain
                admin-c:         TEST-PN
                tech-c:          TEST-PN
                zone-c:          TEST-PN
                nserver:         ns.1.net
                mnt-by:          TEST-MNT
                changed:         test@ripe.net 20120505
                source:          TEST
                password:        update
                """.stripIndent())

      then:
        insertResponse.contains("***Error:   Creating enum domain requires administrative authorisation")
        dnsCheckedFor "2.1.2.1.5.5.5.2.0.2.1.e164.arpa"
    }

    def "create enum domain with ENUM MAINTAINER"() {
      when:
        def insertResponse = syncUpdate new SyncUpdate(data: """\
                domain:          2.1.2.1.5.5.5.2.0.2.1.e164.arpa
                descr:           enum domain
                admin-c:         TEST-PN
                tech-c:          TEST-PN
                zone-c:          TEST-PN
                nserver:         ns.1.net
                mnt-by:          RIPE-NCC-MNT
                changed:         test@ripe.net 20120505
                source:          TEST
                password:        update
                """.stripIndent())

      then:
        insertResponse.contains("Create SUCCEEDED: [domain] 2.1.2.1.5.5.5.2.0.2.1.e164.arpa")
        dnsCheckedFor "2.1.2.1.5.5.5.2.0.2.1.e164.arpa"
    }

    def "create ipv4 domain success"() {
      when:
        def insertResponse = syncUpdate new SyncUpdate(data: """\
                domain:          0.0.193.in-addr.arpa
                descr:           Test domain
                admin-c:         TEST-PN
                tech-c:          TEST-PN
                zone-c:          TEST-PN
                nserver:         ns.foo.net
                nserver:         ns.bar.net
                mnt-by:          TEST-MNT
                changed:         test@ripe.net 20120505
                source:          TEST
                password:        update
                """.stripIndent())

      then:
        insertResponse.contains("Create SUCCEEDED: [domain] 0.0.193.in-addr.arpa")
        dnsCheckedFor "0.0.193.in-addr.arpa"
    }

    def "create ipv4 domain parent auth failed"() {
      when:
        def insertResponse = syncUpdate new SyncUpdate(data: """\
                domain:          0.0.10.in-addr.arpa
                descr:           Test domain
                admin-c:         TEST-PN
                tech-c:          TEST-PN
                zone-c:          TEST-PN
                nserver:         ns.foo.net
                nserver:         ns.bar.net
                mnt-by:          TEST-MNT
                changed:         test@ripe.net 20120505
                source:          TEST
                password:        update
                """.stripIndent())

      then:
        insertResponse.contains("" +
                "***Error:   Authorisation for [inetnum] 10.0.0.0 - 10.0.0.255 failed\n" +
                "            using \"mnt-domains:\"\n" +
                "            not authenticated by: DOMAIN-MNT")

        dnsCheckedFor "0.0.10.in-addr.arpa"
    }

    def "create ipv4 domain parent exists"() {
      when:
        def insertResponse = syncUpdate new SyncUpdate(data: """\
                domain:          0.0.193.in-addr.arpa
                descr:           Test domain
                admin-c:         TEST-PN
                tech-c:          TEST-PN
                zone-c:          TEST-PN
                nserver:         ns.foo.net
                nserver:         ns.bar.net
                mnt-by:          TEST-MNT
                changed:         test@ripe.net 20120505
                source:          TEST
                password:        update
                """.stripIndent())

      then:
        insertResponse.contains("Create SUCCEEDED: [domain] 0.0.193.in-addr.arpa")

      when:
        def parentInsertResponse = syncUpdate new SyncUpdate(data: """\
                domain:          0.193.in-addr.arpa
                descr:           Test domain
                admin-c:         TEST-PN
                tech-c:          TEST-PN
                zone-c:          TEST-PN
                nserver:         ns.foo.net
                nserver:         ns.bar.net
                mnt-by:          TEST-MNT
                changed:         test@ripe.net 20120505
                source:          TEST
                password:        update
                """.stripIndent())

      then:
        parentInsertResponse.contains("***Error:   Existing more specific domain object found 193.0.0.0/24")
    }

    def "create ipv4 domain with nserver and glue"() {
      when:
        def insertResponse = syncUpdate new SyncUpdate(data: """\
                domain:          0.0.193.in-addr.arpa
                descr:           Test domain
                admin-c:         TEST-PN
                tech-c:          TEST-PN
                zone-c:          TEST-PN
                nserver:         ns.foo.net.0.0.193.in-addr.arpa. 10.0.0.0/32
                nserver:         ns.bar.net
                mnt-by:          TEST-MNT
                changed:         test@ripe.net 20120505
                source:          TEST
                password:        update
                """.stripIndent())

      then:
        insertResponse.contains("Create SUCCEEDED: [domain] 0.0.193.in-addr.arpa")
        insertResponse.contains("" +
                "***Info:    Value ns.foo.net.0.0.193.in-addr.arpa. 10.0.0.0/32 converted to\n" +
                "            ns.foo.net.0.0.193.in-addr.arpa 10.0.0.0")

        dnsCheckedFor "0.0.193.in-addr.arpa"
    }

    def "create ipv4 domain with nserver not ending with domain name and glue"() {
      when:
        def insertResponse = syncUpdate new SyncUpdate(data: """\
                domain:          0.0.193.in-addr.arpa
                descr:           Test domain
                admin-c:         TEST-PN
                tech-c:          TEST-PN
                zone-c:          TEST-PN
                nserver:         ns.foo.net 10.0.0.0
                nserver:         ns.bar.net
                mnt-by:          TEST-MNT
                changed:         test@ripe.net 20120505
                source:          TEST
                password:        update
                """.stripIndent())

      then:
        insertResponse.contains("Create FAILED: [domain] 0.0.193.in-addr.arpa")
        insertResponse.contains("***Error:   Glue records only allowed if hostname ends with 0.0.193.in-addr.arpa")

        dnsCheckedFor "0.0.193.in-addr.arpa"
    }

    def "create enum domain with nserver and glue"() {
      when:
        def insertResponse = syncUpdate new SyncUpdate(data: """\
                domain:          0.0.0.e164.arpa
                descr:           Test domain
                admin-c:         TEST-PN
                tech-c:          TEST-PN
                zone-c:          TEST-PN
                nserver:         n.s.0.0.0.e164.arpa 10.0.0.0
                mnt-by:          TEST-MNT
                changed:         test@ripe.net 20120505
                source:          TEST
                password:        update
                """.stripIndent())

      then:
        insertResponse.contains("***Error:   Enum domain has invalid glue 10.0.0.0/32")

        dnsCheckedFor "0.0.0.e164.arpa"
    }

    def "modify ipv4 domain success"() {
      when:
        def insertResponse = syncUpdate new SyncUpdate(data: """\
                domain:          0.0.193.in-addr.arpa
                descr:           Test domain
                admin-c:         TEST-PN
                tech-c:          TEST-PN
                zone-c:          TEST-PN
                nserver:         ns.foo.net
                nserver:         ns.bar.net
                mnt-by:          TEST-MNT
                changed:         test@ripe.net 20120505
                source:          TEST
                password:        update
                """.stripIndent())

      then:
        insertResponse.contains("Create SUCCEEDED: [domain] 0.0.193.in-addr.arpa")
        dnsCheckedFor "0.0.193.in-addr.arpa"

      when:
        def updateResponse = syncUpdate new SyncUpdate(data: """\
                domain:          0.0.193.in-addr.arpa
                descr:           Test domain
                admin-c:         TEST-PN
                tech-c:          TEST-PN
                zone-c:          TEST-PN
                nserver:         ns.foo.net.0.0.193.in-addr.arpa. 10.0.0.0/32
                nserver:         ns.bar.net
                mnt-by:          TEST-MNT
                changed:         test@ripe.net 20120505
                source:          TEST
                password:        update
                """.stripIndent())

      then:
        updateResponse.contains("Modify SUCCEEDED: [domain] 0.0.193.in-addr.arpa")
        updateResponse.contains("" +
                "***Info:    Value ns.foo.net.0.0.193.in-addr.arpa. 10.0.0.0/32 converted to\n" +
                "            ns.foo.net.0.0.193.in-addr.arpa 10.0.0.0")

        dnsCheckedFor "0.0.193.in-addr.arpa"
    }

    def "add ipv4 domain with parent should fail"() {
      when:
        def insertResponse = syncUpdate new SyncUpdate(data: """\
                domain:          0.0.193.in-addr.arpa
                descr:           Test domain
                admin-c:         TEST-PN
                tech-c:          TEST-PN
                zone-c:          TEST-PN
                nserver:         ns.foo.net
                nserver:         ns.bar.net
                mnt-by:          TEST-MNT
                changed:         test@ripe.net 20120505
                source:          TEST
                password:        update
                """.stripIndent())

      then:
        insertResponse.contains("Create SUCCEEDED: [domain] 0.0.193.in-addr.arpa")
        dnsCheckedFor "0.0.193.in-addr.arpa"

      when:
        def updateResponse = syncUpdate new SyncUpdate(data: """\
                domain:          0-127.0.0.193.in-addr.arpa
                descr:           Test domain
                admin-c:         TEST-PN
                tech-c:          TEST-PN
                zone-c:          TEST-PN
                nserver:         ns.foo.net.4.0.0.193.in-addr.arpa 10.0.0.0
                nserver:         ns.bar.net
                mnt-by:          TEST-MNT
                changed:         test@ripe.net 20120505
                source:          TEST
                password:        update
                """.stripIndent())

      then:
        updateResponse.contains("Create FAILED: [domain] 0-127.0.0.193.in-addr.arpa")
        updateResponse.contains("***Error:   Existing less specific domain object found 193.0.0.0/24")

        dnsCheckedFor "0-127.0.0.193.in-addr.arpa"
    }

    def "delete ipv4 domain"() {
      when:
        def insertResponse = syncUpdate new SyncUpdate(data: """\
                domain:          0.0.193.in-addr.arpa
                descr:           Test domain
                admin-c:         TEST-PN
                tech-c:          TEST-PN
                zone-c:          TEST-PN
                nserver:         ns.foo.net
                nserver:         ns.bar.net
                mnt-by:          TEST-MNT
                changed:         test@ripe.net 20120505
                source:          TEST
                password:        update
                """.stripIndent())

      then:
        insertResponse.contains("Create SUCCEEDED: [domain] 0.0.193.in-addr.arpa")
        dnsCheckedFor "0.0.193.in-addr.arpa"

      when:
        def updateResponse = syncUpdate new SyncUpdate(data: """\
                domain:          0.0.193.in-addr.arpa
                descr:           Test domain
                admin-c:         TEST-PN
                tech-c:          TEST-PN
                zone-c:          TEST-PN
                nserver:         ns.foo.net
                nserver:         ns.bar.net
                mnt-by:          TEST-MNT
                changed:         test@ripe.net 20120505
                source:          TEST
                password:        update
                delete:          reason
                """.stripIndent())

      then:
        updateResponse.contains("Delete SUCCEEDED: [domain] 0.0.193.in-addr.arpa")
        !dnsCheckedFor("0.0.193.in-addr.arpa")
    }

    def "create and modify ipv4 domain check number dns check count"() {
      when:
        def insertResponse = syncUpdate new SyncUpdate(data: """\
                domain:          0.0.193.in-addr.arpa
                descr:           Test domain
                admin-c:         TEST-PN
                tech-c:          TEST-PN
                zone-c:          TEST-PN
                nserver:         ns.foo.net
                nserver:         ns.bar.net
                mnt-by:          TEST-MNT
                changed:         test@ripe.net 20120505
                source:          TEST

                domain:          0.0.193.in-addr.arpa
                descr:           Test domain
                admin-c:         TEST-PN
                tech-c:          TEST-PN
                zone-c:          TEST-PN
                nserver:         ns.foo.net
                nserver:         ns.bar.net
                mnt-by:          TEST-MNT
                changed:         test@ripe.net 20120505
                changed:         test@ripe.net 20120606
                source:          TEST

                password:        update
                """.stripIndent())

      then:
        insertResponse.contains("Create SUCCEEDED: [domain] 0.0.193.in-addr.arpa")
        insertResponse.contains("Modify SUCCEEDED: [domain] 0.0.193.in-addr.arpa")
        dnsCheckedFor("0.0.193.in-addr.arpa")
    }

    def "create and noop ipv4 domain check number dns check count"() {
      when:
        def insertResponse = syncUpdate new SyncUpdate(data: """\
                domain:          0.0.193.in-addr.arpa
                descr:           Test domain
                admin-c:         TEST-PN
                tech-c:          TEST-PN
                zone-c:          TEST-PN
                nserver:         ns.foo.net
                nserver:         ns.bar.net
                mnt-by:          TEST-MNT
                changed:         test@ripe.net 20120505
                source:          TEST

                domain:          0.0.193.in-addr.arpa
                descr:           Test domain
                admin-c:         TEST-PN
                tech-c:          TEST-PN
                zone-c:          TEST-PN
                nserver:         ns.foo.net
                nserver:         ns.bar.net
                mnt-by:          TEST-MNT
                changed:         test@ripe.net 20120505
                source:          TEST

                password:        update
                """.stripIndent())

      then:
        insertResponse.contains("Create SUCCEEDED: [domain] 0.0.193.in-addr.arpa")
        insertResponse.contains("No operation: [domain] 0.0.193.in-addr.arpa")
        dnsCheckedFor("0.0.193.in-addr.arpa")
    }

    def "create multiple ipv4 domain objects check number dns check count"() {
        when:
        def insertResponse = syncUpdate new SyncUpdate(data: """\
                domain:          0.0.193.in-addr.arpa
                descr:           Test domain
                admin-c:         TEST-PN
                tech-c:          TEST-PN
                zone-c:          TEST-PN
                nserver:         ns.foo.net
                nserver:         ns.bar.net
                mnt-by:          TEST-MNT
                changed:         test@ripe.net 20120505
                source:          TEST

                domain:          0.0.194.in-addr.arpa
                descr:           Test domain
                admin-c:         TEST-PN
                tech-c:          TEST-PN
                zone-c:          TEST-PN
                nserver:         ns.foo.net
                nserver:         ns.bar.net
                mnt-by:          TEST-MNT
                changed:         test@ripe.net 20120505
                source:          TEST

                person:          Some person
                address:         Somewhere
                phone:           +44 282 411141
                fax-no:          +44 282 411140
                nic-hdl:         AUTO-1
                mnt-by:          TEST-MNT
                changed:         dbtest@ripe.net 20120101
                source:          TEST

                domain:          0.0.195.in-addr.arpa
                descr:           Test domain
                admin-c:         TEST-PN
                tech-c:          TEST-PN
                zone-c:          TEST-PN
                nserver:         ns.foo.net
                nserver:         ns.bar.net
                mnt-by:          TEST-MNT
                changed:         test@ripe.net 20120505
                source:          TEST

                domain:          0.0.196.in-addr.arpa
                descr:           Test domain
                admin-c:         TEST-PN
                tech-c:          TEST-PN
                zone-c:          TEST-PN
                nserver:         ns.foo.net
                nserver:         ns.bar.net
                mnt-by:          TEST-MNT
                changed:         test@ripe.net 20120505
                source:          TEST

                domain:          0.0.197.in-addr.arpa
                descr:           Test domain
                admin-c:         TEST-PN
                tech-c:          TEST-PN
                zone-c:          TEST-PN
                nserver:         ns.foo.net
                nserver:         ns.bar.net
                mnt-by:          TEST-MNT
                changed:         test@ripe.net 20120505
                source:          TEST

                domain:          0.0.198.in-addr.arpa
                descr:           Test domain
                admin-c:         TEST-PN
                tech-c:          TEST-PN
                zone-c:          TEST-PN
                nserver:         ns.foo.net
                nserver:         ns.bar.net
                mnt-by:          TEST-MNT
                changed:         test@ripe.net 20120505
                source:          TEST

                domain:          0.0.199.in-addr.arpa
                descr:           Test domain
                admin-c:         TEST-PN
                tech-c:          TEST-PN
                zone-c:          TEST-PN
                nserver:         ns.foo.net
                nserver:         ns.bar.net
                mnt-by:          TEST-MNT
                changed:         test@ripe.net 20120505
                source:          TEST

                domain:          0.0.200.in-addr.arpa
                descr:           Test domain
                admin-c:         TEST-PN
                tech-c:          TEST-PN
                zone-c:          TEST-PN
                nserver:         ns.foo.net
                nserver:         ns.bar.net
                mnt-by:          TEST-MNT
                changed:         test@ripe.net 20120505
                source:          TEST

                domain:          0.0.201.in-addr.arpa
                descr:           Test domain
                admin-c:         TEST-PN
                tech-c:          TEST-PN
                zone-c:          TEST-PN
                nserver:         ns.foo.net
                nserver:         ns.bar.net
                mnt-by:          TEST-MNT
                changed:         test@ripe.net 20120505
                source:          TEST

                domain:          0.0.202.in-addr.arpa
                descr:           Test domain
                admin-c:         TEST-PN
                tech-c:          TEST-PN
                zone-c:          TEST-PN
                nserver:         ns.foo.net
                nserver:         ns.bar.net
                mnt-by:          TEST-MNT
                changed:         test@ripe.net 20120505
                source:          TEST

                domain:          0.0.203.in-addr.arpa
                descr:           Test domain
                admin-c:         TEST-PN
                tech-c:          TEST-PN
                zone-c:          TEST-PN
                nserver:         ns.foo.net
                nserver:         ns.bar.net
                mnt-by:          TEST-MNT
                changed:         test@ripe.net 20120505
                source:          TEST

                domain:          0.0.204.in-addr.arpa
                descr:           Test domain
                admin-c:         TEST-PN
                tech-c:          TEST-PN
                zone-c:          TEST-PN
                nserver:         ns.foo.net
                nserver:         ns.bar.net
                mnt-by:          TEST-MNT
                changed:         test@ripe.net 20120505
                source:          TEST

                domain:          0.0.205.in-addr.arpa
                descr:           Test domain
                admin-c:         TEST-PN
                tech-c:          TEST-PN
                zone-c:          TEST-PN
                nserver:         ns.foo.net
                nserver:         ns.bar.net
                mnt-by:          TEST-MNT
                changed:         test@ripe.net 20120505
                source:          TEST

                password:        update
                """.stripIndent())

        then:
        println "insertResponse : $insertResponse"
        insertResponse.contains("Create SUCCEEDED: [domain] 0.0.193.in-addr.arpa")
        insertResponse.contains("Create SUCCEEDED: [domain] 0.0.194.in-addr.arpa")
        insertResponse.contains("Create SUCCEEDED: [person] SP1-TEST   Some person")
        insertResponse.contains("Create SUCCEEDED: [domain] 0.0.195.in-addr.arpa")
        insertResponse.contains("Create SUCCEEDED: [domain] 0.0.196.in-addr.arpa")
        insertResponse.contains("Create SUCCEEDED: [domain] 0.0.198.in-addr.arpa")
        insertResponse.contains("Create SUCCEEDED: [domain] 0.0.199.in-addr.arpa")
        insertResponse.contains("Create SUCCEEDED: [domain] 0.0.200.in-addr.arpa")
        insertResponse.contains("Create SUCCEEDED: [domain] 0.0.201.in-addr.arpa")
        insertResponse.contains("Create SUCCEEDED: [domain] 0.0.202.in-addr.arpa")
        insertResponse.contains("Create SUCCEEDED: [domain] 0.0.203.in-addr.arpa")
        insertResponse.contains("Create SUCCEEDED: [domain] 0.0.204.in-addr.arpa")
        insertResponse.contains("Create SUCCEEDED: [domain] 0.0.205.in-addr.arpa")
        dnsCheckedFor("0.0.193.in-addr.arpa")
        dnsCheckedFor("0.0.194.in-addr.arpa")
        dnsCheckedFor("0.0.195.in-addr.arpa")
        dnsCheckedFor("0.0.196.in-addr.arpa")
        dnsCheckedFor("0.0.197.in-addr.arpa")
        dnsCheckedFor("0.0.198.in-addr.arpa")
        dnsCheckedFor("0.0.199.in-addr.arpa")
        dnsCheckedFor("0.0.200.in-addr.arpa")
        dnsCheckedFor("0.0.201.in-addr.arpa")
        dnsCheckedFor("0.0.202.in-addr.arpa")
        dnsCheckedFor("0.0.203.in-addr.arpa")
        dnsCheckedFor("0.0.204.in-addr.arpa")
        dnsCheckedFor("0.0.205.in-addr.arpa")
    }

    def "create ipv4 domain check number dns check count after reorder"() {
      when:
        def insertResponse = syncUpdate new SyncUpdate(data: """\
                domain:          0.0.193.in-addr.arpa
                descr:           Test domain
                admin-c:         TEST-PN
                tech-c:          TEST-PN
                zone-c:          AUTO-1
                nserver:         ns.foo.net
                nserver:         ns.bar.net
                mnt-by:          TEST-MNT
                changed:         test@ripe.net 20120505
                source:          TEST

                person:          Some person
                address:         Somewhere
                phone:           +44 282 411141
                fax-no:          +44 282 411140
                nic-hdl:         AUTO-1
                mnt-by:          TEST-MNT
                changed:         dbtest@ripe.net 20120101
                source:          TEST

                password:        update
                """.stripIndent())

      then:
        insertResponse.contains("Create SUCCEEDED: [person] SP1-TEST   Some person")
        insertResponse.contains("Create SUCCEEDED: [domain] 0.0.193.in-addr.arpa")
        dnsCheckedFor("0.0.193.in-addr.arpa")
    }

    def "create ipv4 domain check skipped for override"() {
      when:
        def insertResponse = syncUpdate new SyncUpdate(data: """\
                domain:          0.0.193.in-addr.arpa
                descr:           Test domain
                admin-c:         TEST-PN
                tech-c:          TEST-PN
                zone-c:          TEST-PN
                nserver:         ns.foo.net
                nserver:         ns.bar.net
                mnt-by:          TEST-MNT
                changed:         test@ripe.net 20120505
                source:          TEST
                override:        override1
                """.stripIndent())

      then:
        insertResponse.contains("Create SUCCEEDED: [domain] 0.0.193.in-addr.arpa")
        !dnsCheckedFor("0.0.193.in-addr.arpa")
    }
}
