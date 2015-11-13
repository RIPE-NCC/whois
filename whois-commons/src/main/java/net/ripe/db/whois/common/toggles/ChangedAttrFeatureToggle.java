package net.ripe.db.whois.common.toggles;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ChangedAttrFeatureToggle {

    final private Boolean isAvailable;

    @Autowired
    public ChangedAttrFeatureToggle(@Value("${feature.toggle.changed.attr.available}") Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    public boolean isChangedAttrAvailable() {
        return isAvailable;
    }
}
