package net.ripe.db.whois.common.domain;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

import static net.ripe.db.whois.common.domain.CIString.ciString;

@Component
public class Maintainers {
    private final Set<CIString> powerMaintainers;
    private final Set<CIString> enduserMaintainers;
    private final Set<CIString> allocMaintainers;
    private final Set<CIString> rsMaintainers;
    private final Set<CIString> enumMaintainers;
    private final Set<CIString> dbmMaintainers;

    @Autowired
    public Maintainers(
            @Value("${whois.maintainers.power}") final String[] powerMaintainers,
            @Value("${whois.maintainers.enduser}") final String[] enduserMaintainers,
            @Value("${whois.maintainers.alloc}") final String[] allocMaintainers,
            @Value("${whois.maintainers.enum}") final String[] enumMaintainers,
            @Value("${whois.maintainers.dbm}") final String[] dbmMaintainers) {

        this.powerMaintainers = createMaintainerSet(powerMaintainers);
        this.enduserMaintainers = createMaintainerSet(enduserMaintainers);
        this.allocMaintainers = createMaintainerSet(allocMaintainers);
        this.enumMaintainers = createMaintainerSet(enumMaintainers);
        this.dbmMaintainers = createMaintainerSet(dbmMaintainers);

        this.rsMaintainers = Sets.newLinkedHashSet(Iterables.concat(this.powerMaintainers, this.enduserMaintainers, this.allocMaintainers));
    }

    private static Set<CIString> createMaintainerSet(final String[] maintainers) {
        final Set<CIString> maintainerSet = Sets.newLinkedHashSetWithExpectedSize(maintainers.length);
        for (final String maintainer : maintainers) {
            maintainerSet.add(ciString(maintainer));
        }

        return Collections.unmodifiableSet(maintainerSet);
    }

    public Set<CIString> getPowerMaintainers() {
        return powerMaintainers;
    }

    public Set<CIString> getEnduserMaintainers() {
        return enduserMaintainers;
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
}
