package net.ripe.db.whois.api.autocomplete;

import com.google.common.base.Strings;
import net.ripe.db.whois.api.freetext.FreeTextSearch;
import net.ripe.db.whois.api.freetext.SearchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * Autocomplete - Suggestions - Typeahead API
 */
@Component
@Path("/autocomplete")
public class AutocompleteService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutocompleteService.class);

    private static final int MINIMUM_PREFIX_LENGTH = 3;

    private static final int RESULTS_LIMIT = 10;

    private final FreeTextSearch freeTextSearch;

    @Autowired
    public AutocompleteService(final FreeTextSearch freeTextSearch) throws IOException {
        this.freeTextSearch = freeTextSearch;
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response lookup(
                @QueryParam("q") final String queryString,
                @QueryParam("t") final ObjectTypeParameter objectType) {

        if (Strings.isNullOrEmpty(queryString) || queryString.length() <= MINIMUM_PREFIX_LENGTH) {
            throw new IllegalArgumentException("q parameter is required, and must be at least " + MINIMUM_PREFIX_LENGTH + " characters long");
        }

        final SearchResponse searchResponse;
        try {
            searchResponse = freeTextSearch.freeTextSearch(String.format("q=%s+AND+object-type%%3A%s", queryString, objectType.toString()));
        } catch (IOException e) {
            throw new IllegalStateException("Query failed.");
        }

        return Response.ok("num found = " + searchResponse.getResult().getNumFound()).build();
    }
}
