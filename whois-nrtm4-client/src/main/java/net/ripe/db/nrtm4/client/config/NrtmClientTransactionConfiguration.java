package net.ripe.db.nrtm4.client.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement(mode = AdviceMode.ASPECTJ)
public class NrtmClientTransactionConfiguration {

    public static final String NRTM_CLIENT_UPDATE_TRANSACTION = "nrtm-update-transaction-manager";
    public static final String NRTM_CLIENT_INFO_UPDATE_TRANSACTION = "nrtm-update-info-transaction-manager";

    @Bean(name = NRTM_CLIENT_UPDATE_TRANSACTION)
    public TransactionManager transactionManagerNrtmUpdate(@Qualifier("nrtmClientMasterDataSource") final DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = NRTM_CLIENT_INFO_UPDATE_TRANSACTION)
    public TransactionManager transactionManagerNrtmInfo(@Qualifier("nrtmClientMasterInfoSource") final DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

}
