package net.ripe.db.whois.api.fulltextsearch;

import net.ripe.db.whois.api.rest.RestServiceHelper;
import net.ripe.db.whois.query.domain.QueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

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
            @Context final HttpServletRequest request) {
        try {
            return ok(search(
                    new SearchRequest.SearchRequestBuilder()
                            .setRows(rows)
                            .setStart(start)
                            .setQuery(query)
                            .setHighlight(highlight)
                            .setHighlightPre(highlightPre)
                            .setHighlightPost(highlightPost)
                            .setFormat(writerType)
                            .setFacet(facet)
                            .build(), request));
        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage());
        } catch (QueryException qe) {
            throw RestServiceHelper.createWebApplicationException(qe, request);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return internalServerError("Unexpected error");
        }
    }

    private Response ok(final SearchResponse searchResponse) {
        return Response.ok(searchResponse).build();
    }

    private Response badRequest(final String message) {
        return javax.ws.rs.core.Response.status(Response.Status.BAD_REQUEST).entity(message).build();
    }

    private Response internalServerError(final String message) {
        return javax.ws.rs.core.Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(message).build();
    }

    //
    // TODO: only search in possibly value fields, according to query string
    //
    public SearchResponse search(final SearchRequest searchRequest, final HttpServletRequest request) {
        try {
            return fulltextSearch.performSearch(searchRequest, request.getRemoteAddr());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
