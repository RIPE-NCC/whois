package net.ripe.db.rndreference;


import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonWriter;
import com.mysql.jdbc.Driver;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.ripe.db.whois.common.dao.jdbc.JdbcStreamingHelper;
import net.ripe.db.whois.common.dao.jdbc.domain.ObjectTypeIds;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectTemplate;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.internal.api.rnd.domain.RpslObjectKey;
import net.ripe.db.whois.internal.api.rnd.domain.RpslObjectReference;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import javax.annotation.Nullable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static net.ripe.db.whois.common.rpsl.AttributeType.ABUSE_MAILBOX;
import static net.ripe.db.whois.common.rpsl.AttributeType.FINGERPR;
import static net.ripe.db.whois.common.rpsl.AttributeType.IFADDR;
import static net.ripe.db.whois.common.rpsl.AttributeType.IRT_NFY;
import static net.ripe.db.whois.common.rpsl.AttributeType.MNT_NFY;
import static net.ripe.db.whois.common.rpsl.AttributeType.NOTIFY;
import static net.ripe.db.whois.common.rpsl.AttributeType.NSERVER;
import static net.ripe.db.whois.common.rpsl.AttributeType.REF_NFY;
import static net.ripe.db.whois.common.rpsl.AttributeType.UPD_TO;
import static org.joda.time.DateTime.now;

/**
 * Retrieves rpslobject timelines from whois database. Optionally a starting timestamp (in seconds)
 * can be defined so the script can be run as cronjob to provide regular updates:
 * Complete dump:
 * <p/>
 * RndRetrieveReferenceAndReferencedBy --url=jdbc:mysql://db-pre-sql-1.prepdev.ripe.net/WHOIS_UPDATE_RIPE --password=xxxx
 * <p/>
 * From a timestamp:
 * <p/>
 * RndRetrieveReferenceAndReferencedBy --url=jdbc:mysql://db-pre-sql-1.prepdev.ripe.net/WHOIS_UPDATE_RIPE --password=dbint --start=10000
 * <p/>
 * NOTE: this program can potentially be very memory intensive due to caching of person/role objects. It is tested with a
 * limit of 100.000 objects with the following VM settings:
 * <p/>
 * -Xms128M -Xmx4096M
 * <p/>
 * NOTE: This program uses redis to store intermediate results and to generate the final JSON result. Before running this program,
 * Redis needs to run as a seperate process on the same machine
 * <p/>
 * NOTE: When running this as an update of an existing datastore, when reading in the generated JSON file the following logic needs to
 * used for pre-existing timelines:
 * - if the pre-existing timeline the enddate of the last interval in the pre-existing timeline needs to be set to
 * start of the first interval of the timeline update.
 * <p/>
 * <p/>
 * Then the new entries can be appended to the timelme.
 */
public class RndRetrieveReferenceAndReferencedBy {
    private static final Logger LOGGER = LoggerFactory.getLogger(RndRetrieveReferenceAndReferencedBy.class);

    // end date far in the future, indicates interval that does not have end dates
    public static final DateTime INFINITE_END_DATE = now().plusYears(100);

    private static final Set<AttributeType> OBJECT_REFERENCE_ATTRIBUTES;
    private static final String PASSWORD_OPTION = "password";
    private static final String DBURL_OPTION = "url";
    private static final String START = "start";           // starting timestamp in seconds
    private static final String TO_DB_OPTION = "to-db";
    private static final String MAX_TIMESTAMP_OPTION = "max-timestamp";
    private static final Long FROM_BEGINNING = -1L;
    public static final Integer DUMMY_OBJECT_TYPE_ID = 999;

    private static Integer MAX_TIMESTAMP = null;


    private final JdbcTemplate jdbcTemplate;
    private final Gson gson;
    private final RedisTemplate redisTemplate;
    int processed = 0;
    Integer maxTimestamp = 0;

    public static void main(final String[] argv) throws Exception {
        // read options
        final OptionParser parser = new OptionParser();
        parser.accepts(PASSWORD_OPTION).withRequiredArg().required();
        parser.accepts(DBURL_OPTION).withRequiredArg().required();
        parser.accepts(TO_DB_OPTION);

        parser.accepts(START).withRequiredArg().ofType(Long.class);
        parser.accepts(MAX_TIMESTAMP_OPTION).withRequiredArg().ofType(Integer.class);

        final OptionSet options = parser.parse(argv);

        if (options.has(MAX_TIMESTAMP_OPTION)) {
            MAX_TIMESTAMP = (Integer) options.valueOf(MAX_TIMESTAMP_OPTION);
        }
        // instantiate app
        final RndRetrieveReferenceAndReferencedBy app =
                new RndRetrieveReferenceAndReferencedBy(
                        options.valueOf(PASSWORD_OPTION).toString(),
                        options.valueOf(DBURL_OPTION).toString());

        // run app
        if (options.has(START)) {
            app.run((Long) options.valueOf(START), options.has(TO_DB_OPTION));
        } else {
            app.run(FROM_BEGINNING, options.has(TO_DB_OPTION));
        }
    }

    static {
        OBJECT_REFERENCE_ATTRIBUTES = Sets.newHashSet();
        for (ObjectTemplate template : ObjectTemplate.getTemplates()) {
            OBJECT_REFERENCE_ATTRIBUTES.addAll(template.getInverseLookupAttributes());
        }
        // excluded from standard lookups, but refers to an object
        OBJECT_REFERENCE_ATTRIBUTES.add(AttributeType.SPONSORING_ORG);

        OBJECT_REFERENCE_ATTRIBUTES.remove(NOTIFY);
        OBJECT_REFERENCE_ATTRIBUTES.remove(IFADDR);
        OBJECT_REFERENCE_ATTRIBUTES.remove(ABUSE_MAILBOX);
        OBJECT_REFERENCE_ATTRIBUTES.remove(IRT_NFY);
        OBJECT_REFERENCE_ATTRIBUTES.remove(FINGERPR);
        OBJECT_REFERENCE_ATTRIBUTES.remove(UPD_TO);
        OBJECT_REFERENCE_ATTRIBUTES.remove(MNT_NFY);
        OBJECT_REFERENCE_ATTRIBUTES.remove(REF_NFY);
        OBJECT_REFERENCE_ATTRIBUTES.remove(NSERVER);
    }

    public RndRetrieveReferenceAndReferencedBy(final String password, final String url) throws SQLException {
        this.jdbcTemplate = new JdbcTemplate(new SimpleDriverDataSource(new Driver(), url, "dbint", password));
        this.redisTemplate = new RedisTemplate();
        this.gson = new GsonBuilder()
                .registerTypeAdapter(Interval.class, new JsonDeserializer<Interval>() {
                    @Override
                    public Interval deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        return Interval.parse(json.getAsString());
                    }
                }).create();
    }

    private void run(final Long start, final boolean toDburl) {
        maxTimestamp = MAX_TIMESTAMP == null ? new Long(now().minusSeconds(5).getMillis() / 1000L).intValue() : MAX_TIMESTAMP;
        LOGGER.info("maximum timestamp {}", maxTimestamp);

        redisTemplate.execute(new RedisRunner() { // store all "events" in the last table in redis
            @Override
            public void run(final Jedis jedis) {
/*
                if (start.equals(FROM_BEGINNING)) {
                    JdbcStreamingHelper.executeStreaming(jdbcTemplate,
                            "SELECT pkey, timestamp, object_type, object, sequence_id FROM last where timestamp < ?",
                            new PreparedStatementSetter() {
                                @Override
                                public void setValues(PreparedStatement ps) throws SQLException {
                                    ps.setInt(1, maxTimestamp);
                                }
                            },
                            new RpslObjectLastEventsRowMapper());
                } else {
                    JdbcStreamingHelper.executeStreaming(jdbcTemplate,
                            "SELECT pkey, timestamp, object_type, object, sequence_id FROM last WHERE timestamp > ? AND TIMESTAMP < ?",
                            new PreparedStatementSetter() {
                                @Override
                                public void setValues(final PreparedStatement ps) throws SQLException {
                                    ps.setLong(1, start);
                                    ps.setInt(2, maxTimestamp);
                                }
                            },
                            new RpslObjectLastEventsRowMapper());
                }

                // add the events from the history table
                LOGGER.info("total objects read from last table: {}", jedis.dbSize());

                LOGGER.info("constructing timelines for all objects");
                createTimelines(start);
                LOGGER.info("all timelines constructed");

                // because person/role is interchangeable, we need to do some fancy footwork ot get the right types for the timeperiods.
                // additionally we need to set the right revisions for the references.
*/
                final Map<String, List<RefObject>> refObjectsCache = new HashMap<>();

                LOGGER.info("fixing all role/person references and add revision information");
                boolean scanningDone = false;
                String cursor = ScanParams.SCAN_POINTER_START;
                Set<String> keyCache = new HashSet<>();      // necessary because redis scan does not guarantee no duplicates

                int processedObjects = 0;
                while (!scanningDone) {
                    ScanResult<String> scanResult = jedis.scan(cursor);
                    for (String key : scanResult.getResult()) {
                        if (!keyCache.contains(key)) {
                            RpslObjectTimeLine timeline = gson.fromJson(jedis.get(key), RpslObjectTimeLine.class);
                            processed = setCorrectObjectType(refObjectsCache, timeline);
                            setRevisionsOfReferences(timeline, jedis);
                            jedis.set(key, gson.toJson(timeline));
                            processedObjects++;
                            if (processedObjects % 1000 == 0) {
                                LOGGER.info("fixed {} person/role references and added reference revisions", processedObjects);
                            }
                            keyCache.add(key);
                        }
                    }
                    if (scanResult.getStringCursor().equalsIgnoreCase("0")) {
                        scanningDone = true;
                    } else {
                        cursor = scanResult.getStringCursor();
                    }
                }
                keyCache.clear();

                if (toDburl) {
                    writeToDb(jedis);
                } else {
                    writeJson(jedis);
                }
            }
        });
        RedisTemplate.shutdown();
    }


    private void setRevisionsOfReferences(RpslObjectTimeLine timeline, Jedis jedis) {
        for (Map.Entry<Interval, RevisionWithReferences> entry : timeline.getRpslObjectIntervals().entrySet()) {
            if (!entry.getValue().isDeleted()) {
                Interval objectInterval = entry.getKey();
                for (final RpslObjectReference reference : entry.getValue().getOutgoingReferences()) {
                    if (!jedis.exists(reference.getKey().toString())) {
                        // get the timeline for this object: on a full run this should noot be called!
                        LOGGER.info("retrieving timeline for reference {}", reference.getKey().toString());
                        JdbcStreamingHelper.executeStreaming(jdbcTemplate,
                                "SELECT pkey, timestamp, object_type, object, sequence_id FROM last where object_type=? and pkey=?",
                                new PreparedStatementSetter() {
                                    @Override
                                    public void setValues(PreparedStatement ps) throws SQLException {
                                        ps.setInt(1, reference.getKey().getObjectType());
                                        ps.setString(2, reference.getKey().getPkey());
                                    }
                                }, new RpslObjectLastEventsRowMapper());
                        createSingleTimeline(FROM_BEGINNING, reference.getKey().toString(), true, jedis);
                    }

                    // make sure that we do not get duplicates when this gets called twice.
                    // TODO turn reference revisions from list to set
                    reference.clearRevisions();
                    RpslObjectTimeLine referenceTimeline = gson.fromJson(jedis.get(reference.getKey().toString()), RpslObjectTimeLine.class);
                    for (Interval referenceInterval : referenceTimeline.getRpslObjectIntervals().keySet()) {
                        if (referenceInterval.overlaps(objectInterval)) {
                            reference.addRevision(referenceTimeline.getRpslObjectIntervals().get(referenceInterval).getRevision());
                        }
                    }
                }
            }
        }
    }

    private void createSingleTimeline(Long start, String key, boolean ignoreReferences, Jedis jedis) {
        final List<HistoricRpslObject> historicRpslObjects = new ArrayList<>();
        final Gson gson = new Gson();

        final String[] tokens = key.split(RpslObjectTimeLine.KEY_SEPERATOR, 2);
        historicRpslObjects.clear();
        // retrieves all the objects not in last table.

        getHistoricRpslObjects(start, historicRpslObjects, tokens);

        final List<DatabaseRpslObject> allEvents = new ArrayList<>();

        // get the events for an object out of redis
        for (String json : jedis.lrange(key, 0, Integer.MAX_VALUE)) {
            allEvents.add(convertToLastEvent(gson.fromJson(json, LastEventForRedis.class)));
        }

        allEvents.addAll(historicRpslObjects);
        Collections.sort(allEvents);

        final RpslObjectTimeLine newTimeline = new RpslObjectTimeLine(tokens[1], Integer.parseInt(tokens[0]));
        newTimeline.setRplsObjectIntervals(constructTimeLine(key, allEvents));

        jedis.set(key, gson.toJson(newTimeline));

    }


    private void createTimelines(final Long start) {
        redisTemplate.execute(new RedisRunner() {
            @Override
            public void run(final Jedis jedis) {
                boolean scanningDone = false;
                String cursor = ScanParams.SCAN_POINTER_START;
                Set<String> keyCache = new HashSet<String>();

                while (!scanningDone) {
                    ScanResult<String> scanResult = jedis.scan(cursor);
                    for (String key : scanResult.getResult()) {
                        if (! keyCache.contains(key)) {
                            createSingleTimeline(start, key, false, jedis);
                            if (processed % 10 == 0) {
                                LOGGER.info("processed {} timelines", processed);
                            }
                            processed++;
                            keyCache.add(key);
                        }
                    }
                    if (scanResult.getStringCursor().equalsIgnoreCase("0")) {
                        scanningDone = true;
                    } else {
                        cursor = scanResult.getStringCursor();
                    }
                }
            }
        });
    }

    private int setCorrectObjectType(final Map<String, List<RefObject>> cache, final RpslObjectTimeLine timeline) {
        for (Map.Entry<Interval, RevisionWithReferences> entry : timeline.getRpslObjectIntervals().entrySet()) {
            final RevisionWithReferences revisionWithReferences = entry.getValue();
            if (!revisionWithReferences.isDeleted()) {
                final Set<RpslObjectReference> fixedSet = new HashSet<>();
                if (revisionWithReferences.getOutgoingReferences() == null) {
                    LOGGER.error("timeline for {} contains null value for outgoing references for revision {}", timeline.getKey(), entry.getKey());
                    revisionWithReferences.setOutgoingReferences(new HashSet<RpslObjectReference>());
                }
                for (RpslObjectReference base : revisionWithReferences.getOutgoingReferences()) {
                    RpslObjectKey baseKey = base.getKey();
                    if (!baseKey.getObjectType().equals(DUMMY_OBJECT_TYPE_ID)) {
                        fixedSet.add(base);
                    } else {
                        final String primaryKey = baseKey.getPkey();
                        if (!cache.containsKey(primaryKey.toUpperCase())) {
                            cache.put(primaryKey.toUpperCase(), findRefObjects(primaryKey));
                        }

                        final List<RefObject> refObjects = cache.get(primaryKey.toUpperCase());
                        switch (refObjects.size()) {
                            case 0:
                                LOGGER.error("Unable to find entry for key {} in object {}", primaryKey, timeline.getKey());
                                break;
                            case 1:
                                fixedSet.add(new RpslObjectReference(new RpslObjectKey(refObjects.get(0).objectId, primaryKey), new ArrayList<Integer>()));
                                break;
                            default:
                                final RefObject refObject = findRefobjectForInterval(entry.getKey(), refObjects);
                                if (refObject != null) {
                                    fixedSet.add(new RpslObjectReference(new RpslObjectKey(refObject.objectId, primaryKey), new ArrayList<Integer>()));
                                } else {
                                    LOGGER.error("Unable to find correct entry for key {} in object {}", primaryKey, timeline.getKey());
                                }
                        }
                    }

                    if (processed % 1000 == 0) {
                        LOGGER.info("removed 999: {}", processed);
                    }
                    processed++;
                }
                revisionWithReferences.setOutgoingReferences(fixedSet);
            }
        }
        return processed;
    }

    private void getHistoricRpslObjects(final Long start, final List<HistoricRpslObject> historicRpslObjects, final String[] tokens) {
        if (start.equals(FROM_BEGINNING)) {
            JdbcStreamingHelper.executeStreaming(jdbcTemplate,
                    "SELECT timestamp, object, object_id, sequence_id FROM history WHERE object_type = ? AND pkey = ? ORDER BY timestamp ;",
                    new PreparedStatementSetter() {
                        @Override
                        public void setValues(final PreparedStatement ps) throws SQLException {
                            ps.setInt(1, Integer.parseInt(tokens[0]));
                            ps.setString(2, tokens[1]);
                        }
                    },
                    new RowCallbackHandler() {
                        @Override
                        public void processRow(final ResultSet rs) throws SQLException {
                            historicRpslObjects.add(new HistoricRpslObject(new DateTime(rs.getLong(1) * 1000L), rs.getBytes(2)));
                        }
                    }
            );
        } else {
            JdbcStreamingHelper.executeStreaming(jdbcTemplate,
                    "SELECT timestamp, object, object_id, sequence_id FROM history WHERE object_type = ? AND pkey = ? AND timestamp > ? ORDER BY timestamp;",
                    new PreparedStatementSetter() {
                        @Override
                        public void setValues(final PreparedStatement ps) throws SQLException {
                            ps.setInt(1, Integer.parseInt(tokens[0]));
                            ps.setString(2, tokens[1]);
                            ps.setLong(3, start);
                        }
                    },
                    new RowCallbackHandler() {
                        @Override
                        public void processRow(final ResultSet rs) throws SQLException {
                            historicRpslObjects.add(new HistoricRpslObject(new DateTime(rs.getLong(1) * 1000L), rs.getBytes(2)));
                        }
                    }
            );
        }
    }

    private Map<Interval, RevisionWithReferences> constructTimeLine(final String key, final List<DatabaseRpslObject> allEvents) {
        final Map<Interval, RevisionWithReferences> rpslObjectTimeline = new HashMap<>();

        int revision = 0;
        for (int i = 0; i < allEvents.size(); i++) {
            final DatabaseRpslObject databaseRpslObject = allEvents.get(i);

            Interval interval;
            if (i == (allEvents.size() - 1)) {
                interval = new Interval(databaseRpslObject.getEventDate(), INFINITE_END_DATE);
            } else {
                interval = new Interval(databaseRpslObject.getEventDate(), allEvents.get(i + 1).getEventDate());
            }

            // only add the interval if begin and end are not the same
            if (!interval.getEnd().equals(interval.getStart())) {
                if (databaseRpslObject instanceof LastEvent && ((LastEvent) databaseRpslObject).isDeleteEvent()) {
                    revision++;
                    rpslObjectTimeline.put(interval, new RevisionWithReferences(true, null, revision));
                } else {
                    try {
                        final RpslObject rpslObject =
                                (databaseRpslObject instanceof LastEvent ?
                                        ((LastEvent) databaseRpslObject).getRpslObject() :
                                        RpslObject.parse(((HistoricRpslObject) databaseRpslObject).getObjectBytes()));
                        revision++;
                        rpslObjectTimeline.put(interval, new RevisionWithReferences(false, getReferencingObjects(rpslObject), revision));
                    } catch (Exception ex) {
                        LOGGER.error("ERROR: object {}: unable to parse object data but not a delete event.", key);
                        LOGGER.error("ERROR: object {}: not deleted object with no data in timeline", key);
                        revision++;
                        rpslObjectTimeline.put(interval, new RevisionWithReferences(false, null, revision));
                    }
                }
            }
        }

        return rpslObjectTimeline;
    }

    @Nullable
    private RefObject findRefobjectForInterval(final Interval interval, final List<RefObject> refObjects) {
        if (new DateTime(refObjects.get(refObjects.size() - 1).timestamp * 1000L).isAfter(interval.getEnd())) {
            return refObjects.get(refObjects.size() - 1);
        } else {
            for (RefObject refObject : Lists.reverse(refObjects)) {
                if (new DateTime(refObject.timestamp * 1000L).isBefore(interval.getStart())) {
                    return refObject;
                }
            }
        }
        return null;
    }

    private List<RefObject> findRefObjects(final String primaryKey) {
        final List<RefObject> refObjects = jdbcTemplate.query(
                "SELECT timestamp, object_type FROM last WHERE pkey = ? ORDER BY timestamp;",
                new RowMapper<RefObject>() {
                    @Override
                    public RefObject mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return new RefObject(rs.getLong(1), rs.getInt(2));
                    }
                }, primaryKey);
        if (refObjects.size() > 1) {
            final List<RefObject> changesInObjectType = new ArrayList<>();
            changesInObjectType.add(refObjects.get(0));
            for (int i = 1; i < refObjects.size(); i++) {
                if (changesInObjectType.get(changesInObjectType.size() - 1).objectId != refObjects.get(i).objectId) {
                    changesInObjectType.add(refObjects.get(i));
                }
            }
            if (changesInObjectType.size() > 1) {
                LOGGER.info("Object with pkey {} has {} identies", primaryKey, changesInObjectType.size());
            }
            return changesInObjectType;
        } else {
            return refObjects;
        }
    }

    // code nicked from objectDao
    private Set<RpslObjectReference> getReferencingObjects(final RpslObject rpslObject) {
        final Set<RpslObjectReference> referencing = Sets.newHashSet();

        for (final RpslAttribute attribute : rpslObject.findAttributes(OBJECT_REFERENCE_ATTRIBUTES)) {
            for (final CIString referenceValue : attribute.getReferenceValues()) {
                Set<ObjectType> objectTypes = attribute.getType().getReferences(referenceValue);

                // optimization: sponsoring has no referene values but we know it must be an org
                if (attribute.getType() == AttributeType.SPONSORING_ORG) {
                    objectTypes = Sets.newHashSet(ObjectType.ORGANISATION);
                }

                if (objectTypes.size() == 1) {
                    referencing.add(new RpslObjectReference(new RpslObjectKey(ObjectTypeIds.getId(objectTypes.iterator().next()), referenceValue.toString()), new ArrayList<Integer>()));
                } else {
                    switch (attribute.getType()) {
                        case AUTH:
                            // do nothing, we do not reverse lookup auths: local optimization.
                            break;
                        default:
                            referencing.add(new RpslObjectReference(new RpslObjectKey(DUMMY_OBJECT_TYPE_ID, referenceValue.toUpperCase()), new ArrayList<Integer>()));
                    }
                }
            }
        }
        return referencing;
    }

    private DatabaseRpslObject convertToLastEvent(final LastEventForRedis lastEventForRedis) {
        if (lastEventForRedis.getObject() != null) {
            return new LastEvent(RpslObject.parse(lastEventForRedis.getObject()), new DateTime(lastEventForRedis.getTimestamp()), lastEventForRedis.getDeleteEvent());
        } else {
            return new LastEvent(null, new DateTime(lastEventForRedis.getTimestamp()), lastEventForRedis.getDeleteEvent());
        }
    }

    private class RefObject {
        int objectId;
        Long timestamp;

        public RefObject(final long timestamp, final int objectId) {
            this.timestamp = timestamp;
            this.objectId = objectId;      //objectid cannot change objectType, is therefore stable
        }
    }

    private void writeJson(final Jedis jedis) {
        // write the data from redis to an output file
        try {
            final JsonWriter writer = new JsonWriter(new OutputStreamWriter(
                    new FileOutputStream("/tmp/data.json"), "utf-8"));
            writer.beginArray();
            boolean scanningDone = false;
            String cursor = ScanParams.SCAN_POINTER_START;

            Set<String> keyCache = new HashSet<>();
            while (!scanningDone) {
                ScanResult<String> scanResult = jedis.scan(cursor);
                for (String key : scanResult.getResult()) {
                    if (!keyCache.contains(key)) {
                        // we have to do this, otherwise we write a string instead of an object
                        //gson.toJson(gson.fromJson(jedis.get(key), RpslObjectTimeLine.class), writer);
                        LOGGER.info("key {}", key);
                        JsonElement element = gson.fromJson(jedis.get(key), JsonElement.class);
                        gson.toJson(element, writer);
                        keyCache.add(key);
                    }
                }
                if (scanResult.getStringCursor().equalsIgnoreCase("0")) {
                    scanningDone = true;
                } else {
                    cursor = scanResult.getStringCursor();
                }
            }
            writer.endArray();
            writer.close();
        } catch (IOException ex) {
            LOGGER.error("Unable to write: {}", ex.getMessage());
        }
    }

    private void writeToDb(Jedis jedis) {
        Map<String, Long> dbPrimaryKeyCache = new HashMap<>();
        // truncate the tables
        jdbcTemplate.execute("TRUNCATE TABLE object_reference");
        jdbcTemplate.execute("TRUNCATE TABLE object_version");

        // store the objects and cache primary key
        LOGGER.info("storing objects in database");

        int insertions = 0;
        boolean scanningDone = false;
        String cursor = ScanParams.SCAN_POINTER_START;
        Set<String> redisKeyCache = new HashSet<>();

        while (!scanningDone) {
            ScanResult<String> scanResult = jedis.scan(cursor);
            for (String redisKey : scanResult.getResult()) {
                if (! redisKeyCache.contains(redisKey)) {
                    final RpslObjectTimeLine timeLine = gson.fromJson(jedis.get(redisKey), RpslObjectTimeLine.class);
                    for (Map.Entry<Interval, RevisionWithReferences> entry : timeLine.getRpslObjectIntervals().entrySet()) {
                        final RevisionWithReferences current = entry.getValue();
                        final Interval interval = entry.getKey();

                        KeyHolder holder = new GeneratedKeyHolder();
                        jdbcTemplate.update(new PreparedStatementCreator() {
                            @Override
                            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                                PreparedStatement ps = con.prepareStatement(
                                        "INSERT INTO object_version (pkey, object_type, from_timestamp, to_timestamp, revision) VALUES (?,?,?,?,?)",
                                        Statement.RETURN_GENERATED_KEYS);
                                ps.setString(1, timeLine.getKey());
                                ps.setInt(2, timeLine.getObjectType());
                                ps.setInt(3, new Long(interval.getStartMillis() / 1000L).intValue());
                                if (interval.getEnd().equals(INFINITE_END_DATE)) {
                                    ps.setNull(4, Types.INTEGER);
                                } else {
                                    ps.setInt(4, new Long(interval.getEndMillis() / 1000L).intValue());

                                }
                                ps.setInt(5, current.getRevision());
                                return ps;
                            }
                        }, holder);
                        dbPrimaryKeyCache.put(current.getRevision() + RpslObjectTimeLine.KEY_SEPERATOR + redisKey, holder.getKey().longValue());
                        redisKeyCache.add(redisKey);
                        insertions++;
                    }
                }
            }
            if (scanResult.getStringCursor().equalsIgnoreCase("0")) {
                scanningDone = true;
            } else {
                cursor = scanResult.getStringCursor();
            }
        }
        LOGGER.info("{} object insertions", insertions);
        redisKeyCache.clear();

        LOGGER.info("inserting references");
        insertions = 0;
        scanningDone = false;
        cursor = ScanParams.SCAN_POINTER_START;
        while (!scanningDone) {
            ScanResult<String> scanResult = jedis.scan(cursor);
            for (String key : scanResult.getResult()) {
                if (! redisKeyCache.contains(key)) {
                    final RpslObjectTimeLine timeLine = gson.fromJson(jedis.get(key), RpslObjectTimeLine.class);
                    for (Map.Entry<Interval, RevisionWithReferences> entry : timeLine.getRpslObjectIntervals().entrySet()) {
                        if (!entry.getValue().isDeleted()) {
                            int objectRevision = entry.getValue().getRevision();
                            for (RpslObjectReference reference : entry.getValue().getOutgoingReferences()) {
                                if (reference.getKey().getObjectType() != 999) {
                                    for (int referenceRevision : reference.getRevisions()) {
                                        jdbcTemplate.update(
                                                "INSERT INTO object_reference (from_version, to_version) VALUES (?, ?)",
                                                dbPrimaryKeyCache.get(objectRevision + RpslObjectTimeLine.KEY_SEPERATOR + key),
                                                dbPrimaryKeyCache.get(referenceRevision + RpslObjectTimeLine.KEY_SEPERATOR + reference.getKey().toString()));
                                        insertions++;
                                    }
                                }
                            }
                        }
                    }
                    redisKeyCache.add(key);
                }
            }
            if (scanResult.getStringCursor().equalsIgnoreCase("0")) {
                scanningDone = true;
            } else {
                cursor = scanResult.getStringCursor();
            }
        }
        LOGGER.info("{} references inserted", insertions);
    }
}
