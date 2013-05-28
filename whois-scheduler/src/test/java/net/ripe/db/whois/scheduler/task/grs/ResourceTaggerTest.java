package net.ripe.db.whois.scheduler.task.grs;

import net.ripe.db.whois.common.dao.TagsDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.grs.AuthoritativeResource;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.source.SourceConfiguration;
import net.ripe.db.whois.common.source.SourceContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ResourceTaggerTest {
    @Mock SourceConfiguration sourceConfiguration;
    @Mock JdbcTemplate jdbcTemplate;
    @Mock ResultSet resultSet;

    @Mock GrsSource grsSource;
    @Mock AuthoritativeResource authoritativeResource;

    @Mock SourceContext sourceContext;
    @Mock TagsDao tagsDao;
    @InjectMocks ResourceTagger subject;

    @Before
    public void setUp() throws Exception {
        when(grsSource.getSource()).thenReturn("RIPE-GRS");
        when(grsSource.getLogger()).thenReturn(LoggerFactory.getLogger(ResourceTaggerTest.class));
        when(grsSource.getAuthoritativeResource()).thenReturn(authoritativeResource);
        when(sourceContext.getCurrentSourceConfiguration()).thenReturn(sourceConfiguration);
        when(sourceConfiguration.getJdbcTemplate()).thenReturn(jdbcTemplate);
    }

    @Test
    public void tagObjects() {
        subject.tagObjects(grsSource);

        verify(sourceContext).setCurrent(any(Source.class));
        verify(sourceContext).removeCurrentSource();
        verify(tagsDao).updateTags(any(CIString.class), any(List.class), any(List.class));
    }

    @Test
    public void tagObjects_cleans_up() {
        doThrow(SQLException.class).when(tagsDao).updateTags(any(CIString.class), any(List.class), any(List.class));

        try {
            subject.tagObjects(grsSource);
            fail("Expected exception");
        } catch (Exception ignored) {
        }

        verify(sourceContext).setCurrent(any(Source.class));
        verify(sourceContext).removeCurrentSource();
    }
}
