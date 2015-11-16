package net.ripe.db.whois.common.rpsl;

import net.ripe.db.whois.common.rpsl.attrs.toggles.ChangedAttrFeatureToggle;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertNull;

public class AttributeTypeWithoutChangedTest {
    @BeforeClass
    public static void beforeClass() throws Exception {
        new ObjectTemplateProvider(new ChangedAttrFeatureToggle(false));
    }

    @Test
    public void getChangedByNameOrNullIsNull() {
        assertNull(AttributeType.getByNameOrNull(AttributeType.CHANGED.getName()));
    }
}
