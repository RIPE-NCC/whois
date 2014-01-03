package net.ripe.db.whois.scheduler.task.unref;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.dao.jdbc.domain.ObjectTypeIds;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.ObjectKey;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

import static net.ripe.db.whois.common.dao.jdbc.JdbcStreamingHelper.executeStreaming;

@Repository
class UnrefCleanupDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(UnrefCleanupDao.class);

    private final JdbcTemplate jdbcTemplate;
    private final DateTimeProvider dateTimeProvider;

    @Autowired
    public UnrefCleanupDao(@Qualifier("whoisSlaveDataSource") final DataSource dataSource, final DateTimeProvider dateTimeProvider) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.dateTimeProvider = dateTimeProvider;
    }

    public Map<ObjectKey, UnrefCleanup.DeleteCandidate> getDeleteCandidates(final Set<ObjectType> includeObjectTypes) {
        return executeStreaming(jdbcTemplate, "" +
                "SELECT object_type, pkey, object_id, timestamp\n" +
                "  FROM last\n" +
                "  WHERE object_type IN (" + createInClauseForObjectTypes(includeObjectTypes) + ")\n" +
                "  AND sequence_id != 0 ",
                new ResultSetExtractor<Map<ObjectKey, UnrefCleanup.DeleteCandidate>>() {
                    @Override
                    public Map<ObjectKey, UnrefCleanup.DeleteCandidate> extractData(final ResultSet rs) throws SQLException, DataAccessException {
                        final Map<ObjectKey, UnrefCleanup.DeleteCandidate> deleteCandidates = Maps.newHashMap();

                        while (rs.next()) {
                            final ObjectType objectType = ObjectTypeIds.getType(rs.getInt(1));
                            final String pkey = rs.getString(2);
                            final int objectId = rs.getInt(3);
                            final LocalDate creationDate = new LocalDate(rs.getLong(4) * 1000);

                            final ObjectKey objectKey = new ObjectKey(objectType, pkey);

                            final UnrefCleanup.DeleteCandidate previousCandidate = deleteCandidates.put(objectKey, new UnrefCleanup.DeleteCandidate(objectId, creationDate));
                            if (previousCandidate != null) {
                                throw new IllegalStateException(String.format("Key %s used in multiple objects: %d and %d", objectKey, previousCandidate.getObjectId(), objectId));
                            }
                        }

                        return deleteCandidates;
                    }
                }
        );
    }

    public void doForCurrentRpslObjects(final DeleteCandidatesFilter deleteCandidatesFilter) {
        executeStreaming(jdbcTemplate,
                "SELECT object_id, object " +
                        "FROM last " +
                        "WHERE sequence_id != 0 ",
                new RowCallbackHandler() {
                    @Override
                    public void processRow(ResultSet rs) throws SQLException {
                        deleteCandidatesFilter.filter(RpslObject.parse(rs.getInt(1), rs.getBytes(2)), dateTimeProvider.getCurrentDate());
                    }
                });
    }

    public void doForHistoricRpslObjects(final DeleteCandidatesFilter deleteCandidatesFilter, final LocalDate fromDate) {
        executeStreaming(jdbcTemplate,
                "SELECT h.object_id, h.object, l.timestamp " +
                        "FROM history h " +
                        "LEFT JOIN last l on l.object_id = h.object_id " +
                        "WHERE l.timestamp > ? ",
                new PreparedStatementSetter() {
                    @Override
                    public void setValues(final PreparedStatement ps) throws SQLException {
                        ps.setLong(1, fromDate.toDate().getTime() / 1000);
                    }
                },
                new RowCallbackHandler() {
                    @Override
                    public void processRow(final ResultSet rs) throws SQLException {
                        final int objectId = rs.getInt(1);
                        try {
                            deleteCandidatesFilter.filter(RpslObject.parse(objectId, rs.getBytes(2)), new LocalDate(rs.getLong(3) * 1000));
                        } catch (RuntimeException e) {
                            LOGGER.warn("Handling object in history with id: {}", objectId, e);
                        }
                    }
                }
        );
    }

    private static String createInClauseForObjectTypes(final Set<ObjectType> cleanupObjects) {
        return Joiner.on(',').join(Iterables.transform(cleanupObjects, new Function<ObjectType, Integer>() {
            @Override
            public Integer apply(@Nullable ObjectType objectType) {
                return ObjectTypeIds.getId(objectType);
            }
        }));
    }

    interface DeleteCandidatesFilter {
        void filter(RpslObject rpslObject, LocalDate date);
    }
}
