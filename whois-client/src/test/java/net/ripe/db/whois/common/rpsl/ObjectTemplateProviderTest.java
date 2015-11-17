package net.ripe.db.whois.common.rpsl;

import org.junit.Test;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

public class ObjectTemplateProviderTest {

    @Test
    public void provideObjectTemplate() {
        final ObjectTemplate templateWith = ObjectTemplateProvider.getTemplate(ObjectType.AS_BLOCK);
        assertThat(templateWith, instanceOf(ObjectTemplateWithChanged.class));
    }

}
