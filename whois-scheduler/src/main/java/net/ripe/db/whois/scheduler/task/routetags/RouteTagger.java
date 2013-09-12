package net.ripe.db.whois.scheduler.task.routetags;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.dao.TagsDao;
import net.ripe.db.whois.common.dao.jdbc.JdbcStreamingHelper;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Tag;
import net.ripe.db.whois.common.grs.AuthoritativeResource;
import net.ripe.db.whois.common.grs.AuthoritativeResourceData;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.scheduler.DailyScheduledTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
//TODO rename class to something better
public class RouteTagger implements DailyScheduledTask {
    private static final TagTemplate ALL_RIPE_RESOURCES = new TagTemplate(CIString.ciString("all-ripe-resources"), "Both asnumber and prefix from RIPE");
    private static final TagTemplate RIPE_PREFIX_ONLY = new TagTemplate(CIString.ciString("ripe-prefix-only"), "RIPE prefix only");
    private static final TagTemplate RIPE_ASN_ONLY = new TagTemplate(CIString.ciString("ripe-asn-only"), "RIPE asnumber only");
    private static final TagTemplate NON_RIPE_RESOURCES = new TagTemplate(CIString.ciString("non-ripe-resources"), "Neither asnumber nor prefix from RIPE");
    private final AuthoritativeResourceData authoritativeResourceData;
    private final TagsDao tagsDao;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    //TODO slave or master datasource?
    public RouteTagger(final AuthoritativeResourceData authoritativeResourceData, final TagsDao tagsDao, @Qualifier("whoisSlaveDataSource") final DataSource dataSource) {
        this.authoritativeResourceData = authoritativeResourceData;
        this.tagsDao = tagsDao;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public void run() {
        tagsDao.deleteTagsOfType(ALL_RIPE_RESOURCES.getType());
        tagsDao.deleteTagsOfType(RIPE_PREFIX_ONLY.getType());
        tagsDao.deleteTagsOfType(RIPE_ASN_ONLY.getType());
        tagsDao.deleteTagsOfType(NON_RIPE_RESOURCES.getType());
        final AuthoritativeResource authoritativeResource = authoritativeResourceData.getAuthoritativeResource(CIString.ciString("RIPE"));

        final List<Tag> tags = Lists.newArrayList();
        JdbcStreamingHelper.executeStreaming(
                jdbcTemplate,
                "",                                    //TODO do we need to query for every route(6) or is there a shortcut?
                new RowCallbackHandler() {
                    @Override
                    public void processRow(final ResultSet rs) throws SQLException {
                        addTagForObject(RpslObject.parse(rs.getInt(1), rs.getBytes(2)), authoritativeResource, tags);
                    }
                });
    }

    private void addTagForObject(final RpslObject object, final AuthoritativeResource authoritativeResource, final List<Tag> tags) {
        final CIString origin = object.getValueForAttribute(AttributeType.ORIGIN);
        final boolean asMaintainedbyRipe = authoritativeResource.isMaintainedByRir(ObjectType.AUT_NUM, origin);
        final boolean prefixMaintainedbyRipe = authoritativeResource.isMaintainedInRirSpace(object.getType() == ObjectType.ROUTE ? ObjectType.INETNUM : ObjectType.INET6NUM, object.getKey());

        if (asMaintainedbyRipe && prefixMaintainedbyRipe) {
            tags.add(new Tag(ALL_RIPE_RESOURCES.getType(), object.getObjectId(), ALL_RIPE_RESOURCES.getValue()));
        } else if (asMaintainedbyRipe) {
            tags.add(new Tag(RIPE_ASN_ONLY.getType(), object.getObjectId(), RIPE_ASN_ONLY.getValue()));
        } else if (prefixMaintainedbyRipe) {
            tags.add(new Tag(RIPE_PREFIX_ONLY.getType(), object.getObjectId(), RIPE_PREFIX_ONLY.getValue()));
        } else {
            tags.add(new Tag(NON_RIPE_RESOURCES.getType(), object.getObjectId(), NON_RIPE_RESOURCES.getValue()));
        }
    }

    private static class TagTemplate {
        private CIString type;
        private String explanation;

        TagTemplate(CIString type, String explanation) {
            this.type = type;
            this.explanation = explanation;
        }

        CIString getType() {
            return type;
        }

        String getValue() {
            return explanation;
        }
    }
}
