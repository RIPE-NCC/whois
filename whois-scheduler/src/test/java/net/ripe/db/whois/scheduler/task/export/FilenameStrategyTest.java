package net.ripe.db.whois.scheduler.task.export;

import net.ripe.db.whois.common.rpsl.ObjectType;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.junit.Assert.assertThat;

public class FilenameStrategyTest {
    @Test
    public void getFilename_SingleFile() {
        final FilenameStrategy subject = new FilenameStrategy.SingleFile();

        assertThat(subject.getFilename(ObjectType.MNTNER), Matchers.is("ripe.db"));
    }

    @Test
    public void getFilename_SplitFile() {
        final FilenameStrategy subject = new FilenameStrategy.SplitFile();

        assertThat(subject.getFilename(ObjectType.MNTNER), Matchers.is("ripe.db.mntner"));
    }
}
