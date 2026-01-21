package net.ripe.db.whois.rdap;

import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import jakarta.ws.rs.core.Response;
import net.ripe.db.whois.api.elasticsearch.ElasticIndexService;
import net.ripe.db.whois.api.elasticsearch.ElasticSearchAccountingCallback;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.query.acl.AccessControlListManager;
import net.ripe.db.whois.query.domain.QueryCompletionInfo;
import net.ripe.db.whois.query.domain.QueryException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static net.ripe.db.whois.api.fulltextsearch.ElasticFulltextSearch.SORT_BUILDERS;

@Component
public class RdapElasticFullTextSearchService implements RdapFullTextSearch {

    private final int maxResultSize;
    private final RpslObjectDao objectDao;
    private final ElasticIndexService elasticIndexService;
    private final AccessControlListManager accessControlListManager;
    private static final Set<String> EXACT_MATCH_SEARCH_FIELDS = Set.of("netname", "inetnum", "inet6num", "as-name", "aut-num");

    @Autowired
    public RdapElasticFullTextSearchService(@Qualifier("jdbcRpslObjectSlaveDao") final RpslObjectDao objectDao,
                                            final ElasticIndexService elasticIndexService,
                                            final AccessControlListManager accessControlListManager,
                                            @Value("${rdap.search.max.results:100}") final int maxResultSize) {
        this.elasticIndexService = elasticIndexService;
        this.maxResultSize = maxResultSize;
        this.objectDao = objectDao;
        this.accessControlListManager = accessControlListManager;
    }

    @Override
    public List<RpslObject> performSearch(final String[] fields, final String term, final String clientIp,
                                          final Source source) throws IOException {

        try {
            return new ElasticSearchAccountingCallback<List<RpslObject>>(accessControlListManager,  clientIp, null, source) {

                @Override
                protected List<RpslObject> doSearch() throws IOException {

                    final SearchRequest searchRequest = SearchRequest.of( s -> s
                            .index(elasticIndexService.getWhoisAliasIndex())
                            .query(getQueryBuilder(fields, term))
                            .size(maxResultSize)
                            .sort(SORT_BUILDERS)
                    );

                    final SearchResponse<Void> searchResponse = elasticIndexService.getClient().search(searchRequest, Void.class);
                    final List<RpslObject> results = new ArrayList<>();

                    searchResponse.hits().hits().forEach( hit -> {
                        try {
                            final RpslObject rpslObject = objectDao.getById(Integer.parseInt(hit.id()));
                            results.add(rpslObject);
                            account(rpslObject);

                        } catch (EmptyResultDataAccessException e) {
                            // object was deleted from the database but index was not updated yet
                        }
                    });
                    return results;
                }

                private Query getQueryBuilder(final String[] fields, final String term) {
                    if (hasWildCard(term)) {
                        return createWildCardQuery(fields, term);
                    }

                    if (isExactMatchSearch(fields)) {
                        return createExactMatchQuery(fields, term);
                    }

                    return Query.of(q -> q.multiMatch(m -> m
                            .query(term)
                            .fields(Arrays.asList(fields))
                            .type(TextQueryType.PhrasePrefix)
                            .operator(Operator.And)
                    ));
                }

                private boolean isExactMatchSearch(String[] fields) {
                    return EXACT_MATCH_SEARCH_FIELDS.containsAll(Stream.of(fields).toList());
                }

                private boolean hasWildCard(String term) {
                    return term.contains("*") || term.contains("?");
                }

                private Query createExactMatchQuery(String[] fields, String term) {
                    String lower = term.toLowerCase();
                    List<Query> shouldQueries = new ArrayList<>();

                    for (String field : fields) {
                        shouldQueries.add(Query.of(q -> q.term(t -> t
                                .field(field + ".lowercase")
                                .value(lower)
                        )));
                    }

                    return Query.of(q -> q.bool(b -> b.should(shouldQueries)));
                }

                private Query createWildCardQuery(String[] fields, String term) {
                    String lower = term.toLowerCase();
                    List<Query> shouldQueries = new ArrayList<>();

                    for (String field : fields) {
                        shouldQueries.add(Query.of(q -> q.wildcard(w -> w
                                .field(field + ".lowercase")
                                .value(lower)
                        )));
                    }

                    return Query.of(q -> q.bool(b -> b.should(shouldQueries)));
                }
            }.search();
        } catch (QueryException e){
            if ( e.getCompletionInfo() == QueryCompletionInfo.BLOCKED) {
                throw new RdapException("Too Many Requests", e.getMessage(), Response.Status.TOO_MANY_REQUESTS.getStatusCode());
            }
            throw new RdapException("Bad Request", e.getMessage(), Response.Status.BAD_REQUEST.getStatusCode());
        }
    }
}
