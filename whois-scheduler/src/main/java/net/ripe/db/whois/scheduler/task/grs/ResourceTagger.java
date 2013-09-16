package net.ripe.db.whois.scheduler.task.grs;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.dao.TagsDao;
import net.ripe.db.whois.common.dao.jdbc.domain.ObjectTypeIds;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Tag;
import net.ripe.db.whois.common.grs.AuthoritativeResource;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.source.SourceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static net.ripe.db.whois.common.dao.jdbc.JdbcStreamingHelper.executeStreaming;
import static net.ripe.db.whois.common.domain.CIString.ciString;

@Component
class ResourceTagger {
    private static final int BATCH_SIZE = 1000;

    private final SourceContext sourceContext;
    private final TagsDao tagsDao;

    @Autowired
    ResourceTagger(final SourceContext sourceContext, final TagsDao tagsDao) {
        this.sourceContext = sourceContext;
        this.tagsDao = tagsDao;
    }

    void tagObjects(final GrsSource grsSource) {
        final Stopwatch stopwatch = new Stopwatch().start();
        try {
            sourceContext.setCurrent(Source.master(grsSource.getName()));
            tagObjectsInContext(grsSource);

            if (sourceContext.isTagRoutes()) {
                tagRouteObjectsInContext(grsSource);
            }

        } finally {
            sourceContext.removeCurrentSource();
            grsSource.getLogger().info("Tagging objects complete in {}", stopwatch.stop());
        }
    }

    private void tagObjectsInContext(final GrsSource grsSource) {
        final AuthoritativeResource authoritativeResource = grsSource.getAuthoritativeResource();
        final String rirName = grsSource.getName().toUpperCase().replace("-GRS", "");
        final CIString registryTagType = ciString(String.format("%s-REGISTRY-RESOURCE", rirName));
        final CIString userTagType = ciString(String.format("%s-USER-RESOURCE", rirName));
        final List<Integer> deletes = Lists.newArrayList();
        final List<Tag> creates = Lists.newArrayList();
        final List<CIString> tagTypes = Lists.newArrayList(registryTagType, userTagType);

        executeStreaming(
                sourceContext.getCurrentSourceConfiguration().getJdbcTemplate(),
                String.format("" +
                        "SELECT object_id, object_type, pkey " +
                        "FROM last " +
                        "WHERE object_type IN (%s) " +
                        "AND sequence_id != 0 ",
                        Joiner.on(',').join(Iterables.transform(authoritativeResource.getResourceTypes(), new Function<ObjectType, Integer>() {
                            @Nullable
                            @Override
                            public Integer apply(final ObjectType input) {
                                return ObjectTypeIds.getId(input);
                            }
                        }))),
                new RowCallbackHandler() {

                    @Override
                    public void processRow(final ResultSet rs) throws SQLException {
                        try {
                            final int objectId = rs.getInt(1);
                            final ObjectType objectType = ObjectTypeIds.getType(rs.getInt(2));
                            final CIString pkey = ciString(rs.getString(3));

                            deletes.add(objectId);

                            if (authoritativeResource.isMaintainedByRir(objectType, pkey)) {
                                creates.add(new Tag(registryTagType, objectId));
                            } else if (authoritativeResource.isMaintainedInRirSpace(objectType, pkey)) {
                                creates.add(new Tag(userTagType, objectId));
                            }

                            if (creates.size() > BATCH_SIZE) {
                                updateTags(tagTypes, deletes, creates);
                            }

                        } catch (RuntimeException e) {
                            grsSource.getLogger().error("Unexpected", e);
                        }
                    }
                });

        updateTags(tagTypes, deletes, creates);

        tagsDao.deleteOrphanedTags();
    }

    private void tagRouteObjectsInContext(final GrsSource grsSource) {
        final AuthoritativeResource authoritativeResource = grsSource.getAuthoritativeResource();
        final String rirName = grsSource.getName().toUpperCase().replace("-GRS", "");
        final CIString asnOnlyTagType = ciString(String.format("%s-ASN-ONLY-RESOURCE", rirName));
        final CIString prefixOnlyTagType = ciString(String.format("%s-PREFIX-ONLY-RESOURCE", rirName));
        final CIString asnAndPrefixTagTag = ciString(String.format("%s-ASN-AND-PREFIX-RESOURCE", rirName));

        final List<Integer> deletes = Lists.newArrayList();
        final List<Tag> creates = Lists.newArrayList();
        final List<CIString> tagTypes = Lists.newArrayList(asnOnlyTagType, prefixOnlyTagType, asnAndPrefixTagTag);

        executeStreaming(
                sourceContext.getCurrentSourceConfiguration().getJdbcTemplate(),
                String.format("" +
                        "SELECT object_id, object " +
                        "FROM last " +
                        "WHERE object_type IN (%d, %d) " +
                        "AND sequence_id != 0",
                ObjectTypeIds.getId(ObjectType.ROUTE), ObjectTypeIds.getId(ObjectType.ROUTE6)),
                new RowCallbackHandler() {
                    @Override
                    public void processRow(ResultSet rs) throws SQLException {
                        try {
                            final RpslObject object = RpslObject.parse(rs.getInt(1), rs.getBytes(2));

                            final boolean autnumMaintainedByRir = isAutnumMaintainedByRir(object);
                            final boolean prefixMaintainedByRir = isRouteMaintainedInRirSpace(object);

                            if (autnumMaintainedByRir) {
                                if (prefixMaintainedByRir) {
                                    creates.add(new Tag(asnAndPrefixTagTag, object.getObjectId()));
                                } else {
                                    creates.add(new Tag(asnOnlyTagType, object.getObjectId()));
                                }
                            } else {
                                if (prefixMaintainedByRir) {
                                    creates.add(new Tag(prefixOnlyTagType, object.getObjectId()));
                                }
                            }

                            if (creates.size() > BATCH_SIZE) {
                                updateTags(tagTypes, deletes, creates);
                            }

                        } catch (RuntimeException e) {
                            grsSource.getLogger().error("Unexpected", e);
                        }
                    }

                    private boolean isAutnumMaintainedByRir(final RpslObject object) {
                        return authoritativeResource.isMaintainedByRir(ObjectType.AUT_NUM, object.getValueForAttribute(AttributeType.ORIGIN));
                    }

                    private boolean isRouteMaintainedInRirSpace(final RpslObject object) {
                        switch (object.getType()) {
                            case ROUTE:
                                return authoritativeResource.isMaintainedInRirSpace(ObjectType.INETNUM, object.getValueForAttribute(AttributeType.ROUTE));
                            case ROUTE6:
                                return authoritativeResource.isMaintainedInRirSpace(ObjectType.INET6NUM, object.getValueForAttribute(AttributeType.ROUTE6));
                            default:
                                throw new IllegalArgumentException("Unhandled type " + object.getType());
                        }
                    }
                }
        );

        updateTags(tagTypes, deletes, creates);
    }

    private void updateTags(final Iterable<CIString> tagTypes, final List<Integer> deletes, final List<Tag> creates) {
        tagsDao.updateTags(tagTypes, deletes, creates);
        deletes.clear();
        creates.clear();
    }
}
