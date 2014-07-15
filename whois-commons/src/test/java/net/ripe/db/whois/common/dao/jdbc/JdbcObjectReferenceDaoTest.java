package net.ripe.db.whois.common.dao.jdbc;

import net.ripe.db.whois.common.dao.ObjectReferenceDao;
import net.ripe.db.whois.common.domain.ObjectReference;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.support.AbstractDaoTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class JdbcObjectReferenceDaoTest extends AbstractDaoTest {

    @Autowired
    ObjectReferenceDao subject;

    @Before
    public void setup() {
        databaseHelper.getWhoisTemplate().update("" +
                "INSERT INTO `object_reference` " +
                "(`from_object_type`, `from_pkey`, `from_object_id`, `from_sequence_id`, `to_object_type`, `to_pkey`, `to_object_id`, `to_sequence_id`, `from_timestamp`, `to_timestamp`)\n" +
                "VALUES\n" +
                "\t(10, 'TP1-TEST', 101, 1, 9,  'MNTNER1',  201, 2, 2001, 3000),\n" +
                "\t(10, 'TP1-TEST', 101, 1, 9,  'MNTNER1',  201, 1, 1001, 2000),\n" +
                "\t(10, 'TP1-TEST', 101, 2, 9,  'MNTNER2',  202, 1, 3001, NULL),\n" +
                "\t(18, 'ORG1',     102, 1, 9,  'MNTNER2',  202, 1, 3001, NULL),\n" +
                "\t(18, 'ORG1',     102, 1, 10, 'TP1-TEST', 101, 1, 1001, 3000),\n" +
                "\t(18, 'ORG1',     102, 1, 10, 'TP1-TEST', 101, 2, 3001, NULL);\n");
    }

    @Test
    public void testGetReferencing() throws Exception {
        final List<ObjectReference> referencing1 = subject.getReferencing(ObjectType.ORGANISATION, "ORG1", 2000);

        assertThat(referencing1, hasSize(1));
        assertThat(referencing1.get(0).getToPkey(), is("TP1-TEST"));
        assertThat(referencing1.get(0).getToSequenceId(), is(1));

        final List<ObjectReference> referencing2 = subject.getReferencing(ObjectType.ORGANISATION, "ORG1", 5000);

        assertThat(referencing2, hasSize(2));
        assertThat(referencing2.get(0).getToPkey(), is("MNTNER2"));
        assertThat(referencing2.get(0).getToObjectId(), is(202));

        assertThat(referencing2.get(1).getToPkey(), is("TP1-TEST"));
        assertThat(referencing2.get(1).getToSequenceId(), is(2));

    }

    @Test
    public void testGetReferenced() throws Exception {
        final List<ObjectReference> referenced1 = subject.getReferenced(ObjectType.MNTNER, "MNTNER1", 1500);

        assertThat(referenced1, hasSize(1));
        assertThat(referenced1.get(0).getFromPkey(), is("TP1-TEST"));
        assertThat(referenced1.get(0).getFromSequenceId(), is(1));

        final List<ObjectReference> referenced2 = subject.getReferenced(ObjectType.MNTNER, "MNTNER2", 5000);

        assertThat(referenced2, hasSize(2));
        assertThat(referenced2.get(0).getFromPkey(), is("TP1-TEST"));
        assertThat(referenced2.get(0).getFromSequenceId(), is(2));
        assertThat(referenced2.get(1).getFromPkey(), is("ORG1"));
        assertThat(referenced2.get(1).getFromObjectId(), is(102));
    }
}