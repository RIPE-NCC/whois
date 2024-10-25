package net.ripe.db.whois.common;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;

@Configuration
public class DataSourceConfigurations {

    public static final String DRIVER_CLASS_NAME = "org.mariadb.jdbc.Driver";

    @Bean
    public DataSource aclDataSource(@Value("${acl.database.url}") final String jdbcUrl, @Value("${acl.database.username}") final String jdbcUser, @Value("${acl.database.password}") final String jdbcPass) throws PropertyVetoException {
        return createDataSource(DRIVER_CLASS_NAME, jdbcUrl, jdbcUser, jdbcPass);
    }

    @Bean
    public DataSource mailupdatesDataSource(@Value("${mailupdates.database.url}") final String jdbcUrl, @Value("${mailupdates.database.username}") final String jdbcUser, @Value("${mailupdates.database.password}") final String jdbcPass) throws PropertyVetoException {
        return createDataSource(DRIVER_CLASS_NAME, jdbcUrl, jdbcUser, jdbcPass);
    }

    @Bean
    public DataSource internalsDataSource(@Value("${internals.database.url}") final String jdbcUrl, @Value("${internals.database.username}") final String jdbcUser, @Value("${internals.database.password}") final String jdbcPass) throws PropertyVetoException {
        return createDataSource(DRIVER_CLASS_NAME, jdbcUrl, jdbcUser, jdbcPass);
    }

    @Bean
    public DataSource internalsSlaveDataSource(@Value("${internals.slave.database.url}") final String jdbcUrl, @Value("${internals.slave.database.username}") final String jdbcUser, @Value("${internals.slave.database.password}") final String jdbcPass) throws PropertyVetoException {
        return createDataSource(DRIVER_CLASS_NAME, jdbcUrl, jdbcUser, jdbcPass);
    }

    @Bean
    @DependsOn("loggingDriver")
    public DataSource whoisMasterDataSource(@Value("${whois.db.master.driver}") final String jdbcDriver, @Value("${whois.db.master.url}") final String jdbcUrl, @Value("${whois.db.master.username}") final String jdbcUser, @Value("${whois.db.master.password}") final String jdbcPass) throws PropertyVetoException {
        return createDataSource(jdbcDriver, jdbcUrl, jdbcUser, jdbcPass);
    }

    @Bean
    public ComboPooledDataSource whoisSlaveDataSource(@Value("${whois.db.slave.url}") final String jdbcUrl, @Value("${whois.db.slave.username}") final String jdbcUser, @Value("${whois.db.slave.password}") final String jdbcPass) throws PropertyVetoException {
       return createDataSource(DRIVER_CLASS_NAME, jdbcUrl, jdbcUser, jdbcPass);
   }

    @Bean
    public ComboPooledDataSource nrtmMasterDataSource(@Value("${nrtm.database.url}") final String jdbcUrl, @Value("${nrtm.database.username}") final String jdbcUser, @Value("${nrtm.database.password}") final String jdbcPass) throws PropertyVetoException {
        return createDataSource(DRIVER_CLASS_NAME, jdbcUrl, jdbcUser, jdbcPass);
    }

    @Bean
    public ComboPooledDataSource nrtmSlaveDataSource(@Value("${nrtm.slave.database.url}") final String jdbcUrl, @Value("${nrtm.slave.database.username}") final String jdbcUser, @Value("${nrtm.slave.database.password}") final String jdbcPass) throws PropertyVetoException {
        return createDataSource(DRIVER_CLASS_NAME, jdbcUrl, jdbcUser, jdbcPass);
    }

    public static ComboPooledDataSource createDataSource(final String jdbcDriver, final String jdbcUrl, final String jdbcUser, final String jdbcPass) throws PropertyVetoException {
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
