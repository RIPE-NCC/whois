package net.ripe.db.whois.common.source;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.sql.DataSource;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SourceAwareDataSourceTest {
    @Mock private DataSource dataSource;
    @Mock private SourceConfiguration sourceConfiguration;
    @Mock private SourceContext sourceContext;
    @InjectMocks private SourceAwareDataSource subject;

    @Before
    public void setUp() throws Exception {
        when(sourceContext.getCurrentSourceConfiguration()).thenReturn(sourceConfiguration);
        when(sourceConfiguration.getDataSource()).thenReturn(dataSource);
    }

    @Test
    public void test_getConnection() throws Exception {
        subject.getConnection();

        verify(dataSource, times(1)).getConnection();
    }

    @Test
    public void test_getConnection_with_user() throws Exception {
        subject.getConnection("username", "password");

        verify(dataSource, times(1)).getConnection("username", "password");
    }
}
