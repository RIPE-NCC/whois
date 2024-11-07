package net.ripe.db.nrtm4.client.config;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import net.ripe.db.nrtm4.client.condition.Nrtm4ClientCondition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.beans.PropertyVetoException;

import static net.ripe.db.whois.common.DataSourceConfigurations.DRIVER_CLASS_NAME;
import static net.ripe.db.whois.common.DataSourceConfigurations.createDataSource;

@Conditional(Nrtm4ClientCondition.class)
@Configuration
public class NrtmClientDataSourceConfigurations {

    @Bean
    public ComboPooledDataSource nrtmClientMasterInfoSource(@Value("${nrtm.client.info.database.url}") final String jdbcUrl,
                                                            @Value("${nrtm.client.info.database.username}") final String jdbcUser,
                                                            @Value("${nrtm.client.info.database.password}") final String jdbcPass) throws PropertyVetoException {
        return createDataSource(DRIVER_CLASS_NAME, jdbcUrl, jdbcUser, jdbcPass);
    }

    @Bean
    public ComboPooledDataSource nrtmClientSlaveInfoSource(@Value("${nrtm.client.info.slave.database.url}") final String jdbcUrl,
                                                           @Value("${nrtm.client.info.slave.database.username}") final String jdbcUser,
                                                           @Value("${nrtm.client.info.slave.database.password}") final String jdbcPass) throws PropertyVetoException {
        return createDataSource(DRIVER_CLASS_NAME, jdbcUrl, jdbcUser, jdbcPass);
    }

    @Bean
    public ComboPooledDataSource nrtmClientMasterDataSource(@Value("${nrtm.client.database.url}") final String jdbcUrl,
                                                            @Value("${nrtm.client.database.username}") final String jdbcUser,
                                                            @Value("${nrtm.client.database.password}") final String jdbcPass) throws PropertyVetoException {
        return createDataSource(DRIVER_CLASS_NAME, jdbcUrl, jdbcUser, jdbcPass);
    }

    @Bean
    public ComboPooledDataSource nrtmClientSlaveDataSource(@Value("${nrtm.client.slave.database.url}") final String jdbcUrl,
                                                           @Value("${nrtm.client.slave.database.username}") final String jdbcUser,
                                                           @Value("${nrtm.client.slave.database.password}") final String jdbcPass) throws PropertyVetoException {
        return createDataSource(DRIVER_CLASS_NAME, jdbcUrl, jdbcUser, jdbcPass);
    }

}
