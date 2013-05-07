package net.ripe.db.whois.query.endtoend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;

abstract class QueryReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryReader.class);

    private final Resource queryResource;

    public QueryReader(final Resource queryResource) {
        this.queryResource = queryResource;
        LOGGER.info("Using queries in: {}", queryResource.getDescription());
    }

    public Iterable<String> getQueries() {
        return new Iterable<String>() {
            @Override
            public Iterator<String> iterator() {
                return new QueryIterator(queryResource);
            }
        };
    }

    private class QueryIterator implements Iterator<String> {

        final BufferedReader reader;
        String line = null;

        private QueryIterator(final Resource queryResource) {
            try {
                reader = new BufferedReader(new InputStreamReader(queryResource.getInputStream()));
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public boolean hasNext() {
            try {
                line = reader.readLine();
                if (line == null) {
                    reader.close();
                }
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }

            return line != null;
        }

        @Override
        public String next() {
            try {
                return getQuery(line).replaceAll("\\-k", "").trim();
            } catch (RuntimeException e) {
                LOGGER.error("Getting query: {}", line, e);
                return "";
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    abstract String getQuery(String line);
}
