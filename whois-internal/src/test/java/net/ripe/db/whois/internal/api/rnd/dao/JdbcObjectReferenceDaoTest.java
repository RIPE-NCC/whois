package net.ripe.db.whois.internal.api.rnd.dao;


import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.internal.AbstractInternalTest;
import net.ripe.db.whois.internal.api.rnd.domain.ObjectReference;
import net.ripe.db.whois.internal.api.rnd.domain.ObjectVersion;
import net.ripe.db.whois.internal.api.rnd.domain.ReferenceType;
import org.joda.time.Interval;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class JdbcObjectReferenceDaoTest extends AbstractInternalTest {
    @Autowired
    ObjectReferenceDao subject;

    @Before
    public void setup() {
        whoisTemplate.execute(
                "INSERT INTO object_version (version_id, object_type, pkey, from_timestamp, to_timestamp)\n" +
                "VALUES\n" +
                "(1, 9, 'MNTNER1', 1000, 2000),\n" +
                "(2, 9, 'MNTNER1', 2000, 3000),\n" +
                "(3, 9, 'MNTNER1', 3000, NULL),\n" +
                "(4, 10, 'TP1-TEST', 1000, 2000),\n" +
                "(5, 10, 'TP1-TEST', 2000, 2000),\n" +
                "(6, 10, 'TP1-TEST', 2000, NULL);");

        /*
            MNTNER1:
               mnt-by: MNTNER1
               admin-c: TP1-TEST
            ORG1:
               mnt-by: MNTNER1
         */
        whoisTemplate.execute(
                "INSERT INTO object_reference (version_id, object_type, pkey, ref_type)\n" +
                        "VALUES\n" +
                        "\t(3, 10, 'TP1-TEST', 0),\n" +
                        "\t(3, 9, 'MNTNER1', 0),\n" +
                        "\t(3, 9, 'MNTNER1', 1),\n" +
                        "\t(3, 18, 'ORG1', 1);\n");
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
        final List<ObjectReference> referencing = subject.getReferencing(3);

        assertThat(referencing, hasSize(2));
        assertThat(referencing , containsInAnyOrder(
                        new ObjectReference(3, ObjectType.MNTNER, "MNTNER1", ReferenceType.REFERENCING),
                        new ObjectReference(3, ObjectType.PERSON, "TP1-TEST", ReferenceType.REFERENCING)));
    }

    @Test
    public void test_referencedBy() {
        final List<ObjectReference> referencedBy = subject.getReferencedBy(3);

        assertThat(referencedBy, hasSize(2));
        assertThat(referencedBy , containsInAnyOrder(
                new ObjectReference(3, ObjectType.MNTNER, "MNTNER1", ReferenceType.REFERENCED_BY),
                new ObjectReference(3, ObjectType.ORGANISATION, "ORG1", ReferenceType.REFERENCED_BY)));
    }
}