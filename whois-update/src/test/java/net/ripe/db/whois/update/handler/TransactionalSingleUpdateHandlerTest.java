package net.ripe.db.whois.update.handler;

import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.RpslObjectUpdateDao;
import net.ripe.db.whois.common.dao.RpslObjectUpdateInfo;
import net.ripe.db.whois.common.dao.UpdateLockDao;
import net.ripe.db.whois.common.iptree.IpTreeUpdater;
import net.ripe.db.whois.common.rpsl.AttributeSanitizer;
import net.ripe.db.whois.common.rpsl.ObjectMessages;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.Authenticator;
import net.ripe.db.whois.update.autokey.AutoKeyResolver;
import net.ripe.db.whois.update.domain.*;
import net.ripe.db.whois.update.log.LoggerContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TransactionalSingleUpdateHandlerTest {
    @Mock RpslObjectDao rpslObjectDao;
    @Mock RpslObjectUpdateDao rpslObjectUpdateDao;
    @Mock UpdateLockDao updateLockDao;
    @Mock Update update;
    @Mock PreparedUpdate preparedUpdate;
    @Mock Paragraph paragraph;
    @Mock Ack ack;
    @Mock Origin origin;
    @Mock UpdateContext updateContext;
    @Mock LoggerContext loggerContext;
    @Mock Authenticator authenticator;
    @Mock UpdateObjectHandler updateObjectHandler;
    @Mock AutoKeyResolver autoKeyResolver;
    @Mock AttributeSanitizer attributeSanitizer;
    @Mock AttributeGenerator attributeGenerator;
    @Mock IpTreeUpdater ipTreeUpdater;
    @Mock PendingUpdateHandler pendingUpdateHandler;
    @InjectMocks SingleUpdateHandler subject;

    @Before
    public void setUp() throws Exception {
        when(update.getOperation()).thenReturn(Operation.DELETE);
        final RpslObject mntner = RpslObject.parse("mntner: DEV-ROOT-MNT");

        when(update.getSubmittedObject()).thenReturn(mntner);
        when(update.getType()).thenReturn(ObjectType.MNTNER);
        when(update.getParagraph()).thenReturn(paragraph);
        when(update.getUpdate()).thenReturn(update);
        when(update.getCredentials()).thenReturn(new Credentials());
        when(preparedUpdate.getUpdate()).thenReturn(update);

        when(attributeSanitizer.sanitize(any(RpslObject.class), any(ObjectMessages.class))).then(new Answer<RpslObject>() {
            @Override
            public RpslObject answer(final InvocationOnMock invocation) throws Throwable {
                return (RpslObject) invocation.getArguments()[0];
            }
        });

        when(attributeGenerator.generateAttributes(any(RpslObject.class), any(Update.class), any(UpdateContext.class))).then(new Answer<RpslObject>() {
            @Override
            public RpslObject answer(final InvocationOnMock invocation) throws Throwable {
                return (RpslObject) invocation.getArguments()[0];
            }
        });

        when(autoKeyResolver.resolveAutoKeys(any(RpslObject.class), any(Update.class), any(UpdateContext.class), any(Action.class))).then(new Answer<RpslObject>() {
            @Override
            public RpslObject answer(final InvocationOnMock invocation) throws Throwable {
                return (RpslObject) invocation.getArguments()[0];
            }
        });

        subject = new SingleUpdateHandler(autoKeyResolver, attributeGenerator, attributeSanitizer, updateLockDao, loggerContext, authenticator, updateObjectHandler, rpslObjectDao, rpslObjectUpdateDao, ipTreeUpdater, pendingUpdateHandler);
        subject.setSource("RIPE");
    }

    @Test
    public void handle_execute() {
        when(update.getOperation()).thenReturn(Operation.UNSPECIFIED);

        subject.handle(origin, Keyword.NONE, update, updateContext);

        verify(updateObjectHandler, times(1)).execute(any(PreparedUpdate.class), any(UpdateContext.class));
    }

    @Test(expected = IllegalStateException.class)
    public void object_modified_unexpectedly() {
        final RpslObject inetnum = RpslObject.parse("inetnum: 192.168.1.1");

        when(update.getOperation()).thenReturn(Operation.UNSPECIFIED);
        when(update.getSubmittedObject()).thenReturn(inetnum);

        when(update.getSubmittedObjectInfo()).thenReturn(new RpslObjectUpdateInfo(1, 1, inetnum.getType(), inetnum.getKey().toString()));
        when(rpslObjectUpdateDao.lookupObject(inetnum.getType(), inetnum.getKey().toString())).
                thenReturn(new RpslObjectUpdateInfo(1, 2, inetnum.getType(), inetnum.getKey().toString()));

        subject.handle(origin, Keyword.NONE, update, updateContext);
    }
}
