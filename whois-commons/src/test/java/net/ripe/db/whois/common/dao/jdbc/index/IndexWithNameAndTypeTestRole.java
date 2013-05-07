package net.ripe.db.whois.common.dao.jdbc.index;

import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class IndexWithNameAndTypeTestRole extends IndexTestBase {
    private IndexStrategy subject;

    @Before
    public void setUp() throws Exception {
        subject = IndexStrategies.get(AttributeType.ROLE);
    }

    @Test
    public void findSingleRoleByNameOneWord() {
        databaseHelper.addObject("role:RIPE DBM\nnic-hdl:RD2964-RIPE");

        List<RpslObjectInfo> result = subject.findInIndex(whoisTemplate, "RIPE");

        assertThat(result, hasSize(1));

        RpslObjectInfo rpslObject = result.get(0);
        assertThat(rpslObject.getObjectType(), is(ObjectType.ROLE));
        assertThat(rpslObject.getKey(), is("RD2964-RIPE"));
    }

    @Test
    public void findSingleRoleByNameTwoWords() {

        databaseHelper.addObject("role:RIPE DBM\nnic-hdl:RD2964-RIPE");
        databaseHelper.addObject("role:RIPE BA\nnic-hdl:BA-RIPE");

        List<RpslObjectInfo> result = subject.findInIndex(whoisTemplate, "RIPE BA");

        assertThat(result, hasSize(1));
        RpslObjectInfo rpslObject = result.get(0);

        assertThat(rpslObject.getObjectType(), is(ObjectType.ROLE));
        assertThat(rpslObject.getKey(), is("BA-RIPE"));
    }

    @Test
    public void findTwoRolesByName() {
        databaseHelper.addObject("role:RIPE DBM\nnic-hdl:RD2964-RIPE");
        databaseHelper.addObject("role:RIPE BA\nnic-hdl:BA-RIPE");

        List<RpslObjectInfo> result = subject.findInIndex(whoisTemplate, "RIPE");

        assertThat(result, hasSize(2));
        assertThat(result.get(0).getObjectType(), is(ObjectType.ROLE));
        assertThat(result.get(1).getObjectType(), is(ObjectType.ROLE));
        assertThat(result.get(0), is(not(result.get(1))));
    }

    @Test
    public void findSingleRoleManyNames() {
        databaseHelper.addObject("role: the quick brown fox jumped over lazy dog\nnic-hdl:RD1-RIPE");

        List<RpslObjectInfo> result = subject.findInIndex(whoisTemplate, "the quick brown fox jumped over lazy dog");

        assertThat(result, hasSize(1));
        assertThat(result.get(0).getKey(), is("RD1-RIPE"));
    }

    @Test
    public void searchNonexistentRoleByName() {
        assertThat(subject.findInIndex(whoisTemplate, "nonexistent"), hasSize(0));
    }
}
