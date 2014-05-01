package net.ripe.db.updateobjects;

import com.google.common.base.Throwables;
import com.sun.istack.NotNull;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.ripe.db.LogUtil;
import net.ripe.db.whois.api.rest.client.RestClient;
import net.ripe.db.whois.api.rest.client.RestClientException;
import net.ripe.db.whois.api.rest.mapper.AttributeMapper;
import net.ripe.db.whois.api.rest.mapper.DirtyClientAttributeMapper;
import net.ripe.db.whois.api.rest.mapper.FormattedClientAttributeMapper;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectMapper;
import net.ripe.db.whois.common.io.RpslObjectFileReader;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * Mass Update Autnum Status script - set the status of all autnums (read from the split file) to the generated value.
 *
 * Command-line options:
 *
 *      --override-username : the override user
 *      --override-password : the override password
 *      --filename          : the input filename containing autnums split file
 *      --output-filename   : the output filename
 *      --rest-api-url      : the REST API url (for lookup/search/update operations)
 *      --source            : the source name (RIPE or TEST)
 *
 * For example:
 *
 *      --override-username dbint --override-password dbint --filename ripe.db.aut-num.gz --output-filename massUpdateAutnumStatus.txt --rest-api-url https://rest.db.ripe.net --source RIPE
 *      the split file can be found in /ncc/ftp/ripe/dbase/split/ripe.db.aut-num.gz
 */
public class MassUpdateAutnumStatus {
    enum UpdateStatus {
        STATUS_UPDATE_SUCCESS,
        STATUS_ALREADY_SET,
        STATUS_UPDATE_ERROR
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(MassUpdateAutnumStatus.class.getSimpleName());

    public static final String ARG_OVERRIDE_USERNAME = "override-username";
    public static final String ARG_OVERRIDE_PASSWORD = "override-password";
    public static final String ARG_FILENAME = "filename";
    public static final String ARG_OUTPUT_FILENAME = "output-filename";
    public static final String ARG_REST_API_URL = "rest-api-url";
    public static final String ARG_SOURCE = "source";

    private final String username;
    private final String password;
    private final RestClient restClient;
    private String filename;
    private File outputFile;

    public static void main(final String[] args) throws Exception {
        LogUtil.initLogger();

        final OptionSet options = setupOptionParser().parse(args);
        new MassUpdateAutnumStatus(
                (String)options.valueOf(ARG_FILENAME),
                (String)options.valueOf(ARG_OUTPUT_FILENAME),
                (String)options.valueOf(ARG_OVERRIDE_USERNAME),
                (String)options.valueOf(ARG_OVERRIDE_PASSWORD),
                (String)options.valueOf(ARG_REST_API_URL),
                (String)options.valueOf(ARG_SOURCE)).execute();

        LOGGER.debug("done");
    }

    public MassUpdateAutnumStatus(
            @NotNull final String filename,
            @NotNull final String outputFilename,
            @NotNull final String username,
            @NotNull final String password,
            @NotNull final String restApiUrl,
            @NotNull final String source) {
        this.filename = filename;
        this.outputFile = new File(outputFilename);
        this.username = username;
        this.password = password;
        this.restClient = createRestClient(restApiUrl, source);
    }

    private static OptionParser setupOptionParser() {
        final OptionParser parser = new OptionParser();
        parser.accepts(ARG_OVERRIDE_USERNAME).withRequiredArg().required();
        parser.accepts(ARG_OVERRIDE_PASSWORD).withRequiredArg().required();
        parser.accepts(ARG_FILENAME).withRequiredArg().required();
        parser.accepts(ARG_OUTPUT_FILENAME).withRequiredArg().required();
        parser.accepts(ARG_REST_API_URL).withRequiredArg().required();
        parser.accepts(ARG_SOURCE).withRequiredArg().required();
        return parser;
    }

    public void execute() throws Exception {
        initOutputFile(outputFile);

        int countSplitfileObjects = 0;
        int countNotUpdatedObjects = 0;
        int countUpdatedObjects = 0;
        int countAlreadySet = 0;

        for (final String nextObject : new RpslObjectFileReader(filename)) {
            final RpslObject rpslObject;
            try {
                rpslObject = RpslObject.parse(nextObject);
            } catch (Exception e) {
                LOGGER.info("Malformed RPSL: \n" + nextObject, e);
                append(outputFile, "Malformed RPSL: " + nextObject);

                continue;
            }

            countSplitfileObjects++;

            final UpdateStatus updateStatus = updateAutnum(rpslObject.getKey().toString());

            append(outputFile, String.format("AUT_NUM:%s:%s",rpslObject.getKey().toString(), updateStatus.name()));
            LOGGER.info(String.format("AUT_NUM:%s:%s",rpslObject.getKey().toString(), updateStatus.name()));

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

        append(outputFile, String.format("%s autnums found in split files.", countSplitfileObjects));
        append(outputFile, String.format("%s autnums updated successfully.", countUpdatedObjects));
        append(outputFile, String.format("%s autnums with status already set.", countAlreadySet));
        append(outputFile, String.format("%s autnums not updated.", countNotUpdatedObjects));
    }

    public UpdateStatus updateAutnum(final String key) {
        try {
            LOGGER.info("Updating autnum: " + key);

            //do a lookup first to get the latest version of the object. The split files may have old info.
            final RpslObject object = restClient.request().addParam("unformatted", "").lookup(ObjectType.AUT_NUM, key);

            if (!object.getValuesForAttribute(AttributeType.STATUS).isEmpty()) {
                return UpdateStatus.STATUS_ALREADY_SET;
            }

            final String override = String.format("%s,%s,mass-update-autnum-status {notify=false}", username, password);
            final RpslObject updatedAutnum = restClient.request().addParam("unformatted", "").addParam("override", override).update(object);

            if (updatedAutnum.containsAttribute(AttributeType.STATUS)) {
                return UpdateStatus.STATUS_UPDATE_SUCCESS;
            }
        } catch (RestClientException e) {
            LOGGER.info(e.getErrorMessages().toString());
            append(outputFile, e.getErrorMessages().toString());
        } catch (Exception e1) {
            LOGGER.debug("Unexpected error", e1);
            append(outputFile, Throwables.getStackTraceAsString(e1));
        }
        return UpdateStatus.STATUS_UPDATE_ERROR;
    }

    private RestClient createRestClient(final String restApiUrl, final String source) {
        final RestClient restClient = new RestClient(restApiUrl, source);
        restClient.setWhoisObjectMapper(
                new WhoisObjectMapper(
                        restApiUrl,
                        new AttributeMapper[]{
                                new FormattedClientAttributeMapper(),
                                new DirtyClientAttributeMapper()
                        }));
        return restClient;
    }


    private static void append(File outputFile, String... lines) {
        //Closing the file on each write to prevent loss of logged data if script crashes
        try {
            FileUtils.writeLines(outputFile, Arrays.asList(lines), true);
        } catch (IOException e) {
            LOGGER.info(e.getMessage(), e);
        }
    }

    private static void initOutputFile(File outputFile) throws IOException {
        //Do not proceed if it cannot write the output file.
        if (outputFile.exists()) {
            FileUtils.write(outputFile, "");
        }
    }
}
