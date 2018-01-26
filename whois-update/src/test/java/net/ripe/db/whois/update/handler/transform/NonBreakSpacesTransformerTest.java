package net.ripe.db.whois.update.handler.transform;

import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class NonBreakSpacesTransformerTest {


    @Mock
    private Update update;
    @Mock
    private UpdateContext updateContext;

    private NonBreakSpacesTransformer subject;

    @Before
    public void setup() {
        this.subject = new NonBreakSpacesTransformer();
    }

    @Test
    public void rpsl_object_not_modified() {
        final RpslObject rpslObject = RpslObject.parse("person: Test Person\nnic-hdl: TP1-TEST\nsource: TEST");

        final RpslObject result = subject.transform(rpslObject, update, updateContext, Action.MODIFY);

        assertEquals(result, rpslObject);
    }

    @Test
    public void non_break_spaces_replaced() {
        final RpslObject rpslObject = RpslObject.parse("person: Test\u00a0Person\nnic-hdl: TP1-TEST\nsource: TEST");

        final RpslObject result = subject.transform(rpslObject, update, updateContext, Action.MODIFY);

        assertNotEquals(result, rpslObject);
        assertThat(result.getValueForAttribute(AttributeType.PERSON), is("Test Person"));
    }


}
