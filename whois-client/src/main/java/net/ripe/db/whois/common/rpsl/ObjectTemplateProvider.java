package net.ripe.db.whois.common.rpsl;

import net.ripe.db.whois.common.rpsl.attrs.toggles.ChangedAttrFeatureToggle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component("objectTemplateProvider")
@Lazy(false)
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ObjectTemplateProvider {
    private static ObjectTemplate objectTemplate;

    @Autowired
    public ObjectTemplateProvider(ChangedAttrFeatureToggle changedAttrFeatureToggle) {
        if(changedAttrFeatureToggle.isChangedAttrAvailable()) {
            this.objectTemplate = new ObjectTemplateWithChanged();
        } else {
            this.objectTemplate = new ObjectTemplateWithoutChanged();
        }
    }

    public static ObjectTemplate getTemplate(ObjectType objectType) {
        return objectTemplate.getTemplate(objectType);
    }

    public static Collection<ObjectTemplate> getTemplates() {
        return objectTemplate.getTemplates();
    }
}
