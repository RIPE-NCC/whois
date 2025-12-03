package net.ripe.db.whois.common.dao.jdbc;

import com.google.common.collect.Maps;
import net.ripe.db.whois.common.dao.KeyCloakApiKeyDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Map;

@Repository
public class JdbcKeyCloakApiKeyDao implements KeyCloakApiKeyDao {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
                                new DateTimeFormatterBuilder()
                                        .parseCaseInsensitive()
                                        .append(DateTimeFormatter.ISO_LOCAL_DATE)
                                        .appendLiteral(' ')
                                        .append(DateTimeFormatter.ISO_TIME)
                                        .toFormatter();

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcKeyCloakApiKeyDao(@Qualifier("internalsDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public Map<String, LocalDate> getAllKeyIdWithExpiry() {
        final Map<String, LocalDate> apikeyIdToExpiry = Maps.newHashMap();
        jdbcTemplate.query("""
                SELECT apikeyId, expiresAt
                FROM keycloak_apikey_details 
                """,
                (rs, rowNum) -> apikeyIdToExpiry.put(
                        rs.getString(1),
                        LocalDate.parse(rs.getString(2), DATE_TIME_FORMATTER))
        );

        return apikeyIdToExpiry;
    }
}
