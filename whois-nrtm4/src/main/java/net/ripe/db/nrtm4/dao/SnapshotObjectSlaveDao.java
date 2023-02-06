package net.ripe.db.nrtm4.dao;

import org.springframework.beans.factory.annotation.Qualifier;

import javax.sql.DataSource;


public class SnapshotObjectSlaveDao extends SnapshotObjectDao {

    public SnapshotObjectSlaveDao(
        @Qualifier("nrtmSlaveDataSource") final DataSource dataSource
    ) {
        super(dataSource);
    }

}
