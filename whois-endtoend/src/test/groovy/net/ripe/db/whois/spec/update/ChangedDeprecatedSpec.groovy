package net.ripe.db.whois.spec.update

import com.google.common.collect.Lists
import net.ripe.db.whois.api.rest.domain.ErrorMessage
import net.ripe.db.whois.common.rpsl.AttributeType
import net.ripe.db.whois.common.rpsl.RpslObject
import net.ripe.db.whois.spec.BaseQueryUpdateSpec
import net.ripe.db.whois.spec.domain.AckResponse
import net.ripe.db.whois.spec.domain.Message

@org.junit.jupiter.api.Tag("IntegrationTest")
class ChangedDeprecatedSpec extends BaseQueryUpdateSpec  {
    private static final String PERSON_WITHOUT_CHANGED = "PERSON_WITHOUT_CHANGED";
    private static final String PERSON_WITHOUT_CHANGED_ADJUSTED = "PERSON_WITHOUT_CHANGED_ADJUSTED";
    private static final String PERSON_WITH_CHANGED = "PERSON_WITH_CHANGED";
    private static final String PERSON_WITH_CHANGED_ADJUSTED = "PERSON_WITH_CHANGED_ADJUSTED";

    @Override
    Map<String, String> getTransients() { [
            "PERSON_WITHOUT_CHANGED": """\
            person:  First Person
            address: St James Street
            address: Burnley
            address: UK
            phone:   +44 282 420469
            nic-hdl: FP1-TEST
            mnt-by:  OWNER-MNT
            source:  TEST
            """,
            "PERSON_WITHOUT_CHANGED_ADJUSTED": """\
            person:  First Person
            address: St James Street
            address: Amsterdam
            address: NL
            phone:   +44 282 420469
            nic-hdl: FP1-TEST
            mnt-by:  OWNER-MNT
            source:  TEST
            """,
            "PERSON_WITH_CHANGED": """\
            person:  First Person
            address: St James Street
            address: Burnley
            address: UK
            phone:   +44 282 420469
            nic-hdl: FP1-TEST
            mnt-by:  OWNER-MNT
            changed: noreply@ripe.net 20140102
            source:  TEST
            """,
            "PERSON_WITH_CHANGED_ADJUSTED": """\
            person:  First Person
            address: St James Street
            address: Amsterdam
            address: NL
            phone:   +44 282 420469
            nic-hdl: FP1-TEST
            mnt-by:  OWNER-MNT
            changed: noreply@ripe.net 20140102
            source:  TEST
                """
    ]}

    def "syncupdates: create person with changed"() {
        expect:
        doesNotExist(PERSON_WITH_CHANGED)

        when:
        def response = syncUpdateCreateModify(PERSON_WITH_CHANGED)

        then:
        syncUpdateVerifyCreateSuccess(PERSON_WITH_CHANGED,response)
        syncUpdateVerifyHasDeprecatedWarning(response)
        verifyExistsAndEquals(PERSON_WITHOUT_CHANGED)
    }

    def "mail: create person with changed"() {
        expect:
        doesNotExist(PERSON_WITH_CHANGED)

        when:
        def response = mailCreate(PERSON_WITH_CHANGED)

        then:
        mailVerifyCreateSuccess(PERSON_WITH_CHANGED,response)
        mailVerifyHasDeprecatedWarning(response)
        verifyExistsAndEquals(PERSON_WITH_CHANGED)
    }

    def "rest: create person with changed"() {
        expect:
        doesNotExist(PERSON_WITH_CHANGED)

        when:
        def errorsAndWarnings = restCreate(PERSON_WITH_CHANGED)

        then:
        restVerifyHasDeprecatedWarning(errorsAndWarnings)
        verifyExistsAndEquals(PERSON_WITH_CHANGED)
    }

    def "syncupdates: create person without changed"() {
        expect:
        doesNotExist(PERSON_WITHOUT_CHANGED)

        when:
        def response = syncUpdateCreateModify(PERSON_WITHOUT_CHANGED)

        then:
        syncUpdateVerifyCreateSuccess(PERSON_WITHOUT_CHANGED,response)
        syncUpdateVerifyHasNoWarnings(response)
        verifyExistsAndEquals(PERSON_WITHOUT_CHANGED)
    }

    def "mail: create person without changed"() {
        expect:
        doesNotExist(PERSON_WITHOUT_CHANGED)

        when:
        def response = mailCreate(PERSON_WITHOUT_CHANGED)

        then:
        mailVerifyCreateSuccess(PERSON_WITHOUT_CHANGED,response)
        mailVerifyHasWarnings(response, 2)
        verifyExistsAndEquals(PERSON_WITHOUT_CHANGED)
    }

    def "rest: create person without changed"() {
        expect:
        doesNotExist(PERSON_WITHOUT_CHANGED)

        when:
        def errorsAndWarnings = restCreate(PERSON_WITHOUT_CHANGED)

        then:
        restVerifyHasDeprecatedWarning(errorsAndWarnings)
        verifyExistsAndEquals(PERSON_WITHOUT_CHANGED)
    }

    def "syncupdates: update-noop person with changed: existing has changed-attribute"() {
        given:
        persist(PERSON_WITH_CHANGED)

        expect:
        verifyExistsAndEquals(PERSON_WITH_CHANGED)

        when:
        def response = syncUpdateModify(PERSON_WITH_CHANGED)

        then:
        syncUpdateVerifyNoOperation(PERSON_WITH_CHANGED,response)
        syncUpdateVerifyHasDeprecatedWarning(response)
        syncUpdateVerifyHasIdenticalWarning(response)
        verifyExistsAndEquals(PERSON_WITH_CHANGED)
    }

    def "mail: update-noop person with changed: existing has changed-attribute"() {
        given:
        persist(PERSON_WITH_CHANGED)

        expect:
        verifyExistsAndEquals(PERSON_WITH_CHANGED)

        when:
        def response = mailModify(PERSON_WITH_CHANGED)

        then:
        mailVerifyNoOperation(PERSON_WITH_CHANGED,response)
        mailVerifyHasDeprecatedWarning(response)
        mailVerifyHasIdenticalWarning(response)
        verifyExistsAndEquals(PERSON_WITH_CHANGED)
    }

    def "rest: update-noop person with changed: existing has changed-attribute"() {
        given:
        persist(PERSON_WITH_CHANGED)

        expect:
        verifyExistsAndEquals(PERSON_WITH_CHANGED)

        when:
        def errorsAndWarnings = restModify(PERSON_WITH_CHANGED)

        then:
        restVerifyHasDeprecatedWarning(errorsAndWarnings)
        restVerifyHasIdenticalWarning(errorsAndWarnings)
        verifyExistsAndEquals(PERSON_WITH_CHANGED)
    }

    def "syncupdates: update-noop person without changed: existing has no changed-attribute"() {
        given:
        persist(PERSON_WITHOUT_CHANGED)

        expect:
        verifyExistsAndEquals(PERSON_WITHOUT_CHANGED)

        when:
        def response = syncUpdateCreateModify(PERSON_WITHOUT_CHANGED)

        then:
        syncUpdateVerifyNoOperation(PERSON_WITHOUT_CHANGED,response)
        syncUpdateVerifyHasIdenticalWarning(response)
        verifyExistsAndEquals(PERSON_WITHOUT_CHANGED)
    }

    def "mail: update-noop person without changed: existing has no changed-attribute"() {
        given:
        persist(PERSON_WITHOUT_CHANGED)

        expect:
        verifyExistsAndEquals(PERSON_WITHOUT_CHANGED)

        when:
        def response = mailModify(PERSON_WITHOUT_CHANGED)

        then:
        mailVerifyNoOperation(PERSON_WITHOUT_CHANGED,response)
        mailVerifyHasIdenticalWarning(response)
        verifyExistsAndEquals(PERSON_WITHOUT_CHANGED)
    }

    def "rest: update-noop person without changed: existing has no changed-attribute"() {
        given:
        persist(PERSON_WITHOUT_CHANGED)

        expect:
        verifyExistsAndEquals(PERSON_WITHOUT_CHANGED)

        when:
        def errorsAndWarnings = restModify(PERSON_WITHOUT_CHANGED)

        then:
        restVerifyHasIdenticalWarning(errorsAndWarnings)
        verifyExistsAndEquals(PERSON_WITHOUT_CHANGED)
    }

    def "syncupdates: update person with changed: existing has changed-attribute"() {
        given:
        persist(PERSON_WITH_CHANGED)

        expect:
        verifyExistsAndEquals(PERSON_WITH_CHANGED)

        when:
        def response = syncUpdateModify(PERSON_WITH_CHANGED_ADJUSTED)

        then:
        syncUpdateVerifyModifySuccess(PERSON_WITH_CHANGED_ADJUSTED,response)
        syncUpdateVerifyHasDeprecatedWarning(response)
        verifyExistsAndEquals(PERSON_WITH_CHANGED_ADJUSTED)
    }

    def "mail: update person with changed: existing has changed-attribute"() {
        given:
        persist(PERSON_WITH_CHANGED)

        expect:
        verifyExistsAndEquals(PERSON_WITH_CHANGED)

        when:
        def response = mailModify(PERSON_WITH_CHANGED_ADJUSTED)

        then:
        mailVerifyModifySuccess(PERSON_WITH_CHANGED_ADJUSTED,response)
        mailVerifyHasDeprecatedWarning(response)
        verifyExistsAndEquals(PERSON_WITH_CHANGED_ADJUSTED)
    }

    def "rest: update person with changed: existing has changed-attribute"() {
        given:
        persist(PERSON_WITH_CHANGED)

        expect:
        verifyExistsAndEquals(PERSON_WITH_CHANGED)

        when:
        def errorsAndWarnings = restModify(PERSON_WITH_CHANGED_ADJUSTED)

        then:
        restVerifyHasDeprecatedWarning(errorsAndWarnings)
        verifyExistsAndEquals(PERSON_WITH_CHANGED_ADJUSTED)
    }

    def "syncupdates: update person without changed: existing has changed-attribute"() {
        given:
        persist(PERSON_WITH_CHANGED)

        expect:
        verifyExistsAndEquals(PERSON_WITH_CHANGED)

        when:
        def response = syncUpdateModify(PERSON_WITHOUT_CHANGED)

        then:
        syncUpdateVerifyNoopSuccess(PERSON_WITHOUT_CHANGED,response)
        syncUpdateVerifyHasNoWarnings(response)
        verifyExistsAndEquals(PERSON_WITHOUT_CHANGED)
    }

    def "mail: update person without changed: existing has changed-attribute"() {
        given:
        persist(PERSON_WITH_CHANGED)

        expect:
        verifyExistsAndEquals(PERSON_WITH_CHANGED)

        when:
        def response = mailModify(PERSON_WITHOUT_CHANGED)

        then:
        mailVerifyNoopSuccess(PERSON_WITHOUT_CHANGED,response)
        verifyExistsAndEquals(PERSON_WITHOUT_CHANGED)
    }

    def "rest: update person without changed: existing has changed-attribute"() {
        given:
        persist(PERSON_WITH_CHANGED)

        expect:
        verifyExistsAndEquals(PERSON_WITH_CHANGED)

        when:
        def errorsAndWarnings = restModify(PERSON_WITHOUT_CHANGED)

        then:
        restVerifyHasIdenticalWarning(errorsAndWarnings)
        verifyExistsAndEquals(PERSON_WITHOUT_CHANGED)
    }

    def "syncupdates: update person with changed: existing has no changed-attribute"() {
        given:
        persist(PERSON_WITHOUT_CHANGED)

        expect:
        verifyExistsAndEquals(PERSON_WITHOUT_CHANGED)

        when:
        def response = syncUpdateCreateModify(PERSON_WITH_CHANGED)

        then:
        syncUpdateVerifyNoopSuccess(PERSON_WITH_CHANGED,response)
        verifyExistsAndEquals(PERSON_WITH_CHANGED)
    }

    def "mail: update person with changed: existing has no changed-attribute"() {
        given:
        persist(PERSON_WITHOUT_CHANGED)

        expect:
        verifyExistsAndEquals(PERSON_WITHOUT_CHANGED)

        when:
        def response = mailModify(PERSON_WITH_CHANGED)

        then:
        mailVerifyNoopSuccess(PERSON_WITH_CHANGED,response)
        verifyExistsAndEquals(PERSON_WITH_CHANGED)
    }

    def "rest: update person with changed: existing has no changed-attribute"() {
        given:
        persist(PERSON_WITHOUT_CHANGED)

        expect:
        verifyExistsAndEquals(PERSON_WITHOUT_CHANGED)

        when:
        restModify(PERSON_WITH_CHANGED)

        then:
        verifyExistsAndEquals(PERSON_WITH_CHANGED)
    }

    def "syncupdates: update person without changed: existing has no changed-attribute"() {
        given:
        persist(PERSON_WITHOUT_CHANGED)

        expect:
        verifyExistsAndEquals(PERSON_WITHOUT_CHANGED)

        when:
        def response = syncUpdateCreateModify(PERSON_WITHOUT_CHANGED_ADJUSTED)

        then:
        syncUpdateVerifyModifySuccess(PERSON_WITHOUT_CHANGED_ADJUSTED,response)
        syncUpdateVerifyHasNoWarnings(response)
        verifyExistsAndEquals(PERSON_WITHOUT_CHANGED_ADJUSTED)
    }

    def "mail: update person without changed: existing has no changed-attribute"() {
        given:
        persist(PERSON_WITHOUT_CHANGED)

        expect:
        verifyExistsAndEquals(PERSON_WITHOUT_CHANGED)

        when:
        def response = mailModify(PERSON_WITHOUT_CHANGED_ADJUSTED)

        then:
        mailVerifyModifySuccess(PERSON_WITHOUT_CHANGED_ADJUSTED,response)
        mailVerifyHasWarnings(response, 2)
        verifyExistsAndEquals(PERSON_WITHOUT_CHANGED_ADJUSTED)
    }

    def "rest: update person without changed: existing has no changed-attribute"() {
        given:
        persist(PERSON_WITHOUT_CHANGED)

        expect:
        verifyExistsAndEquals(PERSON_WITHOUT_CHANGED)

        when:
        def errorsAndWarnings = restModify(PERSON_WITHOUT_CHANGED_ADJUSTED)

        then:
        restVerifyHasDeprecatedWarning(errorsAndWarnings)
        verifyExistsAndEquals(PERSON_WITHOUT_CHANGED_ADJUSTED)
    }

    def "syncupdates: delete person with changed: existing has changed-attribute"() {
        given:
        persist(PERSON_WITH_CHANGED)

        expect:
        verifyExistsAndEquals(PERSON_WITH_CHANGED)

        when:
        def response = syncupdateDelete(PERSON_WITH_CHANGED)

        then:
        syncUpdateVerifyDeleteSuccess(PERSON_WITH_CHANGED,response)
        syncUpdateVerifyHasNoWarnings(response)
        doesNotExist(PERSON_WITH_CHANGED)
    }

    def "mail: delete person with changed: existing has changed-attribute"() {
        given:
        persist(PERSON_WITH_CHANGED)

        expect:
        verifyExistsAndEquals(PERSON_WITH_CHANGED)

        when:
        def response = mailDelete(PERSON_WITH_CHANGED)

        then:
        mailVerifyDeleteSuccess(PERSON_WITH_CHANGED,response)
        mailVerifyHasWarnings(response, 2)
        doesNotExist(PERSON_WITH_CHANGED)
    }

    def "rest: delete person with changed: existing has changed-attribute"() {
        given:
        persist(PERSON_WITH_CHANGED)

        expect:
        verifyExistsAndEquals(PERSON_WITH_CHANGED)

        when:
        def response = restDelete(PERSON_WITH_CHANGED)

        then:
        restVerifyHasDeprecatedWarning(response)
        doesNotExist(PERSON_WITH_CHANGED)
    }

    def "syncupdates: delete person without changed: existing has changed-attribute"() {
        given:
        persist(PERSON_WITH_CHANGED)

        expect:
        verifyExistsAndEquals(PERSON_WITH_CHANGED)

        when:
        def response = syncupdateDelete(PERSON_WITH_CHANGED)

        then:
        syncUpdateVerifyDeleteSuccess(PERSON_WITH_CHANGED,response)
        doesNotExist(PERSON_WITH_CHANGED)
    }

    def "mail: delete person without changed: existing has changed-attribute"() {
        given:
        persist(PERSON_WITH_CHANGED)

        expect:
        verifyExistsAndEquals(PERSON_WITH_CHANGED)

        when:
        def response = mailDelete(PERSON_WITHOUT_CHANGED)

        then:
        mailVerifyDeleteSuccess(PERSON_WITHOUT_CHANGED,response)
        doesNotExist(PERSON_WITH_CHANGED)
    }

    def "rest: delete person without changed: existing has changed-attribute"() {
        given:
        persist(PERSON_WITH_CHANGED)

        expect:
        verifyExistsAndEquals(PERSON_WITH_CHANGED)

        when:
        def response = restDelete(PERSON_WITHOUT_CHANGED)

        then:
        restVerifyHasDeprecatedWarning(response)
        doesNotExist(PERSON_WITH_CHANGED)
    }

    def "syncupdates: delete person with changed: existing has no changed-attribute"() {
        given:
        persist(PERSON_WITHOUT_CHANGED)

        expect:
        verifyExistsAndEquals(PERSON_WITHOUT_CHANGED)

        when:
        def response = syncupdateDelete(PERSON_WITH_CHANGED)

        then:
        syncUpdateVerifyDeleteSuccess(PERSON_WITH_CHANGED,response)
        doesNotExist(PERSON_WITHOUT_CHANGED)
    }

    def "mail: delete person with changed: existing has no changed-attribute"() {
        given:
        persist(PERSON_WITHOUT_CHANGED)

        expect:
        verifyExistsAndEquals(PERSON_WITHOUT_CHANGED)

        when:
        def response = mailDelete(PERSON_WITH_CHANGED)

        then:
        mailVerifyDeleteSuccess(PERSON_WITH_CHANGED,response)
        doesNotExist(PERSON_WITHOUT_CHANGED)
    }

    def "rest: delete person with changed: existing has no changed-attribute"() {
        given:
        persist(PERSON_WITHOUT_CHANGED)

        expect:
        verifyExistsAndEquals(PERSON_WITHOUT_CHANGED)

        when:
        def response = restDelete(PERSON_WITH_CHANGED)

        then:
        restVerifyHasDeprecatedWarning(response)
        doesNotExist(PERSON_WITHOUT_CHANGED)
    }

    def "syncupdates: delete person without changed: existing has no changed-attribute"() {
        given:
        persist(PERSON_WITHOUT_CHANGED)

        expect:
        verifyExistsAndEquals(PERSON_WITHOUT_CHANGED)

        when:
        def response = syncupdateDelete(PERSON_WITHOUT_CHANGED)

        then:
        syncUpdateVerifyDeleteSuccess(PERSON_WITHOUT_CHANGED,response)
        syncUpdateVerifyHasNoWarnings(response)
        doesNotExist(PERSON_WITHOUT_CHANGED)
    }

    def "mail: delete person without changed: existing has no changed-attribute"() {
        given:
        persist(PERSON_WITHOUT_CHANGED)

        expect:
        verifyExistsAndEquals(PERSON_WITHOUT_CHANGED)

        when:
        def response = mailDelete(PERSON_WITHOUT_CHANGED)

        then:
        mailVerifyDeleteSuccess(PERSON_WITHOUT_CHANGED,response)
        mailVerifyHasWarnings(response, 2)
        doesNotExist(PERSON_WITHOUT_CHANGED)
    }

    def "rest: delete person without changed: existing has no changed-attribute"() {
        given:
        persist(PERSON_WITHOUT_CHANGED)

        expect:
        verifyExistsAndEquals(PERSON_WITHOUT_CHANGED)

        when:
        def response = restDelete(PERSON_WITHOUT_CHANGED)

        then:
        restVerifyHasDeprecatedWarning(response)
        doesNotExist(PERSON_WITHOUT_CHANGED)
    }

    def persist(String uid) {
        dbfixture(getTransient(uid))
    }

    def doesNotExist( final String uid) {
        RpslObject obj = RpslObject.parse(getTransient(uid));
        queryObjectNotFound("-r -B -T person " + obj.getValueForAttribute(AttributeType.NIC_HDL).toString(),
                "person", obj.getValueForAttribute(AttributeType.PERSON).toString());
    }

    def verifyExistsAndEquals(final String uid) {
        // collect specific comparson values: Not all because last-modified-created are in the way
        RpslObject obj = RpslObject.parse(getTransient(uid));
        final String nicHandleValue = obj.getValueForAttribute(AttributeType.NIC_HDL).toString();
        final String personValue = obj.getValueForAttribute(AttributeType.PERSON).toString();

        final String queryResponse = queryObject("-r -B -T person " + nicHandleValue, "person", personValue);

        assert queryResponse =~ personValue;
        assert queryResponse =~ nicHandleValue;
        assert queryResponse !=~ "changed:";
        return true
    }

    def syncUpdateCreateModify(final String uid) {
        return syncUpdate(getTransient(uid) + "password: owner")
    }

    def mailCreate(final String uid) {
        def msg = send new Message(
                subject: "NEW",
                body: getTransient(uid) + "password: owner"  )
        def ack = ackFor msg

        return ack
    }

    def mailModify(final String uid) {
        def msg = send new Message(
                subject: "",
                body: getTransient(uid) + "password: owner"  )
        def ack = ackFor msg

        return ack
    }

    def List<ErrorMessage> restCreate(final String uid) {
        RpslObject obj = RpslObject.parse(getTransient(uid));
        List<ErrorMessage> errorMesages = Lists.newArrayList();
        whoisFixture.restPost(obj,errorMesages, "owner");
        return errorMesages;
    }

    def List<ErrorMessage> restModify(final String uid) {
        RpslObject obj = RpslObject.parse(getTransient(uid));
        List<ErrorMessage> errorMesages = Lists.newArrayList();
        whoisFixture.restPut(obj,errorMesages, "owner");
        return errorMesages;
    }

    def List<ErrorMessage> restDelete(final String uid) {
        RpslObject obj = RpslObject.parse(getTransient(uid));
        List<ErrorMessage> errorMesages = Lists.newArrayList();
        whoisFixture.restDelete(obj,errorMesages, "owner");
        return errorMesages;
    }

    def syncUpdateVerifyCreateSuccess(final String uid, final String response ) {
        RpslObject obj = RpslObject.parse(getTransient(uid));
        assert response =~ "Create SUCCEEDED: \\[person\\] " + obj.getValueForAttribute(AttributeType.NIC_HDL).toString()
        return true
    }

    def mailVerifyCreateSuccess(final String uid, final AckResponse response ) {
        RpslObject obj = RpslObject.parse(getTransient(uid));
        assert response.subject =~ "SUCCESS: NEW"
        assert response.contents =~ "Number of objects processed successfully:  1"
        assert response.contents =~ "Create:         1"
        assert response.contents =~ "Create SUCCEEDED: \\[person\\] " + obj.getValueForAttribute(AttributeType.NIC_HDL).toString() + "   " +
                obj.getValueForAttribute(AttributeType.PERSON).toString()

        return true
    }

    def syncUpdateModify(final String uid) {
        return syncUpdate(getTransient(uid) + "password: owner")
    }

    def syncUpdateVerifyNoopSuccess(final String uid, final String response ) {
        RpslObject obj = RpslObject.parse(getTransient(uid));
        assert response =~ "No operation: \\[person\\] " + obj.getValueForAttribute(AttributeType.NIC_HDL).toString() + "   " +
                obj.getValueForAttribute(AttributeType.PERSON).toString()

        return true
    }

    def syncUpdateVerifyModifySuccess(final String uid, final String response ) {
        RpslObject obj = RpslObject.parse(getTransient(uid));
        assert response =~ "Modify SUCCEEDED: \\[person\\] " + obj.getValueForAttribute(AttributeType.NIC_HDL).toString() + "   " +
                obj.getValueForAttribute(AttributeType.PERSON).toString()

        return true
    }

    def mailVerifyNoopSuccess(final String uid, final AckResponse response ) {
        RpslObject obj = RpslObject.parse(getTransient(uid));
        assert response.subject =~ "SUCCESS:"
        assert response.contents =~ "Number of objects processed successfully:  1"
        assert response.contents =~ "No Operation:   1"
        assert response.contents =~ "No operation: \\[person\\] " + obj.getValueForAttribute(AttributeType.NIC_HDL).toString() + "   " +
                obj.getValueForAttribute(AttributeType.PERSON).toString()

        return true
    }

    def mailVerifyModifySuccess(final String uid, final AckResponse response ) {
        RpslObject obj = RpslObject.parse(getTransient(uid));
        assert response.subject =~ "SUCCESS:"
        assert response.contents =~ "Number of objects processed successfully:  1"
        assert response.contents =~ "Modify:         1"
        assert response.contents =~ "Modify SUCCEEDED: \\[person\\] " + obj.getValueForAttribute(AttributeType.NIC_HDL).toString() + "   " +
                obj.getValueForAttribute(AttributeType.PERSON).toString()

        return true
    }

    def syncUpdateVerifyNoOperation(final String uid, final String response ) {
        RpslObject obj = RpslObject.parse(getTransient(uid));
        assert response =~ "No operation: \\[person\\] " + obj.getValueForAttribute(AttributeType.NIC_HDL).toString() + "   " +
                obj.getValueForAttribute(AttributeType.PERSON).toString()

        return true
    }

    def mailVerifyNoOperation(final String uid, final AckResponse response ) {
        RpslObject obj = RpslObject.parse(getTransient(uid));
        assert response.subject =~ "SUCCESS:"
        assert response.contents =~ "Number of objects processed successfully:  1"
        assert response.contents =~ "No Operation:   1"
        assert response.contents =~ "No operation: \\[person\\] " + obj.getValueForAttribute(AttributeType.NIC_HDL).toString() + "   " +
                obj.getValueForAttribute(AttributeType.PERSON).toString()

        return true
    }

    def syncupdateDelete(final String uid) {
        return syncUpdate(getTransient(uid) + "password: owner\n" + "delete:testing")
    }

    def mailDelete(final String uid) {
        def msg = send new Message(
                subject: "",
                body: getTransient(uid) + "delete: testing\npassword: owner"  )
        def ack = ackFor msg

        return ack
    }

    def syncUpdateVerifyDeleteSuccess(final String uid, final String response ) {
        RpslObject obj = RpslObject.parse(getTransient(uid));
        assert response =~ "Delete SUCCEEDED: \\[person\\] " + obj.getValueForAttribute(AttributeType.NIC_HDL).toString() + "   " +
                obj.getValueForAttribute(AttributeType.PERSON).toString()
        return true
    }

    def mailVerifyDeleteSuccess(final String uid, final AckResponse response ) {
        RpslObject obj = RpslObject.parse(getTransient(uid));
        assert response.subject =~ "SUCCESS:"
        assert response.contents =~ "Number of objects processed successfully:  1"
        assert response.contents =~ "Delete:         1"
        assert response.contents =~ "Delete SUCCEEDED: \\[person\\] " + obj.getValueForAttribute(AttributeType.NIC_HDL).toString() + "   " +
                obj.getValueForAttribute(AttributeType.PERSON).toString()

        return true
    }

    def syncUpdateVerifyHasDeprecatedWarning(final String response ) {
        assert response =~ "Warning: Deprecated attribute \"changed\". This attribute has been removed."
        return true
    }

    def restVerifyHasDeprecatedWarning( final List<ErrorMessage> errorsAndWarnings ) {
        boolean found = false;
        for (ErrorMessage msg : errorsAndWarnings) {
            if( msg.toString().contains("Deprecated attribute \"changed\". This attribute has been removed.") ||
                msg.toString().contains("MD5 hashed password authentication is deprecated and support will be removed at the end of 2025. Please switch to an alternative authentication method before then.")) {
                found = true;
                break;
            }
        }
        assert found
        return true
    }

    def mailVerifyHasDeprecatedWarning(final AckResponse response  ) {
        response.contents =~ "Warning: Deprecated attribute \"changed\". This attribute has been removed."
    }

    def syncUpdateVerifyHasIdenticalWarning(final String response ) {
        assert response =~ "Warning: Submitted object identical to database object"
        return true
    }

    def restVerifyHasIdenticalWarning( final List<ErrorMessage> errorsAndWarnings ) {
        boolean found = false;
        for (ErrorMessage msg : errorsAndWarnings) {
            if( msg.toString().contains("Submitted object identical to database object")) {
                found = true;
                break;
            }
        }
        assert found
        return true
    }

    def mailVerifyHasIdenticalWarning(final AckResponse response  ) {
        assert response.contents =~ "Warning: Submitted object identical to database object"
        return true
    }

    def syncUpdateVerifyHasNoWarnings(final String response ) {
        assert response !=~  "Warning:"
        return true
    }

    def mailVerifyHasWarnings(final AckResponse response, final int number ) {
        def warnings = response.allWarnings
        assert warnings.size() == number
        return true
    }

}