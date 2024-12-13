package net.ripe.db.whois.scheduler.task.export;

import com.google.common.collect.Sets;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.scheduler.AbstractSchedulerIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.FileCopyUtils;

import java.util.HashSet;
import java.util.regex.MatchResult;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

@Tag("IntegrationTest")
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

    @BeforeEach
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
                    "nic-hdl: PN" + i + "-TEST\n" +
                    "source: TEST");

            databaseHelper.addObject(personObject);
            objects.add(personObject);

            final RpslObject roleObject = RpslObject.parse("" +
                    "role: Test role " + i + "\n" +
                    "nic-hdl: ROLE" + i + "-TEST\n" +
                    "source: TEST");

            databaseHelper.addObject(roleObject);
            objects.add(roleObject);
        }

        queryServer.start();
    }

    @AfterEach
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
            checkFile("public/split/test.db." + objectType.getName() + ".gz");
            checkFile("internal/split/test.db." + objectType.getName() + ".gz");
        }

        checkFile("public/TEST.CURRENTSERIAL", "120");

        checkNotDuplicateNicHdl("public/test.db.gz");

        checkFile("public/test.db.gz",
                "person:         Placeholder Person Object\n",
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

        checkFile("public/split/test.db.person.gz", "person:         Placeholder Person Object");
        checkFile("public/split/test.db.role.gz", "role:           Placeholder Role Object");

        checkFile("public/split/test.db.mntner.gz",
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

        checkFile("internal/split/test.db.person.gz",
                "person:         Test person 0",
                "person:         Test person 1",
                "person:         Test person 2",
                "person:         Test person 3",
                "person:         Test person 4",
                "" +
                "person:         Test person 9\n" +
                "nic-hdl:        PN9-TEST\n" +
                "source:         TEST");

        checkFile("internal/split/test.db.role.gz",
                "role:           Test role 0",
                "role:           Test role 1",
                "role:           Test role 2",
                "role:           Test role 3",
                "role:           Test role 4",
                "" +
                "role:           Test role 9\n" +
                "nic-hdl:        ROLE9-TEST\n" +
                "source:         TEST");

        checkFile("internal/split/test.db.mntner.gz",
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
                "nic-hdl:        AR1-TEST\n" +
                "abuse-mailbox:  abuse@mailbox.com\n" +
                "source:         TEST"));

        databaseHelper.addObject(RpslObject.parse("" +
                "organisation:   ORG1\n" +
                "abuse-c:        AR1-TEST\n" +
                "source:         TEST"));

        sourceContext.removeCurrentSource();

        rpslObjectsExporter.export();

        assertThat(tmpDir.exists(), is(false));
        assertThat(exportDir.exists(), is(true));

        for (final ObjectType objectType : ObjectType.values()) {
            checkFile("public/split/test.db." + objectType.getName() + ".gz");
            checkFile("internal/split/test.db." + objectType.getName() + ".gz");
        }

        checkNotDuplicateNicHdl("public/test.db.gz");
        checkFile("public/split/test.db.person.gz", "person:         Placeholder Person Object");
        checkFile("public/split/test.db.role.gz", "role:           Placeholder Role Object");

        checkFile("public/split/test.db.role.gz", "" +
                "role:           Abuse role\n" +
                "nic-hdl:        AR1-TEST\n" +
                "abuse-mailbox:  abuse@mailbox.com\n" +
                "source:         TEST");

        checkFile("public/split/test.db.organisation.gz", "" +
                "organisation:   ORG1\n" +
                "abuse-c:        AR1-TEST\n" +
                "source:         TEST");

        checkFile("internal/split/test.db.role.gz", "" +
                "role:           Abuse role\n" +
                "nic-hdl:        AR1-TEST\n" +
                "abuse-mailbox:  abuse@mailbox.com\n" +
                "source:         TEST");

        checkFile("internal/split/test.db.organisation.gz", "" +
                "organisation:   ORG1\n" +
                "abuse-c:        AR1-TEST\n" +
                "source:         TEST");
    }

    private void checkNotDuplicateNicHdl(final String name) throws IOException {
        final String contents = getContents(name);

        final Pattern pattern = Pattern.compile("^nic-hdl: .*", Pattern.MULTILINE);
        final Matcher matcher = pattern.matcher(contents);

        final List<String> results = matcher.results()
                .map(MatchResult::group)
                .toList();
        assertThat(results.size() == new HashSet<>(results).size(), is(true));
    }

    private void checkFile(final String name, final String... expectedContents) throws IOException {
        final String contents = getContents(name);

        for (final String expectedContent : expectedContents) {
            assertThat(contents, containsString(expectedContent));
        }
    }

    private String getContents(String name) throws IOException {
        final File file = new File(exportDir, name);

        assertThat(file.exists(), is(true));

        final Reader reader;
        final boolean isDumpFile = name.endsWith(".gz");

        if (isDumpFile) {
            reader = new InputStreamReader(new GZIPInputStream(new BufferedInputStream(new FileInputStream(file))), StandardCharsets.ISO_8859_1);
        } else {
            reader = new InputStreamReader(new BufferedInputStream(new FileInputStream(file)), StandardCharsets.ISO_8859_1);
        }

        final String contents = FileCopyUtils.copyToString(reader);

        if (isDumpFile) {
            assertThat(contents, startsWith("" +
                    "#\n" +
                    "# The contents of this file are subject to \n" +
                    "# RIPE Database Terms and Conditions\n" +
                    "#\n" +
                    "# https://docs.db.ripe.net/terms-conditions.html\n" +
                    "#"));
        }
        return contents;
    }


    @Test
    public void export_mix_of_sources() throws IOException {
        databaseHelper.addObject(RpslObject.parse("" +
                "aut-num:        AS252\n" +
                "source:         TEST"));
        databaseHelper.addObject(RpslObject.parse("" +
                "aut-num:        AS251\n" +
                "source:         TEST-NONAUTH"));
        databaseHelper.addObject(RpslObject.parse("" +
                "as-set:         AS251:AS-ALL\n" +
                "source:         TEST-NONAUTH"));

        sourceContext.removeCurrentSource();
        rpslObjectsExporter.export();

        assertThat(tmpDir.exists(), is(false));
        assertThat(exportDir.exists(), is(true));

        for (final ObjectType objectType : ObjectType.values()) {
            checkFile("public/split/test.db." + objectType.getName() + ".gz");
            checkFile("internal/split/test.db." + objectType.getName() + ".gz");
            if (ExportFileWriterFactory.NONAUTH_OBJECT_TYPES.contains(objectType)) {
                checkFile("public/split/test-nonauth.db." + objectType.getName() + ".gz");
                checkFile("internal/split/test-nonauth.db." + objectType.getName() + ".gz");
            }
        }

        checkNotDuplicateNicHdl("public/test.db.gz");
        checkFile("public/split/test.db.role.gz", "role:           Placeholder Role Object");
        checkFile("public/split/test.db.aut-num.gz", "aut-num:        AS252");
        checkFile("public/split/test-nonauth.db.aut-num.gz", "aut-num:        AS251");
        checkFile("public/split/test-nonauth.db.as-set.gz", "as-set:         AS251:AS-ALL");
    }


    @Test
    public void export_mix_of_sources_when_existing_dummy() throws IOException {
        databaseHelper.addObject(RpslObject.parse("" +
                "aut-num:        AS252\n" +
                "source:         TEST"));
        databaseHelper.addObject(RpslObject.parse("" +
                "aut-num:        AS251\n" +
                "source:         TEST-NONAUTH"));
        databaseHelper.addObject(RpslObject.parse("" +
                "as-set:         AS251:AS-ALL\n" +
                "source:         TEST-NONAUTH"));
        databaseHelper.addObject(RpslObject.parse("" +
                "role:           ROLE Account of x.ORGanization\n" +
                "nic-hdl:        ROLE-RIPE\n" +
                "abuse-mailbox:   abuse@speednic.eu\n" +
                "source:         TEST"));

        sourceContext.removeCurrentSource();
        rpslObjectsExporter.export();

        checkNotDuplicateNicHdl("public/test.db.gz");
        checkFile("public/split/test.db.role.gz", "role:           Placeholder Role Object");
        checkFile("public/split/test.db.aut-num.gz", "aut-num:        AS252");
        checkFile("public/split/test-nonauth.db.aut-num.gz", "aut-num:        AS251");
        checkFile("public/split/test-nonauth.db.as-set.gz", "as-set:         AS251:AS-ALL");
    }

}
