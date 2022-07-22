package net.ripe.db.whois.common.dao.jdbc;

import net.ripe.db.whois.common.dao.ReferencesDao;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.support.AbstractDaoIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;
import java.util.Set;

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@org.junit.jupiter.api.Tag("IntegrationTest")
public class JdbcReferencesDaoIntegrationTest extends AbstractDaoIntegrationTest {

    @Autowired ReferencesDao subject;
    @Value("${whois.source}") protected String source;

    @BeforeEach
    public void setup() {
        sourceContext.setCurrent(Source.slave(source));
    }

    @AfterEach
    public void cleanup() {
        sourceContext.removeCurrentSource();
    }

    @Test
    public void invalid_reference() {
        final RpslObject maintainer = RpslObject.parse("mntner: TEST-MNT\nmnt-by:TEST-MNT\nadmin-c:NIC-TEST");

        final Map<RpslAttribute, Set<CIString>> invalidReferences = subject.getInvalidReferences(maintainer);
        assertThat(invalidReferences.keySet(), contains(maintainer.findAttribute(AttributeType.ADMIN_C)));
        assertThat(invalidReferences.get(maintainer.findAttribute(AttributeType.ADMIN_C)), contains(ciString("NIC-TEST")));
    }

    @Test
    public void valid_reference() {
        final RpslObject noInvalidRefPerson = RpslObject.parse("person: other person\nnic-hdl:NIC2-TEST");

        assertThat(subject.getInvalidReferences(noInvalidRefPerson).keySet(), hasSize(0));
    }

    @Test
    public void valid_persisted_reference() {
        databaseHelper.addObject(RpslObject.parse("person: person\nnic-hdl:NIC1-TEST"));
        final RpslObject maintainer2 = RpslObject.parse("mntner: TEST-MNT\nmnt-by:TEST-MNT\nadmin-c:NIC1-TEST");

        assertThat(subject.getInvalidReferences(maintainer2).keySet(), hasSize(0));
    }

    @Test
    public void is_referenced() {
        final RpslObject referenced = RpslObject.parse("person:person\nnic-hdl:NIC3-TEST");
        databaseHelper.addObject(referenced);

        final RpslObject mntRpsl = RpslObject.parse("mntner:MNT-TEST\nmnt-by:MNT-TEST\nadmin-c:NIC3-TEST");
        databaseHelper.addObject(mntRpsl);

        assertThat(subject.isReferenced(referenced), is(true));
    }

    @Test
    public void is_not_referenced() {
        final RpslObject person2 = RpslObject.parse("person: person\nnic-hdl:NIC5-TEST");
        databaseHelper.addObject(person2);

        assertThat(subject.isReferenced(person2), is(false));
    }

    @Test
    public void getOrgAttributeReference() {
        final RpslObject object = databaseHelper.addObject(RpslObject.parse("organisation: ORG-TEST"));

        final RpslObject inet = RpslObject.parse("inetnum: 192.168.0.0 - 192.168.0.255\norg:ORG-TEST\nnetname: TEST-RIPE");
        databaseHelper.addObject(inet);

        final RpslObjectInfo attributeReference = subject.getAttributeReference(AttributeType.ORG, ciString("ORG-TEST"));
        assertThat(attributeReference.getObjectId(), is(object.getObjectId()));
    }

    @Test
    public void getSponsoringOrgReference() {
        final RpslObject object = databaseHelper.addObject(RpslObject.parse("organisation: ORG-TEST"));

        final RpslObject inet = RpslObject.parse("inetnum: 192.168.0.0 - 192.168.0.255\nsponsoring-org:ORG-TEST\nnetname: TEST-RIPE");
        databaseHelper.addObject(inet);

        final RpslObjectInfo attributeReference = subject.getAttributeReference(AttributeType.SPONSORING_ORG, ciString("ORG-TEST"));
        assertThat(attributeReference.getObjectId(), is(object.getObjectId()));
    }

    @Test
    public void getReferences() {
        final RpslObject role = RpslObject.parse("role: Role\nnic-hdl: NIC-TEST\nabuse-mailbox:abuse@ripe.net");
        databaseHelper.addObject(role);

        final RpslObject org = RpslObject.parse("organisation: ORG-TEST\nabuse-c: NIC-TEST");
        databaseHelper.addObject(org);

        final Set<RpslObjectInfo> roleReferences = subject.getReferences(role);

        assertThat(roleReferences.size(), is(1));
        assertThat(roleReferences.iterator().next().getKey(), is("ORG-TEST"));
    }

    @Test
    public void getReferences_none_found() {
        final RpslObject role = RpslObject.parse("role: Role\nnic-hdl: NIC-TEST\nabuse-mailbox:abuse@ripe.net");
        databaseHelper.addObject(role);

        final RpslObject org = RpslObject.parse("organisation: ORG-TEST");
        databaseHelper.addObject(org);

        final Set<RpslObjectInfo> roleReferences = subject.getReferences(role);

        assertThat(roleReferences.size(), is(0));
    }
}
