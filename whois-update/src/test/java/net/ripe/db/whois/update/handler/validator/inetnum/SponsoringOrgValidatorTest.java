package net.ripe.db.whois.update.handler.validator.inetnum;

import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SponsoringOrgValidatorTest {
    @Mock PreparedUpdate update;
    @Mock UpdateContext updateContext;
    @Mock RpslObjectDao objectDao;
    @Mock Maintainers maintainers;

    @InjectMocks SponsoringOrgValidator subject;

    @Test
    public void sponsoring_org_not_changed() {

    }

    @Test
    public void update_is_override() {

    }

    @Test
    public void rpslobject_has_no_org_reference() {

    }

    @Test
    public void rpslobject_has_org_ref_with_status_other_than_lir() {

    }
}
