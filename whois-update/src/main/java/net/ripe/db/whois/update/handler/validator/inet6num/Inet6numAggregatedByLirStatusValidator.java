package net.ripe.db.whois.update.handler.validator.inet6num;

import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.ip.Ipv6Resource;
import net.ripe.db.whois.common.iptree.Ipv6Entry;
import net.ripe.db.whois.common.iptree.Ipv6Tree;
import net.ripe.db.whois.common.rpsl.ObjectType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Inet6numAggregatedByLirStatusValidator extends AggregatedByLirStatusValidator<Ipv6Resource, Ipv6Entry> {

    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.INET6NUM);

    private static final int MAX_ASSIGNMENT_SIZE = 128;

    @Autowired
    public Inet6numAggregatedByLirStatusValidator(final Ipv6Tree ipv6Tree, final RpslObjectDao rpslObjectDao) {
        super(ipv6Tree, rpslObjectDao);
    }

    public boolean isAssignmentSizeAttributeMandatory() {
        return true;
    }

    @Override
    public Ipv6Resource createResource(final String key) {
        return Ipv6Resource.parse(key);
    }

    @Override
    public int getMaxAssignmentSize() {
        return MAX_ASSIGNMENT_SIZE;
    }

    @Override
    public ImmutableList<ObjectType> getTypes() {
        return TYPES;
    }
}
