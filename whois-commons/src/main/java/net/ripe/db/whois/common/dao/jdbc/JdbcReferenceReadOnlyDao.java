package net.ripe.db.whois.common.dao.jdbc;

import net.ripe.db.whois.common.TransactionConfiguration;
import net.ripe.db.whois.common.dao.ReferenceDao;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.CheckForNull;
import javax.sql.DataSource;
import java.util.Map;
import java.util.Set;

@Repository
@Transactional(transactionManager = TransactionConfiguration.WHOIS_READONLY_TRANSACTION)
public class JdbcReferenceReadOnlyDao implements ReferenceDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcReferenceReadOnlyDao(@Qualifier("whoisSlaveDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public boolean isReferenced(final RpslObject object) {
        return JdbcReferencesOperations.isReferenced(jdbcTemplate, object);
    }

    @Override
    public Set<RpslObjectInfo> getReferences(final RpslObject object) {
        return JdbcReferencesOperations.getReferences(jdbcTemplate, object);
    }

    @Override
    public Map<RpslAttribute, Set<CIString>> getInvalidReferences(final RpslObject object) {
        return JdbcReferencesOperations.getInvalidReferences(jdbcTemplate, object);
    }

    @CheckForNull
    @Override
    public RpslObjectInfo getAttributeReference(AttributeType attributeType, CIString keyValue) {
        return JdbcReferencesOperations.getAttributeReference(jdbcTemplate, attributeType, keyValue);
    }
}
