package net.ripe.db.whois.common.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;

public class BlockEventTest {

    @Test
    public void test_accessors() throws Exception {
        final LocalDateTime time = LocalDateTime.now();
        final int limit = 100;
        final BlockEvent.Type type = BlockEvent.Type.BLOCK_TEMPORARY;

        final BlockEvent blockEvent = new BlockEvent(time, limit, type);

        assertThat(blockEvent.getTime(), is(time));
        assertThat(blockEvent.getLimit(), is(limit));
        assertThat(blockEvent.getType(), is(type));
    }

    @Test
    public void equals() {
        final BlockEvent subject = new BlockEvent(LocalDateTime.of(2012, 2, 16, 12, 0), 1, BlockEvent.Type.BLOCK_TEMPORARY);
        final BlockEvent clone = new BlockEvent(LocalDateTime.of(2012, 2, 16, 12, 0), 1, BlockEvent.Type.BLOCK_TEMPORARY);
        final BlockEvent newDate = new BlockEvent(LocalDateTime.of(2011, 2, 16, 12, 0), 1, BlockEvent.Type.BLOCK_TEMPORARY);
        final BlockEvent newType = new BlockEvent(LocalDateTime.of(2012, 2, 16, 12, 0), 1, BlockEvent.Type.UNBLOCK);

        assertEquals("same", subject, subject);
        assertEquals("equal", subject, clone);
        assertEquals("hashcode", subject.hashCode(), clone.hashCode());

        assertFalse("null", subject.equals(null));
        assertFalse("different class", subject.equals(1));
        assertFalse("different date", subject.equals(newDate));
        assertFalse("different type", subject.equals(newType));
    }
}
