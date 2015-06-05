package net.ripe.db.whois.api.autocomplete;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import net.ripe.db.whois.api.freetext.FreeTextIndex;
import net.ripe.db.whois.api.freetext.FreeTextSearch;
import net.ripe.db.whois.api.freetext.SearchOptions;
import net.ripe.db.whois.api.freetext.SearchResponse;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
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
 * TODO: [ES] encapsulate internal freetextsearch / lucene functionality better (query string, sort, operator..)
 *
 */
@Component
@Path("/autocomplete")
public class AutocompleteService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutocompleteService.class);

    private static final int MINIMUM_PREFIX_LENGTH = 2;

    private static final int MAXIMUM_SEARCH_RESULTS = 10;

    private final FreeTextSearch freeTextSearch;

    @Autowired
    public AutocompleteService(final FreeTextSearch freeTextSearch) throws IOException {
        this.freeTextSearch = freeTextSearch;
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response lookup(
                @QueryParam("q") final String queryString,
                @QueryParam("ot") final ObjectTypeParameter objectType,
                @QueryParam("at") final AttributeTypeParameter attributeType) {

        if (Strings.isNullOrEmpty(queryString) || queryString.length() < MINIMUM_PREFIX_LENGTH) {
            return badRequest("query (q) parameter is required, and must be at least " + MINIMUM_PREFIX_LENGTH + " characters long");
        }

        if (attributeType == null) {
            return badRequest("attribute type (at) parameter is required");
        }

        if (objectType == null) {
            return badRequest("object type (ot) parameter is required");
        }

        final SearchResponse searchResponse;
        try {
            searchResponse = freeTextSearch.freeTextSearch(
                                String.format("q=(%s:(%s*))+AND+(object-type:%s)", attributeType.toString(), queryString, objectType.toString()),
                                createSearchOptions(attributeType));
        } catch (IOException e) {
            return badRequest("Query failed.");
        }

        final List<String> result = Lists.newArrayList();

        for (SearchResponse.Result.Doc doc : searchResponse.getResult().getDocs()) {
            for (SearchResponse.Str str : doc.getStrs()) {
                if (str.getName().equals(attributeType.toString())) {
                    result.add(str.getValue());
                }
            }
        }

        return ok(result);
    }

    // helper methods

    private SearchOptions createSearchOptions(final AttributeTypeParameter attributeType) {
        return new SearchOptions(
            MAXIMUM_SEARCH_RESULTS,
            new Sort(new SortField(FreeTextIndex.PRIMARY_KEY_FIELD_NAME, SortField.Type.STRING)),   // TODO: [ES] sort by attribute type and not object primary key
            QueryParser.Operator.AND
        );
    }

    private Response badRequest(final String message) {
        return Response.status(Response.Status.BAD_REQUEST).entity(message).build();
    }

    private Response ok(final Object message) {
        return Response.ok(message).build();
    }

}
