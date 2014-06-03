package net.ripe.db.whois.internal.api.source;

import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.source.BasicSourceContext;
import net.ripe.db.whois.internal.AbstractInternalTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.sql.DataSource;

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


@Category(IntegrationTest.class)
public class InternalSourceContextTest extends AbstractInternalTest {

    @Autowired
    BasicSourceContext internalSourceContext;

    @Autowired @Qualifier("whoisReadOnlySlaveDataSource")
    DataSource whoisReadOnlySlaveDataSource;

    @Test
    public void should_return_TEST_with_WhoiSlaveDatasource_as_currentSource() {
        assertThat(internalSourceContext.getCurrentSource().getName(), is(ciString("TEST")));
        assertThat(internalSourceContext.getCurrentSourceConfiguration().getDataSource(), is(whoisReadOnlySlaveDataSource));
    }
}