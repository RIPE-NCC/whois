package net.ripe.db.whois.api.autocomplete;

import com.google.common.base.Strings;
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
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Autocomplete - Suggestions - Typeahead API
 *
 *
 */
@Component
@Path("/autocomplete")
public class AutocompleteService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutocompleteService.class);

    private static final int MINIMUM_PREFIX_LENGTH = 2;

    private final AutocompleteSearch autocompleteSearch;

    @Autowired
    public AutocompleteService(final AutocompleteSearch autocompleteSearch) {
        this.autocompleteSearch = autocompleteSearch;
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response lookup(
                @QueryParam("q") final String query,
                @QueryParam("f") final String field) {

        if (Strings.isNullOrEmpty(query) || query.length() < MINIMUM_PREFIX_LENGTH) {
            return badRequest("query (q) parameter is required, and must be at least " + MINIMUM_PREFIX_LENGTH + " characters long");
        }

        if (Strings.isNullOrEmpty(field)) {
            return badRequest("field (f) parameter is required");
        }

        try {
            return ok(autocompleteSearch.search(query, field));
        } catch (IOException e) {
            return badRequest("Query failed.");
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/details")
    public Response lookupDetailed(
            @QueryParam("q") final String query,
            @QueryParam("f") final String field,
            @QueryParam("a") final Set<String> attributes) {

        if (Strings.isNullOrEmpty(query) || query.length() < MINIMUM_PREFIX_LENGTH) {
            return badRequest("query (q) parameter is required, and must be at least " + MINIMUM_PREFIX_LENGTH + " characters long");
        }

        if (Strings.isNullOrEmpty(field)) {
            return badRequest("field (f) parameter is required");
        }

        try {
            final List<Map<String, Object>> results = autocompleteSearch.searchDetailed(query, field, attributes);
            return Response.ok(results).build();
        } catch (IOException e) {
            return badRequest("Query failed.");
        }
    }

    // helper methods

    private Response badRequest(final String message) {
        return Response.status(Response.Status.BAD_REQUEST).entity(message).build();
    }

    private Response ok(final Object message) {
        return Response.ok(message).build();
    }

}
