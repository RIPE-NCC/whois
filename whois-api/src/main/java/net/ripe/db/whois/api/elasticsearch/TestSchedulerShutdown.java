package net.ripe.db.whois.api.elasticsearch;

import com.google.common.base.Stopwatch;
import jakarta.annotation.PostConstruct;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations;
import net.ripe.db.whois.common.domain.serials.SerialEntry;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Map;

// TODO [DA] Lucene implementation has some mechanism around thread safety. check if that is also necessary
@Component
public class TestSchedulerShutdown {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestSchedulerShutdown.class);
    private static final String TASK_NAME = "testSchedulerShutdown";



    @Scheduled(fixedDelayString = "${fulltext.index.update.interval.msecs:60000}")
    @SchedulerLock(name = TASK_NAME)
    public void scheduledUpdate() {

        try {
            Thread.sleep(8 *   // minutes to sleep
                    60 *   // seconds to a minute
                    1000); // milliseconds to a second
        } catch (InterruptedException e) {
        }
    }
}


