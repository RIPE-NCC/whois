package net.ripe.db.nrtm4.client.config;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.beans.PropertyVetoException;

@Configuration
public class NrtmDataSourceConfigurations {

    private static final String DRIVER_CLASS_NAME = "org.mariadb.jdbc.Driver";

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

    private ComboPooledDataSource createDataSource(final String jdbcDriver, final String jdbcUrl, final String jdbcUser, final String jdbcPass) throws PropertyVetoException {
        final ComboPooledDataSource source = new ComboPooledDataSource();

        source.setJdbcUrl(jdbcUrl);
        source.setUser(jdbcUser);
        source.setPassword(jdbcPass);
        source.setDriverClass(jdbcDriver);

        source.setMinPoolSize(0);
        source.setMaxPoolSize(100);
        source.setMaxIdleTime(7200);
        source.setPreferredTestQuery("SELECT 1");
        source.setIdleConnectionTestPeriod(15);
        source.setConnectionCustomizerClassName("net.ripe.db.whois.common.jdbc.WhoisConnectorCustomizer");
        source.setConnectionTesterClassName("com.mchange.v2.c3p0.impl.DefaultConnectionTester");

        return source;
    }
}
