package net.ripe.db.whois.scheduler.task.grs;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.aspects.RetryFor;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.io.Downloader;
import net.ripe.db.whois.common.grs.AuthoritativeResourceData;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.ip.Ipv6Resource;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.transform.FilterChangedFunction;
import net.ripe.db.whois.common.source.SourceContext;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.elasticsearch.common.Strings;
import org.glassfish.jersey.client.ClientProperties;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static net.ripe.db.whois.common.domain.CIString.ciString;

@Component
class LacnicGrsSource extends GrsSource {
    private static final FilterChangedFunction FILTER_CHANGED_FUNCTION = new FilterChangedFunction();
    private static final int TIMEOUT = 10_000;
    private static final DateTimeFormatter LAST_MODIFIED_FORMAT = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss VV").withZone(ZoneId.of("GMT"));

    private static final Logger LOGGER = LoggerFactory.getLogger(LacnicGrsSource.class);

    private final String userId;
    private final String password;
    private final String irrDownload;
    private final Client client;

    @Autowired
    LacnicGrsSource(
            @Value("${grs.import.lacnic.source:}") final String source,
            final SourceContext sourceContext,
            final DateTimeProvider dateTimeProvider,
            final AuthoritativeResourceData authoritativeResourceData,
            final Downloader downloader,
            @Value("${grs.import.lacnic.userId:}") final String userId,
            @Value("${grs.import.lacnic.password:}") final String password,
            @Value("${grs.import.lacnic.irr.download:}") final String irrDownload) {
        super(source, sourceContext, dateTimeProvider, authoritativeResourceData, downloader);

        this.irrDownload = irrDownload;
        this.userId = userId;
        this.password = password;
        this.client = ClientBuilder.newBuilder()
                .property(ClientProperties.CONNECT_TIMEOUT, TIMEOUT)
                .property(ClientProperties.READ_TIMEOUT, TIMEOUT)
                .build();
    }

    @Override
    @RetryFor(value = IOException.class, attempts = 5, intervalMs = 60000)
    public void acquireDump(final Path path) throws IOException {
        final Document loginPage = parse(get("https://lacnic.net/cgi-bin/lacnic/stini?lg=EN"));
        final String loginAction = "https://lacnic.net" + loginPage.select("form").attr("action");

        LOGGER.info("Login page:\n{}", loginPage.outerHtml());
        LOGGER.info("loginAction = {}", loginAction);

        final Document downloadPage = parse(post(loginAction));

        LOGGER.info("Download page:\n{}", downloadPage.outerHtml());

        final String downloadAction = "https://lacnic.net" + downloadPage.select("a[href~=/cgi-bin/lacnic/nav.*]").attr("href");
        LOGGER.info("downloadAction = {}", downloadAction);

        downloadTo(logger, new URL(downloadAction), path);
    }

    @Override
    public void acquireIrrDump(final Path path) throws IOException {
        if (Strings.isNullOrEmpty(irrDownload)) {
            return;
        }
        downloadTo(logger, new URL(irrDownload), path);
    }

    // TODO: @RetryFor(value = IOException.class, attempts = 10, intervalMs = 10000)
    private void downloadTo(final Logger logger, final URL url, final Path path) throws IOException {
        logger.info("Downloading {} from {}", path, url);

        try {
            final Invocation.Builder request = client.target(url.toString()).request();

            logger.info("user info: {}", url.getUserInfo());

            if ("https".equals(url.getProtocol()) && !Strings.isNullOrEmpty(url.getUserInfo())) {
                request.header(HttpHeaders.AUTHORIZATION,
                    String.format("Basic %s",
                        Base64.getEncoder().encodeToString(url.getUserInfo().getBytes(StandardCharsets.UTF_8))));
            }

            logger.info("request: {}", request);

            final Response response = request.get();

            logger.info("Response status: {}", response.getStatus());

            final InputStream inputStream = response.readEntity(InputStream.class);
            logger.info("file copy");
            Files.copy(inputStream, path);
            logger.info("set file timestamp");
            setFileTimes(logger, response, path);
        } catch (Exception e) {
            logger.error("Error downloading or setting connection for url {}", url, e);
            throw e;
        }
    }

    private void setFileTimes(final Logger logger, final Response response, final Path path) {
        final String lastModified = response.getHeaderString(HttpHeaders.LAST_MODIFIED);
        if (lastModified == null) {
            logger.info("Couldn't set last modified on {} because no header found", path);
        } else {
            try {
                final ZonedDateTime lastModifiedDateTime = LocalDateTime.from(LAST_MODIFIED_FORMAT.parse(lastModified)).atZone(ZoneOffset.UTC);
                final BasicFileAttributeView attributes = Files.getFileAttributeView(path, BasicFileAttributeView.class);
                final FileTime time = FileTime.from(lastModifiedDateTime.toInstant());
                attributes.setTimes(time, time, time);
                logger.info("{} last modified {}", path, lastModifiedDateTime);
            } catch (Exception e) {
                logger.info("Couldn't set last modified {} on {} due to {}: {}", lastModified, path, e.getClass().getName(), e.getMessage());
            }
        }
    }

    private String get(final String url) {
        return client.target(url)
                .request()
                .get(String.class);
    }

    private String post(final String url) {
        return client.target(url)
            .queryParam("handle", userId)
            .queryParam("passwd", password)
            .request()
            .post(null, String.class);
    }

    private static Document parse(final String data) {
        return Jsoup.parse(data);
    }

    @Override
    public void handleObjects(final File file, final ObjectHandler handler) throws IOException {
        try (FileInputStream is = new FileInputStream(file)) {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.ISO_8859_1));
            handleLines(reader, lines -> new LacnicLineHandler(handler).handleLines(lines));
        }
    }

    @Override
    public void handleIrrObjects(final File file, final ObjectHandler handler) throws IOException {
        try (InputStream is = new GzipCompressorInputStream(new FileInputStream(file))) {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.ISO_8859_1));
            handleLines(reader, lines -> new LacnicLineHandler(handler).handleLines(lines));
        }
    }

    private class LacnicLineHandler implements LineHandler {

        private final ObjectHandler objectHandler;

        public LacnicLineHandler(final ObjectHandler objectHandler) {
            this.objectHandler = objectHandler;
        }

        @Override
        public void handleLines(final List<String> lines) {
            final String rpslObjectString = Joiner.on("").join(lines);
            final RpslObject rpslObjectBase = RpslObject.parse(rpslObjectString);

            final List<RpslAttribute> newAttributes = Lists.newArrayList();
            for (RpslAttribute attribute : rpslObjectBase.getAttributes()) {

                final Function<RpslAttribute, RpslAttribute> transformFunction = TRANSFORM_FUNCTIONS.get(ciString(attribute.getKey()));
                if (transformFunction != null) {
                    attribute = transformFunction.apply(attribute);
                }

                if (attribute.getType() != null) {
                    newAttributes.add(attribute);
                }
            }

            objectHandler.handle(FILTER_CHANGED_FUNCTION.apply(new RpslObject(newAttributes)));
        }
    }

    private static final Map<CIString, Function<RpslAttribute, RpslAttribute>> TRANSFORM_FUNCTIONS = Maps.newHashMap();

    private static void addTransformFunction(final Function<RpslAttribute, RpslAttribute> function, final String... keys) {
        for (final String key : keys) {
            TRANSFORM_FUNCTIONS.put(ciString(key), function);
        }
    }

    static {
        addTransformFunction(input -> new RpslAttribute(AttributeType.AUT_NUM, "AS" + input.getCleanValue()), "aut-num");

        addTransformFunction(input -> {
            final String date = input.getCleanValue().toString().replaceAll("-", "");
            final String value = String.format("%s", date);
            return new RpslAttribute(AttributeType.CREATED, value);
        }, "created");

        addTransformFunction(input -> {
            final IpInterval<?> ipInterval = IpInterval.parse(input.getCleanValue());
            return switch (ipInterval) {
                case Ipv4Resource ipv4Resource -> new RpslAttribute(AttributeType.INETNUM, input.getValue());
                case Ipv6Resource ipv6Resource -> new RpslAttribute(AttributeType.INET6NUM, input.getValue());
                case null -> throw new IllegalArgumentException(String.format("Unexpected input: %s", input.getCleanValue()));
            };
        }, "inetnum");

        addTransformFunction(input -> new RpslAttribute(AttributeType.DESCR, input.getValue()), "owner");
    }
}
