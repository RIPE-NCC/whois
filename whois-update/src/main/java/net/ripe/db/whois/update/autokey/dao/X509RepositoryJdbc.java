package net.ripe.db.whois.update.autokey.dao;

import net.ripe.db.whois.update.domain.X509KeycertId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public class X509RepositoryJdbc implements X509Repository {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public X509RepositoryJdbc(@Qualifier("sourceAwareDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public boolean claimSpecified(final X509KeycertId x509) {
        final int updatedRows = jdbcTemplate.update("UPDATE x509 SET keycert_id = ? WHERE keycert_id < ?", x509.getIndex(), x509.getIndex());
        return updatedRows > 0;
    }

    @Override
    public X509KeycertId claimNextAvailableIndex(final String space, final String suffix) {
        final int highestX509Id = jdbcTemplate.queryForInt("SELECT keycert_id FROM x509");
        final X509KeycertId x509 = new X509KeycertId(space, highestX509Id + 1, suffix);
        final int updatedRows = jdbcTemplate.update("UPDATE x509 SET keycert_id = ? WHERE keycert_id = ?", x509.getIndex(), highestX509Id);

        if (updatedRows != 1) {
            throw new IllegalStateException("Unexpected rows: " + updatedRows + " when updating x509 table");
        }

        return x509;
    }

}
