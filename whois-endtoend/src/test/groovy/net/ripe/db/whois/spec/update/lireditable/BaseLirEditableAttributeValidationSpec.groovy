package net.ripe.db.whois.spec.update.lireditable;

public class BaseLirEditableAttributeValidationSpec extends BaseLirEditableSpec {

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
                "Adding or removing a RIPE NCC maintainer requires administrative authorisation"
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
                "Adding or removing a RIPE NCC maintainer requires administrative authorisation"
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
                "Adding or removing a RIPE NCC maintainer requires administrative authorisation"
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
                "Adding or removing a RIPE NCC maintainer requires administrative authorisation"
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
                "Adding or removing a RIPE NCC maintainer requires administrative authorisation"
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
                "Adding or removing a RIPE NCC maintainer requires administrative authorisation"
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