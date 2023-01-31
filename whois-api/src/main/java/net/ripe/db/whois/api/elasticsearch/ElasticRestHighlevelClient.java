package net.ripe.db.whois.api.elasticsearch;

import org.elasticsearch.client.RestHighLevelClient;

public interface ElasticRestHighlevelClient {

    RestHighLevelClient getClient();
}
