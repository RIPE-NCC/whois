package net.ripe.db.nrtm4.client.scheduler;

import jakarta.ws.rs.core.MediaType;
import net.ripe.db.whois.api.rest.client.RestClient;
import org.springframework.stereotype.Component;

@Component
public class NrtmRestClient {

    /*private final String baseUrl;

    public NrtmRestClient(@Value("${nrtm.baseUrl}") final String baseUrl) {
        //this.baseUrl = baseUrl;
    }
    */

    public UpdateNotificationFile getNotificationFile(final String source){
        return RestClient.target("https://nrtm-rc.db.ripe.net/nrtmv4", source)
                .path("update-notification-file.json")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(UpdateNotificationFile.class);
    }
}
