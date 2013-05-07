package net.ripe.db.whois.common.dao.jdbc.index;

import net.ripe.db.whois.common.rpsl.AttributeType;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class IndexStrategyAdapterTest {
    public static final AttributeType ATTRIBUTE_TYPE = AttributeType.ORG;
    IndexStrategyAdapter subject;

    @Before
    public void setUp() throws Exception {
        subject = new IndexStrategyAdapter(ATTRIBUTE_TYPE) {
        };
    }

    @Test
    public void getAttributeType() {
        assertThat(subject.getAttributeType(), is(ATTRIBUTE_TYPE));
    }

    @Test
    public void addToIndex() {
        assertThat(subject.addToIndex(null, null, null, (String) null), is(1));
    }

    @Test
    public void findInIndex() {
        assertThat(subject.findInIndex(null, (String) null), hasSize(0));
    }

    @Test
    public void removeFromIndex() {
        subject.removeFromIndex(null, null);
    }

    @Test
    public void getLookupTableName() {
        assertNull(subject.getLookupTableName());
    }

    @Test
    public void getLookupColumnName() {
        assertNull(subject.getLookupColumnName());
    }
}
