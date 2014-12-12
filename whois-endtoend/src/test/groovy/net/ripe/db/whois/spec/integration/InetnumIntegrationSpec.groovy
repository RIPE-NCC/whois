package net.ripe.db.whois.spec.integration

import net.ripe.db.whois.common.IntegrationTest
import net.ripe.db.whois.spec.domain.SyncUpdate

@org.junit.experimental.categories.Category(IntegrationTest.class)
class InetnumIntegrationSpec extends BaseWhoisSourceSpec {

  @Override
  Map<String, String> getFixtures() {
    return [
            "TEST-PN"  : """\
                    person: some one
                    nic-hdl: TEST-PN
                    mnt-by: TEST-MNT
                    changed: ripe@test.net
                    source: TEST
                """,
            "TEST-MNT" : """\
                    mntner: TEST-MNT
                    admin-c: TEST-PN
                    mnt-by: TEST-MNT
                    referral-bxWy: TEST-MNT
                    upd-to: dbtest@ripe.net
                    auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                    source: TEST
                """,
            "TEST2-MNT": """\
                    mntner: TEST2-MNT
                    admin-c: TEST-PN
                    mnt-by: TEST2-MNT
                    referral-by: TEST2-MNT
                    upd-to: dbtest@ripe.net
                    auth:    MD5-PW \$1\$/7f2XnzQ\$p5ddbI7SXq4z4yNrObFS/0 # emptypassword
                    source: TEST
                """,
            "PWR-MNT"  : """\
                    mntner:  RIPE-NCC-HM-MNT
                    descr:   description
                    admin-c: TEST-PN
                    mnt-by:  RIPE-NCC-HM-MNT
                    referral-by: RIPE-NCC-HM-MNT
                    upd-to:  dbtest@ripe.net
                    auth:    MD5-PW \$1\$mV2gSZtj\$1oVwjZr0ecFZQHsNbw2Ss.  #hm
                    changed: dbtest@ripe.net 20120707
                    source:  TEST
                """,
            "END-MNT"  : """\
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
            "LEGACY-MNT"  : """\
                    mntner:  RIPE-NCC-LEGACY-MNT
                    descr:   description
                    admin-c: TEST-PN
                    mnt-by:  RIPE-NCC-LEGACY-MNT
                    referral-by: RIPE-NCC-LEGACY-MNT
                    upd-to:  dbtest@ripe.net
                    auth:    MD5-PW \$1\$gTs46J2Z\$.iohp.IUDhNAMj7evxnFS1   # legacy
                    changed: dbtest@ripe.net 20120707
                    source:  TEST
                """,
            "ORG1"     : """\
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
            "ORG2"     : """\
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
            "ORG3"     : """\
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
            "ORG4"     : """\
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
            "ORG5"     : """\
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
            "ORG6"     : """\
                    organisation: ORG-TOL6-TEST
                    org-name:     Test Organisation Ltd
                    org-type:     LIR
                    descr:        test org
                    address:      street 5
                    e-mail:       org1@test.com
                    mnt-ref:      TEST2-MNT
                    mnt-by:       RIPE-NCC-HM-MNT
                    changed:      dbtest@ripe.net 20120505
                    source:       TEST
                """,
            "INET1"    : """\
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
            "INET2"    : """\
                    inetnum: 194.0.0.0 - 194.255.255.255
                    netname: TEST-NET
                    descr: description
                    country: NL
                    admin-c: TEST-PN
                    tech-c: TEST-PN
                    status: ALLOCATED PA
                    mnt-by: RIPE-NCC-HM-MNT
                    mnt-lower: TEST-MNT
                    changed: ripe@test.net 20120505
                    source: TEST
                """,
            "INET3"    : """\
                    inetnum: 195.0.0.0 - 195.255.255.255
                    netname: TEST-NET
                    descr: description
                    country: NL
                    admin-c: TEST-PN
                    tech-c: TEST-PN
                    status: ALLOCATED PI
                    mnt-by: TEST-MNT
                    mnt-lower: TEST-MNT
                    changed: ripe@test.net 20120505
                    source: TEST
                """,
            "INETROOT" : """\
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

            "IRT"      : """\
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

  def "delete inetnum"() {
    given:
      def insertResponse = syncUpdate(new SyncUpdate(data: """\
                inetnum: 193.0.0.0 - 193.0.0.255
                netname: RIPE-NCC
                descr: description
                country: DK
                admin-c: TEST-PN
                tech-c: TEST-PN
                status: SUB-ALLOCATED PA
                mnt-by: TEST-MNT
                org: ORG-TOL2-TEST
                changed: ripe@test.net 20120505
                source: TEST
                password:update
                """.stripIndent()))
    expect:
      insertResponse =~ /SUCCESS/
    when:
      def delete = new SyncUpdate(data: """\
                inetnum: 193.0.0.0 - 193.0.0.255
                netname: RIPE-NCC
                descr: description
                country: DK
                admin-c: TEST-PN
                tech-c: TEST-PN
                status: SUB-ALLOCATED PA
                mnt-by: TEST-MNT
                org: ORG-TOL2-TEST
                changed: ripe@test.net 20120505
                source: TEST
                delete:yes
                password:update
                """.stripIndent())
    then:
      def response = syncUpdate delete

    then:
      response =~ /SUCCESS/
  }

  def "delete inetnum with no status attribute"() {
    given:
      databaseHelper.addObject("""\
                inetnum:    192.168.0.0 - 192.168.0.255
                netname:    RIPE-NCC
                descr:      description
                country:    NL
                admin-c:    TEST-PN
                tech-c:     TEST-PN
                mnt-by:     TEST-MNT
                changed:    ripe@test.net 20120505
                source:     TEST
                """.stripIndent())
      whoisFixture.reloadTrees()
    when:
      def response = syncUpdate(new SyncUpdate(data: """\
                inetnum:    192.168.0.0 - 192.168.0.255
                netname:    RIPE-NCC
                descr:      description
                country:    NL
                admin-c:    TEST-PN
                tech-c:     TEST-PN
                mnt-by:     TEST-MNT
                changed:    ripe@test.net 20120505
                source:     TEST
                delete:     yes
                password:   update
                """.stripIndent()))
    then:
      response =~ /SUCCESS/
  }

  def "modify inetnum dash notation multiple spaces"() {
    when:
      def response = syncUpdate new SyncUpdate(data: """\
                inetnum: 193.0.0.0   -  193.0.0.255
                netname: RIPE-NCC
                descr: description
                country: DK
                admin-c: TEST-PN
                tech-c: TEST-PN
                status: SUB-ALLOCATED PA
                mnt-by: TEST-MNT
                changed: ripe@test.net 20120505
                source: TEST
                password:update
                """.stripIndent())
    then:
      response =~ /No operation: \[inetnum\] 193.0.0.0 - 193.0.0.255/
  }

  def "modify inetnum newline in primary key"() {
    when:
      def response = syncUpdate new SyncUpdate(data: """\
                inetnum: 193.0.0.0
                 -  193.0.0.255
                netname: RIPE-NCC
                descr: description
                country: DK
                admin-c: TEST-PN
                tech-c: TEST-PN
                status: SUB-ALLOCATED PA
                mnt-by: TEST-MNT
                changed: ripe@test.net 20120505
                source: TEST
                password:update
                """.stripIndent())
    then:
      response =~ /Continuation lines are not allowed here and have been removed/
      response =~ /No operation: \[inetnum\] 193.0.0.0 - 193.0.0.255/
  }

  def "create CIDR notation"() {
    when:
      def response = syncUpdate(new SyncUpdate(data: """\
                    inetnum: 192.0.0.0/24
                    netname: RIPE-NCC
                    descr: description
                    country: DK
                    admin-c: TEST-PN
                    tech-c: TEST-PN
                    status: ALLOCATED PI
                    mnt-by: RIPE-NCC-HM-MNT
                    org: ORG-TOL5-TEST
                    changed: ripe@test.net 20120505
                    source: TEST
                    password: update
                    password: hm
                    """.stripIndent()))
    then:
      response =~ /Create SUCCEEDED: \[inetnum\] 192.0.0.0 - 192.0.0.255/
      response =~ /\*\*\*Info:    Value 192.0.0.0\/24 converted to 192.0.0.0 - 192.0.0.255/
  }

  def "modify status ALLOCATED PI has reference to RIR organisation"() {
    given:
      def insertResponse = syncUpdate(new SyncUpdate(data: """\
                            inetnum: 192.0.0.0 - 192.0.0.255
                            netname: RIPE-NCC
                            descr: description
                            country: DK
                            admin-c: TEST-PN
                            tech-c: TEST-PN
                            changed: ripe@test.net 20120505
                            org: ORG-TOL1-TEST
                            status: ALLOCATED PI
                            mnt-by:RIPE-NCC-HM-MNT
                            source: TEST
                            password: hm
                            password: update
                        """.stripIndent()))
    when:
      insertResponse =~ /SUCCESS/
    then:
      def response = syncUpdate new SyncUpdate(data: """\
                    inetnum: 192.0.0.0 - 192.0.0.255
                    netname: RIPE-NCC
                    descr: description
                    country: DK
                    admin-c: TEST-PN
                    tech-c: TEST-PN
                    mnt-by: RIPE-NCC-HM-MNT
                    changed: ripe@test.net 20120505
                    org: ORG-TOL5-TEST
                    status: ALLOCATED PI
                    source: TEST
                    password: hm
                    password: update
                """.stripIndent())
    then:
      response =~ /SUCCESS/
      response =~ /Modify SUCCEEDED: \[inetnum\] 192.0.0.0 - 192.0.0.255/
  }

  def "create status ALLOCATED PI no alloc maintainer"() {
    when:
      def insertResponse = syncUpdate(new SyncUpdate(data: """\
            inetnum: 192.0.0.0 - 192.0.0.255
            netname: RIPE-NCC
            descr: description
            country: DK
            admin-c: TEST-PN
            tech-c: TEST-PN
            status: ALLOCATED PI
            mnt-by: TEST-MNT
            org: ORG-TOL1-TEST
            changed: ripe@test.net 20120505
            source: TEST
            password: update
            password: hm
        """.stripIndent()))
    then:
      insertResponse =~ /SUCCESS/
      insertResponse =~ /Create SUCCEEDED: \[inetnum\] 192.0.0.0 - 192.0.0.255/
  }

  def "modify status ALLOCATED PI override"() {
    when:
      def insertResponse = syncUpdate(new SyncUpdate(data: """\
            inetnum: 192.0.0.0 - 192.0.0.255
            netname: RIPE-NCC
            descr: description
            country: DK
            admin-c: TEST-PN
            tech-c: TEST-PN
            status: ALLOCATED PI
            mnt-by: TEST-MNT
            org: ORG-TOL1-TEST
            changed: ripe@test.net 20120505
            source: TEST
            override:denis,override1
        """.stripIndent()))
    then:
      insertResponse =~ /Create SUCCEEDED: \[inetnum\] 192.0.0.0 - 192.0.0.255/
  }

  def "modify status ALLOCATED PI has reference to non-RIR organisation"() {
    given:
      def insertResponse = syncUpdate(new SyncUpdate(data: """\
            inetnum: 192.0.0.0 - 192.0.0.255
            netname: RIPE-NCC
            descr: description
            country: DK
            admin-c: TEST-PN
            tech-c: TEST-PN
            status: ALLOCATED PI
            mnt-by: RIPE-NCC-HM-MNT
            org: ORG-TOL1-TEST
            changed: ripe@test.net 20120505
            source: TEST
            password: update
            password: hm
        """.stripIndent()))
    expect:
      insertResponse =~ /SUCCESS/
    when:
      def response = syncUpdate new SyncUpdate(data: """\
            inetnum: 192.0.0.0 - 192.0.0.255
            netname: RIPE-NCC
            descr: description
            country: DK
            admin-c: TEST-PN
            tech-c: TEST-PN
            status: ALLOCATED PI
            mnt-by: RIPE-NCC-HM-MNT
            org: ORG-TOL2-TEST
            changed: ripe@test.net 20120505
            source: TEST
            password: hm""".stripIndent())
    then:
      response =~ /FAIL/
      response =~ /Referenced organisation has wrong "org-type"/
      response =~ /Allowed values are \[IANA, RIR, LIR\]/
  }

  def "modify status ALLOCATED PI has reference to non-RIR organisation with override"() {
    when:
      def response = syncUpdate(new SyncUpdate(data: """\
            inetnum: 192.0.0.0 - 192.0.0.255
            netname: RIPE-NCC
            descr: description
            country: DK
            admin-c: TEST-PN
            tech-c: TEST-PN
            status: ALLOCATED PI
            mnt-by: RIPE-NCC-HM-MNT
            org: ORG-TOL1-TEST
            changed: ripe@test.net 20120505
            source: TEST

            password: update
            password: hm

            inetnum: 192.0.0.0 - 192.0.0.255
            netname: RIPE-NCC
            descr: description
            country: DK
            admin-c: TEST-PN
            tech-c: TEST-PN
            status: ALLOCATED PI
            mnt-by: RIPE-NCC-HM-MNT
            org: ORG-TOL2-TEST
            changed: ripe@test.net 20120505
            source: TEST
            override:denis,override1""".stripIndent()))
    then:
      response =~ /Create SUCCEEDED: \[inetnum\] 192.0.0.0 - 192.0.0.255/
      response =~ /Modify SUCCEEDED: \[inetnum\] 192.0.0.0 - 192.0.0.255/
  }

  def "modify status ALLOCATED PI has no reference to organisation"() {
    given:
      def insertResponse = syncUpdate(new SyncUpdate(data: """\
                            inetnum: 192.0.0.0 - 192.0.0.255
                            netname: RIPE-NCC
                            descr: description
                            country: DK
                            admin-c: TEST-PN
                            tech-c: TEST-PN
                            status: ALLOCATED PI
                            org: ORG-TOL1-TEST
                            mnt-by: RIPE-NCC-HM-MNT
                            changed: ripe@test.net 20120505
                            source: TEST
                            password: update
                            password: hm
                        """.stripIndent()))
    expect:
      insertResponse =~ /SUCCESS/
    when:
      def response = syncUpdate new SyncUpdate(data: ("""\
                            inetnum: 192.0.0.0 - 192.0.0.255
                            netname: RIPE-NCC
                            descr: description
                            country: DK
                            admin-c: TEST-PN
                            tech-c: TEST-PN
                            status: ALLOCATED PI
                            mnt-by: RIPE-NCC-HM-MNT
                            changed: ripe@test.net 20120505
                            source: TEST
                            password: hm
                        """.stripIndent()))
    then:
      response =~ /FAIL/
      response =~ /Missing required "org:" attribute/
  }

  def "status EARLY-REGISTRATION is allowed for an RS maintainer"() {
    when:
      def insertResponse = syncUpdate(new SyncUpdate(data: """\
                    inetnum: 10.0.0.0 - 10.0.0.255
                    netname: RIPE-NCC
                    descr: description
                    country: DK
                    admin-c: TEST-PN
                    tech-c: TEST-PN
                    status: EARLY-REGISTRATION
                    mnt-by: RIPE-NCC-HM-MNT
                    org: ORG-TOL5-TEST
                    changed: ripe@test.net 20120505
                    source: TEST
                    password: update
                    password: hm
                    """.stripIndent()))
    then:
      insertResponse =~ /Create SUCCEEDED: \[inetnum\] 10.0.0.0 - 10.0.0.255/
  }

  def "status EARLY-REGISTRATION is not allowed for regular maintainer"() {
    when:
      def insertResponse = syncUpdate(new SyncUpdate(data: """\
                    inetnum: 10.0.0.0 - 10.0.0.255
                    netname: RIPE-NCC
                    descr: description
                    country: DK
                    admin-c: TEST-PN
                    tech-c: TEST-PN
                    status: EARLY-REGISTRATION
                    mnt-by: TEST2-MNT
                    org: ORG-TOL6-TEST
                    changed: ripe@test.net 20120505
                    source: TEST
                    password:emptypassword
                    """.stripIndent()))
    then:
      insertResponse =~ /Status EARLY-REGISTRATION can only be created by the database\n\s+administrator/
  }

  def "status EARLY-REGISTRATION with override"() {
    when:
      def insertResponse = syncUpdate(new SyncUpdate(data: """\
                    inetnum: 10.0.0.0 - 10.0.0.255
                    netname: RIPE-NCC
                    descr: description
                    country: DK
                    admin-c: TEST-PN
                    tech-c: TEST-PN
                    status: EARLY-REGISTRATION
                    mnt-by: RIPE-NCC-HM-MNT
                    org: ORG-TOL5-TEST
                    changed: ripe@test.net 20120505
                    source: TEST
                    override:denis,override1
                    """.stripIndent()))
    then:
      insertResponse =~ /Create SUCCEEDED: \[inetnum\] 10.0.0.0 - 10.0.0.255/
  }

  def "modify status ASSIGNED PA does not reference organisation of type LIR or OTHER"() {
    given:
      def insertResponse = syncUpdate(new SyncUpdate(data: """\
                    inetnum: 10.0.0.0 - 10.0.0.255
                    netname: RIPE-NCC
                    descr: description
                    country: DK
                    admin-c: TEST-PN
                    tech-c: TEST-PN
                    status: ASSIGNED PA
                    org: ORG-TOL2-TEST
                    mnt-by: RIPE-NCC-HM-MNT
                    changed: ripe@test.net 20120505
                    source: TEST
                    password: hm
                    password: update
                """.stripIndent()))
    expect:
      insertResponse =~ /SUCCESS/
    when:
      def response = syncUpdate new SyncUpdate(data: """\
                    inetnum: 10.0.0.0 - 10.0.0.255
                    netname: RIPE-NCC
                    descr: description
                    country: DK
                    admin-c: TEST-PN
                    tech-c: TEST-PN
                    status: ASSIGNED PA
                    org: ORG-TOL1-TEST
                    mnt-by: RIPE-NCC-HM-MNT
                    changed: ripe@test.net 20120505
                    source: TEST
                    password: hm
                """.stripIndent())
    then:
      response =~ /FAIL/
      response =~ /Referenced organisation has wrong "org-type"./
      response =~ /Allowed values are \[LIR, OTHER\]/
  }

  def "modify status ASSIGNED PA does not reference organisation of type LIR or OTHER with override"() {
    when:
      def response = syncUpdate(new SyncUpdate(data: """\
                    inetnum: 10.0.0.0 - 10.0.0.255
                    netname: RIPE-NCC
                    descr: description
                    country: DK
                    admin-c: TEST-PN
                    tech-c: TEST-PN
                    status: ASSIGNED PA
                    org: ORG-TOL2-TEST
                    mnt-by: RIPE-NCC-HM-MNT
                    changed: ripe@test.net 20120505
                    source: TEST
                    password: hm
                    password: update

                    inetnum: 10.0.0.0 - 10.0.0.255
                    netname: RIPE-NCC
                    descr: description
                    country: DK
                    admin-c: TEST-PN
                    tech-c: TEST-PN
                    status: ASSIGNED PA
                    org: ORG-TOL1-TEST
                    mnt-by: RIPE-NCC-HM-MNT
                    changed: ripe@test.net 20120505
                    source: TEST
                    override:denis,override1
                """.stripIndent()))
    then:
      response =~ /Create SUCCEEDED: \[inetnum\] 10.0.0.0 - 10.0.0.255/
      response =~ /Modify SUCCEEDED: \[inetnum\] 10.0.0.0 - 10.0.0.255/
  }

  def "modify status ASSIGNED PA does not reference any organisation"() {
    given:
      def insertResponse = syncUpdate(new SyncUpdate(data: """\
                    inetnum: 10.0.0.0 - 10.0.0.255
                    netname: RIPE-NCC
                    descr: description
                    country: DK
                    admin-c: TEST-PN
                    tech-c: TEST-PN
                    status: ASSIGNED PA
                    org: ORG-TOL2-TEST
                    mnt-by: RIPE-NCC-HM-MNT
                    changed: ripe@test.net 20120505
                    source: TEST
                    password: hm
                    password: update
                """.stripIndent()))
    expect:
      insertResponse =~ /SUCCESS/
    when:
      def response = syncUpdate new SyncUpdate(data: """\
                    inetnum: 10.0.0.0 - 10.0.0.255
                    netname: RIPE-NCC
                    descr: description
                    country: DK
                    admin-c: TEST-PN
                    tech-c: TEST-PN
                    status: ASSIGNED PA
                    mnt-by: RIPE-NCC-HM-MNT
                    changed: ripe@test.net 20120505
                    source: TEST
                    password: hm
                """.stripIndent())
    then:
      response =~ /SUCCESS/
  }

  def "modify status ASSIGNED PI has reference to organisation not of type DIRECT_ASSIGNMENT"() {
    given:
      def insertResponse = syncUpdate(new SyncUpdate(data: """\
                    inetnum: 192.0.0.0 - 192.0.0.255
                    netname: RIPE-NCC
                    descr: description
                    country: DK
                    admin-c: TEST-PN
                    tech-c: TEST-PN
                    status: ASSIGNED PI
                    mnt-by:RIPE-NCC-END-MNT
                    mnt-by: TEST-MNT
                    changed: ripe@test.net 20120505
                    org: ORG-TOL1-TEST
                    source: TEST
                    password:update
                    password:hm
                """.stripIndent()))
    expect:
      insertResponse =~ /SUCCESS/
    when:
      def response = syncUpdate(new SyncUpdate(data: """\
                    inetnum: 192.0.0.0 - 192.0.0.255
                    netname: RIPE-NCC
                    descr: description
                    country: DK
                    admin-c: TEST-PN
                    tech-c: TEST-PN
                    status: ASSIGNED PI
                    mnt-by: TEST-MNT
                    changed: ripe@test.net 20120505
                    org: ORG-TOL4-TEST
                    source: TEST
                    password:update
                """.stripIndent()))
    then:
      response =~ /FAIL/
      response =~ /Referenced organisation has wrong "org-type".
            Allowed values are \[RIR, LIR, OTHER\]/
  }

  def "modify status ASSIGNED PI has reference to organisation not of type DIRECT_ASSIGNMENT with override"() {
    when:
      def response = syncUpdate(new SyncUpdate(data: """\
                    inetnum: 192.0.0.0 - 192.0.0.255
                    netname: RIPE-NCC
                    descr: description
                    country: DK
                    admin-c: TEST-PN
                    tech-c: TEST-PN
                    status: ASSIGNED PI
                    mnt-by:RIPE-NCC-END-MNT
                    mnt-by: TEST-MNT
                    changed: ripe@test.net 20120505
                    org: ORG-TOL1-TEST
                    source: TEST

                    password:update
                    password:hm

                    inetnum: 192.0.0.0 - 192.0.0.255
                    netname: RIPE-NCC
                    descr: description
                    country: DK
                    admin-c: TEST-PN
                    tech-c: TEST-PN
                    status: ASSIGNED PI
                    mnt-by: TEST-MNT
                    changed: ripe@test.net 20120505
                    org: ORG-TOL4-TEST
                    source: TEST
                    override:denis,override1
                """.stripIndent()))
    then:
      response =~ /Create SUCCEEDED: \[inetnum\] 192.0.0.0 - 192.0.0.255/
      response =~ /Modify SUCCEEDED: \[inetnum\] 192.0.0.0 - 192.0.0.255/
  }

  def "modify status ALLOCATED PA has no reference to organisation"() {
    given:
      def insertResponse = syncUpdate(new SyncUpdate(data: """\
                    inetnum: 192.0.0.0 - 192.0.0.255
                    netname: RIPE-NCC
                    descr: description
                    country: DK
                    admin-c: TEST-PN
                    tech-c: TEST-PN
                    status: ALLOCATED PA
                    org: ORG-TOL1-TEST
                    mnt-by: RIPE-NCC-HM-MNT
                    changed: ripe@test.net 20120505
                    source: TEST
                    password: hm
                    password: update
                """.stripIndent()))
    expect:
      insertResponse =~ /SUCCESS/
    when:
      def response = syncUpdate new SyncUpdate(data: """\
                    inetnum: 192.0.0.0 - 192.0.0.255
                    netname: RIPE-NCC
                    descr: description
                    country: DK
                    admin-c: TEST-PN
                    tech-c: TEST-PN
                    status: ALLOCATED PA
                    mnt-by: RIPE-NCC-HM-MNT
                    changed: ripe@test.net 20120505
                    source: TEST
                    password:update
                """.stripIndent())
    then:
      response =~ /FAIL/
      response =~ /Missing required "org:" attribute/
  }

  def "create status ALLOCATED PA requires alloc with multiple maintainer"() {
    given:
      def insertResponse = syncUpdate(new SyncUpdate(data: """\
                    inetnum: 192.0.0.0 - 192.0.0.255
                    netname: RIPE-NCC
                    descr: description
                    country: DK
                    admin-c: TEST-PN
                    tech-c: TEST-PN
                    status: ALLOCATED PA
                    org: ORG-TOL1-TEST
                    mnt-by: RIPE-NCC-HM-MNT
                    mnt-by: TEST-MNT
                    changed: ripe@test.net 20120505
                    source: TEST
                    password:update
                    password:hm
                """.stripIndent()))
    expect:
      insertResponse =~ /SUCCESS/
      insertResponse =~ /Create SUCCEEDED: \[inetnum\] 192.0.0.0 - 192.0.0.255/
  }

  def "create status LEGACY, parent not LEGACY, not RS or override"() {
    when:
      def insertResponse = syncUpdate(new SyncUpdate(data: """\
                    inetnum: 192.0.0.0 - 192.0.0.255
                    netname: RIPE-NCC
                    descr: description
                    country: DK
                    admin-c: TEST-PN
                    tech-c: TEST-PN
                    status: LEGACY
                    mnt-by: TEST-MNT
                    changed: ripe@test.net 20120505
                    source: TEST
                    password:update
                """.stripIndent()))
    then:
      insertResponse =~ /FAIL/
      insertResponse =~ /Error:   Only RIPE NCC can create\/delete a top level object with status
            'LEGACY'
            Contact legacy@ripe.net for more info/
  }

  def "create status LEGACY, parent legacy, not RS or override"() {
    given:
      databaseHelper.addObject("" +
              "inetnum: 192.0.0.0 - 192.0.255.255\n" +
              "netname: RIPE-NCC\n" +
              "descr: description\n" +
              "country: DK\n" +
              "admin-c: TEST-PN\n" +
              "tech-c: TEST-PN\n" +
              "status: LEGACY\n" +
              "mnt-by: TEST-MNT\n" +
              "changed: ripe@test.net 20120505\n" +
              "source: TEST")
    when:
      def insertResponse = syncUpdate(new SyncUpdate(data: """\
                    inetnum: 192.0.0.0 - 192.0.0.255
                    netname: RIPE-NCC
                    descr: description
                    country: DK
                    admin-c: TEST-PN
                    tech-c: TEST-PN
                    status: LEGACY
                    mnt-by: TEST-MNT
                    changed: ripe@test.net 20120505
                    source: TEST
                    password:update
                """.stripIndent()))
    then:
      insertResponse =~ /Create SUCCEEDED: \[inetnum\] 192.0.0.0 - 192.0.0.255/
  }

  def "create status LEGACY, parent not legacy, RS"() {
    when:
      def insertResponse = syncUpdate(new SyncUpdate(data: """\
                    inetnum: 192.0.0.0 - 192.0.0.255
                    netname: RIPE-NCC
                    descr: description
                    country: DK
                    admin-c: TEST-PN
                    tech-c: TEST-PN
                    status: LEGACY
                    mnt-by: TEST-MNT
                    mnt-by: RIPE-NCC-HM-MNT
                    changed: ripe@test.net 20120505
                    source: TEST
                    password:hm
                """.stripIndent()))
    then:
      insertResponse =~ /Create SUCCEEDED: \[inetnum\] 192.0.0.0 - 192.0.0.255/
  }

  def "create status LEGACY, legacy maintainer reference cannot be added by enduser maintainer"() {
    given:
      syncUpdate(new SyncUpdate(data: """\
                    inetnum:    192.0.0.0 - 192.255.255.255
                    netname:    RIPE-NCC
                    descr:      description
                    country:    DK
                    admin-c:    TEST-PN
                    tech-c:     TEST-PN
                    status:     LEGACY
                    mnt-lower:  TEST-MNT
                    mnt-by:     RIPE-NCC-HM-MNT
                    changed:    ripe@test.net 20120505
                    source:     TEST
                    password:hm
                """.stripIndent()))
    when:
      def insertResponse = syncUpdate(new SyncUpdate(data: """\
                    inetnum:    192.0.0.0 - 192.0.0.255
                    netname:    RIPE-NCC
                    descr:      description
                    country:    DK
                    admin-c:    TEST-PN
                    tech-c:     TEST-PN
                    status:     LEGACY
                    mnt-by:     RIPE-NCC-LEGACY-MNT
                    mnt-by:     TEST-MNT
                    changed:    ripe@test.net 20120505
                    source:     TEST
                    password: update
                """.stripIndent()))
    then:
      insertResponse =~ /Create FAILED: \[inetnum\] 192.0.0.0 - 192.0.0.255/
      insertResponse =~ /\*\*\*Error:   Adding or removing a RIPE NCC maintainer requires administrative\n\s+authorisation/
  }

  def "create status LEGACY, parent not legacy or allocated unspecified, RS"() {
    given:
      databaseHelper.addObject("" +
              "inetnum: 192.0.0.0 - 192.0.255.255\n" +
              "netname: RIPE-NCC\n" +
              "descr: description\n" +
              "country: DK\n" +
              "admin-c: TEST-PN\n" +
              "tech-c: TEST-PN\n" +
              "status: ASSIGNED PI\n" +
              "mnt-by: TEST-MNT\n" +
              "changed: ripe@test.net 20120505\n" +
              "source: TEST")
    when:
      def insertResponse = syncUpdate(new SyncUpdate(data: """\
                    inetnum: 192.0.0.0 - 192.0.0.255
                    netname: RIPE-NCC
                    descr: description
                    country: DK
                    admin-c: TEST-PN
                    tech-c: TEST-PN
                    status: LEGACY
                    mnt-by: TEST-MNT
                    mnt-by: RIPE-NCC-HM-MNT
                    changed: ripe@test.net 20120505
                    source: TEST
                    password:hm
                    password:update
                """.stripIndent()))
    then:
      insertResponse =~ /Create FAILED: \[inetnum\] 192.0.0.0 - 192.0.0.255/
      insertResponse =~ /Error:   inetnum parent has incorrect status: ASSIGNED PI/
  }

  def "create child ASSIGNED PA, parent LEGACY, not RS or override"() {
    given:
      databaseHelper.addObject("" +
              "inetnum: 192.0.0.0 - 192.0.255.255\n" +
              "netname: RIPE-NCC\n" +
              "descr: description\n" +
              "country: DK\n" +
              "admin-c: TEST-PN\n" +
              "tech-c: TEST-PN\n" +
              "status: LEGACY\n" +
              "mnt-by: TEST-MNT\n" +
              "changed: ripe@test.net 20120505\n" +
              "source: TEST")
    when:
      def create = syncUpdate(new SyncUpdate(data: """\
                    inetnum: 192.0.0.0 - 192.0.0.255
                    netname: RIPE-NCC
                    descr: updated description
                    country: DK
                    admin-c: TEST-PN
                    tech-c: TEST-PN
                    status: ASSIGNED PA
                    mnt-by: TEST-MNT
                    changed: ripe@test.net 20120505
                    source: TEST
                    password:update
                """.stripIndent()))
    then:
      create =~ /Create SUCCEEDED: \[inetnum\] 192.0.0.0 - 192.0.0.255/
      create =~ /Info:    Value ASSIGNED PA converted to LEGACY/

  }

  def "create with parent status LEGACY, update has status LEGACY"() {
    databaseHelper.addObject("" +
            "inetnum: 192.168.0.0 - 192.168.0.255\n" +
            "netname: test netname\n" +
            "status: LEGACY\n" +
            "mnt-by: TEST2-MNT\n" +
            "source: test");
    ipTreeUpdater.rebuild();

    when:
      def response = syncUpdate(new SyncUpdate(data: """\
                inetnum:      192.168.0.5 - 192.168.0.10
                netname:      test-netname
                status:       LEGACY
                descr:        /24 assigned
                country:      NL
                admin-c:      TEST-PN
                tech-c:       TEST-PN
                mnt-by:       test2-mnt
                changed:      test@ripe.net
                source:       TEST
                password:     emptypassword
                """))

    then:
      response =~ /Create SUCCEEDED: \[inetnum\] 192.168.0.5 - 192.168.0.10/
      !(response =~ /\*\*\*Info:    Value ASSIGNED PA converted to LEGACY/)
  }

  def "delete status LEGACY, parent not LEGACY, not RS or override"() {
    given:
      databaseHelper.addObject("" +
              "inetnum: 192.0.0.0 - 192.0.255.255\n" +
              "netname: RIPE-NCC\n" +
              "descr: description\n" +
              "country: DK\n" +
              "admin-c: TEST-PN\n" +
              "tech-c: TEST-PN\n" +
              "status: ALLOCATED UNSPECIFIED\n" +
              "mnt-by: TEST-MNT\n" +
              "changed: ripe@test.net 20120505\n" +
              "source: TEST")
      databaseHelper.addObject("" +
              "inetnum: 192.0.0.0 - 192.0.0.255\n" +
              "netname: RIPE-NCC\n" +
              "descr: description\n" +
              "country: DK\n" +
              "admin-c: TEST-PN\n" +
              "tech-c: TEST-PN\n" +
              "status: LEGACY\n" +
              "mnt-by: TEST-MNT\n" +
              "changed: ripe@test.net 20120505\n" +
              "source: TEST")
    when:
      def delete = syncUpdate(new SyncUpdate(data: """\
                    inetnum: 192.0.0.0 - 192.0.0.255
                    netname: RIPE-NCC
                    descr: description
                    country: DK
                    admin-c: TEST-PN
                    tech-c: TEST-PN
                    status: LEGACY
                    mnt-by: TEST-MNT
                    changed: ripe@test.net 20120505
                    source: TEST
                    delete: reason
                    password:update
                """.stripIndent()))
    then:
      delete =~ /Delete FAILED: \[inetnum\] 192.0.0.0 - 192.0.0.255/
      delete =~ /Error:   Only RIPE NCC can create\/delete a top level object with status
            'LEGACY'
            Contact legacy@ripe.net for more info/
  }

  def "delete status LEGACY, parent legacy, not RS or override"() {
    given:
      databaseHelper.addObject("" +
              "inetnum: 192.0.0.0 - 192.0.255.255\n" +
              "netname: RIPE-NCC\n" +
              "descr: description\n" +
              "country: DK\n" +
              "admin-c: TEST-PN\n" +
              "tech-c: TEST-PN\n" +
              "status: LEGACY\n" +
              "mnt-by: TEST-MNT\n" +
              "changed: ripe@test.net 20120505\n" +
              "source: TEST")
      databaseHelper.addObject("" +
              "inetnum: 192.0.0.0 - 192.0.0.255\n" +
              "netname: RIPE-NCC\n" +
              "descr: description\n" +
              "country: DK\n" +
              "admin-c: TEST-PN\n" +
              "tech-c: TEST-PN\n" +
              "status: LEGACY\n" +
              "mnt-by: TEST-MNT\n" +
              "changed: ripe@test.net 20120505\n" +
              "source: TEST")
    when:
      def insertResponse = syncUpdate(new SyncUpdate(data: """\
                    inetnum: 192.0.0.0 - 192.0.0.255
                    netname: RIPE-NCC
                    descr: description
                    country: DK
                    admin-c: TEST-PN
                    tech-c: TEST-PN
                    status: LEGACY
                    mnt-by: TEST-MNT
                    changed: ripe@test.net 20120505
                    source: TEST
                    delete: reason
                    password:update
                """.stripIndent()))
    then:
      insertResponse =~ /Delete SUCCEEDED: \[inetnum\] 192.0.0.0 - 192.0.0.255/
  }

  def "delete status LEGACY, parent not legacy, RS"() {
    given:
      databaseHelper.addObject("" +
              "inetnum: 192.0.0.0 - 192.0.255.255\n" +
              "netname: RIPE-NCC\n" +
              "descr: description\n" +
              "country: DK\n" +
              "admin-c: TEST-PN\n" +
              "tech-c: TEST-PN\n" +
              "status: ASSIGNED PI\n" +
              "mnt-by: TEST-MNT\n" +
              "changed: ripe@test.net 20120505\n" +
              "source: TEST")
      databaseHelper.addObject("" +
              "inetnum: 192.0.0.0 - 192.0.0.255\n" +
              "netname: RIPE-NCC\n" +
              "descr: description\n" +
              "country: DK\n" +
              "admin-c: TEST-PN\n" +
              "tech-c: TEST-PN\n" +
              "status: LEGACY\n" +
              "mnt-by: RIPE-NCC-HM-MNT\n" +
              "changed: ripe@test.net 20120505\n" +
              "source: TEST")
    when:
      def delete = syncUpdate(new SyncUpdate(data: """\
                    inetnum: 192.0.0.0 - 192.0.0.255
                    netname: RIPE-NCC
                    descr: description
                    country: DK
                    admin-c: TEST-PN
                    tech-c: TEST-PN
                    status: LEGACY
                    mnt-by: RIPE-NCC-HM-MNT
                    changed: ripe@test.net 20120505
                    source: TEST
                    delete: reason
                    password:hm
                """.stripIndent()))
    then:
      delete =~ /SUCCEEDED/
  }

  def "modify other attribute than status LEGACY, parent LEGACY, not RS or override"() {
    given:
      databaseHelper.addObject("" +
              "inetnum: 192.0.0.0 - 192.0.255.255\n" +
              "netname: RIPE-NCC\n" +
              "descr: description\n" +
              "country: DK\n" +
              "admin-c: TEST-PN\n" +
              "tech-c: TEST-PN\n" +
              "status: ASSIGNED PI\n" +
              "mnt-by: TEST-MNT\n" +
              "changed: ripe@test.net 20120505\n" +
              "source: TEST")
      databaseHelper.addObject("" +
              "inetnum: 192.0.0.0 - 192.0.0.255\n" +
              "netname: RIPE-NCC\n" +
              "descr: description\n" +
              "country: DK\n" +
              "admin-c: TEST-PN\n" +
              "tech-c: TEST-PN\n" +
              "status: LEGACY\n" +
              "mnt-by: TEST-MNT\n" +
              "changed: ripe@test.net 20120505\n" +
              "source: TEST")
    when:
      def modify = syncUpdate(new SyncUpdate(data: """\
                    inetnum: 192.0.0.0 - 192.0.0.255
                    netname: RIPE-NCC
                    descr: updated description
                    country: DK
                    admin-c: TEST-PN
                    tech-c: TEST-PN
                    status: LEGACY
                    mnt-by: TEST-MNT
                    changed: ripe@test.net 20120505
                    source: TEST
                    password:update
                """.stripIndent()))
    then:
      modify =~ /Modify SUCCEEDED: \[inetnum\] 192.0.0.0 - 192.0.0.255/
  }

  def "modify status ASSIGNED PI | ANYCAST authed by enduser maintainer may change org, desc, mnt-by, mnt-lower"() {
    given:
      def insertResponse = syncUpdate(new SyncUpdate(data: """\
                    inetnum: 192.0.0.0 - 192.0.0.255
                    netname: RIPE-NCC
                    descr: description
                    country: DK
                    admin-c: TEST-PN
                    tech-c: TEST-PN
                    status: ASSIGNED ANYCAST
                    mnt-by:RIPE-NCC-END-MNT
                    org:ORG-TOL5-TEST
                    changed: ripe@test.net 20120505
                    source: TEST
                    password: hm
                    password: update
                """.stripIndent()))
    expect:
      insertResponse =~ /SUCCESS/
    when:
      def response = syncUpdate new SyncUpdate(data: """\
                    inetnum: 192.0.0.0 - 192.0.0.255
                    netname: RIPE-NCC
                    descr: new description
                    country: DK
                    admin-c: TEST-PN
                    tech-c: TEST-PN
                    status: ASSIGNED ANYCAST
                    mnt-by: TEST-MNT
                    mnt-by:RIPE-NCC-HM-MNT
                    mnt-by:RIPE-NCC-END-MNT
                    mnt-by: TEST-MNT
                    org: ORG-TOL2-TEST
                    mnt-lower:RIPE-NCC-END-MNT
                    changed: ripe@test.net 20120505
                    source: TEST
                    password:update
                """.stripIndent())
    then:
      response =~ /SUCCESS/
  }

  def "modify status ASSIGNED PI maintained by enduser maintainer may not change org, 1st descr, mnt-lower"() {
    when:
      def insertResponse = syncUpdate(new SyncUpdate(data: """\
            inetnum: 192.0.0.0 - 192.0.0.255
            netname: RIPE-NCC
            descr: description
            country: DK
            admin-c: TEST-PN
            tech-c: TEST-PN
            status: ASSIGNED PI
            mnt-by: TEST-MNT
            mnt-by:RIPE-NCC-END-MNT
            changed: ripe@test.net 20120505
            source: TEST
            org:ORG-TOL1-TEST
            password:update
            password:hm
            """.stripIndent()))
    then:
      insertResponse =~ /SUCCESS/
    when:
      def response = syncUpdate new SyncUpdate(data: """\
            inetnum: 192.0.0.0 - 192.0.0.255
            netname: RIPE-NCC
            descr: new description
            country: DK
            admin-c: TEST-PN
            tech-c: TEST-PN
            status: ASSIGNED PI
            org: ORG-TOL2-TEST
            mnt-by: RIPE-NCC-HM-MNT
            mnt-lower: TEST-MNT
            changed: ripe@test.net 20120505
            source: TEST
            """.stripIndent())
    then:
      response =~ /FAIL/
      response =~ /not authenticated by: TEST-MNT, RIPE-NCC-END-MNT/
  }

  def "modify status ASSIGNED PI maintained by enduser maintainer may not change org, 1st descr, mnt-lower with override"() {
    when:
      def response = syncUpdate(new SyncUpdate(data: """\
            inetnum: 192.0.0.0 - 192.0.0.255
            netname: RIPE-NCC
            descr: description
            country: DK
            admin-c: TEST-PN
            tech-c: TEST-PN
            status: ASSIGNED PI
            mnt-by: TEST-MNT
            mnt-by:RIPE-NCC-END-MNT
            changed: ripe@test.net 20120505
            source: TEST
            org:ORG-TOL1-TEST

            password:update
            password:hm

            inetnum: 192.0.0.0 - 192.0.0.255
            netname: RIPE-NCC
            descr: new description
            country: DK
            admin-c: TEST-PN
            tech-c: TEST-PN
            status: ASSIGNED PI
            org: ORG-TOL2-TEST
            mnt-by: RIPE-NCC-HM-MNT
            mnt-lower: TEST-MNT
            changed: ripe@test.net 20120505
            source: TEST
            override: denis,override1
            """.stripIndent()))
    then:
      response =~ /Create SUCCEEDED: \[inetnum\] 192.0.0.0 - 192.0.0.255/
      response =~ /Modify SUCCEEDED: \[inetnum\] 192.0.0.0 - 192.0.0.255/
  }

 def "modify ALLOCATED UNSPECIFIED status, cannot be changed to LEGACY by enduser maintainer"() {
    given:
      syncUpdate(new SyncUpdate(data: """\
                    inetnum:    192.0.0.0 - 192.255.255.255
                    netname:    RIPE-NCC
                    descr:      description
                    org:        ORG-TOL2-TEST
                    country:    DK
                    admin-c:    TEST-PN
                    tech-c:     TEST-PN
                    status:     ASSIGNED PI
                    mnt-by:     RIPE-NCC-HM-MNT
                    mnt-lower:  TEST-MNT
                    changed:    ripe@test.net 20120505
                    source:     TEST
                    override:denis,override1
                """.stripIndent()))
      syncUpdate(new SyncUpdate(data: """\
                    inetnum:    192.0.0.0 - 192.0.0.255
                    netname:    RIPE-NCC
                    descr:      description
                    org:        ORG-TOL2-TEST
                    country:    DK
                    admin-c:    TEST-PN
                    tech-c:     TEST-PN
                    status:     ALLOCATED UNSPECIFIED
                    mnt-by:     TEST-MNT
                    changed:    ripe@test.net 20120505
                    source:     TEST
                    override:denis,override1
                """.stripIndent()))
    when:
      def response = syncUpdate(new SyncUpdate(data: """\
                    inetnum:    192.0.0.0 - 192.0.0.255
                    netname:    RIPE-NCC
                    descr:      description
                    org:        ORG-TOL2-TEST
                    country:    DK
                    admin-c:    TEST-PN
                    tech-c:     TEST-PN
                    status:     LEGACY
                    mnt-by:     TEST-MNT
                    changed:    ripe@test.net 20120505
                    source:     TEST
                    password: update
                """.stripIndent()))
    then:
      response =~ /Modify FAILED: \[inetnum\] 192.0.0.0 - 192.0.0.255/
      response =~ /\*\*\*Error:   status value cannot be changed, you must delete and re-create the\n\s+object/
  }

 def "modify LEGACY status, status converted automatically"() {
    given:
      syncUpdate(new SyncUpdate(data: """\
                    inetnum:    192.0.0.0 - 192.255.255.255
                    netname:    RIPE-NCC
                    descr:      description
                    country:    DK
                    admin-c:    TEST-PN
                    tech-c:     TEST-PN
                    status:     LEGACY
                    mnt-by:     RIPE-NCC-HM-MNT
                    mnt-lower:  TEST-MNT
                    changed:    ripe@test.net 20120505
                    source:     TEST
                    password:hm
                """.stripIndent()))
      syncUpdate(new SyncUpdate(data: """\
                    inetnum:    192.0.0.0 - 192.0.0.255
                    netname:    RIPE-NCC
                    descr:      description
                    country:    DK
                    admin-c:    TEST-PN
                    tech-c:     TEST-PN
                    status:     LEGACY
                    mnt-by:     TEST-MNT
                    changed:    ripe@test.net 20120505
                    source:     TEST
                    password: update
                """.stripIndent()))
    when:
      def response = syncUpdate(new SyncUpdate(data: """\
                    inetnum:    192.0.0.0 - 192.0.0.255
                    netname:    RIPE-NCC
                    descr:      description
                    country:    DK
                    admin-c:    TEST-PN
                    tech-c:     TEST-PN
                    status:     ASSIGNED PI
                    mnt-by:     TEST-MNT
                    changed:    ripe@test.net 20120505
                    source:     TEST
                    password: update
                """.stripIndent()))
    then:
      response =~ /No operation: \[inetnum\] 192.0.0.0 - 192.0.0.255/
      response =~ /\*\*\*Info:    Value ASSIGNED PI converted to LEGACY/
  }

 def "modify LEGACY status, legacy maintainer mnt-by reference cannot be added by enduser maintainer"() {
    given:
      syncUpdate(new SyncUpdate(data: """\
                    inetnum:    192.0.0.0 - 192.255.255.255
                    netname:    RIPE-NCC
                    descr:      description
                    country:    DK
                    admin-c:    TEST-PN
                    tech-c:     TEST-PN
                    status:     LEGACY
                    mnt-by:     RIPE-NCC-HM-MNT
                    mnt-lower:  TEST-MNT
                    changed:    ripe@test.net 20120505
                    source:     TEST
                    password:hm
                """.stripIndent()))
      syncUpdate(new SyncUpdate(data: """\
                    inetnum:    192.0.0.0 - 192.0.0.255
                    netname:    RIPE-NCC
                    descr:      description
                    country:    DK
                    admin-c:    TEST-PN
                    tech-c:     TEST-PN
                    status:     LEGACY
                    mnt-by:     TEST-MNT
                    changed:    ripe@test.net 20120505
                    source:     TEST
                    password: update
                """.stripIndent()))
    when:
      def response = syncUpdate(new SyncUpdate(data: """\
                    inetnum:    192.0.0.0 - 192.0.0.255
                    netname:    RIPE-NCC
                    descr:      description
                    country:    DK
                    admin-c:    TEST-PN
                    tech-c:     TEST-PN
                    status:     LEGACY
                    mnt-by:     RIPE-NCC-LEGACY-MNT
                    mnt-by:     TEST-MNT
                    changed:    ripe@test.net 20120505
                    source:     TEST
                    password: update
                """.stripIndent()))
    then:
      response =~ /Modify FAILED: \[inetnum\] 192.0.0.0 - 192.0.0.255/
      response =~ /\*\*\*Error:   Adding or removing a RIPE NCC maintainer requires administrative\n\s+authorisation/
  }

 def "modify LEGACY status, legacy maintainer mnt-lower reference cannot be added by enduser maintainer"() {
    given:
      syncUpdate(new SyncUpdate(data: """\
                    inetnum:    192.0.0.0 - 192.255.255.255
                    netname:    RIPE-NCC
                    descr:      description
                    country:    DK
                    admin-c:    TEST-PN
                    tech-c:     TEST-PN
                    status:     LEGACY
                    mnt-by:     RIPE-NCC-HM-MNT
                    mnt-lower:  TEST-MNT
                    changed:    ripe@test.net 20120505
                    source:     TEST
                    password:hm
                """.stripIndent()))
      syncUpdate(new SyncUpdate(data: """\
                    inetnum:    192.0.0.0 - 192.0.0.255
                    netname:    RIPE-NCC
                    descr:      description
                    country:    DK
                    admin-c:    TEST-PN
                    tech-c:     TEST-PN
                    status:     LEGACY
                    mnt-by:     TEST-MNT
                    changed:    ripe@test.net 20120505
                    source:     TEST
                    password: update
                """.stripIndent()))
    when:
      def response = syncUpdate(new SyncUpdate(data: """\
                    inetnum:    192.0.0.0 - 192.0.0.255
                    netname:    RIPE-NCC
                    descr:      description
                    country:    DK
                    admin-c:    TEST-PN
                    tech-c:     TEST-PN
                    status:     LEGACY
                    mnt-lower:  RIPE-NCC-LEGACY-MNT
                    mnt-by:     TEST-MNT
                    changed:    ripe@test.net 20120505
                    source:     TEST
                    password: update
                """.stripIndent()))
    then:
      response =~ /Modify FAILED: \[inetnum\] 192.0.0.0 - 192.0.0.255/
      response =~ /\*\*\*Error:   Adding or removing a RIPE NCC maintainer requires administrative\n\s+authorisation/
  }

  def "modify LEGACY status, legacy maintainer mnt-by reference cannot be removed by enduser maintainer"() {
    given:
      syncUpdate(new SyncUpdate(data: """\
                    inetnum:    192.0.0.0 - 192.255.255.255
                    netname:    RIPE-NCC
                    descr:      description
                    country:    DK
                    admin-c:    TEST-PN
                    tech-c:     TEST-PN
                    status:     LEGACY
                    mnt-by:     RIPE-NCC-HM-MNT
                    changed:    ripe@test.net 20120505
                    source:     TEST
                    password:hm
                """.stripIndent()))
      syncUpdate(new SyncUpdate(data: """\
                    inetnum:    192.0.0.0 - 192.0.0.255
                    netname:    RIPE-NCC
                    descr:      description
                    country:    DK
                    admin-c:    TEST-PN
                    tech-c:     TEST-PN
                    mnt-by:     RIPE-NCC-LEGACY-MNT
                    mnt-by:     TEST-MNT
                    status:     LEGACY
                    changed:    ripe@test.net 20120505
                    source:     TEST
                    override:denis,override1
                """.stripIndent()))
    when:
      def response = syncUpdate(new SyncUpdate(data: """\
                    inetnum:    192.0.0.0 - 192.0.0.255
                    netname:    RIPE-NCC
                    descr:      description
                    country:    DK
                    admin-c:    TEST-PN
                    tech-c:     TEST-PN
                    status:     LEGACY
                    mnt-by:     TEST-MNT
                    changed:    ripe@test.net 20120505
                    source:     TEST
                    password: update
                """.stripIndent()))
    then:
      response =~ /Modify FAILED: \[inetnum\] 192.0.0.0 - 192.0.0.255/
      response =~ /\*\*\*Error:   Adding or removing a RIPE NCC maintainer requires administrative\n\s+authorisation/
  }

  def "modify LEGACY status, add legacy maintainer mnt-by reference with override"() {
    given:
      syncUpdate(new SyncUpdate(data: """\
                    inetnum:    192.0.0.0 - 192.255.255.255
                    netname:    RIPE-NCC
                    descr:      description
                    country:    DK
                    admin-c:    TEST-PN
                    tech-c:     TEST-PN
                    status:     LEGACY
                    mnt-by:     RIPE-NCC-HM-MNT
                    mnt-lower:  TEST-MNT
                    changed:    ripe@test.net 20120505
                    source:     TEST
                    password:hm
                """.stripIndent()))
      syncUpdate(new SyncUpdate(data: """\
                    inetnum:    192.0.0.0 - 192.0.0.255
                    netname:    RIPE-NCC
                    descr:      description
                    country:    DK
                    admin-c:    TEST-PN
                    tech-c:     TEST-PN
                    mnt-by:     TEST-MNT
                    status:     LEGACY
                    changed:    ripe@test.net 20120505
                    source:     TEST
                    password:update
                """.stripIndent()))
    when:
      def response = syncUpdate(new SyncUpdate(data: """\
                    inetnum:    192.0.0.0 - 192.0.0.255
                    netname:    RIPE-NCC
                    descr:      description
                    country:    DK
                    admin-c:    TEST-PN
                    tech-c:     TEST-PN
                    status:     LEGACY
                    mnt-by:     RIPE-NCC-LEGACY-MNT
                    mnt-by:     TEST-MNT
                    changed:    ripe@test.net 20120505
                    source:     TEST
                    override:denis,override1
                """.stripIndent()))
    then:
      response =~ /Modify SUCCEEDED: \[inetnum\] 192.0.0.0 - 192.0.0.255/
      response =~ /\*\*\*Info:    Authorisation override used/
  }

  def "modify LEGACY status, with legacy maintainer mnt-by, org reference cannot be removed by enduser maintainer"() {
    given:
      syncUpdate(new SyncUpdate(data: """\
                    inetnum:    192.0.0.0 - 192.255.255.255
                    netname:    RIPE-NCC
                    descr:      description
                    country:    DK
                    admin-c:    TEST-PN
                    tech-c:     TEST-PN
                    status:     LEGACY
                    mnt-by:     RIPE-NCC-HM-MNT
                    mnt-lower:  TEST-MNT
                    changed:    ripe@test.net 20120505
                    source:     TEST
                    password:hm
                """.stripIndent()))
      syncUpdate(new SyncUpdate(data: """\
                    inetnum:    192.0.0.0 - 192.0.0.255
                    netname:    RIPE-NCC
                    descr:      description
                    org:        ORG-TOL1-TEST
                    country:    DK
                    admin-c:    TEST-PN
                    tech-c:     TEST-PN
                    status:     LEGACY
                    mnt-by:     RIPE-NCC-LEGACY-MNT
                    mnt-by:     TEST-MNT
                    changed:    ripe@test.net 20120505
                    source:     TEST
                    override:denis,override1
                """.stripIndent()))
    when:
      def response = syncUpdate(new SyncUpdate(data: """\
                    inetnum:    192.0.0.0 - 192.0.0.255
                    netname:    RIPE-NCC
                    descr:      description
                    country:    DK
                    admin-c:    TEST-PN
                    tech-c:     TEST-PN
                    status:     LEGACY
                    mnt-by:     RIPE-NCC-LEGACY-MNT
                    mnt-by:     TEST-MNT
                    changed:    ripe@test.net 20120505
                    source:     TEST
                    password: update
                """.stripIndent()))
    then:
      response =~ /Modify FAILED: \[inetnum\] 192.0.0.0 - 192.0.0.255/
      response =~ /\*\*\*Error:   Referenced organisation can only be removed by the RIPE NCC for this\n\s+resource/
  }

  def "modify LEGACY status, with legacy maintainer mnt-by, org reference cannot be changed by enduser maintainer"() {
    given:
      syncUpdate(new SyncUpdate(data: """\
                    inetnum:    192.0.0.0 - 192.255.255.255
                    netname:    RIPE-NCC
                    descr:      description
                    country:    DK
                    admin-c:    TEST-PN
                    tech-c:     TEST-PN
                    status:     LEGACY
                    mnt-by:     RIPE-NCC-HM-MNT
                    mnt-lower:  TEST-MNT
                    changed:    ripe@test.net 20120505
                    source:     TEST
                    password:hm
                """.stripIndent()))
      syncUpdate(new SyncUpdate(data: """\
                    inetnum:    192.0.0.0 - 192.0.0.255
                    netname:    RIPE-NCC
                    descr:      description
                    org:        ORG-TOL1-TEST
                    country:    DK
                    admin-c:    TEST-PN
                    tech-c:     TEST-PN
                    status:     LEGACY
                    mnt-by:     RIPE-NCC-LEGACY-MNT
                    mnt-by:     TEST-MNT
                    changed:    ripe@test.net 20120505
                    source:     TEST
                    override:denis,override1
                """.stripIndent()))
    when:
      def response = syncUpdate(new SyncUpdate(data: """\
                    inetnum:    192.0.0.0 - 192.0.0.255
                    netname:    RIPE-NCC
                    descr:      description
                    org:        ORG-TOL2-TEST
                    country:    DK
                    admin-c:    TEST-PN
                    tech-c:     TEST-PN
                    status:     LEGACY
                    mnt-by:     RIPE-NCC-LEGACY-MNT
                    mnt-by:     TEST-MNT
                    changed:    ripe@test.net 20120505
                    source:     TEST
                    password: update
                """.stripIndent()))
    then:
      response =~ /Modify FAILED: \[inetnum\] 192.0.0.0 - 192.0.0.255/
      response =~ /\*\*\*Error:   Referenced organisation can only be changed by the RIPE NCC for this\n\s+resource/
  }

  def "adding mnt-irt fails if not authenticated against IRT"() {
    given:
      def insertResponse = syncUpdate(new SyncUpdate(data: """\
                    inetnum: 193.0.0.0 - 193.0.0.255
                    netname: RIPE-NCC
                    descr: description
                    country: DK
                    admin-c: TEST-PN
                    tech-c: TEST-PN
                    status: SUB-ALLOCATED PA
                    mnt-by: TEST-MNT
                    org: ORG-TOL2-TEST
                    changed: ripe@test.net 20120505
                    source: TEST
                    password:update
                """.stripIndent()))
    expect:
      insertResponse =~ /SUCCESS/
    when:
      def response = syncUpdate new SyncUpdate(data: """\
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
                    mnt-irt:irt-IRT1
                    org:ORG-TOL2-TEST
                    password:FAIL
                """.stripIndent())
    then:
      response =~ /FAIL/
      response =~ /not authenticated by: irt-IRT1/
  }

  def "adding mnt-irt fails if not authenticated against IRT with override"() {
    when:
      def response = syncUpdate(new SyncUpdate(data: """\
                    inetnum: 193.0.0.0 - 193.0.0.255
                    netname: RIPE-NCC
                    descr: description
                    country: DK
                    admin-c: TEST-PN
                    tech-c: TEST-PN
                    status: SUB-ALLOCATED PA
                    mnt-by: TEST-MNT
                    org: ORG-TOL2-TEST
                    changed: ripe@test.net 20120505
                    source: TEST

                    password:update

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
                    mnt-irt:irt-IRT1
                    org:ORG-TOL2-TEST
                    override:denis,override1
                """.stripIndent()))
    then:
      !(response =~ /FAIL/)
      response =~ /Modify SUCCEEDED: \[inetnum\] 193.0.0.0 - 193.0.0.255/
  }

  def "adding mnt-irt succeeds when authenticated correctly against IRT"() {
    given:
      def insertResponse = syncUpdate(new SyncUpdate(data: """\
                    inetnum: 193.0.0.0 - 193.0.0.255
                    netname: RIPE-NCC
                    descr: description
                    country: DK
                    admin-c: TEST-PN
                    tech-c: TEST-PN
                    status: SUB-ALLOCATED PA
                    mnt-by: TEST-MNT
                    org: ORG-TOL2-TEST
                    changed: ripe@test.net 20120505
                    source: TEST
                    password:update
                """.stripIndent()))
    expect:
      insertResponse =~ /SUCCESS/
    when:
      def response = syncUpdate new SyncUpdate(data: """\
                    inetnum: 193.0.0.0 - 193.0.0.255
                    netname: RIPE-NCC
                    descr: description
                    country: DK
                    admin-c: TEST-PN
                    tech-c: TEST-PN
                    status: SUB-ALLOCATED PA
                    mnt-by: TEST-MNT
                    mnt-irt: irt-IRT1
                    changed: ripe@test.net 20120505
                    org:ORG-TOL2-TEST
                    source: TEST
                    password:update
                    """.stripIndent())
    then:
      response =~ /SUCCESS/
  }

  def "create, allocated pa has wrong errormessage"() {
    when:
      def response = syncUpdate(new SyncUpdate(data: """\
                inetnum:      192.168.128.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-TOL1-TEST
                admin-c:      TEST-PN
                tech-c:       TEST-PN
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       TEST-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                password: update
                password: hm
                """.stripIndent()))
    then:
      response =~ /SUCCESS/
      response =~ /Create SUCCEEDED: \[inetnum\] 192.168.128.0 - 192.168.255.255/
  }

  def "create, auth by parent lacks 'parent' in errormessage"() {
    when:
      def response = syncUpdate(new SyncUpdate(data: """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TEST-PN
                tech-c:       TEST-PN
                status:       ASSIGNED PA
                mnt-by:       TEST2-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                password:     emptypassword
                """.stripIndent()))
    then:
      response =~ /FAIL/
      response =~ /\*\*\*Error:   Authorisation for parent \[inetnum\] 0.0.0.0 - 255.255.255.255 failed/
      response =~ /using "mnt-by:"/
      response =~ /not authenticated by: RIPE-NCC-HM-MNT/
  }

  def "create, auth by parent lacks 'parent' in errormessage with override"() {
    when:
      def response = syncUpdate(new SyncUpdate(data: """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TEST-PN
                tech-c:       TEST-PN
                status:       ASSIGNED PA
                mnt-by:       TEST2-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                override:     denis,override1
                """.stripIndent()))
    then:
      response =~ /Create SUCCEEDED: \[inetnum\] 192.168.200.0 - 192.168.200.255/
  }

  def "create, assigned pi can have other mntby's than rs maintainer"() {
    when:
      def response = syncUpdate(new SyncUpdate(data: """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TEST-PN
                tech-c:       TEST-PN
                status:       ASSIGNED PI
                mnt-by:       TEST2-MNT
                mnt-by:       RIPE-NCC-HM-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                password:     emptypassword
                password:     pimaintainer
                password:     update
                password:     hm
                """.stripIndent()))
    then:
      response =~ /SUCCESS/
      response =~ /Create SUCCEEDED: \[inetnum\] 192.168.200.0 - 192.168.200.255/
  }


  def "create, assigned pi must have at least one rs maintainer"() {
    when:
      def response = syncUpdate(new SyncUpdate(data: """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TEST-PN
                tech-c:       TEST-PN
                status:       ASSIGNED PI
                mnt-by:       TEST2-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                password:     emptypassword
                password:     update
                password:     hm
                """.stripIndent()))
    then:
      response =~ /SUCCESS/
      response =~ /Create SUCCEEDED: \[inetnum\] 192.168.200.0 - 192.168.200.255/
  }

  def "create, assigned pi must have at least one rs maintainer with override"() {
    when:
      def response = syncUpdate(new SyncUpdate(data: """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TEST-PN
                tech-c:       TEST-PN
                status:       ASSIGNED PI
                mnt-by:       TEST2-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                password:     emptypassword
                password:     update
                override:     denis,override1
                """.stripIndent()))
    then:
      response =~ /Create SUCCEEDED: \[inetnum\] 192.168.200.0 - 192.168.200.255/
  }
}
