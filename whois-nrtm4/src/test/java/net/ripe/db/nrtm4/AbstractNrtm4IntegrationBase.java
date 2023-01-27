package net.ripe.db.nrtm4;

import net.ripe.db.whois.common.dao.jdbc.AbstractDatabaseHelperIntegrationTest;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


@ContextConfiguration(locations = {"classpath:applicationContext-nrtm4-test.xml"})
public class AbstractNrtm4IntegrationBase extends AbstractDatabaseHelperIntegrationTest {

    @BeforeAll
    public static void setupNrtmFilePath() throws IOException {
        final Path tempDirectory = Files.createTempDirectory("nrtmv4");
        tempDirectory.toFile().deleteOnExit();
        System.setProperty("nrtm.file.path", tempDirectory.toFile().getAbsolutePath());
    }

}
