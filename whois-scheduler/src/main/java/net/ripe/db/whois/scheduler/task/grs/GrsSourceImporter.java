package net.ripe.db.whois.scheduler.task.grs;

import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.grs.AuthoritativeResource;
import net.ripe.db.whois.common.rpsl.*;
import net.ripe.db.whois.common.source.SourceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    private final ResourceTagger resourceTagger;
    private final SourceContext sourceContext;

    private Path downloadDir;

    @Autowired
    public GrsSourceImporter(
            @Value("${dir.grs.import.download}") final String downloadDir,
            final AttributeSanitizer sanitizer,
            final ResourceTagger resourceTagger,
            final SourceContext sourceContext) {
        this.sourceContext = sourceContext;
        this.downloadDir = Paths.get(downloadDir);
        this.sanitizer = sanitizer;
        this.resourceTagger = resourceTagger;

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

        resourceTagger.tagObjects(grsSource);
    }

    private void acquireAndUpdateGrsData(final GrsSource grsSource, final boolean rebuild, final AuthoritativeResource authoritativeData) {
        final Logger logger = grsSource.getLogger();

        new Runnable() {
            private int nrCreated;
            private int nrUpdated;
            private int nrDeleted;
            private int nrIgnored;

            private Set<Integer> currentObjectIds;
            private Set<Integer> incompletelyIndexedObjectIds = Sets.newHashSet();

            @Override
            public void run() {
                final Path dump = downloadDir.resolve(String.format("%s-DMP", grsSource.getName().toUpperCase()));

                try {
                    grsSource.acquireDump(dump);
                } catch (IOException e) {
                    throw new RuntimeException("Unable to acquire dump", e);
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
                    // TODO: continue from here to switch to Path
                    importObjects(dump.toFile());
                    deleteNotFoundInImport();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    logger.info("created {} / updated {} / deleted {} / ignored {} in {}", nrCreated, nrUpdated, nrDeleted, nrIgnored, stopwatch.stop());
                }

                updateIndexes();
            }

            private void importObjects(final File dumpFile) throws IOException {
                grsSource.handleObjects(dumpFile, new ObjectHandler() {
                    @Override
                    public void handle(final List<String> lines) {
                        final String rpslObjectString = LINE_JOINER.join(lines);

                        final RpslObject rpslObject;
                        try {
                            rpslObject = RpslObject.parse(rpslObjectString);
                        } catch (RuntimeException e) {
                            logger.warn("Unable to parse input as object:\n\n{}\n", rpslObjectString);
                            return;
                        }

                        handle(rpslObject);
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
                                logger.debug("Errors for object with key {}: {}", typeAttribute, messages);
                                nrIgnored++;
                            } else if (authoritativeData.isMaintainedInRirSpace(cleanObject)) {
                                createOrUpdate(cleanObject);
                            }
                        }
                    }

                    private RpslObject filterObject(final RpslObject rpslObject) {
                        final RpslAttribute sourceAttribute = new RpslAttribute(AttributeType.SOURCE, grsSource.getName().toUpperCase());

                        final ObjectTemplate objectTemplate = ObjectTemplate.getTemplate(rpslObject.getType());
                        final Set<AttributeType> attributeTypes = objectTemplate.getAllAttributes();

                        final List<RpslAttribute> attributes = rpslObject.getAttributes();
                        final List<RpslAttribute> newAttributes = Lists.newArrayListWithExpectedSize(attributes.size());
                        final Set<AttributeType> newAttributeTypes = Sets.newHashSet();

                        for (final RpslAttribute attribute : attributes) {
                            final AttributeType attributeType = attribute.getType();
                            if (attributeType == null || !attributeTypes.contains(attributeType)) {
                                logger.debug("Ignoring attribute in object {}: {}", rpslObject.getFormattedKey(), attribute);
                                continue;
                            }

                            newAttributeTypes.add(attributeType);
                            if (attributeType.equals(AttributeType.SOURCE)) {
                                newAttributes.add(sourceAttribute);
                            } else {
                                newAttributes.add(attribute);
                            }
                        }

                        if (!newAttributeTypes.contains(AttributeType.SOURCE)) {
                            newAttributes.add(sourceAttribute);
                        }

                        return new RpslObject(newAttributes);
                    }

                    @Transactional
                    private void createOrUpdate(final RpslObject importedObject) {
                        final String pkey = importedObject.getKey().toString();
                        final ObjectType type = importedObject.getType();
                        final GrsObjectInfo grsObjectInfo = grsSource.getDao().find(pkey, type);

                        if (grsObjectInfo == null) {
                            if (type == ObjectType.PERSON && grsSource.getDao().find(pkey, ObjectType.ROLE) != null) {
                                return;
                            }

                            if (type == ObjectType.ROLE && grsSource.getDao().find(pkey, ObjectType.PERSON) != null) {
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
                });
            }

            private void deleteNotFoundInImport() {
                if (nrCreated == 0 && nrUpdated == 0) {
                    logger.warn("Skipping deletion since there were no other updates");
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
            }

            private void updateIndexes() {
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
            }
        }.run();
    }
}
