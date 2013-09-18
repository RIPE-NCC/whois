package net.ripe.db.whois.common.grs;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Ipv4Resource;
import net.ripe.db.whois.common.domain.Ipv6Resource;
import net.ripe.db.whois.common.etree.IntervalMap;
import net.ripe.db.whois.common.etree.NestedIntervalMap;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.slf4j.Logger;

import javax.annotation.concurrent.Immutable;
import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.ripe.db.whois.common.domain.CIString.ciString;

@Immutable
public class AuthoritativeResource {
    private static final Set<ObjectType> RESOURCE_TYPES = Sets.newEnumSet(Lists.newArrayList(ObjectType.AUT_NUM, ObjectType.INETNUM, ObjectType.INET6NUM), ObjectType.class);

    private final Set<CIString> autNums;
    private final IntervalMap<Ipv4Resource, Ipv4Resource> inetRanges;
    private final int nrInetRanges;
    private final IntervalMap<Ipv6Resource, Ipv6Resource> inet6Ranges;
    private final int nrInet6Ranges;

    public static AuthoritativeResource unknown(final Logger logger) {
        return new AuthoritativeResource(logger, Collections.<CIString>emptySet(), new NestedIntervalMap<Ipv4Resource, Ipv4Resource>(), new NestedIntervalMap<Ipv6Resource, Ipv6Resource>());
    }

    public static AuthoritativeResource loadFromFile(final Logger logger, final String name, final File file) {
        try {
            return loadFromScanner(logger, name, new Scanner(file));
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException(String.format("File not found: %s", file.getAbsolutePath()), e);
        }
    }

    static AuthoritativeResource loadFromScanner(final Logger logger, final String name, final Scanner scanner) {
        try {
            return load(logger, name, scanner);
        } finally {
            scanner.close();
        }
    }

    private static AuthoritativeResource load(final Logger logger, final String name, final Scanner scanner) {
        scanner.useDelimiter("\n");

        final Set<String> allowedStatus = Sets.newHashSet("allocated", "assigned", "available", "reserved");
        final Pattern linePattern = Pattern.compile("^([a-zA-Z]+)\\|(.*?)\\|(.*?)\\|(.*?)\\|(.*?)\\|(.*?)\\|(.*?)(?:\\|.*|$)");

        return new Callable<AuthoritativeResource>() {
            final Set<CIString> autNums = Sets.newHashSet();
            final IntervalMap<Ipv4Resource, Ipv4Resource> inetnums = new NestedIntervalMap<>();
            final IntervalMap<Ipv6Resource, Ipv6Resource> inet6nums = new NestedIntervalMap<>();

            @Override
            public AuthoritativeResource call() {
                final String expectedSource = name.replace("-GRS", "").toLowerCase();

                while (scanner.hasNext()) {
                    final String line = scanner.next();
                    handleLine(expectedSource, line);
                }

                return new AuthoritativeResource(logger, autNums, inetnums, inet6nums);
            }

            private void handleLine(final String expectedSource, final String line) {
                final Matcher matcher = linePattern.matcher(line);
                if (!matcher.matches()) {
                    logger.debug("Skipping: {}", line);
                    return;
                }

                final String source = matcher.group(1);
                final String cc = matcher.group(2);
                final String type = matcher.group(3).toLowerCase();
                final String start = matcher.group(4);
                final String value = matcher.group(5);
                final String status = matcher.group(7).toLowerCase();

                if (!source.toLowerCase().contains(expectedSource)) {
                    logger.debug("Ignoring source '{}': {}", source, line);
                    return;
                }

                if (cc.indexOf('*') != -1) {
                    logger.debug("Ignoring country code '{}': {}", cc, line);
                    return;
                }

                if (!allowedStatus.contains(status)) {
                    logger.debug("Ignoring status '{}': {}", status, line);
                    return;
                }

                try {
                    if (type.equals("ipv4")) {
                        createIpv4Resource(start, value);
                    } else if (type.equals("ipv6")) {
                        createIpv6Resource(start, value);
                    } else if (type.equals("asn")) {
                        createAutNum(start, value);
                    } else {
                        logger.debug("Unsupported type '{}': {}", type, line);
                    }
                } catch (RuntimeException ignored) {
                    logger.warn("Unexpected '{}-{}': {}", ignored, ignored.getMessage(), line);
                }
            }

            private void createAutNum(final String start, final String value) {
                final int startNum = Integer.parseInt(start);
                final int count = Integer.parseInt(value);
                for (int i = 0; i < count; i++) {
                    autNums.add(ciString(String.format("AS%s", startNum + i)));
                }
            }

            private void createIpv4Resource(final String start, final String value) {
                final long begin = Ipv4Resource.parse(start).begin();
                final long end = begin + (Long.parseLong(value) - 1);
                final Ipv4Resource ipv4Resource = new Ipv4Resource(begin, end);
                inetnums.put(ipv4Resource, ipv4Resource);
            }

            private void createIpv6Resource(final String start, final String value) {
                final Ipv6Resource ipv6Resource = Ipv6Resource.parse(String.format("%s/%s", start, value));
                inet6nums.put(ipv6Resource, ipv6Resource);
            }
        }.call();
    }

    private AuthoritativeResource(final Logger logger, final Set<CIString> autNums, final IntervalMap<Ipv4Resource, Ipv4Resource> inetRanges, final IntervalMap<Ipv6Resource, Ipv6Resource> inet6Ranges) {
        this.autNums = autNums;
        this.inetRanges = inetRanges;
        this.inet6Ranges = inet6Ranges;
        this.nrInetRanges = inetRanges.findExactAndAllMoreSpecific(Ipv4Resource.parse("0/0")).size();
        this.nrInet6Ranges = inet6Ranges.findExactAndAllMoreSpecific(Ipv6Resource.parse("::0/0")).size();

        logger.info("Resources: {}", String.format("asn: %5d; ipv4: %5d; ipv6: %5d", getNrAutNums(), getNrInetnums(), getNrInet6nums()));
    }

    int getNrAutNums() {
        return autNums.size();
    }

    int getNrInetnums() {
        return nrInetRanges;
    }

    int getNrInet6nums() {
        return nrInet6Ranges;
    }

    boolean isEmpty() {
        return getNrAutNums() == 0 && getNrInetnums() == 0 && getNrInet6nums() == 0;
    }

    private Ipv4Resource concatenateIpv4Resources(final List<Ipv4Resource> resources) {
        if (resources.isEmpty()) {
            throw new IllegalArgumentException();
        }

        for (int index = 1; index < resources.size(); index++) {
            if (resources.get(index).begin() != resources.get(index - 1).end() + 1) {
                throw new IllegalArgumentException("found gap");
            }
        }

        return new Ipv4Resource(resources.get(0).begin(), resources.get(resources.size() - 1).end());
    }

    private Ipv6Resource concatenateIpv6Resources(final List<Ipv6Resource> resources) {
        if (resources.isEmpty()) {
            throw new IllegalArgumentException();
        }

        for (int index = 1; index < resources.size(); index++) {
            if (!resources.get(index).begin().equals(resources.get(index - 1).end().add(BigInteger.ONE))) {
                throw new IllegalArgumentException("found gap");
            }
        }

        return new Ipv6Resource(resources.get(0).begin(), resources.get(resources.size() - 1).end());
    }

    public boolean isMaintainedByRir(final ObjectType objectType, final CIString pkey) {
        try {
            switch (objectType) {
                case AUT_NUM:
                    return autNums.contains(pkey);
                case INETNUM:
                {
                    final Ipv4Resource pkeyResource = Ipv4Resource.parse(pkey);

                    if (!inetRanges.findExact(pkeyResource).isEmpty()) {
                        return true;
                    }

                    List<Ipv4Resource> matches = inetRanges.findFirstMoreSpecific(pkeyResource);
                    if (matches.isEmpty()) {
                        return false;
                    }

                    try {
                        Ipv4Resource concatenatedResource = concatenateIpv4Resources(matches);
                        if (concatenatedResource.compareTo(pkeyResource) == 0) {
                            return true;
                        }
                    } catch (IllegalArgumentException ignored) {
                        // empty match or gap in range
                    }

                    return false;
                }
                case INET6NUM:
                {
                    final Ipv6Resource pkeyResource = Ipv6Resource.parse(pkey);

                    if (!inet6Ranges.findExact(pkeyResource).isEmpty()) {
                        return true;
                    }

                    List<Ipv6Resource> matches = inet6Ranges.findFirstMoreSpecific(pkeyResource);
                    if (matches.isEmpty()) {
                        return false;
                    }

                    try {
                        Ipv6Resource concatenatedResource = concatenateIpv6Resources(matches);
                        if (concatenatedResource.compareTo(pkeyResource) == 0) {
                            return true;
                        }
                    } catch (IllegalArgumentException ignored) {
                        // empty match or gap in range
                    }

                    return false;
                }
                default:
                    return true;
            }
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    public boolean isMaintainedInRirSpace(final RpslObject rpslObject) {
        return isMaintainedInRirSpace(rpslObject.getType(), rpslObject.getKey());
    }

    public boolean isMaintainedInRirSpace(final ObjectType objectType, final CIString pkey) {
        try {
            switch (objectType) {
                case AUT_NUM:
                    return autNums.contains(pkey);
                case INETNUM:
                    return !inetRanges.findExactOrFirstLessSpecific(Ipv4Resource.parse(pkey)).isEmpty();
                case INET6NUM:
                    return !inet6Ranges.findExactOrFirstLessSpecific(Ipv6Resource.parse(pkey)).isEmpty();
                default:
                    return true;
            }
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    public Set<ObjectType> getResourceTypes() {
        return RESOURCE_TYPES;
    }

    Set<CIString> getAutNums() {
        return autNums;
    }

    IntervalMap<Ipv4Resource, Ipv4Resource> getInetRanges() {
        return inetRanges;
    }

    IntervalMap<Ipv6Resource, Ipv6Resource> getInet6Ranges() {
        return inet6Ranges;
    }
}
