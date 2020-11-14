package net.ripe.db.whois.common.domain;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class BlockEventsTest {

    private String prefix = "128.0.0.0";

    @Test
    public void test_events_empty() {
        final BlockEvents blockEvents = new BlockEvents(prefix, Collections.emptyList());
        assertThat(blockEvents.getPrefix(), is(prefix));
        assertThat(blockEvents.getTemporaryBlockCount(), is(0));
    }

    @Test(expected = NullPointerException.class)
    public void test_events_null() {
        final BlockEvents blockEvents = new BlockEvents(prefix, null);
        blockEvents.getTemporaryBlockCount();
    }

    @Test
    public void test_number_of_blocks() {
        final BlockEvents blockEvents = createBlockEvents("10.0.0.0", 3);
        assertThat(blockEvents.getTemporaryBlockCount(), is(3));
    }

    @Test
    public void test_number_of_blocks_after_unblock() {
        final List<BlockEvent> events = Arrays.asList(
                createBlockEvent(1, BlockEvent.Type.BLOCK_TEMPORARY),
                createBlockEvent(2, BlockEvent.Type.BLOCK_TEMPORARY),
                createBlockEvent(3, BlockEvent.Type.BLOCK_TEMPORARY),
                createBlockEvent(4, BlockEvent.Type.UNBLOCK),
                createBlockEvent(5, BlockEvent.Type.BLOCK_TEMPORARY)
        );

        final BlockEvents blockEvents = new BlockEvents(prefix, events);
        assertThat(blockEvents.getTemporaryBlockCount(), is(1));
    }

    @Test
    public void test_number_of_blocks_after_unblock_unspecified_order() {
        final List<BlockEvent> events = Arrays.asList(
                createBlockEvent(4, BlockEvent.Type.UNBLOCK),
                createBlockEvent(5, BlockEvent.Type.BLOCK_TEMPORARY),
                createBlockEvent(3, BlockEvent.Type.BLOCK_TEMPORARY),
                createBlockEvent(2, BlockEvent.Type.BLOCK_TEMPORARY),
                createBlockEvent(1, BlockEvent.Type.BLOCK_TEMPORARY)
        );

        final BlockEvents blockEvents = new BlockEvents(prefix, events);
        assertThat(blockEvents.getTemporaryBlockCount(), is(1));
    }

    @Test
    public void test_permanent_block_limit_reached_9() {
        final BlockEvents blockEvents = createBlockEvents(prefix, 9);

        assertThat(blockEvents.isPermanentBlockRequired(), is(false));
    }

    @Test
    public void test_permanent_block_limit_reached_10() {
        final BlockEvents blockEvents = createBlockEvents(prefix, 10);

        assertThat(blockEvents.isPermanentBlockRequired(), is(true));
    }

    @Test
    public void test_permanent_block_limit_reached_50() {
        final BlockEvents blockEvents = createBlockEvents(prefix, 50);

        assertThat(blockEvents.isPermanentBlockRequired(), is(true));
    }

    private static BlockEvents createBlockEvents(final String prefix, final int count) {
        final List<BlockEvent> blockEventList = Lists.newArrayListWithExpectedSize(count);
        for (int i = 0; i < count; i++) {
            blockEventList.add(createBlockEvent(i, BlockEvent.Type.BLOCK_TEMPORARY));
        }

        return new BlockEvents(prefix, blockEventList);
    }

    private static BlockEvent createBlockEvent(final int minute, final BlockEvent.Type type) {
        final LocalDateTime time = LocalDateTime.now()
                                        .with(ChronoField.HOUR_OF_DAY, 0L)
                                        .with(ChronoField.MINUTE_OF_HOUR, minute);
        return new BlockEvent(time, 5000, type);
    }

}
