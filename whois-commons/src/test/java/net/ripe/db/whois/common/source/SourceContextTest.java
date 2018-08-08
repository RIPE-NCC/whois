package net.ripe.db.whois.common.source;

import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.jdbc.DataSourceFactory;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.util.Set;

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SourceContextTest {
    final String mainSourceNameString = "RIPE";
    final String nonauthRipeSourceNameString = "RIPE-NONAUTH";
    final String additionalSourceNames = "RIPE,RIPE-GRS,APNIC-GRS";
    final String grsSourceNames = "RIPE-GRS,APNIC-GRS";
    final String nrtmSourceNames = "NRTM-GRS";
    final String grsSourceNamesForDummification = "RIPE-GRS";
    final String grsSourceNamesToTagRoutes = "RIPE-GRS";
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

        subject = new DefaultSourceContext(
                mainSourceNameString,
                nonauthRipeSourceNameString,
                additionalSourceNames,
                grsSourceNames,
                nrtmSourceNames,
                grsSourceNamesForDummification,
                grsSourceNamesToTagRoutes,
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

    @Test
    public void getAdditionalSources() {
        final Set<CIString> sourceNames = subject.getAdditionalSourceNames();
        assertThat(sourceNames, hasSize(3));
        assertThat(sourceNames, hasItems(ciString("RIPE"), ciString("RIPE-GRS"), ciString("APNIC-GRS")));
    }

    @Test
    public void invalidAdditionalSource() {
        final String invalidAdditionalSource = "INVALID";
        try {
            new DefaultSourceContext(
                mainSourceNameString,
                nonauthRipeSourceNameString,
                invalidAdditionalSource,
                grsSourceNames,
                nrtmSourceNames,
                grsSourceNamesForDummification,
                grsSourceNamesToTagRoutes,
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
            fail();
        } catch (IllegalSourceException e) {
            assertThat(e.getMessage(), containsString("Invalid source specified: INVALID"));
        }
    }

    @Test
    public void noAdditionalSources() {
        final String noAdditionalSources = "";
        subject = new DefaultSourceContext(
            mainSourceNameString,
            nonauthRipeSourceNameString,
            noAdditionalSources,
            grsSourceNames,
            nrtmSourceNames,
            grsSourceNamesForDummification,
            grsSourceNamesToTagRoutes,
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
        assertThat(subject.getAdditionalSourceNames(), Matchers.hasSize(0));
    }
}
