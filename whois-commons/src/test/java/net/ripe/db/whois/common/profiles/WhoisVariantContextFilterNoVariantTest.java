package net.ripe.db.whois.common.profiles;

import net.ripe.db.whois.common.dao.jdbc.AbstractDatabaseHelperTest;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.StringUtils;

import static org.junit.Assert.assertNotNull;

@ContextConfiguration(locations = {"classpath:applicationContext-commons-test.xml"})
public class WhoisVariantContextFilterNoVariantTest extends AbstractDatabaseHelperTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(WhoisVariantContextFilterNoVariantTest.class);

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @BeforeClass
    public static void beforeClass() throws Exception {
        WhoisVariant.setWhoIsVariant(WhoisVariant.Type.NONE);
        LOGGER.info("Setting whois variant [" + WhoisVariant.getWhoIsVariant() + "]");
    }

    @Test
    public void load_expected_bean() {
        exception.expect(NoSuchBeanDefinitionException.class);
        WhoisVariantBeanIncludeWhen whoisVariantBeanIncludeWhen = (WhoisVariantBeanIncludeWhen)applicationContext.getBean(StringUtils.uncapitalize(WhoisVariantBeanIncludeWhen.class.getSimpleName()));
    }

    @Test
    public void try_load_unexpected_bean() {
        WhoisVariantBeanExcludeWhen whoisVariantBeanExcludeWhen = (WhoisVariantBeanExcludeWhen)applicationContext.getBean(StringUtils.uncapitalize(WhoisVariantBeanExcludeWhen.class.getSimpleName()));
        assertNotNull(whoisVariantBeanExcludeWhen.getUpdateDao());
    }

}
