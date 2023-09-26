package net.ripe.db.whois.common.dao.jdbc;

import net.ripe.db.whois.common.aspects.RetryFor;
import net.ripe.db.whois.common.source.SourceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

@Repository
@RetryFor(RecoverableDataAccessException.class)
public class JdbcRpslObjectSlaveDao extends JdbcRpslObjectDao {

    @Autowired
    public JdbcRpslObjectSlaveDao(@Qualifier("whoisSlaveDataSource") final DataSource dataSource, final SourceContext sourceContext) {
        super(dataSource, sourceContext);
    }


}
