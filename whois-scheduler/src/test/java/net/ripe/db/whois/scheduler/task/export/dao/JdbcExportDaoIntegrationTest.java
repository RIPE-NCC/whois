package net.ripe.db.whois.scheduler.task.export.dao;

import com.google.common.collect.Sets;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.scheduler.AbstractSchedulerIntegrationTest;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;

@Tag("IntegrationTest")
public class JdbcExportDaoIntegrationTest extends AbstractSchedulerIntegrationTest {
    @Autowired ExportDao subject;

    @Test
    public void getMaxSerial_in_context() {
        databaseHelper.addObject(RpslObject.parse("mntner: DEV-MNT"));

        assertThat(subject.getMaxSerial(), is(1));
    }

    @Test
    public void exportObjects_in_context() {
        databaseHelper.addObject(RpslObject.parse("mntner: DEV-MNT"));
        final AtomicBoolean callback = new AtomicBoolean();

        subject.exportObjects(invocation -> callback.set(true));

        Awaitility.await().until(() -> true);
    }

    @Test
    public void exportObjects_in_context_with_exception() {
        databaseHelper.addObject(RpslObject.parse("mntner: DEV-MNT"));

        try {
            subject.exportObjects(invocation -> { throw new RuntimeException("Oops"); });
            fail("Should throw exception");
        } catch (RuntimeException ignored) {
            // expected
        }
    }

    @Test
    public void exportObjects() {
        final Set<RpslObject> objects = Sets.newHashSet();

        IntStream.range(0, 100).forEach(index -> {
            final RpslObject object = RpslObject.parse("mntner: DEV-MNT" + index);
            objects.add(object);
            databaseHelper.addObject(object);
        });

        subject.exportObjects(object -> {
            if (!objects.remove(object)) {
                fail("Object not in set: " + object);
            }
        });

        assertThat(objects, hasSize(0));
    }
}
