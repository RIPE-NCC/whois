package net.ripe.db.whois.scheduler.task.unref;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ripe.db.whois.api.whois.InternalJob;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.TagsDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Tag;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.update.domain.*;
import net.ripe.db.whois.update.handler.SingleUpdateHandler;
import net.ripe.db.whois.update.log.LoggerContext;
import net.ripe.db.whois.update.log.UpdateLog;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UnrefCleanupTest {
    @Mock UnrefCleanupDao unrefCleanupDao;
    @Mock SourceContext sourceContext;
    @Mock RpslObjectDao objectDao;
    @Mock DateTimeProvider dateTimeProvider;
    @Mock SingleUpdateHandler singleUpdateHandler;
    @Mock LoggerContext loggerContext;
    @Mock UpdateLog updateLog;
    @Mock TagsDao tagsDao;

    @InjectMocks UnrefCleanup subject;

    Map<ObjectKey, UnrefCleanup.DeleteCandidate> deleteCandidates;
    List<RpslObject> rpslObjectsInLast;
    List<RpslObject> rpslObjectsInHistory;

    @Before
    public void setUp() throws Exception {
        subject.setUnrefCleanupEnabled(true);
        subject.setUnrefCleanupDeletes(true);

        when(dateTimeProvider.getCurrentDate()).thenReturn(new LocalDate());

        deleteCandidates = Maps.newHashMap();
        when(unrefCleanupDao.getDeleteCandidates(anySet())).thenReturn(deleteCandidates);

        rpslObjectsInLast = Lists.newArrayList();
        rpslObjectsInHistory = Lists.newArrayList();

        doAnswer(new RpslObjectCallbackAnswer(rpslObjectsInLast)).when(unrefCleanupDao).doForCurrentRpslObjects(any(UnrefCleanupDao.DeleteCandidatesFilter.class));
        doAnswer(new RpslObjectCallbackAnswer(rpslObjectsInHistory)).when(unrefCleanupDao).doForHistoricRpslObjects(any(UnrefCleanupDao.DeleteCandidatesFilter.class), any(LocalDate.class));
    }

    private static final class RpslObjectCallbackAnswer implements Answer<Void> {
        private final List<RpslObject> rpslObjects;

        private RpslObjectCallbackAnswer(final List<RpslObject> rpslObjects) {
            this.rpslObjects = rpslObjects;
        }

        @Override
        public Void answer(InvocationOnMock invocation) throws Throwable {
            final UnrefCleanupDao.DeleteCandidatesFilter callback = (UnrefCleanupDao.DeleteCandidatesFilter) invocation.getArguments()[0];

            for (final RpslObject rpslObject : rpslObjects) {
                callback.filter(rpslObject, new LocalDate());
            }

            return null;
        }
    }

    @Test
    public void correct_types() {
        subject.run();

        verify(unrefCleanupDao).getDeleteCandidates(Sets.newHashSet(
                ObjectType.IRT,
                ObjectType.KEY_CERT,
                ObjectType.MNTNER,
                ObjectType.ORGANISATION,
                ObjectType.PERSON,
                ObjectType.ROLE));
    }

    @Test
    public void logging_initialized() {
        subject.run();

        verify(loggerContext).init("unrefcleanup");
        verify(loggerContext).remove();
    }

    @Test
    public void candidate_referenced_in_last() {
        final LocalDate now = new LocalDate();
        deleteCandidates.put(new ObjectKey(ObjectType.PERSON, "TEST-PN"), new UnrefCleanup.DeleteCandidate(1, now));

        rpslObjectsInLast.add(RpslObject.parse("mntner: DEV-MNT\nadmin-c: TEST-PN"));

        subject.run();

        verifyZeroInteractions(singleUpdateHandler);

        final ArgumentCaptor<List> argument = ArgumentCaptor.forClass(List.class);
        verify(tagsDao).rebuild(eq(CIString.ciString("unref")), argument.capture());
        assertThat(argument.getValue().iterator().hasNext(), is(false));
    }

    @Test
    public void candidate_referenced_in_history() {
        deleteCandidates.put(new ObjectKey(ObjectType.PERSON, "TEST-PN"), new UnrefCleanup.DeleteCandidate(1, new LocalDate()));

        rpslObjectsInHistory.add(RpslObject.parse("mntner: DEV-MNT\nadmin-c: TEST-PN"));

        subject.run();

        verifyZeroInteractions(singleUpdateHandler);

        final ArgumentCaptor<List> argument = ArgumentCaptor.forClass(List.class);
        verify(tagsDao).rebuild(eq(CIString.ciString("unref")), argument.capture());
        assertThat(argument.getValue().iterator().hasNext(), is(false));
    }

    @Test
    public void candidate_not_referenced_disabled() {
        subject.setUnrefCleanupEnabled(false);
        deleteCandidates.put(new ObjectKey(ObjectType.PERSON, "TEST-PN"), new UnrefCleanup.DeleteCandidate(1, new LocalDate()));

        subject.run();

        verifyZeroInteractions(singleUpdateHandler);
        verifyZeroInteractions(tagsDao);
    }

    @Test
    public void candidate_not_referenced_does_not_delete() {
        subject.setUnrefCleanupDeletes(false);
        deleteCandidates.put(new ObjectKey(ObjectType.PERSON, "TEST-PN"), new UnrefCleanup.DeleteCandidate(1, new LocalDate()));

        final RpslObject rpslObject = RpslObject.parse("person: test\nnic-hdl: TEST-PN");
        when(objectDao.getById(1)).thenReturn(rpslObject);

        subject.run();

        verifyZeroInteractions(singleUpdateHandler);

        final ArgumentCaptor<List> argument = ArgumentCaptor.forClass(List.class);
        verify(tagsDao).rebuild(eq(CIString.ciString("unref")), argument.capture());

        assertThat(argument.getValue().iterator().hasNext(), is(true));
        final Tag tag = (Tag) argument.getValue().iterator().next();
        assertThat(tag.getObjectId(), is(1));
        assertThat(tag.getType(), is(CIString.ciString("unref")));
    }

    @Test
    public void candidate_referenced_in_last_self_reference_only() {
        deleteCandidates.put(new ObjectKey(ObjectType.MNTNER, "DEV-MNT"), new UnrefCleanup.DeleteCandidate(1, new LocalDate()));

        final RpslObject rpslObject = RpslObject.parse("mntner: DEV-MNT\nmnt-by: DEV-MNT");
        rpslObjectsInLast.add(rpslObject);

        when(objectDao.getById(1)).thenReturn(rpslObject);
        when(dateTimeProvider.getCurrentDate()).thenReturn(new LocalDate().plusDays(100));

        subject.run();

        verify(singleUpdateHandler).handle(any(Origin.class), any(Keyword.class), any(Update.class), any(UpdateContext.class));

        verifyZeroInteractions(singleUpdateHandler);

        final ArgumentCaptor<List> argument = ArgumentCaptor.forClass(List.class);
        verify(tagsDao).rebuild(eq(CIString.ciString("unref")), argument.capture());
        assertThat(argument.getValue().iterator().hasNext(), is(false));
    }

    @Test
    public void candidate_not_referenced() {
        deleteCandidates.put(new ObjectKey(ObjectType.PERSON, "TEST-PN"), new UnrefCleanup.DeleteCandidate(1, new LocalDate()));

        final RpslObject rpslObject = RpslObject.parse("person: test\nnic-hdl: TEST-PN");
        when(objectDao.getById(1)).thenReturn(rpslObject);
        when(dateTimeProvider.getCurrentDate()).thenReturn(new LocalDate().plusDays(100));

        subject.run();

        verify(singleUpdateHandler).handle(argThat(
                new ArgumentMatcher<Origin>() {
                    @Override
                    public boolean matches(final Object argument) {
                        return argument instanceof InternalJob;
                    }
                }),
                eq(Keyword.NONE),
                argThat(new ArgumentMatcher<Update>() {
                    @Override
                    public boolean matches(final Object argument) {
                        if (!(argument instanceof Update)) {
                            return false;
                        }

                        final Update update = (Update) argument;
                        return !update.isOverride()
                                && !update.isSigned()
                                && update.getOperation().equals(Operation.DELETE)
                                && update.getCredentials().all().isEmpty()
                                && update.getDeleteReasons().containsAll(Lists.newArrayList("unreferenced object cleanup"))
                                && update.getSubmittedObject().equals(rpslObject)
                                && update.getType().equals(ObjectType.PERSON)
                                && update.getParagraph().getContent().equals("" +
                                "delete: unreferenced object cleanup\n" +
                                "person:         test\n" +
                                "nic-hdl:        TEST-PN\n");
                    }
                }), any(UpdateContext.class));

        final ArgumentCaptor<List> argument = ArgumentCaptor.forClass(List.class);
        verify(tagsDao).rebuild(eq(CIString.ciString("unref")), argument.capture());
        assertThat(argument.getValue().iterator().hasNext(), is(false));
    }

    @Test
    public void organisation_type_LIR_not_referenced_should_not_be_deleted() {
        deleteCandidates.put(new ObjectKey(ObjectType.ORGANISATION, "LIR-ORG"), new UnrefCleanup.DeleteCandidate(1, new LocalDate()));


        final RpslObject rpslObject = RpslObject.parse("organisation: LIR-ORG\norg-type: LIR");
        when(objectDao.getById(1)).thenReturn(rpslObject);

        rpslObjectsInLast.add(rpslObject);

        subject.run();

        verify(singleUpdateHandler, never()).handle(any(Origin.class), any(Keyword.class), any(Update.class), any(UpdateContext.class));

        final ArgumentCaptor<List> argument = ArgumentCaptor.forClass(List.class);
        verify(tagsDao).rebuild(eq(CIString.ciString("unref")), argument.capture());
        assertThat(argument.getValue().iterator().hasNext(), is(false));
    }

    @Test
    public void candidate_not_referenced_not_found_on_delete() {
        deleteCandidates.put(new ObjectKey(ObjectType.PERSON, "TEST-PN"), new UnrefCleanup.DeleteCandidate(1, new LocalDate()));

        when(objectDao.getById(1)).thenThrow(EmptyResultDataAccessException.class);

        subject.run();

        verifyZeroInteractions(singleUpdateHandler);

        final ArgumentCaptor<List> argument = ArgumentCaptor.forClass(List.class);
        verify(tagsDao).rebuild(eq(CIString.ciString("unref")), argument.capture());

        assertThat(argument.getValue().iterator().hasNext(), is(true));
        final Tag tag = (Tag) argument.getValue().iterator().next();
        assertThat(tag.getObjectId(), is(1));
        assertThat(tag.getType(), is(CIString.ciString("unref")));
    }

    @Test
    public void candidate_not_referenced_delete_error() {
        deleteCandidates.put(new ObjectKey(ObjectType.PERSON, "TEST-PN"), new UnrefCleanup.DeleteCandidate(1, new LocalDate()));

        final RpslObject rpslObject = RpslObject.parse("person: test\nnic-hdl: TEST-PN");
        when(objectDao.getById(1)).thenReturn(rpslObject);

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                final Update update = (Update) invocation.getArguments()[2];
                final UpdateContext updateContext = (UpdateContext) invocation.getArguments()[3];
                updateContext.addMessage(update, UpdateMessages.objectMismatch("TEST-PN"));

                return null;
            }
        }).when(singleUpdateHandler).handle(any(Origin.class), any(Keyword.class), any(Update.class), any(UpdateContext.class));

        subject.run();

        final ArgumentCaptor<List> argument = ArgumentCaptor.forClass(List.class);
        verify(tagsDao).rebuild(eq(CIString.ciString("unref")), argument.capture());

        final ArgumentCaptor<List> argument1 = ArgumentCaptor.forClass(List.class);
        verify(tagsDao).rebuild(eq(CIString.ciString("unref")), argument1.capture());

        assertThat(argument1.getValue().iterator().hasNext(), is(true));
        final Tag tag = (Tag) argument1.getValue().iterator().next();
        assertThat(tag.getObjectId(), is(1));
        assertThat(tag.getType(), is(CIString.ciString("unref")));
    }

    @Test(expected = IllegalStateException.class)
    public void too_many_errors() {
        deleteCandidates.put(new ObjectKey(ObjectType.MNTNER, "DEV-MNT"), new UnrefCleanup.DeleteCandidate(1, new LocalDate()));

        final RpslObject rpslObject = RpslObject.parse("" +
                "route: 10/8\n" +
                "origin: AS1\n" +
                StringUtils.repeat("mnt-routes: DEV-MNT {ANY}\n", 1000));
        rpslObjectsInLast.add(rpslObject);

        when(objectDao.getById(1)).thenReturn(rpslObject);

        subject.run();
    }

}
