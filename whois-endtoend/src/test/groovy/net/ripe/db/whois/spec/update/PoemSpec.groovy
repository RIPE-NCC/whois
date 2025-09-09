package net.ripe.db.whois.spec.update


import net.ripe.db.whois.spec.BaseQueryUpdateSpec
import net.ripe.db.whois.spec.domain.Message

@org.junit.jupiter.api.Tag("IntegrationTest")
class PoemSpec extends BaseQueryUpdateSpec{

    @Override
    Map<String, String> getTransients() {
        [
            "FORM-HAIKU": """\
                poetic-form:    FORM-HAIKU
                descr:          The haiku object
                descr:          only seven syllables
                descr:          in its density
                admin-c:        TP2-TEST
                mnt-by:         RIPE-DBM-MNT
                source:         TEST
                """,
            "FORM-LIMERICK": """\
                poetic-form:    FORM-LIMERICK
                descr:          The haiku object
                descr:          only seven syllables
                descr:          in its density
                admin-c:        TP1-TEST
                mnt-by:         RIPE-DBM-MNT
                source:         TEST
                """,
            "POEM-EXISTING":"""\
                poem:           POEM-EXISTING
                text:           Our SQL
                text:           In the photo, you can see
                text:           Engineers coding the DB,
                text:           You will be amazed & excited,
                text:           Impressed & delighted,
                text:           When they release
                text:           Version 3!
                form:           FORM-HAIKU
                remarks:        This object is translated from HAIKU type to POEM type.
                mnt-by:         LIM-MNT
                source:         TEST
            """
    ]}

    def "create poem, with form, and mny-by LIM-MNT with author"() {
        given:
            syncUpdate(dbfixture(getTransient("FORM-HAIKU")))
            queryObject("-r -T poetic-form FORM-HAIKU", "poetic-form", "FORM-HAIKU")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                poem:           POEM-OUR-SQL
                text:           Our SQL
                text:           In the photo, you can see
                text:           Engineers coding the DB,
                text:           You will be amazed & excited,
                text:           Impressed & delighted,
                text:           When they release
                text:           Version 3!
                form:           FORM-HAIKU
                author:         TP1-TEST
                remarks:        This object is translated from HAIKU type to POEM type.
                mnt-by:         LIM-MNT
                source:         TEST

                password:   lim
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[poem] POEM-OUR-SQL" }

        query_object_matches("-rGBT poem POEM-OUR-SQL", "poem", "POEM-OUR-SQL", "mnt-by:         LIM-MNT")
    }

    def "create poem, with form, and mny-by other than LIM-MNT"() {
        given:
            syncUpdate(dbfixture(getTransient("FORM-HAIKU")))
            queryObject("-r -T poetic-form FORM-HAIKU", "poetic-form", "FORM-HAIKU")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                poem:           POEM-OUR-SQL
                text:           Our SQL
                text:           In the photo, you can see
                text:           Engineers coding the DB,
                text:           You will be amazed & excited,
                text:           Impressed & delighted,
                text:           When they release
                text:           Version 3!
                form:           FORM-HAIKU
                remarks:        This object is translated from HAIKU type to POEM type.
                mnt-by:         TST-MNT2
                source:         TEST

                password:   test2
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 2, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[poem] POEM-OUR-SQL" }
        queryObjectNotFound("-rGBT poem POEM-OUR-SQL", "poem", "POEM-OUR-SQL")
        ack.errorMessagesFor("Create", "[poem] POEM-OUR-SQL") ==
                ["Poem must be maintained by 'LIM-MNT', which has a public password"]
    }

    def "create poem, with form, and mny-by LIM-MNT, without author"() {
        given:
        syncUpdate(dbfixture(getTransient("FORM-HAIKU")))
        queryObject("-r -T poetic-form FORM-HAIKU", "poetic-form", "FORM-HAIKU")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                poem:           POEM-OUR-SQL
                text:           Our SQL
                text:           In the photo, you can see
                text:           Engineers coding the DB,
                text:           You will be amazed & excited,
                text:           Impressed & delighted,
                text:           When they release
                text:           Version 3!
                form:           FORM-HAIKU
                remarks:        This object is translated from HAIKU type to POEM type.
                mnt-by:         LIM-MNT
                source:         TEST

                password:   lim
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[poem] POEM-OUR-SQL" }

        query_object_matches("-rGBT poem POEM-OUR-SQL", "poem", "POEM-OUR-SQL", "mnt-by:         LIM-MNT")
    }

    def "modify poem, adding description"() {
        given:
            syncUpdate(dbfixture(getTransient("FORM-HAIKU")))
            queryObject("-r -T poetic-form FORM-HAIKU", "poetic-form", "FORM-HAIKU")

            syncUpdate(dbfixture(getTransient("POEM-EXISTING")))
            queryObject("-r -T poem POEM-EXISTING", "poem", "POEM-EXISTING")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                poem:           POEM-EXISTING
                descr:          Adding Description
                text:           Our SQL
                text:           In the photo, you can see
                text:           Engineers coding the DB,
                text:           You will be amazed & excited,
                text:           Impressed & delighted,
                text:           When they release
                text:           Version 3!
                form:           FORM-HAIKU
                remarks:        This object is translated from HAIKU type to POEM type.
                mnt-by:         LIM-MNT
                source:         TEST

                password:   lim
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[poem] POEM-EXISTING" }

        query_object_matches("-rGBT poem POEM-EXISTING", "poem", "POEM-EXISTING", "descr:          Adding Description")
    }

    def "modify poem, with new form, and mny-by LIM-MNT"() {
        given:
            syncUpdate(dbfixture(getTransient("FORM-HAIKU")))
            queryObject("-r -T poetic-form FORM-HAIKU", "poetic-form", "FORM-HAIKU")

            syncUpdate(dbfixture(getTransient("FORM-LIMERICK")))
            queryObject("-r -T poetic-form FORM-LIMERICK", "poetic-form", "FORM-LIMERICK")

            syncUpdate(dbfixture(getTransient("POEM-EXISTING")))
            queryObject("-r -T poem POEM-EXISTING", "poem", "POEM-EXISTING")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                poem:           POEM-EXISTING
                descr:          Adding Description
                text:           Our SQL
                text:           In the photo, you can see
                text:           Engineers coding the DB,
                text:           You will be amazed & excited,
                text:           Impressed & delighted,
                text:           When they release
                text:           Version 3!
                form:           FORM-LIMERICK
                remarks:        This object is translated from HAIKU type to POEM type.
                mnt-by:         LIM-MNT
                source:         TEST

                password:   lim
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[poem] POEM-EXISTING" }

        query_object_matches("-rGBT poem POEM-EXISTING", "poem", "POEM-EXISTING", "descr:          Adding Description")
    }

    def "delete poem"() {
        given:
            syncUpdate(dbfixture(getTransient("FORM-HAIKU")))
            queryObject("-r -T poetic-form FORM-HAIKU", "poetic-form", "FORM-HAIKU")

            syncUpdate(dbfixture(getTransient("POEM-EXISTING")))
            queryObject("-r -T poem POEM-EXISTING", "poem", "POEM-EXISTING")


        when:
        def message = send new Message(
                subject: "delete POEM-EXISTING",
                body: """\
                poem:           POEM-EXISTING
                text:           Our SQL
                text:           In the photo, you can see
                text:           Engineers coding the DB,
                text:           You will be amazed & excited,
                text:           Impressed & delighted,
                text:           When they release
                text:           Version 3!
                form:           FORM-HAIKU
                remarks:        This object is translated from HAIKU type to POEM type.
                mnt-by:         LIM-MNT
                source:         TEST
                delete:         test delete

                password:   lim
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 4, 0)
        ack.successes.any { it.operation == "Delete" && it.key == "[poem] POEM-EXISTING" }

        queryObjectNotFound("-rGBT poem POEM-EXISTING", "poem", "POEM-EXISTING")
    }
}
