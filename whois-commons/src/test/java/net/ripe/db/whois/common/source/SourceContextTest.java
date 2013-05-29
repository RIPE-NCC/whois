package net.ripe.db.whois.common.source;

import net.ripe.db.whois.common.jdbc.DataSourceFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.sql.DataSource;

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SourceContextTest {
    final String mainSourceNameString = "RIPE";
    final String grsSourceNames = "RIPE-GRS,APNIC-GRS,MISCONFIGURED";
    final String nrtmSourceNames = "NRTM-GRS";
    final String grsSourceNamesForDummification = "RIPE-GRS";
    final String grsMasterBaseUrl = "jdbc://localhost/master";
    final String whoisMasterUsername = "masterUser";
    final String whoisMasterPassword = "masterPw";
    final String grsSlaveBaseUrl = "jdbc://localhost/slave";
    final String whoisSlaveUsername = "slaveUser";
    final String whoisSlavePassword = "slavePw";
    @Mock DataSource whoisMasterDataSource;
    @Mock DataSource whoisSlaveDataSource;
    @Mock DataSource grsDataSource;
    @Mock DataSourceFactory dataSourceFactory;
    SourceContext subject;

    @Before
    public void setUp() throws Exception {
        when(dataSourceFactory.createDataSource(anyString(), anyString(), anyString())).thenReturn(grsDataSource);

        subject = new SourceContext(
                mainSourceNameString,
                grsSourceNames,
                nrtmSourceNames,
                grsSourceNamesForDummification,
                grsMasterBaseUrl,
                whoisMasterUsername,
                whoisMasterPassword,
                grsSlaveBaseUrl,
                whoisSlaveUsername,
                whoisSlavePassword,
                whoisMasterDataSource,
                whoisSlaveDataSource,
                dataSourceFactory
        );
    }

    @After
    public void tearDown() {
        subject.removeCurrentSource();
    }

    @Test(expected = IllegalSourceException.class)
    public void setCurrent_unknown_source() {
        subject.setCurrent(Source.slave("UNKNOWN-GRS"));
    }

    @Test
    public void getCurrent_default() {
        final Source currentSource = subject.getCurrentSource();
        assertThat(currentSource.getName(), is(ciString("RIPE")));
        assertThat(currentSource.isGrs(), is(false));
    }

    @Test
    public void isDummificationRequired_RIPE_GRS() {
        subject.setCurrent(Source.slave("RIPE-GRS"));
        assertThat(subject.isDummificationRequired(), is(true));
    }

    @Test
    public void isDummificationRequired_APNIC_GRS() {
        subject.setCurrent(Source.slave("APNIC-GRS"));
        assertThat(subject.isDummificationRequired(), is(false));
    }

    @Test
    public void getNrtmSource() {
        subject.setCurrent(Source.master("NRTM-GRS"));
        assertThat(subject.getCurrentSource().isGrs(), is(true));
    }
}
