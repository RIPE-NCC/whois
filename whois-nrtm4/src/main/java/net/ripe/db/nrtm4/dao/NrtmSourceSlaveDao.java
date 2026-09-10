package net.ripe.db.nrtm4.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

@Repository
public class NrtmSourceSlaveDao extends NrtmSourceDao {

    @Autowired
    public NrtmSourceSlaveDao(
        @Qualifier("nrtmSlaveDataSource") final DataSource dataSource,
        @Value("${whois.source}") final String source,
        @Value("${whois.nonauth.source}") final String nonauthSource) {
        super(dataSource, source, nonauthSource);
    }
}
