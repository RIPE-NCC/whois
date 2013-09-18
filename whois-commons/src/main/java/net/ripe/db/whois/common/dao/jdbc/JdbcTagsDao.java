package net.ripe.db.whois.common.dao.jdbc;

import net.ripe.db.whois.common.aspects.RetryFor;
import net.ripe.db.whois.common.dao.TagsDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
@RetryFor(RecoverableDataAccessException.class)
public class JdbcTagsDao implements TagsDao {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcTagsDao(@Qualifier("sourceAwareDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public List<Tag> getTags(final Integer objectId) {
        return jdbcTemplate.query("" +
                "SELECT object_id, tag_id, data " +
                "FROM tags " +
                "WHERE object_id = ?",
                new TagRowMapper(),
                objectId);
    }

    @Override
    public List<Tag> getTagsOfType(final CIString type) {
        return jdbcTemplate.query("" +
                "SELECT object_id, tag_id, data " +
                "FROM tags " +
                "WHERE tag_id = ?",
                new TagRowMapper(),
                type.toString());
    }

    @Override
    public void createTag(final Tag tag) {
        jdbcTemplate.update("INSERT INTO tags(object_id, tag_id, data) VALUES(?, ?, ?)",
                tag.getObjectId(),
                tag.getType().toString(),
                tag.getValue());
    }

    @Override
    public void createTags(final List<Tag> tags) {
        jdbcTemplate.batchUpdate("INSERT INTO tags(object_id, tag_id, data) VALUES(?, ?, ?)", new BatchPreparedStatementSetter() {
            @Override
            public void setValues(final PreparedStatement ps, final int i) throws SQLException {
                final Tag tag = tags.get(i);
                ps.setInt(1, tag.getObjectId());
                ps.setString(2, tag.getType().toString());
                ps.setString(3, tag.getValue());
            }

            @Override
            public int getBatchSize() {
                return tags.size();
            }
        });
    }

    @Override
    public void deleteTag(final CIString type, final Integer objectId) {
        jdbcTemplate.update("DELETE FROM tags WHERE tag_id = ? AND object_id = ?", type.toString(), objectId);
    }

    @Override
    public void deleteTags(final CIString type, final List<Integer> objectIds) {
        jdbcTemplate.batchUpdate("DELETE FROM tags WHERE tag_id = ? AND object_id = ?", new BatchPreparedStatementSetter() {
            @Override
            public void setValues(final PreparedStatement ps, final int i) throws SQLException {
                ps.setString(1, type.toString());
                ps.setInt(2, objectIds.get(i));
            }

            @Override
            public int getBatchSize() {
                return objectIds.size();
            }
        });
    }

    @Override
    public void deleteOrphanedTags() {
        jdbcTemplate.update("DELETE tags FROM tags LEFT OUTER JOIN last ON last.object_id = tags.object_id WHERE last.sequence_id = 0");
    }

    @Override
    public void deleteTagsOfType(CIString type) {
        jdbcTemplate.update("DELETE FROM tags WHERE tag_id = ?", type.toString());
    }

    @Override
    @Transactional
    public void rebuild(final CIString type, final List<Tag> tags) {
        deleteTagsOfType(type);
        createTags(tags);
    }

    @Override
    @Transactional
    public void updateTags(final Iterable<CIString> tagTypes, final List<Integer> deletes, final List<Tag> creates) {
        for (final CIString tagType : tagTypes) {
            deleteTags(tagType, deletes);
        }

        createTags(creates);
    }

    private static class TagRowMapper implements RowMapper<Tag> {
        @Override
        public Tag mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            final int objectId = rs.getInt(1);
            final CIString type = CIString.ciString(rs.getString(2));
            final String data = rs.getString(3);
            return new Tag(type, objectId, data);
        }
    }
}
