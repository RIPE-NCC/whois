package net.ripe.db.whois.api.autocomplete;

import com.google.common.base.Strings;
import net.ripe.db.whois.api.rest.RestServiceHelper;
import net.ripe.db.whois.common.rpsl.AttributeType;
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
import java.util.stream.Collectors;

/**
 * Autocomplete - Suggestions - Type-ahead API
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

    /**
     * Autocomplete lookup service
     *
     * @param query (required) term to search for (i.e. whatever the user has typed)
     * @param field (required) query field name
     * @param extended (optional) flag for extended mode (key and type included in response)
     * @param attributes (optional) also include specified attribute(s) in response
     * @return
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response lookup(
            @QueryParam("query") final String query,
            @QueryParam("field") final String field,
            @QueryParam("extended") final String extended,
            @QueryParam("attribute") final List<String> attributes) {

        if (Strings.isNullOrEmpty(query) || query.length() < MINIMUM_PREFIX_LENGTH) {
            return badRequest("query parameter is required, and must be at least " + MINIMUM_PREFIX_LENGTH + " characters long");
        }

        if (Strings.isNullOrEmpty(field)) {
            return badRequest("field parameter is required");
        }

        if (AttributeType.getByNameOrNull(field) == null) {
            return badRequest("invalid name for field");
        }

        final List<String> badAttributes = attributes.stream()
                                .filter(input -> (AttributeType.getByNameOrNull(input) == null))
                                .collect(Collectors.toList());
        if (!badAttributes.isEmpty()) {
            return badRequest(String.format("invalid name for attribute(s) : %s", badAttributes));
        }

        try {
            if ((RestServiceHelper.isQueryParamSet(extended)) || !attributes.isEmpty()) {
                return ok(autocompleteSearch.searchExtended(query, field, attributes));
            }

            return ok(autocompleteSearch.search(query, field));

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
