package net.ripe.db.check;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ripe.db.LogUtil;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.zip.GZIPInputStream;

public class CheckDatabaseDump {
    private static final Logger LOGGER = LoggerFactory.getLogger(CheckDatabaseDump.class);

    private final Map<File, File> comparisonMap;

    public CheckDatabaseDump(final File legacyFolder, final File rdpFolder, final ObjectType... objectTypes) {
        comparisonMap = Maps.newTreeMap();

        for (final ObjectType objectType : objectTypes) {
            final File lgcFullFile = new File(legacyFolder, String.format("db.%s.gz", objectType.getName()));
            final File rdpFullFile = new File(rdpFolder, String.format("internal/ripe.db.%s.gz", objectType.getName()));
            comparisonMap.put(lgcFullFile, rdpFullFile);

            final File lgcDummyFile = new File(legacyFolder, String.format("dummy.%s.gz", objectType.getName()));
            final File rdpDummyFile = new File(rdpFolder, String.format("dbase/ripe.db.%s.gz", objectType.getName()));
            comparisonMap.put(lgcDummyFile, rdpDummyFile);
        }

        for (final Map.Entry<File, File> entry : comparisonMap.entrySet()) {
            checkFile(entry.getKey());
            checkFile(entry.getValue());
        }
    }

    private void checkFile(final File file) {
        final String filename = file.getAbsolutePath();

        Validate.isTrue(file.exists(), "File does not exist: " + filename);
        Validate.isTrue(file.isFile(), "Not a file: " + filename);
        Validate.isTrue(file.canRead(), "File can not be read: " + filename);
    }

    public void compare() throws IOException {
        for (final Map.Entry<File, File> entry : comparisonMap.entrySet()) {
            Scanner lgcScanner = null;
            Scanner rdpScanner = null;
            try {
                lgcScanner = getScanner(entry.getKey());
                rdpScanner = getScanner(entry.getValue());

                compareFiles(lgcScanner, rdpScanner);

            } finally {
                closeScanner(lgcScanner);
                closeScanner(rdpScanner);
            }
        }
    }

    private void compareFiles(final Scanner lgcScanner, final Scanner rdpScanner) {
        final Set<AttributeType> ignoreAttributes = Sets.newEnumSet(Lists.newArrayList(AttributeType.DESCR, AttributeType.AUTH, AttributeType.REMARKS, AttributeType.CHANGED, AttributeType.PING_HDL), AttributeType.class);

        while (true) {
            final RpslObject lgcObject = getNextObject(lgcScanner);
            final RpslObject rdpObject = getNextObject(rdpScanner);

            if (lgcObject == null && rdpObject == null) {
                return;
            }

            if (lgcObject == null || rdpObject == null) {
                logDifference("Different number of objects", lgcObject, rdpObject);
                continue;
            }

            for (final AttributeType attributeType : AttributeType.values()) {
                if (ignoreAttributes.contains(attributeType)) {
                    continue;
                }

                final List<RpslAttribute> lgcAttributes = lgcObject.findAttributes(attributeType);
                final List<RpslAttribute> rdpAttributes = rdpObject.findAttributes(attributeType);

                if (!lgcAttributes.equals(rdpAttributes)) {
                    logDifference("Different attributes: " + attributeType.getName(), lgcObject, rdpObject);
                }
            }
        }
    }

    private void logDifference(final String message, final RpslObject lgcObject, final RpslObject rdpObject) {
        LOGGER.warn("{}, \n\nlgc:\n\n{}\n\nrdp:\n\n{}", message, lgcObject, rdpObject);
    }

    private Scanner getScanner(final File file) throws IOException {
        checkFile(file);

        LOGGER.info("Using {}", file.getAbsolutePath());
        return new Scanner(new GZIPInputStream(new FileInputStream(file)), Charsets.ISO_8859_1.name()).useDelimiter("\n\n");
    }

    private RpslObject getNextObject(final Scanner scanner) {
        while (true) {
            if (!scanner.hasNext()) {
                return null;
            }

            String lgcObjectString = scanner.next();
            if (!lgcObjectString.startsWith("#")) {
                try {
                    return RpslObject.parse(lgcObjectString);
                } catch (RuntimeException e) {
                    LOGGER.warn("Unable to parse: {} ", lgcObjectString);
                    return RpslObject.parse("mntner: DUMMY_PLACEHOLDER");
                }
            }
        }
    }

    private void closeScanner(final Scanner scanner) {
        if (scanner != null) {
            try {
                scanner.close();
            } catch (RuntimeException e) {
                LOGGER.error("Closing scanner", e);
            }
        }
    }

    public static void main(final String[] args) {
        LogUtil.initLogger();

        final File lgcFolder = new File(System.getProperty("user.home"), "/tmp/lgc");
        final File rdpFolder = new File(System.getProperty("user.home"), "/tmp/rdp");

        try {
            new CheckDatabaseDump(lgcFolder, rdpFolder, ObjectType.values()).compare();

            LOGGER.info("Comparison finished");
        } catch (IOException e) {
            LOGGER.error("Comparison failed", e);
        }
    }
}
