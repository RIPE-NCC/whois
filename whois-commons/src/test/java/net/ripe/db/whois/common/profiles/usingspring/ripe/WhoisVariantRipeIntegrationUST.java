package net.ripe.db.whois.common.profiles.usingspring.ripe;

import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.profiles.usingspring.ModifiedAbstractDatabaseHelperTest;
import net.ripe.db.whois.common.profiles.usingspring.WhoisVariantBeanApnic;
import net.ripe.db.whois.common.profiles.usingspring.WhoisVariantBeanRipe;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.StringUtils;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@ContextConfiguration(locations = {"classpath:applicationContext-commons-test-usingprofiles.xml"})
@Category(IntegrationTest.class)
public class WhoisVariantRipeIntegrationUST extends ModifiedAbstractDatabaseHelperTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(WhoisVariantRipeIntegrationUST.class);

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @BeforeClass
    public static void beforeClass() throws Exception {
        LOGGER.info(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME + " [" + System.getProperty(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME) + "]");
    }

    @Test
    public void load_apnic_bean_fail() {
        exception.expect(NoSuchBeanDefinitionException.class);
        WhoisVariantBeanApnic whoisVariantBeanApnic = (WhoisVariantBeanApnic)applicationContext.getBean(StringUtils.uncapitalize(WhoisVariantBeanApnic.class.getSimpleName()));
    }

    @Test
    public void load_ripe_bean_success() {
        WhoisVariantBeanRipe whoisVariantBeanRipe = (WhoisVariantBeanRipe)applicationContext.getBean(StringUtils.uncapitalize(WhoisVariantBeanRipe.class.getSimpleName()));
        assertTrue(whoisVariantBeanRipe.isRipe());
        assertNotNull(whoisVariantBeanRipe.getUpdateDao());
    }

}
