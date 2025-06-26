package net.ripe.db.whois.common.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

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

        assertThat(subject, equalTo(subject));
        assertThat(subject, equalTo(clone));
        assertThat(subject.hashCode(), equalTo(clone.hashCode()));

        assertThat(subject, not(equalTo(null)));
        assertThat(subject, not(equalTo(1))); // different class
        assertThat(subject, not(equalTo(newDate))); // different date
        assertThat(subject, not(equalTo(newType))); // different type
    }
}
