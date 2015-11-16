package net.ripe.db.whois.common.rpsl.attrs;

import net.ripe.db.whois.common.rpsl.attrs.toggles.ChangedAttrFeatureToggle;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class ChangedIsNotAvailableTest {

    @Before
    public void before() {
        new ChangedAttrFeatureToggle(false);
    }

    @Test
    public void fail_to_parse() {
        try {
            Changed.parse("a@a.a 20010101");
            fail("It should not parse changed attribute");
        } catch (AttributeParseException e) {
            assertEquals("Changed attribute is not in use. (a@a.a 2001-01-01)", e.getMessage());
        }
    }

    @Test
    public void fail_to_create_new() {
        try {
            new Changed("a@a.a", LocalDate.parse("2001-01-01"));
            fail("It should not create new changed attribute");
        } catch (AttributeParseException e) {
            assertEquals("Changed attribute is not in use. (a@a.a 2001-01-01)", e.getMessage());
        }
    }
}
