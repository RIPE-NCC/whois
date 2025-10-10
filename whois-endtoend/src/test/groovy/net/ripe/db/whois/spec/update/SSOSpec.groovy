package net.ripe.db.whois.spec.update



import net.ripe.db.whois.common.rpsl.ObjectType
import net.ripe.db.whois.spec.BaseQueryUpdateSpec
import net.ripe.db.whois.spec.domain.AckResponse

@org.junit.jupiter.api.Tag("IntegrationTest")
class SSOSpec extends BaseQueryUpdateSpec {

    @Override
    Map<String, String> getTransients() {
        [
                "NO-SSO-MNT": """\
                mntner:      NO-SSO-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      updto_test@ripe.net
                auth:        MD5-PW \$1\$yntkntNY\$k8Fr7y5mq17LQcbL4CNLf.  #sso
                mnt-by:      NO-SSO-MNT
                source:      TEST
                """,
                "ONE-SSO-MNT": """\
                mntner:      ONE-SSO-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      updto_test@ripe.net
                auth:        MD5-PW \$1\$yntkntNY\$k8Fr7y5mq17LQcbL4CNLf.  #sso
                auth:        SSO test@ripe.net
                mnt-by:      ONE-SSO-MNT
                source:      TEST
                """,
        ]
    }

    def "add SSO to mntner"() {
        given:
        syncUpdate(getTransient("NO-SSO-MNT") + "password: sso")

        expect:
        query_object_not_matches("-r -BG -T mntner NO-SSO-MNT", "mntner", "NO-SSO-MNT", "auth:\\s*SSO")

        when:
        def message = syncUpdate("""\
                mntner:      NO-SSO-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      updto_test@ripe.net
                auth:        MD5-PW \$1\$yntkntNY\$k8Fr7y5mq17LQcbL4CNLf.  #sso
                auth:        SSO db-test@ripe.net
                mnt-by:      NO-SSO-MNT
                source:      TEST

                password: sso
                """.stripIndent(true)
        )

        then:
        def objLU = restLookup(ObjectType.MNTNER, "NO-SSO-MNT", "sso");
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any {it.operation == "Modify" && it.key == "[mntner] NO-SSO-MNT"}

        hasAttribute(objLU, "auth", "SSO db-test@ripe.net", null);
        hasAttribute(objLU, "auth", "MD5-PW \$1\$yntkntNY\$k8Fr7y5mq17LQcbL4CNLf.", "sso");

        query_object_matches("-r -BG -T mntner NO-SSO-MNT", "mntner", "NO-SSO-MNT", "auth:\\s*SSO")

        def fullObj = databaseHelper.lookupObject(ObjectType.MNTNER, "NO-SSO-MNT")
        fullObj =~ /auth:\s+SSO ed7cd420-6402-11e3-949a-0800200c9a66/
    }

    def "replace pw with SSO in mntner"() {
        given:
        syncUpdate(getTransient("NO-SSO-MNT") + "password: sso")

        expect:
        query_object_not_matches("-r -BG -T mntner NO-SSO-MNT", "mntner", "NO-SSO-MNT", "auth:\\s*SSO")

        when:
        def message = syncUpdate("""\
                mntner:      NO-SSO-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      updto_test@ripe.net
                auth:        SSO db-test@ripe.net
                mnt-by:      NO-SSO-MNT
                source:      TEST

                password: sso
                """.stripIndent(true)
        )

        then:
        def objLU = restLookup(ObjectType.MNTNER, "NO-SSO-MNT");
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any {it.operation == "Modify" && it.key == "[mntner] NO-SSO-MNT"}

        hasAttribute(objLU, "auth", "SSO", "Filtered");

        query_object_matches("-r -BG -T mntner NO-SSO-MNT", "mntner", "NO-SSO-MNT", "auth:\\s*SSO")
        query_object_not_matches("-r -BG -T mntner NO-SSO-MNT", "mntner", "NO-SSO-MNT", "auth:\\s*MD5")

        def fullObj = databaseHelper.lookupObject(ObjectType.MNTNER, "NO-SSO-MNT")
        fullObj =~ /auth:\s+SSO ed7cd420-6402-11e3-949a-0800200c9a66/
        fullObj !=~ /auth:\s+MD5-PW/
    }

    def "add SSO to mntner with existing SSO"() {
        given:
        syncUpdate(getTransient("ONE-SSO-MNT") + "password: sso")

        expect:
        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*SSO")

        when:
        def message = syncUpdate("""\
                mntner:      ONE-SSO-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      updto_test@ripe.net
                auth:        MD5-PW \$1\$yntkntNY\$k8Fr7y5mq17LQcbL4CNLf.  #sso
                auth:        SSO test@ripe.net
                auth:        SSO db-test@ripe.net
                mnt-by:      ONE-SSO-MNT
                source:      TEST

                password: sso
                """.stripIndent(true)
        )

        then:
        def objLU = restLookup(ObjectType.MNTNER, "ONE-SSO-MNT", "sso");
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any {it.operation == "Modify" && it.key == "[mntner] ONE-SSO-MNT"}

        hasAttribute(objLU, "auth", "SSO test@ripe.net", null);
        hasAttribute(objLU, "auth", "SSO db-test@ripe.net", null);
        hasAttribute(objLU, "auth", "MD5-PW \$1\$yntkntNY\$k8Fr7y5mq17LQcbL4CNLf.", "sso");

        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*SSO")

        def fullObj = databaseHelper.lookupObject(ObjectType.MNTNER, "ONE-SSO-MNT")
        fullObj =~ /auth:\s+SSO 8ffe29be-89ef-41c8-ba7f-0e1553a623e5/
        fullObj =~ /auth:\s+SSO ed7cd420-6402-11e3-949a-0800200c9a66/
        fullObj =~ /auth:\s+MD5-PW/
    }

    def "replace pw with SSO in mntner with existing SSO"() {
        given:
        syncUpdate(getTransient("ONE-SSO-MNT") + "password: sso")

        expect:
        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*SSO")

        when:
        def message = syncUpdate("""\
                mntner:      ONE-SSO-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      updto_test@ripe.net
                auth:        SSO test@ripe.net
                auth:        SSO db-test@ripe.net
                mnt-by:      ONE-SSO-MNT
                source:      TEST

                password: sso
                """.stripIndent(true)
        )

        then:
        def objLU = restLookup(ObjectType.MNTNER, "ONE-SSO-MNT");
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any {it.operation == "Modify" && it.key == "[mntner] ONE-SSO-MNT"}

        hasAttribute(objLU, "auth", "SSO", "Filtered");

        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*SSO")
        query_object_not_matches("-r -BG -T mntner NO-SSO-MNT", "mntner", "NO-SSO-MNT", "auth:\\s*MD5")

        def fullObj = databaseHelper.lookupObject(ObjectType.MNTNER, "ONE-SSO-MNT")
        fullObj =~ /auth:\s+SSO 8ffe29be-89ef-41c8-ba7f-0e1553a623e5/
        fullObj =~ /auth:\s+SSO ed7cd420-6402-11e3-949a-0800200c9a66/
        fullObj !=~ /auth:\s+MD5-PW/
    }

    def "replace SSO with new SSO in mntner with existing SSO"() {
        given:
        syncUpdate(getTransient("ONE-SSO-MNT") + "password: sso")

        expect:
        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*SSO")

        when:
        def message = syncUpdate("""\
                mntner:      ONE-SSO-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      updto_test@ripe.net
                auth:        MD5-PW \$1\$yntkntNY\$k8Fr7y5mq17LQcbL4CNLf.  #sso
                auth:        SSO db-test@ripe.net
                mnt-by:      ONE-SSO-MNT
                source:      TEST

                password: sso
                """.stripIndent(true)
        )

        then:
        def objLU = restLookup(ObjectType.MNTNER, "ONE-SSO-MNT", "sso");
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any {it.operation == "Modify" && it.key == "[mntner] ONE-SSO-MNT"}

        hasAttribute(objLU, "auth", "SSO db-test@ripe.net", null);
        hasAttribute(objLU, "auth", "MD5-PW \$1\$yntkntNY\$k8Fr7y5mq17LQcbL4CNLf.", "sso");

        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*SSO")
        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*MD5")

        def fullObj = databaseHelper.lookupObject(ObjectType.MNTNER, "ONE-SSO-MNT")
        fullObj !=~ /auth:\s+SSO 8ffe29be-89ef-41c8-ba7f-0e1553a623e5/
        fullObj =~ /auth:\s+SSO ed7cd420-6402-11e3-949a-0800200c9a66/
        fullObj =~ /auth:\s+MD5-PW/
    }

    def "remove SSO from mntner with existing SSO"() {
        given:
        syncUpdate(getTransient("ONE-SSO-MNT") + "password: sso")

        expect:
        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*SSO")
        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*MD5")

        when:
        def message = syncUpdate("""\
                mntner:      ONE-SSO-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      updto_test@ripe.net
                auth:        MD5-PW \$1\$yntkntNY\$k8Fr7y5mq17LQcbL4CNLf.  #sso
                mnt-by:      ONE-SSO-MNT
                source:      TEST

                password: sso
                """.stripIndent(true)
        )

        then:
        def objLU = restLookup(ObjectType.MNTNER, "ONE-SSO-MNT", "sso");
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any {it.operation == "Modify" && it.key == "[mntner] ONE-SSO-MNT"}

        hasAttribute(objLU, "auth", "MD5-PW \$1\$yntkntNY\$k8Fr7y5mq17LQcbL4CNLf.", "sso");

        query_object_not_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*SSO")
        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*MD5")

        def fullObj = databaseHelper.lookupObject(ObjectType.MNTNER, "ONE-SSO-MNT")
        fullObj !=~ /auth:\s+SSO/
        fullObj =~ /auth:\s+MD5-PW/
    }

    def "remove pw from mntner with existing SSO"() {
        given:
        syncUpdate(getTransient("ONE-SSO-MNT") + "password: sso")

        expect:
        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*SSO")
        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*MD5")

        when:
        def message = syncUpdate("""\
                mntner:      ONE-SSO-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      updto_test@ripe.net
                auth:        SSO test@ripe.net
                mnt-by:      ONE-SSO-MNT
                source:      TEST

                password: sso
                """.stripIndent(true)
        )

        then:
        def objLU = restLookup(ObjectType.MNTNER, "ONE-SSO-MNT");
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any {it.operation == "Modify" && it.key == "[mntner] ONE-SSO-MNT"}

        hasAttribute(objLU, "auth", "SSO", "Filtered");

        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*SSO")
        query_object_not_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*MD5")

        def fullObj = databaseHelper.lookupObject(ObjectType.MNTNER, "ONE-SSO-MNT")
        fullObj =~ /auth:\s+SSO 8ffe29be-89ef-41c8-ba7f-0e1553a623e5/
        fullObj !=~ /auth:\s+MD5-PW/
    }

    def "remove pw, replace SSO from mntner with existing SSO"() {
        given:
        syncUpdate(getTransient("ONE-SSO-MNT") + "password: sso")

        expect:
        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*SSO")
        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*MD5")

        when:
        def message = syncUpdate("""\
                mntner:      ONE-SSO-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      updto_test@ripe.net
                auth:        SSO db-test@ripe.net
                mnt-by:      ONE-SSO-MNT
                source:      TEST

                password: sso
                """.stripIndent(true)
        )

        then:
        def objLU = restLookup(ObjectType.MNTNER, "ONE-SSO-MNT");
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any {it.operation == "Modify" && it.key == "[mntner] ONE-SSO-MNT"}

        hasAttribute(objLU, "auth", "SSO", "Filtered");

        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*SSO")
        query_object_not_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*MD5")

        def fullObj = databaseHelper.lookupObject(ObjectType.MNTNER, "ONE-SSO-MNT")
        fullObj =~ /auth:\s+SSO ed7cd420-6402-11e3-949a-0800200c9a66/
        fullObj !=~ /auth:\s+MD5-PW/
    }

    def "remove pw, remove SSO from mntner with existing SSO"() {
        given:
        syncUpdate(getTransient("ONE-SSO-MNT") + "password: sso")

        expect:
        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*SSO")
        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*MD5")

        when:
        def message = syncUpdate("""\
                mntner:      ONE-SSO-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      updto_test@ripe.net
                mnt-by:      ONE-SSO-MNT
                source:      TEST

                password: sso
                """.stripIndent(true)
        )

        then:
        def objLU = restLookup(ObjectType.MNTNER, "ONE-SSO-MNT", "sso");
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any {it.operation == "Modify" && it.key == "[mntner] ONE-SSO-MNT"}
        ack.errorMessagesFor("Modify", "[mntner] ONE-SSO-MNT") ==
                ["Mandatory attribute \"auth\" is missing"]

        hasAttribute(objLU, "auth", "SSO test@ripe.net", null);
        hasAttribute(objLU, "auth", "MD5-PW \$1\$yntkntNY\$k8Fr7y5mq17LQcbL4CNLf.", "sso");

        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*SSO")
        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*MD5")

        def fullObj = databaseHelper.lookupObject(ObjectType.MNTNER, "ONE-SSO-MNT")
        fullObj =~ /auth:\s+SSO 8ffe29be-89ef-41c8-ba7f-0e1553a623e5/
        fullObj =~ /auth:\s+MD5-PW/
    }

    def "add 2 SSO to mntner with existing SSO"() {
        given:
        syncUpdate(getTransient("ONE-SSO-MNT") + "password: sso")

        expect:
        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*SSO")

        when:
        def message = syncUpdate("""\
                mntner:      ONE-SSO-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      updto_test@ripe.net
                auth:        MD5-PW \$1\$yntkntNY\$k8Fr7y5mq17LQcbL4CNLf.  #sso
                auth:        SSO test@ripe.net
                auth:        SSO db-test@ripe.net
                auth:        SSO person@net.net
                mnt-by:      ONE-SSO-MNT
                source:      TEST

                password: sso
                """.stripIndent(true)
        )

        then:
        def objLU = restLookup(ObjectType.MNTNER, "ONE-SSO-MNT", "sso");
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any {it.operation == "Modify" && it.key == "[mntner] ONE-SSO-MNT"}

        hasAttribute(objLU, "auth", "SSO db-test@ripe.net", null);
        hasAttribute(objLU, "auth", "SSO person@net.net", null);
        hasAttribute(objLU, "auth", "MD5-PW \$1\$yntkntNY\$k8Fr7y5mq17LQcbL4CNLf.", "sso");

        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*SSO")

        def fullObj = databaseHelper.lookupObject(ObjectType.MNTNER, "ONE-SSO-MNT")
        fullObj =~ /auth:\s+SSO 8ffe29be-89ef-41c8-ba7f-0e1553a623e5/
        fullObj =~ /auth:\s+SSO ed7cd420-6402-11e3-949a-0800200c9a66/
        fullObj =~ /auth:\s+SSO 906635c2-0405-429a-800b-0602bd716124/
        fullObj =~ /auth:\s+MD5-PW/
    }

    def "add invalid SSO to mntner"() {
        given:
        syncUpdate(getTransient("NO-SSO-MNT") + "password: sso")

        expect:
        query_object_matches("-r -BG -T mntner NO-SSO-MNT", "mntner", "NO-SSO-MNT", "auth:\\s*MD5")
        query_object_not_matches("-r -BG -T mntner NO-SSO-MNT", "mntner", "NO-SSO-MNT", "auth:\\s*SSO")

        when:
        def message = syncUpdate("""\
                mntner:      NO-SSO-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      updto_test@ripe.net
                auth:        MD5-PW \$1\$yntkntNY\$k8Fr7y5mq17LQcbL4CNLf.  #sso
                auth:        SSO unknown@ripe.net
                mnt-by:      NO-SSO-MNT
                source:      TEST

                password: sso
                """.stripIndent(true)
        )

        then:
        def objLU = restLookup(ObjectType.MNTNER, "NO-SSO-MNT", "sso");
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any {it.operation == "Modify" && it.key == "[mntner] NO-SSO-MNT"}
        ack.errorMessagesFor("Modify", "[mntner] NO-SSO-MNT") ==
                ["No RIPE NCC Access Account found for unknown@ripe.net"]

        hasAttribute(objLU, "auth", "MD5-PW \$1\$yntkntNY\$k8Fr7y5mq17LQcbL4CNLf.", "sso");

        query_object_not_matches("-r -BG -T mntner NO-SSO-MNT", "mntner", "NO-SSO-MNT", "auth:\\s*SSO")
        query_object_matches("-r -BG -T mntner NO-SSO-MNT", "mntner", "NO-SSO-MNT", "auth:\\s*MD5")
    }

    def "add invalid SSO to mntner with existing SSO"() {
        given:
        syncUpdate(getTransient("ONE-SSO-MNT") + "password: sso")

        expect:
        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*SSO")
        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*MD5")

        when:
        def message = syncUpdate("""\
                mntner:      ONE-SSO-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      updto_test@ripe.net
                auth:        MD5-PW \$1\$yntkntNY\$k8Fr7y5mq17LQcbL4CNLf.  #sso
                auth:        SSO test@ripe.net
                auth:        SSO unknown@ripe.net
                mnt-by:      ONE-SSO-MNT
                source:      TEST

                password: sso
                """.stripIndent(true)
        )

        then:
        def objLU = restLookup(ObjectType.MNTNER, "ONE-SSO-MNT", "sso");
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any {it.operation == "Modify" && it.key == "[mntner] ONE-SSO-MNT"}
        ack.errorMessagesFor("Modify", "[mntner] ONE-SSO-MNT") ==
                ["No RIPE NCC Access Account found for unknown@ripe.net"]

        hasAttribute(objLU, "auth", "SSO test@ripe.net", null);
        hasAttribute(objLU, "auth", "MD5-PW \$1\$yntkntNY\$k8Fr7y5mq17LQcbL4CNLf.", "sso");

        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*SSO")
        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*MD5")
    }

    def "replace SSO with invalid SSO in mntner with existing SSO"() {
        given:
        syncUpdate(getTransient("ONE-SSO-MNT") + "password: sso")

        expect:
        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*SSO")
        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*MD5")

        when:
        def message = syncUpdate("""\
                mntner:      ONE-SSO-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      updto_test@ripe.net
                auth:        MD5-PW \$1\$yntkntNY\$k8Fr7y5mq17LQcbL4CNLf.  #sso
                auth:        SSO unknown@ripe.net
                mnt-by:      ONE-SSO-MNT
                source:      TEST

                password: sso
                """.stripIndent(true)
        )

        then:
        def objLU = restLookup(ObjectType.MNTNER, "ONE-SSO-MNT", "sso");
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any {it.operation == "Modify" && it.key == "[mntner] ONE-SSO-MNT"}
        ack.errorMessagesFor("Modify", "[mntner] ONE-SSO-MNT") ==
                ["No RIPE NCC Access Account found for unknown@ripe.net"]

        hasAttribute(objLU, "auth", "SSO test@ripe.net", null);
        hasAttribute(objLU, "auth", "MD5-PW \$1\$yntkntNY\$k8Fr7y5mq17LQcbL4CNLf.", "sso");

        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*SSO")
        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*MD5")
    }

    def "create mntner with pw and SSO"() {
        given:

        expect:
        queryObjectNotFound("-r -BG -T mntner NO-SSO-MNT", "mntner", "NO-SSO-MNT")

        when:
        def message = syncUpdate("""\
                mntner:      NO-SSO-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      updto_test@ripe.net
                auth:        MD5-PW \$1\$yntkntNY\$k8Fr7y5mq17LQcbL4CNLf.  #sso
                auth:        SSO db-test@ripe.net
                mnt-by:      NO-SSO-MNT
                source:      TEST

                password: sso
                """.stripIndent(true)
        )

        then:
        def objLU = restLookup(ObjectType.MNTNER, "NO-SSO-MNT", "sso");
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any {it.operation == "Create" && it.key == "[mntner] NO-SSO-MNT"}

        hasAttribute(objLU, "auth", "SSO db-test@ripe.net", null);
        hasAttribute(objLU, "auth", "MD5-PW \$1\$yntkntNY\$k8Fr7y5mq17LQcbL4CNLf.", "sso");

        query_object_matches("-r -BG -T mntner NO-SSO-MNT", "mntner", "NO-SSO-MNT", "auth:\\s*SSO")
        query_object_matches("-r -BG -T mntner NO-SSO-MNT", "mntner", "NO-SSO-MNT", "auth:\\s*MD5")

        def fullObj = databaseHelper.lookupObject(ObjectType.MNTNER, "NO-SSO-MNT")
        fullObj =~ /auth:\s+SSO ed7cd420-6402-11e3-949a-0800200c9a66/
        fullObj =~ /auth:\s+MD5-PW/
    }

    def "create mntner with pw and invalid SSO"() {
        given:

        expect:
        queryObjectNotFound("-r -BG -T mntner NO-SSO-MNT", "mntner", "NO-SSO-MNT")

        when:
        def message = syncUpdate("""\
                mntner:      NO-SSO-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      updto_test@ripe.net
                auth:        MD5-PW \$1\$yntkntNY\$k8Fr7y5mq17LQcbL4CNLf.  #sso
                auth:        SSO unknown@ripe.net
                mnt-by:      NO-SSO-MNT
                source:      TEST

                password: sso
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any {it.operation == "Create" && it.key == "[mntner] NO-SSO-MNT"}
        ack.errorMessagesFor("Create", "[mntner] NO-SSO-MNT") ==
                ["No RIPE NCC Access Account found for unknown@ripe.net"]

        queryObjectNotFound("-r -BG -T mntner NO-SSO-MNT", "mntner", "NO-SSO-MNT")
    }

    def "delete mntner with existing SSO"() {
        given:
        syncUpdate(getTransient("ONE-SSO-MNT") + "password: sso")

        expect:
        query_object_matches("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT", "auth:\\s*SSO")

        when:
        def message = syncUpdate("""\
                mntner:      ONE-SSO-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      updto_test@ripe.net
                auth:        MD5-PW \$1\$yntkntNY\$k8Fr7y5mq17LQcbL4CNLf.  #sso
                auth:        SSO test@ripe.net
                mnt-by:      ONE-SSO-MNT
                source:      TEST
                delete:   test

                password: sso
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any {it.operation == "Delete" && it.key == "[mntner] ONE-SSO-MNT"}

        queryObjectNotFound("-r -BG -T mntner ONE-SSO-MNT", "mntner", "ONE-SSO-MNT")
    }

}
