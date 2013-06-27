package net.ripe.db.whois.api.whois;

import com.google.common.collect.ImmutableList;
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
import java.util.List;

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
    public void search_disallowedFlags() {
        final List<String> disallowedFlags = ImmutableList.of("t", "template", "v", "verbose", "V", "client", "G", "no-grouping", "no-tag-info",
                "show-tag-info", "a", "all-sources", "q", "list-sources", "diff-versions", "list-versions", "show-version", "k", "persistent-connection");
        for (String disallowedFlag : disallowedFlags) {
            try {
                subject.search(
                        request,
                        Sets.newHashSet("TEST"),
                        "AARDVARK-MNT",
                        Collections.EMPTY_SET,
                        Collections.EMPTY_SET,
                        Collections.EMPTY_SET,
                        Collections.EMPTY_SET,
                        Sets.newHashSet(disallowedFlag));
                fail("Disallowed option " + disallowedFlag + " did not throw error");
            } catch (IllegalArgumentException expected) {
                assertThat(expected.getMessage(), is("Disallowed option '" + disallowedFlag + "'"));
            }

        }
    }
}
