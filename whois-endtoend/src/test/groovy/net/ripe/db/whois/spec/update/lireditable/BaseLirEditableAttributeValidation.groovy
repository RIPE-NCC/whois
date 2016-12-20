package net.ripe.db.whois.spec.update.lireditable

import spock.lang.Ignore;

@Ignore("base class don't run tests here directly")
class BaseLirEditableAttributeValidation extends BaseLirEditableAttributes {

    //  MODIFY resource attributes by LIR

    def "modify resource, add (all excl. mnt-lower) lir-unlocked attributes by lir"() {
        given:
        syncUpdate(getTransient("RSC-MANDATORY") + "override: denis, override1")
        syncUpdate(getTransient("IRT") + "override: denis, override1")

        expect:
        queryObject("-GBr -T ${resourceType} ${resourceValue}", resourceType, resourceValue)
        queryObject("-r -T irt IRT-TEST", "irt", "IRT-TEST")

        when:
        def ack = syncUpdateWithResponse("""
                ${resourceType}: ${resourceValue}
                netname:      TEST-NET-NAME
                descr:        some description  # added
                country:      NL
                country:      DE                # added
                geoloc:       0.0 0.0           # added
                language:     NL                # added
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                admin-c:      TP2-TEST          # added
                tech-c:       TP1-TEST
                tech-c:       TP2-TEST          # added
                remarks:      a new remark      # added
                notify:       notify@ripe.net   # added
                status:       ${resourceStatus}
                mnt-by:       ${resourceRipeMntner}
                mnt-by:       LIR-MNT
                mnt-routes:   OWNER-MNT         # added
                mnt-domains:  DOMAINS-MNT       # added
                mnt-irt:      IRT-TEST          # added
                source:       TEST
                password: lir
                password: irt
                """.stripIndent()
        )

        then:
        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[${resourceType}] ${resourceValue}" }
    }

    def "modify resource, change (all excl. mnt-lower) lir-unlocked attributes by lir"() {
        given:
        syncUpdate(getTransient("IRT") + "override: denis, override1")
        syncUpdate(getTransient("IRT2") + "override: denis, override1")
        syncUpdate(getTransient("DOMAINS2-MNT") + "override: denis, override1")
        syncUpdate(getTransient("RSC-EXTRA") + "override: denis, override1")

        expect:
        queryObject("-GBr -T ${resourceType} ${resourceValue}", resourceType, resourceValue)
        queryObject("-r -T mntner DOMAINS2-MNT", "mntner", "DOMAINS2-MNT")
        queryObject("-r -T irt IRT-TEST", "irt", "IRT-TEST")
        queryObject("-r -T irt IRT-2-TEST", "irt", "IRT-2-TEST")

        when:
        def ack = syncUpdateWithResponse("""
                ${resourceType}: ${resourceValue}
                netname:      TEST-NET-NAME
                descr:        other description # changed
                country:      DE                # changed
                geoloc:       9.0 9.0           # changed
                language:     DE                # changed
                org:          ORG-LIR1-TEST
                admin-c:      TP2-TEST          # changed
                tech-c:       TP2-TEST          # changed
                status:       ${resourceStatus}
                mnt-by:       ${resourceRipeMntner}
                mnt-by:       LIR-MNT
                remarks:      a different remark# changed
                notify:       other@ripe.net    # changed
                mnt-lower:    LIR-MNT
                mnt-routes:   OWNER2-MNT        # changed
                mnt-domains:  DOMAINS-MNT       # changed
                mnt-irt:      IRT-2-TEST        # changed
                source:       TEST
                password: lir
                password: irt
                """.stripIndent()
        )

        then:
        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[${resourceType}] ${resourceValue}" }
    }

    def "modify resource, cannot change org and status (lir-locked) attributes by lir"() {
        given:
        syncUpdate(getTransient("RSC-MANDATORY") + "override: denis, override1")

        expect:
        queryObject("-GBr -T ${resourceType} ${resourceValue}", resourceType, resourceValue)

        when:
        def ack = syncUpdateWithResponse("""
                ${resourceType}: ${resourceValue}
                netname:      TEST-NET-NAME
                country:      NL
                org:          ORG-LIR2-TEST         # changed
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ${differentStatus}    # changed
                mnt-by:       ${resourceRipeMntner}
                mnt-by:       LIR-MNT
                source:       TEST
                password: lir
                password: owner3
                """.stripIndent()
        )

        then:
        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)
        ack.countErrorWarnInfo(2, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[${resourceType}] ${resourceValue}" }
        ack.errorMessagesFor("Modify", "[${resourceType}] ${resourceValue}") == [
                "Referenced organisation can only be changed by the RIPE NCC for this resource. Please contact \"ncc@ripe.net\" to change this reference.",
                "status value cannot be changed, you must delete and re-create the object"
        ]
    }

    def "modify resource, cannot delete (some) mandatory lir-unlocked attributes by lir"() {
        given:
        syncUpdate(getTransient("RSC-MANDATORY") + "override: denis, override1")

        expect:
        queryObject("-GBr -T ${resourceType} ${resourceValue}", resourceType, resourceValue)

        when:
        //        org:          ORG-LIR1-TEST # cannot delete, but warning is NOT presented!!
        //        country:      NL            # cannot delete
        //        admin-c:      TP1-TEST      # cannot delete
        //        tech-c:       TP1-TEST      # cannot delete
        def ack = syncUpdateWithResponse("""
                ${resourceType}: ${resourceValue}
                netname:      TEST-NET-NAME
                status:       ${resourceStatus}
                mnt-by:       ${resourceRipeMntner}
                mnt-by:       LIR-MNT
                source:       TEST
                password: lir
                """.stripIndent()
        )

        then:
        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)
        ack.countErrorWarnInfo(3, 0, 0)
        ack.errorMessagesFor("Modify", "[${resourceType}] ${resourceValue}") == [
                "Mandatory attribute \"country\" is missing",
                "Mandatory attribute \"admin-c\" is missing",
                "Mandatory attribute \"tech-c\" is missing"]
    }

    def "modify resource, cannot change ripe-ncc mntner (mnt-routes) by lir"() {
        given:
        syncUpdate(getTransient("RSC-RIPE-NCC-MNTNER") + "override: denis, override1")
        syncUpdate(getTransient("IRT") + "override: denis, override1")

        expect:
        queryObject("-GBr -T ${resourceType} ${resourceValue}", resourceType, resourceValue)
        queryObject("-r -T irt IRT-TEST", "irt", "IRT-TEST")

        when:
        def ack = syncUpdateWithResponse("""
                ${resourceType}: ${resourceValue}
                netname:      TEST-NET-NAME
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ${resourceStatus}
                mnt-by:       ${resourceRipeMntner}
                mnt-by:       LIR-MNT
                mnt-lower:    ${resourceRipeMntner}
                mnt-routes:   LIR2-MNT          # changed
                mnt-domains:  ${resourceRipeMntner}
                source:       TEST
                password: lir
                password: irt
                """.stripIndent()
        )

        then:
        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)
        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[${resourceType}] ${resourceValue}" }
        ack.errorMessagesFor("Modify", "[${resourceType}] ${resourceValue}") == [
                "You cannot add or remove a RIPE NCC maintainer"
        ]
    }

    def "modify resource, cannot add ripe-ncc mntner (mnt-routes) by lir"() {
        given:
        syncUpdate(getTransient("RSC-RIPE-NCC-MNTNER") + "override: denis, override1")
        syncUpdate(getTransient("IRT") + "override: denis, override1")

        expect:
        queryObject("-GBr -T ${resourceType} ${resourceValue}", resourceType, resourceValue)
        queryObject("-r -T irt IRT-TEST", "irt", "IRT-TEST")

        when:
        def ack = syncUpdateWithResponse("""
                ${resourceType}: ${resourceValue}
                netname:      TEST-NET-NAME
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ${resourceStatus}
                mnt-by:       ${resourceRipeMntner}
                mnt-by:       LIR-MNT
                mnt-lower:    ${resourceRipeMntner}
                mnt-routes:   ${resourceRipeMntner}
                mnt-routes:   ${differentRipeMntner} # added
                mnt-domains:  ${resourceRipeMntner}
                source:       TEST
                password: lir
                password: irt
                """.stripIndent()
        )

        then:
        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)
        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[${resourceType}] ${resourceValue}" }
        ack.errorMessagesFor("Modify", "[${resourceType}] ${resourceValue}") == [
                "You cannot add or remove a RIPE NCC maintainer"
        ]
    }

    def "modify resource, cannot delete ripe-ncc mntner (mnt-routes) by lir"() {
        given:
        syncUpdate(getTransient("RSC-EXTRA-RIPE-NCC-MNTNER") + "override: denis, override1")
        syncUpdate(getTransient("IRT") + "override: denis, override1")

        expect:
        queryObject("-GBr -T ${resourceType} ${resourceValue}", resourceType, resourceValue)
        queryObject("-r -T irt IRT-TEST", "irt", "IRT-TEST")

        when:
        //      mnt-routes:   ${resourceRipeMntner}  # cannot deleted
        def ack = syncUpdateWithResponse("""
                ${resourceType}: ${resourceValue}
                netname:      TEST-NET-NAME
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ${resourceStatus}
                mnt-by:       ${resourceRipeMntner}
                mnt-by:       LIR-MNT
                mnt-lower:    ${resourceRipeMntner}  # ripe-ncc-mnt
                mnt-lower:    LIR-MNT          # extra
                mnt-routes:   OWNER-MNT        # extra
                mnt-domains:  ${resourceRipeMntner}  # ripe-ncc-mnt
                mnt-domains:  DOMAINS-MNT      # extra
                source:       TEST
                password: lir
                password: irt
                """.stripIndent()
        )

        then:
        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)
        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[${resourceType}] ${resourceValue}" }
        ack.errorMessagesFor("Modify", "[${resourceType}] ${resourceValue}") == [
                "You cannot add or remove a RIPE NCC maintainer"
        ]
    }

    def "modify resource, cannot change ripe-ncc mntner (mnt-domains) by lir"() {
        given:
        syncUpdate(getTransient("RSC-RIPE-NCC-MNTNER") + "override: denis, override1")
        syncUpdate(getTransient("IRT") + "override: denis, override1")

        expect:
        queryObject("-GBr -T ${resourceType} ${resourceValue}", resourceType, resourceValue)
        queryObject("-r -T irt IRT-TEST", "irt", "IRT-TEST")

        when:
        def ack = syncUpdateWithResponse("""
                ${resourceType}: ${resourceValue}
                netname:      TEST-NET-NAME
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ${resourceStatus}
                mnt-by:       ${resourceRipeMntner}
                mnt-by:       LIR-MNT
                mnt-lower:    ${resourceRipeMntner}
                mnt-routes:   ${resourceRipeMntner}
                mnt-domains:  LIR2-MNT          # changed
                source:       TEST
                password: lir
                password: irt
                """.stripIndent()
        )

        then:
        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)
        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[${resourceType}] ${resourceValue}" }
        ack.errorMessagesFor("Modify", "[${resourceType}] ${resourceValue}") == [
                "You cannot add or remove a RIPE NCC maintainer"
        ]
    }

    def "modify resource, cannot add ripe-ncc mntner (mnt-domains) by lir"() {
        given:
        syncUpdate(getTransient("RSC-RIPE-NCC-MNTNER") + "override: denis, override1")
        syncUpdate(getTransient("IRT") + "override: denis, override1")

        expect:
        queryObject("-GBr -T ${resourceType} ${resourceValue}", resourceType, resourceValue)
        queryObject("-r -T irt IRT-TEST", "irt", "IRT-TEST")

        when:
        def ack = syncUpdateWithResponse("""
                ${resourceType}: ${resourceValue}
                netname:      TEST-NET-NAME
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ${resourceStatus}
                mnt-by:       ${resourceRipeMntner}
                mnt-by:       LIR-MNT
                mnt-lower:    ${resourceRipeMntner}
                mnt-routes:   ${resourceRipeMntner}
                mnt-domains:  ${resourceRipeMntner}
                mnt-domains:  ${differentRipeMntner} # added
                source:       TEST
                password: lir
                password: irt
                """.stripIndent()
        )

        then:
        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)
        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[${resourceType}] ${resourceValue}" }
        ack.errorMessagesFor("Modify", "[${resourceType}] ${resourceValue}") == [
                "You cannot add or remove a RIPE NCC maintainer"
        ]
    }

    def "modify resource, cannot delete ripe-ncc mntner (mnt-domains) by lir"() {
        given:
        syncUpdate(getTransient("RSC-EXTRA-RIPE-NCC-MNTNER") + "override: denis, override1")
        syncUpdate(getTransient("IRT") + "override: denis, override1")

        expect:
        queryObject("-GBr -T ${resourceType} ${resourceValue}", resourceType, resourceValue)
        queryObject("-r -T irt IRT-TEST", "irt", "IRT-TEST")

        when:
        //      mnt-domains:   ${resourceRipeMntner}  # cannot deleted
        def ack = syncUpdateWithResponse("""
                ${resourceType}: ${resourceValue}
                netname:      TEST-NET-NAME
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ${resourceStatus}
                mnt-by:       ${resourceRipeMntner}
                mnt-by:       LIR-MNT
                mnt-lower:    ${resourceRipeMntner}  # ripe-ncc-mnt
                mnt-lower:    LIR-MNT          # extra
                mnt-routes:   ${resourceRipeMntner}  # ripe-ncc-mnt
                mnt-routes:   OWNER-MNT        # extra
                mnt-domains:  DOMAINS-MNT      # extra
                source:       TEST
                password: lir
                password: irt
                """.stripIndent()
        )

        then:
        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)
        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[${resourceType}] ${resourceValue}" }
        ack.errorMessagesFor("Modify", "[${resourceType}] ${resourceValue}") == [
                "You cannot add or remove a RIPE NCC maintainer"
        ]
    }

    //  MODIFY resource attributes WITH RS PASSWORD

    def "modify resource, change lir-locked attributes with rs password"() {
        given:
        syncUpdate(getTransient("RSC-MANDATORY") + "override: denis, override1")

        expect:
        queryObject("-GBr -T ${resourceType} ${resourceValue}", resourceType, resourceValue)

        when:
        def ack = syncUpdateWithResponse("""
                ${resourceType}: ${resourceValue}
                netname:      TEST-NET-NAME-CHANGED # changed
                country:      NL
                org:          ORG-LIRA-TEST         # changed
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ${resourceStatus}
                mnt-by:       ${resourceRipeMntner}
                mnt-by:       LIR2-MNT              # changed
                source:       TEST
                password: ${resourceRipeMntnerPassword}
                password: owner3
                """.stripIndent()
        )

        then:
        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[${resourceType}] ${resourceValue}" }
    }

    def "modify resource, change lir-locked (status) attributes with rs password"() {
        given:
        syncUpdate(getTransient("RSC-MANDATORY") + "override: denis, override1")

        expect:
        queryObject("-GBr -T ${resourceType} ${resourceValue}", resourceType, resourceValue)

        when:
        def ack = syncUpdateWithResponse("""
                ${resourceType}: ${resourceValue}
                netname:      TEST-NET-NAME
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ${differentStatus}     # changed
                mnt-by:       ${resourceRipeMntner}
                mnt-by:       LIR-MNT
                source:       TEST
                password: ${resourceRipeMntnerPassword}
                """.stripIndent()
        )

        then:
        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)
        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[${resourceType}] ${resourceValue}" }
        ack.errorMessagesFor("Modify", "[${resourceType}] ${resourceValue}") == [
                "status value cannot be changed, you must delete and re-create the object"
        ]
    }

    def "modify resource, add 'single' attributes with rs password"() {
        given:
        syncUpdate(getTransient("RSC-MANDATORY") + "override: denis, override1")

        expect:
        queryObject("-GBr -T ${resourceType} ${resourceValue}", resourceType, resourceValue)

        when:
        def ack = syncUpdateWithResponse("""
                ${resourceType}: ${resourceValue}
                netname:      TEST-NET-NAME
                netname:      TEST-NET-NAME-2      # added
                country:      NL
                org:          ORG-LIR1-TEST
                org:          ORG-LIR2-TEST        # added
                sponsoring-org: ORG-LIR1-TEST      # added
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ${resourceStatus}
                status:       ${differentStatus}   # added
                mnt-by:       ${resourceRipeMntner}
                mnt-by:       LIR-MNT
                source:       TEST
                password: ${resourceRipeMntnerPassword}
                """.stripIndent()
        )

        then:
        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)
        ack.countErrorWarnInfo(3, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[${resourceType}] ${resourceValue}" }
        ack.errorMessagesFor("Modify", "[${resourceType}] ${resourceValue}") == [
                "Attribute \"netname\" appears more than once",
                "Attribute \"org\" appears more than once",
                "Attribute \"status\" appears more than once"
        ]
    }

    //  MODIFY resource attributes WITH OVERRIDE

    def "modify resource, change lir-locked attributes with override"() {
        given:
        syncUpdate(getTransient("RSC-MANDATORY") + "override: denis, override1")

        expect:
        queryObject("-GBr -T ${resourceType} ${resourceValue}", resourceType, resourceValue)

        when:
        def ack = syncUpdateWithResponse("""
                ${resourceType}: ${resourceValue}
                netname:      TEST-NET-NAME-CHANGED # changed
                country:      NL
                org:          ORG-LIRA-TEST         # changed
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ${differentStatus}    # changed
                mnt-by:       ${resourceRipeMntner}
                mnt-by:       LIR2-MNT              # changed
                source:       TEST
                override:     denis,override1
                """.stripIndent()
        )

        then:
        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Modify" && it.key == "[${resourceType}] ${resourceValue}" }
    }

}
