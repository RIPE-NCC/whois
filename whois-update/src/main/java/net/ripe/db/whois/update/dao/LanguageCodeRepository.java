package net.ripe.db.whois.update.dao;

import com.google.common.collect.Sets;
import net.ripe.db.whois.common.domain.CIString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

import static net.ripe.db.whois.common.domain.CIString.ciString;

@Component
public class LanguageCodeRepository {
    private Set<CIString> languageCodes;

    @Value("${whois.languagecodes}")
    public void setLanguageCodes(final String[] languageCodes) {
        final Set<CIString> set = Sets.newHashSetWithExpectedSize(languageCodes.length);
        for (final String countryCode : languageCodes) {
            set.add(ciString(countryCode));
        }

        this.languageCodes = Collections.unmodifiableSet(set);
    }

    public Set<CIString> getLanguageCodes() {
        return languageCodes;
    }
}
