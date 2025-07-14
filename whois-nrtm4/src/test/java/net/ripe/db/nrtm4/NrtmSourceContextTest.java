package net.ripe.db.nrtm4;

import net.ripe.db.nrtm4.source.NrtmSourceContext;
import net.ripe.db.whois.common.jdbc.DataSourceFactory;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.source.SourceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(MockitoExtension.class)
public class NrtmSourceContextTest {
    final String mainSourceNameString = "RIPE";
    final String nonauthRipeSourceNameString = "RIPE-NONAUTH";
    final String additionalSourceNames = "RIPE,RIPE-GRS,APNIC-GRS";
    final String grsSourceNames = "RIPE-GRS,APNIC-GRS";
    final String nrtmSourceNames = "NRTM-GRS";
    final String grsSourceNamesForDummification = "RIPE-GRS";
    final String grsSourceNamesToTagRoutes = "RIPE-GRS";
    final String grsMasterBaseUrl = "jdbc://localhost/master";
    final String nrtmMasterUsername = "masterUser";
    final String nrtmMasterPassword = "masterPw";
    final String grsSlaveBaseUrl = "jdbc://localhost/slave";
    final String nrtmSlaveUsername = "slaveUser";
    final String nrtmSlavePassword = "slavePw";
    @Mock DataSource nrtmMasterDataSource;
    @Mock DataSource nrtmSlaveDataSource;
    @Mock DataSource grsDataSource;
    @Mock DataSourceFactory dataSourceFactory;
    SourceContext subject;

    @BeforeEach
    public void setUp() throws Exception {
        subject = new NrtmSourceContext(
                mainSourceNameString,
                nonauthRipeSourceNameString,
                nrtmMasterDataSource,
                nrtmSlaveDataSource);
    }

    @AfterEach
    public void tearDown() {
        subject.removeCurrentSource();
    }

    @Test
    public void getCurrent_default() {
        final Source currentSource = subject.getCurrentSource();
        assertThat(currentSource.getName(), is(ciString("RIPE")));
        assertThat(currentSource.getType(), is(Source.Type.SLAVE));
    }
}
