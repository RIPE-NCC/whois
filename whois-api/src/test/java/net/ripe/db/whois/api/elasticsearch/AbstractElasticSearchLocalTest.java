package net.ripe.db.whois.api.elasticsearch;

import net.ripe.db.whois.api.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

//TODO[MA]: Setting up gitlab to use test container was not working as docker:dind was not starting up properly. It requires some config changes to runners
@Testcontainers
public class AbstractElasticSearchLocalTest extends AbstractIntegrationTest {

    @Container
    public static ElasticsearchContainer elasticsearchContainer = new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:7.15.0");

    @BeforeAll
    public static void startElastic() {
        System.setProperty("elastic.host", elasticsearchContainer.getHttpHostAddress().split(":")[0]);
        System.setProperty("elastic.port", elasticsearchContainer.getHttpHostAddress().split(":")[1]);
    }
}
