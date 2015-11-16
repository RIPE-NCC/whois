package net.ripe.db.whois.common.rpsl;


import net.ripe.db.whois.common.rpsl.attrs.toggles.ChangedAttrFeatureToggle;
import org.junit.BeforeClass;

public abstract class ObjectTemplateDependentTest {

    @BeforeClass
    public static void beforeClass() throws Exception {
        new ObjectTemplateProvider(new ChangedAttrFeatureToggle(true));
    }

}
