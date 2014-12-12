package net.ripe.db.whois.update.autokey.dao;

import com.google.common.collect.Range;
import net.ripe.db.whois.update.domain.NicHandle;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
class NicHandleRepositoryJdbc implements NicHandleRepository {
    private final JdbcTemplate jdbcTemplate;

    private static final class NicHandleRange {
        static enum Action {NONE, SAVE, DELETE}

        private Integer rangeId;
        private Range<Integer> range;
        private Action action;

        private NicHandleRange(final Range<Integer> range) {
            this.range = range;
            this.action = Action.SAVE;
        }

        private NicHandleRange(final int rangeId, Range<Integer> range) {
            this.rangeId = rangeId;
            this.range = range;
            this.action = Action.NONE;
        }
    }

    private static final Comparator<NicHandleRange> NICH_HANDLE_RANGE_COMPARATOR = new Comparator<NicHandleRange>() {
        @Override
        public int compare(final NicHandleRange range1, final NicHandleRange range2) {
            return range1.range.lowerEndpoint().compareTo(range2.range.lowerEndpoint());
        }
    };

    @Autowired
    public NicHandleRepositoryJdbc(@Qualifier("sourceAwareDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public boolean claimSpecified(final NicHandle nicHandle) {
        if (!isAvailable(nicHandle)) {
            return false;
        }

        final List<NicHandleRange> nicHandleRanges = getNicHandleRanges(nicHandle.getSpace(), nicHandle.getSuffix());
        nicHandleRanges.add(new NicHandleRange(Range.closed(nicHandle.getIndex(), nicHandle.getIndex())));
        saveCompact(nicHandleRanges, nicHandle.getSpace(), nicHandle.getSuffix());
        return true;
    }

    @Override
    public NicHandle claimNextAvailableIndex(final String space, final String suffix) {
        final List<NicHandleRange> nicHandleRanges = getNicHandleRanges(space, suffix);
        final int availableIndex = claimNextAvailableIndexInRanges(nicHandleRanges);
        saveCompact(nicHandleRanges, space, suffix);

        return new NicHandle(space, availableIndex, suffix);
    }

    private boolean isAvailable(final NicHandle nicHandle) {
        return jdbcTemplate.queryForList("" +
                "select range_id " +
                "  from nic_hdl " +
                "  where range_start <= ? " +
                "          and range_end >= ? " +
                "          and space = ? " +
                "          and source = ? ",
                nicHandle.getIndex(),
                nicHandle.getIndex(),
                nicHandle.getSpace(),
                getSuffixForSql(nicHandle.getSuffix())).isEmpty();
    }

    private List<NicHandleRange> getNicHandleRanges(final String space, final String suffix) {
        return jdbcTemplate.query("" +
                "select range_id, range_start, range_end " +
                "  from nic_hdl " +
                "  where space = ? and source = ? ",
                new RowMapper<NicHandleRange>() {
                    @Override
                    public NicHandleRange mapRow(final ResultSet rs, final int rowNum) throws SQLException {
                        return new NicHandleRange(rs.getInt("range_id"), Range.closed(rs.getInt("range_start"), rs.getInt("range_end")));
                    }
                }, space, getSuffixForSql(suffix));
    }

    private int claimNextAvailableIndexInRanges(final List<NicHandleRange> nicHandleRanges) {
        Collections.sort(nicHandleRanges, NICH_HANDLE_RANGE_COMPARATOR);

        if (nicHandleRanges.isEmpty() || nicHandleRanges.get(0).range.lowerEndpoint() > 1) {
            nicHandleRanges.add(new NicHandleRange(Range.closed(1, 1)));
            return 1;
        }

        final Iterator<NicHandleRange> nicHandleRangeIterator = nicHandleRanges.iterator();
        NicHandleRange previousRange = nicHandleRangeIterator.next();
        while (nicHandleRangeIterator.hasNext()) {
            final NicHandleRange currentRange = nicHandleRangeIterator.next();
            if (currentRange.range.lowerEndpoint() > previousRange.range.upperEndpoint() + 1) {
                break;
            }

            previousRange = currentRange;
        }

        final int nextAvailableIndex = previousRange.range.upperEndpoint() + 1;
        previousRange.range = Range.closed(previousRange.range.lowerEndpoint(), nextAvailableIndex);
        previousRange.action = NicHandleRange.Action.SAVE;
        return nextAvailableIndex;
    }

    private void saveCompact(final List<NicHandleRange> nicHandleRanges, final String space, final String suffix) {
        Collections.sort(nicHandleRanges, NICH_HANDLE_RANGE_COMPARATOR);

        NicHandleRange previousRange = null;
        for (final NicHandleRange currentRange : nicHandleRanges) {
            if (previousRange != null && currentRange.range.lowerEndpoint() == previousRange.range.upperEndpoint() + 1) {
                previousRange.range = Range.closed(previousRange.range.lowerEndpoint(), currentRange.range.upperEndpoint());
                previousRange.action = NicHandleRange.Action.SAVE;
                currentRange.action = NicHandleRange.Action.DELETE;
            } else {
                previousRange = currentRange;
            }
        }

        for (final NicHandleRange nicHandleRange : nicHandleRanges) {
            switch (nicHandleRange.action) {
                case SAVE:
                    if (nicHandleRange.rangeId == null) {
                        createRange(space, suffix, nicHandleRange.range.lowerEndpoint(), nicHandleRange.range.upperEndpoint());
                    } else {
                        updateRange(nicHandleRange.rangeId, nicHandleRange.range.lowerEndpoint(), nicHandleRange.range.upperEndpoint());
                    }
                    break;
                case DELETE:
                    if (nicHandleRange.rangeId != null) {
                        deleteRange(nicHandleRange.rangeId);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public void createRange(final String space, final String suffix, final int start, final int end) {
        jdbcTemplate.update("" +
                "insert into nic_hdl(range_start, range_end, space, source) " +
                "  values(?, ?, ?, ?)",
                start, end, space, getSuffixForSql(suffix));
    }

    private void updateRange(final int rangeId, final int start, final int end) {
        jdbcTemplate.update(
                "update nic_hdl set range_start = ?, range_end = ? where range_id = ?",
                start, end, rangeId);
    }

    private void deleteRange(final int rangeId) {
        jdbcTemplate.update(
                "delete from nic_hdl where range_id = ?",
                rangeId);
    }

    private String getSuffixForSql(final String suffix) {
        return StringUtils.isEmpty(suffix) ? "" : "-" + suffix;
    }
}
