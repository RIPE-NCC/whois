package net.ripe.db.whois.spec.update

import net.ripe.db.whois.common.IntegrationTest
import net.ripe.db.whois.spec.BaseQueryUpdateSpec


@org.junit.experimental.categories.Category(IntegrationTest.class)
class  LirOrganisationAttributeValidationSpec extends BaseQueryUpdateSpec {

    @Override
    Map<String, String> getTransients() {
        [
        "LIR-ORG": """\
            organisation:   AUTO-1
            org-name:       Reseaux IP Europeens Network Coordination Centre (RIPE NCC)
            org-type:       LIR
            address:        P.O. Box 10096
            phone:          +31205354444
            fax-no:         +31205354445
            e-mail:         ncc@ripe.net
            abuse-c:        AH1-TEST
            mnt-ref:        LIR-MNT
            mnt-by:         RIPE-NCC-HM-MNT
            mnt-by:         LIR-MNT
            source:         TEST
        """,
        "LIR-ORG-EXTRA": """\
            organisation:   AUTO-1
            org-name:       Reseaux IP Europeens Network Coordination Centre (RIPE NCC)
            org-type:       LIR
            descr:          RIPE NCC Operations      # extra
            remarks:        Test organisation        # extra
            address:        P.O. Box 10096
            phone:          +31205354444
            fax-no:         +31205354445
            e-mail:         ncc@ripe.net
            geoloc:         0.0 0.0                  # extra
            language:       NL                       # extra
            org:            ORG-OTO1-TEST
            admin-c:        TP1-TEST                 # extra
            tech-c:         TP1-TEST                 # extra
            abuse-c:        AH1-TEST                 # extra
            mnt-ref:        LIR-MNT
            mnt-by:         RIPE-NCC-HM-MNT
            mnt-by:         LIR-MNT
            notify:         notify@ripe.net          # extra
            source:         TEST
        """,
        "LIR-ORG-LIR-LOCKED-ATTRIBUTES": """\
            organisation:   AUTO-1
            org-name:       Reseaux IP Europeens Network Coordination Centre (RIPE NCC)
            org-type:       LIR
            org:            ORG-OTO1-TEST
            abuse-mailbox:  abuse@ripe.net
            address:        P.O. Box 10096
            phone:          +31205354444
            fax-no:         +31205354445
            e-mail:         ncc@ripe.net
            abuse-c:        AH1-TEST
            mnt-ref:        LIR-MNT
            mnt-by:         RIPE-NCC-HM-MNT
            mnt-by:         LIR-MNT
            source:         TEST
        """
        ]
    }

    def "modify portal attributes with lir password should not be possible"() {
        given:
        def ack1 = syncUpdate(getTransient("LIR-ORG") + "override: denis,override1")
        ack1.toString()

        expect:
        queryObject("-GBr -T organisation ORG-RIEN1-TEST", "organisation", "ORG-RIEN1-TEST")

        when:
        def ack = syncUpdateWithResponse("""
            organisation:   ORG-RIEN1-TEST
            org-name:       Reseaux IP Europeens Network Coordination Centre (RIPE NCC) modified
            org-type:       OTHER # modified
            address:        P.O. Box 10096 modified
            phone:          +31111111111 # modified
            fax-no:         +31111111111 # modified
            e-mail:         nccmodified@ripe.net
            abuse-c:        AH1-TEST
            mnt-ref:        LIR-MNT
            mnt-by:         RIPE-NCC-HM-MNT
            mnt-by:         LIR2-MNT
            source:         TEST
            password:       lir
        """.stripIndent()
        )

        then:

        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(7, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[organisation] ORG-RIEN1-TEST" }

        ack.errorMessagesFor("Modify", "[organisation] ORG-RIEN1-TEST") == [
                "Attribute \"address:\" can only be changed by the RIPE NCC for this object. Please contact \"ncc@ripe.net\" to change it.",
                "Attribute \"phone:\" can only be changed by the RIPE NCC for this object. Please contact \"ncc@ripe.net\" to change it.",
                "Attribute \"fax-no:\" can only be changed by the RIPE NCC for this object. Please contact \"ncc@ripe.net\" to change it.",
                "Attribute \"e-mail:\" can only be changed by the RIPE NCC for this object. Please contact \"ncc@ripe.net\" to change it.",
                "Attribute \"mnt-by:\" can only be changed by the RIPE NCC for this object. Please contact \"ncc@ripe.net\" to change it.",
                "Attribute \"org-name:\" can only be changed by the RIPE NCC for this object. Please contact \"ncc@ripe.net\" to change it.",
                "Attribute \"org-type:\" can only be changed by the RIPE NCC for this object. Please contact \"ncc@ripe.net\" to change it."
        ]
    }

    def "delete non mandatory portal attributes with lir password should not be possible"() {
        given:
        def ack1 = syncUpdate(getTransient("LIR-ORG") + "override: denis,override1")
        ack1.toString()

        expect:
        queryObject("-GBr -T organisation ORG-RIEN1-TEST", "organisation", "ORG-RIEN1-TEST")

        when:
        //    phone:          +31205354444 # cannot deleted
        //    fax-no:         +31205354445 # cannot deleted
        //    e-mail:         ncc@ripe.net # cannot deleted
        //    abuse-c:        AH1-TEST     # cannot deleted
        def ack = syncUpdateWithResponse("""
            organisation:   ORG-RIEN1-TEST
            org-name:       Reseaux IP Europeens Network Coordination Centre (RIPE NCC)
            org-type:       LIR
            address:        P.O. Box 10096
            e-mail:         ncc@ripe.net
            mnt-ref:        LIR-MNT
            mnt-by:         RIPE-NCC-HM-MNT
            mnt-by:         LIR-MNT
            source:         TEST
            password:       lir
        """.stripIndent()
        )

        then:

        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(3, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[organisation] ORG-RIEN1-TEST" }
        ack.errorMessagesFor("Modify", "[organisation] ORG-RIEN1-TEST") == [
                "\"abuse-c:\" cannot be removed from an ORGANISATION object referenced by a resource object",
               "Attribute \"phone:\" can only be changed by the RIPE NCC for this object. Please contact \"ncc@ripe.net\" to change it.",
               "Attribute \"fax-no:\" can only be changed by the RIPE NCC for this object. Please contact \"ncc@ripe.net\" to change it."
                ]
    }

    def "add LIR non mandatory portal attributes with lir password"() {
        given:
        def ack1 = syncUpdate(getTransient("LIR-ORG") + "override: denis,override1")
        ack1.toString()

        expect:
        queryObject("-GBr -T organisation ORG-RIEN1-TEST", "organisation", "ORG-RIEN1-TEST")

        when:
        def ack = syncUpdateWithResponse("""
            organisation:   ORG-RIEN1-TEST
            org-name:       Reseaux IP Europeens Network Coordination Centre (RIPE NCC)
            org-type:       LIR
            descr:          RIPE NCC Operations      # added
            remarks:        Test organisation        # added
            address:        P.O. Box 10096
            phone:          +31205354444
            fax-no:         +31205354445
            e-mail:         ncc@ripe.net
            geoloc:         0.0 0.0                  # added
            language:       NL                       # added
            admin-c:        TP1-TEST                 # added
            tech-c:         TP1-TEST                 # added
            abuse-c:        AH1-TEST                 # added
            mnt-ref:        LIR-MNT
            mnt-ref:        LIR2-MNT                 # added
            mnt-by:         RIPE-NCC-HM-MNT
            mnt-by:         LIR-MNT
            notify:         notify@ripe.net          # added
            source:         TEST
            password:       lir
        """.stripIndent()
        )

        then:
        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[organisation] ORG-RIEN1-TEST" }
    }

    def "update LIR non mandatory portal attributes with lir password should not be possible"() {
        given:
        def ack1 = syncUpdate(getTransient("LIR-ORG-EXTRA") + "override: denis,override1")
        ack1.toString()

        expect:
        queryObject("-GBr -T organisation ORG-RIEN1-TEST", "organisation", "ORG-RIEN1-TEST")

        when:
        def ack = syncUpdateWithResponse("""
            organisation:   ORG-RIEN1-TEST
            org-name:       Reseaux IP Europeens Network Coordination Centre (RIPE NCC)
            org-type:       LIR
            descr:          RIPE NCC Operations      # modified
            remarks:        Test organisation        # modified
            address:        P.O. Box 10096
            phone:          +31205354444
            fax-no:         +31205354445
            e-mail:         ncc@ripe.net
            geoloc:         0.0 0.0                  # modified
            language:       NL                       # modified
            org:            ORG-OTO1-TEST
            admin-c:        TP2-TEST                 # modified
            tech-c:         TP2-TEST                 # modified
            abuse-c:        AH1-TEST                 # modified
            mnt-ref:        LIR2-MNT                 # modified
            mnt-by:         RIPE-NCC-HM-MNT
            mnt-by:         LIR-MNT
            notify:         notify@ripe.net          # modified
            source:         TEST
            password:       lir
        """.stripIndent()
        )

        then:
        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[organisation] ORG-RIEN1-TEST" }
    }

    def "delete LIR non mandatory portal attributes with lir password should not be possible"() {
        given:
        def ack1 = syncUpdate(getTransient("LIR-ORG-EXTRA") + "override: denis,override1")
        ack1.toString()

        expect:
        queryObject("-GBr -T organisation ORG-RIEN1-TEST", "organisation", "ORG-RIEN1-TEST")

        when:
        //   descr:          RIPE NCC Operations      # deleted
        //   remarks:        Test organisation        # deleted
        //   geoloc:         0.0 0.0                  # deleted
        //   language:       NL                       # deleted
        //   admin-c:        TP1-TEST                 # deleted
        //   tech-c:         TP1-TEST                 # deleted
        //   notify:         notify@ripe.net          # deleted
        def ack = syncUpdateWithResponse("""
            organisation:   ORG-RIEN1-TEST
            org-name:       Reseaux IP Europeens Network Coordination Centre (RIPE NCC)
            org-type:       LIR
            address:        P.O. Box 10096
            phone:          +31205354444
            fax-no:         +31205354445
            e-mail:         ncc@ripe.net
            org:            ORG-OTO1-TEST
            abuse-c:        AH1-TEST
            mnt-ref:        LIR-MNT
            mnt-by:         RIPE-NCC-HM-MNT
            mnt-by:         LIR-MNT
            source:         TEST
            password:       lir
        """.stripIndent()
        )

        then:
        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[organisation] ORG-RIEN1-TEST" }
    }

    def "modify other lir-locked attributes with lir password should not be possible"() {
        given:
        def ack1 = syncUpdate(getTransient("LIR-ORG-LIR-LOCKED-ATTRIBUTES") + "override: denis,override1")
        ack1.toString()

        expect:
        queryObject("-GBr -T organisation ORG-RIEN1-TEST", "organisation", "ORG-RIEN1-TEST")

        when:
        def ack = syncUpdateWithResponse("""
            organisation:   ORG-RIEN1-TEST
            org-name:       RIPE NCC                             # modified
            org-type:       OTHER                                # modified
            org:            ORG-HR1-TEST                         # modified
            abuse-mailbox:  abuse-modified@ripe.net              # modified
            descr:          RIPE NCC Operations
            address:        New Address, New City, New Country   # modified
            phone:          +31205354444-1                       # modified
            fax-no:         +31205354445-1                       # modified
            e-mail:         different-email@ripe.net             # modified
            admin-c:        TP1-TEST
            abuse-c:        AH1-TEST
            mnt-ref:        LIR-MNT
            mnt-by:         RIPE-NCC-HM-MNT
            mnt-by:         LIR2-MNT                             # modified
            source:         TEST
            password:       lir
            password:       owner3
        """.stripIndent()
        )

        then:

        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(9, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[organisation] ORG-RIEN1-TEST" }

        ack.errorMessagesFor("Modify", "[organisation] ORG-RIEN1-TEST") == [
                "Attribute \"address:\" can only be changed by the RIPE NCC for this object. Please contact \"ncc@ripe.net\" to change it.",
                "Attribute \"phone:\" can only be changed by the RIPE NCC for this object. Please contact \"ncc@ripe.net\" to change it.",
                "Attribute \"fax-no:\" can only be changed by the RIPE NCC for this object. Please contact \"ncc@ripe.net\" to change it.",
                "Attribute \"e-mail:\" can only be changed by the RIPE NCC for this object. Please contact \"ncc@ripe.net\" to change it.",
                "Attribute \"mnt-by:\" can only be changed by the RIPE NCC for this object. Please contact \"ncc@ripe.net\" to change it.",
                "Attribute \"org-name:\" can only be changed by the RIPE NCC for this object. Please contact \"ncc@ripe.net\" to change it.",
                "Attribute \"org:\" can only be changed by the RIPE NCC for this object. Please contact \"ncc@ripe.net\" to change it.",
                "Attribute \"org-type:\" can only be changed by the RIPE NCC for this object. Please contact \"ncc@ripe.net\" to change it.",
                "Attribute \"abuse-mailbox:\" can only be changed by the RIPE NCC for this object. Please contact \"ncc@ripe.net\" to change it."
        ]
    }

    def "add non mandatory lir-locked attributes with lir password should not be possible"() {
        given:
        def ack1 = syncUpdate(getTransient("LIR-ORG") + "override: denis,override1")
        ack1.toString()

        expect:
        queryObject("-GBr -T organisation ORG-RIEN1-TEST", "organisation", "ORG-RIEN1-TEST")

        when:
        def ack = syncUpdateWithResponse("""
            organisation:   ORG-RIEN1-TEST
            org-name:       Reseaux IP Europeens Network Coordination Centre (RIPE NCC)
            org-type:       LIR
            address:        P.O. Box 10096
            address:        Amsterdam, Netherlands   # added
            phone:          +31000000000
            phone:          +31000000001             # added
            fax-no:         +31000000000
            fax-no:         +31000000002             # added
            e-mail:         ncc@ripe.net
            e-mail:         second@ripe.net          # added
            org:            ORG-RIEN1-TEST           # added
            abuse-c:        AH1-TEST
            mnt-ref:        LIR-MNT
            abuse-mailbox:  abuse@ripe.net           # added
            mnt-by:         RIPE-NCC-HM-MNT
            mnt-by:         LIR-MNT
            mnt-by:         LIR2-MNT                 # added
            source:         TEST
            password:       lir
            password:       owner3
        """.stripIndent()
        )

        then:

        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(8, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[organisation] ORG-RIEN1-TEST" }

        ack.errorMessagesFor("Modify", "[organisation] ORG-RIEN1-TEST") == [
                "Multiple user-'mnt-by:' are not allowed, found are: 'LIR-MNT, LIR2-MNT'",
                "Attribute \"address:\" can only be changed by the RIPE NCC for this object. Please contact \"ncc@ripe.net\" to change it.",
                "Attribute \"phone:\" can only be changed by the RIPE NCC for this object. Please contact \"ncc@ripe.net\" to change it.",
                "Attribute \"fax-no:\" can only be changed by the RIPE NCC for this object. Please contact \"ncc@ripe.net\" to change it.",
                "Attribute \"e-mail:\" can only be changed by the RIPE NCC for this object. Please contact \"ncc@ripe.net\" to change it.",
                "Attribute \"mnt-by:\" can only be changed by the RIPE NCC for this object. Please contact \"ncc@ripe.net\" to change it.",
                "Attribute \"org:\" can only be changed by the RIPE NCC for this object. Please contact \"ncc@ripe.net\" to change it.",
                "Attribute \"abuse-mailbox:\" can only be changed by the RIPE NCC for this object. Please contact \"ncc@ripe.net\" to change it."
        ]
    }

    def "delete non mandatory lir-locked attributes with lir password should not be possible"() {
        given:
        def ack1 = syncUpdate(getTransient("LIR-ORG-LIR-LOCKED-ATTRIBUTES") + "override: denis,override1")
        ack1.toString()

        expect:
        queryObject("-GBr -T organisation ORG-RIEN1-TEST", "organisation", "ORG-RIEN1-TEST")

        when:
        //    org:            ORG-OTO1-TEST   # cannot delete
        //    abuse-mailbox:  abuse@ripe.net  # cannot delete
        //    phone:          +31205354444    # cannot delete
        //    fax-no:         +31205354445    # cannot delete
        //    mnt-by:         LIR-MNT         # cannot delete
        def ack = syncUpdateWithResponse("""
            organisation:   ORG-RIEN1-TEST
            org-name:       Reseaux IP Europeens Network Coordination Centre (RIPE NCC)
            org-type:       LIR
            address:        P.O. Box 10096
            e-mail:         ncc@ripe.net
            abuse-c:        AH1-TEST
            mnt-ref:        LIR-MNT
            mnt-by:         RIPE-NCC-HM-MNT
            mnt-by:         LIR-MNT
            source:         TEST
            password:       lir
        """.stripIndent()
        )

        then:

        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(4, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[organisation] ORG-RIEN1-TEST" }

        ack.errorMessagesFor("Modify", "[organisation] ORG-RIEN1-TEST") == [
                "Attribute \"phone:\" can only be changed by the RIPE NCC for this object. Please contact \"ncc@ripe.net\" to change it.",
                "Attribute \"fax-no:\" can only be changed by the RIPE NCC for this object. Please contact \"ncc@ripe.net\" to change it.",
                "Attribute \"org:\" can only be changed by the RIPE NCC for this object. Please contact \"ncc@ripe.net\" to change it.",
                "Attribute \"abuse-mailbox:\" can only be changed by the RIPE NCC for this object. Please contact \"ncc@ripe.net\" to change it."
        ]
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  MODIFY organisation attributes WITH OVERRIDE
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    def "modify portal locked attributes with override should be possible"() {
        given:
        def ack1 = syncUpdate(getTransient("LIR-ORG") + "override: denis,override1")
        ack1.toString()

        expect:
        queryObject("-GBr -T organisation ORG-RIEN1-TEST", "organisation", "ORG-RIEN1-TEST")

        when:
        def ack = syncUpdateWithResponse("""
            organisation:   ORG-RIEN1-TEST
            org-name:       RIPE NCC                             # modified
            org-type:       OTHER                                # modified
            org:            ORG-HR1-TEST                         # modified
            abuse-mailbox:  abuse-modified@ripe.net              # modified
            descr:          RIPE NCC Operations
            address:        New Address, New City, New Country   # modified
            phone:          +31205354444-1                       # modified
            fax-no:         +31205354445-1                       # modified
            e-mail:         different-email@ripe.net             # modified
            admin-c:        TP1-TEST
            abuse-c:        AH1-TEST
            mnt-ref:        LIR-MNT
            mnt-by:         RIPE-NCC-HM-MNT
            mnt-by:         LIR-MNT
            source:         TEST
            override:       denis,override1
        """.stripIndent()
        )

        then:

        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Modify" && it.key == "[organisation] ORG-RIEN1-TEST" }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  MODIFY organisation attributes WITH RS PASSWORD
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    def "modify portal attributes with rs password should be possible"() {
        given:
        def ack1 = syncUpdate(getTransient("LIR-ORG") + "override: denis,override1")
        ack1.toString()

        expect:
        queryObject("-GBr -T organisation ORG-RIEN1-TEST", "organisation", "ORG-RIEN1-TEST")

        when:
        def ack = syncUpdateWithResponse("""
            organisation:   ORG-RIEN1-TEST
            org-name:       RIPE NCC                             # modified
            org-type:       LIR
            abuse-mailbox:  abuse-modified@ripe.net              # modified
            descr:          RIPE NCC Operations
            address:        New Address, New City, New Country   # modified
            phone:          +31205354444-1                       # modified
            fax-no:         +31205354445-1                       # modified
            e-mail:         different-email@ripe.net             # modified
            admin-c:        TP1-TEST
            abuse-c:        AH1-TEST
            mnt-ref:        LIR-MNT
            mnt-by:         RIPE-NCC-HM-MNT
            mnt-by:         LIR2-MNT
            source:         TEST
            password:       hm
        """.stripIndent()
        )

        then:

        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[organisation] ORG-RIEN1-TEST" }
    }

    def "add portal and lir editable attributes with rs password should be possible"() {
        given:
        def ack1 = syncUpdate(getTransient("LIR-ORG") + "override: denis,override1")
        ack1.toString()

        expect:
        queryObject("-GBr -T organisation ORG-RIEN1-TEST", "organisation", "ORG-RIEN1-TEST")

        when:
        def ack = syncUpdateWithResponse("""
            organisation:   ORG-RIEN1-TEST
            org-name:       Reseaux IP Europeens Network Coordination Centre (RIPE NCC)
            org-type:       LIR
            descr:          RIPE NCC Operations      # added
            remarks:        Test organisation        # added
            address:        P.O. Box 10096
            address:        Amsterdam, Netherlands   # added
            phone:          +31000000000
            phone:          +31000000001             # added
            fax-no:         +31000000000
            fax-no:         +31000000002             # added
            e-mail:         ncc@ripe.net
            e-mail:         second@ripe.net          # added
            abuse-c:        AH1-TEST
            mnt-ref:        LIR-MNT
            abuse-mailbox:  abuse@ripe.net           # added
            mnt-by:         RIPE-NCC-HM-MNT
            mnt-by:         LIR-MNT
            source:         TEST
            password:       hm
        """.stripIndent()
        )

        then:
        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[organisation] ORG-RIEN1-TEST" }
    }

    def "add second lir mnt-by with rs password should not be possible"() {
        given:
        def ack1 = syncUpdate(getTransient("LIR-ORG") + "override: denis,override1")
        ack1.toString()

        expect:
        queryObject("-GBr -T organisation ORG-RIEN1-TEST", "organisation", "ORG-RIEN1-TEST")

        when:
        def ack = syncUpdateWithResponse("""
            organisation:   ORG-RIEN1-TEST
            org-name:       Reseaux IP Europeens Network Coordination Centre (RIPE NCC)
            org-type:       LIR
            address:        P.O. Box 10096
            phone:          +31000000000
            fax-no:         +31000000000
            e-mail:         ncc@ripe.net
            abuse-c:        AH1-TEST
            mnt-ref:        LIR-MNT
            mnt-by:         RIPE-NCC-HM-MNT
            mnt-by:         LIR-MNT
            mnt-by:         LIR2-MNT                 # added
            source:         TEST
            password:       hm
        """.stripIndent()
        )

        then:
        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)
        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[organisation] ORG-RIEN1-TEST" }
        ack.errorMessagesFor("Modify", "[organisation] ORG-RIEN1-TEST") == [
                "Multiple user-'mnt-by:' are not allowed, found are: 'LIR-MNT, LIR2-MNT'"
        ]
    }

    def "delete all lir mnt-by with rs password should not be possible"() {
        given:
        def ack1 = syncUpdate(getTransient("LIR-ORG") + "override: denis,override1")
        ack1.toString()

        expect:
        queryObject("-GBr -T organisation ORG-RIEN1-TEST", "organisation", "ORG-RIEN1-TEST")

        when:
        // mnt-by:         LIR-MNT # deleted
        def ack = syncUpdateWithResponse("""
            organisation:   ORG-RIEN1-TEST
            org-name:       Reseaux IP Europeens Network Coordination Centre (RIPE NCC)
            org-type:       LIR
            address:        P.O. Box 10096
            phone:          +31000000000
            fax-no:         +31000000000
            e-mail:         ncc@ripe.net
            abuse-c:        AH1-TEST
            mnt-ref:        LIR-MNT
            mnt-by:         RIPE-NCC-HM-MNT
            source:         TEST
            password:       hm
        """.stripIndent()
        )

        then:
        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[organisation] ORG-RIEN1-TEST" }
    }

    def "delete portal and lir editable attributes with rs password should be possible"() {
        given:
        def ack1 = syncUpdate(getTransient("LIR-ORG") + "override: denis,override1")
        ack1.toString()

        expect:
        queryObject("-GBr -T organisation ORG-RIEN1-TEST", "organisation", "ORG-RIEN1-TEST")

        when:
        //    org-name:       Reseaux IP Europeens Network Coordination Centre (RIPE NCC) # cannot deleted
        //    phone:          +31205354444 # deleted
        //    fax-no:         +31205354445 # deleted
        //    e-mail:         ncc@ripe.net # cannot deleted
        //    abuse-c:        AH1-TEST     # cannot deleted
        def ack = syncUpdateWithResponse("""
            organisation:   ORG-RIEN1-TEST
            org-type:       LIR
            mnt-ref:        LIR-MNT
            mnt-by:         RIPE-NCC-HM-MNT
            source:         TEST
            password:       hm
        """.stripIndent()
        )

        then:
        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)
        ack.countErrorWarnInfo(3, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[organisation] ORG-RIEN1-TEST" }
        ack.errorMessagesFor("Modify", "[organisation] ORG-RIEN1-TEST") == [
                "Mandatory attribute \"org-name\" is missing",
                "Mandatory attribute \"address\" is missing",
                "Mandatory attribute \"e-mail\" is missing"
        ]
    }

}

