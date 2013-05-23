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
import net.ripe.db.whois.common.rpsl.ObjectType;
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

    void tagObjects(final GrsSource grsSource, final AuthoritativeResource authoritativeResource) {
        final Stopwatch stopwatch = new Stopwatch().start();
        try {
            sourceContext.setCurrent(Source.master(grsSource.getSource()));
            tagObjectsInContext(grsSource, authoritativeResource);
        } finally {
            sourceContext.removeCurrentSource();
            grsSource.getLogger().info("Tagging objects complete in {}", stopwatch.stop());
        }
    }

    private void tagObjectsInContext(final GrsSource grsSource, final AuthoritativeResource authoritativeResource) {
        final CIString tagType = ciString(String.format("%s_RESOURCE", grsSource.getSource().toUpperCase().replace("-GRS", "")));
        final List<Integer> deletes = Lists.newArrayList();
        final List<Tag> creates = Lists.newArrayList();

        executeStreaming(
                sourceContext.getCurrentSourceConfiguration().getJdbcTemplate(),
                String.format("" +
                        "SELECT object_id, object_type, pkey " +
                        "FROM last " +
                        "WHERE object_type in (%s) " +
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
                                creates.add(new Tag(tagType, objectId, "Registry maintained"));
                            } else if (authoritativeResource.isMaintainedInRirSpace(objectType, pkey)) {
                                creates.add(new Tag(tagType, objectId, "User maintained"));
                            }

                            if (creates.size() > BATCH_SIZE) {
                                tagsDao.updateTags(tagType, deletes, creates);
                                deletes.clear();
                                creates.clear();
                            }

                        } catch (RuntimeException e) {
                            grsSource.getLogger().error("Unexpected", e);
                        }
                    }
                });

        tagsDao.updateTags(tagType, deletes, creates);

        // TODO [AK] Delete all tags that still exists but where no RPSL Object exists in last
    }
}
