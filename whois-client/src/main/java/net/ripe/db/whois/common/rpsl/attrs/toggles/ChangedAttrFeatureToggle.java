package net.ripe.db.whois.common.rpsl.attrs.toggles;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Lazy(false)
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ChangedAttrFeatureToggle {

    private static Boolean isAvailable = Boolean.TRUE;

    @Autowired
    public ChangedAttrFeatureToggle(@Value("${feature.toggle.changed.attr.available:true}") final Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    public static boolean isChangedAttrAvailable() {
        return isAvailable;
    }
}
