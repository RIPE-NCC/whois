package net.ripe.db.whois.api.fulltextsearch;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.ripe.db.whois.api.rest.RestServiceHelper;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.sso.AuthServiceClient;
import net.ripe.db.whois.query.domain.QueryException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;

import static net.ripe.db.whois.api.elasticsearch.ElasticIndexService.OBJECT_TYPE_FIELD_NAME;

@Component
@Path("/fulltextsearch")
public class FullTextSearchService {
    private static final Logger LOGGER = LoggerFactory.getLogger(FullTextSearchService.class);

    private final FulltextSearch fulltextSearch;

    @Autowired
    public FullTextSearchService(final FulltextSearch fulltextSearch) {
        this.fulltextSearch = fulltextSearch;
    }

    @GET
    @Path("/select")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response search(
            @QueryParam("q") final String query,
            @QueryParam("rows") @DefaultValue("10") final String rows,
            @QueryParam("start") @DefaultValue("0") final String start,
            @QueryParam("hl") @DefaultValue("false") final String highlight,
            @QueryParam("hl.simple.pre") @DefaultValue("<b>") final String highlightPre,
            @QueryParam("hl.simple.post") @DefaultValue("</b>") final String highlightPost,
            @QueryParam("wt") @DefaultValue("xml") final String writerType,
            @QueryParam("facet") @DefaultValue("false") final String facet,
            @CookieParam(AuthServiceClient.TOKEN_KEY) final String crowdTokenKey,
            @Context final HttpServletRequest request) {
        try {
            return ok(search(
                    new SearchRequest.SearchRequestBuilder()
                            .setRows(rows)
                            .setStart(start)
                            .setQuery(escapeColon(query))
                            .setHighlight(highlight)
                            .setHighlightPre(highlightPre)
                            .setHighlightPost(highlightPost)
                            .setFormat(writerType)
                            .setFacet(facet)
                            .build(), crowdTokenKey, request));
        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage());
        } catch (QueryException qe) {
            throw RestServiceHelper.createWebApplicationException(qe, request);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return internalServerError("Unexpected error");
        }
    }

    private static String escapeColon(final String query){
        if (StringUtils.isEmpty(query) || !query.contains(":") || query.contains("\\:")){
            return query;
        }

        final StringBuilder sb = new StringBuilder();

        final String[] splittedQuery = query.split(":");
        Arrays.stream(splittedQuery).limit(splittedQuery.length - 1).forEach(splitColon -> sb.append(splitColon).append(shouldEscapeColon(splitColon) ? "\\:" : ":"));
        return sb.append(splittedQuery[splittedQuery.length - 1]).toString();
    }

    private static boolean shouldEscapeColon(final String splitColon) {
        final String[] words = splitColon.split("[^\\w-]+"); //Split in words (including dashes)
        return words.length == 0 || (AttributeType.getByNameOrNull(words[words.length - 1]) == null && !OBJECT_TYPE_FIELD_NAME.equalsIgnoreCase(words[words.length - 1]));
    }

    private Response ok(final SearchResponse searchResponse) {
        return Response.ok(searchResponse).build();
    }

    private Response badRequest(final String message) {
        return Response.status(Response.Status.BAD_REQUEST).entity(message).build();
    }

    private Response internalServerError(final String message) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(message).build();
    }

    //
    // TODO: only search in possibly value fields, according to query string
    //
    public SearchResponse search(final SearchRequest searchRequest, final String ssoToken, final HttpServletRequest request) {
        try {
            return fulltextSearch.performSearch(searchRequest, ssoToken, request.getRemoteAddr());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
