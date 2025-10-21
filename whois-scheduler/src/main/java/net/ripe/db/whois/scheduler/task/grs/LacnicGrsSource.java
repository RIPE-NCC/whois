package net.ripe.db.whois.scheduler.task.grs;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static net.ripe.db.whois.common.domain.CIString.ciString;

@Component
class LacnicGrsSource extends GrsSource {
    private static final FilterChangedFunction FILTER_CHANGED_FUNCTION = new FilterChangedFunction();
    private static final int TIMEOUT = 10_000;

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
            @Qualifier("jaxRSDownloader") final Downloader downloader,
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

        Document postLoginPage = parse(post(loginAction));
        final String refreshAction = postLoginPage.select("meta[http-equiv=Refresh]").attr("content");
        logger.debug("refresh {}", refreshAction);
        if (refreshAction.contains("=")) {
            final String refreshURL = refreshAction.substring(refreshAction.indexOf("=") + 1);
            postLoginPage = parse(get(refreshURL));
        }

        final String downloadAction = "https://lacnic.net" + postLoginPage.select("A[HREF~=/cgi-bin/lacnic/bulkWhoisLoader.*]").attr("href");
        logger.debug("download {}", downloadAction);

        downloader.downloadTo(logger, new URL(downloadAction), path);

        final String logoffAction = "https://lacnic.net" + postLoginPage.select("A[HREF~=/cgi-bin/lacnic/stini\\?logoff.*]").attr("href");
        logger.debug("logoff {}", logoffAction);
        get(logoffAction);
    }

    @Override
    public void acquireIrrDump(final Path path) throws IOException {
        if (Strings.isNullOrEmpty(irrDownload)) {
            return;
        }
        downloader.downloadTo(logger, new URL(irrDownload), path);
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
