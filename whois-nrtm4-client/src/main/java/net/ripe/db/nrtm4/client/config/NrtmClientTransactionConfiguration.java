package net.ripe.db.nrtm4.client.config;

import net.ripe.db.nrtm4.client.condition.Nrtm4ClientCondition;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;


@Configuration
@Conditional(Nrtm4ClientCondition.class)
@EnableTransactionManagement(mode = AdviceMode.ASPECTJ)
public class NrtmClientTransactionConfiguration {

    public static final String NRTM_CLIENT_UPDATE_TRANSACTION = "nrtm-client-update-transaction-manager";
    public static final String NRTM_CLIENT_INFO_TRANSACTION = "nrtm-client-info-transaction-manager";

    @Bean(name = NRTM_CLIENT_UPDATE_TRANSACTION)
    public PlatformTransactionManager transactionManagerNrtmClientUpdate(@Qualifier("nrtmClientMasterDataSource") final DataSource masterDataSource) {
        return new DataSourceTransactionManager(masterDataSource);
    }

    @Bean(name = NRTM_CLIENT_INFO_TRANSACTION)
    public PlatformTransactionManager transactionManagerNrtmClientInfo(@Qualifier("nrtmClientMasterInfoSource") final DataSource masterDataSource) {
        return new DataSourceTransactionManager(masterDataSource);
    }
}
