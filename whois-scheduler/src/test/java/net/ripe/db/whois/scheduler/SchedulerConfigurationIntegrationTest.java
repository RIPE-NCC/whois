package net.ripe.db.whois.scheduler;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.scheduling.TaskScheduler;

import java.util.Set;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;

@Tag("IntegrationTest")
public class SchedulerConfigurationIntegrationTest extends AbstractSchedulerIntegrationTest {

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
        assertThat(beanName, hasItem("taskScheduler"));
    }

}
