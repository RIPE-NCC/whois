package net.ripe.db.whois.common.hazelcast;

import com.hazelcast.cluster.MembershipEvent;
import com.hazelcast.cluster.MembershipListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HazelcastMemberShipListener implements MembershipListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(HazelcastMemberShipListener.class);

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
