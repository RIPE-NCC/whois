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
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

public class AutnumMassupdateEmailListGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger("AutnumMassupdateEmailListGenerator");
    private static final String ARG_OUTPUT_FILENAME = "output-filename";
    private static final String ARG_DBPASSWORD = "db-password";

    public static void main(final String[] args) throws Exception {
        LogUtil.initLogger();

        final OptionSet options = setupOptionParser().parse(args);
        final File outputFile = new File((String)options.valueOf(ARG_OUTPUT_FILENAME));
        initOutputFile(outputFile);

        final AutnumMassupdateEmailListGenerator emailGenerator = new AutnumMassupdateEmailListGenerator(outputFile, (String)options.valueOf(ARG_DBPASSWORD));
        emailGenerator.executeForAutnum();
    }

    private static OptionParser setupOptionParser() {
        final OptionParser parser = new OptionParser();
        parser.accepts(ARG_OUTPUT_FILENAME).withRequiredArg().required();
        parser.accepts(ARG_DBPASSWORD).withRequiredArg().required();
        return parser;
    }

    private static void initOutputFile(final File outputFile) throws IOException {
        if (outputFile.exists()) {
            FileUtils.write(outputFile, "");
        }
    }

    private final JdbcTemplate jdbcTemplate;
    private final File outputFile;
    public AutnumMassupdateEmailListGenerator(final File outputFile, final String dbPassword) throws SQLException {
        this.outputFile = outputFile;
        jdbcTemplate = new JdbcTemplate(new SimpleDriverDataSource(new Driver(), "jdbc:mysql://dbc-whois5.ripe.net/WHOIS_UPDATE_RIPE", "rdonly", dbPassword));
    }

    public Map<CIString, Set<CIString>> executeForAutnum() {
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

                        final Set<CIString> emailAddresses = Sets.newHashSet();
                        final IndexStrategy indexStrategy = IndexStrategies.get(AttributeType.MNT_BY);
                        for (CIString mntBy : object.getValuesForAttribute(AttributeType.MNT_BY)) {
                            for (RpslObjectInfo objectInfo : indexStrategy.findInIndex(jdbcTemplate, mntBy, ObjectType.MNTNER)) {
                                final RpslObject mntner = JdbcRpslObjectOperations.getObjectById(jdbcTemplate, objectInfo.getObjectId());
                                emailAddresses.addAll(mntner.getValuesForAttribute(AttributeType.UPD_TO));
                            }
                        }

                        for (CIString emailAddress : emailAddresses) {
                            Set<CIString> autnums = autnumsPerEmail.get(emailAddress);
                            if (autnums == null) {
                                autnums = Sets.newHashSet();
                            }
                            autnums.add(object.getKey());
                            autnumsPerEmail.put(emailAddress, autnums);
                        }
                    }
                });

        print(autnumsPerEmail);
        return autnumsPerEmail;
    }

    private void print(final Map<CIString, Set<CIString>> emailAutnumsMap) {
        final Joiner joiner = Joiner.on(',');
        final boolean append = Boolean.TRUE;

        for (CIString email : emailAutnumsMap.keySet()) {
            try {
                FileUtils.write(outputFile, String.format("%s | %s", email, joiner.join(emailAutnumsMap.get(email))), append);
            } catch (IOException e) {
                LOGGER.info("Error writing email {}: {},\n{}",email, joiner.join(emailAutnumsMap.get(email)), e.getMessage());
            }
        }
    }
}
