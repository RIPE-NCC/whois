package net.ripe.db.whois.update.handler.validator.sets;


import com.google.common.collect.Lists;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.authentication.Subject;
import net.ripe.db.whois.update.authentication.credential.AuthenticationModule;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContainer;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SetnameMustExistValidatorTest {

    @Mock PreparedUpdate update;
    @Mock UpdateContext updateContext;
    @Mock RpslObjectDao objectDao;
    @Mock AuthenticationModule authenticationModule;
    @Mock Subject updateSubject;

    @InjectMocks
    SetnameMustExistValidator subject;

    @Before
    public void setUp() {
        when(updateContext.getSubject(any(UpdateContainer.class))).thenReturn(updateSubject);
    }

    @Test
    public void getActions() {
        assertThat(subject.getActions(), containsInAnyOrder(Action.CREATE));
    }

    @Test
    public void getTypes() {
        assertThat(subject.getTypes(), containsInAnyOrder(ObjectType.AS_SET, ObjectType.FILTER_SET, ObjectType.PEERING_SET, ObjectType.ROUTE_SET, ObjectType.RTR_SET));
    }

    @Test
    public void validate_missing_parent() {
        when(update.getType()).thenReturn(ObjectType.FILTER_SET);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("filter-set: FLTR-RIPE:FLTR-ALLOCBNDR:FLTR-IPV6:fltr-something"));
        when(objectDao.getByKeys(eq(ObjectType.FILTER_SET), anyCollection())).thenReturn(Lists.<RpslObject>newArrayList());

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.parentObjectNotFound("FLTR-RIPE:FLTR-ALLOCBNDR:FLTR-IPV6"));
    }

    @Test
    public void validate_no_hiearchy() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("filter-set: FLTR-ND"));

        subject.validate(update, updateContext);

        verify(updateContext).getSubject(any(UpdateContainer.class));
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void validate_autnum_lookup_does_not_exist() {
        when(update.getType()).thenReturn(ObjectType.AS_SET);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("as-set: AS101:AS-TEST"));
        when(objectDao.getByKeys(eq(ObjectType.AUT_NUM), anyCollection())).thenReturn(Lists.<RpslObject>newArrayList());

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.parentObjectNotFound("AS101"));
    }

    @Test
    public void validate_autnum_lookup_does_not_exist_with_override() {
        when(updateSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(true);

        subject.validate(update, updateContext);

        verify(updateContext).getSubject(any(UpdateContainer.class));
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void validate_autnum_lookup_exists_but_is_not_authenticated() {
        when(update.getType()).thenReturn(ObjectType.AS_SET);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("as-set: AS101:AS-TEST"));
        when(objectDao.getByKeys(eq(ObjectType.AUT_NUM), anyCollection())).thenReturn(Lists.<RpslObject>newArrayList(RpslObject.parse("aut-num: AS101")));
        when(authenticationModule.authenticate(eq(update), eq(updateContext), anyList())).thenReturn(Lists.<RpslObject>newArrayList());

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.parentAuthenticationFailed(RpslObject.parse("aut-num: AS101"), AttributeType.MNT_BY, Lists.<RpslObject>newArrayList()));
    }


    @Test
    public void validate_autnum_lookup_exists_and_is_authenticated() {
        when(update.getType()).thenReturn(ObjectType.AS_SET);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("as-set: AS101:AS-TEST"));
        when(objectDao.getByKeys(eq(ObjectType.AUT_NUM), anyCollection())).thenReturn(Lists.<RpslObject>newArrayList(RpslObject.parse("aut-num: AS101\nmnt-by: TEST-MNT")));
        when(authenticationModule.authenticate(eq(update), eq(updateContext), anyList())).thenReturn(Lists.<RpslObject>newArrayList(RpslObject.parse("mntner: TEST-MNT")));

        subject.validate(update, updateContext);

        verify(updateContext).getSubject(any(UpdateContainer.class));
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void validate_routeset_against_autnum() {
        when(update.getType()).thenReturn(ObjectType.ROUTE_SET);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("route-set: As101:RS-ROUTESET"));
        when(objectDao.getByKeys(eq(ObjectType.AUT_NUM), anyCollection())).thenReturn(Lists.<RpslObject>newArrayList(RpslObject.parse("aut-num: AS101\nmnt-by: TEST-MNT")));
        when(authenticationModule.authenticate(eq(update), eq(updateContext), anyList())).thenReturn(Lists.<RpslObject>newArrayList(RpslObject.parse("mntner: TEST-MNT")));

        subject.validate(update, updateContext);

        verify(updateContext).getSubject(any(UpdateContainer.class));
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void validate_routeset_against_parent() {
        when(update.getType()).thenReturn(ObjectType.ROUTE_SET);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("route-set: RS-PARENT:RS-CHILD"));
        when(objectDao.getByKeys(eq(ObjectType.ROUTE_SET), anyCollection())).thenReturn(Lists.<RpslObject>newArrayList(RpslObject.parse("route-set: RS-PARENT\nmnt-lower: TEST-MNT")));
        when(authenticationModule.authenticate(eq(update), eq(updateContext), anyList())).thenReturn(Lists.<RpslObject>newArrayList(RpslObject.parse("mntner: TEST-MNT")));

        subject.validate(update, updateContext);

        verify(updateContext).getSubject(any(UpdateContainer.class));
        verifyNoMoreInteractions(updateContext);
    }


    @Test
    public void validate_routeset_against_parent_not_authenticated() {
        when(update.getType()).thenReturn(ObjectType.ROUTE_SET);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("route-set: RS-PARENT:RS-CHILD"));
        when(objectDao.getByKeys(eq(ObjectType.ROUTE_SET), anyCollection())).thenReturn(Lists.<RpslObject>newArrayList(RpslObject.parse("route-set: RS-PARENT\nmnt-by: TEST-MNT")));
        when(authenticationModule.authenticate(eq(update), eq(updateContext), anyList())).thenReturn(Lists.<RpslObject>newArrayList());

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.parentAuthenticationFailed(RpslObject.parse("route-set: RS-PARENT"), AttributeType.MNT_BY, Lists.<RpslObject>newArrayList()));
    }
}
