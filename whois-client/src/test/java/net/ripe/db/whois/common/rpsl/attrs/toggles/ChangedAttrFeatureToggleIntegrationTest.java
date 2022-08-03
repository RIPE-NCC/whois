package net.ripe.db.whois.common.rpsl.attrs.toggles;

import net.ripe.db.whois.api.rest.AbstractJUnit5SpringContextTests;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("IntegrationTest")
@ContextConfiguration(locations = {"classpath:applicationContext-toggles-test.xml"})
public class ChangedAttrFeatureToggleIntegrationTest extends AbstractJUnit5SpringContextTests {

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
