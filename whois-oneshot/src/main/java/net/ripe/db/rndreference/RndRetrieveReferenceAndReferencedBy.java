package net.ripe.db.rndreference;


import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.*;
import com.google.gson.stream.JsonWriter;
import com.mysql.jdbc.Driver;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.ripe.db.whois.common.dao.jdbc.JdbcStreamingHelper;
import net.ripe.db.whois.common.dao.jdbc.domain.ObjectTypeIds;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.*;
import net.ripe.db.whois.internal.api.rnd.domain.RpslObjectKey;
import net.ripe.db.whois.internal.api.rnd.domain.RpslObjectTimeLine;
import net.ripe.db.whois.internal.api.rnd.domain.RpslObjectWithReferences;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import redis.clients.jedis.Jedis;

import javax.annotation.Nullable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static net.ripe.db.whois.common.rpsl.AttributeType.*;
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

    private static final Set<AttributeType> RELATED_TO_ATTRIBUTES;
    private static final String PASSWORD_OPTION = "password";
    private static final String DBURL_OPTION = "url";
    private static final String START = "start";           // starting timestamp in seconds
    private static final Long FROM_BEGINNING = -1L;
    public static final Integer DUMMY_OBJECT_TYPE_ID = 999;


    private final JdbcTemplate jdbcTemplate;
    private final Gson gson;
    private final RedisTemplate redisTemplate;
    int processed = 0;

    public static void main(final String[] argv) throws Exception {
        // read options
        final OptionParser parser = new OptionParser();
        parser.accepts(PASSWORD_OPTION).withRequiredArg().required();
        parser.accepts(DBURL_OPTION).withRequiredArg().required();
        parser.accepts(START).withRequiredArg().ofType(Long.class);
        final OptionSet options = parser.parse(argv);

        // instantiate app
        final RndRetrieveReferenceAndReferencedBy app =
                new RndRetrieveReferenceAndReferencedBy(
                        options.valueOf(PASSWORD_OPTION).toString(),
                        options.valueOf(DBURL_OPTION).toString());

        // run app
        if (options.has(START)) {
            app.run((Long) options.valueOf(START));
        } else {
            app.run(FROM_BEGINNING);
        }
    }

    static {
        RELATED_TO_ATTRIBUTES = Sets.newHashSet();
        for (ObjectTemplate template : ObjectTemplate.getTemplates()) {
            RELATED_TO_ATTRIBUTES.addAll(template.getInverseLookupAttributes());
        }
        // excluded from standard lookups, but refers to an object
        RELATED_TO_ATTRIBUTES.add(AttributeType.SPONSORING_ORG);

        RELATED_TO_ATTRIBUTES.remove(NOTIFY);
        RELATED_TO_ATTRIBUTES.remove(IFADDR);
        RELATED_TO_ATTRIBUTES.remove(ABUSE_MAILBOX);
        RELATED_TO_ATTRIBUTES.remove(IRT_NFY);
        RELATED_TO_ATTRIBUTES.remove(FINGERPR);
        RELATED_TO_ATTRIBUTES.remove(UPD_TO);
        RELATED_TO_ATTRIBUTES.remove(MNT_NFY);
        RELATED_TO_ATTRIBUTES.remove(REF_NFY);
    }

    public RndRetrieveReferenceAndReferencedBy(final String password, final String url) throws SQLException {
        this.jdbcTemplate = new JdbcTemplate(new SimpleDriverDataSource(new Driver(), url, "dbint", password));
        this.redisTemplate = new RedisTemplate();
        this.gson = new GsonBuilder().registerTypeAdapter(Interval.class, new JsonDeserializer<Interval>() {
            @Override
            public Interval deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                return Interval.parse(json.getAsString());
            }
        }).create();
    }

    private void run(final Long start) {
        redisTemplate.clearAndExecute(new RedisRunner() { // store all "events" in the last table in redis
            @Override
            public void run(final Jedis jedis) {
                if (start.equals(FROM_BEGINNING)) {
                    JdbcStreamingHelper.executeStreaming(jdbcTemplate,
                            "SELECT pkey, timestamp, object_type, object, sequence_id FROM last LIMIT 10", new RpslObjectLastEventsRowMapper());
                } else {
                    JdbcStreamingHelper.executeStreaming(jdbcTemplate,
                            "SELECT pkey, timestamp, object_type, object, sequence_id FROM last WHERE timestamp > ? LIMIT 10",
                            new PreparedStatementSetter() {
                                @Override
                                public void setValues(final PreparedStatement ps) throws SQLException {
                                    ps.setLong(1, start);
                                }
                            },
                            new RpslObjectLastEventsRowMapper());
                }

                // add the events from the history table
                createTimelines(start);

                // because person/role is interchangeable, we need to do some fancy footwork ot get the right types for the timeperiods.
                final Map<String, List<RefObject>> refObjectsCache = new HashMap<>();

                for (String key : jedis.keys("*")) {
                    final RpslObjectTimeLine timeline = gson.fromJson(jedis.get(key), RpslObjectTimeLine.class);
                    processed = setCorrectObjectType(refObjectsCache, timeline);
                    jedis.set(key, gson.toJson(timeline));
                }

                writeJson(jedis);
            }
        });
        RedisTemplate.shutdown();
    }

    private void createTimelines(final Long start) {
        final List<HistoricRpslObject> historicRpslObjects = new ArrayList<>();
        final Gson gson = new Gson();

        redisTemplate.execute(new RedisRunner() {
            @Override
            public void run(final Jedis jedis) {
                for (String key : jedis.keys("*")) {
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
                    if (processed % 10 == 0) {
                        LOGGER.info("processed {} timelines", processed);
                    }
                    processed++;
                }
            }
        });
    }

    private int setCorrectObjectType(final Map<String, List<RefObject>> mappedRefobjects, final RpslObjectTimeLine timeline) {
        for (Map.Entry<Interval, RpslObjectWithReferences> entry : timeline.getRpslObjectIntervals().entrySet()) {
            final RpslObjectWithReferences rpslObjectWithReferences = entry.getValue();
            if (!rpslObjectWithReferences.isDeleted()) {
                final Set<RpslObjectKey> fixedSet = new HashSet<>();

                for (RpslObjectKey base : rpslObjectWithReferences.getOutgoing()) {
                    if (!base.getObjectType().equals(DUMMY_OBJECT_TYPE_ID)) {
                        fixedSet.add(base);
                    } else {
                        final String primaryKey = base.getPkey(); //base.split(RpslObjectTimeLine.KEY_SEPERATOR, 2)[1];
                        if (!mappedRefobjects.containsKey(primaryKey)) {
                            mappedRefobjects.put(primaryKey, findRefObjects(primaryKey));
                        }

                        final List<RefObject> refObjects = mappedRefobjects.get(primaryKey);
                        switch (refObjects.size()) {
                            case 0:
                                LOGGER.error("Unable to find entry for key {} in object {}", primaryKey, timeline.getKey());
                                break;
                            case 1:
                                fixedSet.add(new RpslObjectKey(refObjects.get(0).objectId, primaryKey));
                                break;
                            default:
                                final RefObject refObject = findRefobjectForInterval(entry.getKey(), refObjects);
                                if (refObject != null) {
                                    fixedSet.add(new RpslObjectKey(refObject.objectId, primaryKey));
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
                rpslObjectWithReferences.setOutgoing(fixedSet);
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

    private Map<Interval, RpslObjectWithReferences> constructTimeLine(final String key, final List<DatabaseRpslObject> allEvents) {
        final Map<Interval, RpslObjectWithReferences> rpslObjectTimeline = new HashMap<>();
        for (int i = 0; i < allEvents.size(); i++) {
            final DatabaseRpslObject databaseRpslObject = allEvents.get(i);

            Interval interval;
            if (i == (allEvents.size() - 1)) {
                interval = new Interval(databaseRpslObject.getEventDate(), INFINITE_END_DATE);
            } else {
                interval = new Interval(databaseRpslObject.getEventDate(), allEvents.get(i + 1).getEventDate());
            }

            if (databaseRpslObject instanceof LastEvent && ((LastEvent) databaseRpslObject).isDeleteEvent()) {
                rpslObjectTimeline.put(interval, new RpslObjectWithReferences(true, null));
            } else {
                try {
                    final RpslObject rpslObject =
                            (databaseRpslObject instanceof LastEvent ?
                                    ((LastEvent) databaseRpslObject).getRpslObject() :
                                    RpslObject.parse(((HistoricRpslObject) databaseRpslObject).getObjectBytes()));
                    rpslObjectTimeline.put(interval, new RpslObjectWithReferences(false, getReferencingObjects(rpslObject)));
                } catch (Exception ex) {
                    LOGGER.error("ERROR: object {}: unable to parse object data but not a delete event.", key);
                    LOGGER.error("ERROR: object {}: not deleted object with no data in timeline", key);
                    rpslObjectTimeline.put(interval, new RpslObjectWithReferences(false, null));
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
    private Set<RpslObjectKey> getReferencingObjects(final RpslObject rpslObject) {
        final Set<RpslObjectKey> referencing = Sets.newHashSet();

        for (final RpslAttribute attribute : rpslObject.findAttributes(RELATED_TO_ATTRIBUTES)) {
            for (final CIString referenceValue : attribute.getReferenceValues()) {
                Set<ObjectType> objectTypes = attribute.getType().getReferences(referenceValue);

                // optimization: sponsoring has no referene values but we know it must be an org
                if (attribute.getType() == AttributeType.SPONSORING_ORG) {
                    objectTypes = Sets.newHashSet(ObjectType.ORGANISATION);
                }

                if (objectTypes.size() == 1) {
                    referencing.add(new RpslObjectKey(ObjectTypeIds.getId(objectTypes.iterator().next()), referenceValue.toString()));
                } else {
                    switch (attribute.getType()) {
                        case AUTH:
                            // do nothing, we do not reverse lookup auths: local optimization.
                            break;
                        default:
                            referencing.add(new RpslObjectKey(DUMMY_OBJECT_TYPE_ID, referenceValue.toString()));
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
            for (String key : jedis.keys("*")) {
                // we have to do this, otherwise we write a string instead of an object
                writer.value(gson.toJson(gson.fromJson(jedis.get(key), RpslObjectTimeLine.class)));
            }
            writer.endArray();
            writer.close();
        } catch (IOException ex) {
            LOGGER.error("Unable to write: {}", ex.getMessage());
        }
    }
}
