package net.ripe.db.whois.common.dao.jdbc;

import net.ripe.db.whois.common.TransactionConfiguration;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.dao.RpslObjectUpdateInfo;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Set;

import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.lookupRpslObjectUpdateInfo;

@Repository
@Transactional(transactionManager = TransactionConfiguration.WHOIS_READONLY_TRANSACTION)
public class JdbcReferenceReadOnlyDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcReferenceReadOnlyDao.class);

    private final JdbcTemplate jdbcTemplate;
    private final RpslObjectDao rpslObjectDao;

    @Autowired
    public JdbcReferenceReadOnlyDao(@Qualifier("whoisSlaveDataSource") final DataSource dataSource, @Qualifier("jdbcRpslObjectSlaveDao") final RpslObjectDao rpslObjectDao) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.rpslObjectDao = rpslObjectDao;
    }

    public boolean isReferenced(final RpslObject object) {
        return JdbcReferencesOperations.isReferenced(jdbcTemplate, object);
    }

    public Set<RpslObjectInfo> getReferences(final RpslObject object) {
        return JdbcReferencesOperations.getReferences(jdbcTemplate, object);
    }

    public Map<RpslAttribute, Set<CIString>> getInvalidReferences(final RpslObject object) {
        return JdbcReferencesOperations.getInvalidReferences(jdbcTemplate, object);
    }


    public RpslObjectUpdateInfo lookupObject(ObjectType type, String pkey) {
        return lookupRpslObjectUpdateInfo(jdbcTemplate, type, pkey);
    }

    public Map<RpslObjectInfo, RpslObject> findReferences(final String pKey, final ObjectType objectType) {
       return JdbcReferencesOperations.findReferences(jdbcTemplate, lookupObjectByKey(pKey, objectType));
    }

    private RpslObject lookupObjectByKey(final String primaryKey, final ObjectType objectType) {
        try {
            return rpslObjectDao.getByKey(objectType, primaryKey);
        } catch (EmptyResultDataAccessException e) {
            throw e;
        } catch (DataAccessException e) {
            LOGGER.error("Unexpected", e);
            throw new EmptyResultDataAccessException(1);
        }
    }
}
