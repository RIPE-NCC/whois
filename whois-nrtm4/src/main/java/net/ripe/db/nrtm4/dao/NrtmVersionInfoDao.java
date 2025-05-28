package net.ripe.db.nrtm4.dao;

import net.ripe.db.nrtm4.domain.NrtmDocumentType;
import net.ripe.db.nrtm4.domain.NrtmSource;
import net.ripe.db.nrtm4.domain.NrtmVersionInfo;
import net.ripe.db.whois.common.domain.CIString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;


@Repository
public class NrtmVersionInfoDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(NrtmVersionInfoDao.class);
    static final Function<Integer, RowMapper<NrtmVersionInfo>> rowMapperWithOffset = (offset) -> (rs, rowNum) -> {
        final NrtmSource source = new NrtmSource(rs.getLong(offset + 2), CIString.ciString(rs.getString(offset + 3)));
        return new NrtmVersionInfo(
            rs.getLong(offset + 1),
            source,
            rs.getLong(offset + 4),
            rs.getString(offset + 5),
            NrtmDocumentType.valueOf(rs.getString(offset + 6)),
            rs.getInt(offset + 7),
            rs.getInt(offset + 8)
        );
    };
    private final RowMapper<NrtmVersionInfo> rowMapper = rowMapperWithOffset.apply(0);
    private final JdbcTemplate jdbcTemplate;

    public NrtmVersionInfoDao(@Qualifier("nrtmMasterDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public List<NrtmVersionInfo> findLastVersionPerSource() {
        try {
            return jdbcTemplate.query("""
                   SELECT vio.id, src.id, src.name, vio.version, vio.session_id, vio.type, vio.last_serial_id, vio.created
                   FROM version_info vio
                   JOIN source src ON src.id = vio.source_id
                   WHERE vio.id IN (
                       SELECT max(vi.id)
                       FROM version_info vi
                       JOIN (SELECT source_id, MAX(version) version FROM version_info GROUP BY source_id) maxv
                       ON vi.version = maxv.version AND vi.source_id = maxv.source_id
                       WHERE vi.source_id = maxv.source_id AND vi.version = maxv.version
                       GROUP BY vi.source_id
                      )
                   ORDER BY vio.last_serial_id DESC
                    """,
                rowMapper);
        } catch (final EmptyResultDataAccessException ex) {
            LOGGER.debug("findLastVersions found no entries");
            return List.of();
        }
    }

    public Optional<NrtmVersionInfo> findLastVersion(final NrtmSource source) {
       return findLastVersionPerSource().stream().filter( (versionInfo) ->  versionInfo.source().getName().equals(source.getName())).findFirst();
    }

    public NrtmVersionInfo findById(final long versionId) {
        final String sql = """
            SELECT v.id, src.id, src.name, v.version, v.session_id, v.type, v.last_serial_id, v.created
            FROM version_info v
            JOIN source src ON src.id = v.source_id
            WHERE v.id = ?
            """;
        return jdbcTemplate.queryForObject(sql, rowMapper, versionId);
    }

    public List<Long> getAllVersionsByTypeBefore(final NrtmDocumentType type, final LocalDateTime before) {
        final long beforeTimestamp = before.toEpochSecond(ZoneOffset.UTC);

        final String sql = """
            SELECT v.id
            FROM version_info v
            WHERE v.type = ? AND v.created < ? 
            """;
        return jdbcTemplate.queryForList(sql, Long.class, type.name(), beforeTimestamp);
    }

    public List<NrtmVersionInfo> getAllVersionsByType(final NrtmDocumentType type) {
        final String sql = """
            SELECT v.id, src.id, src.name, v.version, v.session_id, v.type, v.last_serial_id, v.created
            FROM version_info v
            JOIN source src ON src.id = v.source_id
            WHERE v.type = ? ORDER BY v.last_serial_id DESC
            """;
        return jdbcTemplate.query(sql, rowMapper, type.name());
    }
}
