package net.ripe.db.whois.api.elasticsearch;


import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Uninterruptibles;
import net.ripe.db.whois.common.elasticsearch.ElasticIndexMetadata;
import net.ripe.db.whois.common.elasticsearch.ElasticIndexService;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@org.junit.jupiter.api.Tag("ElasticSearchTest")
public class ElasticIndexServiceIntegrationTest extends AbstractElasticSearchIntegrationTest {

    private static final String WHOIS_INDEX = "whois";
    private static final String METADATA_INDEX = "metadata";

    private static RestHighLevelClient esClient;
    private static final RpslObject RPSL_MNT_PERSON = new RpslObject(2, ImmutableList.of(new RpslAttribute("person", "first person name"), new RpslAttribute("nic-hdl", "P1")));

    @BeforeAll
    public static void startElastic() {
        esClient = testClient();
    }

    @BeforeEach
    public void setUp() throws IOException {
        createWhoisIndex(esClient);
        createMetadataIndex(esClient);
    }

    @AfterEach
    public void tearDown() throws IOException {
        deleteWhoisIndex(esClient);
        deleteMetadataIndex(esClient);
    }

    @Test
    public void addThenCountAndThenDeleteByEntry() throws IOException {
        ElasticIndexService elasticIndexService = getElasticIndexService(WHOIS_INDEX, METADATA_INDEX);
        long whoisDocCount = elasticIndexService.getWhoisDocCount();
        // No document in index
        assertEquals(whoisDocCount, 0);
        elasticIndexService.addEntry(RPSL_MNT_PERSON);
        Uninterruptibles.sleepUninterruptibly(2, TimeUnit.SECONDS);
        whoisDocCount = elasticIndexService.getWhoisDocCount();
        // one document after adding
        assertEquals(whoisDocCount, 1);
        elasticIndexService.deleteEntry(RPSL_MNT_PERSON.getObjectId());
        Uninterruptibles.sleepUninterruptibly(2, TimeUnit.SECONDS);
        whoisDocCount = elasticIndexService.getWhoisDocCount();
        // no document in index after deleting
        assertEquals(whoisDocCount, 0);
    }

    @Test
    public void addThenCountAndThenDeleteAll() throws IOException {
        ElasticIndexService elasticIndexService = getElasticIndexService(WHOIS_INDEX, METADATA_INDEX);
        long whoisDocCount = elasticIndexService.getWhoisDocCount();
        // No document in index
        assertEquals(whoisDocCount, 0);
        elasticIndexService.addEntry(RPSL_MNT_PERSON);
        Uninterruptibles.sleepUninterruptibly(2, TimeUnit.SECONDS);
        whoisDocCount = elasticIndexService.getWhoisDocCount();
        // one document after adding
        assertEquals(whoisDocCount, 1);
        elasticIndexService.deleteAll();
        Uninterruptibles.sleepUninterruptibly(2, TimeUnit.SECONDS);
        whoisDocCount = elasticIndexService.getWhoisDocCount();
        // no document in index after deleting
        assertEquals(whoisDocCount, 0);
    }

    @Test
    public void isEnabledWhenWhoisIndexDoesNotExist() throws IOException {
        ElasticIndexService elasticIndexService = getElasticIndexService(WHOIS_INDEX, METADATA_INDEX);
        deleteWhoisIndex(esClient);
        assertFalse(elasticIndexService.isEnabled());
    }

    @Test
    public void isEnabledWhenMetadataIndexDoesNotExist() throws IOException {
        ElasticIndexService elasticIndexService = getElasticIndexService(WHOIS_INDEX, METADATA_INDEX);
        deleteMetadataIndex(esClient);
        assertFalse(elasticIndexService.isEnabled());
    }

    @Test
    public void isEnabledWhenIndexIsNotRunning() throws IOException {
        ElasticIndexService elasticIndexService = new ElasticIndexService("host", 12345, WHOIS_INDEX, METADATA_INDEX);
        assertFalse(elasticIndexService.isEnabled());
    }

    @Test
    public void isEnabledWhenIndicesExist() {
        ElasticIndexService elasticIndexService = getElasticIndexService(WHOIS_INDEX, METADATA_INDEX);
        assertTrue(elasticIndexService.isEnabled());
    }

    @Test
    public void updateAndGetMetadata() throws IOException {
        ElasticIndexService elasticIndexService = getElasticIndexService(WHOIS_INDEX, METADATA_INDEX);
        ElasticIndexMetadata elasticIndexMetadata = new ElasticIndexMetadata(1, "RIPE");
        assertNull(elasticIndexService.getMetadata());
        elasticIndexService.updateMetadata(elasticIndexMetadata);
        ElasticIndexMetadata retrievedMetaData = elasticIndexService.getMetadata();
        assertEquals(1L, retrievedMetaData.getSerial().longValue());
        assertEquals("RIPE", retrievedMetaData.getSource());
    }

    private static void createWhoisIndex(RestHighLevelClient esClient) throws IOException {
        CreateIndexRequest whoisRequest = new CreateIndexRequest(WHOIS_INDEX);
        esClient.indices().create(whoisRequest, RequestOptions.DEFAULT);
    }

    private static void createMetadataIndex(RestHighLevelClient esClient) throws IOException {
        CreateIndexRequest whoisRequest = new CreateIndexRequest(METADATA_INDEX);
        esClient.indices().create(whoisRequest, RequestOptions.DEFAULT);
    }

    private void deleteWhoisIndex(RestHighLevelClient esClient) throws IOException {
        try {
            DeleteIndexRequest whoisRequest = new DeleteIndexRequest(WHOIS_INDEX);
            esClient.indices().delete(whoisRequest, RequestOptions.DEFAULT);
        } catch (Exception ignored) {
        }
    }

    private void deleteMetadataIndex(RestHighLevelClient esClient) throws IOException {
        try {
            DeleteIndexRequest metadataRequest = new DeleteIndexRequest(METADATA_INDEX);
            esClient.indices().delete(metadataRequest, RequestOptions.DEFAULT);
        } catch (Exception ignored) {
        }
    }
}
