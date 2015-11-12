package net.ripe.db.whois.common;

import net.ripe.db.whois.common.profiles.WhoisProfile;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import static org.junit.Assert.assertEquals;

@Category(IntegrationTest.class)
@ActiveProfiles(WhoisProfile.TEST)
@ContextConfiguration(locations = {"classpath:applicationContext-commons-test.xml"})
public class ChangedAttrFeatureToggleIntegrationTest extends AbstractJUnit4SpringContextTests {

    private static final Boolean TOGGLE_VALUE = Boolean.TRUE;

    @Autowired
    private ChangedAttrFeatureToggle toggle;

    @BeforeClass
    public synchronized static void beforeClass() {
        System.setProperty("feature.toggle.changed.attr.available", TOGGLE_VALUE.toString());
    }

    @Test
    public void changedIsAvailable() {
        assertEquals(TOGGLE_VALUE, toggle.isChangedAttrAvailable());
    }
}
