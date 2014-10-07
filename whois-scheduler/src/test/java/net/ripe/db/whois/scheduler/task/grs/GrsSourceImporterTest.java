package net.ripe.db.whois.scheduler.task.grs;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.grs.AuthoritativeResource;
import net.ripe.db.whois.common.rpsl.AttributeSanitizer;
import net.ripe.db.whois.common.rpsl.ObjectMessages;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.SourceContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GrsSourceImporterTest {

    @Rule public TemporaryFolder folder = new TemporaryFolder();

    @Mock AttributeSanitizer sanitizer;
    @Mock ResourceTagger resourceTagger;
    @Mock GrsSource grsSource;
    @Mock GrsDao grsDao;
    @Mock GrsDao.UpdateResult updateResultCreate;
    @Mock GrsDao.UpdateResult updateResultUpdate;
    @Mock AuthoritativeResource authoritativeResource;
    @Mock SourceContext sourceContext;

    Logger logger = LoggerFactory.getLogger(GrsSourceImporter.class);

    GrsSourceImporter subject;

    @Before
    public void setUp() throws Exception {
        when(grsSource.getDao()).thenReturn(grsDao);
        when(grsSource.getLogger()).thenReturn(logger);
        when(grsSource.getAuthoritativeResource()).thenReturn(authoritativeResource);

        when(sanitizer.sanitize(any(RpslObject.class), any(ObjectMessages.class))).thenAnswer(new Answer<RpslObject>() {
            @Override
            public RpslObject answer(InvocationOnMock invocation) throws Throwable {
                return (RpslObject) invocation.getArguments()[0];
            }
        });

        when(grsDao.createObject(any(RpslObject.class))).thenReturn(updateResultCreate);
        when(grsDao.updateObject(any(GrsObjectInfo.class), any(RpslObject.class))).thenReturn(updateResultUpdate);

        subject = new GrsSourceImporter(folder.getRoot().getAbsolutePath(), sanitizer, resourceTagger, sourceContext);
    }

    @Test
    public void run_rebuild() {
        when(grsSource.getName()).thenReturn(ciString("APNIC-GRS"));
        subject.grsImport(grsSource, true);

        verify(grsDao).cleanDatabase();
        verify(grsDao, never()).getCurrentObjectIds();
    }

    @Test
    public void run_rebuild_ripe() {
        when(grsSource.getName()).thenReturn(ciString("RIPE-GRS"));
        when(sourceContext.isVirtual(ciString("RIPE-GRS"))).thenReturn(true);
        subject.grsImport(grsSource, true);

        verify(grsDao, never()).cleanDatabase();
        verify(grsDao, never()).getCurrentObjectIds();
    }

    @Test
    public void run_without_rebuild() {
        when(grsSource.getName()).thenReturn(ciString("APNIC-GRS"));
        subject.grsImport(grsSource, false);

        verify(grsDao, never()).cleanDatabase();
        verify(grsDao).getCurrentObjectIds();
    }

    @Test
    public void run_without_rebuild_ripe() {
        when(grsSource.getName()).thenReturn(ciString("RIPE-GRS"));
        when(sourceContext.isVirtual(ciString("RIPE-GRS"))).thenReturn(true);
        subject.grsImport(grsSource, false);

        verify(grsDao, never()).cleanDatabase();
        verify(grsDao, never()).getCurrentObjectIds();
    }

    @Test
    public void acquire_and_process() throws IOException {
        when(grsSource.getName()).thenReturn(ciString("APNIC-GRS"));

        subject.grsImport(grsSource, false);

        final Path dumpFile = folder.getRoot().toPath().resolve("APNIC-GRS-DMP");
        verify(grsSource).acquireDump(dumpFile);
        verify(grsSource).handleObjects(eq(dumpFile.toFile()), any(ObjectHandler.class));
    }

    @Test
    public void acquire_and_process_ripe() throws IOException {
        when(grsSource.getName()).thenReturn(ciString("RIPE-GRS"));
        when(sourceContext.isVirtual(ciString("RIPE-GRS"))).thenReturn(true);

        subject.grsImport(grsSource, false);

        verify(grsSource, never()).acquireDump(any(Path.class));
        verify(grsSource, never()).handleObjects(any(File.class), any(ObjectHandler.class));
    }

    @Test
    public void process_nothing_does_not_delete() {
        when(grsSource.getName()).thenReturn(ciString("APNIC-GRS"));
        when(grsDao.getCurrentObjectIds()).thenReturn(Lists.newArrayList(1));

        subject.grsImport(grsSource, false);

        verify(grsDao, never()).deleteObject(anyInt());
    }

    @Test
    public void process_throws_exception() throws IOException {
        when(grsSource.getName()).thenReturn(ciString("APNIC-GRS"));
        doThrow(RuntimeException.class).when(grsSource).handleObjects(any(File.class), any(ObjectHandler.class));

        try {
            subject.grsImport(grsSource, false);
            fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            verify(grsSource).handleObjects(any(File.class), any(ObjectHandler.class));
        }
    }

    @Test
    public void handle_object_create() throws IOException {
        when(grsSource.getName()).thenReturn(ciString("APNIC-GRS"));
        when(authoritativeResource.isMaintainedByRirCombined(any(RpslObject.class))).thenReturn(true);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                final ObjectHandler objectHandler = (ObjectHandler) invocationOnMock.getArguments()[1];
                objectHandler.handle(RpslObject.parse("" +
                        "aut-num:       AS1263\n" +
                        "as-name:       NSN-TEST-AS\n" +
                        "descr:         NSN-TEST-AS\n" +
                        "admin-c:       Not available\n" +
                        "tech-c:        See MAINT-AS1263\n" +
                        "mnt-by:        MAINT-AS1263\n" +
                        "changed:       DB-admin@merit.edu 19950201\n" +
                        "source:        RIPE"
                ));
                return null;
            }
        }).when(grsSource).handleObjects(any(File.class), any(ObjectHandler.class));

        subject.grsImport(grsSource, false);

        verify(grsDao).createObject(RpslObject.parse("" +
                "aut-num:        AS1263\n" +
                "as-name:        NSN-TEST-AS\n" +
                "descr:          NSN-TEST-AS\n" +
                "admin-c:        Not available\n" +
                "tech-c:         See MAINT-AS1263\n" +
                "mnt-by:         MAINT-AS1263\n" +
                "changed:        DB-admin@merit.edu 19950201\n" +
                "source:         APNIC-GRS"));

        verify(sanitizer).sanitize(any(RpslObject.class), any(ObjectMessages.class));
    }

    @Test
    public void handle_object_create_syntax_errors() throws IOException {
        when(grsSource.getName()).thenReturn(ciString("APNIC-GRS"));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                final ObjectHandler objectHandler = (ObjectHandler) invocationOnMock.getArguments()[1];
                objectHandler.handle(RpslObject.parse("" +
                        "mntner:        SOME\n" +
                        "changed:       DB-admin@merit.edu 19950201\n" +
                        "unknown:       unknown" +
                        "source:        RIPE"
                ));

                return null;
            }
        }).when(grsSource).handleObjects(any(File.class), any(ObjectHandler.class));

        subject.grsImport(grsSource, false);

        verify(grsDao, never()).createObject(any(RpslObject.class));
        verify(sanitizer).sanitize(any(RpslObject.class), any(ObjectMessages.class));
    }

    @Test
    public void handle_lines_create_with_unknown_attribute() throws IOException {
        when(grsSource.getName()).thenReturn(ciString("APNIC-GRS"));
        when(authoritativeResource.isMaintainedByRirCombined(any(RpslObject.class))).thenReturn(true);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                final ObjectHandler objectHandler = (ObjectHandler) invocationOnMock.getArguments()[1];
                objectHandler.handle(Lists.newArrayList(
                        "aut-num:       AS1263\n",
                        "as-name:       NSN-TEST-AS\n",
                        "descr:         NSN-TEST-AS\n",
                        "               NSN-TEST-AS\n",
                        "admin-c:       Not available\n",
                        "unknown:       oops\n",
                        "tech-c:        See MAINT-AS1263\n",
                        "mnt-by:        MAINT-AS1263\n",
                        "changed:       DB-admin@merit.edu 19950201\n",
                        "source:        RIPE\n"
                ));
                return null;
            }
        }).when(grsSource).handleObjects(any(File.class), any(ObjectHandler.class));

        subject.grsImport(grsSource, false);

        verify(grsDao).createObject(RpslObject.parse("" +
                "aut-num:        AS1263\n" +
                "as-name:        NSN-TEST-AS\n" +
                "descr:          NSN-TEST-AS\n" +
                "                NSN-TEST-AS\n" +
                "admin-c:        Not available\n" +
                "tech-c:         See MAINT-AS1263\n" +
                "mnt-by:         MAINT-AS1263\n" +
                "changed:        DB-admin@merit.edu 19950201\n" +
                "source:         APNIC-GRS"));

        verify(sanitizer).sanitize(any(RpslObject.class), any(ObjectMessages.class));
    }

    @Test
    public void handle_lines_no_source_managed_by_rir() throws IOException {
        when(grsSource.getName()).thenReturn(ciString("APNIC-GRS"));
        when(authoritativeResource.isMaintainedByRirCombined(any(RpslObject.class))).thenReturn(true);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                final ObjectHandler objectHandler = (ObjectHandler) invocationOnMock.getArguments()[1];
                objectHandler.handle(Lists.newArrayList(
                        "aut-num:       AS1263\n",
                        "changed:       DB-admin@merit.edu 19950201\n"
                ));
                return null;
            }
        }).when(grsSource).handleObjects(any(File.class), any(ObjectHandler.class));

        subject.grsImport(grsSource, false);

        verify(grsDao).createObject(RpslObject.parse("" +
                "aut-num:        AS1263\n" +
                "changed:        DB-admin@merit.edu 19950201\n" +
                "source:         APNIC-GRS"));

        verify(sanitizer).sanitize(any(RpslObject.class), any(ObjectMessages.class));
    }

    @Test
    public void try_inserting_role_and_person_with_same_nichdl() throws Exception {
        when(grsSource.getName()).thenReturn(ciString("APNIC-GRS"));
        when(authoritativeResource.isMaintainedInRirSpace(any(RpslObject.class))).thenReturn(true);

        doAnswer(new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                final ObjectHandler objectHandler = (ObjectHandler) invocation.getArguments()[1];

                objectHandler.handle(RpslObject.parse("" +
                        "person: Ninja Person\n" +
                        "nic-hdl: NI124-RIPE\n"));

                return null;
            }
        }).when(grsSource).handleObjects(any(File.class), any(ObjectHandler.class));

        final GrsObjectInfo grsObjectInfo1 = new GrsObjectInfo(1, 1, RpslObject.parse("role: Ninja Role\nnic-hdl: NI124-RIPE\n"));
        when(grsDao.find("NI124-RIPE", ObjectType.PERSON)).thenReturn(null);
        when(grsDao.find("NI124-RIPE", ObjectType.ROLE)).thenReturn(grsObjectInfo1);

        subject.grsImport(grsSource, false);

        verify(grsDao, times(0)).createObject(any(RpslObject.class));
        verify(grsDao, times(0)).updateObject(any(GrsObjectInfo.class), any(RpslObject.class));
    }

    @Test
    public void run_create_update_delete() throws IOException {
        when(grsSource.getName()).thenReturn(ciString("APNIC-GRS"));
        when(grsDao.getCurrentObjectIds()).thenReturn(Lists.newArrayList(1, 2, 3));
        when(authoritativeResource.isMaintainedByRirCombined(any(RpslObject.class))).thenReturn(true);

        doAnswer(new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                final ObjectHandler objectHandler = (ObjectHandler) invocation.getArguments()[1];

                objectHandler.handle(RpslObject.parse("" +
                        "mntner: MODIFY-MNT\n" +
                        "mnt-by: CREATE-MNT\n"));

                objectHandler.handle(RpslObject.parse("" +
                        "mntner: CREATE-MNT\n" +
                        "mnt-by: CREATE-MNT\n"));

                objectHandler.handle(RpslObject.parse("" +
                        "mntner: NOOP-MNT\n"));

                return null;
            }
        }).when(grsSource).handleObjects(any(File.class), any(ObjectHandler.class));

        when(updateResultUpdate.hasMissingReferences()).thenReturn(true);

        final GrsObjectInfo grsObjectInfo1 = new GrsObjectInfo(1, 1, RpslObject.parse("mntner: MODIFY-MNT"));
        when(grsDao.find("MODIFY-MNT", ObjectType.MNTNER)).thenReturn(grsObjectInfo1);

        final GrsObjectInfo grsObjectInfo2 = new GrsObjectInfo(2, 2, RpslObject.parse("mntner:         NOOP-MNT\nsource:         APNIC-GRS"));
        when(grsDao.find("NOOP-MNT", ObjectType.MNTNER)).thenReturn(grsObjectInfo2);

        subject.grsImport(grsSource, false);

        verify(grsDao).createObject(RpslObject.parse("" +
                "mntner:         CREATE-MNT\n" +
                "mnt-by:         CREATE-MNT\n" +
                "source:         APNIC-GRS"));

        verify(grsDao).updateObject(grsObjectInfo1, RpslObject.parse("" +
                "mntner:         MODIFY-MNT\n" +
                "mnt-by:         CREATE-MNT\n" +
                "source:         APNIC-GRS"));

        verify(grsDao).updateIndexes(0);
        verify(grsDao, times(1)).updateObject(any(GrsObjectInfo.class), any(RpslObject.class));

        verify(grsDao).deleteObject(3);
    }
}
