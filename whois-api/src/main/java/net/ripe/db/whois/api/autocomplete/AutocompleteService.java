package net.ripe.db.whois.api.autocomplete;

import com.google.common.base.Strings;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectTemplate;
import net.ripe.db.whois.common.rpsl.ObjectType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.util.Collection;
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
            @QueryParam("select") final Set<String> select,
            @QueryParam("from") final Set<String> from,
            @QueryParam("where") final Set<String> where,
            @QueryParam("like") final String like) {
        try {
            if (!Strings.isNullOrEmpty(query) && !Strings.isNullOrEmpty(field)) {

                // query by field name

                if (query.length() < MINIMUM_PREFIX_LENGTH) {
                    return badRequest("query parameter is required, and must be at least " + MINIMUM_PREFIX_LENGTH + " characters long");
                }

                if (AttributeType.getByNameOrNull(field) == null) {
                    return badRequest("invalid name for field");
                }
                return okResponse(autocompleteSearch.search(query, getLookupAttributes(field), getAttributeTypes(attributes), Collections.emptySet()));
            } else if (!select.isEmpty() && !where.isEmpty() && !Strings.isNullOrEmpty(like)) {

                // query by attribute(s)

                return okResponse(autocompleteSearch.search(like, getAttributeTypes(where), getAttributeTypes(select), getObjectTypes(from)));
            } else {
                return badRequest("invalid arguments");
            }
        } catch (IOException e) {
            return badRequest("Query failed.");
        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage());
        }
    }

    // helper methods

    //    translate from field to attributes
    //      e.g. abuse-c (input field) -> role -> nic-hdl (attribute to search on)
    //
    private Set<AttributeType> getLookupAttributes(final String field) {
        final AttributeType attributeType = AttributeType.getByNameOrNull(field);
        if ( attributeType == null ) {
            throw new IllegalArgumentException("not valid field");
        }

        final ObjectType objectType = ObjectType.getByNameOrNull(field);
        if (objectType != null) {
            return Collections.singleton(ObjectTemplate.getTemplate(objectType).getKeyLookupAttribute());
        }

        return attributeType.getReferences()
            .stream()
            .map(input -> ObjectTemplate.getTemplate(input).getKeyLookupAttribute())
            .collect(Collectors.toSet());
    }

    private Set<AttributeType> getAttributeTypes(final Collection<String> attributes) {
        return attributes.stream()
                .map(AttributeType::getByName)
                .collect(Collectors.toSet());
    }

    private Set<ObjectType> getObjectTypes(final Collection<String> types) {
        return types.stream()
                .map(ObjectType::getByName)
                .collect(Collectors.toSet());
    }

    private Response badRequest(final String message) {
        return Response
                    .status(Response.Status.BAD_REQUEST)
                    .type(MediaType.TEXT_PLAIN_TYPE)
                    .entity(message).build();
    }

    private Response okResponse(final Object message) {
        return Response.ok(message).build();
    }
}
