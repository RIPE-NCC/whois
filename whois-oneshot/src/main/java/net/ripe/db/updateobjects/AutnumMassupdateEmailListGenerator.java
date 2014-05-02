package net.ripe.db.updateobjects;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mysql.jdbc.Driver;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.ripe.db.LogUtil;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations;
import net.ripe.db.whois.common.dao.jdbc.JdbcStreamingHelper;
import net.ripe.db.whois.common.dao.jdbc.index.IndexStrategies;
import net.ripe.db.whois.common.dao.jdbc.index.IndexStrategy;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.io.RpslObjectFileReader;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *  Generates list of resources per emails on this format
 *    test@ripe.net | AS123, AS245, AS378
 *
 *  Example commandline arguments:
 *  --output-autnum autnumemails --output-inetnum legacyinetnumemails --db-password <rdonly password>
 *
 *  Expects to read a file called 'legacyInetnums' which contains inetnums in this format:
 *    159.100.128.0 - 159.100.159.255
 *    one inetnum per line
 *
 *  Also expects the aut-num splitfile, 'ripe.db.aut-num.gz'
 */
public class AutnumMassupdateEmailListGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger("AutnumMassupdateEmailListGenerator");
    private static final String ARG_OUTPUTFILE_AUTNUM = "output-autnum";
    private static final String ARG_OUTPUTFILE_INETNUM = "output-inetnum";
    private static final String ARG_DBPASSWORD = "db-password";

    public static void main(final String[] args) throws Exception {
        LogUtil.initLogger();

        final OptionSet options = setupOptionParser().parse(args);
        final File outputAutnum = new File((String)options.valueOf(ARG_OUTPUTFILE_AUTNUM));
        final File outputInetnum = new File((String)options.valueOf(ARG_OUTPUTFILE_INETNUM));
        initOutputFile(outputAutnum);
        initOutputFile(outputInetnum);

        final AutnumMassupdateEmailListGenerator emailGenerator = new AutnumMassupdateEmailListGenerator((String)options.valueOf(ARG_DBPASSWORD));
        emailGenerator.executeForAutnumFromSplitfile("ripe.db.aut-num.gz", outputAutnum);
        emailGenerator.executeForInetnum(outputInetnum);
    }

    private static OptionParser setupOptionParser() {
        final OptionParser parser = new OptionParser();
        parser.accepts(ARG_OUTPUTFILE_AUTNUM).withRequiredArg().required();
        parser.accepts(ARG_OUTPUTFILE_INETNUM).withRequiredArg().required();
        parser.accepts(ARG_DBPASSWORD).withRequiredArg().required();
        return parser;
    }

    private static void initOutputFile(final File outputFile) throws IOException {
        if (outputFile.exists()) {
            FileUtils.write(outputFile, "");
        }
    }

    private final JdbcTemplate jdbcTemplate;
    public AutnumMassupdateEmailListGenerator(final String dbPassword) throws SQLException {
        jdbcTemplate = new JdbcTemplate(new SimpleDriverDataSource(new Driver(), "jdbc:mysql://dbc-whois5.ripe.net/WHOIS_UPDATE_RIPE", "rdonly", dbPassword));
    }

    private int counter = 0;
    public void executeForAutnum(final File output) {
        //         email        autnumkeys
        final Map<CIString, Set<CIString>> autnumsPerEmail = Maps.newHashMap();
        JdbcStreamingHelper.executeStreaming(jdbcTemplate,
                "SELECT object_id, object " +
                        "FROM last " +
                        "WHERE sequence_id != 0 " +
                        "AND object_type = 2",
                new RowCallbackHandler() {
                    @Override
                    public void processRow(final ResultSet rs) throws SQLException {
                        final int objectId = rs.getInt(1);
                        RpslObject object;
                        try {
                            object = RpslObject.parse(objectId, rs.getBytes(2));
                        } catch (RuntimeException e) {
                            LOGGER.warn("Unable to parse RPSL object with object_id: {}", objectId);
                            return;
                        }
                        counter++;
                        if (counter % 100 == 0) {
                            LOGGER.info("autnums {}", counter);
                        }
                        collectResourcesPerEmail(object, autnumsPerEmail);
                    }
                });

        print(autnumsPerEmail, output);
        LOGGER.info("finished executing autnums");
    }

    public void executeForAutnumFromSplitfile(final String splitFile, final File output) {
        final Map<CIString, Set<CIString>> autnumsPerEmail = Maps.newHashMap();
        for (final String nextObject : new RpslObjectFileReader(splitFile)) {
            final RpslObject object;
            try {
                object = RpslObject.parse(nextObject);
            } catch (Exception e) {
                LOGGER.info("Malformed RPSL: \n" + nextObject, e);
                continue;
            }

            counter++;
            if (counter % 100 == 0) {
                LOGGER.info("autnums {}", counter);
            }
            collectResourcesPerEmail(object, autnumsPerEmail);
        }
        print(autnumsPerEmail, output);
        LOGGER.info("finished executing autnums");
    }


    public void executeForInetnum(final File output) throws IOException {
//                 email         inetnums
        final Map<CIString, Set<CIString>> inetnumsPerEmail = Maps.newHashMap();
        final LineIterator iterator = FileUtils.lineIterator(new File("legacyInetnums"));
        counter = 0;
        while (iterator.hasNext()) {
            final String line = iterator.nextLine().trim();
            final IndexStrategy indexStrategy = IndexStrategies.get(AttributeType.INETNUM);
            final List<RpslObjectInfo> inetnumInfos = indexStrategy.findInIndex(jdbcTemplate, line, ObjectType.INETNUM);
            if (inetnumInfos.size() != 1) {
                LOGGER.info("skipping {}, found {} in index", line, inetnumInfos.size());
                continue;
            }

            final RpslObject inetnum = JdbcRpslObjectOperations.getObjectById(jdbcTemplate, inetnumInfos.get(0).getObjectId());

            counter++;
            if (counter % 100 == 0) {
                LOGGER.info("inetnums {}", counter);
            }
            collectResourcesPerEmail(inetnum, inetnumsPerEmail);
        }

        print(inetnumsPerEmail, output);
    }

    private void collectResourcesPerEmail(final RpslObject object, final Map<CIString, Set<CIString>> resourcesPerEmail) {
        final Set<CIString> emailAddresses = Sets.newHashSet();
        final IndexStrategy indexStrategy = IndexStrategies.get(AttributeType.MNTNER);
        for (CIString mntBy : object.getValuesForAttribute(AttributeType.MNT_BY)) {
            for (RpslObjectInfo objectInfo : indexStrategy.findInIndex(jdbcTemplate, mntBy, ObjectType.MNTNER)) {
                RpslObject mntner;
                try {
                    mntner = JdbcRpslObjectOperations.getObjectById(jdbcTemplate, objectInfo.getObjectId());
                } catch (EmptyResultDataAccessException e) {
                    LOGGER.info("can't seem to find {} with oid {} in the database", objectInfo.getKey(), objectInfo.getObjectId());
                    continue;
                }
                emailAddresses.addAll(mntner.getValuesForAttribute(AttributeType.UPD_TO));
            }
        }


        for (CIString emailAddress : emailAddresses) {
            Set<CIString> inetnums = resourcesPerEmail.get(emailAddress);
            if (inetnums == null) {
                inetnums = Sets.newHashSet();
            }
            inetnums.add(object.getKey());
            resourcesPerEmail.put(emailAddress, inetnums);
        }
    }

    private void print(final Map<CIString, Set<CIString>> emailAutnumsMap, final File outputFile) {
        final Joiner joiner = Joiner.on(',');
        final boolean append = Boolean.TRUE;

        for (CIString email : emailAutnumsMap.keySet()) {
            try {
                FileUtils.write(outputFile, String.format("%s | %s\n", email, joiner.join(emailAutnumsMap.get(email))), append);
            } catch (IOException e) {
                LOGGER.info("Error writing email {}: {},\n{}",email, joiner.join(emailAutnumsMap.get(email)), e.getMessage());
            }
        }

        // add denis' email so that he knows when the mailing is done
        try {
            FileUtils.write(outputFile, String.format("denis@ripe.net | AS123"), append);
        } catch (IOException e) {
            LOGGER.info("Failed to generate denis email entry");
        }
    }
}
