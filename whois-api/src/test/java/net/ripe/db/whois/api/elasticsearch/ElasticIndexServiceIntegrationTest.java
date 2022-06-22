package net.ripe.db.whois.api.elasticsearch;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Uninterruptibles;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@org.junit.jupiter.api.Tag("ElasticSearchTest")
public class ElasticIndexServiceIntegrationTest extends AbstractElasticSearchIntegrationTest {

    private static final String WHOIS_INDEX = "whois";
    private static final String METADATA_INDEX = "metadata";

    private static final RpslObject RPSL_MNT_PERSON = new RpslObject(2, ImmutableList.of(new RpslAttribute("person", "first person name"), new RpslAttribute("nic-hdl", "P1")));

    @Test
    public void addThenCountAndThenDeleteByEntry() throws IOException {
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
    public void isEnabledWhenIndicesExist() {
        assertTrue(elasticIndexService.isEnabled());
    }

    @Test
    public void updateAndGetMetadata() throws IOException {
        deleteAll();
        Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);

        ElasticIndexMetadata elasticIndexMetadata = new ElasticIndexMetadata(1, "RIPE");
        assertNull(elasticIndexService.getMetadata());
        elasticIndexService.updateMetadata(elasticIndexMetadata);
        ElasticIndexMetadata retrievedMetaData = elasticIndexService.getMetadata();
        assertEquals(1L, retrievedMetaData.getSerial().longValue());
        assertEquals("RIPE", retrievedMetaData.getSource());
    }

    @Test
    public void should_not_throw_error_invalid_objectType_history() throws IOException {
        elasticIndexService.addEntry(RPSL_MNT_PERSON);
        ElasticIndexMetadata elasticIndexMetadata = new ElasticIndexMetadata(1, "RIPE");
        elasticIndexService.updateMetadata(elasticIndexMetadata);

        Uninterruptibles.sleepUninterruptibly(2, TimeUnit.SECONDS);
        // one document after adding
        assertEquals(elasticIndexService.getWhoisDocCount(), 1);

         whoisTemplate.update("INSERT INTO serials "
                        + " (serial_id, object_id, sequence_id, atlast, operation) "
                        + " VALUES "
                        + " (2, 1880251, 1, 0, 1)");

        whoisTemplate.update("INSERT INTO last "
                + " (object_id, sequence_id, object, object_type, pkey) "
                + " VALUES "
                + " (1880251, 0, '', 8,'LIM-WEBUPDATES')");

        whoisTemplate.update("INSERT INTO history "
                + " (object_id, sequence_id, object_type, object, pkey) "
                + " VALUES "
                + " (1880251, 1, 8,'limerick:     LIM-WEBUPDATES\n" +
                                    "changed:      limerick-dbm@ripe.net 20021107\n" +
                                    "source:       RIPE', 'LIM-WEBUPDATES')");

        rebuildIndex();
        assertEquals(elasticIndexService.getWhoisDocCount(), 1);
    }

    @Override
    public String getWhoisIndex() {
        return WHOIS_INDEX;
    }

    @Override
    public String getMetadataIndex() {
        return METADATA_INDEX;
    }

}
