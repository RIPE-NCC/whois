package net.ripe.db.whois.api.rdap;

import net.ripe.db.whois.api.autocomplete.ElasticSearchCondition;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.elasticsearch.ElasticIndexService;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.query.QueryMessages;
import com.google.common.net.InetAddresses;
import net.ripe.db.whois.query.acl.AccessControlListManager;
import net.ripe.db.whois.query.domain.QueryCompletionInfo;
import net.ripe.db.whois.query.domain.QueryException;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@Conditional(ElasticSearchCondition.class)
public class RdapElasticFullTextSearchService implements RdapFullTextSearch {

    private final int maxResultSize;
    private final RpslObjectDao objectDao;
    private final ElasticIndexService elasticIndexService;
    private final AccessControlListManager accessControlListManager;

    @Autowired
    public RdapElasticFullTextSearchService( @Qualifier("jdbcRpslObjectSlaveDao") final RpslObjectDao objectDao,
                                             final ElasticIndexService elasticIndexService,
                                             final AccessControlListManager accessControlListManager,
                                             @Value("${rdap.search.max.results:100}") final int maxResultSize) {
        this.elasticIndexService = elasticIndexService;
        this.maxResultSize = maxResultSize;
        this.objectDao = objectDao;
        this.accessControlListManager = accessControlListManager;
    }

    @Override
    public List<RpslObject> performSearch(final String[] fields, final String term, final String clientIp, final Source source) throws IOException {

        final InetAddress remoteAddress = InetAddresses.forString(clientIp);
        checkBlocked(remoteAddress);

        int accountingLimit = -1;
        int accountedObjects = 0;

       try {

           final SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
           sourceBuilder.query(getQueryBuilder(fields, term));
           sourceBuilder.size(maxResultSize);
           sourceBuilder.sort(Arrays.asList(SortBuilders.scoreSort(), SortBuilders.fieldSort("primary-key.keyword")));

           final SearchRequest searchRequest = new SearchRequest(elasticIndexService.getWHOIS_INDEX());
           searchRequest.source(sourceBuilder);

           final SearchResponse searchResponse = elasticIndexService.getClient().search(searchRequest, RequestOptions.DEFAULT);
           SearchHit[] hits = searchResponse.getHits().getHits();

           final List<RpslObject> results = new ArrayList<>();

           for (final SearchHit hit : hits) {
               final RpslObject rpslObject;
               try {
                   rpslObject = objectDao.getById(Integer.parseInt(hit.getId()));

                   if (!accessControlListManager.isUnlimited(remoteAddress) && accessControlListManager.requiresAcl(rpslObject, source)) {
                       if (accountingLimit == -1) {
                           accountingLimit = accessControlListManager.getPersonalObjects(remoteAddress);
                       }

                       if (++accountedObjects > accountingLimit) {
                           throw new QueryException(QueryCompletionInfo.BLOCKED, QueryMessages.accessDeniedTemporarily(remoteAddress));
                       }
                   }

                   results.add(rpslObject);

               } catch (EmptyResultDataAccessException e) {
                   // object was deleted from the database but index was not updated yet
                   continue;
               }
           }

           return results;
       } finally {
           if (!accessControlListManager.isUnlimited(remoteAddress) && accountedObjects > 0) {
               accessControlListManager.accountPersonalObjects(remoteAddress, accountedObjects);
           }
       }
    }

    private void checkBlocked(InetAddress remoteAddress) {
        if (accessControlListManager.isDenied(remoteAddress)) {
            throw new QueryException(QueryCompletionInfo.BLOCKED, QueryMessages.accessDeniedPermanently(remoteAddress));
        } else if (!accessControlListManager.canQueryPersonalObjects(remoteAddress)) {
            throw new QueryException(QueryCompletionInfo.BLOCKED, QueryMessages.accessDeniedTemporarily(remoteAddress));
        }
    }

    private QueryBuilder getQueryBuilder(final String[] fields, final String term) {

        if(term.indexOf('*') == -1 && term.indexOf('?') == -1 ) {
            final MultiMatchQueryBuilder multiMatchQuery = new MultiMatchQueryBuilder(term, fields)
                    .type(MultiMatchQueryBuilder.Type.PHRASE_PREFIX)
                    .operator(Operator.AND);
            return multiMatchQuery;
        }

        final BoolQueryBuilder wildCardBuilder = QueryBuilders.boolQuery();
        for (String field: fields) {
            wildCardBuilder.should(QueryBuilders.wildcardQuery(String.format("%s.keyword", field), term));
        }

        return wildCardBuilder;
    }
}
