package net.ripe.db.whois.internal.api.rnd.dao;


import net.ripe.db.whois.internal.AbstractInternalTest;
import org.junit.Ignore;
import org.springframework.beans.factory.annotation.Autowired;

@Ignore
public class JdbcObjectReferenceDaoTest extends AbstractInternalTest {
    @Autowired
    ObjectReferenceDao subject;

//    @Test
//    public void test_referencing() {
//        final List<ObjectReference> referencing = subject.getOutgoing(3);
//
//        assertThat(referencing, hasSize(2));
//        assertThat(referencing , containsInAnyOrder(
//                        new ObjectReference(3, ObjectType.MNTNER, "MNTNER1", ReferenceType.OUTGOING),
//                        new ObjectReference(3, ObjectType.PERSON, "TP1-TEST", ReferenceType.OUTGOING)));
//    }
//
//    @Test
//    public void test_referencedBy() {
//        final List<ObjectReference> referencedBy = subject.getIncoming(3);
//
//        assertThat(referencedBy, hasSize(2));
//        assertThat(referencedBy , containsInAnyOrder(
//                new ObjectReference(3, ObjectType.MNTNER, "MNTNER1", ReferenceType.INCOMING),
//                new ObjectReference(3, ObjectType.ORGANISATION, "ORG1", ReferenceType.INCOMING)));
//    }
}