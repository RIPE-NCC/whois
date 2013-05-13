package net.ripe.db.whois.common.dao.jdbc.index;

import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class IndexWithNameAndTypeTestPerson extends IndexTestBase {
    private IndexStrategy subject;

    @Before
    public void setUp() throws Exception {
        subject = IndexStrategies.get(AttributeType.PERSON);
    }

    @Test
    public void findSinglePersonByName() {
        databaseHelper.addObject("person:Denis Walker\nnic-hdl:DW-RIPE");

        List<RpslObjectInfo> result = subject.findInIndex(whoisTemplate, "Denis Walker");

        assertThat(result, hasSize(1));
        RpslObjectInfo rpslObject = result.get(0);
        assertThat(rpslObject.getObjectType(), is(ObjectType.PERSON));
        assertThat(rpslObject.getKey(), is("DW-RIPE"));
    }

    @Test
    public void findSinglePersonNotRole() {
        databaseHelper.addObject("person:Denis Walker\nnic-hdl:DW-RIPE");
        databaseHelper.addObject("role:Role Walker\nnic-hdl:RW-RIPE");

        List<RpslObjectInfo> result = subject.findInIndex(whoisTemplate, "Walker");

        assertThat(result, hasSize(1));
        RpslObjectInfo rpslObject = result.get(0);
        assertThat(rpslObject.getObjectType(), is(ObjectType.PERSON));
        assertThat(rpslObject.getKey(), is("DW-RIPE"));
    }

    @Test
    public void findTwoPersonsByName() {
        databaseHelper.addObject("person:Denis Walker\nnic-hdl:DW-RIPE");
        databaseHelper.addObject("person:Brian Walker\nnic-hdl:BW-RIPE");

        List<RpslObjectInfo> result = subject.findInIndex(whoisTemplate, "Walker");

        assertThat(result, hasSize(2));
        assertThat(result.get(0).getObjectType(), is(ObjectType.PERSON));
        assertThat(result.get(1).getObjectType(), is(ObjectType.PERSON));
        assertThat(result.get(0), is(not(result.get(1))));
    }

    @Test
    public void findTwoPersonsNotRole() {
        databaseHelper.addObject("person:Denis Walker\nnic-hdl:DW-RIPE");
        databaseHelper.addObject("person:Brian Walker\nnic-hdl:BW-RIPE");
        databaseHelper.addObject("role:Role Walker\nnic-hdl:RW-RIPE");

        List<RpslObjectInfo> result = subject.findInIndex(whoisTemplate, "Walker");

        assertThat(result, hasSize(2));
        assertThat(result.get(0).getObjectType(), is(ObjectType.PERSON));
        assertThat(result.get(1).getObjectType(), is(ObjectType.PERSON));
        assertThat(result.get(0), is(not(result.get(1))));
    }

    @Test
    public void searchNonexistentPerson() {
        assertThat(subject.findInIndex(whoisTemplate, "nonexistent"), hasSize(0));
    }
}
