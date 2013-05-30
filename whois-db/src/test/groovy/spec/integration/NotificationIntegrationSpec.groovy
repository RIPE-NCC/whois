package spec.integration

import net.ripe.db.whois.common.IntegrationTest
import spec.domain.Message
import spec.domain.SyncUpdate

@org.junit.experimental.categories.Category(IntegrationTest.class)
class NotificationIntegrationSpec extends BaseWhoisSourceSpec {
    @Override
    Map<String, String> getFixtures() {
        return ["TEST-MNT": """\
                    mntner: TEST-MNT
                    admin-c: TEST-PN
                    mnt-by: TEST-MNT
                    referral-by: TEST-MNT
                    notify: test_test@ripe.net
                    upd-to: dbtest@ripe.net
                    auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                    source: TEST
                """,
                "TEST-PN": """\
                    person: some one
                    nic-hdl: TEST-PN
                    mnt-by: TEST-MNT
                    changed: ripe@test.net 20121221
                    source: TEST
                """,
                "INETROOT":"""\
                    inet6num: 0::/0
                    netname: IANA-BLK
                    descr: The whole IPv4 address space
                    country: DK
                    admin-c: TEST-PN
                    tech-c: TEST-PN
                    status: ALLOCATED-BY-RIR
                    mnt-by: TEST-MNT
                    changed: ripe@test.net 20120505
                    source: TEST"""]
    }

    def "create, single object, notif sent to notify"() {
      when:
        def create = """\
                mntner: ADMIN-MNT
                descr: description
                admin-c: TEST-PN
                mnt-by: ADMIN-MNT
                notify: notify_test@ripe.net
                referral-by: ADMIN-MNT
                upd-to: dbtest@ripe.net
                auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                changed: dbtest@ripe.net 20120707
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
                    changed: ripe@test.net 20121221
                    source: TEST

                    mntner: TEST-MNT
                    mnt-nfy: test_test@ripe.net
                    admin-c: TEST-PN
                    mnt-by: TEST-MNT
                    referral-by: TEST-MNT
                    notify: test_test@ripe.net
                    upd-to: dbtest@ripe.net
                    auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                    changed: dbtest@ripe.net 20121221
                    descr:  description
                    source: TEST

                    password: update

                """.stripIndent(),
                subject: "update"
        )

      then:
        ackFor firstUpdate
        notificationFor "test_test@ripe.net"
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
                    changed: ripe@test.net 20121221
                    remarks: updated again
                    source: TEST

                    password: update
                """.stripIndent(),
                subject: "update"
        )

      then:
        ackFor secondUpdate

        def notification = notificationFor "test_test@ripe.net"

        notification.subject =~ "Notification of RIPE Database changes"
        notification.contents =~ /(?ms)OBJECT BELOW MODIFIED:\n\nperson:         some one\n.*\nOBJECT BELOW MODIFIED:\n\nperson:         some one\n/
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
                referral-by: ADMIN-MNT
                upd-to: dbtest@ripe.net
                auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                changed: dbtest@ripe.net 20120707
                source: TEST

                mntner: TEST-MNT
                descr: description
                admin-c: TEST-PN
                mnt-by: ADMIN-MNT
                notify: test_test@ripe.net
                referral-by: TEST-MNT
                upd-to: test@ripe.net
                auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                changed: test@ripe.net 20120707
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
        updateNotif.contents =~ /OBJECT BELOW MODIFIED:\n\nmntner:         TEST-MNT\nadmin-c:        TEST-PN/
        updateNotif.contents =~ /REPLACED BY:\n\nmntner:         TEST-MNT\ndescr:          description/


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
                referral-by: ADMIN-MNT
                upd-to: dbtest@ripe.net
                auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                changed: dbtest@ripe.net 20120707
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
                referral-by: ADMIN-MNT
                upd-to: dbtest@ripe.net
                auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                changed: dbtest@ripe.net 20120707
                source: TEST
                override: dbase1,override1,{notify=false}
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
                referral-by: ADMIN-MNT
                upd-to: dbtest@ripe.net
                auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                changed: dbtest@ripe.net 20120707
                source: TEST
                override: dbase1,override1,{notify=true}
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
                referral-by: ADMIN-MNT
                upd-to: dbtest@ripe.net
                auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                changed: dbtest@ripe.net 20120707
                source: TEST

                password: update


                mntner: TEST-MNT
                descr: description
                admin-c: TEST-PN
                mnt-by: ADMIN-MNT
                notify: test_test@ripe.net
                referral-by: TEST-MNT
                upd-to: test@ripe.net
                auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                changed: test@ripe.net 20120707
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
                    changed: ripe@test.net
                    source: TEST
                    password: update

                    person: some one
                    nic-hdl: OLW-PN
                    address: streetwise
                    phone: +42 33 81394393
                    mnt-by: TEST-MNT
                    notify: test_test@ripe.net
                    changed: ripe@test.net
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

        noMoreMessages()
    }

    def "update, single object, multiple times"() {
      when:
        def mntner = """\
                    mntner: TEST-MNT
                    admin-c: TEST-PN
                    mnt-by: TEST-MNT
                    referral-by: TEST-MNT
                    notify: test_test@ripe.net
                    upd-to: dbtest@ripe.net
                    auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                    changed: test@ripe.net
                    descr: first update
                    source: TEST

                    mntner: TEST-MNT
                    admin-c: TEST-PN
                    mnt-by: TEST-MNT
                    referral-by: TEST-MNT
                    notify: test_test@ripe.net
                    upd-to: dbtest@ripe.net
                    auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                    changed: test@ripe.net
                    descr: second update
                    source: TEST

                    password: update
        """
        def update = send new Message(body: mntner)

      then:
        ackFor update

        def notif = notificationFor "test_test@ripe.net"

        notif.subject =~ "Notification of RIPE Database changes"
        notif.modified.size() == 2
        notif.modified.any { it.type == "mntner" && it.key == "TEST-MNT" }

        noMoreMessages()
    }

    def "update, multiple objects, notif sent to notify"() {
      when:
        def updates = """\
                    password: update

                    person: some one
                    nic-hdl: OLW-PN
                    address: street
                    phone: +42 33 81394393
                    mnt-by: TEST-MNT
                    notify: modify_person@ripe.net
                    changed: ripe@test.net
                    source: TEST

                    mntner: OTHER-MNT
                    admin-c: OLW-PN
                    mnt-by: TEST-MNT
                    descr: description
                    referral-by: TEST-MNT
                    changed: ripe@test.net 20121221
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
                    changed: ripe@test.net
                    source: TEST

                    mntner: OTHER-MNT
                    admin-c: OLW-PN
                    mnt-by: TEST-MNT
                    descr: description
                    referral-by: TEST-MNT
                    changed: ripe@test.net 20121221
                    notify: updated_modify_mntner@ripe.net
                    upd-to: dbtest@ripe.net
                    auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                    source: TEST
                    """.stripIndent()

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

        notifModifyMaintainer.contents.contains("" +
                "OBJECT BELOW CREATED:\n" +
                "\n" +
                "mntner:         OTHER-MNT\n" +
                "admin-c:        OLW-PN\n" +
                "mnt-by:         TEST-MNT\n" +
                "descr:          description\n" +
                "referral-by:    TEST-MNT\n" +
                "changed:        ripe@test.net 20121221\n" +
                "notify:         modify_mntner@ripe.net\n" +
                "upd-to:         dbtest@ripe.net\n" +
                "auth:           MD5-PW # Filtered\n" +
                "source:         TEST # Filtered\n" +
                "\n" +
                "---\n" +
                "OBJECT BELOW MODIFIED:\n" +
                "\n" +
                "mntner:         OTHER-MNT\n" +
                "admin-c:        OLW-PN\n" +
                "mnt-by:         TEST-MNT\n" +
                "descr:          description\n" +
                "referral-by:    TEST-MNT\n" +
                "changed:        ripe@test.net 20121221\n" +
                "notify:         modify_mntner@ripe.net\n" +
                "upd-to:         dbtest@ripe.net\n" +
                "auth:           MD5-PW # Filtered\n" +
                "source:         TEST # Filtered\n" +
                "\n" +
                "REPLACED BY:\n" +
                "\n" +
                "mntner:         OTHER-MNT\n" +
                "admin-c:        OLW-PN\n" +
                "mnt-by:         TEST-MNT\n" +
                "descr:          description\n" +
                "referral-by:    TEST-MNT\n" +
                "changed:        ripe@test.net 20121221\n" +
                "notify:         updated_modify_mntner@ripe.net\n" +
                "upd-to:         dbtest@ripe.net\n" +
                "auth:           MD5-PW # Filtered\n" +
                "source:         TEST # Filtered")


        def notifModifyPerson = notificationFor "modify_person@ripe.net"
        notifModifyPerson.subject.equals("Notification of RIPE Database changes")
        notifModifyPerson.contents.contains("" +
                "OBJECT BELOW CREATED:\n" +
                "\n" +
                "person:         some one\n" +
                "nic-hdl:        OLW-PN\n" +
                "address:        street\n" +
                "phone:          +42 33 81394393\n" +
                "mnt-by:         TEST-MNT\n" +
                "notify:         modify_person@ripe.net\n" +
                "changed:        ripe@test.net\n" +
                "source:         TEST\n" +
                "\n" +
                "\n" +
                "The RIPE Database is subject to Terms and Conditions:\n" +
                "http://www.ripe.net/db/support/db-terms-conditions.pdf"
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
            changed:      dbtest@ripe.net 20120505
            source:       TEST
            password:     update
         """.stripIndent()

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
            changed:      dbtest@ripe.net 20120505
            source:       TEST
            password:     update


            mntner: OTHER-MNT
            admin-c: OLW-PN
            mnt-by: TEST-MNT
            descr: description
            referral-by: TEST-MNT
            changed: ripe@test.net
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
            changed: ripe@test.net
            source: TEST
            password: update
         """.stripIndent()

        def creates = send new Message(body: objects)

      then:
        def ack = ackFor creates
        ack.summary.nrFound == 3
        ack.summary.assertSuccess(3, 3, 0, 0, 0)

        def notifCreateObjects = notificationFor "same@test.net"
        notifCreateObjects.subject.equals("Notification of RIPE Database changes")
        notifCreateObjects.contents.contains("" +
                "OBJECT BELOW CREATED:\n" +
                "\n" +
                "mntner:         OTHER-MNT\n" +
                "admin-c:        OLW-PN\n" +
                "mnt-by:         TEST-MNT\n" +
                "descr:          description\n" +
                "referral-by:    TEST-MNT\n" +
                "changed:        ripe@test.net\n" +
                "notify:         same@test.net\n" +
                "upd-to:         dbtest@ripe.net\n" +
                "auth:           MD5-PW # Filtered\n" +
                "source:         TEST # Filtered\n" +
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
                "changed:        dbtest@ripe.net 20120505\n" +
                "source:         TEST"
        )

        def notifCreatePerson = notificationFor "person@ripe.net"
        notifCreatePerson.subject.equals("Notification of RIPE Database changes")
        notifCreatePerson.contents.contains("" +
                "OBJECT BELOW CREATED:\n" +
                "\n" +
                "person:         test person\n" +
                "nic-hdl:        OLW-PN\n" +
                "address:        streetwise\n" +
                "phone:          +42 33 81394393\n" +
                "mnt-by:         TEST-MNT\n" +
                "notify:         person@ripe.net\n" +
                "changed:        ripe@test.net\n" +
                "source:         TEST"
        )

        noMoreMessages()
    }

    def "update, organisation, multiple objects"() {
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
            changed:      dbtest@ripe.net 20120505
            notify:       barry@test.net
            source:       TEST
            password:     update


            mntner: OTHER-MNT
            admin-c: OLW-PN
            mnt-by: TEST-MNT
            descr: description
            referral-by: TEST-MNT
            changed: ripe@test.net 20120505
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
            changed: ripe@test.net
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
            changed:      dbtest@ripe.net 20120505
            notify:       barry@test.net
            source:       TEST
            password:     update


            mntner: OTHER-MNT
            admin-c: OLW-PN
            mnt-by: TEST-MNT
            descr: description  updated
            referral-by: TEST-MNT
            changed: ripe@test.net 20120505
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
            changed:      dbtest@ripe.net 20120505
            notify:       barry@test.net
            source:       TEST
            password:     update
            delete:       reason
         """.stripIndent()

        def updates = send new Message(body: objects)

      then:
        def ack = ackFor updates
        ack.summary.nrFound == 6
        ack.summary.assertSuccess(6, 3, 2, 1, 0)

        def notifPerson = notificationFor "person@ripe.net"
        notifPerson.subject.equals("Notification of RIPE Database changes")
        notifPerson.contents.contains("" +
            "OBJECT BELOW CREATED:\n" +
                "\n" +
                "person:         test person\n" +
                "nic-hdl:        OLW-PN\n" +
                "address:        streetwise\n" +
                "phone:          +42 33 81394393\n" +
                "mnt-by:         TEST-MNT\n" +
                "notify:         person@ripe.net\n" +
                "changed:        ripe@test.net\n" +
                "source:         TEST"
        )

        def notifRutger = notificationFor "rutger@test.net"
        notifRutger.subject.equals("Notification of RIPE Database changes")
        notifRutger.contents.contains("OBJECT BELOW MODIFIED:\n\n" +
                "mntner:         OTHER-MNT\n" +
                "admin-c:        OLW-PN\n" +
                "mnt-by:         TEST-MNT\n" +
                "descr:          description  updated\n" +
                "referral-by:    TEST-MNT\n" +
                "changed:        ripe@test.net 20120505\n" +
                "notify:         rutger@test.net\n" +
                "upd-to:         dbtest@ripe.net\n" +
                "auth:           MD5-PW # Filtered\n" +
                "source:         TEST # Filtered\n" +
                "\n" +
                "REPLACED BY:\n" +
                "\n" +
                "mntner:         OTHER-MNT\n" +
                "admin-c:        OLW-PN\n" +
                "mnt-by:         TEST-MNT\n" +
                "descr:          description\n" +
                "referral-by:    TEST-MNT\n" +
                "changed:        ripe@test.net 20120505\n" +
                "notify:         same@test.net\n" +
                "upd-to:         dbtest@ripe.net\n" +
                "auth:           MD5-PW # Filtered\n" +
                "source:         TEST # Filtered\n\n" +
                "---\n" +
                "OBJECT BELOW CREATED:\n" +
                "\n" +
                "mntner:         OTHER-MNT\n" +
                "admin-c:        OLW-PN\n" +
                "mnt-by:         TEST-MNT\n" +
                "descr:          description  updated\n" +
                "referral-by:    TEST-MNT\n" +
                "changed:        ripe@test.net 20120505\n" +
                "notify:         rutger@test.net\n" +
                "upd-to:         dbtest@ripe.net\n" +
                "auth:           MD5-PW # Filtered\n" +
                "source:         TEST # Filtered"
        )

        def notifBarry = notificationFor "barry@test.net"
        notifBarry.subject.equals("Notification of RIPE Database changes")
        notifBarry.contents.contains("" +
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
                "changed:        dbtest@ripe.net 20120505\n" +
                "notify:         barry@test.net\n" +
                "source:         TEST\n" +
                "\n" +
                "---\n" +
                "OBJECT BELOW MODIFIED:\n" +
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
                "changed:        dbtest@ripe.net 20120505\n" +
                "notify:         barry@test.net\n" +
                "source:         TEST\n" +
                "\n" +
                "REPLACED BY:\n" +
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
                "changed:        dbtest@ripe.net 20120505\n" +
                "notify:         barry@test.net\n" +
                "source:         TEST\n" +
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
                "changed:        dbtest@ripe.net 20120505\n" +
                "notify:         barry@test.net\n" +
                "source:         TEST\n" +
                "\n" +
                "***Info:    reason\n" +
                "\n" +
                "\n" +
                "The RIPE Database is subject to Terms and Conditions:")

        noMoreMessages()
    }

    def "create, single notif to irt-nfy"() {
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
                changed: org@ripe.net 20120505
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
                changed:    test@ripe.net 20120505
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
        notifIrt.contents.contains("" +
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
                "changed:        org@ripe.net 20120505\n" +
                "source:         TEST"
        )
        noMoreMessages()
    }

    def "update, single notif to irt-nfy"() {
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
                changed: org@ripe.net 20120505
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
                changed:    test@ripe.net 20120505
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
                changed: org@ripe.net 20120505
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
            "changed:        org@ripe.net 20120505\n" +
            "source:         TEST")

        noMoreMessages()
    }


}
