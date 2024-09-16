package net.ripe.db.whois.common.rpsl;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Test
    public void getByName_throws_on_unknown() {
        assertThrows(IllegalArgumentException.class, () -> {
            AttributeType.getByName("BOOOYAKAAAA!!!");
        });
    }

    @Test
    public void getByNameOrNull_supports_shortkeys() {
        assertThat(AttributeType.getByNameOrNull("*as"), is(AttributeType.AS_SET));
    }

    @Test
    public void name_transformations() {
        assertThat(AttributeType.MNTNER.getNameToFirstLower(), is("mntner"));
        assertThat(AttributeType.MNTNER.getNameToFirstUpper(), is("Mntner"));

        assertThat(AttributeType.MNT_BY.getNameToFirstLower(), is("mntByRef"));
        assertThat(AttributeType.MNT_BY.getNameToFirstUpper(), is("MntByRef") );

        assertThat(AttributeType.DEFAULT.getNameToFirstLower(), is("default_") );
        assertThat(AttributeType.DEFAULT.getNameToFirstUpper(), is("Default") );

        assertThat(AttributeType.INTERFACE.getNameToFirstLower(), is("interface_") );
        assertThat(AttributeType.INTERFACE.getNameToFirstUpper(), is("Interface") );

        assertThat(AttributeType.IMPORT.getNameToFirstLower(), is("import_") );
        assertThat(AttributeType.IMPORT.getNameToFirstUpper(), is("Import") );
    }
}
