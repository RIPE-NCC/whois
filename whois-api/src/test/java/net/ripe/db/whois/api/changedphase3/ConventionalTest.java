package net.ripe.db.whois.api.changedphase3;

import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@Category(IntegrationTest.class)
public class ConventionalTest extends AbstractChangedPhase3Test {

    @Test
    public void rest_create_with_changed_old_mode() throws Exception {
        verifyObjectNotExists();

        final RpslObject output = restCreateObject(PERSON_WITH_CHANGED());

        verifyResponse(output, MUST_CONTAIN_CHANGED);
        verifyMail(MUST_CONTAIN_CHANGED);
    }

    @Test
    public void rest_create_without_changed_old_mode() throws Exception {
        verifyObjectNotExists();

        final RpslObject output = restCreateObject(PERSON_WITHOUT_CHANGED());

        verifyResponse(output, MUST_NOT_CONTAIN_CHANGED);
        verifyMail(MUST_NOT_CONTAIN_CHANGED);
    }

}
