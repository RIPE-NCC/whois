package net.ripe.db.whois.common.dao.jdbc.index;

import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.rpsl.AttributeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class UnindexedTest {
    Unindexed subject;

    @BeforeEach
    public void setUp() throws Exception {
        subject = new Unindexed(AttributeType.ALIAS);
    }

    @Test
    public void getAttributeType() {
        assertThat(subject.getAttributeType(), is(AttributeType.ALIAS));
    }

    @Test
    public void addToIndex() {
        final int rows = subject.addToIndex(null, null, null, (String) null);
        assertThat(rows, is(1));
    }

    @Test
    public void findInIndex() {
        final List<RpslObjectInfo> result = subject.findInIndex(null, (String) null);
        assertThat(result, hasSize(0));
    }

    @Test
    public void removeFromIndex() {
        subject.removeFromIndex(null, null);
    }
}
