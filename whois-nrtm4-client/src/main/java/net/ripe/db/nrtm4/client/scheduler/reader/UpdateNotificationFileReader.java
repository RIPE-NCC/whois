package net.ripe.db.nrtm4.client.scheduler.reader;

import jakarta.ws.rs.core.MediaType;
import net.ripe.db.nrtm4.client.scheduler.NrtmRestClient;
import net.ripe.db.nrtm4.client.scheduler.UpdateNotificationFile;
import net.ripe.db.nrtm4.client.scheduler.dao.Nrtm4ClientMirrorRepository;
import net.ripe.db.whois.api.rest.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UpdateNotificationFileReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateNotificationFileReader.class);

    private final NrtmRestClient nrtmRestClient;

    private final Nrtm4ClientMirrorRepository nrtm4ClientMirrorDao;

    private static final String[] AVAILABLE_SOURCES = {
            "RIPE",
            "RIPE-NONAUTH"
    };

    public UpdateNotificationFileReader(final NrtmRestClient nrtmRestClient,
                                        final Nrtm4ClientMirrorRepository nrtm4ClientMirrorDao) {
        this.nrtmRestClient = nrtmRestClient;
        this.nrtm4ClientMirrorDao = nrtm4ClientMirrorDao;
    }

    public void readFile(){
        final Map<String, UpdateNotificationFile> notificationFilePerSource = Arrays.stream(AVAILABLE_SOURCES).
                        collect(Collectors.toMap(
                                string -> string,
                                nrtmRestClient::getNotificationFile
                        ));
        LOGGER.info("Succeeded to read notification files from {}", notificationFilePerSource.keySet());

        //TODO: [MH] Review integrity of the data checking the signature using the public key
        notificationFilePerSource.forEach((source, updateNotificationFile) -> {
            try {
                nrtm4ClientMirrorDao.saveUpdateNotificationFileVersion(source, updateNotificationFile.getVersion(), updateNotificationFile.getSessionID());
            } catch (DuplicateKeyException ex){
                LOGGER.info("There is no new version associated with the source {}", source);
            }
        });

        //TODO: [MH] if last_mirror is empty, we need to store from scratch. Take snapshot the snapshot.
    }
}
