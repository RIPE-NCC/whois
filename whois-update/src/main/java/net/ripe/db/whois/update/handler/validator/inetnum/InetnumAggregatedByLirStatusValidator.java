package net.ripe.db.whois.update.handler.validator.inetnum;

import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.iptree.Ipv4Entry;
import net.ripe.db.whois.common.iptree.Ipv4Tree;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.update.handler.validator.inet6num.AggregatedByLirStatusValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InetnumAggregatedByLirStatusValidator extends AggregatedByLirStatusValidator<Ipv4Resource, Ipv4Entry> {

    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.INETNUM);

    private static final int MAX_ASSIGNMENT_SIZE = 32;

    @Autowired
    public InetnumAggregatedByLirStatusValidator(final Ipv4Tree ipv4Tree, final RpslObjectDao rpslObjectDao) {
        super(ipv4Tree, rpslObjectDao);
    }

    public boolean isAssignmentSizeAttributeMandatory() {
        return false;
    }

    @Override
    public Ipv4Resource createResource(final String key) {
        return Ipv4Resource.parse(key);
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
