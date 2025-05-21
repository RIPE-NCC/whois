package net.ripe.db.whois.scheduler.task.grs;

import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.grs.AuthoritativeResource;
import net.ripe.db.whois.common.rpsl.AttributeSanitizer;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectMessages;
import net.ripe.db.whois.common.rpsl.ObjectTemplate;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.common.rpsl.transform.FilterChangedFunction;
import net.ripe.db.whois.common.source.SourceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
class GrsSourceImporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(GrsSourceImporter.class);

    private static final Joiner LINE_JOINER = Joiner.on("");
    private static final int LOG_EVERY_NR_HANDLED = 100000;

    private final AttributeSanitizer sanitizer;
    private final SourceContext sourceContext;

    private final Path downloadDir;

    private static final FilterChangedFunction FILTER_CHANGED_FUNCTION = new FilterChangedFunction();

    @Autowired
    public GrsSourceImporter(
            @Value("${dir.grs.import.download}") final String downloadDir,
            final AttributeSanitizer sanitizer,
            final SourceContext sourceContext) {
        this.sourceContext = sourceContext;
        this.downloadDir = Paths.get(downloadDir);
        this.sanitizer = sanitizer;

        try {
            Files.createDirectories(this.downloadDir);
        } catch (IOException e) {
            LOGGER.warn("Create directory structure", e);
        }
    }

    void grsImport(final GrsSource grsSource, final boolean rebuild) {
        final AuthoritativeResource authoritativeResource = grsSource.getAuthoritativeResource();

        if (sourceContext.isVirtual(grsSource.getName())) {
            grsSource.getLogger().info("Not updating GRS data");
        } else {
            acquireAndUpdateGrsData(grsSource, rebuild, authoritativeResource);
        }
    }

    private void acquireAndUpdateGrsData(final GrsSource grsSource, final boolean rebuild, final AuthoritativeResource authoritativeData) {
        new GrsSourceImporterWorker(grsSource, rebuild, authoritativeData).run();
    }

    private class GrsSourceImporterWorker implements Runnable {

        private int nrCreated;
        private int nrUpdated;
        private int nrDeleted;
        private int nrIgnored;

        private Set<Integer> currentObjectIds;
        private final Set<Integer> incompletelyIndexedObjectIds = Sets.newHashSet();
        private final GrsSource grsSource;
        private final boolean rebuild;
        private final Logger logger;
        private final AuthoritativeResource authoritativeData;
        private final RpslAttribute sourceAttribute;

        public GrsSourceImporterWorker(final GrsSource grsSource, final boolean rebuild, final AuthoritativeResource authoritativeResource) {
            this.grsSource = grsSource;
            this.logger = grsSource.getLogger();
            this.rebuild = rebuild;
            this.authoritativeData = authoritativeResource;
            this.sourceAttribute = new RpslAttribute(AttributeType.SOURCE, grsSource.getName().toUpperCase());
        }

        @Override
        public void run() {
            final Path dump = downloadDir.resolve(String.format("%s-DMP", grsSource.getName().toUpperCase()));
            try {
                grsSource.acquireDump(dump);
            } catch (IOException e) {
                logger.error(e.getClass().getName(), e);
                throw new RuntimeException("Unable to acquire GRS dump", e);
            }

            final Path irrDump = downloadDir.resolve(String.format("%s-IRR-DMP", grsSource.getName().toUpperCase()));
            try {
                grsSource.acquireIrrDump(irrDump);
            } catch (IOException e) {
                logger.error(e.getClass().getName(), e);
                throw new RuntimeException("Unable to acquire IRR dump", e);
            }

            final Stopwatch stopwatch = Stopwatch.createStarted();

            if (rebuild) {
                grsSource.getDao().cleanDatabase();
                currentObjectIds = Collections.emptySet();
                logger.info("Rebuilding database");
            } else {
                currentObjectIds = Sets.newHashSet(grsSource.getDao().getCurrentObjectIds());
                logger.info("Updating {} current objects in database", currentObjectIds.size());
            }

            try {
                importObjects(dump.toFile());
                importIrrObjects(irrDump.toFile());
                deleteNotFoundInImport();
            } catch (IOException e) {
                logger.error(e.getClass().getName(), e);
                throw new RuntimeException(e);
            } finally {
                logger.info("created {} / updated {} / deleted {} / ignored {} in {}", nrCreated, nrUpdated, nrDeleted, nrIgnored, stopwatch.stop());
            }

            updateIndexes();
        }

        private void importIrrObjects(final File irrDumpFile) throws IOException {
            grsSource.getDao().transactionTemplate().execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(final TransactionStatus status) {
                    logger.info("importIrrObjects START");
                    if (!irrDumpFile.exists()) {
                        return;
                    }
                    try {
                        grsSource.handleIrrObjects(irrDumpFile, new GrsSourceObjectHandler());
                    } catch (IOException e) {
                        logger.error(e.getClass().getName(), e);
                        throw new IllegalStateException(e);
                    }
                    logger.info("importIrrObjects END");
                }
            });
        }

        private void importObjects(final File dumpFile) throws IOException {
            grsSource.getDao().transactionTemplate().execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(final TransactionStatus status) {
                    logger.info("importObjects START");
                    try {
                        grsSource.handleObjects(dumpFile, new GrsSourceObjectHandler());
                    } catch (IOException e) {
                        logger.error(e.getClass().getName(), e);
                        throw new IllegalStateException(e);
                    }
                    logger.info("importObjects END");
                }
            });
        }

        private void deleteNotFoundInImport() {
            grsSource.getDao().transactionTemplate().execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(final TransactionStatus status) {
                    logger.info("deleteNotFoundInImport START");

                    if (nrCreated == 0 && nrUpdated == 0) {
                        logger.info("Skipping deletion since there were no other updates");
                        return;
                    }

                    logger.info("Cleaning up {} currently unreferenced objects", currentObjectIds.size());
                    for (final Integer objectId : currentObjectIds) {
                        try {
                            grsSource.getDao().deleteObject(objectId);
                            nrDeleted++;
                        } catch (RuntimeException e) {
                            logger.error("Deleting object with id: {}", objectId, e);
                        }
                    }
                    logger.info("deleteNotFoundInImport END");
                }
            });
        }

        private void updateIndexes() {
            grsSource.getDao().transactionTemplate().execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(final TransactionStatus status) {
                    logger.info("updateIndexes START");

                    logger.info("Updating indexes for {} changed objects with missing references", incompletelyIndexedObjectIds.size());

                    int nrUpdated = 0;

                    for (final Integer objectId : incompletelyIndexedObjectIds) {
                        try {
                            grsSource.getDao().updateIndexes(objectId);
                        } catch (RuntimeException e) {
                            logger.error("Updating index for object with id: {}", objectId, e);
                        }

                        nrUpdated++;
                        if (nrUpdated % LOG_EVERY_NR_HANDLED == 0) {
                            logger.info("Updated {} indexes", nrUpdated);
                        }
                    }

                    logger.info("updateIndexes END");
                }
            });
        }

        private class GrsSourceObjectHandler implements ObjectHandler {
            @Override
            public void handle(final List<String> lines) {
                final String rpslObjectString = LINE_JOINER.join(lines);

                final RpslObject rpslObject;
                try {
                    rpslObject = RpslObject.parse(rpslObjectString);
                } catch (RuntimeException e) {
                    logger.info("Unable to parse input as object: {}\n\n{}\n", e.getMessage(), rpslObjectString);
                    return;
                }

                handle(FILTER_CHANGED_FUNCTION.apply(rpslObject));
            }

            @Override
            public void handle(final RpslObject rpslObject) {
                if (rpslObject.getType() == null) {
                    logger.debug("Unknown type: \n\n{}\n", rpslObject);
                    nrIgnored++;
                } else {
                    final ObjectMessages messages = new ObjectMessages();
                    final RpslObject filteredObject = filterObject(rpslObject);
                    final RpslObject cleanObject = sanitizer.sanitize(filteredObject, messages);
                    final RpslAttribute typeAttribute = cleanObject.getTypeAttribute();
                    typeAttribute.validateSyntax(cleanObject.getType(), messages);
                    if (messages.hasErrors()) {
                        logger.info("Errors for object with key {}: {}", typeAttribute, messages);
                        nrIgnored++;
                    } else if (authoritativeData.isMaintainedInRirSpace(cleanObject)) {
                        // TODO: interaction with authoritative resource data (must update that first!)
                        createOrUpdate(cleanObject);
                    }
                }
            }

            private RpslObject filterObject(final RpslObject rpslObject) {
                final ObjectTemplate objectTemplate = ObjectTemplate.getTemplate(rpslObject.getType());

                final RpslObjectBuilder builder = new RpslObjectBuilder(rpslObject);

                for (int i = 0; i < builder.size(); i++) {
                    final RpslAttribute rpslAttribute = builder.get(i);
                    final AttributeType attributeType = rpslAttribute.getType();

                    if (attributeType == null || !objectTemplate.hasAttribute(attributeType)) {
                        logger.debug("Ignoring attribute in object {}: {}", rpslObject.getFormattedKey(), rpslAttribute);
                        builder.remove(i--);

                    } else  if (attributeType.equals(AttributeType.SOURCE)) {
                        builder.remove(i--);
                    }
                }

                // best not to sort to avoid reordering remarks: attributes
                builder.append(sourceAttribute);

                return builder.get();
            }

            private void createOrUpdate(final RpslObject importedObject) {
                final String pkey = importedObject.getKey().toString();
                final ObjectType type = importedObject.getType();
                final GrsObjectInfo grsObjectInfo = grsSource.getDao().find(pkey, type);

                if (grsObjectInfo == null) {
                    if (type == ObjectType.PERSON && grsSource.getDao().find(pkey, ObjectType.ROLE) != null) {
                        logger.info("Errors for object with key {}: There is already an existing ROLE object with same pkey", pkey);
                        return;
                    }

                    if (type == ObjectType.ROLE && grsSource.getDao().find(pkey, ObjectType.PERSON) != null) {
                        logger.info("Errors for object with key {}: There is already an existing PERSON object with same pkey", pkey);
                        return;
                    }

                    create(importedObject);
                } else {
                    currentObjectIds.remove(grsObjectInfo.getObjectId());
                    if (!grsObjectInfo.getRpslObject().equals(importedObject)) {
                        update(importedObject, grsObjectInfo);
                    }
                }

                final int nrImported = nrCreated + nrUpdated;
                if ((nrImported % LOG_EVERY_NR_HANDLED == 0) && (nrImported > 0)) {
                    logger.info("Imported {} objects", nrImported);
                }
            }

            private void create(final RpslObject importedObject) {
                final GrsDao.UpdateResult updateResult = grsSource.getDao().createObject(importedObject);
                if (updateResult.hasMissingReferences()) {
                    incompletelyIndexedObjectIds.add(updateResult.getObjectId());
                }
                nrCreated++;
            }

            private void update(final RpslObject importedObject, final GrsObjectInfo grsObjectInfo) {
                final GrsDao.UpdateResult updateResult = grsSource.getDao().updateObject(grsObjectInfo, importedObject);
                if (updateResult.hasMissingReferences()) {
                    incompletelyIndexedObjectIds.add(updateResult.getObjectId());
                }
                nrUpdated++;
            }
        }

    }

}
