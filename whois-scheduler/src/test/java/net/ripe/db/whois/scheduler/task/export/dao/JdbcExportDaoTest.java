package net.ripe.db.whois.scheduler.task.export.dao;

import com.google.common.collect.Sets;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.scheduler.AbstractSchedulerIntegrationTest;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

public class JdbcExportDaoTest extends AbstractSchedulerIntegrationTest {
    @Autowired ExportDao subject;

    @Test
    public void getMaxSerial_in_context() {
        subject.getMaxSerial();
    }

    @Test
    public void exportObjects_in_context() {
        subject.exportObjects(new ExportCallbackHandler() {
            @Override
            public void exportObject(final RpslObject object) {
            }
        });
    }

    @Test
    public void exportObjects_in_context_with_exception() {
        databaseHelper.addObject(RpslObject.parse("mntner: DEV-MNT"));

        try {
            subject.exportObjects(new ExportCallbackHandler() {
                @Override
                public void exportObject(final RpslObject object) {
                    throw new RuntimeException("Oops");
                }
            });

            Assert.fail("Should throw exception");
        } catch (RuntimeException ignored) {
        }
    }

    @Test
    public void exportObjects() {
        final int nrObjects = 100;

        final Set<RpslObject> objects = Sets.newHashSetWithExpectedSize(nrObjects);
        for (int i = 0; i < nrObjects; i++) {
            final RpslObject object = RpslObject.parse("mntner: DEV-MNT" + i);
            objects.add(object);
            databaseHelper.addObject(object);
        }

        subject.exportObjects(new ExportCallbackHandler() {
            @Override
            public void exportObject(final RpslObject object) {
                if (!objects.remove(object)) {
                    Assert.fail("Object not in set: " + object);
                }
            }
        });

        Assert.assertThat(objects, Matchers.hasSize(0));
    }
}
