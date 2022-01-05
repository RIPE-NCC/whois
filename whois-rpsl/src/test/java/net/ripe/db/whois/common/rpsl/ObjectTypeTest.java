package net.ripe.db.whois.common.rpsl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ObjectTypeTest {
    @Test
    public void getByName() {
        for (ObjectType objectType : ObjectType.values()) {
            assertThat(ObjectType.getByName(objectType.getName()), is(objectType));
        }
    }

    @Test
    public void getByName_unknown() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
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
        assertNull(ObjectType.getByNameOrNull("UNKNOWN"));
    }
}
