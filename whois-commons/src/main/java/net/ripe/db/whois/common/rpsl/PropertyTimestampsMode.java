package net.ripe.db.whois.common.rpsl;

import net.ripe.db.whois.common.profiles.DeployedProfile;
import org.springframework.beans.factory.annotation.Value;

import javax.inject.Inject;
import javax.inject.Named;

@Named
@DeployedProfile
public class PropertyTimestampsMode implements TimestampsMode {

    private final boolean timestampsOff;

    @Inject
    public PropertyTimestampsMode(@Value("${rpsl.timestamps.off:false}") boolean timestampsOff) {
        this.timestampsOff = timestampsOff;
    }

    @Override
    public boolean isTimestampsOff() {
        return timestampsOff;
    }
}
