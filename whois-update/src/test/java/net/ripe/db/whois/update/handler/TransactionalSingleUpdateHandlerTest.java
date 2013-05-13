package net.ripe.db.whois.update.handler;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
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
    @InjectMocks TransactionalSingleUpdateHandler subject;

    @Before
    public void setUp() throws Exception {
        when(updateObjectHandler.getSupportedTypes()).thenReturn(Sets.newHashSet(ObjectType.values()));
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

        subject = new TransactionalSingleUpdateHandler(autoKeyResolver, attributeGenerator, attributeSanitizer, updateLockDao, loggerContext, authenticator, updateObjectHandler, rpslObjectDao, rpslObjectUpdateDao, ipTreeUpdater);
        subject.setSource("RIPE");
    }

    @Test
    public void supportAll_empty() throws Exception {
        assertThat(subject.supportAll(Lists.<Update>newArrayList()), is(true));
    }

    @Test
    public void supportAll_supported() throws Exception {
        when(updateObjectHandler.getSupportedTypes()).thenReturn(Sets.newHashSet(ObjectType.MNTNER));
        when(update.getType()).thenReturn(ObjectType.MNTNER);

        assertThat(subject.supportAll(Lists.newArrayList(update)), is(true));
    }

    @Test
    public void supportAll_supported_multiple() throws Exception {
        final Update update1 = mock(Update.class);
        when(update1.getType()).thenReturn(ObjectType.MNTNER);

        final Update update2 = mock(Update.class);
        when(update2.getType()).thenReturn(ObjectType.PERSON);

        final Update update3 = mock(Update.class);
        when(update3.getType()).thenReturn(ObjectType.ROLE);

        assertThat(subject.supportAll(Lists.newArrayList(update1, update2, update3)), is(true));
    }

    @Test
    public void supportAll_unsupported() throws Exception {
        when(updateObjectHandler.getSupportedTypes()).thenReturn(Sets.newHashSet(ObjectType.PERSON));
        when(update.getType()).thenReturn(ObjectType.MNTNER);

        assertThat(subject.supportAll(Lists.newArrayList(update)), is(false));
    }

    @Test
    public void supportAll_unsupported_multiple() throws Exception {
        when(updateObjectHandler.getSupportedTypes()).thenReturn(Sets.newHashSet(ObjectType.PERSON, ObjectType.MNTNER));

        final Update update1 = mock(Update.class);
        when(update1.getType()).thenReturn(ObjectType.MNTNER);

        final Update update2 = mock(Update.class);
        when(update2.getType()).thenReturn(ObjectType.PERSON);

        final Update update3 = mock(Update.class);
        when(update3.getType()).thenReturn(ObjectType.ROLE);

        assertThat(subject.supportAll(Lists.newArrayList(update1, update2, update3)), is(false));
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
