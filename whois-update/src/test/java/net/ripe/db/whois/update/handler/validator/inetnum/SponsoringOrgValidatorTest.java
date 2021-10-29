package net.ripe.db.whois.update.handler.validator.inetnum;

import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static net.ripe.db.whois.common.rpsl.ObjectType.AUT_NUM;
import static net.ripe.db.whois.common.rpsl.ObjectType.INET6NUM;
import static net.ripe.db.whois.common.rpsl.ObjectType.INETNUM;
import static net.ripe.db.whois.update.domain.Action.CREATE;
import static net.ripe.db.whois.update.domain.Action.MODIFY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;

@ExtendWith(MockitoExtension.class)
public class SponsoringOrgValidatorTest {

    /*
        For the tests related to SponsoringOrgValidator behaviour, check SponsoringOrgSpec.groovy
     */
    @InjectMocks
    private SponsoringOrgValidator subject;

    @Test
    public void getActions() {
        assertThat(subject.getActions(), contains(CREATE, MODIFY));
    }

    @Test
    public void getTypes() {
        assertThat(subject.getTypes(), containsInAnyOrder(INETNUM, INET6NUM, AUT_NUM));
    }

}
