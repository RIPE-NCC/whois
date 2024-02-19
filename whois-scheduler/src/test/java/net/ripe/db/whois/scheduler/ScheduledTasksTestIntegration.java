package net.ripe.db.whois.scheduler;


import net.ripe.db.whois.common.iptree.IpTreeCacheManager;
import net.ripe.db.whois.common.source.SourceConfiguration;
import net.ripe.db.whois.query.dao.IpAccessControlListDao;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(locations = {"classpath:applicationContext-scheduler-test.xml", "classpath:applicationContext-scheduler-test-mock.xml"}, inheritLocations = false)
@Tag("IntegrationTest")
public class ScheduledTasksTestIntegration extends AbstractSchedulerIntegrationTest {
    @Autowired private IpAccessControlListDao jdbcIpAccessControlListDao;
    @Autowired private IpTreeCacheManager ipTreeCacheManager;

    @Test
    public void testIpResourceConfiguration() throws Exception {
        Awaitility.await().atMost(1L, TimeUnit.SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                try {
                    verify(jdbcIpAccessControlListDao, atLeastOnce()).loadIpLimits();
                    verify(jdbcIpAccessControlListDao, atLeastOnce()).loadIpProxy();
                    verify(jdbcIpAccessControlListDao, atLeastOnce()).loadIpDenied();
                    verify(ipTreeCacheManager, atLeastOnce()).update(any(SourceConfiguration.class));

                    return true;
                } catch (AssertionError e) {
                    return false;
                }
            }
        }, is(Boolean.TRUE));
    }
}
