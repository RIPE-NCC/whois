package net.ripe.db.whois.update.dao;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.dao.jdbc.domain.ObjectTypeIds;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObjectBase;
import net.ripe.db.whois.update.domain.PendingUpdate;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class JdbcPendingUpdateDao implements PendingUpdateDao {
    private final JdbcTemplate jdbcTemplate;
    private final Splitter SPLITTER = Splitter.on(",");
    private final Joiner JOINER = Joiner.on(",");


    @Autowired
    public JdbcPendingUpdateDao(@Qualifier("deferredUpdateDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public List<PendingUpdate> findByTypeAndKey(final ObjectType type, final String key) {
        return jdbcTemplate.query("SELECT id, passed_authentications, object, stored_date FROM pending_updates WHERE object_type = ? AND pkey = ? ORDER BY stored_date ASC",
                new RowMapper<PendingUpdate>() {
            @Override
            public PendingUpdate mapRow(final ResultSet rs, final int rowNum) throws SQLException {

                return new PendingUpdate(
                        rs.getInt("id"),
                        Sets.newHashSet(SPLITTER.split(rs.getString("passed_authentications"))),
                        RpslObjectBase.parse(rs.getString("object")),
                        new LocalDateTime(rs.getDate("stored_date"))
                );
            }
        }, ObjectTypeIds.getId(type), key);
    }

    @Override
    public void store(final PendingUpdate pendingUpdate) {
        jdbcTemplate.update("" +
                "INSERT INTO pending_updates(object, object_type, pkey, stored_date, passed_authentications) " +
                "VALUES (?, ?, ?, ?, ?)",
                pendingUpdate.getObject().toString(),
                ObjectTypeIds.getId(pendingUpdate.getObject().getType()),
                pendingUpdate.getObject().getKey(),
                pendingUpdate.getStoredDate().toDate(),
                JOINER.join(pendingUpdate.getPassedAuthentications()));
    }

    @Override
    public void remove(final PendingUpdate pendingUpdate) {
        jdbcTemplate.update("DELETE FROM pending_updates WHERE id = ?",
                pendingUpdate.getId());
    }

    @Override
    public void removePendingUpdatesBefore(final LocalDateTime date) {
        jdbcTemplate.update("DELETE FROM pending_updates WHERE stored_date < ?", date.toDate());
    }
}
