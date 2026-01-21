package net.ripe.db.whois.api.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;

public interface ElasticRestHighlevelClient {

    ElasticsearchClient getClient();
}
