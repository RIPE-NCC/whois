package net.ripe.db.whois.scheduler.task.export;

import com.google.common.base.Charsets;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.scheduler.AbstractSchedulerIntegrationTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@Category(IntegrationTest.class)
public class ExportDatabaseTestIntegration extends AbstractSchedulerIntegrationTest {
    @Autowired RpslObjectsExporter rpslObjectsExporter;
    @Autowired SourceContext sourceContext;

    File exportDir;

    @Value("${dir.rpsl.export}")
    public void setExportDir(final String exportDirName) {
        this.exportDir = new File(exportDirName);
    }

    File tmpDir;

    @Value("${dir.rpsl.export.tmp}")
    public void setTmpDir(final String tmpDirNAme) {
        this.tmpDir = new File(tmpDirNAme);
    }

    Set<RpslObject> objects;

    @Before
    public void setupServer() {
        objects = Sets.newHashSet();

        for (int i = 0; i < 100; i++) {
            final RpslObject rpslObject = RpslObject.parse("" +
                    "mntner:         DEV-MNT" + i + "\n" +
                    "auth:           MD5-PW $1$xNv6umMG$cBd9DXqWEpsqeBq2AUjGy/\n" +
                    "source:         TEST");

            databaseHelper.addObject(rpslObject);
            objects.add(rpslObject);
        }

        for (int i = 0; i < 10; i++) {
            final RpslObject personObject = RpslObject.parse("" +
                    "person: Test person " + i + "\n" +
                    "nic-hdl: PN" + i + "-RIPE\n" +
                    "source: TEST");

            databaseHelper.addObject(personObject);
            objects.add(personObject);

            final RpslObject roleObject = RpslObject.parse("" +
                    "role: Test role " + i + "\n" +
                    "nic-hdl: ROLE" + i + "-RIPE\n" +
                    "source: TEST");

            databaseHelper.addObject(roleObject);
            objects.add(roleObject);
        }

        queryServer.start();
    }

    @After
    public void tearDownServer() {
        queryServer.stop(true);
    }

    @Test
    public void export() throws IOException {
        sourceContext.removeCurrentSource();

        rpslObjectsExporter.export();

        assertThat(tmpDir.exists(), is(false));
        assertThat(exportDir.exists(), is(true));

        for (final ObjectType objectType : ObjectType.values()) {
            checkFile("dbase/split/ripe.db." + objectType.getName() + ".gz");
            checkFile("internal/split/ripe.db." + objectType.getName() + ".gz");
        }

        checkFile("dbase/RIPE.CURRENTSERIAL", "120");

        checkFile("dbase/ripe.db.gz",
                "person:         Placeholder Person Object\n",
                "role:           Placeholder Role Object\n",
                "mntner:         DEV-MNT0\n",
                "mntner:         DEV-MNT1\n",
                "mntner:         DEV-MNT2\n",
                "mntner:         DEV-MNT3\n",
                "mntner:         DEV-MNT4\n",
                "mntner:         DEV-MNT5\n",
                "mntner:         DEV-MNT6\n",
                "mntner:         DEV-MNT7\n",
                "" +
                        "mntner:         DEV-MNT99\n" +
                        "auth:           MD5-PW $1$SaltSalt$DummifiedMD5HashValue.   # Real value hidden for security\n" +
                        "source:         TEST\n" +
                        "remarks:        ****************************\n" +
                        "remarks:        * THIS OBJECT IS MODIFIED\n" +
                        "remarks:        * Please note that all data that is generally regarded as personal\n" +
                        "remarks:        * data has been removed from this object.\n" +
                        "remarks:        * To view the original object, please query the RIPE Database at:\n" +
                        "remarks:        * http://www.ripe.net/whois\n" +
                        "remarks:        ****************************\n");

        checkFile("dbase/split/ripe.db.person.gz", "person:         Placeholder Person Object");
        checkFile("dbase/split/ripe.db.role.gz", "role:           Placeholder Role Object");

        checkFile("dbase/split/ripe.db.mntner.gz",
                "mntner:         DEV-MNT0\n",
                "mntner:         DEV-MNT1\n",
                "mntner:         DEV-MNT2\n",
                "mntner:         DEV-MNT3\n",
                "mntner:         DEV-MNT4\n",
                "mntner:         DEV-MNT5\n",
                "mntner:         DEV-MNT6\n",
                "mntner:         DEV-MNT7\n",
                "" +
                        "mntner:         DEV-MNT99\n" +
                        "auth:           MD5-PW $1$SaltSalt$DummifiedMD5HashValue.   # Real value hidden for security\n" +
                        "source:         TEST\n" +
                        "remarks:        ****************************\n" +
                        "remarks:        * THIS OBJECT IS MODIFIED\n" +
                        "remarks:        * Please note that all data that is generally regarded as personal\n" +
                        "remarks:        * data has been removed from this object.\n" +
                        "remarks:        * To view the original object, please query the RIPE Database at:\n" +
                        "remarks:        * http://www.ripe.net/whois\n" +
                        "remarks:        ****************************\n");

        checkFile("internal/split/ripe.db.person.gz",
                "person:         Test person 0",
                "person:         Test person 1",
                "person:         Test person 2",
                "person:         Test person 3",
                "person:         Test person 4",
                "" +
                        "person:         Test person 9\n" +
                        "nic-hdl:        PN9-RIPE\n" +
                        "source:         TEST");

        checkFile("internal/split/ripe.db.role.gz",
                "role:           Test role 0",
                "role:           Test role 1",
                "role:           Test role 2",
                "role:           Test role 3",
                "role:           Test role 4",
                "" +
                        "role:           Test role 9\n" +
                        "nic-hdl:        ROLE9-RIPE\n" +
                        "source:         TEST");

        checkFile("internal/split/ripe.db.mntner.gz",
                "" +
                        "mntner:         DEV-MNT0\n" +
                        "auth:           MD5-PW $1$xNv6umMG$cBd9DXqWEpsqeBq2AUjGy/\n" +
                        "source:         TEST\n" +
                        "\n" +
                        "mntner:         DEV-MNT1\n" +
                        "auth:           MD5-PW $1$xNv6umMG$cBd9DXqWEpsqeBq2AUjGy/\n" +
                        "source:         TEST\n" +
                        "\n" +
                        "mntner:         DEV-MNT2\n" +
                        "auth:           MD5-PW $1$xNv6umMG$cBd9DXqWEpsqeBq2AUjGy/\n" +
                        "source:         TEST\n" +
                        "\n" +
                        "mntner:         DEV-MNT3\n" +
                        "auth:           MD5-PW $1$xNv6umMG$cBd9DXqWEpsqeBq2AUjGy/\n" +
                        "source:         TEST\n" +
                        "\n" +
                        "mntner:         DEV-MNT4\n" +
                        "auth:           MD5-PW $1$xNv6umMG$cBd9DXqWEpsqeBq2AUjGy/\n" +
                        "source:         TEST\n" +
                        "\n" +
                        "mntner:         DEV-MNT5\n" +
                        "auth:           MD5-PW $1$xNv6umMG$cBd9DXqWEpsqeBq2AUjGy/\n" +
                        "source:         TEST\n" +
                        "\n" +
                        "mntner:         DEV-MNT6\n" +
                        "auth:           MD5-PW $1$xNv6umMG$cBd9DXqWEpsqeBq2AUjGy/\n" +
                        "source:         TEST\n" +
                        "\n" +
                        "mntner:         DEV-MNT7\n" +
                        "auth:           MD5-PW $1$xNv6umMG$cBd9DXqWEpsqeBq2AUjGy/\n" +
                        "source:         TEST\n" +
                        "\n" +
                        "mntner:         DEV-MNT8\n" +
                        "auth:           MD5-PW $1$xNv6umMG$cBd9DXqWEpsqeBq2AUjGy/\n" +
                        "source:         TEST\n" +
                        "\n" +
                        "mntner:         DEV-MNT9\n" +
                        "auth:           MD5-PW $1$xNv6umMG$cBd9DXqWEpsqeBq2AUjGy/\n" +
                        "source:         TEST\n" +
                        "\n" +
                        "mntner:         DEV-MNT10\n" +
                        "auth:           MD5-PW $1$xNv6umMG$cBd9DXqWEpsqeBq2AUjGy/\n" +
                        "source:         TEST");
    }

    @Test
    public void export_role_with_abuse_mailbox() throws IOException {
        databaseHelper.addObject(RpslObject.parse("" +
                "role:           Abuse role\n" +
                "nic-hdl:        AR1-RIPE\n" +
                "abuse-mailbox:  abuse@mailbox.com\n" +
                "source:         TEST"));

        databaseHelper.addObject(RpslObject.parse("" +
                "organisation:   ORG1\n" +
                "abuse-c:        AR1-RIPE\n" +
                "source:         TEST"));

        sourceContext.removeCurrentSource();

        rpslObjectsExporter.export();

        assertThat(tmpDir.exists(), is(false));
        assertThat(exportDir.exists(), is(true));

        for (final ObjectType objectType : ObjectType.values()) {
            checkFile("dbase/split/ripe.db." + objectType.getName() + ".gz");
            checkFile("internal/split/ripe.db." + objectType.getName() + ".gz");
        }

        checkFile("dbase/split/ripe.db.person.gz", "person:         Placeholder Person Object");
        checkFile("dbase/split/ripe.db.role.gz", "role:           Placeholder Role Object");

        checkFile("dbase/split/ripe.db.role.gz", "" +
                "role:           Abuse role\n" +
                "nic-hdl:        AR1-RIPE\n" +
                "abuse-mailbox:  abuse@mailbox.com\n" +
                "source:         TEST");

        checkFile("dbase/split/ripe.db.organisation.gz", "" +
                "organisation:   ORG1\n" +
                "abuse-c:        AR1-RIPE\n" +
                "source:         TEST");

        checkFile("internal/split/ripe.db.role.gz", "" +
                "role:           Abuse role\n" +
                "nic-hdl:        AR1-RIPE\n" +
                "abuse-mailbox:  abuse@mailbox.com\n" +
                "source:         TEST");

        checkFile("internal/split/ripe.db.organisation.gz", "" +
                "organisation:   ORG1\n" +
                "abuse-c:        AR1-RIPE\n" +
                "source:         TEST");
    }

    @Test
    public void export_proposed_dummification() throws IOException {
        databaseHelper.addObject(RpslObject.parse("" +
                "inetnum:   193.0.0.0 - 193.255.255.255\n" +
                "netname:   TEST-RIPE\n" +
                "admin-c:   PN1-RIPE\n" +
                "tech-c:    PN1-RIPE\n" +
                "notify:    test@ripe.net\n" +
                "changed:   test@ripe.net 20120101\n" +
                "source:    TEST"));

        databaseHelper.addObject(RpslObject.parse("" +
                "organisation:  ORG-TO1-TEST\n" +
                "org-name:      Test Organisation\n" +
                "org-type:      OTHER\n" +
                "address:       Test Org\n" +
                "               Street\n" +
                "               1234 City\n" +
                "               Country\n" +
                "phone:         +12 3456 78\n" +
                "fax-no:        +12 234 567\n" +
                "e-mail:        test@ripe.net\n" +
                "source:        TEST"));

        databaseHelper.addObject(RpslObject.parse("" +
                "mntner:        TEST-MNT\n" +
                "descr:         description\n" +
                "upd-to:        test@ripe.net\n" +
                "auth:          X509-1\n" +
                "auth:          PGPKEY-AA\n" +
                "auth:          MD5-PW\n" +
                "mnt-nfy:       test@test.com\n" +
                "ref-nfy:       foo@bar.com\n" +
                "mnt-by:        TEST-MNT\n" +
                "source:        TEST"));

        databaseHelper.addObject(RpslObject.parse("" +
                "role:      Test Role1\n" +
                "address:   Street\n" +
                "address:   City\n" +
                "address:   Country\n" +
                "phone:     +12 345 678\n" +
                "fax-no:    +12 345 678\n" +
                "e-mail:    test@bar.com\n" +
                "nic-hdl:   ROLE-NIC\n" +
                "changed:   foo@test.net\n" +
                "source:    TEST"));

        databaseHelper.addObject(RpslObject.parse("" +
                "role:          Test Role2\n" +
                "address:       Street\n" +
                "address:       City\n" +
                "address:       Country\n" +
                "phone:         +12 345 678\n" +
                "fax-no:        +12 345 678\n" +
                "e-mail:        test@bar.com\n" +
                "abuse-mailbox: abuse@test.net\n" +
                "nic-hdl:       AB-NIC\n" +
                "changed:       foo@test.net\n" +
                "source:        TEST"));

        sourceContext.removeCurrentSource();

        rpslObjectsExporter.export();

        for (final ObjectType objectType : ObjectType.values()) {
            checkFile("dbase_new/split/ripe.db." + objectType.getName() + ".gz");
        }

        checkFile("dbase_new/split/ripe.db.inetnum.gz", "" +
                "inetnum:        193.0.0.0 - 193.255.255.255\n" +
                "netname:        TEST-RIPE\n" +
                "admin-c:        PN1-RIPE\n" +
                "tech-c:         PN1-RIPE\n" +
                "notify:         ***@ripe.net\n" +
                "changed:        ***@ripe.net 20120101\n" +
                "source:         TEST");

        checkFile("dbase_new/split/ripe.db.organisation.gz", "" +
                "organisation:   ORG-TO1-TEST\n" +
                "org-name:       Test Organisation\n" +
                "org-type:       OTHER\n" +
                "address:        Test Org\n" +
                "                Street\n" +
                "                1234 City\n" +
                "                Country\n" +
                "phone:          +12 3... ..\n" +
                "fax-no:         +12 2.. ...\n" +
                "e-mail:         ***@ripe.net\n" +
                "source:         TEST");

        checkFile("dbase_new/split/ripe.db.mntner.gz", "" +
                "mntner:         TEST-MNT\n" +
                "descr:          description\n" +
                "upd-to:         ***@ripe.net\n" +
                "auth:           X509-1\n" +
                "auth:           PGPKEY-AA\n" +
                "auth:           MD5-PW # Filtered\n" +
                "mnt-nfy:        ***@test.com\n" +
                "ref-nfy:        ***@bar.com\n" +
                "mnt-by:         TEST-MNT\n" +
                "source:         TEST");

        checkFile("dbase_new/split/ripe.db.role.gz", "" +
                "role:           Test Role1\n" +
                "address:        ***\n" +
                "address:        ***\n" +
                "address:        Country\n" +
                "phone:          +12 3.. ...\n" +
                "fax-no:         +12 3.. ...\n" +
                "e-mail:         ***@bar.com\n" +
                "nic-hdl:        ROLE-NIC\n" +
                "changed:        ***@test.net\n" +
                "source:         TEST");

        checkFile("dbase_new/split/ripe.db.role.gz", "" +
                "role:           Test Role2\n" +
                "address:        Street\n" +
                "address:        City\n" +
                "address:        Country\n" +
                "phone:          +12 345 678\n" +
                "fax-no:         +12 345 678\n" +
                "e-mail:         ***@bar.com\n" +
                "abuse-mailbox:  abuse@test.net\n" +
                "nic-hdl:        AB-NIC\n" +
                "changed:        ***@test.net\n" +
                "source:         TEST");
    }

    @Test
    public void export_tags_in_all_dumps() throws IOException {
        final RpslObject org = databaseHelper.addObject(RpslObject.parse("" +
                "organisation:  ORG-TO1-TEST\n" +
                "org-name:      Test Organisation\n" +
                "org-type:      OTHER\n" +
                "source:        TEST"));

        whoisTemplate.update("INSERT INTO tags VALUES (?, ?, ?)", org.getObjectId(), "bar", "Bar Data");
        whoisTemplate.update("INSERT INTO tags VALUES (?, ?, ?)", org.getObjectId(), "foo", "Foo Data");
        sourceContext.removeCurrentSource();

        rpslObjectsExporter.export();

        checkFile("internal/split/ripe.db.organisation.gz", "" +
                "Tags relating to 'ORG-TO1-TEST'", "bar # Bar Data", "foo # Foo Data");

        checkFile("dbase_new/split/ripe.db.organisation.gz",
                "Tags relating to 'ORG-TO1-TEST'", "bar # Bar Data", "foo # Foo Data");

        checkFile("dbase/split/ripe.db.organisation.gz",
                "Tags relating to 'ORG-TO1-TEST'", "bar # Bar Data", "foo # Foo Data");

        checkFile("dbase_new/ripe.db.gz",
                "Tags relating to 'ORG-TO1-TEST'", "bar # Bar Data", "foo # Foo Data");

        checkFile("dbase/ripe.db.gz",
                "Tags relating to 'ORG-TO1-TEST'", "bar # Bar Data", "foo # Foo Data");
    }

    private void checkFile(final String name, final String... expectedContents) throws IOException {
        final File file = new File(exportDir, name);

        assertThat(file.exists(), is(true));

        final Reader reader;
        final boolean isDumpFile = name.endsWith(".gz");

        if (isDumpFile) {
            reader = new InputStreamReader(new GZIPInputStream(new BufferedInputStream(new FileInputStream(file))), Charsets.ISO_8859_1);
        } else {
            reader = new InputStreamReader(new BufferedInputStream(new FileInputStream(file)), Charsets.ISO_8859_1);
        }

        final String contents = FileCopyUtils.copyToString(reader);

        if (isDumpFile) {
            assertThat(contents, startsWith("" +
                    "#\n" +
                    "# The contents of this file are subject to \n" +
                    "# RIPE Database Terms and Conditions\n" +
                    "#\n" +
                    "# http://www.ripe.net/db/support/db-terms-conditions.pdf\n" +
                    "#"));
        }

        for (final String expectedContent : expectedContents) {
            assertThat(contents, containsString(expectedContent));
        }
    }
}
