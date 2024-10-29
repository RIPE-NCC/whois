package net.ripe.db.nrtm4.client.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;
import com.fasterxml.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationIntrospector;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.ripe.db.nrtm4.client.scheduler.Nrtm4ClientCondition;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpScheme;
import org.glassfish.jersey.client.ClientProperties;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

@Component
@Conditional(Nrtm4ClientCondition.class)
public class NrtmRestClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(NrtmRestClient.class);

    private final String baseUrl;

    private static final int CLIENT_CONNECT_TIMEOUT = 10_000;

    private static final int CLIENT_READ_TIMEOUT = 60_000;

    private final Client client;

    public static final String RECORD_SEPARATOR = "\u001E";

    public NrtmRestClient(@Value("${nrtm.baseUrl}") final String baseUrl) {
        final ObjectMapper objectMapper = JsonMapper.builder()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .build();
        objectMapper.setAnnotationIntrospector(
                new AnnotationIntrospectorPair(
                        new JacksonAnnotationIntrospector(),
                        new JakartaXmlBindAnnotationIntrospector(TypeFactory.defaultInstance())));
        objectMapper.registerModule(new JavaTimeModule());
        final JacksonJsonProvider jsonProvider = (new JacksonJsonProvider())
                .configure(DeserializationFeature.UNWRAP_ROOT_VALUE, false)
                .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        jsonProvider.setMapper(objectMapper);

        this.client = (ClientBuilder.newBuilder()
                .register(jsonProvider))
                .property(ClientProperties.CONNECT_TIMEOUT, CLIENT_CONNECT_TIMEOUT)
                .property(ClientProperties.READ_TIMEOUT, CLIENT_READ_TIMEOUT)
                .build();
        this.baseUrl = baseUrl;
    }

    public List<String> getNrtmAvailableSources(){
        try {
            final String response = client.target(baseUrl)
                    .request(MediaType.TEXT_HTML_TYPE)
                    .get(String.class);

            return extractSources(response);
        } catch (final Exception e) {
            LOGGER.error("Unable to get the available sources", e);
            return Lists.newArrayList();
        }
    }

    public UpdateNotificationFileResponse getNotificationFile(final String source){
        return client.target(String.format("%s/%s", baseUrl, source))
                .path("update-notification-file.json")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(UpdateNotificationFileResponse.class);
    }

    @Nullable
    public SnapshotFileResponse getSnapshotFile(final String url){
        final Response response =  client.target(url)
                .request(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeader.X_FORWARDED_PROTO.asString(), HttpScheme.HTTPS.asString())
                .get(Response.class);

        try {
            final byte[] payload = response.readEntity(byte[].class);
            final String[] records = getSnapshotRecords(payload);
            final JSONObject jsonObject = new JSONObject(records[0]);
            final int snapshotVersion = jsonObject.getInt("version");
            final String snapshotSessionId = jsonObject.getString("session_id");

            final List<MirrorRpslObject> rpslObjects = Lists.newArrayList();
            for (int i = 1; i < records.length; i++) {
                rpslObjects.add(new ObjectMapper().readValue(records[i], MirrorRpslObject.class));
            }
            return new SnapshotFileResponse(rpslObjects, snapshotVersion, snapshotSessionId, calculateSha256(payload));
        } catch (IOException | JSONException ex){
            LOGGER.error("Unable to get the records from the snapshot", ex);
            return null;
        }
    }

    private static List<String> extractSources(final String html) {
        final List<String> sources = com.google.common.collect.Lists.newArrayList();

        final String regex = "<a[^>]*>(.*?)</a>";
        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(html);

        while (matcher.find()) {
            final String source = matcher.group(1).trim();
            sources.add(source);
        }

        return sources;
    }

    private static String[] getSnapshotRecords(byte[] compressed) throws IOException {
        return StringUtils.split(decompress(compressed), RECORD_SEPARATOR);
    }

    public static String decompress(final byte[] compressed) throws IOException {
        final int BUFFER_SIZE = 32;
        ByteArrayInputStream is = new ByteArrayInputStream(compressed);
        GZIPInputStream gis = new GZIPInputStream(is, BUFFER_SIZE);
        StringBuilder string = new StringBuilder();
        byte[] data = new byte[BUFFER_SIZE];
        int bytesRead;
        while ((bytesRead = gis.read(data)) != -1) {
            string.append(new String(data, 0, bytesRead));
        }
        gis.close();
        is.close();
        return string.toString();
    }

    private static String calculateSha256(final byte[] bytes) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] encodedSha256hex = digest.digest(bytes);
            return byteArrayToHexString(encodedSha256hex);
        } catch (final NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static String byteArrayToHexString(final byte[] bytes) {
        final StringBuilder hexString = new StringBuilder(2 * bytes.length);
        for (final byte b : bytes) {
            final String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
