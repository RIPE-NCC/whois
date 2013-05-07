package net.ripe.db.whois.update.handler.validator.common;

import com.google.common.collect.Sets;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.dao.EmptyResultDataAccessException;

import static net.ripe.db.whois.common.domain.CIString.ciSet;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MemberOfValidatorTest {

    @Mock RpslObjectDao objectDao;
    @Mock PreparedUpdate update;
    @Mock UpdateContext updateContext;

    @InjectMocks
    MemberOfValidator subject;

    @Test
    public void getActions() {
        assertThat(subject.getActions().size(), is(2));
        assertThat(subject.getActions().contains(Action.MODIFY), is(true));
        assertThat(subject.getActions().contains(Action.CREATE), is(true));
    }

    @Test
    public void getTypes() {
        assertThat(subject.getTypes().size(), is(4));
        assertThat(subject.getTypes().contains(ObjectType.AUT_NUM), is(true));
        assertThat(subject.getTypes().contains(ObjectType.ROUTE), is(true));
        assertThat(subject.getTypes().contains(ObjectType.ROUTE6), is(true));
        assertThat(subject.getTypes().contains(ObjectType.INET_RTR), is(true));
    }

    @Test
    public void nothing_to_validate_when_no_new_member_of() {
        when(update.getNewValues(AttributeType.MEMBER_OF)).thenReturn(Sets.<CIString>newHashSet());
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("aut-num: AS23454"));

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void referenced_asset_not_found() {
        when(update.getType()).thenReturn(ObjectType.AUT_NUM);
        when(update.getNewValues(AttributeType.MEMBER_OF)).thenReturn(ciSet("AS-23425"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("aut-num: AS23454\nmnt-by: TEST-MNT\nmember-of: AS-23425"));
        when(objectDao.getByKey(ObjectType.AS_SET, "AS-23425")).thenThrow(EmptyResultDataAccessException.class);

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(update, UpdateMessages.referenceNotFound("AS-23425"));
    }

    @Test
    public void maintainer_does_not_exist_in_referenced_mbrsbyref() {
        when(update.getType()).thenReturn(ObjectType.AUT_NUM);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("aut-num: AS23454\nmnt-by: TEST-MNT\nmember-of: AS-23425"));
        when(objectDao.getByKey(ObjectType.AS_SET, "AS-23425")).thenReturn(RpslObject.parse("as-set: AS-23425\nmbrs-by-ref: OTHER-MNT\ndescr: description"));

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.membersNotSupportedInReferencedSet("[AS-23425]"));
    }

    @Test
    public void success() {
        when(update.getType()).thenReturn(ObjectType.AUT_NUM);
        when(update.getNewValues(AttributeType.MEMBER_OF)).thenReturn(ciSet("AS-23425"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("aut-num: AS23454\nmnt-by: TEST-MNT\nmember-of: AS-23425"));
        when(objectDao.getByKey(ObjectType.AS_SET, "AS-23425")).thenReturn(RpslObject.parse("as-set: AS-23425\nmbrs-by-ref: TEST-MNT\ndescr: description"));

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void referenced_routeset_not_found() {
        when(update.getType()).thenReturn(ObjectType.ROUTE);
        when(update.getNewValues(AttributeType.MEMBER_OF)).thenReturn(ciSet("RS-TEST-FOO"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("route: 193.254.30.0/24\norigin:AS12726\nmnt-by: TEST-MNT\nmember-of: RS-TEST-FOO"));
        when(objectDao.getByKey(ObjectType.ROUTE_SET, "RS-TEST-FOO")).thenThrow(EmptyResultDataAccessException.class);

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void maintainer_does_not_exist_in_referenced_mbrsbyref_route() {
        when(update.getType()).thenReturn(ObjectType.ROUTE);
        when(update.getNewValues(AttributeType.MEMBER_OF)).thenReturn(ciSet("RS-TEST-FOO"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("route: 193.254.30.0/24\norigin:AS12726\nmnt-by: TEST-MNT\nmember-of: RS-TEST-FOO"));
        when(objectDao.getByKey(ObjectType.ROUTE_SET, "RS-TEST-FOO")).thenReturn(RpslObject.parse("route-set:RS-TEST-FOO\nmbrs-by-ref: any\ndescr: description"));

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void route_success() {
        when(update.getType()).thenReturn(ObjectType.ROUTE);
        when(update.getNewValues(AttributeType.MEMBER_OF)).thenReturn(ciSet("RS-TEST-FOO"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("route: 193.254.30.0/24\norigin:AS12726\nmnt-by: TEST-MNT\nmember-of: RS-TEST-FOO"));
        when(objectDao.getByKey(ObjectType.ROUTE_SET, "RS-TEST-FOO")).thenReturn(RpslObject.parse("route-set: RS-TEST-FOO\nmbrs-by-ref: TEST-MNT\ndescr: description"));

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void referenced_routeset_not_found_route6() {
        when(update.getType()).thenReturn(ObjectType.ROUTE6);
        when(update.getNewValues(AttributeType.MEMBER_OF)).thenReturn(ciSet("RS-TEST-FOO"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("route6:2001:1578:0200::/40\norigin:AS12726\nmnt-by: TEST-MNT\nmember-of: RS-TEST-FOO"));
        when(objectDao.getByKey(ObjectType.ROUTE_SET, "RS-TEST-FOO")).thenThrow(EmptyResultDataAccessException.class);

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void maintainer_does_not_exist_in_referenced_mbrsbyref_route6() {
        when(update.getType()).thenReturn(ObjectType.ROUTE6);
        when(update.getNewValues(AttributeType.MEMBER_OF)).thenReturn(ciSet("RS-TEST-FOO"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("route6:2001:1578:0200::/40\norigin:AS12726\nmnt-by: TEST-MNT\nmember-of: RS-TEST-FOO"));
        when(objectDao.getByKey(ObjectType.ROUTE_SET, "RS-TEST-FOO")).thenReturn(RpslObject.parse("route-set:RS-TEST-FOO\nmbrs-by-ref: any\ndescr: description"));

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void route6_success() {
        when(update.getType()).thenReturn(ObjectType.ROUTE6);
        when(update.getNewValues(AttributeType.MEMBER_OF)).thenReturn(ciSet("RS-TEST-FOO"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("route6:2001:1578:0200::/40\norigin:AS12726\nmnt-by: TEST-MNT\nmember-of: RS-TEST-FOO"));
        when(objectDao.getByKey(ObjectType.ROUTE_SET, "RS-TEST-FOO")).thenReturn(RpslObject.parse("route-set: RS-TEST-FOO\nmbrs-by-ref: TEST-MNT\ndescr: description"));

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }


    @Test
    public void referenced_maintainer_not_found_inetrtr() {
        when(update.getType()).thenReturn(ObjectType.INET_RTR);
        when(update.getNewValues(AttributeType.MEMBER_OF)).thenReturn(ciSet("RTRS-23425"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inet-rtr: test.ripe.net\nmnt-by: TEST-MNT\nmember-of: RTRS-23425"));
        when(objectDao.getByKey(ObjectType.RTR_SET, "RTRS-23425")).thenThrow(EmptyResultDataAccessException.class);

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void maintainer_does_not_exist_in_referenced_mbrsbyref_inetrtr() {
        when(update.getType()).thenReturn(ObjectType.INET_RTR);
        when(update.getNewValues(AttributeType.MEMBER_OF)).thenReturn(ciSet("RTRS-23425"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inet-rtr: test.ripe.net\nmnt-by: TEST-MNT\nmember-of: RTRS-23425"));
        when(objectDao.getByKey(ObjectType.RTR_SET, "RTRS-23425")).thenReturn(RpslObject.parse("route-set: RTRS-23425\nmbrs-by-ref: OTHER-MNT\ndescr: description"));

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.membersNotSupportedInReferencedSet("[RTRS-23425]"));
    }

    @Test
    public void success_inetrtr() {
        when(update.getType()).thenReturn(ObjectType.INET_RTR);
        when(update.getNewValues(AttributeType.MEMBER_OF)).thenReturn(ciSet("RTRS-23425"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inet-rtr: test.ripe.net\nmnt-by: TEST-MNT\nmember-of: RTRS-23425"));
        when(objectDao.getByKey(ObjectType.RTR_SET, "RTRS-23425")).thenReturn(RpslObject.parse("rtr-set: RTRS-23425\nmbrs-by-ref: TEST-MNT\ndescr: description"));

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }
}
