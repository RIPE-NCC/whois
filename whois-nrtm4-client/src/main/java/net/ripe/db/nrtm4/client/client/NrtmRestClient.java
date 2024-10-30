package net.ripe.db.nrtm4.client.client;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import net.ripe.db.nrtm4.client.condition.Nrtm4ClientCondition;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpScheme;
import org.glassfish.jersey.client.ClientProperties;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
    public NrtmClientFileResponse getSnapshotFile(final String url){
        try {
            final Response response =  client.target(url)
                    .request(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeader.X_FORWARDED_PROTO.asString(), HttpScheme.HTTPS.asString())
                    .get(Response.class);

            final byte[] payload = response.readEntity(byte[].class);
            final String[] records = getSnapshotRecords(payload);
            final Metadata metadata = getMetadata(records);

            return new NrtmClientFileResponse(getMirrorRpslObjects(records), metadata.version, metadata.sessionId, calculateSha256(payload));
        } catch (Exception ex){
            LOGGER.error("Unable to get the records from the snapshot", ex);
            return null;
        }
    }

    @Nullable
    public NrtmClientFileResponse getDeltaFiles(final String url){
        try {
            final Response response =  client.target(url)
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Response.class);

            final byte[] payload = response.readEntity(byte[].class);
            final String[] records = StringUtils.split(new String(payload, StandardCharsets.UTF_8), RECORD_SEPARATOR);
            final Metadata metadata = getMetadata(records);

            return new NrtmClientFileResponse(getMirrorDeltaObjects(records), metadata.version, metadata.sessionId,
                    calculateSha256(payload));
        } catch (Exception ex){
            LOGGER.error("Unable to get the records from the snapshot", ex);
            return null;
        }
    }

    private static List<MirrorObjectInfo> getMirrorDeltaObjects(final String[] records) throws JsonProcessingException {
        final List<MirrorObjectInfo> mirrorDeltaInfos = Lists.newArrayList();
        for (int i = 1; i < records.length; i++) {
            final JSONObject jsonObject = new JSONObject(records[i]);
            final String deltaAction = jsonObject.getString("action");
            final String deltaObjectType = jsonObject.getString("object_class");
            final String deltaPrimaryKey = jsonObject.getString("primary_key");
            final String deltaUpdatedObject = jsonObject.getString("object");
            final MirrorDeltaInfo mirrorDeltaInfo =
                    new MirrorDeltaInfo(new ObjectMapper().readValue(deltaUpdatedObject, RpslObject.class),
                            deltaAction,
                            ObjectType.valueOf(deltaObjectType),
                            deltaPrimaryKey);
            mirrorDeltaInfos.add(mirrorDeltaInfo);
        }
        return mirrorDeltaInfos;
    }

    private static List<MirrorObjectInfo> getMirrorRpslObjects(final String[] records) throws JsonProcessingException {
        final List<MirrorObjectInfo> mirrorObjectInfos = Lists.newArrayList();
        for (int i = 1; i < records.length; i++) {
            mirrorObjectInfos.add(new ObjectMapper().readValue(records[i], MirrorObjectInfo.class));
        }
        return mirrorObjectInfos;
    }

    private static Metadata getMetadata(String[] records) {
        final JSONObject jsonObject = new JSONObject(records[0]);
        final int deltatVersion = jsonObject.getInt("version");
        final String deltaSessionId = jsonObject.getString("session_id");
        return new Metadata(deltatVersion, deltaSessionId);
    }

    private record Metadata(int version, String sessionId) {}

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

    private static String decompress(final byte[] compressed) throws IOException {
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
