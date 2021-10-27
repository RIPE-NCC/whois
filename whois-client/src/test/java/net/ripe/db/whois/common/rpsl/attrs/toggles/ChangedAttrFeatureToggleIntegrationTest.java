package net.ripe.db.whois.common.rpsl.attrs.toggles;

import net.ripe.db.whois.common.IntegrationTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import static org.junit.Assert.assertEquals;

@Category(IntegrationTest.class)
@ContextConfiguration(locations = {"classpath:applicationContext-toggles-test.xml"})
public class ChangedAttrFeatureToggleIntegrationTest extends AbstractJUnit4SpringContextTests {

    private static final Boolean TOGGLE_VALUE = Boolean.TRUE;

    @BeforeAll
    public synchronized static void beforeClass() {
        System.setProperty("feature.toggle.changed.attr.available", TOGGLE_VALUE.toString());
    }

    @Test
    public void changedIsAvailable() {
        assertEquals(TOGGLE_VALUE, ChangedAttrFeatureToggle.isChangedAttrAvailable());
    }


}
