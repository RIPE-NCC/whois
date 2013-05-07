package net.ripe.db.whois.common.support;

import net.ripe.db.whois.common.dao.jdbc.AbstractDatabaseHelperTest;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(locations = {"classpath:applicationContext-commons-test.xml"})
public abstract class AbstractDaoTest extends AbstractDatabaseHelperTest {
}
