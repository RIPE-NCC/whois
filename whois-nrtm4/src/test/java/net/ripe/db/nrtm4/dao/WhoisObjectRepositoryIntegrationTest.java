/*
package net.ripe.db.nrtm4.dao;

import net.ripe.db.nrtm4.AbstractNrtm4IntegrationBase;
import net.ripe.db.nrtm4.domain.WhoisObjectData;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


@Tag("IntegrationTest")
public class WhoisObjectRepositoryIntegrationTest extends AbstractNrtm4IntegrationBase {

    @Autowired
    WhoisObjectRepository whoisObjectRepository;

    @Test
    public void prepared_query_gets_all_rows() {
        loadScripts(whoisTemplate, "nrtm_sample_sm.sql");

        final var objects = List.of(
            new WhoisObjectData(11044887, 1),
            new WhoisObjectData(11044888, 1),
            new WhoisObjectData(5158, 2)
        );
        final var map = whoisObjectRepository.findRpslMapForObjects(objects);
        assertThat(map.size(), is(3));
    }

}
*/
