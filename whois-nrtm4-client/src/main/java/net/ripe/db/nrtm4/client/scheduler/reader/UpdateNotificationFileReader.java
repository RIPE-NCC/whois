package net.ripe.db.nrtm4.client.scheduler.reader;

import jakarta.ws.rs.core.MediaType;
import net.ripe.db.nrtm4.client.scheduler.UpdateNotificationFile;
import net.ripe.db.nrtm4.client.scheduler.dao.Nrtm4ClientMirrorRepository;
import net.ripe.db.whois.api.rest.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UpdateNotificationFileReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateNotificationFileReader.class);

    //private final String baseUrl;

    private final Nrtm4ClientMirrorRepository nrtm4ClientMirrorDao;

    private static final String[] AVAILABLE_SOURCES = {
            "RIPE",
            "RIPE-NONAUTH"
    };

    public UpdateNotificationFileReader(@Value("${nrtm.baseUrl}") final String baseUrl,
                                        final Nrtm4ClientMirrorRepository nrtm4ClientMirrorDao) {
        //this.baseUrl = baseUrl;
        this.nrtm4ClientMirrorDao = nrtm4ClientMirrorDao;
    }

    public void readFile(){
        final Map<String, UpdateNotificationFile> notificationFilePerSource = Arrays.stream(AVAILABLE_SOURCES).
                        collect(Collectors.toMap(
                                string -> string,
                                this::getNotificationFile
                        ));
        LOGGER.info("Succeeded to read notification files from {}", notificationFilePerSource.keySet());

        //TODO: [MH] Review integrity of the data checking the signature using the public key
        notificationFilePerSource.forEach((source, updateNotificationFile) -> {
            nrtm4ClientMirrorDao.saveUpdateNotificationFileVersion(source, updateNotificationFile.getVersion(), updateNotificationFile.getSessionID());
        });

        //TODO: [MH] if last_mirror is empty, we need to store from scratch. Take snapshot the snapshot.
    }

    private UpdateNotificationFile getNotificationFile(final String source){

        return RestClient.target("https://nrtm-prepdev.db.ripe.net/nrtmv4", source)
                .path("update-notification-file.json")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(UpdateNotificationFile.class);
    }
}
