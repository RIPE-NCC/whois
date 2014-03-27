package net.ripe.db.whois.common.rpsl;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class AttributeTypeTest {
    @Test
    public void getByName() {
        for (AttributeType attributeType : AttributeType.values()) {
            assertThat("by name " + attributeType.toString(), AttributeType.getByName(attributeType.getName()), is(attributeType));
            assertThat("by flag " + attributeType.toString(), AttributeType.getByName(attributeType.getFlag()), is(attributeType));
        }
    }

    @Test
    public void getByNameOrNull() {
        for (AttributeType attributeType : AttributeType.values()) {
            assertThat("by name " + attributeType.toString(), AttributeType.getByNameOrNull(attributeType.getName()), is(attributeType));
            assertThat("by flag " + attributeType.toString(), AttributeType.getByNameOrNull(attributeType.getFlag()), is(attributeType));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void getByName_throws_on_unknown() {
        AttributeType.getByName("BOOOYAKAAAA!!!");
    }

    @Test
    public void getByNameOrNull_supports_shortkeys() {
        assertThat(AttributeType.getByNameOrNull("*as"), is(AttributeType.AS_SET));
    }
}
