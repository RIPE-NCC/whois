package net.ripe.db.whois.common.rpki;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import net.ripe.db.whois.common.profiles.WhoisProfile;
import org.glassfish.jersey.client.ClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.List;

@Component
@Profile({WhoisProfile.DEPLOYED})
public class RoutinatorDataProvider implements RpkiDataProvider{

    private final static Logger LOGGER = LoggerFactory.getLogger(RoutinatorDataProvider.class);
    private static final int CLIENT_CONNECT_TIMEOUT = 20_000;
    private static final int CLIENT_READ_TIMEOUT = 120_000;

    private final Client client;
    private final String rpkiBaseUrl;

    public RoutinatorDataProvider(@Value("${rpki.base.url:}") final String rpkiBaseUrl) {
        final JacksonJsonProvider jsonProvider = new JacksonJsonProvider()
                .configure(DeserializationFeature.UNWRAP_ROOT_VALUE, false)
                .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        client = (ClientBuilder.newBuilder()
                .register(jsonProvider))
                .property(ClientProperties.CONNECT_TIMEOUT, CLIENT_CONNECT_TIMEOUT)
                .property(ClientProperties.READ_TIMEOUT, CLIENT_READ_TIMEOUT)
                .build();
        this.rpkiBaseUrl = rpkiBaseUrl;
    }

    @Override
    @Nullable
    public List<Roa> loadRoas() {
        if (Strings.isNullOrEmpty(rpkiBaseUrl)){
            LOGGER.error("rpki.base.url property is not set but client is being used");
            return Lists.newArrayList();
        }

        return this.client.target(rpkiBaseUrl)
                    .path("json")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Roas.class)
                    .getRoas();

    }
}
