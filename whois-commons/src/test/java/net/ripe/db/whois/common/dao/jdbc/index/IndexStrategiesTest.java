package net.ripe.db.whois.common.dao.jdbc.index;

import com.google.common.collect.Sets;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectTemplate;
import net.ripe.db.whois.common.rpsl.ObjectType;
import org.junit.Test;

import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class IndexStrategiesTest {
    @Test
    public void check_index_strategied_for_lookup_attributes() {
        final Set<AttributeType> attibutesWithrequiredIndex = Sets.newHashSet();
        for (final ObjectType objectType : ObjectType.values()) {
            final ObjectTemplate objectTemplate = ObjectTemplate.getTemplate(objectType);
            attibutesWithrequiredIndex.addAll(objectTemplate.getInverseLookupAttributes());
        }

        for (final AttributeType attributeType : attibutesWithrequiredIndex) {
            assertThat(attributeType.getName(), IndexStrategies.get(attributeType) instanceof Unindexed, is(false));
        }
    }
}
