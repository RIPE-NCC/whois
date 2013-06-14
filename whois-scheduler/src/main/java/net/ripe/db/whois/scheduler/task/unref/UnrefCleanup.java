package net.ripe.db.whois.scheduler.task.unref;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import com.google.common.collect.*;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.TagsDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Tag;
import net.ripe.db.whois.common.domain.attrs.AttributeParseException;
import net.ripe.db.whois.common.domain.attrs.OrgType;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.scheduler.DailyScheduledTask;
import net.ripe.db.whois.scheduler.MaintenanceJob;
import net.ripe.db.whois.update.domain.*;
import net.ripe.db.whois.update.handler.SingleUpdateHandler;
import net.ripe.db.whois.update.handler.UpdateFailedException;
import net.ripe.db.whois.update.log.LoggerContext;
import net.ripe.db.whois.update.log.UpdateLog;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/* Clean up old, unreferenced, non-resource objects
Algorithm:
- collect all objects in last table, call this set 'deletion candidates'
- go through all objects in last, remove all referenced objects from deletion candidates
- go though all objects in history of last 90 days, remove all referenced objects from deletion candidates, add tags of unref cleanup
- from remaining deletion candidates, remove objects from deletion candidates that are <90 days old and tag for unref cleanup
- remaining deletion candidates are deleted via updates
 */

// TODO: [AH] hard to follow code, should be refactored
// TODO: [AH] does not handle circles of unreferenced objects (e.g. person referenced from key-cert, key-cert referenced from mntner, mntner referenced from person), need a different approach to support this
@Component
public class UnrefCleanup implements DailyScheduledTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(UnrefCleanup.class);
    private static final int MAX_DELETE_OBJECTS = 10000;
    private static final int MAX_ERRORS = 100;

    private static final int ALLOW_UNREFERENCED_DAYS = 90;
    private static final String REASON = "unreferenced object cleanup";

    private static final Set<ObjectType> CLEANUP_OBJECTS = Sets.newHashSet(
            ObjectType.IRT,
            ObjectType.KEY_CERT,
            ObjectType.MNTNER,
            ObjectType.ORGANISATION,
            ObjectType.PERSON,
            ObjectType.ROLE);

    private final UnrefCleanupDao unrefCleanupDao;
    private final RpslObjectDao objectDao;
    private final DateTimeProvider dateTimeProvider;
    private final SourceContext sourceContext;
    private final SingleUpdateHandler singleUpdateHandler;
    private final LoggerContext loggerContext;
    private final UpdateLog updateLog;
    private final TagsDao tagsDao;

    private boolean unrefCleanupEnabled;

    @Value("${unrefcleanup.enabled:false}")
    public void setUnrefCleanupEnabled(final boolean unrefCleanupEnabled) {
        LOGGER.info("Unref cleanup enabled: {}", unrefCleanupEnabled);
        this.unrefCleanupEnabled = unrefCleanupEnabled;
    }

    private boolean unrefCleanupDeletes;

    @Value("${unrefcleanup.deletes:false}")
    public void setUnrefCleanupDeletes(final boolean unrefCleanupDeletes) {
        LOGGER.info("Unref cleanup deletes: {}", unrefCleanupDeletes);
        this.unrefCleanupDeletes = unrefCleanupDeletes;
    }

    @Autowired
    public UnrefCleanup(final UnrefCleanupDao unrefCleanupDao, final RpslObjectDao objectDao, final DateTimeProvider dateTimeProvider, final SourceContext sourceContext, final SingleUpdateHandler singleUpdateHandler, final LoggerContext loggerContext, final UpdateLog updateLog, final TagsDao tagsDao) {
        this.unrefCleanupDao = unrefCleanupDao;
        this.objectDao = objectDao;
        this.dateTimeProvider = dateTimeProvider;
        this.sourceContext = sourceContext;
        this.singleUpdateHandler = singleUpdateHandler;
        this.loggerContext = loggerContext;
        this.updateLog = updateLog;
        this.tagsDao = tagsDao;
    }

    private static final Set<AttributeType> REFERENCE_ATTRIBUTETYPES = Sets.newHashSet();

    static {
        for (final AttributeType attributeType : AttributeType.values()) {
            if (!Sets.intersection(attributeType.getReferences(), CLEANUP_OBJECTS).isEmpty()) {
                REFERENCE_ATTRIBUTETYPES.add(attributeType);
            }
        }

        LOGGER.info("Reference cleanup for [{}] using [{}]",
                Joiner.on(',').join(CLEANUP_OBJECTS),
                Joiner.on(',').join(REFERENCE_ATTRIBUTETYPES));
    }

    Map<ObjectKey, DeleteCandidate> deleteCandidates;
    Map<ObjectKey, UnreferencedObject> unreferencedObjects;
    final ReentrantLock unrefCleanupLock = new ReentrantLock();

    @Override
    public void run() {
        if (!unrefCleanupEnabled) {
            LOGGER.info("Unref cleanup is not enabled");
            return;
        }

        if (!unrefCleanupLock.tryLock()) {
            throw new IllegalStateException("Unref cleanup already running");
        }

        try {
            LOGGER.info("Starting unreferenced object cleanup");
            final Stopwatch stopwatch = new Stopwatch().start();

            deleteCandidates = unrefCleanupDao.getDeleteCandidates(CLEANUP_OBJECTS);
            unreferencedObjects = Maps.newHashMap();

            filterReferencedObjects(deleteCandidates, unreferencedObjects);

            LOGGER.info("Tagging {} unreferenced objects", unreferencedObjects.size());
            tagsDao.rebuild(CIString.ciString("unref"), Lists.newArrayList(Iterables.transform(unreferencedObjects.values(), new Function<UnreferencedObject, Tag>() {
                @Nullable
                @Override
                public Tag apply(final UnreferencedObject unreferencedObject) {
                    return new Tag(CIString.ciString("unref"), unreferencedObject.getObjectId(), String.valueOf(ALLOW_UNREFERENCED_DAYS - unreferencedObject.getNrDays()));
                }
            })));

            loggerContext.init("unrefcleanup");
            try {
                sourceContext.setCurrentSourceToWhoisMaster();
                performCleanup(deleteCandidates);
            } finally {
                sourceContext.removeCurrentSource();
                loggerContext.remove();
                LOGGER.info("Unreferenced object cleanup complete in {}", stopwatch.stop());
            }
        } finally {
            unrefCleanupLock.unlock();
        }
    }

    void addUnrefObjectTag(ObjectKey objectKey, int objectId, int daysUnreferenced) {
        final UnreferencedObject unreferencedObject = unreferencedObjects.get(objectKey);
        if (unreferencedObject == null || daysUnreferenced < unreferencedObject.getNrDays()) {
            unreferencedObjects.put(objectKey, new UnreferencedObject(objectId, daysUnreferenced));
        }
    }

    private void filterReferencedObjects(final Map<ObjectKey, DeleteCandidate> deleteCandidates, final Map<ObjectKey, UnreferencedObject> nrDaysUnrefByObjectKey) {
        final UnrefCleanupDao.DeleteCandidatesFilter deleteCandidatesFilter = new UnrefCleanupDao.DeleteCandidatesFilter() {
            private int nrErrors = 0;

            @Override
            public void filter(final RpslObject rpslObject, final LocalDate date) {
                if (ObjectType.ORGANISATION.equals(rpslObject.getType())) {
                    final OrgType orgType = OrgType.getFor(rpslObject.getValueForAttribute(AttributeType.ORG_TYPE));
                    if (OrgType.LIR.equals(orgType)) {
                        LOGGER.debug("Never remove organisation of type LIR:\n\n{}\n", rpslObject);
                        deleteCandidates.remove(new ObjectKey(ObjectType.ORGANISATION, rpslObject.getKey()));
                    }
                }

                for (final RpslAttribute rpslAttribute : rpslObject.findAttributes(REFERENCE_ATTRIBUTETYPES)) {
                    try {
                        filterReferencedInAttribute(rpslObject, rpslAttribute, date);
                    } catch (RuntimeException e) {
                        if (e instanceof AttributeParseException) {
                            LOGGER.error("Processing attribute {}: {}", rpslObject.getFormattedKey(), rpslAttribute);
                        } else {
                            LOGGER.error("Processing attribute {}: {}", rpslObject.getFormattedKey(), rpslAttribute, e);
                        }

                        if (nrErrors++ > MAX_ERRORS) {
                            throw new IllegalStateException("Too many errors occured removing delete candidates, aborting unref cleanup");
                        }
                    }
                }
            }

            private void filterReferencedInAttribute(final RpslObject rpslObject, final RpslAttribute rpslAttribute, final LocalDate date) {
                for (final CIString value : rpslAttribute.getReferenceValues()) {
                    final Set<ObjectType> referenceTypes = rpslAttribute.getType().getReferences(value);
                    if (referenceTypes.contains(rpslObject.getType()) && value.equals(rpslObject.getKey())) {
                        LOGGER.debug("Skipping self reference for {} in \n\n{}\n", rpslAttribute, rpslObject);
                    } else {
                        for (final ObjectType referenceType : referenceTypes) {
                            final ObjectKey objectKey = new ObjectKey(referenceType, value);
                            DeleteCandidate deleteCandidate = deleteCandidates.remove(objectKey);

                            if (deleteCandidate != null) {
                                final int daysUnreferenced = Days.daysBetween(date, dateTimeProvider.getCurrentDate()).getDays();
                                if (daysUnreferenced > 0) {     // don't tag currently referenced objects
                                    addUnrefObjectTag(objectKey, deleteCandidate.getObjectId(), daysUnreferenced);
                                }
                            }
                        }
                    }
                }
            }
        };

        unrefCleanupDao.doForCurrentRpslObjects(deleteCandidatesFilter);
        LOGGER.info("Delete candidates after checking references in last: {}", deleteCandidates.size());

        unrefCleanupDao.doForHistoricRpslObjects(deleteCandidatesFilter, dateTimeProvider.getCurrentDate().minusDays(ALLOW_UNREFERENCED_DAYS));
        LOGGER.info("Delete candidates after checking references in history: {}", deleteCandidates.size());

        // keep unreferenced objects if they are still 'young'
        final Iterator<Map.Entry<ObjectKey, DeleteCandidate>> deleteCandidateEntryIterator = deleteCandidates.entrySet().iterator();
        while (deleteCandidateEntryIterator.hasNext()) {
            final Map.Entry<ObjectKey, DeleteCandidate> deleteCandidateEntry = deleteCandidateEntryIterator.next();
            final int daysUnreferenced = Days.daysBetween(deleteCandidateEntry.getValue().getCreationDate(), dateTimeProvider.getCurrentDate()).getDays();
            if (daysUnreferenced <= ALLOW_UNREFERENCED_DAYS) {
                addUnrefObjectTag(deleteCandidateEntry.getKey(), deleteCandidateEntry.getValue().getObjectId(), daysUnreferenced);
                deleteCandidateEntryIterator.remove();
            }
        }

        LOGGER.info("Unreferenced created over {} days ago: {}", ALLOW_UNREFERENCED_DAYS, deleteCandidates.size());
    }

    private void performCleanup(final Map<ObjectKey, DeleteCandidate> deleteCandidates) {
        final Origin origin = new MaintenanceJob(REASON);
        final UpdateContext updateContext = new UpdateContext(loggerContext);

        int remainingDeletes = MAX_DELETE_OBJECTS;
        for (final DeleteCandidate deleteCandidate : deleteCandidates.values()) {
            final int objectId = deleteCandidate.getObjectId();
            try {
                final RpslObject object = objectDao.getById(objectId);

                if (unrefCleanupDeletes) {
                    LOGGER.info("Remove: {}", object.getFormattedKey());

                    final String updateMessage = "delete: " + REASON + "\n" + object;
                    final Paragraph paragraph = new Paragraph(updateMessage);
                    final Update update = new Update(paragraph, Operation.DELETE, Lists.newArrayList(REASON), object);
                    final Stopwatch stopwatch = new Stopwatch().start();

                    try {
                        singleUpdateHandler.handle(origin, Keyword.NONE, update, updateContext);
                    } catch (UpdateFailedException e) {
                        LOGGER.warn("Unable to delete {}: {}", objectId, updateContext.getMessages(update));
                    }

                    final UpdateRequest updateRequest = new UpdateRequest(origin, Keyword.NONE, updateMessage, ImmutableList.of(update));
                    updateLog.logUpdateResult(updateRequest, updateContext, update, stopwatch.stop());

                    if (--remainingDeletes <= 0) {
                        LOGGER.info("Reached delete limit of {}, exiting", MAX_DELETE_OBJECTS);
                        break;
                    }
                }
            } catch (EmptyResultDataAccessException e) {
                LOGGER.info("Object not found: {}", objectId);
            } catch (RuntimeException e) {
                LOGGER.error("Unexpected exception deleting: {}", objectId, e);
            }
        }
    }

    @Immutable
    static class DeleteCandidate {
        private final int objectId;
        private final LocalDate creationDate;

        public DeleteCandidate(final int objectId, final LocalDate creationDate) {
            this.objectId = objectId;
            this.creationDate = creationDate;
        }

        public int getObjectId() {
            return objectId;
        }

        public LocalDate getCreationDate() {
            return creationDate;
        }

        @Override
        public String toString() {
            return "DeleteCandidate{" +
                    "objectId=" + objectId +
                    ", creationDate=" + creationDate +
                    '}';
        }
    }

    @Immutable
    static class UnreferencedObject {
        private final int objectId;
        private final int nrDays;

        public UnreferencedObject(final int objectId, final int nrDays) {
            this.objectId = objectId;
            this.nrDays = nrDays;
        }

        public int getObjectId() {
            return objectId;
        }

        public int getNrDays() {
            return nrDays;
        }
    }
}
