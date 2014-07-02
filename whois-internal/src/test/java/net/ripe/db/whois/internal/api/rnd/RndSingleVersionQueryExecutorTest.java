package net.ripe.db.whois.internal.api.rnd;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.dao.VersionDao;
import net.ripe.db.whois.common.dao.VersionInfo;
import net.ripe.db.whois.common.domain.serials.Operation;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.BasicSourceContext;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.query.domain.MessageObject;
import net.ripe.db.whois.query.domain.ResponseHandler;
import net.ripe.db.whois.query.query.Query;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RndSingleVersionQueryExecutorTest {

    @Mock VersionDao versionDao;
    @Mock BasicSourceContext sourceContext;
    @Mock ResponseHandler responseHandler;

    @InjectMocks RndSingleVersionQueryExecutor subject;

    @Test
    public void aclNotSupported() {
        assertThat(subject.isAclSupported(), is(false));
    }

    @Test
    public void supports() {
//        assertThat(subject.supports(Query.parse("--show-timestamp-version 1404301680000 NINJA")), is(true));
        assertThat(subject.supports(Query.parse("--show-version NINJA")), is(false));
    }

    @Test
    public void multipleVersionsForTimestamp() {
        final Message message = RndSingleVersionQueryExecutor.multipleVersionsForTimestamp(3);
        assertThat(message.getText(), is("%s versions for timestamp found."));
    }

    @Test
    public void execute_noVersionsFound() {
        when(versionDao.getVersionsBeforeTimestamp(ObjectType.PERSON, "TP1-TEST", 123456789l)).thenReturn(Collections.EMPTY_LIST);
        when(sourceContext.getCurrentSource()).thenReturn(Source.slave("TEST"));

        subject.execute(Query.parse("--show-timestamp-version 1404301680000 NINJA"), responseHandler);

        verify(versionDao, never()).getRpslObject(any(VersionInfo.class));
    }

    @Test
    public void execute_multipleVersionsForTimestamp() {
        final VersionInfo v1 = new VersionInfo(false, 1, 1, 12345678l, Operation.UPDATE);
        final VersionInfo v2 = new VersionInfo(true, 1, 2, 12345678l, Operation.UPDATE);

        when(versionDao.getVersionsBeforeTimestamp(ObjectType.PERSON, "TP1-TEST", 123456789l)).thenReturn(Lists.newArrayList(v1, v2));
        when(sourceContext.getCurrentSource()).thenReturn(Source.slave("TEST"));
        when(versionDao.getRpslObject(v2)).thenReturn(RpslObject.parse("" +
                "person: Test Person\n" +
                "nic-hdl: TP1-TEST"));

        subject.execute(Query.parse("--select-types PERSON --show-timestamp-version 123456789 TP1-TEST"), responseHandler);

        verify(responseHandler).handle(new MessageObject("2 versions for timestamp found."));
    }

    @Test
    public void execute_happyCase() {
        final VersionInfo v1 = new VersionInfo(false, 1, 1, 12345678l, Operation.UPDATE);

        when(versionDao.getVersionsBeforeTimestamp(ObjectType.PERSON, "TP1-TEST", 555555555l)).thenReturn(Lists.newArrayList(v1));
        when(sourceContext.getCurrentSource()).thenReturn(Source.slave("TEST"));
        when(versionDao.getRpslObject(v1)).thenReturn(RpslObject.parse("" +
                "person: Test Person\n" +
                "phone: +312132423324\n" +
                "address: street\n" +
                "mnt-by: TEST-MNT\n" +
                "nic-hdl: TP1-TEST\n" +
                "changed: test@ripe.net 20340303\n" +
                "source: TEST"));


        subject.execute(Query.parse("--select-types PERSON --show-timestamp-version 555555555 TP1-TEST"), responseHandler);

        verify(responseHandler, times(1)).handle(any(RpslObjectWithTimestamp.class));
    }
}