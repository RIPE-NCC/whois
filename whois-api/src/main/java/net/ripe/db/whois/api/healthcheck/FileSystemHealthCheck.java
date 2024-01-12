package net.ripe.db.whois.api.healthcheck;

import net.ripe.db.whois.common.HealthCheck;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class FileSystemHealthCheck implements HealthCheck {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemHealthCheck.class);

    private final AtomicBoolean fileSystemHealthy = new AtomicBoolean(true);
    private final File checkFile;

    @Autowired
    public FileSystemHealthCheck(@Value("${dir.var:}") final String filesystemRoot) {
        if (StringUtils.isNotBlank(filesystemRoot)) {
            this.checkFile = new File(filesystemRoot, "lock");
            LOGGER.info("File system health check file {}", checkFile);
        } else {
            this.checkFile = null;
            LOGGER.info("File system health check is disabled");
        }
    }

    @Override
    public boolean check() {
        return fileSystemHealthy.get();
    }

    @Scheduled(fixedDelay = 60 * 1_000)
    void updateStatus() {
        if (checkFile != null) {
            try {
                FileUtils.touch(checkFile);
                fileSystemHealthy.set(true);
            } catch (IOException ioe) {
                LOGGER.info("Failed to touch check file: {}", ioe.getMessage());
                fileSystemHealthy.set(false);
            }
        }
    }

}
