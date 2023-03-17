/*
package net.ripe.db.nrtm4.dao;

import net.ripe.db.nrtm4.domain.DeltaFile;
import net.ripe.db.whois.common.source.SourceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.Optional;

public abstract class AbstractSourceAwareRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSourceAwareRepository.class);

    private final JdbcTemplate jdbcTemplate;
    private final SourceContext sourceContext;

    public AbstractSourceAwareRepository(@Qualifier("nrtmSourceAwareDataSource") final DataSource dataSource, final SourceContext sourceContext) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.sourceContext = sourceContext;
    }


    public <T> Optional<T> execute() {

    }

    abstract <T> Optional<T> getPayload() ;
}
*/
