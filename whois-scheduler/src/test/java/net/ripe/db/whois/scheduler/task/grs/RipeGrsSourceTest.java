package net.ripe.db.whois.scheduler.task.grs;

import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.grs.RipeResourceData;
import net.ripe.db.whois.common.rpsl.RpslObjectBase;
import net.ripe.db.whois.common.source.SourceContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class RipeGrsSourceTest {
    @Mock SourceContext sourceContext;
    @Mock DateTimeProvider dateTimeProvider;
    @Mock RipeResourceData ripeResourceData;
    RipeGrsSource subject;

    @Before
    public void setUp() throws Exception {
        subject = new RipeGrsSource("RIPE", sourceContext, dateTimeProvider, ripeResourceData);

    }

    @Test(expected = UnsupportedOperationException.class)
    public void acquireDump() throws IOException {
        subject.acquireDump(new File(""));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void handleObjects() throws IOException {
        subject.handleObjects(new File(""), new ObjectHandler() {
            @Override
            public void handle(final List<String> lines) {
            }

            @Override
            public void handle(final RpslObjectBase rpslObjectBase) {
            }
        });
    }
}
