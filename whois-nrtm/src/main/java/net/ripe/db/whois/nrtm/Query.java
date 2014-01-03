package net.ripe.db.whois.nrtm;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import javax.annotation.concurrent.Immutable;

@Immutable
public class Query {
    private static final OptionParser PARSER = new OptionParser() {{
        accepts("g", "Get the NRTM stream").withRequiredArg().ofType(String.class).describedAs("<SOURCE>:<VERSION>:<FIRST_SERIAL-LAST_SERIAL>, e.g. RIPE:3:155753-LAST");
        accepts("q", "(sources) Returns the current set of sources along with the information required for mirroring.").withRequiredArg().ofType(String.class).describedAs("source");
        accepts("k", "Requests a persistent connection.");
    }};

    public enum QueryArgument {
        SOURCES, VERSION
    }

    private static final Splitter SPACE_SPLITTER = Splitter.on(' ').omitEmptyStrings();
    private static final Splitter COLON_SPLITTER = Splitter.on(':');
    private static final Splitter DASH_SPLITTER = Splitter.on('-').trimResults();

    private final OptionSet options;
    private final String supportedSource;

    private int version;
    private int serialBegin;
    private int serialEnd;
    private QueryArgument queryArgument;

    public Query(final String supportedSource, final String queryString) {
        this.supportedSource = supportedSource;
        options = PARSER.parse(Iterables.toArray(SPACE_SPLITTER.split(queryString), String.class));

        validateAndParseQuery();
    }

    private void validateAndParseQuery() {
        if (!options.hasOptions()) {
            throw new IllegalArgumentException("%ERROR:405: no flags passed");
        }

        if (options.has("q") && (options.has("g") || options.has("k"))) {
            throw new IllegalArgumentException("%ERROR:405: -q cannot be used with any other options");
        }

        if (options.has("k") && !options.has("g")) {
            throw new IllegalArgumentException("%ERROR:405: -k cannot be used with out -g");
        }

        if (options.has("g")) {
            validateAndParseGFlag();
        }

        if (options.has("q")) {
            try {
                queryArgument = QueryArgument.valueOf(options.valueOf("q").toString().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("%ERROR:405: unsupported option for -q flag.");
            }
        }
    }

    private void validateAndParseGFlag() {
        try {
            Iterator<String> streamInfo = COLON_SPLITTER.split((String) options.valueOf("g")).iterator();

            final String source = streamInfo.next();
            if (!supportedSource.equalsIgnoreCase(source)) {
                throw new IllegalArgumentException("%ERROR:403: unknown source " + source);
            }

            version = Integer.parseInt(streamInfo.next());
            if (version < 1 || version > NrtmServer.NRTM_VERSION) {
                throw new IllegalArgumentException("%ERROR:406: NRTM version mismatch");
            }

            Iterator<String> serialRange = DASH_SPLITTER.split(streamInfo.next()).iterator();

            serialBegin = Integer.parseInt(serialRange.next());

            String nextSerialEnd = serialRange.next();

            if (nextSerialEnd.equalsIgnoreCase("LAST")) {
                this.serialEnd = -1;
            } else {
                this.serialEnd = Integer.parseInt(nextSerialEnd);
                if (this.serialEnd < this.serialBegin) {
                    throw new IllegalArgumentException("%ERROR:405: syntax error");
                }
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("%ERROR:405: syntax error", e);
        } catch (NoSuchElementException e) {
            throw new IllegalArgumentException("%ERROR:405: syntax error", e);
        }
    }

    public int getSerialBegin() {
        return serialBegin;
    }

    public int getSerialEnd() {
        return serialEnd;
    }

    public int getVersion() {
        return version;
    }

    public boolean isInfoQuery() {
        return options.has("q");
    }

    public boolean isMirrorQuery() {
        return options.has("g");
    }

    public boolean isKeepalive() {
        return options.has("k");
    }

    public QueryArgument getQueryOption() {
        return queryArgument;
    }

    public void setSerialEnd(int endRange) {
        this.serialEnd = endRange;
    }
}
