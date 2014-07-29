package net.ripe.db.whois.internal.api.rnd.dao;


import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.internal.AbstractInternalTest;
import net.ripe.db.whois.internal.api.rnd.domain.ObjectReference;
import net.ripe.db.whois.internal.api.rnd.domain.ObjectVersion;
import net.ripe.db.whois.internal.api.rnd.domain.ReferenceType;
import org.joda.time.Interval;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class JdbcObjectReferenceDaoTest extends AbstractInternalTest {
    @Autowired
    ObjectReferenceDao subject;

    @Before
    public void setup() {
        whoisTemplate.execute(
                "INSERT INTO object_version (version_id, object_type, pkey, from_timestamp, to_timestamp) " +
                "VALUES " +
                "(1, 9, 'MNTNER1', 1000, 2000), " +
                "(2, 9, 'MNTNER1', 2000, 3000), " +
                "(3, 9, 'MNTNER1', 3000, NULL), " +
                "(4, 10, 'TP1-TEST', 1000, 2000), " +
                "(5, 10, 'TP1-TEST', 2000, 2000), " +
                "(6, 10, 'TP1-TEST', 2000, NULL);");

        /*
            MNTNER1:
               mnt-by: MNTNER1
               admin-c: TP1-TEST
            ORG1:
               mnt-by: MNTNER1
         */
        whoisTemplate.execute(
                "INSERT INTO object_reference (version_id, object_type, pkey, ref_type) " +
                        "VALUES " +
                        "\t(3, 10, 'TP1-TEST', 0), " +
                        "\t(3, 9, 'MNTNER1', 0), " +
                        "\t(3, 9, 'MNTNER1', 1), " +
                        "\t(3, 18, 'ORG1', 1); ");
    }

    @After
    public void teardown() {
        whoisTemplate.execute("DELETE FROM object_reference");
        whoisTemplate.execute("DELETE FROM object_version");
    }

    @Test
    public void version_between_interval() {
        final List<ObjectVersion> versions = subject.getObjectVersion(ObjectType.MNTNER, "MNTNER1", 1500);
        assertThat(versions, hasSize(1));
        assertThat(versions.get(0), is(
                new ObjectVersion(1L, ObjectType.MNTNER, "MNTNER1", new Interval(1000000L, 2000000L))));
    }

    @Test
    public void query_version_with_null_end_date(){
        final List<ObjectVersion> versions = subject.getObjectVersion(ObjectType.MNTNER, "MNTNER1", 5000);
        assertThat(versions, hasSize(1));
        assertThat(versions.get(0), is(
                new ObjectVersion(3L, ObjectType.MNTNER, "MNTNER1", new Interval(3000000L, Long.MAX_VALUE))));
    }

    @Test
    public void start_timestamp_parameter_returns_version() {
        final List<ObjectVersion> versions = subject.getObjectVersion(ObjectType.MNTNER, "MNTNER1", 2000);
        assertThat(versions, hasSize(2));
        assertThat(versions, contains(
                new ObjectVersion(2L, ObjectType.MNTNER, "MNTNER1", new Interval(2000000L, 3000000L)),
                new ObjectVersion(1L, ObjectType.MNTNER, "MNTNER1", new Interval(1000000L, 2000000L))));
    }

    @Test
    public void end_timestamp_parameter_returns_version(){
        final List<ObjectVersion> versions = subject.getObjectVersion(ObjectType.MNTNER, "MNTNER1", 1500);
        assertThat(versions, hasSize(1));
        assertThat(versions.get(0), is(
                new ObjectVersion(1L, ObjectType.MNTNER, "MNTNER1", new Interval(1000000L, 2000000L))));
    }

    @Test
    public void multiple_versions_for_timestamp(){
        final List<ObjectVersion> versions = subject.getObjectVersion(ObjectType.PERSON, "TP1-TEST", 2000);
        assertThat(versions, hasSize(3));
        assertThat(versions, contains(
                new ObjectVersion(6L, ObjectType.PERSON, "TP1-TEST", new Interval(2000000L, Long.MAX_VALUE)),
                new ObjectVersion(5L, ObjectType.PERSON, "TP1-TEST", new Interval(2000000L, 2000000L)),
                new ObjectVersion(4L, ObjectType.PERSON, "TP1-TEST", new Interval(1000000L, 2000000L))));
    }
    
    @Test
    public void test_referencing() {
        final List<ObjectReference> referencing = subject.getOutgoing(3);

        assertThat(referencing, hasSize(2));
        assertThat(referencing , containsInAnyOrder(
                        new ObjectReference(3, ObjectType.MNTNER, "MNTNER1", ReferenceType.OUTGOING),
                        new ObjectReference(3, ObjectType.PERSON, "TP1-TEST", ReferenceType.OUTGOING)));
    }

    @Test
    public void test_referencedBy() {
        final List<ObjectReference> referencedBy = subject.getIncoming(3);

        assertThat(referencedBy, hasSize(2));
        assertThat(referencedBy , containsInAnyOrder(
                new ObjectReference(3, ObjectType.MNTNER, "MNTNER1", ReferenceType.INCOMING),
                new ObjectReference(3, ObjectType.ORGANISATION, "ORG1", ReferenceType.INCOMING)));
    }
}