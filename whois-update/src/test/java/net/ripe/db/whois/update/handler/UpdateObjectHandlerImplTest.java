package net.ripe.db.whois.update.handler;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.dao.RpslObjectUpdateDao;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.Operation;
import net.ripe.db.whois.update.domain.Paragraph;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import net.ripe.db.whois.update.sso.SsoTranslator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Transactional
@ExtendWith(MockitoExtension.class)
public class UpdateObjectHandlerImplTest {

    @Mock UpdateContext updateContext;
    @Mock RpslObjectUpdateDao rpslObjectUpdateDao;
    @Mock SsoTranslator ssoTranslator;
    private UpdateObjectHandler subject;

    @BeforeEach
    public void setUp() throws Exception {
        lenient().when(ssoTranslator.translateFromCacheAuthToUsername(any(UpdateContext.class), any(RpslObject.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return invocation.getArguments()[1];
            }
        });
        lenient().when(ssoTranslator.translateFromCacheAuthToUuid(any(UpdateContext.class), any(RpslObject.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return invocation.getArguments()[1];
            }
        });
        subject = new UpdateObjectHandler(rpslObjectUpdateDao, Lists.<BusinessRuleValidator>newArrayList(), ssoTranslator);
    }

    @Test
    public void create_success() {
        final RpslObject mntner = RpslObject.parse("mntner: MNT");
        final PreparedUpdate update = update(null, mntner, Operation.UNSPECIFIED, Action.CREATE);

        when(updateContext.hasErrors(update)).thenReturn(false);

        subject.execute(update, updateContext);

        verify(rpslObjectUpdateDao, times(1)).createObject(mntner);
    }

    @Test
    public void create_fail() {
        final RpslObject mntner = RpslObject.parse("mntner: MNT");
        final PreparedUpdate update = update(null, mntner, Operation.UNSPECIFIED, Action.CREATE);
        when(updateContext.hasErrors(update)).thenReturn(true);

        subject.execute(update, updateContext);

        verify(rpslObjectUpdateDao, never()).createObject(mntner);
    }

    @Test
    public void update_success() {
        final RpslObject mntner = RpslObject.parse(1, "mntner: MNTY");
        final RpslObject mntner2 = RpslObject.parse(2, "mntner: MNT2");
        final PreparedUpdate update = update(mntner, mntner2, Operation.UNSPECIFIED, Action.MODIFY);
        when(updateContext.hasErrors(update)).thenReturn(false);

        subject.execute(update, updateContext);

        verify(rpslObjectUpdateDao, times(1)).updateObject(anyInt(), eq(mntner2));
    }

    @Test
    public void update_fail() {
        final RpslObject mntner = RpslObject.parse("mntner: MNTY");
        final RpslObject mntner2 = RpslObject.parse("mntner: MNT2");
        final PreparedUpdate update = update(mntner, mntner2, Operation.UNSPECIFIED, Action.MODIFY);
        when(updateContext.hasErrors(update)).thenReturn(true);

        subject.execute(update, updateContext);

        verify(rpslObjectUpdateDao, never()).updateObject(anyInt(), eq(mntner2));
    }

    @Test
    public void delete_success() {
        RpslObject mntner = RpslObject.parse(1, "mntner: MNT\nmnt-by: MNT-BY");
        Update update = update(mntner, mntner, Operation.DELETE);
        final PreparedUpdate preparedUpdate = new PreparedUpdate(update, mntner, mntner, Action.DELETE);
        when(updateContext.hasErrors(preparedUpdate)).thenReturn(false);

        subject.execute(preparedUpdate, updateContext);

        verify(rpslObjectUpdateDao, times(1)).deleteObject(mntner.getObjectId(), mntner.getKey().toString());
    }

    @Test
    public void delete_fail() {
        RpslObject mntner = RpslObject.parse(1, "mntner: MNT\nmnt-by: MNT-BY");
        Update update = update(mntner, mntner, Operation.DELETE);
        final PreparedUpdate preparedUpdate = new PreparedUpdate(update, null, mntner, Action.DELETE);

        when(updateContext.hasErrors(preparedUpdate)).thenReturn(true);

        subject.execute(preparedUpdate, updateContext);

        verify(rpslObjectUpdateDao, never()).deleteObject(mntner.getObjectId(), mntner.getKey().toString());
    }

    private Update update(final RpslObject originalObject, final RpslObject updatedObject, final Operation operation) {
        Paragraph paragraph = new Paragraph((originalObject != null) ? originalObject.toString() : "", null);
        return new Update(paragraph, operation, null, updatedObject);
    }


    private PreparedUpdate update(final RpslObject originalObject, final RpslObject updatedObject, final Operation operation, final Action action) {
        final Update update = new Update(new Paragraph(updatedObject.toString()), operation, null, updatedObject);
        return new PreparedUpdate(update, originalObject, updatedObject, action);
    }
}
