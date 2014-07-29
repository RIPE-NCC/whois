package net.ripe.db.whois.internal.api.rnd;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import net.ripe.db.whois.internal.api.rnd.dao.JdbcObjectReferenceDao;
import net.ripe.db.whois.internal.api.rnd.domain.RpslObjectKey;
import net.ripe.db.whois.internal.api.rnd.domain.RpslObjectTimeLine;
import net.ripe.db.whois.internal.api.rnd.domain.RpslObjectWithReferences;
import org.joda.time.Interval;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Heavy WIP - I imagine this class turning into a JMX call or something
 *
 * Calls to JdbcObjectReferenceDao is commented out, at the time of writing this those calls don't yet exist
 */
public class JsonReader {
    private final Gson gson;
    private final JdbcObjectReferenceDao referenceDao;

    public JsonReader(final Gson gson, final JdbcObjectReferenceDao referenceDao) {
        this.gson = gson;
        this.referenceDao = referenceDao;
    }

    public void readJsonFromFile(final String fileName) throws IOException {
        BufferedReader reader = null;
        try {
             reader = Files.newBufferedReader(Paths.get(URI.create(fileName)), Charsets.UTF_8);

            String line = null;
            while ((line = reader.readLine()) != null) {
                final RpslObjectTimeLine timeLine = gson.fromJson(line, RpslObjectTimeLine.class);
                final Map<Interval, RpslObjectWithReferences> rpslObjectIntervals = timeLine.getRpslObjectIntervals();

                for (Interval interval : rpslObjectIntervals.keySet()) {
//                    int versionId = referenceDao.addObjectVersion(timeLine.getObjectType(), timeLine.getKey(), interval.getStart(), interval.getEnd());
                    final RpslObjectWithReferences references = rpslObjectIntervals.get(interval);

                    for (RpslObjectKey referencing : references.getOutgoing()) {
//                        referenceDao.addReferencing(versionId, referencing.getObjectType(), referencing.getPkey());
                    }

                    for (RpslObjectKey referencedBy : references.getIncoming()) {
//                        referenceDao.addReferencedBy(versionId, referencedBy.getObjectType(), referencedBy.getPkey());
                    }
                }
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }
}
