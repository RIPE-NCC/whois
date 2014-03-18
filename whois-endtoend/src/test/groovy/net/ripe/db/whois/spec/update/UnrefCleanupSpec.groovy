package net.ripe.db.whois.spec.update

import net.ripe.db.whois.common.EndToEndTest
import net.ripe.db.whois.spec.BaseQueryUpdateSpec
import net.ripe.db.whois.spec.domain.AckResponse
import org.joda.time.LocalDateTime
import spock.lang.Ignore

@Ignore("We are not pushing current unref cleanup into production, but reimplement it - review this suite then")
@org.junit.experimental.categories.Category(EndToEndTest.class)
class UnrefCleanupSpec extends BaseQueryUpdateSpec {

    def "non-existing object"() {
      given:
        queryObjectNotFound("UNR-MNT", "mntner", "UNR-MNT")

      when:
        whoisFixture.unrefCleanup();
        def response = query("--show-tag-info UNR-MNT")

      then:
        !(response =~ "Unreferenced #")
    }

    def "recently deleted object"() {
      given:
        databaseHelper.addObject("""\
                aut-num:     AS10000
                as-name:     TEST-AS
                descr:       Testing Authorisation code
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                mnt-by:      OWNER-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """.stripIndent())
      expect:
        queryObject("AS10000", "aut-num", "AS10000")

      when:
        syncUpdate("""\
                aut-num:     AS10000
                as-name:     TEST-AS
                descr:       Testing Authorisation code
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                mnt-by:      OWNER-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                delete:     reason
                password:   owner
                """.stripIndent())

      then:
        queryObjectNotFound("AS10000", "aut-num", "AS10000")

      when:
        whoisFixture.unrefCleanup()
        def result = query("--show-tag-info -r AS10000")

      then:
        !(result =~ "Unreferenced #")
    }

    def "deleted by unrefcleanup"() {
      given:
        whoisFixture.getTestDateTimeProvider().setTime(new LocalDateTime().minusDays(100))
        databaseHelper.addObject("""\
                aut-num:     AS10000
                as-name:     TEST-AS
                descr:       Testing Authorisation code
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                mnt-by:      OWNER-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """.stripIndent())
      expect:
        queryObject("AS10000", "aut-num", "AS10000")

      when:
        whoisFixture.getTestDateTimeProvider().reset()
        whoisFixture.unrefCleanup()

      then:
        queryObjectNotFound("aut-num", "aut-num", "AS10000")
    }

    def "recently created object not referenced"() {
      given:
        databaseHelper.addObject("""\
                aut-num:     AS10000
                as-name:     TEST-AS
                descr:       Testing Authorisation code
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                mnt-by:      OWNER-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """.stripIndent())
      expect:
        queryObject("-r AS10000", "aut-num", "AS10000")

      when:
        whoisFixture.unrefCleanup()
        def result = query("--show-tag-info AS10000")

      then:
        !(result =~ "Unreferenced #")
    }

    def "recently created object referenced"() {
      given:
        databaseHelper.addObject("""\
                aut-num:     AS10000
                as-name:     TEST-AS
                descr:       Testing Authorisation code
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                mnt-by:      OWNER-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """.stripIndent())
      expect:
        queryObject("-r AS10000", "aut-num", "AS10000")

      when:
        whoisFixture.unrefCleanup()
        def result = query("--show-tag-info -r TP1-TEST")

      then:
        !(result =~ "Unreferenced #")
    }

    def "not recently created object not referenced"() {
      given:
        whoisFixture.getTestDateTimeProvider().setTime(new LocalDateTime().minusDays(80))
        def sync = syncUpdate("""\
                person:     GRU PERSON
                address:     street
                phone:       +31 123456
                nic-hdl:     GRU-TEST
                mnt-by:      OWNER-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                password:    owner
                """.stripIndent())
        def ack = new AckResponse("", sync)

      expect:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        queryObject("-T person GRU-TEST", "person", "GRU PERSON")

      when:
        whoisFixture.getTestDateTimeProvider().reset()
        whoisFixture.unrefCleanup()

      then:
        query("--show-tag-info -r GRU-TEST") =~ "Unreferenced # 'GRU-TEST' will be deleted in 10 days"
    }

    def "not recently created object referenced"() {
      given:
        whoisFixture.getTestDateTimeProvider().setTime(new LocalDateTime().minusDays(100))
        def sync1 = syncUpdate("""\
                person:  Gru Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: GRU-TEST
                remarks: bla bla
                mnt-by:  OWNER-MNT
                changed: dbtest@ripe.net 20120101
                source:  TEST
                password: owner
                """.stripIndent())
        def ack = new AckResponse("", sync1)

      expect:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        queryObject("-r TP1-TEST", "person", "Test Person")

      when:
        def sync2 = syncUpdate("""\
                role:     Jon Bob
                address:     street
                phone:       +44 123456
                nic-hdl:     UGU-TEST
                e-mail:      hugu@test.net
                admin-c:     GRU-TEST
                mnt-by:      OWNER-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                password: owner
                """.stripIndent())
        def ack2 = new AckResponse("", sync2)

      then:
        ack2.summary.nrFound == 1
        ack2.summary.assertSuccess(1, 1, 0, 0, 0)
        ack2.summary.assertErrors(0, 0, 0, 0)
        queryObject("-T role UGU-TEST", "role", "Jon Bob")

      when:
        whoisFixture.getTestDateTimeProvider().reset()
        whoisFixture.unrefCleanup()

      then:
        !(query("--show-tag-info -r GRU-TEST") =~ "Unreferenced #")
    }

    def "object created long ago recently referenced"() {
      given:
        whoisFixture.getTestDateTimeProvider().setTime(new LocalDateTime().minusDays(100))
        databaseHelper.addObject("""\
                person:  Gru Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: GRU-TEST
                remarks: bla bla
                mnt-by:  OWNER-MNT
                changed: dbtest@ripe.net 20120101
                source:  TEST
                password: owner
                """.stripIndent())
        whoisFixture.getTestDateTimeProvider().reset()

      expect:
        queryObject("-r GRU-TEST", "person", "Gru Person")

      when:
        whoisFixture.getTestDateTimeProvider().setTime(new LocalDateTime().minusDays(50))
        databaseHelper.addObject("""\
                role:     Jon Bob
                address:     street
                phone:       +44 123456
                nic-hdl:     JON-TEST
                e-mail:      hugu@test.net
                admin-c:     GRU-TEST
                mnt-by:      OWNER-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """.stripIndent())
      then:
        queryObject("JON-TEST", "role", "Jon Bob")

      when:
        whoisFixture.getTestDateTimeProvider().reset()
        whoisFixture.unrefCleanup()

      then:
        !(query("--show-tag-info -r GRU-TEST") =~ "Unreferenced #")
    }

    def "object created long ago now unreferenced"() {
      given:
        whoisFixture.getTestDateTimeProvider().setTime(new LocalDateTime().minusDays(100))
        databaseHelper.addObject("""\
                person:  Gru Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: GRU-TEST
                remarks: bla bla
                mnt-by:  OWNER-MNT
                changed: dbtest@ripe.net 20120101
                source:  TEST
                """.stripIndent())
      expect:
        queryObject("-r GRU-TEST", "person", "Gru Person")

      when:
        databaseHelper.addObject("""\
                role:     Jon Bob
                address:     street
                phone:       +44 123456
                nic-hdl:     JON-TEST
                e-mail:      hugu@test.net
                admin-c:     GRU-TEST
                mnt-by:      OWNER-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """.stripIndent())
      then:
        queryObject("-T role JON-TEST", "role", "Jon Bob")

      when:
        whoisFixture.getTestDateTimeProvider().reset()
        def ack = new AckResponse("", syncUpdate("""\
                role:     Jon Bob
                address:     street
                phone:       +44 123456
                nic-hdl:     JON-TEST
                e-mail:      hugu@test.net
                admin-c:     GRU-TEST
                mnt-by:      OWNER-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                delete:      reason
                password:    owner
                """.stripIndent()))
      then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        queryObjectNotFound("-T role JON-TEST", "role", "Jon Bob")

      when:
        whoisFixture.unrefCleanup()

      then:
        !(query("--show-tag-info -r GRU-TEST") =~ "Unreferenced #")
    }

    def "object created long ago recently unreferenced"() {
      given:
        whoisFixture.getTestDateTimeProvider().setTime(new LocalDateTime().minusDays(100))
        databaseHelper.addObject("""\
                person:  Gru Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: GRU-TEST
                remarks: bla bla
                mnt-by:  OWNER-MNT
                changed: dbtest@ripe.net 20120101
                source:  TEST
                """.stripIndent())
      expect:
        queryObject("-r GRU-TEST", "person", "Gru Person")

      when:
        databaseHelper.addObject("""\
                role:     Jon Bob
                address:     street
                phone:       +44 123456
                nic-hdl:     JON-TEST
                e-mail:      hugu@test.net
                admin-c:     GRU-TEST
                mnt-by:      OWNER-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """.stripIndent())
      then:
        queryObject("-r JON-TEST", "role", "Jon Bob")

      when:
        whoisFixture.getTestDateTimeProvider().setTime(new LocalDateTime().minusDays(50))
        def ack = new AckResponse("", syncUpdate("""\
                role:     Jon Bob
                address:     street
                phone:       +44 123456
                nic-hdl:     JON-TEST
                e-mail:      hugu@test.net
                admin-c:     GRU-TEST
                mnt-by:      OWNER-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                delete:      reason
                password:    owner
                """.stripIndent()))
      then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        queryObjectNotFound("-T role JON-TEST", "role", "Jon Bob")

      when:
        whoisFixture.getTestDateTimeProvider().reset()
        whoisFixture.unrefCleanup()

      then:
        query("--show-tag-info -r GRU-TEST") =~ "Unreferenced # 'GRU-TEST' will be deleted in 40 days"
    }

    def "object created recently now referenced"() {
      given:
        whoisFixture.getTestDateTimeProvider().setTime(new LocalDateTime().minusDays(50))
        databaseHelper.addObject("""\
                person:  Gru Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: GRU-TEST
                remarks: bla bla
                mnt-by:  OWNER-MNT
                changed: dbtest@ripe.net 20120101
                source:  TEST
                """.stripIndent())
      expect:
        queryObject("-r GRU-TEST", "person", "Gru Person")

      when:
        whoisFixture.getTestDateTimeProvider().reset()
        databaseHelper.addObject("""\
                role:     Jon Bob
                address:     street
                phone:       +44 123456
                nic-hdl:     JON-TEST
                e-mail:      hugu@test.net
                admin-c:     GRU-TEST
                mnt-by:      OWNER-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """.stripIndent())
      then:
        queryObject("-T role JON-TEST", "role", "Jon Bob")

      when:
        whoisFixture.unrefCleanup()

      then:
        !(query("--show-tag-info -r GRU-TEST") =~ "Unreferenced #")
    }

    def "object created recently now unreferenced"() {
      given:
        whoisFixture.getTestDateTimeProvider().setTime(new LocalDateTime().minusDays(50))
        databaseHelper.addObject("""\
                person:  Gru Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: GRU-TEST
                remarks: bla bla
                mnt-by:  OWNER-MNT
                changed: dbtest@ripe.net 20120101
                source:  TEST
                """.stripIndent())
      expect:
        queryObject("-r GRU-TEST", "person", "Gru Person")

      when:
        databaseHelper.addObject("""\
                role:     Jon Bob
                address:     street
                phone:       +44 123456
                nic-hdl:     JON-TEST
                e-mail:      hugu@test.net
                admin-c:     GRU-TEST
                mnt-by:      OWNER-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """.stripIndent())
      then:
        queryObject("-r JON-TEST", "role", "Jon Bob")

      when:
        whoisFixture.getTestDateTimeProvider().reset()
        def ack = new AckResponse("", syncUpdate("""\
                role:     Jon Bob
                address:     street
                phone:       +44 123456
                nic-hdl:     JON-TEST
                e-mail:      hugu@test.net
                admin-c:     GRU-TEST
                mnt-by:      OWNER-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                delete:      reason
                password:    owner
                """.stripIndent()))
      then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        queryObjectNotFound("-r JON-TEST", "role", "Jon Bob")

      when:
        whoisFixture.unrefCleanup()

      then:
        !(query("--show-tag-info -r GRU-TEST") =~ "Unreferenced #")
    }

    def "LIR organisation referenced since long ago"() {
      given:
        whoisFixture.getTestDateTimeProvider().setTime(new LocalDateTime().minusDays(100))
        databaseHelper.addObject("" +
                "organisation: ORG-TST-TEST\n" +
                "org-name: Test Organisation\n" +
                "org-type: LIR\n" +
                "address: street\n" +
                "e-mail: org@test.net\n" +
                "mnt-ref: OWNER-MNT\n" +
                "mnt-by: OWNER-MNT\n" +
                "changed: changed@test.net\n" +
                "source: TEST")
      expect:
        queryObject("-r ORG-TST-TEST", "organisation", "ORG-TST-TEST")

      when:
        databaseHelper.addObject("" +
                "mntner: TOT-MNT\n" +
                "mnt-by: TOT-MNT\n" +
                "org: ORG-TST-TEST\n" +
                "descr: description\n" +
                "admin-c: TP1-TEST\n" +
                "tech-c: TP1-TEST\n" +
                "auth: MD5-PW \$1\$9vNwegLB\$SrX4itajapDaACGZaLOIY1\n" +
                "referral-by: OWNER2-MNT\n" +
                "changed: changed@test.net\n" +
                "source: TEST")
      then:
        queryObject("-r TST-MNT", "mntner", "TST-MNT")

      when:
        whoisFixture.getTestDateTimeProvider().reset()
        whoisFixture.unrefCleanup()

      then:
        !(query("--show-tag-info -r ORG-TST-TEST") =~ "Unreferenced #")
        queryObject("-r ORG-TST-TEST", "organisation", "ORG-TST-TEST")
    }

    def "LIR organisation unreferenced since long ago"() {
      given:
        whoisFixture.getTestDateTimeProvider().setTime(new LocalDateTime().minusDays(100))
        databaseHelper.addObject("" +
                "organisation: ORG-TST-TEST\n" +
                "org-name: Test Organisation\n" +
                "org-type: LIR\n" +
                "address: street\n" +
                "e-mail: org@test.net\n" +
                "mnt-ref: OWNER-MNT\n" +
                "mnt-by: OWNER-MNT\n" +
                "changed: changed@test.net\n" +
                "source: TEST")
      expect:
        queryObject("-r ORG-TST-TEST", "organisation", "ORG-TST-TEST")

      when:
        whoisFixture.getTestDateTimeProvider().reset()
        whoisFixture.unrefCleanup()

      then:
        !(query("--show-tag-info -r ORG-TST-TEST") =~ "Unreferenced #")
        queryObject("ORG-TST-TEST", "organisation", "ORG-TST-TEST")
    }

    def "LIR organisation unreferenced since recently"() {
      given:
        whoisFixture.getTestDateTimeProvider().setTime(new LocalDateTime().minusDays(50))
        databaseHelper.addObject("" +
                "organisation: ORG-TST-TEST\n" +
                "org-name: Test Organisation\n" +
                "org-type: LIR\n" +
                "address: street\n" +
                "e-mail: org@test.net\n" +
                "mnt-ref: OWNER-MNT\n" +
                "mnt-by: OWNER-MNT\n" +
                "changed: changed@test.net\n" +
                "source: TEST")
      expect:
        queryObject("-r ORG-TST-TEST", "organisation", "ORG-TST-TEST")

      when:
        whoisFixture.getTestDateTimeProvider().reset()
        whoisFixture.unrefCleanup()

      then:
        !(query("--show-tag-info -r ORG-TST-TEST") =~ "Unreferenced #")
        queryObject("ORG-TST-TEST", "organisation", "ORG-TST-TEST")
    }
}
