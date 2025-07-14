package net.ripe.db.whois.common.dao.jdbc.index;

import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class IndexStrategyAdapterTest {
    public static final AttributeType ATTRIBUTE_TYPE = AttributeType.ORG;
    IndexStrategyAdapter subject;

    @BeforeEach
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
        assertThat(subject.findInIndex(null, new RpslObjectInfo(1, ObjectType.AUT_NUM, "AS000")), hasSize(0));
        assertThat(subject.findInIndex(null, new RpslObjectInfo(1, ObjectType.AUT_NUM, "AS000"), ObjectType.AUT_NUM), hasSize(0));
    }

    @Test
    public void removeFromIndex() {
        subject.removeFromIndex(null, null);
    }

    @Test
    public void getLookupTableName() {
        assertThat(subject.getLookupTableName(), is(nullValue()));
    }

    @Test
    public void getLookupColumnName() {
        assertThat(subject.getLookupColumnName(), is(nullValue()));
    }
}
