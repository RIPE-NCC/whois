package net.ripe.db.whois.common.domain;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

import static net.ripe.db.whois.common.domain.CIString.ciImmutableSet;

@Component
public class Maintainers {
    private final Set<CIString> enduserMaintainers;
    private final Set<CIString> legacyMaintainers;
    private final Set<CIString> allocMaintainers;
    private final Set<CIString> rsMaintainers;
    private final Set<CIString> enumMaintainers;
    private final Set<CIString> dbmMaintainers;

    @Autowired
    public Maintainers(
            @Value("${whois.maintainers.enduser:}") final String[] enduserMaintainers,
            @Value("${whois.maintainers.legacy:}") final String[] legacyMaintainers,
            @Value("${whois.maintainers.alloc:}") final String[] allocMaintainers,
            @Value("${whois.maintainers.enum:}") final String[] enumMaintainers,
            @Value("${whois.maintainers.dbm:}") final String[] dbmMaintainers) {

        this.enduserMaintainers = ciImmutableSet(enduserMaintainers);
        this.legacyMaintainers = ciImmutableSet(legacyMaintainers);
        this.allocMaintainers = ciImmutableSet(allocMaintainers);
        this.enumMaintainers = ciImmutableSet(enumMaintainers);
        this.dbmMaintainers = ciImmutableSet(dbmMaintainers);

        this.rsMaintainers = ImmutableSet.copyOf(Iterables.concat(this.enduserMaintainers, this.legacyMaintainers, this.allocMaintainers));
    }

    public Set<CIString> getEnduserMaintainers() {
        return enduserMaintainers;
    }

    public Set<CIString> getLegacyMaintainers() {
        return legacyMaintainers;
    }

    public Set<CIString> getAllocMaintainers() {
        return allocMaintainers;
    }

    public Set<CIString> getRsMaintainers() {
        return rsMaintainers;
    }

    public Set<CIString> getEnumMaintainers() {
        return enumMaintainers;
    }

    public Set<CIString> getDbmMaintainers() {
        return dbmMaintainers;
    }

    public boolean isEnduserMaintainer(Set<CIString> mntner) {
        return !Sets.intersection(enduserMaintainers, mntner).isEmpty();
    }

    public boolean isAllocMaintainer(Set<CIString> mntner) {
        return !Sets.intersection(allocMaintainers, mntner).isEmpty();
    }

    public boolean isRsMaintainer(CIString mntner) {
        return isRsMaintainer(Collections.singleton(mntner));
    }

    public boolean isRsMaintainer(Set<CIString> mntner) {
        return !Sets.intersection(rsMaintainers, mntner).isEmpty();
    }
}
