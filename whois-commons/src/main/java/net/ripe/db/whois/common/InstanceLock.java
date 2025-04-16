package net.ripe.db.whois.common;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class InstanceLock {

    private final static int MAX_INSTANCES = 16;
    private final static String LOCK_FILE_NAME = "lock";

    private final String instanceNamePrefix;

    private Path lockFile;
    private String instanceName;

    public InstanceLock(final String baseDir, final String instanceNamePrefix) {
        if (StringUtils.isEmpty(baseDir)) {
            throw new IllegalStateException("Base directory has not been set");
        }

        if (StringUtils.isEmpty(instanceNamePrefix)) {
            throw new IllegalStateException("Instance name prefix has not been set");
        }

        final Path base = Path.of(baseDir);
        if (!Files.isDirectory(base)) {
            throw new IllegalStateException(String.format("Base directory %s does not exist", baseDir));
        }

        this.instanceNamePrefix = instanceNamePrefix;

        obtain(base);
    }

    private void obtain(final Path base) {
        for (int i = 1; i < MAX_INSTANCES; i++) {
            try {
                final String instanceName = this.instanceNamePrefix + i;
                final Path instanceDir = base.resolve(Path.of(instanceName));

                if (!Files.isDirectory(instanceDir)) {
                    Files.createDirectory(instanceDir);
                }

                lockFile = instanceDir.resolve(LOCK_FILE_NAME);

                if (!Files.exists(lockFile)) {
                    Files.createFile(lockFile);
                    this.instanceName = instanceName;
                    return;
                }
            } catch (IOException ioe) {
                lockFile = null;
            }
        }
        throw new IllegalStateException("Could not acquire instance lock file");
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void release() {
        try {
            Files.deleteIfExists(lockFile);
            lockFile = null;
            instanceName = null;
        } catch (IOException ioe) {
            throw new IllegalStateException(String.format("Failed to delete lock file %s", lockFile), ioe);
        }
    }

}
