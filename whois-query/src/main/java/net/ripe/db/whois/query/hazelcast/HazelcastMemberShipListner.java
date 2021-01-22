package net.ripe.db.whois.query.hazelcast;

import com.hazelcast.cluster.MembershipEvent;
import net.ripe.db.whois.query.acl.HazelcastPersonalObjectAccounting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hazelcast.cluster.MembershipListener;

public class HazelcastMemberShipListner  implements MembershipListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(HazelcastPersonalObjectAccounting.class);

    @Override
    public void memberAdded(MembershipEvent membershipEvent) {
       LOGGER.info("Member added: " + membershipEvent.getMember());
       LOGGER.info("List of members now :" + membershipEvent.getMembers());
    }

    @Override
    public void memberRemoved(MembershipEvent membershipEvent) {
        LOGGER.info("Member removed: " + membershipEvent.getMember());
        LOGGER.info("list of members now :" + membershipEvent.getMembers());
    }
}
