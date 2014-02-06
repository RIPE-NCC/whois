package net.ripe.db.whois.scheduler.task.unref;

import com.google.common.collect.Sets;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.scheduler.AbstractSchedulerIntegrationTest;
import net.ripe.db.whois.update.domain.ObjectKey;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Set;

import static net.ripe.db.whois.scheduler.task.unref.UnrefCleanup.DeleteCandidate;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

public class UnrefCleanupDaoTest extends AbstractSchedulerIntegrationTest {
    final static Set<ObjectType> referenceCheckObjectTypes = Sets.newHashSet(ObjectType.PERSON, ObjectType.ROLE);

    @Autowired UnrefCleanupDao subject;

    @Test
    public void getDeleteCandidates_empty() {
        final Map<ObjectKey, UnrefCleanup.DeleteCandidate> deleteCandidates = subject.getDeleteCandidates(referenceCheckObjectTypes);
        assertThat(deleteCandidates.entrySet(), hasSize(0));
    }

    @Test
    public void getDeleteCandidates_person() {
        databaseHelper.addObject(RpslObject.parse("person: test\nnic-hdl: TEST-PN"));

        final Map<ObjectKey, DeleteCandidate> deleteCandidates = subject.getDeleteCandidates(referenceCheckObjectTypes);
        final Set<Map.Entry<ObjectKey, DeleteCandidate>> entrySet = deleteCandidates.entrySet();
        assertThat(entrySet, hasSize(1));

        final Map.Entry<ObjectKey, DeleteCandidate> next = entrySet.iterator().next();
        assertThat(next.getKey(), is(new ObjectKey(ObjectType.PERSON, "TEST-PN")));
        assertThat(next.getValue().getObjectId(), is(1));
        assertThat(next.getValue().getCreationDate(), is(new LocalDate()));
    }

    @Test
    public void getDeleteCandidates_person_deleted_and_readded() {
        final RpslObject person = RpslObject.parse("person: test\nnic-hdl: TEST-PN");
        databaseHelper.deleteObject(databaseHelper.addObject(person));
        databaseHelper.addObject(person);

        final Map<ObjectKey, DeleteCandidate> deleteCandidates = subject.getDeleteCandidates(referenceCheckObjectTypes);
        final Set<Map.Entry<ObjectKey, DeleteCandidate>> entrySet = deleteCandidates.entrySet();
        assertThat(entrySet, hasSize(1));

        final Map.Entry<ObjectKey, DeleteCandidate> next = entrySet.iterator().next();
        assertThat(next.getKey(), is(new ObjectKey(ObjectType.PERSON, "TEST-PN")));
        assertThat(next.getValue().getObjectId(), is(2));
        assertThat(next.getValue().getCreationDate(), is(new LocalDate()));
    }

    @Test
    public void getDeleteCandidates_person_and_role() {
        databaseHelper.addObject(RpslObject.parse("person: test\nnic-hdl: TEST-PN"));
        databaseHelper.addObject(RpslObject.parse("role: test\nnic-hdl: TEST-RL"));

        final Map<ObjectKey, DeleteCandidate> deleteCandidates = subject.getDeleteCandidates(referenceCheckObjectTypes);
        assertThat(deleteCandidates.entrySet(), hasSize(2));
        assertThat(deleteCandidates.get(new ObjectKey(ObjectType.PERSON, "TEST-PN")).getObjectId(), is(1));
        assertThat(deleteCandidates.get(new ObjectKey(ObjectType.ROLE, "TEST-RL")).getObjectId(), is(2));
    }

    @Test
    public void getDeleteCandidates_person_and_inetnum() {
        databaseHelper.addObject(RpslObject.parse("person: test\nnic-hdl: TEST-PN"));
        databaseHelper.addObject(RpslObject.parse("inetnum: 193.0.0.10\nnetname: netname"));

        final Map<ObjectKey, DeleteCandidate> deleteCandidates = subject.getDeleteCandidates(referenceCheckObjectTypes);
        assertThat(deleteCandidates.entrySet(), hasSize(1));
        assertThat(deleteCandidates.get(new ObjectKey(ObjectType.PERSON, "TEST-PN")).getObjectId(), is(1));
    }

    @Test
    public void getDeleteCandidates_key_determined_by_type_and_pkey() {
        databaseHelper.addObject(RpslObject.parse("person: test\nnic-hdl: TEST-PN"));
        databaseHelper.addObject(RpslObject.parse("role: test\nnic-hdl: TEST-PN"));

        subject.getDeleteCandidates(referenceCheckObjectTypes);
    }

    @Test
    public void doForCurrentRpslObjects_empty() {
        final UnrefCleanupDao.DeleteCandidatesFilter objectCallback = mock(UnrefCleanupDao.DeleteCandidatesFilter.class);

        subject.doForCurrentRpslObjects(objectCallback);

        verifyZeroInteractions(objectCallback);
    }

    @Test
    public void doForCurrentRpslObjects_all() {
        final RpslObject person = RpslObject.parse("person: test\nnic-hdl: TEST-PN");
        databaseHelper.addObject(person);

        final RpslObject inetnum = RpslObject.parse("inetnum: 193.0.0.10\nnetname: netname");
        databaseHelper.addObject(inetnum);

        final Set<RpslObject> handled = Sets.newHashSet();

        subject.doForCurrentRpslObjects(new UnrefCleanupDao.DeleteCandidatesFilter() {
            @Override
            public void filter(final RpslObject rpslObject, final LocalDate date) {
                handled.add(rpslObject);
            }
        });

        assertThat(handled, hasSize(2));
        assertThat(handled.contains(person), is(true));
        assertThat(handled.contains(inetnum), is(true));
    }

    @Test
    public void doForHistoricRpslObjects_empty() {
        final UnrefCleanupDao.DeleteCandidatesFilter objectCallback = mock(UnrefCleanupDao.DeleteCandidatesFilter.class);

        subject.doForHistoricRpslObjects(objectCallback, new LocalDate());

        verifyZeroInteractions(objectCallback);
    }

    @Test
    public void doForHistoricRpslObjects_person_and_role() {
        final RpslObject person = RpslObject.parse("person: test\nnic-hdl: TEST-PN");
        databaseHelper.deleteObject(databaseHelper.addObject(person));

        final RpslObject inetnum = RpslObject.parse("inetnum: 193.0.0.10\nnetname: netname");
        databaseHelper.deleteObject(databaseHelper.addObject(inetnum));

        final Set<RpslObject> handled = Sets.newHashSet();

        subject.doForHistoricRpslObjects(new UnrefCleanupDao.DeleteCandidatesFilter() {
            @Override
            public void filter(final RpslObject rpslObject, final LocalDate date) {
                handled.add(rpslObject);
            }
        }, new LocalDate().minusDays(1));

        assertThat(handled, hasSize(2));
        assertThat(handled.contains(person), is(true));
        assertThat(handled.contains(inetnum), is(true));
    }

    @Test
    public void doForHistoricRpslObjects_person_and_role_too_long_ago() {
        final RpslObject person = RpslObject.parse("person: test\nnic-hdl: TEST-PN");
        databaseHelper.deleteObject(databaseHelper.addObject(person));

        final RpslObject inetnum = RpslObject.parse("inetnum: 193.0.0.10\nnetname: netname");
        databaseHelper.deleteObject(databaseHelper.addObject(inetnum));

        final Set<RpslObject> handled = Sets.newHashSet();

        subject.doForHistoricRpslObjects(new UnrefCleanupDao.DeleteCandidatesFilter() {
            @Override
            public void filter(final RpslObject rpslObject, final LocalDate date) {
                handled.add(rpslObject);
            }
        }, new LocalDate().plusDays(1));

        assertThat(handled, hasSize(0));
    }
}
