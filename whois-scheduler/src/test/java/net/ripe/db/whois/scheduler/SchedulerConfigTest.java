package net.ripe.db.whois.scheduler;

import net.ripe.db.whois.common.IntegrationTest;
import org.hamcrest.MatcherAssert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.TaskScheduler;

import javax.sql.DataSource;
import java.util.Set;

import static org.hamcrest.CoreMatchers.hasItem;

@Category(IntegrationTest.class)
public class SchedulerConfigTest extends AbstractSchedulerIntegrationTest {

    @Autowired
    GenericApplicationContext context;

    /**
     * Spring scheduling mechanism requires there should be a unique bean of type TaskScheduler
     * or a TaskScheduler bean named taskScheduler in the spring context. If not a local
     * single-threaded default scheduler will be created and used.
     *
     * We have another TaskScheduler bean named clientSynchronisationScheduler used in NRTM
     * hence we need to have a bean named taskScheduler, which will be used for the scheduled jobs
     */
    @Test
    public void bean_named_taskScheduler_in_context() {
        Set<String> beanName = context.getBeanFactory().getBeansOfType(TaskScheduler.class).keySet();
        MatcherAssert.assertThat(beanName, hasItem("taskScheduler"));
    }

}
