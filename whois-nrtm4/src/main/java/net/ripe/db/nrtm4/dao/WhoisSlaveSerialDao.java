package net.ripe.db.nrtm4.dao;

import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.dao.jdbc.JdbcSerialDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;


@Repository
public class WhoisSlaveSerialDao extends JdbcSerialDao {

    @Autowired
    public WhoisSlaveSerialDao(@Qualifier("whoisSlaveDataSource") final DataSource dataSource, final DateTimeProvider dateTimeProvider) {
        super(dataSource, dateTimeProvider);
    }

}