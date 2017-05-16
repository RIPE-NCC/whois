package net.ripe.db.whois.common.rpsl;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class ObjectTypeTest {
    @Test
    public void getByName() {
        for (ObjectType objectType : ObjectType.values()) {
            assertThat(ObjectType.getByName(objectType.getName()), is(objectType));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void getByName_unknown() {
        ObjectType.getByName("UNKNOWN");
    }

    @Test
    public void getByNameOrNull() {
        for (ObjectType objectType : ObjectType.values()) {
            assertThat(ObjectType.getByNameOrNull(objectType.getName()), is(objectType));
        }
    }

    @Test
    public void getByNameOrNull_unknown() {
        assertNull(ObjectType.getByNameOrNull("UNKNOWN"));
    }
}
