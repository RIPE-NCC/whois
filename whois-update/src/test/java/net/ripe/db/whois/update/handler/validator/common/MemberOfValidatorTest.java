package net.ripe.db.whois.update.handler.validator.common;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.authentication.Subject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MemberOfValidatorTest {

    @Mock RpslObjectDao objectDao;
    @Mock PreparedUpdate update;
    @Mock UpdateContext updateContext;
    @Mock
    private Subject subject;

    @InjectMocks
    MemberOfValidator memberOfValidator;

    @BeforeEach
    public void setup() {
        lenient().when(updateContext.getSubject(update)).thenReturn(subject);
        lenient().when(subject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(false);
    }

    @Test
    public void getActions() {
        assertThat(memberOfValidator.getActions(), hasSize(2));
        assertThat(memberOfValidator.getActions().contains(Action.MODIFY), is(true));
        assertThat(memberOfValidator.getActions().contains(Action.CREATE), is(true));
    }

    @Test
    public void getTypes() {
        assertThat(memberOfValidator.getTypes(), hasSize(4));
        assertThat(memberOfValidator.getTypes().contains(ObjectType.AUT_NUM), is(true));
        assertThat(memberOfValidator.getTypes().contains(ObjectType.ROUTE), is(true));
        assertThat(memberOfValidator.getTypes().contains(ObjectType.ROUTE6), is(true));
        assertThat(memberOfValidator.getTypes().contains(ObjectType.INET_RTR), is(true));
    }

    @Test
    public void nothing_to_validate_when_no_new_member_of() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("aut-num: AS23454"));

       memberOfValidator.validate(update, updateContext);

        verify(updateContext).getSubject(update);
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void referenced_asset_not_found() {
        when(update.getType()).thenReturn(ObjectType.AUT_NUM);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("aut-num: AS23454\nmnt-by: TEST-MNT\nmember-of: AS-23425"));
        when(objectDao.getByKey(ObjectType.AS_SET, "AS-23425")).thenThrow(EmptyResultDataAccessException.class);

       memberOfValidator.validate(update, updateContext);

        verify(updateContext, never()).addMessage(update, UpdateMessages.referenceNotFound("AS-23425"));
    }

    @Test
    public void maintainer_does_not_exist_in_referenced_mbrsbyref() {
        when(update.getType()).thenReturn(ObjectType.AUT_NUM);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("aut-num: AS23454\nmnt-by: TEST-MNT\nmember-of: AS-23425"));
        when(objectDao.getByKey(ObjectType.AS_SET, "AS-23425")).thenReturn(RpslObject.parse("as-set: AS-23425\nmbrs-by-ref: OTHER-MNT\ndescr: description"));

       memberOfValidator.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.membersNotSupportedInReferencedSet("[AS-23425]"));
    }

    @Test
    public void success() {
        when(update.getType()).thenReturn(ObjectType.AUT_NUM);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("aut-num: AS23454\nmnt-by: TEST-MNT\nmember-of: AS-23425"));
        when(objectDao.getByKey(ObjectType.AS_SET, "AS-23425")).thenReturn(RpslObject.parse("as-set: AS-23425\nmbrs-by-ref: TEST-MNT\ndescr: description"));

       memberOfValidator.validate(update, updateContext);

        verify(updateContext).getSubject(update);
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void referenced_routeset_not_found() {
        when(update.getType()).thenReturn(ObjectType.ROUTE);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("route: 193.254.30.0/24\norigin:AS12726\nmnt-by: TEST-MNT\nmember-of: RS-TEST-FOO"));
        when(objectDao.getByKey(ObjectType.ROUTE_SET, "RS-TEST-FOO")).thenThrow(EmptyResultDataAccessException.class);

       memberOfValidator.validate(update, updateContext);

       verify(updateContext).getSubject(update);
       verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void maintainer_does_not_exist_in_referenced_mbrsbyref_route() {
        when(update.getType()).thenReturn(ObjectType.ROUTE);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("route: 193.254.30.0/24\norigin:AS12726\nmnt-by: TEST-MNT\nmember-of: RS-TEST-FOO"));
        when(objectDao.getByKey(ObjectType.ROUTE_SET, "RS-TEST-FOO")).thenReturn(RpslObject.parse("route-set:RS-TEST-FOO\nmbrs-by-ref: any\ndescr: description"));

       memberOfValidator.validate(update, updateContext);

        verify(updateContext).getSubject(update);
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void route_success() {
        when(update.getType()).thenReturn(ObjectType.ROUTE);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("route: 193.254.30.0/24\norigin:AS12726\nmnt-by: TEST-MNT\nmember-of: RS-TEST-FOO"));
        when(objectDao.getByKey(ObjectType.ROUTE_SET, "RS-TEST-FOO")).thenReturn(RpslObject.parse("route-set: RS-TEST-FOO\nmbrs-by-ref: TEST-MNT\ndescr: description"));

       memberOfValidator.validate(update, updateContext);

        verify(updateContext).getSubject(update);
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void referenced_routeset_not_found_route6() {
        when(update.getType()).thenReturn(ObjectType.ROUTE6);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("route6:2001:1578:0200::/40\norigin:AS12726\nmnt-by: TEST-MNT\nmember-of: RS-TEST-FOO"));
        when(objectDao.getByKey(ObjectType.ROUTE_SET, "RS-TEST-FOO")).thenThrow(EmptyResultDataAccessException.class);

       memberOfValidator.validate(update, updateContext);

        verify(updateContext).getSubject(update);
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void maintainer_does_not_exist_in_referenced_mbrsbyref_route6() {
        when(update.getType()).thenReturn(ObjectType.ROUTE6);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("route6:2001:1578:0200::/40\norigin:AS12726\nmnt-by: TEST-MNT\nmember-of: RS-TEST-FOO"));
        when(objectDao.getByKey(ObjectType.ROUTE_SET, "RS-TEST-FOO")).thenReturn(RpslObject.parse("route-set:RS-TEST-FOO\nmbrs-by-ref: any\ndescr: description"));

       memberOfValidator.validate(update, updateContext);

        verify(updateContext).getSubject(update);
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void route6_success() {
        when(update.getType()).thenReturn(ObjectType.ROUTE6);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("route6:2001:1578:0200::/40\norigin:AS12726\nmnt-by: TEST-MNT\nmember-of: RS-TEST-FOO"));
        when(objectDao.getByKey(ObjectType.ROUTE_SET, "RS-TEST-FOO")).thenReturn(RpslObject.parse("route-set: RS-TEST-FOO\nmbrs-by-ref: TEST-MNT\ndescr: description"));

       memberOfValidator.validate(update, updateContext);
        verify(updateContext).getSubject(update);
        verifyNoMoreInteractions(updateContext);
    }


    @Test
    public void referenced_maintainer_not_found_inetrtr() {
        when(update.getType()).thenReturn(ObjectType.INET_RTR);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inet-rtr: test.ripe.net\nmnt-by: TEST-MNT\nmember-of: RTRS-23425"));
        when(objectDao.getByKey(ObjectType.RTR_SET, "RTRS-23425")).thenThrow(EmptyResultDataAccessException.class);

       memberOfValidator.validate(update, updateContext);

        verify(updateContext).getSubject(update);
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void maintainer_does_not_exist_in_referenced_mbrsbyref_inetrtr() {
        when(update.getType()).thenReturn(ObjectType.INET_RTR);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inet-rtr: test.ripe.net\nmnt-by: TEST-MNT\nmember-of: RTRS-23425"));
        when(objectDao.getByKey(ObjectType.RTR_SET, "RTRS-23425")).thenReturn(RpslObject.parse("route-set: RTRS-23425\nmbrs-by-ref: OTHER-MNT\ndescr: description"));

       memberOfValidator.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.membersNotSupportedInReferencedSet("[RTRS-23425]"));
    }

    @Test
    public void success_inetrtr() {
        when(update.getType()).thenReturn(ObjectType.INET_RTR);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inet-rtr: test.ripe.net\nmnt-by: TEST-MNT\nmember-of: RTRS-23425"));
        when(objectDao.getByKey(ObjectType.RTR_SET, "RTRS-23425")).thenReturn(RpslObject.parse("rtr-set: RTRS-23425\nmbrs-by-ref: TEST-MNT\ndescr: description"));

       memberOfValidator.validate(update, updateContext);

        verify(updateContext).getSubject(update);
        verifyNoMoreInteractions(updateContext);
    }
}
