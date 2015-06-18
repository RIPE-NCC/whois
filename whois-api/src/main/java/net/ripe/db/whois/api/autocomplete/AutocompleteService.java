package net.ripe.db.whois.api.autocomplete;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
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

        final List<String> badAttributes = Lists.newArrayList();
        for (final String attribute : attributes){
            if (AttributeType.getByNameOrNull(attribute) == null) {
                badAttributes.add(attribute);
            }
        }
        if (!badAttributes.isEmpty()){
            return badRequest(String.format("invalid name for attribute(s) : %s", badAttributes));
        }

        try {
            if ((isExtendedParameter(extended)) || !attributes.isEmpty()) {
                return ok(autocompleteSearch.searchExtended(query, field, attributes));
            }

            return ok(autocompleteSearch.search(query, field));

        } catch (IOException e) {
            return badRequest("Query failed.");
        }
    }

    private boolean isExtendedParameter(final String extended) {
        return extended != null && (extended.isEmpty() || extended.equalsIgnoreCase("true"));
    }

    // helper methods

    private Response badRequest(final String message) {
        return Response.status(Response.Status.BAD_REQUEST).entity(message).build();
    }

    private Response ok(final Object message) {
        return Response.ok(message).build();
    }

}
