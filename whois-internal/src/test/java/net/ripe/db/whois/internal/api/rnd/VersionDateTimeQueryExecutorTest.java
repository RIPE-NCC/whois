package net.ripe.db.whois.internal.api.rnd;

import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class VersionDateTimeQueryExecutorTest {

//    @Mock VersionDao versionDao;
//    @Mock BasicSourceContext sourceContext;
//    @Mock ResponseHandler responseHandler;
//
//    @InjectMocks
//    VersionWithReferencesQueryExecutor subject;
//
//    @Test
//    public void aclNotSupported() {
//        assertThat(subject.isAclSupported(), is(false));
//    }
//
//    @Test
//    public void supports() {
//        assertThat(subject.supports(Query.parse("--show-internal-version 1 NINJA")), is(false));
//        assertThat(subject.supports(Query.parse("--show-version 1 NINJA")), is(false));
//    }
//
//    @Test
//    public void execute_noVersionsFound() {
//        when(versionDao.getVersionsForTimestamp(ObjectType.PERSON, "TP1-TEST", 123456789l)).thenReturn(Collections.EMPTY_LIST);
//        when(sourceContext.getCurrentSource()).thenReturn(Source.slave("TEST"));
//
//        subject.execute(Query.parse("--show-internal-version 1404301680000 NINJA"), responseHandler);
//
//        verify(versionDao, never()).getRpslObject(any(VersionInfo.class));
//    }
//
//    @Test
//    public void execute_multipleVersionsForTimestamp() {
//        final VersionInfo v1 = new VersionInfo(false, 1, 1, 12345678l, Operation.UPDATE);
//        final VersionInfo v2 = new VersionInfo(true, 1, 2, 12345678l, Operation.UPDATE);
//
//        when(versionDao.getVersionsForTimestamp(ObjectType.PERSON, "TP1-TEST", 123456789l)).thenReturn(Lists.newArrayList(v1, v2));
//        when(sourceContext.getCurrentSource()).thenReturn(Source.slave("TEST"));
//        when(versionDao.getRpslObject(v2)).thenReturn(RpslObject.parse("" +
//                "person: Test Person\n" +
//                "nic-hdl: TP1-TEST"));
//
//        subject.execute(Query.parse("--select-types PERSON --show-internal-version 123456789 TP1-TEST"), responseHandler);
//
//        verify(responseHandler).handle(new MessageObject("There are 2 versions for the supplied datetime."));
//    }
//
//    @Test
//    public void execute_happyCase() {
//        final VersionInfo v1 = new VersionInfo(false, 1, 1, 12345678l, Operation.UPDATE);
//
//        when(versionDao.getVersionsForTimestamp(ObjectType.PERSON, "TP1-TEST", 555555555l)).thenReturn(Lists.newArrayList(v1));
//        when(sourceContext.getCurrentSource()).thenReturn(Source.slave("TEST"));
//        when(versionDao.getRpslObject(v1)).thenReturn(RpslObject.parse("" +
//                "person: Test Person\n" +
//                "phone: +312132423324\n" +
//                "address: street\n" +
//                "mnt-by: TEST-MNT\n" +
//                "nic-hdl: TP1-TEST\n" +
//                "changed: test@ripe.net 20340303\n" +
//                "source: TEST"));
//
//
//        subject.execute(Query.parse("--select-types PERSON --show-internal-version 555555555 TP1-TEST"), responseHandler);
//
//        verify(responseHandler, times(1)).handle(any(RpslObjectWithTimestamp.class));
//    }
}