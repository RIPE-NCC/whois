package net.ripe.db.nrtm4;

import net.ripe.db.whois.common.dao.jdbc.AbstractDatabaseHelperIntegrationTest;
import net.ripe.db.whois.common.source.SourceAwareDataSource;
import org.springframework.beans.factory.annotation.Autowired;


public class AbstractNrtm4IntegrationBase extends AbstractDatabaseHelperIntegrationTest {
    @Autowired
    protected SourceAwareDataSource sourceAwareDataSource;
}
