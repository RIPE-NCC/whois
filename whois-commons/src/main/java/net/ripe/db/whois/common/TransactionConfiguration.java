package net.ripe.db.whois.common;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement(mode = AdviceMode.ASPECTJ)
public class TransactionConfiguration {

    public static final String  ACL_UPDATE_TRANSACTION_MANAGER = "acl-update-transaction-manager";

    public static final String MAIL_UPDATES_TRANSACTION = "mail-updates-transaction-manager";

    public static final String  INTERNALS_UPDATE_TRANSACTION = "internals-update-transaction-manager";
    public static final String INTERNALS_READONLY_TRANSACTION = "internals-readonly-transaction-manager";

    public static final String WHOIS_UPDATE_TRANSACTION = "whois-update-transaction-manager";
    public static final String WHOIS_READONLY_TRANSACTION = "whois-readonly-transaction-manager";

    public static final String NRTM_UPDATE_TRANSACTION = "nrtm-update-transaction-manager";
    public static final String NRTM_READONLY_TRANSACTION = "nrtm-readonly-transaction-manager";

    @Bean
    @Primary
    public TransactionManager transactionManager(@Qualifier("sourceAwareDataSource") final DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = ACL_UPDATE_TRANSACTION_MANAGER)
    public TransactionManager transactionManagerAclUpdate(final @Qualifier("aclDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = MAIL_UPDATES_TRANSACTION)
    public TransactionManager transactionManagerMailUpdates(@Qualifier("mailupdatesDataSource") final DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = INTERNALS_UPDATE_TRANSACTION)
    public TransactionManager transactionManagerInternalsUpdates(@Qualifier("internalsDataSource") final DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = INTERNALS_READONLY_TRANSACTION)
    public TransactionManager transactionManagerInternalsReadOnly(@Qualifier("internalsSlaveDataSource") final DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = WHOIS_UPDATE_TRANSACTION)
    public TransactionManager transactionManagerWhoisUpdates( @Qualifier("whoisMasterDataSource") final DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = WHOIS_READONLY_TRANSACTION)
    public TransactionManager transactionManagerWhoisReadOnly(@Qualifier("whoisSlaveDataSource") final DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = NRTM_UPDATE_TRANSACTION)
    public TransactionManager transactionManagerNrtmUpdate(@Qualifier("nrtmMasterDataSource") final DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = NRTM_READONLY_TRANSACTION)
    public TransactionManager transactionManagerNrtmReadOnly(@Qualifier("nrtmSlaveDataSource") final DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

}
