package net.ripe.db.whois.common.dao.jdbc.index;


import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@Tag("IntegrationTest")
public class IndexWithNameIntegrationTest extends IndexIntegrationTestBase {
    private IndexStrategy subject;

    @BeforeEach
    public void setUp() throws Exception {
        subject = IndexStrategies.get(AttributeType.ORG_NAME);
    }

    @Test
    public void findOrganisationByName() {
        databaseHelper.addObject("organisation:ORG-ZV1-RIPE\norg-name:ZOO");

        List<RpslObjectInfo> result = subject.findInIndex(new JdbcTemplate(sourceAwareDataSource), "ZOO");

        assertThat(result, hasSize(1));
        RpslObjectInfo rpslObject = result.get(0);
        assertThat(rpslObject.getObjectType(), is(ObjectType.ORGANISATION));
        assertThat(rpslObject.getKey(), is("ORG-ZV1-RIPE"));
    }

    @Test
    public void findTwoOrganisationsByName() {
        databaseHelper.addObject("organisation:ORG-ZV1-RIPE\norg-name:ZOO");
        databaseHelper.addObject("organisation:ORG-ZV2-RIPE\norg-name:ZOO");

        List<RpslObjectInfo> result = subject.findInIndex(new JdbcTemplate(sourceAwareDataSource), "ZOO");

        assertThat(result, hasSize(2));
        assertThat(result.get(0).getObjectType(), is(ObjectType.ORGANISATION));
        assertThat(result.get(1).getObjectType(), is(ObjectType.ORGANISATION));
        assertThat(result.get(0), is(not(result.get(1))));
    }

    @Test
    public void findOneOrganisationTwoNames() {
        databaseHelper.addObject("organisation:ORG-ML199-RIPE\norg-name:Moo Less.");

        List<RpslObjectInfo> result = subject.findInIndex(new JdbcTemplate(sourceAwareDataSource), "Moo Less.");

        assertThat(result, hasSize(1));
        assertThat(result.get(0).getKey(), is("ORG-ML199-RIPE"));
    }

    @Test
    public void searchNonexistentOrganisationByName() {
        assertThat(subject.findInIndex(new JdbcTemplate(sourceAwareDataSource), "nonexistent"), hasSize(0));
    }
}
