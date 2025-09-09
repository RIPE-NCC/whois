package net.ripe.db.whois.spec.update.lireditable

import org.junit.jupiter.api.Tag


@Tag("IntegrationTest")
class LirEditableAllocation6AttributeValidationSpec extends BaseLirEditableAttributeValidation {

    // data for tests
    def resourceType = "inet6num"
    def resourceValue = "2001::/20"
    def resourceStatus = "ALLOCATED-BY-RIR"
    def resourceRipeMntner = "RIPE-NCC-HM-MNT"
    def resourceRipeMntnerPassword = "hm"
    // other resource specifics
    def differentStatus = "ALLOCATED-BY-LIR"
    def differentRipeMntner = "RIPE-NCC-LEGACY-MNT"

    @Override
    Map<String, String> getTransients() {
        ["RSC-MANDATORY"            :
                 createMandatory(resourceType,
                         resourceValue,
                         resourceStatus,
                         resourceRipeMntner),
         "RSC-EXTRA"                :
                 createExtra(resourceType,
                         resourceValue,
                         resourceStatus,
                         resourceRipeMntner),
         "RSC-RIPE-NCC-MNTNER"      :
                 createRipeNccMntner(resourceType,
                         resourceValue,
                         resourceStatus,
                         resourceRipeMntner),
         "RSC-EXTRA-RIPE-NCC-MNTNER":
                 createExtraRipeNccMntner(resourceType,
                         resourceValue,
                         resourceStatus,
                         resourceRipeMntner),
         "IRT"                      :
                 createIrtTest(),
         "IRT2"                     :
                 createIrt2Test(),
         "DOMAINS2-MNT"             :
                 createDomains2Mnt()
        ]
    }

    //  MODIFY resource attributes by LIR

    def "modify resource, add (all excl. mnt-lower) lir-unlocked attributes by lir"() {
        given:
        dbfixture(getTransient("RSC-MANDATORY"))
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
                """.stripIndent(true)
        )

        then:
        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[${resourceType}] ${resourceValue}" }
    }

    def "modify resource, change (all excl. mnt-lower) lir-unlocked attributes by lir"() {
        given:
        syncUpdate(getTransient("IRT") + "override: denis, override1")
        syncUpdate(getTransient("IRT2") + "override: denis, override1")
        syncUpdate(getTransient("DOMAINS2-MNT") + "override: denis, override1")
        dbfixture(getTransient("RSC-EXTRA"))

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
                """.stripIndent(true)
        )

        then:
        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[${resourceType}] ${resourceValue}" }
    }

    def "modify resource, cannot change ripe-ncc mntner (mnt-routes) by lir"() {
        given:
        dbfixture(getTransient("RSC-RIPE-NCC-MNTNER"))
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
                """.stripIndent(true)
        )

        then:
        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)
        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[${resourceType}] ${resourceValue}" }
        ack.errorMessagesFor("Modify", "[${resourceType}] ${resourceValue}") == [
                "You cannot add or remove a RIPE NCC maintainer"
        ]
    }

    def "modify resource, add (mnt-lower) lir-unlocked attributes by lir"() {
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
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ${resourceStatus}
                mnt-by:       ${resourceRipeMntner}
                mnt-by:       LIR-MNT
                mnt-lower:    LIR2-MNT          # added
                source:       TEST
                password: lir
                password: irt
                """.stripIndent(true)
        )

        then:
        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[${resourceType}] ${resourceValue}" }
    }

    def "modify resource, cannot add ripe-ncc mntner (mnt-routes) by lir"() {
        given:
        dbfixture(getTransient("RSC-RIPE-NCC-MNTNER"))
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
                """.stripIndent(true)
        )

        then:
        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)
        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[${resourceType}] ${resourceValue}" }
        ack.errorMessagesFor("Modify", "[${resourceType}] ${resourceValue}") == [
                "You cannot add or remove a RIPE NCC maintainer"
        ]
    }

    def "modify resource, cannot delete ripe-ncc mntner (mnt-routes) by lir"() {
        given:
        dbfixture(getTransient("RSC-EXTRA-RIPE-NCC-MNTNER"))
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
                """.stripIndent(true)
        )

        then:
        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)
        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[${resourceType}] ${resourceValue}" }
        ack.errorMessagesFor("Modify", "[${resourceType}] ${resourceValue}") == [
                "You cannot add or remove a RIPE NCC maintainer"
        ]
    }

    def "modify resource, cannot change ripe-ncc mntner (mnt-domains) by lir"() {
        given:
        dbfixture(getTransient("RSC-RIPE-NCC-MNTNER"))
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
                """.stripIndent(true)
        )

        then:
        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)
        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[${resourceType}] ${resourceValue}" }
        ack.errorMessagesFor("Modify", "[${resourceType}] ${resourceValue}") == [
                "You cannot add or remove a RIPE NCC maintainer"
        ]
    }

    def "modify resource, cannot add ripe-ncc mntner (mnt-domains) by lir"() {
        given:
        dbfixture(getTransient("RSC-RIPE-NCC-MNTNER"))
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
                """.stripIndent(true)
        )

        then:
        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)
        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[${resourceType}] ${resourceValue}" }
        ack.errorMessagesFor("Modify", "[${resourceType}] ${resourceValue}") == [
                "You cannot add or remove a RIPE NCC maintainer"
        ]
    }

    def "modify resource, cannot delete ripe-ncc mntner (mnt-domains) by lir"() {
        given:
        dbfixture(getTransient("RSC-EXTRA-RIPE-NCC-MNTNER"))
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
                """.stripIndent(true)
        )

        then:
        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)
        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[${resourceType}] ${resourceValue}" }
        ack.errorMessagesFor("Modify", "[${resourceType}] ${resourceValue}") == [
                "You cannot add or remove a RIPE NCC maintainer"
        ]
    }

    //  MODIFY resource attributes WITH RS PASSWORD

    def "modify resource, change lir-locked attributes with rs password"() {
        given:
        dbfixture(getTransient("RSC-MANDATORY"))

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
                """.stripIndent(true)
        )

        then:
        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[${resourceType}] ${resourceValue}" }
    }

    def "modify resource, change (mnt-lower) lir-unlocked attributes by lir"() {
        given:
        syncUpdate(getTransient("IRT") + "override: denis, override1")
        syncUpdate(getTransient("RSC-EXTRA") + "override: denis, override1")

        expect:
        queryObject("-GBr -T ${resourceType} ${resourceValue}", resourceType, resourceValue)
        queryObject("-r -T irt IRT-TEST", "irt", "IRT-TEST")

        when:
        def ack = syncUpdateWithResponse("""
                ${resourceType}: ${resourceValue}
                netname:      TEST-NET-NAME
                descr:        some description
                country:      NL
                geoloc:       0.0 0.0
                language:     NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ${resourceStatus}
                mnt-by:       ${resourceRipeMntner}
                mnt-by:       LIR-MNT
                remarks:      a new remark
                notify:       notify@ripe.net
                mnt-lower:    LIR2-MNT          # changed
                mnt-routes:   OWNER-MNT
                mnt-domains:  DOMAINS-MNT
                mnt-irt:      IRT-TEST
                source:       TEST
                password: lir
                password: irt
                """.stripIndent(true)
        )

        then:
        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[${resourceType}] ${resourceValue}" }
    }

    def "modify resource, cannot change net-name and mnt-by (lir-locked) attributes by lir"() {
        given:
        syncUpdate(getTransient("RSC-MANDATORY") + "override: denis, override1")

        expect:
        queryObject("-GBr -T ${resourceType} ${resourceValue}", resourceType, resourceValue)

        when:
        def ack = syncUpdateWithResponse("""
                ${resourceType}: ${resourceValue}
                netname:      TEST-NET-NAME-CHANGED
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ${resourceStatus}
                mnt-by:       ${resourceRipeMntner}
                mnt-by:       LIR2-MNT
                source:       TEST
                password: lir
                password: owner3
                """.stripIndent(true)
        )

        then:
        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)
        ack.countErrorWarnInfo(2, 1, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[${resourceType}] ${resourceValue}" }
        ack.errorMessagesFor("Modify", "[${resourceType}] ${resourceValue}") == [
                "Attribute \"mnt-by:\" can only be changed via the LIR portal. Please login to https://lirportal.ripe.net and select \"LIR Account\" under \"My LIR\" to change it.",
                "The \"netname\" attribute can only be changed by the RIPE NCC"
        ]
    }

    def "modify resource, cannot add sponsoring-org by lir"() {
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
                status:       ${resourceStatus}
                mnt-by:       ${resourceRipeMntner}
                mnt-by:       LIR-MNT
                sponsoring-org: ORG-LIR1-TEST # added
                source:       TEST
                password: lir
                password: owner3
                """.stripIndent(true)
        )

        then:
        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)
        ack.countErrorWarnInfo(2, 1, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[${resourceType}] ${resourceValue}" }
        ack.errorMessagesFor("Modify", "[${resourceType}] ${resourceValue}") == [
                "The \"sponsoring-org:\" attribute is not allowed with status value \"${resourceStatus}\"",
                "The \"sponsoring-org\" attribute can only be added by the RIPE NCC"
        ]
    }

    def "modify resource, cannot change ripe-ncc mntner (mnt-lower) by lir"() {
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
                mnt-lower:    LIR2-MNT          # changed
                mnt-routes:   ${resourceRipeMntner}
                mnt-domains:  ${resourceRipeMntner}
                source:       TEST
                password: lir
                password: irt
                """.stripIndent(true)
        )

        then:
        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)
        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[${resourceType}] ${resourceValue}" }
        ack.errorMessagesFor("Modify", "[${resourceType}] ${resourceValue}") == [
                "You cannot add or remove a RIPE NCC maintainer"
        ]
    }

    def "modify resource, cannot add ripe-ncc mntner (mnt-lower) by lir"() {
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
                mnt-lower:    ${differentRipeMntner} # added
                mnt-routes:   ${resourceRipeMntner}
                mnt-domains:  ${resourceRipeMntner}
                source:       TEST
                password: lir
                password: irt
                """.stripIndent(true)
        )

        then:
        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)
        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[${resourceType}] ${resourceValue}" }
        ack.errorMessagesFor("Modify", "[${resourceType}] ${resourceValue}") == [
                "You cannot add or remove a RIPE NCC maintainer"
        ]
    }

    def "modify resource, cannot delete ripe-ncc mntner (mnt-lower) by lir"() {
        given:
        syncUpdate(getTransient("RSC-EXTRA-RIPE-NCC-MNTNER") + "override: denis, override1")
        syncUpdate(getTransient("IRT") + "override: denis, override1")

        expect:
        queryObject("-GBr -T ${resourceType} ${resourceValue}", resourceType, resourceValue)
        queryObject("-r -T irt IRT-TEST", "irt", "IRT-TEST")

        when:
        //      mnt-lower:   ${resourceRipeMntner}  # cannot deleted
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
                mnt-lower:    LIR-MNT          # extra
                mnt-routes:   ${resourceRipeMntner}  # ripe-ncc-mnt
                mnt-routes:   OWNER-MNT        # extra
                mnt-domains:  ${resourceRipeMntner}  # ripe-ncc-mnt
                mnt-domains:  DOMAINS-MNT      # extra
                source:       TEST
                password: lir
                password: irt
                """.stripIndent(true)
        )

        then:
        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)
        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[${resourceType}] ${resourceValue}" }
        ack.errorMessagesFor("Modify", "[${resourceType}] ${resourceValue}") == [
                "You cannot add or remove a RIPE NCC maintainer"
        ]
    }


    def "modify resource, delete (all) lir-unlocked attributes by lir"() {
        given:
        syncUpdate(getTransient("IRT") + "override: denis, override1")
        syncUpdate(getTransient("RSC-EXTRA") + "override: denis, override1")

        expect:
        queryObject("-GBr -T ${resourceType} ${resourceValue}", resourceType, resourceValue)
        queryObject("-r -T irt IRT-TEST", "irt", "IRT-TEST")

        when:
        //        descr:        other description # deleted
        //        geoloc:       9.0 9.0           # deleted
        //        language:     DE                # deleted
        //        admin-c:      TP2-TEST          # deleted
        //        tech-c:       TP2-TEST          # deleted
        //        remarks:      a different remark# deleted
        //        notify:       other@ripe.net    # deleted
        //        mnt-lower:    LIR2-MNT          # deleted
        //        mnt-routes:   OWNER2-MNT        # deleted
        //        mnt-domains:  DOMAINS-MNT       # deleted
        //        mnt-irt:      IRT-2-TEST        # deleted
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
                source:       TEST
                password: lir
                """.stripIndent(true)
        )

        then:
        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[${resourceType}] ${resourceValue}" }
    }

    def "modify resource, cannot delete (org) lir-unlocked attributes by lir"() {
        given:
        syncUpdate(getTransient("RSC-MANDATORY") + "override: denis, override1")

        expect:
        queryObject("-GBr -T ${resourceType} ${resourceValue}", resourceType, resourceValue)

        when:
        //        org:          ORG-LIR1-TEST # cannot delete
        def ack = syncUpdateWithResponse("""
                ${resourceType}: ${resourceValue}
                netname:      TEST-NET-NAME
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ${resourceStatus}
                mnt-by:       ${resourceRipeMntner}
                mnt-by:       LIR-MNT
                source:       TEST
                password: lir
                """.stripIndent(true)
        )

        then:
        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)
        ack.countErrorWarnInfo(2, 1, 0)
        ack.errorMessagesFor("Modify", "[${resourceType}] ${resourceValue}") == [
                "Referenced organisation can only be removed by the RIPE NCC for this resource. Please contact \"ncc@ripe.net\" to remove this reference.",
                "Missing required \"org:\" attribute"
        ]
    }

    //  MODIFY resource attributes WITH RS PASSWORD

    def "modify resource, add sponsoring attributes with rs password"() {
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
                sponsoring-org: ORG-LIR1-TEST      # added
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ${resourceStatus}
                mnt-by:       ${resourceRipeMntner}
                mnt-by:       LIR-MNT
                source:       TEST
                password: ${resourceRipeMntnerPassword}
                """.stripIndent(true)
        )

        then:
        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)
        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[${resourceType}] ${resourceValue}" }
        ack.errorMessagesFor("Modify", "[${resourceType}] ${resourceValue}") == [
                "The \"sponsoring-org:\" attribute is not allowed with status value \"${resourceStatus}\""
        ]
    }

    // DELETE resource by LIR

    def "cannot delete resource by lir if also has ripe ncc mntner"() {
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
                status:       ${resourceStatus}
                mnt-by:       ${resourceRipeMntner}
                mnt-by:       LIR-MNT
                source:       TEST
                delete: some reason
                password: lir
                """.stripIndent(true)
        )

        then:
        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 0, 1)
        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Delete" && it.key == "[${resourceType}] ${resourceValue}" }
        ack.errorMessagesFor("Delete", "[${resourceType}] ${resourceValue}") == [
                "Deleting this object requires administrative authorisation"
        ]
    }

    //  MODIFY resource attributes WITH OVERRIDE

    def "modify resource, change lir-locked attributes with override"() {
        given:
        dbfixture(getTransient("RSC-MANDATORY"))

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
                """.stripIndent(true)
        )

        then:
        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 4, 1)
        ack.successes.any { it.operation == "Modify" && it.key == "[${resourceType}] ${resourceValue}" }
    }
}
