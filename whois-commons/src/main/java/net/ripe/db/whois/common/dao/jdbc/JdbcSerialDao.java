package net.ripe.db.whois.common.dao.jdbc;

import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.dao.SerialDao;
import net.ripe.db.whois.common.domain.serials.SerialEntry;
import net.ripe.db.whois.common.domain.serials.SerialRange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.CheckForNull;
import javax.sql.DataSource;

@Repository
@Primary
public class JdbcSerialDao implements SerialDao {
    private final JdbcTemplate jdbcTemplate;
    private final DateTimeProvider dateTimeProvider;

    @Autowired
    public JdbcSerialDao(@Qualifier("sourceAwareDataSource") final DataSource dataSource, final DateTimeProvider dateTimeProvider) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.dateTimeProvider = dateTimeProvider;
    }

    @Override
    public SerialRange getSerials() {
        return JdbcRpslObjectOperations.getSerials(jdbcTemplate);
    }

    @Override
    @CheckForNull
    public SerialEntry getById(final int serialId) {
        return JdbcRpslObjectOperations.getSerialEntry(jdbcTemplate, serialId);
    }

    @Override
    @CheckForNull
    public SerialEntry getByIdForNrtm(final int serialId) {
        return JdbcRpslObjectOperations.getSerialEntryForNrtm(jdbcTemplate, serialId);
    }

    @Override
    @CheckForNull
    public Integer getAgeOfExactOrNextExistingSerial(final int serialId) {
        return JdbcRpslObjectOperations.getAgeOfExactOrNextExistingSerial(dateTimeProvider, jdbcTemplate, serialId);
    }
}
