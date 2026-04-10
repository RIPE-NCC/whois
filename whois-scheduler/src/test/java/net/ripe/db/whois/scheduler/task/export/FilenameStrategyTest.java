package net.ripe.db.whois.scheduler.task.export;

import net.ripe.db.whois.common.rpsl.ObjectType;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class FilenameStrategyTest {
    @Test
    public void getFilename_SingleFile() {
        final FilenameStrategy subject = new FilenameStrategy.SingleFile("TEST");

        assertThat(subject.getFilename(ObjectType.MNTNER, StandardCharsets.ISO_8859_1), is("test.db"));
    }


    @Test
    public void getFilename_SplitFile() {
        final FilenameStrategy subject = new FilenameStrategy.SplitFile("TEST");

        assertThat(subject.getFilename(ObjectType.MNTNER, StandardCharsets.ISO_8859_1), is("test.db.mntner"));
    }

    @Test
    public void getFilename_Single_utf8_File() {
        final FilenameStrategy subject = new FilenameStrategy.SingleFile("TEST");

        assertThat(subject.getFilename(ObjectType.MNTNER, StandardCharsets.UTF_8), is("test.db.utf8"));
    }

    @Test
    public void getFilename_Split_utf8_File() {
        final FilenameStrategy subject = new FilenameStrategy.SplitFile("TEST");

        assertThat(subject.getFilename(ObjectType.MNTNER, StandardCharsets.UTF_8), is("test.db.mntner.utf8"));
    }
}
