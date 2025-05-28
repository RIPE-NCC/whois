package net.ripe.db.whois.common.dao.jdbc.domain;

import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.common.rpsl.ObjectType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ObjectTypeIdsTest {

    @Test
    public void getBySerialType() {
        for (Integer objectTypeId : ImmutableList.of(0, 1, 2, 3, 4, 5, 6, 7, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21)) {
            assertThat(ObjectTypeIds.getType(objectTypeId), Matchers.instanceOf(ObjectType.class));
        }
    }

    @Test
    public void getBySerialType_unknown() {
        assertThrows(IllegalArgumentException.class, () -> {
            ObjectTypeIds.getType(-1000);
        });
    }
}
