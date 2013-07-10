package net.ripe.db.whois.scheduler.task.grs;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.IpInterval;
import net.ripe.db.whois.common.domain.Ipv4Resource;
import net.ripe.db.whois.common.domain.Ipv6Resource;
import net.ripe.db.whois.common.grs.AuthoritativeResourceData;
import net.ripe.db.whois.common.io.Downloader;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.SourceContext;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static net.ripe.db.whois.common.domain.CIString.ciString;

@Component
class LacnicGrsSource extends GrsSource {
    private final String userId;
    private final String password;

    @Autowired
    LacnicGrsSource(
            @Value("${grs.import.lacnic.source:}") final String source,
            final SourceContext sourceContext,
            final DateTimeProvider dateTimeProvider,
            final AuthoritativeResourceData authoritativeResourceData,
            final Downloader downloader,
            @Value("${grs.import.lacnic.userId:}") final String userId,
            @Value("${grs.import.lacnic.password:}") final String password) {
        super(source, sourceContext, dateTimeProvider, authoritativeResourceData, downloader);

        this.userId = userId;
        this.password = password;
    }

    @Override
    public void acquireDump(final File file) throws IOException {
        final String action = "http://lacnic.net" + Jsoup.connect("http://www.lacnic.net/login")
                .timeout(10000)
                .get()
                .select("form")
                .attr("action");

        Jsoup.connect(action)
                .timeout(10000)
                .data("handle", userId)
                .data("passwd", password)
                .post();


        final String downloadAction = action.replace("stini", "bulkWhoisLoader");
        downloader.downloadToFile(logger, new URL(downloadAction), file);
    }

    @Override
    public void handleObjects(final File file, final ObjectHandler handler) throws IOException {
        FileInputStream is = null;

        try {
            is = new FileInputStream(file);

            final BufferedReader reader = new BufferedReader(new InputStreamReader(is, Charsets.UTF_8));
            handleLines(reader, new LineHandler() {
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

                    handler.handle(new RpslObject(newAttributes));
                }
            });
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    private static final Map<CIString, Function<RpslAttribute, RpslAttribute>> TRANSFORM_FUNCTIONS = Maps.newHashMap();

    private static void addTransformFunction(final Function<RpslAttribute, RpslAttribute> function, final String... keys) {
        for (final String key : keys) {
            TRANSFORM_FUNCTIONS.put(ciString(key), function);
        }
    }

    static {
        addTransformFunction(new Function<RpslAttribute, RpslAttribute>() {
            @Nullable
            @Override
            public RpslAttribute apply(@Nullable RpslAttribute input) {
                return new RpslAttribute(AttributeType.AUT_NUM, "AS" + input.getCleanValue());
            }
        }, "aut-num");

        addTransformFunction(new Function<RpslAttribute, RpslAttribute>() {
            @Nullable
            @Override
            public RpslAttribute apply(@Nullable RpslAttribute input) {
                final String date = input.getCleanValue().toString().replaceAll("-", "");
                final String value = String.format("unread@ripe.net %s # %s", date, input.getKey());
                return new RpslAttribute(AttributeType.CHANGED, value);
            }
        }, "changed", "created");

        addTransformFunction(new Function<RpslAttribute, RpslAttribute>() {
            @Nullable
            @Override
            public RpslAttribute apply(@Nullable RpslAttribute input) {
                final IpInterval<?> ipInterval = IpInterval.parse(input.getCleanValue());
                if (ipInterval instanceof Ipv4Resource) {
                    return new RpslAttribute(AttributeType.INETNUM, input.getValue());
                } else if (ipInterval instanceof Ipv6Resource) {
                    return new RpslAttribute(AttributeType.INET6NUM, input.getValue());
                } else {
                    throw new IllegalArgumentException(String.format("Unexpected input: %s", input.getCleanValue()));
                }
            }
        }, "inetnum");

        addTransformFunction(new Function<RpslAttribute, RpslAttribute>() {
            @Nullable
            @Override
            public RpslAttribute apply(@Nullable RpslAttribute input) {
                return new RpslAttribute(AttributeType.DESCR, input.getValue());
            }
        }, "owner");
    }
}
