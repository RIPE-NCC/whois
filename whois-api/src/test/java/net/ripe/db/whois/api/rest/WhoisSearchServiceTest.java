package net.ripe.db.whois.api.rest;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.source.SourceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WhoisSearchServiceTest {

    @Mock HttpServletRequest request;
    @Mock WhoisResources whoisResources;
    @Mock SourceContext sourceContext;
    @InjectMocks WhoisSearchService subject;

    @BeforeEach
    public void setup() {
        when(sourceContext.getAllSourceNames()).thenReturn(CIString.ciSet("TEST", "TEST-GRS"));
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
                        null,
                        Collections.EMPTY_SET,
                        Collections.EMPTY_SET,
                        Collections.EMPTY_SET,
                        Sets.newHashSet(disallowedFlag),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null);
                fail("Disallowed option " + disallowedFlag + " did not throw error");
            } catch (WebApplicationException e) {
                assertThat(((WhoisResources)e.getResponse().getEntity()).getErrorMessages().get(0).getText(), is("Disallowed search flag '%s'"));
            }
        }
    }

}
