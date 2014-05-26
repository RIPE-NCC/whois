package net.ripe.db.whois.update.dao;

import net.ripe.db.whois.common.domain.CIString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class LanguageCodeRepository {

    @Value("#{T(net.ripe.db.whois.common.domain.CIString).ciImmutableSet('${whois.languagecodes}')}")
    private Set<CIString> languageCodes;

    public Set<CIString> getLanguageCodes() {
        return languageCodes;
    }
}
