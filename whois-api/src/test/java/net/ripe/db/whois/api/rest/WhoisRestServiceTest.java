package net.ripe.db.whois.api.rest;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.api.rest.domain.ErrorMessage;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.dao.RpslObjectDao;
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
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WhoisRestServiceTest {

    @Mock DateTimeProvider dateTimeProvider;
    @Mock UpdateRequestHandler updateRequestHandler;
    @Mock LoggerContext loggerContext;
    @Mock RpslObjectDao rpslObjectDao;
    @Mock QueryHandler queryHandler;
    @Mock HttpServletRequest request;
    @Mock WhoisService whoisService;
    @Mock WhoisResources whoisResources;

    @Mock SourceContext sourceContext;
    private Source source;

    @InjectMocks WhoisRestService subject;

    @Before
    public void setup() {
        source = Source.slave("TEST");
        when(sourceContext.getCurrentSource()).thenReturn(source);
        when(sourceContext.getAllSourceNames()).thenReturn(CIString.ciSet("TEST", "TEST-GRS"));
        when(whoisService.createErrorEntity(eq(request), Matchers.<Message[]>anyVararg())).thenReturn(whoisResources);
        when(whoisResources.getErrorMessages()).thenReturn(Lists.newArrayList(new ErrorMessage(new Message(Messages.Type.ERROR, "Disallowed search flag '%s'"))));
        when(request.getRequestURL()).thenReturn(new StringBuffer());
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
            } catch (WebApplicationException e) {
                assertThat(((WhoisResources)e.getResponse().getEntity()).getErrorMessages().get(0).getText(), is("Disallowed search flag '%s'"));
            }
        }
    }
}
