package net.ripe.db.nrtm4.client.reader;

import net.ripe.db.nrtm4.client.client.NrtmRestClient;
import net.ripe.db.nrtm4.client.dao.Nrtm4ClientMirrorRepository;
import net.ripe.db.nrtm4.client.client.NrtmVersionResponse;
import net.ripe.db.nrtm4.client.dao.NrtmVersionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UpdateNotificationFileReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateNotificationFileReader.class);

    private final NrtmRestClient nrtmRestClient;

    private final Nrtm4ClientMirrorRepository nrtm4ClientMirrorDao;


    public UpdateNotificationFileReader(final NrtmRestClient nrtmRestClient,
                                        final Nrtm4ClientMirrorRepository nrtm4ClientMirrorDao) {
        this.nrtmRestClient = nrtmRestClient;
        this.nrtm4ClientMirrorDao = nrtm4ClientMirrorDao;
    }

    public void readFile(){
        final Map<String, NrtmVersionResponse> notificationFilePerSource =
                nrtmRestClient.getNrtmAvailableSources()
                .stream()
                .collect(Collectors.toMap(
                        string -> string,
                        nrtmRestClient::getNotificationFile
                ));
        LOGGER.info("Succeeded to read notification files from {}", notificationFilePerSource.keySet());

        //TODO: [MH] Review integrity of the data checking the signature using the public key
        notificationFilePerSource.forEach((source, updateNotificationFile) -> {
            final NrtmVersionInfo nrtmVersionInfo = nrtm4ClientMirrorDao.getNrtmVersionInfo(source,
                    updateNotificationFile.getVersion());

            if (nrtmVersionInfo != null && !nrtmVersionInfo.sessionID().equals(updateNotificationFile.getSessionID())){
                LOGGER.info("Different session");
                nrtm4ClientMirrorDao.truncateTables();
                return;
            }

            if (nrtmVersionInfo != null && nrtmVersionInfo.version().equals(updateNotificationFile.getVersion())){
                LOGGER.info("There is no new version associated with the source {}", source);
                return;
            }

            nrtm4ClientMirrorDao.saveUpdateNotificationFileVersion(source, updateNotificationFile.getVersion(), updateNotificationFile.getSessionID());

        });

        //TODO: [MH] if last_mirror is empty, we need to store from scratch. Take snapshot the snapshot.
    }

}
