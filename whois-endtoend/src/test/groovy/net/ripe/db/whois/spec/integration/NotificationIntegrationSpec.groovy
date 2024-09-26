package net.ripe.db.whois.spec.integration

import net.ripe.db.whois.spec.domain.Message
import net.ripe.db.whois.spec.domain.SyncUpdate
import java.time.LocalDateTime

@org.junit.jupiter.api.Tag("IntegrationTest")
class NotificationIntegrationSpec extends BaseWhoisSourceSpec {
    @Override
    Map<String, String> getFixtures() {
        return ["TEST-MNT": """\
                    mntner:         TEST-MNT
                    admin-c:        TEST-PN
                    mnt-by:         TEST-MNT
                    notify:         test_test@ripe.net
                    upd-to:         dbtest@ripe.net
                    auth:           MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                    auth:           SSO ssotest@ripe.net
                    source:         TEST
                """,
                "TEST-PN": """\
                    person:         some one
                    nic-hdl:        TEST-PN
                    mnt-by:         TEST-MNT
                    source:         TEST
                """,
                "INETROOT":"""\
                    inet6num:       0::/0
                    netname:        IANA-BLK
                    descr:          The whole IPv4 address space
                    country:        DK
                    admin-c:        TEST-PN
                    tech-c:         TEST-PN
                    status:         ALLOCATED-BY-RIR
                    mnt-by:         TEST-MNT
                    source:         TEST
                """]
    }

    def setupSpec() {
        resetTime()
    }

    def "create, single object, notif sent to notify"() {
      when:
        def create = """\
                mntner: ADMIN-MNT
                descr: description
                admin-c: TEST-PN
                mnt-by: ADMIN-MNT
                notify: notify_test@ripe.net
                upd-to: dbtest@ripe.net
                auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                source: TEST
                password: update
                """
        def message = send new Message(
                body: create,
                subject: "NEW"
        )

      then:
        ackFor message

        def notif = notificationFor "notify_test@ripe.net"
        notif.subject =~ "Notification of RIPE Database changes"
        notif.created.any { it.type == "mntner" && it.key == "ADMIN-MNT" }

        noMoreMessages()
    }

    def "update single object, one notification sent to mnt-nfy and notify"() {
      when:
        def firstUpdate = send new Message(
                body: """
                    person: some one
                    address: 258 Singel
                    phone: +31 60 1234567
                    nic-hdl: TEST-PN
                    mnt-by: TEST-MNT
                    notify: test_test@ripe.net
                    source: TEST

                    mntner: TEST-MNT
                    mnt-nfy: test_test@ripe.net
                    admin-c: TEST-PN
                    mnt-by: TEST-MNT
                    notify: test_test@ripe.net
                    upd-to: dbtest@ripe.net
                    auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                    descr:  description
                    source: TEST

                    password: update

                """.stripIndent(true),
                subject: "update"
        )

      then:
        ackFor firstUpdate
        def firstNotif = notificationFor "test_test@ripe.net"
        firstNotif.contents !=~ /--show-version 2 TEST-PN/
        firstNotif.contents =~ /--show-version 2 TEST-MNT/
        noMoreMessages()

      then:
        def secondUpdate = send new Message(
                body: """
                    person: some one
                    address: 258 Singel
                    phone: +31 60 1234567
                    nic-hdl: TEST-PN
                    mnt-by: TEST-MNT
                    notify: test_test@ripe.net
                    remarks: updated again
                    source: TEST

                    password: update
                """.stripIndent(true),
                subject: "update"
        )

      then:
        ackFor secondUpdate

        def notification = notificationFor "test_test@ripe.net"

        notification.subject =~ "Notification of RIPE Database changes"
        notification.contents =~ /(?ms)OBJECT BELOW MODIFIED:\n\n.*\nperson:         some one\n/
        notification.contents !=~ /--show-version 3 TEST-PN/
        noMoreMessages()
    }

    def "create, multiple objects, notif sent to notify"() {
      when:
        def createMntners = """\
                mntner: ADMIN-MNT
                descr: description
                admin-c: TEST-PN
                mnt-by: ADMIN-MNT
                notify: notify_test@ripe.net
                upd-to: dbtest@ripe.net
                auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                source: TEST

                mntner: TEST-MNT
                descr: description
                admin-c: TEST-PN
                mnt-by: ADMIN-MNT
                notify: test_test@ripe.net
                upd-to: test@ripe.net
                auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                source: TEST

                password: update
                """

        def message = send new Message(
                body: createMntners,
                subject: "Update"
        )

      then:
        ackFor message

        def updateNotif = notificationFor "test_test@ripe.net"
        updateNotif.subject =~ "Notification of RIPE Database changes"
        updateNotif.contents =~ /OBJECT BELOW MODIFIED:\n\n@@ -1,8 \+1,9 @@\n mntner:         TEST-MNT/
        updateNotif.contents =~ /THIS IS THE NEW VERSION OF THE OBJECT:\n\nmntner:         TEST-MNT\ndescr:          description/
        updateNotif.contents =~ /--show-version 2 TEST-MNT/

        def createNotif = notificationFor "notify_test@ripe.net"
        createNotif.contents =~ /OBJECT BELOW CREATED:\n\nmntner:         ADMIN-MNT/

        noMoreMessages()
    }


    def "create, single object, notif sent to mnt-nfy"() {
      when:
        def create = """\
                mntner: ADMIN-MNT
                descr: description
                admin-c: TEST-PN
                mnt-by: ADMIN-MNT
                mnt-nfy: notify_test@ripe.net
                upd-to: dbtest@ripe.net
                auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                source: TEST
                password: update
                """
        def message = send new Message(
                body: create,
                subject: "NEW"
        )

        ackFor message

      then:
        def notif = notificationFor "notify_test@ripe.net"
        notif.subject =~ "Notification of RIPE Database changes"
        notif.created.any { it.type == "mntner" && it.key == "ADMIN-MNT" }

        noMoreMessages()
    }

    def "create, single object, notif disabled by override option"() {
      when:
        def create = """\
                mntner: ADMIN-MNT
                descr: description
                admin-c: TEST-PN
                mnt-by: ADMIN-MNT
                mnt-nfy: notify_test@ripe.net
                upd-to: dbtest@ripe.net
                auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                source: TEST
                override: denis,override1,{notify=false}
                """
        def response = syncUpdate new SyncUpdate(data: create)

      then:
        response.contains("Create SUCCEEDED: [mntner] ADMIN-MNT")
        response.contains("***Info:    Authorisation override used")

        noMoreMessages()
    }

    def "create, single object, notif enabled by override option"() {
      when:
        def create = """\
                mntner: ADMIN-MNT
                descr: description
                admin-c: TEST-PN
                mnt-by: ADMIN-MNT
                mnt-nfy: notify_test@ripe.net
                upd-to: dbtest@ripe.net
                auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                source: TEST
                override: denis,override1,{notify=true}
                """
        def response = syncUpdate new SyncUpdate(data: create)

      then:
        response.contains("Create SUCCEEDED: [mntner] ADMIN-MNT")
        response.contains("***Info:    Authorisation override used")

        def notif = notificationFor "notify_test@ripe.net"
        notif.subject =~ "Notification of RIPE Database changes"
        notif.created.any { it.type == "mntner" && it.key == "ADMIN-MNT" }

        noMoreMessages()
    }

    def "create, multiple objects, notif sent to mnt-nfy"() {
      when:
        def create = """\
                mntner: ADMIN-MNT
                descr: description
                admin-c: TEST-PN
                mnt-by: ADMIN-MNT
                mnt-nfy: notify_test@ripe.net
                upd-to: dbtest@ripe.net
                auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                source: TEST

                password: update


                mntner: TEST-MNT
                descr: description
                admin-c: TEST-PN
                mnt-by: ADMIN-MNT
                notify: test_test@ripe.net
                upd-to: test@ripe.net
                auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                source: TEST
                """
        def message = send new Message(
                body: create,
                subject: "NEW"
        )

        ackFor message

      then:
        def notif = notificationFor "notify_test@ripe.net"
        notif.subject =~ "Notification of RIPE Database changes"
        notif.created.any { it.type == "mntner" && it.key == "ADMIN-MNT" }

        noMoreMessages()
    }


    def "update, single object, notif sent to notify"() {
      when:
        def person = """\
                    person: some one
                    nic-hdl: OLW-PN
                    address: street
                    phone: +42 33 81394393
                    mnt-by: TEST-MNT
                    notify: modify_test@ripe.net
                    source: TEST
                    password: update

                    person: some one
                    nic-hdl: OLW-PN
                    address: streetwise
                    phone: +42 33 81394393
                    mnt-by: TEST-MNT
                    notify: test_test@ripe.net
                    source: TEST

                    password: update
        """
        def update = send new Message(body: person)

      then:
        ackFor update

        def notif = notificationFor "modify_test@ripe.net"

        notif.subject =~ "Notification of RIPE Database changes"
        notif.created.any { it.type == "person" && it.key == "some one" }
        notif.modified.any { it.type == "person" && it.key == "some one" }

        notif.contents !=~ /--show-version 1 OLW-PN/
        notif.contents =~ /\+notify/

        noMoreMessages()
    }

    def "update single object fail, notification contains diff"() {
        when:
        def first = """\
                    person: some one
                    nic-hdl: OLW-PN
                    address: street
                    phone: +42 33 81394393
                    mnt-by: TEST-MNT
                    notify: modify_test@ripe.net
                    source: TEST
                    password: update
        """
        def create = send new Message(body: first)

        then:
            def ack = ackFor create

        when:
        def person = """\
                    person: some one
                    nic-hdl: OLW-PN
                    address: streetwise
                    phone: +42 33 81394393
                    mnt-by: TEST-MNT
                    notify: test_test@ripe.net
                    source: TEST

                    password: fail
        """
        def update = send new Message(body: person)

        then:
        ackFor update

        def notif = notificationFor "dbtest@ripe.net"

        notif.subject =~ "RIPE Database updates, auth error notification"

        notif.contents.contains("-notify:         modify_test@ripe.net\n")
        notif.contents.contains("+notify:         test_test@ripe.net\n")
    }

    def "update single maintainer fail, notification contains diff and no auth details"() {
        when:
        databaseHelper.addObject("" +
                "mntner: UPD-MNT\n" +
                "admin-c: TEST-PN\n" +
                "descr: descr\n" +
                "mnt-by: TEST-MNT\n" +
                "mnt-nfy: notif_test@ripe.net\n" +
                "notify: notif_test@ripe.net\n" +
                "upd-to: dbtest@ripe.net\n" +
                "auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update\n" +
                "auth: SSO ssotest@ripe.net\n" +
                "source: TEST")

        def upd = """\
                    mntner: UPD-MNT
                    admin-c: TEST-PN
                    mnt-by: TEST-MNT
                    descr: test
                    notify: notif_test@ripe.net
                    upd-to: dbtest@ripe.net
                    auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                    source: TEST
                    password: fail
        """
        syncUpdate(new SyncUpdate(data: upd))

        then:
        def notif = notificationFor "dbtest@ripe.net"

        notif.subject =~ "RIPE Database updates, auth error notification"

        notif.contents.contains("+descr:          test")
        !notif.contents.contains("auth: SSO ssotest@ripe.net")
        notif.contents.contains("@@ -8,3 +7,2 @@\n" +
                " auth:           MD5-PW # Filtered\n" +
                "-auth:           SSO # Filtered\n")
    }

    def "update, single object, multiple times"() {
      when:
        def mntner = """\
                    mntner: TEST-MNT
                    admin-c: TEST-PN
                    mnt-by: TEST-MNT
                    notify: test_test@ripe.net
                    upd-to: dbtest@ripe.net
                    auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                    descr: first update
                    source: TEST

                    mntner: TEST-MNT
                    admin-c: TEST-PN
                    mnt-by: TEST-MNT
                    notify: test_test@ripe.net
                    upd-to: dbtest@ripe.net
                    auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                    descr: second update
                    source: TEST

                    password: update
        """
        def update = send new Message(body: mntner)

      then:
        ackFor update

        def notif = notificationFor "test_test@ripe.net"
        notif.contents =~ /--show-version 2 TEST-MNT/
        notif.contents =~ /--show-version 3 TEST-MNT/

        notif.subject =~ "Notification of RIPE Database changes"
        notif.modified.size() == 2
        notif.modified.any { it.type == "mntner" && it.key == "TEST-MNT" }

        noMoreMessages()
    }

    def "update, multiple objects, notif sent to notify"() {
      given:
        setTime(LocalDateTime.parse("2013-06-25T09:00:00"))
      when:
        def updates = """\
                    password: update

                    person: some one
                    nic-hdl: OLW-PN
                    address: street
                    phone: +42 33 81394393
                    mnt-by: TEST-MNT
                    notify: modify_person@ripe.net
                    source: TEST

                    mntner: OTHER-MNT
                    admin-c: OLW-PN
                    mnt-by: TEST-MNT
                    descr: description
                    notify: modify_mntner@ripe.net
                    upd-to: dbtest@ripe.net
                    auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                    source: TEST

                    person: some one
                    nic-hdl: TEST-PN
                    address: streetwise
                    phone: +42 33 81394393
                    mnt-by: OTHER-MNT
                    notify: test_test@ripe.net
                    source: TEST

                    mntner: OTHER-MNT
                    admin-c: OLW-PN
                    mnt-by: TEST-MNT
                    descr: description
                    notify: updated_modify_mntner@ripe.net
                    upd-to: dbtest@ripe.net
                    auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                    source: TEST
                    """.stripIndent(true)

      def update = send new Message(body: updates)

      then:
        def ack = ackFor update
        ack.summary.nrFound == 4
        ack.summary.assertSuccess(4, 2, 2, 0, 0)

        ack.contents.contains("Create SUCCEEDED: [person] OLW-PN   some one")
        ack.contents.contains("Create SUCCEEDED: [mntner] OTHER-MNT")
        ack.contents.contains("Modify SUCCEEDED: [person] TEST-PN   some one")
        ack.contents.contains("Modify SUCCEEDED: [mntner] OTHER-MNT")

        def notifModifyMaintainer = notificationFor "modify_mntner@ripe.net"
        notifModifyMaintainer.subject.equals("Notification of RIPE Database changes")

        notifModifyMaintainer.contents.contains(
                "OBJECT BELOW CREATED:\n" +
                "\n" +
                "mntner:         OTHER-MNT\n" +
                "admin-c:        OLW-PN\n" +
                "mnt-by:         TEST-MNT\n" +
                "descr:          description\n" +
                "notify:         modify_mntner@ripe.net\n" +
                "upd-to:         dbtest@ripe.net\n" +
                "auth:           MD5-PW # Filtered\n" +
                "created:        2013-06-25T09:00:00Z\n" +
                "last-modified:  2013-06-25T09:00:00Z\n" +
                "source:         TEST # Filtered\n" +
                "\n" +
                "Changed by password.\n" +
                "\n" +
                "---\n" +
                "OBJECT BELOW MODIFIED:\n" +
                "\n" +
                "@@ -4,3 +4,3 @@\n" +
                " descr:          description\n" +
                "-notify:         modify_mntner@ripe.net\n" +
                "+notify:         updated_modify_mntner@ripe.net\n" +
                " upd-to:         dbtest@ripe.net\n" +
                "\n" +
                "\n" +
                "THIS IS THE NEW VERSION OF THE OBJECT:\n" +
                "\n" +
                "mntner:         OTHER-MNT\n" +
                "admin-c:        OLW-PN\n" +
                "mnt-by:         TEST-MNT\n" +
                "descr:          description\n" +
                "notify:         updated_modify_mntner@ripe.net\n" +
                "upd-to:         dbtest@ripe.net\n" +
                "auth:           MD5-PW # Filtered\n" +
                "created:        2013-06-25T09:00:00Z\n" +
                "last-modified:  2013-06-25T09:00:00Z\n" +
                "source:         TEST # Filtered")

        def notifModifyPerson = notificationFor "modify_person@ripe.net"
        notifModifyPerson.subject.equals("Notification of RIPE Database changes")
        notifModifyPerson.contents.contains(
                "OBJECT BELOW CREATED:\n" +
                "\n" +
                "person:         some one\n" +
                "nic-hdl:        OLW-PN\n" +
                "address:        street\n" +
                "phone:          +42 33 81394393\n" +
                "mnt-by:         TEST-MNT\n" +
                "notify:         modify_person@ripe.net\n" +
                "created:        2013-06-25T09:00:00Z\n" +
                "last-modified:  2013-06-25T09:00:00Z\n" +
                "source:         TEST\n" +
                "\n" +
                "Changed by password.\n" +
                "\n" +
                "\n" +
                "The RIPE Database is subject to Terms and Conditions:\n" +
                "https://docs.db.ripe.net/terms-conditions.html"
        )

        noMoreMessages()
    }

    def "create, organisation notif to ref-nfy"() {
      when:
        def org =
            """\
            organisation: AUTO-1
            org-name:     Test Organisation Ltd
            org-type:     OTHER
            org:          AUTO-1
            descr:        test org
            address:      street 5
            e-mail:       org1@test.com
            mnt-ref:      TEST-MNT
            mnt-by:       TEST-MNT
            ref-nfy:      orgtest@test.net
            source:       TEST
            password:     update
         """.stripIndent(true)

        def create = send new Message(body: org)

      then:
        def ack = ackFor create
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)

        def notifCreateOrg = notificationFor "orgtest@test.net"
        notifCreateOrg.subject.equals("Notification of RIPE Database changes")
        notifCreateOrg.contents.contains("" +
                "OBJECT BELOW CREATED:\n" +
                "\n" +
                "organisation:   ORG-TOL1-TEST\n" +
                "org-name:       Test Organisation Ltd\n" +
                "org-type:       OTHER\n" +
                "org:            ORG-TOL1-TEST\n" +
                "descr:          test org\n" +
                "address:        street 5\n" +
                "e-mail:         org1@test.com\n" +
                "mnt-ref:        TEST-MNT\n" +
                "mnt-by:         TEST-MNT\n" +
                "ref-nfy:        orgtest@test.net"
        )

        noMoreMessages()
    }

    def "create, multiple objects, notif to ref-nfy and notify"() {
      given:
        setTime(LocalDateTime.parse("2013-06-25T09:00:00"))
      when:
        def objects =
            """\
            organisation: AUTO-1
            org-name:     Test Organisation Ltd
            org-type:     OTHER
            org:          AUTO-1
            descr:        test org
            address:      street 5
            e-mail:       org1@test.com
            mnt-ref:      TEST-MNT
            mnt-by:       TEST-MNT
            ref-nfy:      same@test.net
            source:       TEST
            password:     update


            mntner: OTHER-MNT
            admin-c: OLW-PN
            mnt-by: TEST-MNT
            descr: description
            notify: same@test.net
            upd-to: dbtest@ripe.net
            auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            source: TEST


            person: test person
            nic-hdl: OLW-PN
            address: streetwise
            phone: +42 33 81394393
            mnt-by: TEST-MNT
            notify: person@ripe.net
            source: TEST
            password: update
         """.stripIndent(true)

        def creates = send new Message(body: objects)

      then:
        def ack = ackFor creates
        ack.summary.nrFound == 3
        ack.summary.assertSuccess(3, 3, 0, 0, 0)

        def notifCreateObjects = notificationFor "same@test.net"
        notifCreateObjects.subject.equals("Notification of RIPE Database changes")
        notifCreateObjects.contents.contains(
                "OBJECT BELOW CREATED:\n" +
                "\n" +
                "mntner:         OTHER-MNT\n" +
                "admin-c:        OLW-PN\n" +
                "mnt-by:         TEST-MNT\n" +
                "descr:          description\n" +
                "notify:         same@test.net\n" +
                "upd-to:         dbtest@ripe.net\n" +
                "auth:           MD5-PW # Filtered\n" +
                "created:        2013-06-25T09:00:00Z\n" +
                "last-modified:  2013-06-25T09:00:00Z\n" +
                "source:         TEST # Filtered\n" +
                "\n" +
                "Changed by password.\n" +
                "\n" +
                "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "Some object(s) in RIPE Database added references to\n" +
                "objects you are listed in as to-be-notified.\n" +
                "\n" +
                "---\n" +
                "OBJECT BELOW CREATED:\n" +
                "\n" +
                "organisation:   ORG-TOL1-TEST\n" +
                "org-name:       Test Organisation Ltd\n" +
                "org-type:       OTHER\n" +
                "org:            ORG-TOL1-TEST\n" +
                "descr:          test org\n" +
                "address:        street 5\n" +
                "e-mail:         org1@test.com\n" +
                "mnt-ref:        TEST-MNT\n" +
                "mnt-by:         TEST-MNT\n" +
                "ref-nfy:        same@test.net\n" +
                "created:        2013-06-25T09:00:00Z\n" +
                "last-modified:  2013-06-25T09:00:00Z\n" +
                "source:         TEST"
        )

        def notifCreatePerson = notificationFor "person@ripe.net"
        notifCreatePerson.subject.equals("Notification of RIPE Database changes")
        notifCreatePerson.contents.contains(
                "OBJECT BELOW CREATED:\n" +
                "\n" +
                "person:         test person\n" +
                "nic-hdl:        OLW-PN\n" +
                "address:        streetwise\n" +
                "phone:          +42 33 81394393\n" +
                "mnt-by:         TEST-MNT\n" +
                "notify:         person@ripe.net\n" +
                "created:        2013-06-25T09:00:00Z\n" +
                "last-modified:  2013-06-25T09:00:00Z\n" +
                "source:         TEST"
        )

        noMoreMessages()
    }

    def "update, organisation, multiple objects"() {
      when:
      setTime(LocalDateTime.parse("2013-06-25T09:00:00"))
      def currentDateTime = getTimeUtcString()
      def objects =
            """\
            organisation: AUTO-1
            org-name:     Test Organisation Ltd
            org-type:     OTHER
            org:          AUTO-1
            descr:        test org
            address:      street 5
            e-mail:       org1@test.com
            mnt-ref:      TEST-MNT
            mnt-by:       TEST-MNT
            ref-nfy:      same@test.net
            notify:       barry@test.net
            source:       TEST
            password:     update


            mntner: OTHER-MNT
            admin-c: OLW-PN
            mnt-by: TEST-MNT
            descr: description
            notify: same@test.net
            upd-to: dbtest@ripe.net
            auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            source: TEST


            person: test person
            nic-hdl: OLW-PN
            address: streetwise
            phone: +42 33 81394393
            mnt-by: TEST-MNT
            notify: person@ripe.net
            source: TEST
            password: update


            organisation: ORG-TOL1-TEST
            org-name:     Test Organisation Ltd
            org-type:     OTHER
            org:          ORG-TOL1-TEST
            descr:        test org  updated
            address:      street 5
            e-mail:       org1@test.com
            mnt-ref:      TEST-MNT
            mnt-by:       TEST-MNT
            ref-nfy:      notsame@test.net
            notify:       barry@test.net
            source:       TEST
            password:     update


            mntner: OTHER-MNT
            admin-c: OLW-PN
            mnt-by: TEST-MNT
            descr: description  updated
            notify: rutger@test.net
            upd-to: dbtest@ripe.net
            auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            source: TEST


            organisation: ORG-TOL1-TEST
            org-name:     Test Organisation Ltd
            org-type:     OTHER
            org:          ORG-TOL1-TEST
            descr:        test org  updated
            address:      street 5
            e-mail:       org1@test.com
            mnt-ref:      TEST-MNT
            mnt-by:       TEST-MNT
            ref-nfy:      notsame@test.net
            notify:       barry@test.net
            source:       TEST
            password:     update
            delete:       reason
         """.stripIndent(true)

        def updates = send new Message(body: objects)

      then:
        def ack = ackFor updates
        ack.summary.nrFound == 6
        ack.summary.assertSuccess(6, 3, 2, 1, 0)

        def notifPerson = notificationFor "person@ripe.net"
        notifPerson.subject.equals("Notification of RIPE Database changes")
        notifPerson.contents.contains(String.format(
            "OBJECT BELOW CREATED:\n" +
            "\n" +
            "person:         test person\n" +
            "nic-hdl:        OLW-PN\n" +
            "address:        streetwise\n" +
            "phone:          +42 33 81394393\n" +
            "mnt-by:         TEST-MNT\n" +
            "notify:         person@ripe.net\n" +
            "created:        %s\n" +
            "last-modified:  %s\n" +
            "source:         TEST", currentDateTime, currentDateTime))

        def notifRutger = notificationFor "rutger@test.net"
        notifRutger.subject.equals("Notification of RIPE Database changes")
        notifRutger.contents.contains(String.format(
                "OBJECT BELOW MODIFIED:\n" +
                "\n" +
                "@@ -3,4 +3,4 @@\n" +
                " mnt-by:         TEST-MNT\n" +
                "-descr:          description  updated\n" +
                "-notify:         rutger@test.net\n" +
                "+descr:          description\n" +
                "+notify:         same@test.net\n" +
                " upd-to:         dbtest@ripe.net\n" +
                "\n" +
                "\n" +
                "THIS IS THE NEW VERSION OF THE OBJECT:\n" +
                "\n" +
                "mntner:         OTHER-MNT\n" +
                "admin-c:        OLW-PN\n" +
                "mnt-by:         TEST-MNT\n" +
                "descr:          description\n" +
                "notify:         same@test.net\n" +
                "upd-to:         dbtest@ripe.net\n" +
                "auth:           MD5-PW # Filtered\n" +
                "created:        %s\n" +
                "last-modified:  %s\n" +
                "source:         TEST # Filtered\n" +
                "\n" +
                "The old object can be seen in the history using the query options --list-versions and --show-version 1 OTHER-MNT\n" +
                "\n" +
                "Changed by password.\n" +
                "\n" +
                "---\n" +
                "OBJECT BELOW CREATED:\n" +
                "\n" +
                "mntner:         OTHER-MNT\n" +
                "admin-c:        OLW-PN\n" +
                "mnt-by:         TEST-MNT\n" +
                "descr:          description  updated\n" +
                "notify:         rutger@test.net\n" +
                "upd-to:         dbtest@ripe.net\n" +
                "auth:           MD5-PW # Filtered\n" +
                "created:        %s\n" +
                "last-modified:  %s\n" +
                "source:         TEST # Filtered",
                currentDateTime, currentDateTime, currentDateTime, currentDateTime)
        )

        def notifBarry = notificationFor "barry@test.net"
        notifBarry.subject.equals("Notification of RIPE Database changes")
        notifBarry.contents.contains(String.format(
                "---\n" +
                "OBJECT BELOW CREATED:\n" +
                "\n" +
                "organisation:   ORG-TOL1-TEST\n" +
                "org-name:       Test Organisation Ltd\n" +
                "org-type:       OTHER\n" +
                "org:            ORG-TOL1-TEST\n" +
                "descr:          test org\n" +
                "address:        street 5\n" +
                "e-mail:         org1@test.com\n" +
                "mnt-ref:        TEST-MNT\n" +
                "mnt-by:         TEST-MNT\n" +
                "ref-nfy:        same@test.net\n" +
                "notify:         barry@test.net\n" +
                "created:        %s\n" +
                "last-modified:  %s\n" +
                "source:         TEST\n" +
                "\n" +
                "Changed by password.\n" +
                "\n" +
                "---\n" +
                "OBJECT BELOW MODIFIED:\n" +
                "\n" +
                "@@ -4,3 +4,3 @@\n" +
                " org:            ORG-TOL1-TEST\n" +
                "-descr:          test org\n" +
                "+descr:          test org  updated\n" +
                " address:        street 5\n" +
                "@@ -9,3 +9,3 @@\n" +
                " mnt-by:         TEST-MNT\n" +
                "-ref-nfy:        same@test.net\n" +
                "+ref-nfy:        notsame@test.net\n" +
                " notify:         barry@test.net\n" +
                "\n" +
                "\n" +
                "THIS IS THE NEW VERSION OF THE OBJECT:\n" +
                "\n" +
                "organisation:   ORG-TOL1-TEST\n" +
                "org-name:       Test Organisation Ltd\n" +
                "org-type:       OTHER\n" +
                "org:            ORG-TOL1-TEST\n" +
                "descr:          test org  updated\n" +
                "address:        street 5\n" +
                "e-mail:         org1@test.com\n" +
                "mnt-ref:        TEST-MNT\n" +
                "mnt-by:         TEST-MNT\n" +
                "ref-nfy:        notsame@test.net\n" +
                "notify:         barry@test.net\n" +
                "created:        %s\n" +
                "last-modified:  %s\n" +
                "source:         TEST\n" +
                "\n" +
                "The old object can be seen in the history using the query options --list-versions and --show-version\n" +
                "\n" +
                "Changed by password.\n" +
                "\n" +
                "---\n" +
                "OBJECT BELOW DELETED:\n" +
                "\n" +
                "organisation:   ORG-TOL1-TEST\n" +
                "org-name:       Test Organisation Ltd\n" +
                "org-type:       OTHER\n" +
                "org:            ORG-TOL1-TEST\n" +
                "descr:          test org  updated\n" +
                "address:        street 5\n" +
                "e-mail:         org1@test.com\n" +
                "mnt-ref:        TEST-MNT\n" +
                "mnt-by:         TEST-MNT\n" +
                "ref-nfy:        notsame@test.net\n" +
                "notify:         barry@test.net\n" +
                "created:        %s\n" +
                "last-modified:  %s\n" +
                "source:         TEST\n" +
                "\n" +
                "***Info:    reason\n" +
                "\n" +
                "Changed by password.\n" +
                "\n" +
                "\n" +
                "The RIPE Database is subject to Terms and Conditions:\n",
                currentDateTime, currentDateTime, currentDateTime, currentDateTime, currentDateTime, currentDateTime))

        noMoreMessages()
    }

    def "create, single notif to irt-nfy"() {
      given:
        setTime(LocalDateTime.parse("2013-06-25T09:00:00"))
      when:
        def irt = """\
                inet6num: 2001::/48
                netname: RIPE-NCC
                descr: some description
                country: DK
                admin-c: TEST-PN
                tech-c: TEST-PN
                status: ASSIGNED
                mnt-by: TEST-MNT
                mnt-irt: irt-IRT1
                source: TEST
                password: emptypassword
                password: update


                irt:        irt-IRT1
                address:    Street 1
                e-mail:     test@ripe.net
                admin-c:    TEST-PN
                tech-c:     TEST-PN
                auth:       MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                mnt-by:     TEST-MNT
                irt-nfy:    irt@test.net
                source:     TEST
                password:   update
        """
        def update = send new Message(body: irt)

        def ack = ackFor update

      then:
        ack.summary.nrFound == 2
        ack.summary.assertSuccess(2, 2, 0, 0, 0)

        def notifIrt = notificationFor "irt@test.net"
        notifIrt.subject.equals("Notification of RIPE Database changes")
        notifIrt.contents.contains(
                "OBJECT BELOW CREATED:\n" +
                "\n" +
                "inet6num:       2001::/48\n" +
                "netname:        RIPE-NCC\n" +
                "descr:          some description\n" +
                "country:        DK\n" +
                "admin-c:        TEST-PN\n" +
                "tech-c:         TEST-PN\n" +
                "status:         ASSIGNED\n" +
                "mnt-by:         TEST-MNT\n" +
                "mnt-irt:        irt-IRT1\n" +
                "created:        2013-06-25T09:00:00Z\n" +
                "last-modified:  2013-06-25T09:00:00Z\n" +
                "source:         TEST"
        )
        noMoreMessages()
    }

    def "update, single notif to irt-nfy"() {
      given:
        setTime(LocalDateTime.parse("2013-06-25T09:00:00"))
      when:
        def irt = """\
                inet6num: 2001::/48
                netname: RIPE-NCC
                descr: some description
                country: DE
                admin-c: TEST-PN
                tech-c: TEST-PN
                status: ASSIGNED
                mnt-by: TEST-MNT
                mnt-irt: irt-IRT1
                source: TEST
                password: emptypassword
                password: update


                irt:        irt-IRT1
                address:    Street 1
                e-mail:     test@ripe.net
                admin-c:    TEST-PN
                tech-c:     TEST-PN
                auth:       MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                mnt-by:     TEST-MNT
                irt-nfy:    irt@test.net
                source:     TEST
                password:   update


                inet6num: 2001::/48
                netname: RIPE-NCC
                descr: some description
                country: DK
                admin-c: TEST-PN
                tech-c: TEST-PN
                status: ASSIGNED
                mnt-by: TEST-MNT
                mnt-irt: irt-IRT1
                source: TEST
                password: emptypassword
                password: update
                """

        def create = send new Message(body: irt)

      then:
        def createAck = ackFor create
        createAck.summary.nrFound == 3
        createAck.summary.assertSuccess(3, 2, 1, 0, 0)

        def notifIrt = notificationFor "irt@test.net"
        notifIrt.subject.equals("Notification of RIPE Database changes")

        notifIrt.contents.contains(
            "OBJECT BELOW CREATED:\n\n" +
            "inet6num:       2001::/48\n" +
            "netname:        RIPE-NCC\n" +
            "descr:          some description\n" +
            "country:        DK\n" +
            "admin-c:        TEST-PN\n" +
            "tech-c:         TEST-PN\n" +
            "status:         ASSIGNED\n" +
            "mnt-by:         TEST-MNT\n" +
            "mnt-irt:        irt-IRT1\n" +
            "created:        2013-06-25T09:00:00Z\n" +
            "last-modified:  2013-06-25T09:00:00Z\n" +
            "source:         TEST")

        noMoreMessages()
    }


}
