package net.ripe.db.whois.api.whois;

import com.google.common.collect.Sets;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.RpslObjectUpdateDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.query.handler.QueryHandler;
import net.ripe.db.whois.update.handler.UpdateRequestHandler;
import net.ripe.db.whois.update.log.LoggerContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WhoisRestServiceTest {

    @Mock DateTimeProvider dateTimeProvider;
    @Mock UpdateRequestHandler updateRequestHandler;
    @Mock LoggerContext loggerContext;
    @Mock RpslObjectDao rpslObjectDao;
    @Mock RpslObjectUpdateDao rpslObjectUpdateDao;
    @Mock QueryHandler queryHandler;
    @Mock HttpServletRequest request;

    @Mock SourceContext sourceContext;
    @Mock Source source;

    @InjectMocks WhoisRestService subject;

    @Before
    public void setup() {
        when(sourceContext.getCurrentSource()).thenReturn(source);
        when(source.getName()).thenReturn(CIString.ciString("TEST"));
    }

    @Test
    public void search_invalidFlag_template_short() {
        try {
            subject.search(request, Sets.newHashSet("TEST"), "AARDVARK-MNT", Collections.EMPTY_SET, Collections.EMPTY_SET, Sets.newHashSet("t"));
            fail();
        } catch (IllegalArgumentException expected) {
            assertThat(expected.getMessage(), is("Invalid option 't'"));
        }
    }
    @Test
    public void search_invalidFlag_template_long() {
        try {
            subject.search(request, Sets.newHashSet("TEST"), "AARDVARK-MNT", Collections.EMPTY_SET, Collections.EMPTY_SET, Sets.newHashSet("template"));
            fail();
        } catch (IllegalArgumentException expected) {
            assertThat(expected.getMessage(), is("Invalid option 'template'"));
        }
    }

    @Test
    public void search_invalidFlag_verbose_short() {
        try {
            subject.search(request, Sets.newHashSet("TEST"), "AARDVARK-MNT", Collections.EMPTY_SET, Collections.EMPTY_SET, Sets.newHashSet("v"));
            fail();
        } catch (IllegalArgumentException expected) {
            assertThat(expected.getMessage(), is("Invalid option 'v'"));
        }
    }
    @Test
    public void search_invalidFlag_verbose_long() {
        try {
            subject.search(request, Sets.newHashSet("TEST"), "AARDVARK-MNT", Collections.EMPTY_SET, Collections.EMPTY_SET, Sets.newHashSet("verbose"));
            fail();
        } catch (IllegalArgumentException expected) {
            assertThat(expected.getMessage(), is("Invalid option 'verbose'"));
        }
    }

    @Test
    public void search_invalidFlag_client_short() {
        try {
            subject.search(request, Sets.newHashSet("TEST"), "AARDVARK-MNT", Collections.EMPTY_SET, Collections.EMPTY_SET, Sets.newHashSet("V"));
            fail();
        } catch (IllegalArgumentException expected) {
            assertThat(expected.getMessage(), is("Invalid option 'V'"));
        }
    }
    @Test
    public void search_invalidFlag_client_long() {
        try {
            subject.search(request, Sets.newHashSet("TEST"), "AARDVARK-MNT", Collections.EMPTY_SET, Collections.EMPTY_SET, Sets.newHashSet("client"));
            fail();
        } catch (IllegalArgumentException expected) {
            assertThat(expected.getMessage(), is("Invalid option 'client'"));
        }
    }

    @Test
    public void search_invalidFlag_nogrouping_short() {
        try {
            subject.search(request, Sets.newHashSet("TEST"), "AARDVARK-MNT", Collections.EMPTY_SET, Collections.EMPTY_SET, Sets.newHashSet("G"));
            fail();
        } catch (IllegalArgumentException expected) {
            assertThat(expected.getMessage(), is("Invalid option 'G'"));
        }
    }
    @Test
    public void search_invalidFlag_nogrouping_long() {
        try {
            subject.search(request, Sets.newHashSet("TEST"), "AARDVARK-MNT", Collections.EMPTY_SET, Collections.EMPTY_SET, Sets.newHashSet("no-grouping"));
            fail();
        } catch (IllegalArgumentException expected) {
            assertThat(expected.getMessage(), is("Invalid option 'no-grouping'"));
        }
    }

    @Test
    public void search_invalidFlag_notaginfo() {
        try {
            subject.search(request, Sets.newHashSet("TEST"), "AARDVARK-MNT", Collections.EMPTY_SET, Collections.EMPTY_SET, Sets.newHashSet("no-tag-info"));
            fail();
        } catch (IllegalArgumentException expected) {
            assertThat(expected.getMessage(), is("Invalid option 'no-tag-info'"));
        }
    }

    @Test
    public void search_invalidFlag_showtaginfo() {
        try {
            subject.search(request, Sets.newHashSet("TEST"), "AARDVARK-MNT", Collections.EMPTY_SET, Collections.EMPTY_SET, Sets.newHashSet("show-tag-info"));
            fail();
        } catch (IllegalArgumentException expected) {
            assertThat(expected.getMessage(), is("Invalid option 'show-tag-info'"));
        }
    }

    @Test
    public void search_invalidFlag_allsources_short() {
        try {
            subject.search(request, Sets.newHashSet("TEST"), "AARDVARK-MNT", Collections.EMPTY_SET, Collections.EMPTY_SET, Sets.newHashSet("a"));
            fail();
        } catch (IllegalArgumentException expected) {
            assertThat(expected.getMessage(), is("Invalid option 'a'"));
        }
    }
    @Test
    public void search_invalidFlag_allsources_long() {
        try {
            subject.search(request, Sets.newHashSet("TEST"), "AARDVARK-MNT", Collections.EMPTY_SET, Collections.EMPTY_SET, Sets.newHashSet("all-sources"));
            fail();
        } catch (IllegalArgumentException expected) {
            assertThat(expected.getMessage(), is("Invalid option 'all-sources'"));
        }
    }

    @Test
    public void search_invalidFlag_listsourcesorversion() {
        try {
            subject.search(request, Sets.newHashSet("TEST"), "AARDVARK-MNT", Collections.EMPTY_SET, Collections.EMPTY_SET, Sets.newHashSet("q"));
            fail();
        } catch (IllegalArgumentException expected) {
            assertThat(expected.getMessage(), is("Invalid option 'q'"));
        }
    }

    @Test
    public void search_invalidFlag_listsources() {
        try {
            subject.search(request, Sets.newHashSet("TEST"), "AARDVARK-MNT", Collections.EMPTY_SET, Collections.EMPTY_SET, Sets.newHashSet("list-sources"));
            fail();
        } catch (IllegalArgumentException expected) {
            assertThat(expected.getMessage(), is("Invalid option 'list-sources'"));
        }
    }

    @Test
    public void search_invalidFlag_diffversions() {
        try {
            subject.search(request, Sets.newHashSet("TEST"), "AARDVARK-MNT", Collections.EMPTY_SET, Collections.EMPTY_SET, Sets.newHashSet("diff-versions"));
            fail();
        } catch (IllegalArgumentException expected) {
            assertThat(expected.getMessage(), is("Invalid option 'diff-versions'"));
        }
    }

    @Test
    public void search_invalidFlag_listversions() {
        try {
            subject.search(request, Sets.newHashSet("TEST"), "AARDVARK-MNT", Collections.EMPTY_SET, Collections.EMPTY_SET, Sets.newHashSet("list-versions"));
            fail();
        } catch (IllegalArgumentException expected) {
            assertThat(expected.getMessage(), is("Invalid option 'list-versions'"));
        }
    }

    @Test
    public void search_invalidFlag_showversion() {
        try {
            subject.search(request, Sets.newHashSet("TEST"), "AARDVARK-MNT", Collections.EMPTY_SET, Collections.EMPTY_SET, Sets.newHashSet("show-version"));
            fail();
        } catch (IllegalArgumentException expected) {
            assertThat(expected.getMessage(), is("Invalid option 'show-version'"));
        }
    }

    @Test
    public void search_invalidFlag_persistentconnection_short() {
        try {
            subject.search(request, Sets.newHashSet("TEST"), "AARDVARK-MNT", Collections.EMPTY_SET, Collections.EMPTY_SET, Sets.newHashSet("k"));
            fail();
        } catch (IllegalArgumentException expected) {
            assertThat(expected.getMessage(), is("Invalid option 'k'"));
        }
    }
    @Test
    public void search_invalidFlag_persistentconnection_long() {
        try {
            subject.search(request, Sets.newHashSet("TEST"), "AARDVARK-MNT", Collections.EMPTY_SET, Collections.EMPTY_SET, Sets.newHashSet("persistent-connection"));
            fail();
        } catch (IllegalArgumentException expected) {
            assertThat(expected.getMessage(), is("Invalid option 'persistent-connection'"));
        }
    }
}
