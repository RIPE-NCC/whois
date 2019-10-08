package net.ripe.db.whois.query.support;

import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.BlockEvent;
import net.ripe.db.whois.common.domain.BlockEvents;
import net.ripe.db.whois.common.domain.Identifiable;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;

public class Fixture {

    public static BlockEvents createBlockEvents(final String prefix, final int count) {
        final List<BlockEvent> blockEventList = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            final LocalDateTime time = LocalDateTime.now()
                                            .with(ChronoField.HOUR_OF_DAY, 0)
                                            .with(ChronoField.MINUTE_OF_HOUR, i);
            blockEventList.add(new BlockEvent(time, 5000, BlockEvent.Type.BLOCK_TEMPORARY));
        }

        return new BlockEvents(prefix, blockEventList);
    }

    public static void mockRpslObjectDaoLoadingBehavior(final RpslObjectDao rpslObjectDao) {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                List<Identifiable> proxy = (List<Identifiable>) args[0];
                List<RpslObject> response = (List<RpslObject>) args[1];

                for (Identifiable p : proxy) {
                    response.add(rpslObjectDao.getById(p.getObjectId()));
                }

                return null;
            }
        }).when(rpslObjectDao).load(any(List.class), any(List.class));
    }

}
