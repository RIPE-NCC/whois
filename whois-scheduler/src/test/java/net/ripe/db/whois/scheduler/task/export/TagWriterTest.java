package net.ripe.db.whois.scheduler.task.export;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Tag;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(MockitoJUnitRunner.class)
public class TagWriterTest {

    @Mock private Writer writer;

    @InjectMocks TagWriter subject;

    @Test
    @SuppressWarnings("unchecked")
    public void no_tags_found() throws IOException {
        subject.writeTags(writer, RpslObject.parse(5, "mntner: TEST-MNT\nmnt-by:TEST-MNT"), Collections.EMPTY_LIST);

        verifyZeroInteractions(writer);
    }

    @Test
    public void tags_found() throws IOException {
        subject.writeTags(writer, RpslObject.parse(3, "person: Test Person\nnic-hdl: TEST-NIC"),
                Lists.newArrayList(
                        new Tag(CIString.ciString("bar"), 3, "Bar Value\n"),
                        new Tag(CIString.ciString("foo"), 3, "FOO Value\n"))
                );

        verify(writer).write("% Tags relating to 'TEST-NIC'");
        verify(writer).write("% bar # Bar Value\n");
        verify(writer).write("% foo # FOO Value\n");
    }
}
