package net.ripe.db.whois.common.rpsl;

import net.ripe.db.whois.common.profiles.WhoisProfile;
import org.springframework.context.annotation.Profile;

import javax.inject.Named;

@Profile({WhoisProfile.TEST, WhoisProfile.ENDTOEND})
@Named
public class TestTimestampsMode implements TimestampsMode {
    private boolean timestampsOff;

    @Override
    public boolean isTimestampsOff() {
        return timestampsOff;
    }

    public void setTimestampsOff(final boolean timestampsOff) {
        this.timestampsOff = timestampsOff;
    }


}
