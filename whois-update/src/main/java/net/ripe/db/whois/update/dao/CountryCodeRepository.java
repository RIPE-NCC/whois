package net.ripe.db.whois.update.dao;

import com.google.common.collect.Sets;
import net.ripe.db.whois.common.domain.CIString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

import static net.ripe.db.whois.common.domain.CIString.ciString;

@Component
public class CountryCodeRepository {
    private Set<CIString> countryCodes;

    @Autowired
    public CountryCodeRepository(@Value("${whois.countrycodes}") final String[] countryCodes) {
        final Set<CIString> set = Sets.newHashSetWithExpectedSize(countryCodes.length);
        for (final String countryCode : countryCodes) {
            set.add(ciString(countryCode));
        }

        this.countryCodes = Collections.unmodifiableSet(set);
    }

    public Set<CIString> getCountryCodes() {
        return countryCodes;
    }
}
