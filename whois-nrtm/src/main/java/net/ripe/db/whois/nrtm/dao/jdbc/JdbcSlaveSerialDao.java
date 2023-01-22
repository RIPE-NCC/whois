package net.ripe.db.whois.nrtm.dao.jdbc;

import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.dao.jdbc.JdbcSerialDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import jakarta.sql.DataSource;

@Repository
public class JdbcSlaveSerialDao extends JdbcSerialDao {

    @Autowired
    public JdbcSlaveSerialDao(@Qualifier("whoisSlaveDataSource") final DataSource dataSource, final DateTimeProvider dateTimeProvider) {
        super(dataSource, dateTimeProvider);
    }

}
