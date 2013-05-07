package net.ripe.db.whois.scheduler.task.export;

import net.ripe.db.whois.common.rpsl.ObjectType;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class FilenameStrategyTest {
    @Test
    public void getFilename_SingleFile() {
        final FilenameStrategy subject = new FilenameStrategy.SingleFile();

        assertThat(subject.getFilename(ObjectType.MNTNER), is("ripe.db"));
    }

    @Test
    public void getFilename_SplitFile() {
        final FilenameStrategy subject = new FilenameStrategy.SplitFile();

        assertThat(subject.getFilename(ObjectType.MNTNER), is("ripe.db.mntner"));
    }
}
