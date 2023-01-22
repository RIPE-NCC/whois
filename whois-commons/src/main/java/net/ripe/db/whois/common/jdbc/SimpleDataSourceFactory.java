package net.ripe.db.whois.common.jdbc;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;

@Component
public class SimpleDataSourceFactory implements DataSourceFactory {

    private final String driverClass;

    @Autowired
    public SimpleDataSourceFactory(@Value("${whois.db.driver}") String driverClass) {
        this.driverClass = driverClass;
    }

    @Override
    public DataSource createDataSource(final String url, final String username, final String password) {
        try {
            final ComboPooledDataSource cpds = new ComboPooledDataSource();
            cpds.setDriverClass(driverClass);
            cpds.setJdbcUrl(url);
            cpds.setUser(username);
            cpds.setPassword(password);

            cpds.setMinPoolSize(0);
            cpds.setMaxPoolSize(100);
            cpds.setMaxIdleTime(7200);
            cpds.setPreferredTestQuery("SELECT 1");
            cpds.setIdleConnectionTestPeriod(15);

            return cpds;
        } catch (PropertyVetoException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
