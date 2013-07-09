package net.ripe.db.whois.common.profiles;

import net.ripe.db.whois.common.dao.RpslObjectUpdateDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@WhoisVariantContext( excludeWhen = WhoisVariant.Type.APNIC)
@Component
public class WhoisVariantBeanExcludeWhen {
    @Autowired
    private RpslObjectUpdateDao updateDao;

    public RpslObjectUpdateDao getUpdateDao() {
        return updateDao;
    }
}
