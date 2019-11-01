package net.ripe.db.whois.scheduler.task.grs;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.io.Downloader;
import net.ripe.db.whois.common.grs.AuthoritativeResourceData;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.ip.Ipv6Resource;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.common.source.SourceContext;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static net.ripe.db.whois.common.domain.CIString.ciSet;
import static net.ripe.db.whois.common.domain.CIString.ciString;

@Component
class ArinGrsSource extends GrsSource {
    private static final Pattern IPV6_SPLIT_PATTERN = Pattern.compile("(?i)([0-9a-f:]*)\\s*-\\s*([0-9a-f:]*)\\s*");
    private static final Pattern AS_NUMBER_RANGE = Pattern.compile("^(\\d+) [-] (\\d+)$");

    private final String download;
    private final String zipEntryName;

    @Autowired
    ArinGrsSource(
            @Value("${grs.import.arin.source:}") final String source,
            final SourceContext sourceContext,
            final DateTimeProvider dateTimeProvider,
            final AuthoritativeResourceData authoritativeResourceData,
            final Downloader downloader,
            @Value("${grs.import.arin.download:}") final String download,
            @Value("${grs.import.arin.zipEntryName:}") final String zipEntryName) {
        super(source, sourceContext, dateTimeProvider, authoritativeResourceData, downloader);

        this.download = download;
        this.zipEntryName = zipEntryName;
    }

    @Override
    public void acquireDump(final Path path) throws IOException {
        downloader.downloadTo(logger, new URL(download), path);
    }

    @Override
    public void handleObjects(final File file, final ObjectHandler handler) throws IOException {
        ZipFile zipFile = null;

        try {
            zipFile = new ZipFile(file, ZipFile.OPEN_READ);
            final ZipEntry zipEntry = zipFile.getEntry(zipEntryName);
            if (zipEntry == null) {
                logger.error("Zipfile {} does not contain dump {}", file, zipEntryName);
                return;
            }

            final BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(zipEntry), StandardCharsets.UTF_8));
            handleLines(reader, new LineHandler() {
                @Override
                public void handleLines(final List<String> lines) {
                    if (lines.isEmpty() || IGNORED_OBJECTS.contains(ciString(StringUtils.substringBefore(lines.get(0), ":")))) {
                        logger.debug("Ignoring:\n\n{}\n", lines);
                        return;
                    }

                    final RpslObjectBuilder rpslObjectBuilder = new RpslObjectBuilder(Joiner.on("").join(lines));
                    for (RpslObject next : expand(rpslObjectBuilder.getAttributes())) {
                        handler.handle(next);
                    }
                }

                private List<RpslObject> expand(final List<RpslAttribute> attributes) {
                    if (attributes.get(0).getKey().equals("ashandle")) {
                        final String asnumber = findAttributeValue(attributes, "asnumber");
                        if (asnumber != null) {
                            final Matcher rangeMatcher = AS_NUMBER_RANGE.matcher(asnumber);
                            if (rangeMatcher.find()) {
                                final List<RpslObject> objects = Lists.newArrayList();

                                final int begin = Integer.parseInt(rangeMatcher.group(1));
                                final int end = Integer.parseInt(rangeMatcher.group(2));

                                for (int index = begin; index <= end; index++) {
                                    attributes.set(0, new RpslAttribute(AttributeType.AUT_NUM, String.format("AS%d", index)));
                                    objects.add(new RpslObject(transform(attributes)));
                                }

                                return objects;
                            }
                        }
                    }

                    return Lists.newArrayList(new RpslObject(transform(attributes)));
                }

                private List<RpslAttribute> transform(final List<RpslAttribute> attributes) {
                    final List<RpslAttribute> newAttributes = Lists.newArrayList();
                    for (RpslAttribute attribute : attributes) {
                        final Function<RpslAttribute, RpslAttribute> transformFunction = TRANSFORM_FUNCTIONS.get(ciString(attribute.getKey()));
                        if (transformFunction != null) {
                            attribute = transformFunction.apply(attribute);
                        }

                        final AttributeType attributeType = attribute.getType();
                        if (attributeType == null) {
                            continue;
                        } else if (AttributeType.INETNUM.equals(attributeType) || AttributeType.INET6NUM.equals(attributeType)) {
                            newAttributes.add(0, attribute);
                        } else {
                            newAttributes.add(attribute);
                        }
                    }

                    return newAttributes;
                }

                @Nullable
                private String findAttributeValue(final List<RpslAttribute> attributes, final String key) {
                    for (RpslAttribute attribute : attributes) {
                        if (attribute.getKey().equals(key)) {
                            return attribute.getCleanValue().toString();
                        }
                    }
                    return null;
                }
            });
        } finally {
            if (zipFile != null) {
                zipFile.close();
            }
        }
    }

    private static final Set<CIString> IGNORED_OBJECTS = ciSet("OrgID", "POCHandle", "Updated");

    private static final Map<CIString, Function<RpslAttribute, RpslAttribute>> TRANSFORM_FUNCTIONS = Maps.newHashMap();

    private static void addTransformFunction(final Function<RpslAttribute, RpslAttribute> function, final String... keys) {
        for (final String key : keys) {
            TRANSFORM_FUNCTIONS.put(ciString(key), function);
        }
    }

    static {
        for (final Object[] mapped : new Object[][]{
                {"ASHandle", AttributeType.AUT_NUM},

                {"AbuseHandle", AttributeType.TECH_C},
                {"NOCHandle", AttributeType.TECH_C},
                {"OrgAbuseHandle", AttributeType.TECH_C},
                {"OrgAdminHandle", AttributeType.TECH_C},
                {"OrgNOCHandle", AttributeType.TECH_C},
                {"OrgTechHandle", AttributeType.TECH_C},
                {"TechHandle", AttributeType.TECH_C},

                {"ASName", AttributeType.AS_NAME},
                {"OrgID", AttributeType.ORG},
                {"OrgName", AttributeType.ORG_NAME},
                {"Comment", AttributeType.REMARKS},
                {"NetName", AttributeType.NETNAME},
                {"NetType", AttributeType.STATUS},
                {"Source", AttributeType.SOURCE},
        }) {
            addTransformFunction(input -> new RpslAttribute((AttributeType) mapped[1], input.getValue()), (String) mapped[0]);
        }

        addTransformFunction(input -> {
                return new RpslAttribute(AttributeType.ADDRESS, String.format("%s # %s", input.getValue(), input.getKey()));
        }, "City", "Country", "PostalCode", "Street", "State/Prov");

        addTransformFunction(input -> {
            final String value = input.getCleanValue().toString();

            // Fix IPv6 syntax
            if (value.contains(":")) {
                final Matcher matcher = IPV6_SPLIT_PATTERN.matcher(value);
                if (matcher.find()) {
                    final Ipv6Resource beginResource = Ipv6Resource.parse(matcher.group(1));
                    final Ipv6Resource endResource = Ipv6Resource.parse(matcher.group(2));
                    final Ipv6Resource ipv6Resource = new Ipv6Resource(beginResource.begin(), endResource.end());

                    return new RpslAttribute(AttributeType.INET6NUM, ipv6Resource.toString());
                }
            }

            final IpInterval<?> ipInterval = IpInterval.parse(value);
            if (ipInterval instanceof Ipv4Resource) {
                return new RpslAttribute(AttributeType.INETNUM, input.getValue());
            } else if (ipInterval instanceof Ipv6Resource) {
                return new RpslAttribute(AttributeType.INET6NUM, input.getValue());
            } else {
                throw new IllegalArgumentException(String.format("Unexpected input: %s", input.getCleanValue()));
            }
        }, "NetRange");
    }
}
