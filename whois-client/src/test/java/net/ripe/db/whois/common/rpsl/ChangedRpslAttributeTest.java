package net.ripe.db.whois.common.rpsl;

import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.attrs.toggles.ChangedAttrFeatureToggle;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.spockframework.util.Assert.fail;

public class ChangedRpslAttributeTest {

    @Before
    public void setup() {
        new ChangedAttrFeatureToggle(false);
    }

    @Test
    public void fail_to_create_attribute() {
        try {
            new RpslAttribute(AttributeType.CHANGED, "blabla");
            fail("it should fail to create Changed RpslAttribute");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is(AttributeType.CHANGED.getName()+" is not a valid attribute"));
        }

        try {
            new RpslAttribute(AttributeType.CHANGED, CIString.ciString("blabla"));
            fail("it should fail to create Changed RpslAttribute");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is(AttributeType.CHANGED.getName()+" is not a valid attribute"));
        }

        try {
            new RpslAttribute(AttributeType.CHANGED.getName(), "blabla");
            fail("it should fail to create Changed RpslAttribute");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is(AttributeType.CHANGED.getName()+" is not a valid attribute"));
        }

        try {
            new RpslAttribute(AttributeType.CHANGED.getName(), CIString.ciString("blabla"));
            fail("it should fail to create Changed RpslAttribute");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is(AttributeType.CHANGED.getName()+" is not a valid attribute"));
        }

    }


}
