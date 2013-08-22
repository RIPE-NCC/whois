package net.ripe.db.whois.common.profiles.usingspring;

import net.ripe.db.whois.common.dao.RpslObjectUpdateDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@RipeProfile
@Component
public class WhoisVariantBeanRipe {
    @Autowired
    private RpslObjectUpdateDao updateDao;

    public RpslObjectUpdateDao getUpdateDao() {
        return updateDao;
    }

    public boolean isRipe() {
        return StaticInitExample.isRIPE();
    }
}
