package net.ripe.db.whois.common.rpsl;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ObjectTypeTest {
    @Test
    public void getByName() {
        for (ObjectType objectType : ObjectType.values()) {
            assertThat(ObjectType.getByName(objectType.getName()), is(objectType));
        }
    }

    @Test
    public void getByName_unknown() {
        assertThrows(IllegalArgumentException.class, () -> {
            ObjectType.getByName("UNKNOWN");
        });
    }

    @Test
    public void getByNameOrNull() {
        for (ObjectType objectType : ObjectType.values()) {
            assertThat(ObjectType.getByNameOrNull(objectType.getName()), is(objectType));
        }
    }

    @Test
    public void getByNameOrNull_unknown() {
        assertThat(ObjectType.getByNameOrNull("UNKNOWN"), is(nullValue()));
    }
}
