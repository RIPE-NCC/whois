package net.ripe.db.whois.common.dao;

import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Repository
public class KeyCloakApiKeyDao {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public KeyCloakApiKeyDao(@Qualifier("internalsDataSource") final DataSource dataSource) {
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
                        LocalDate.parse(rs.getString(2), formatter))
        );

        return apikeyIdToExpiry;
    }
}
