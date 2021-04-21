package net.ripe.db.whois.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ApplicationVersion {

    private final String version;
    private final String timestamp;
    private final String commitId;

    @Autowired
    public ApplicationVersion(
        @Value("${application.version}") final String version,
        @Value("${application.build.timestamp}") final String timestamp,
        @Value("${application.build.commit.id}") final String commitId) {
        this.version = version;
        this.timestamp = timestamp;
        this.commitId = commitId;
    }

    public String getVersion() {
        return version;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getCommitId() {
        return commitId;
    }
}
