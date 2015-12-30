package net.ripe.db.whois.api.autocomplete;

import com.google.common.base.Strings;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectTemplateProvider;
import net.ripe.db.whois.common.rpsl.ObjectType;
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
import java.util.Collections;
import java.util.List;
import java.util.Set;
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
     * Autocomplete service
     *
     * There are two ways of calling this service, either using the field name, or by attribute name(s).
     *
     * Lookup by field name:
     *
     *      query (required)      = term to search for (i.e. whatever the user has typed)
     *      field (required)      = query field name
     *      attributes (optional) = also include specified attribute(s) in response. By default, only the primary key is returned.
     *
     * Lookup by attribute(s):
     *
     *      select = attributes to return
     *      from   = object type
     *      where  = attribute(s) to search in
     *      like   = query string
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response lookup(
            // query by field name
            @QueryParam("query") final String query,
            @QueryParam("field") final String field,
            @QueryParam("attribute") final List<String> attributes,
            // query by attribute(s)
            @QueryParam("select") final List<String> select,
            @QueryParam("from") final String from,
            @QueryParam("where") final List<String> where,
            @QueryParam("like") final String like) {

        if (!Strings.isNullOrEmpty(query) && !Strings.isNullOrEmpty(field)) {

            // (simple) field and value lookup

            if (query.length() < MINIMUM_PREFIX_LENGTH) {
                return badRequest("query parameter is required, and must be at least " + MINIMUM_PREFIX_LENGTH + " characters long");
            }

            if (AttributeType.getByNameOrNull(field) == null) {
                return badRequest("invalid name for field");
            }

            try {
                return ok(autocompleteSearch.search(query, getLookupAttributes(field), getResponseAttributes(attributes), Collections.emptySet()));
            } catch (IOException e) {
                return badRequest("Query failed.");
            } catch (IllegalArgumentException e) {
                return badRequest(e.getMessage());
            }

        } else if (!select.isEmpty() && !where.isEmpty() && !Strings.isNullOrEmpty(like)) {

            // TODO: select lookup

            return badRequest("not implemented yet");


        } else {
            return badRequest("invalid arguments");
        }
    }

    // helper methods

    //    translate from field to attributes
    //      e.g. abuse-c (input field) -> role -> nic-hdl (attribute to search on)
    //
    private Set<AttributeType> getLookupAttributes(final String field) {
        final AttributeType attributeType = AttributeType.getByNameOrNull(field);
        if ( attributeType == null ) {
            throw new IllegalArgumentException("not valid field");  // TODO: map to bad request
        }

        final ObjectType objectType = ObjectType.getByNameOrNull(field);
        if (objectType != null) {
            return Collections.singleton(ObjectTemplateProvider.getTemplate(objectType).getKeyLookupAttribute());
        }

        return attributeType.getReferences()
            .stream()
            .map(input -> ObjectTemplateProvider.getTemplate(input).getKeyLookupAttribute())
            .collect(Collectors.toSet());
    }

    private Set<AttributeType> getResponseAttributes(final List<String> attributes) {
        return attributes.stream()
                .map(input -> AttributeType.getByName(input))
                .collect(Collectors.toSet());
    }

    private Response badRequest(final String message) {
        return Response.status(Response.Status.BAD_REQUEST).entity(message).build();
    }

    private Response ok(final Object message) {
        return Response.ok(message).build();
    }

}
