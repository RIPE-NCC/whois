package net.ripe.db.whois.update.handler.validator.common;

import net.ripe.db.whois.common.rpsl.ObjectTemplateProvider;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.attrs.toggles.ChangedAttrFeatureToggle;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static net.ripe.db.whois.update.handler.validator.ValidatorTestHelper.validateUpdate;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.spockframework.util.Assert.fail;

@RunWith(MockitoJUnitRunner.class)
public class ChangedNotAvailableAttributeValidatorTest {

    private ChangedAttributeValidator subject = new ChangedAttributeValidator();

    @BeforeClass
    public static void beforeClass() {
        new ObjectTemplateProvider(new ChangedAttrFeatureToggle(false));
    }

    @Test
    public void getActionsShouldFail() {
        try {
            subject.getActions();
            fail("it should fail");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("Operation not allowed (changed attribute is disabled)"));
        }
    }

    @Test
    public void getTypesShouldFail() {
        try {
            subject.getTypes();
            fail("it should fail");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("Operation not allowed (changed attribute is disabled)"));
        }
    }

    @Test
    public void validateShouldFail() {

        try {
            final RpslObject object = RpslObject.parse("mntner: MNT");
            validateUpdate(subject, null, object);

            fail("it should fail");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("Operation not allowed (changed attribute is disabled)"));
        }

    }

}
