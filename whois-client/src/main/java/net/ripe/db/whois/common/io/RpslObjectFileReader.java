package net.ripe.db.whois.common.io;

import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;

public class RpslObjectFileReader implements Iterable<String> {
    private final String fileName;

    public RpslObjectFileReader(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public Iterator<String> iterator() {
        return new StringIterator(fileName);
    }

    private class StringIterator implements Iterator<String> {
        private final BufferedReader bufferedReader;
        private String nextObject;

        public StringIterator(String fileName) {
            try {
                InputStream in = new FileInputStream(fileName);
                if (fileName.endsWith(".gz")) {
                    in = new GZIPInputStream(in);
                }
                bufferedReader = new BufferedReader(new InputStreamReader(in));
            } catch (IOException e) {
                throw new IllegalArgumentException(fileName, e);
            }
        }

        @Override
        public boolean hasNext() {
            if (nextObject == null) {
                nextObject = next();
            }
            return nextObject != null;
        }

        @Override
        public String next() {
            if (nextObject != null) {
                String ret = nextObject;
                nextObject = null;
                return ret;
            }

            try {
                String result;

                do {
                    String line;
                    final StringBuilder partialObject = new StringBuilder(1024);

                    while ((line = bufferedReader.readLine()) != null) {
                        if (StringUtils.isBlank(line)) {
                            break;
                        } else {
                            if (line.charAt(0) != '#' && line.charAt(0) != '%') {
                                partialObject.append(line).append('\n');
                            }
                        }
                    }

                    if (line == null && partialObject.length() == 0) {
                        return null; // terminator
                    }

                    result = partialObject.toString();

                } while (StringUtils.isBlank(result));

                return result;
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
