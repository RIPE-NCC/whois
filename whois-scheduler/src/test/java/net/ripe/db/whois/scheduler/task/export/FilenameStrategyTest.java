package net.ripe.db.whois.scheduler.task.export;

import net.ripe.db.whois.common.rpsl.ObjectType;
import org.junit.jupiter.api.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class FilenameStrategyTest {
    @Test
    public void getFilename_SingleFile() {
        final FilenameStrategy subject = new FilenameStrategy.SingleFile("TEST");

        assertThat(subject.getFilename(ObjectType.MNTNER), is("test.db"));
    }

    @Test
    public void getFilename_SplitFile() {
        final FilenameStrategy subject = new FilenameStrategy.SplitFile("TEST");

        assertThat(subject.getFilename(ObjectType.MNTNER), is("test.db.mntner"));
    }
}
