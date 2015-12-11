package net.ripe.db.whois.common.rpsl;

import net.ripe.db.whois.common.rpsl.attrs.toggles.ChangedAttrFeatureToggle;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

public class ObjectTemplateProviderWithoutChangeTest {

    @BeforeClass
    public static void beforeClass() {
        new ObjectTemplateProvider(new ChangedAttrFeatureToggle(false));
    }

    @Test
    public void provideObjectTemplate() {
        final ObjectTemplate templateWithout = ObjectTemplateProvider.getTemplate(ObjectType.AS_BLOCK);
        assertThat(templateWithout, instanceOf(ObjectTemplateWithoutChanged.class));
    }


}
