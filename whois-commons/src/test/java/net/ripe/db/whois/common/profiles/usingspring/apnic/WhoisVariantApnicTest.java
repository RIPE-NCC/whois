package net.ripe.db.whois.common.profiles.usingspring.apnic;

import net.ripe.db.whois.common.profiles.usingspring.StaticInitExample;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.AbstractEnvironment;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WhoisVariantApnicTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(WhoisVariantApnicTest.class);

    @BeforeClass
    public static void beforeClass() throws Exception {
        LOGGER.info(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME + " [" + System.getProperty(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME) + "]");
    }

    @Test
    public void init_apnic_config() {
        assertTrue(StaticInitExample.isAPNIC());
    }

    @Test
    public void init_ripe_config() {
        assertFalse(StaticInitExample.isRIPE());
    }


}
