package net.ripe.db.updateobjects;

import com.google.common.base.Throwables;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.ripe.db.LogUtil;
import net.ripe.db.whois.api.rest.client.RestClient;
import net.ripe.db.whois.api.rest.client.RestClientException;
import net.ripe.db.whois.common.io.RpslObjectFileReader;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
    This class is an one shot operation to add the status attribute to all autnums that do not have it.
    The arguments for executing the class are --override person,override_password
 */
public class MassUpdateAutnumStatus {
    enum UpdateStatus {
        STATUS_UPDATE_SUCCESS,
        STATUS_ALREADY_SET,
        STATUS_UPDATE_ERROR
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(MassUpdateAutnumStatus.class.getSimpleName());
    private static final String OVERRIDE = "override";
    private static final File OUTPUT_FILE = new File("massUpdateAutnumStatus.txt");

    private static final String SPLIT_FILE = "/ncc/ftp/ripe/dbase/split/ripe.db.aut-num.gz";
//    private static final String SPLIT_FILE = "ripe.db.aut-num.gz";

    private static final RestClient restClient = new RestClient("http://dbc-dev2:1080/whois", "RIPE");

    public static void main(final String[] argv) throws Exception {
        LogUtil.initLogger();
        initOutputFile();

        final OptionSet options = setupOptionParser().parse(argv);
        Validate.isTrue(options.hasArgument(OVERRIDE));

        final String override = options.valueOf(OVERRIDE).toString();

        append("Updating autnums using override user: " + override.split(",")[0]);

        updateAutnums(override + ",mass_setting_autnum_status {notify=false}");

        LOGGER.debug("done");
    }

    private static OptionParser setupOptionParser() {
        final OptionParser parser = new OptionParser();
        parser.accepts(OVERRIDE).withRequiredArg();
        return parser;
    }

    public static void updateAutnums(final String override) {
        int countSplitfileObjects = 0;
        int countNotUpdatedObjects = 0;
        int countUpdatedObjects = 0;
        int countAlreadySet = 0;

        for (final String nextObject : new RpslObjectFileReader(SPLIT_FILE)) {
            final RpslObject rpslObject;
            try {
                rpslObject = RpslObject.parse(nextObject);
            } catch (Exception e) {
                LOGGER.info("Malformed RPSL: \n" + nextObject, e);
                append("Malformed RPSL: " + nextObject);

                continue;
            }

            countSplitfileObjects++;

            final UpdateStatus updateStatus = updateAutnum(rpslObject.getKey().toString(), override);

            append(String.format("AUT_NUM:%s:%s",rpslObject.getKey().toString(), updateStatus.name()));

            switch (updateStatus) {
                case STATUS_UPDATE_SUCCESS:
                    countUpdatedObjects++;
                    break;
                case STATUS_UPDATE_ERROR:
                    countNotUpdatedObjects++;
                    break;
                case STATUS_ALREADY_SET:
                    countAlreadySet++;
                    break;
                default:
                    break;
            }
        }

        append(String.format("%s autnums found in split files.", countSplitfileObjects));
        append(String.format("%s autnums updated successfully.", countUpdatedObjects));
        append(String.format("%s autnums with status already set.", countAlreadySet));
        append(String.format("%s autnums not updated.", countNotUpdatedObjects));
    }

    public static UpdateStatus updateAutnum(final String key, final String override) {
        try {
            LOGGER.info("Updating autnum: " + key);

            //do a lookup first to get the latest version of the object. The split files may have old info.
            final RpslObject object = restClient.request().lookup(ObjectType.AUT_NUM, key);

            if (!object.getValuesForAttribute(AttributeType.STATUS).isEmpty()) {
                return UpdateStatus.STATUS_ALREADY_SET;
            }

            final RpslObject updatedAutnum = restClient.request().addParam("unformatted", "").addParam("override", override).update(object);

            if (updatedAutnum.containsAttribute(AttributeType.STATUS)) {
                return UpdateStatus.STATUS_UPDATE_SUCCESS;
            }
        } catch (RestClientException e) {
            LOGGER.info(e.getErrorMessages().toString());
            append(e.getErrorMessages().toString());
        } catch (Exception e1) {
            LOGGER.debug("Unexpected error", e1);
            append(Throwables.getStackTraceAsString(e1));
        }
        return UpdateStatus.STATUS_UPDATE_ERROR;
    }

    private static void append(String... lines) {
        //Closing the file on each write to prevent loss of logged data if script crashes
        try {
            FileUtils.writeLines(OUTPUT_FILE, Arrays.asList(lines), true);
        } catch (IOException e) {
            LOGGER.info(e.getMessage(), e);
        }
    }

    private static void initOutputFile() throws IOException {
        //Do not proceed if it cannot write the output file.
        if (OUTPUT_FILE.exists()) {
            FileUtils.write(OUTPUT_FILE, "");
        }
    }
}
