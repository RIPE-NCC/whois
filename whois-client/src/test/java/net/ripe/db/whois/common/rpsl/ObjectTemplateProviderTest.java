package net.ripe.db.whois.common.rpsl;

import net.ripe.db.whois.common.rpsl.attrs.toggles.ChangedAttrFeatureToggle;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.*;

public class ObjectTemplateProviderTest {

    @Test
    public void provideObjectTemplateWithChanged() {
        final boolean changedIsAvailable = true;
        initObjectTemplateProvider(changedIsAvailable);

        final ObjectTemplate template = ObjectTemplateProvider.getTemplate(ObjectType.AS_BLOCK);
        assertThat(template, instanceOf(ObjectTemplateWithChanged.class));
    }

    @Test
    public void provideObjectTemplateWithoutChanged() {
        final boolean changedIsAvailable = false;
        initObjectTemplateProvider(changedIsAvailable);
        final ObjectTemplate template = ObjectTemplateProvider.getTemplate(ObjectType.AS_BLOCK);
        assertThat(template, instanceOf(ObjectTemplateWithoutChanged.class));
    }

    @NotNull
    private void initObjectTemplateProvider(boolean changedIsAvailable) {
        final ChangedAttrFeatureToggle changedAttrFeatureToggle = new ChangedAttrFeatureToggle(changedIsAvailable);
        new ObjectTemplateProvider(changedAttrFeatureToggle);
    }

}
