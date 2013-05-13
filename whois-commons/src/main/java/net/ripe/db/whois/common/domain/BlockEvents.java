package net.ripe.db.whois.common.domain;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BlockEvents {
    public static final int NR_TEMP_BLOCKS_BEFORE_PERMANENT = 10;

    private final String prefix;
    private final List<BlockEvent> blockEvents;

    private static final class BlockEventsComparator implements Comparator<BlockEvent> {
        @Override
        public int compare(final BlockEvent blockEvent1, final BlockEvent blockEvent2) {
            return blockEvent2.getTime().compareTo(blockEvent1.getTime());
        }
    }

    public BlockEvents(final String prefix, final List<BlockEvent> blockEvents) {
        this.prefix = prefix;
        this.blockEvents = blockEvents;

        Collections.sort(blockEvents, new BlockEventsComparator());
    }

    public String getPrefix() {
        return prefix;
    }

    public int getTemporaryBlockCount() {
        int numberOfBlocks = 0;

        for (final BlockEvent blockEvent : blockEvents) {
            switch (blockEvent.getType()) {
                case BLOCK_TEMPORARY:
                    numberOfBlocks++;
                    break;
                case UNBLOCK:
                case BLOCK_PERMANENTLY:
                    return numberOfBlocks;
                default:
                    throw new IllegalStateException("Unexpected block event type: " + blockEvent.getType());
            }
        }

        return numberOfBlocks;
    }

    public boolean isPermanentBlockRequired() {
        return getTemporaryBlockCount() >= NR_TEMP_BLOCKS_BEFORE_PERMANENT;
    }
}
