package net.ripe.db.rndreference;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.ripe.db.whois.internal.api.rnd.domain.RpslObjectReference;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.beans.PropertyVetoException;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;


public class CopyRedisToSqlAlternative {
    private static final Logger LOGGER = LoggerFactory.getLogger(CopyRedisToSQL.class);
    private static final String PASSWORD_OPTION = "password";
    private static final String USER_OPTION = "username"  ;
    private static final String DBURL_OPTION = "url";

    private final JdbcTemplate jdbcTemplate;
    private final Gson gson;


    public static void main(String[] args) {
        final OptionParser parser = new OptionParser();

        parser.accepts(PASSWORD_OPTION).withRequiredArg();
        parser.accepts(USER_OPTION).withRequiredArg().required();
        parser.accepts(DBURL_OPTION).withRequiredArg().required();

        OptionSet options = parser.parse(args);
        CopyRedisToSqlAlternative app = new CopyRedisToSqlAlternative((String) options.valueOf(PASSWORD_OPTION), (String) options.valueOf(USER_OPTION), (String) options.valueOf(DBURL_OPTION));
        app.run();
    }

    public CopyRedisToSqlAlternative(String password, String username, String url) {
        ComboPooledDataSource dataSource = new ComboPooledDataSource();
        try {
            dataSource.setDriverClass("com.mysql.jdbc.Driver");
        } catch (PropertyVetoException e) {
            e.printStackTrace();
        }
        dataSource.setUser(username);
        dataSource.setPassword(password);
        dataSource.setJdbcUrl(url);
        this.jdbcTemplate = new JdbcTemplate(dataSource);

        this.gson = new GsonBuilder()
                .registerTypeAdapter(Interval.class, new JsonDeserializer<Interval>() {
                    @Override
                    public Interval deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        return Interval.parse(json.getAsString());
                    }
                }).create();
    }

    private void run() {
        RedisTemplate redisTemplate = new RedisTemplate();
        redisTemplate.execute(new RedisRunner() {
            @Override
            public void run(Jedis jedis) {
                writeToDb(jedis);
            }
        });
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
        HashMap<String, Boolean> referencesProcessed = new HashMap<>();

        while (!scanningDone) {
            ScanResult<String> scanResult = jedis.scan(cursor);
            for (String redisKey : scanResult.getResult()) {
                final RpslObjectTimeLine timeLine = gson.fromJson(jedis.get(redisKey), RpslObjectTimeLine.class);

                // add the object
                if (!referencesProcessed.containsKey(redisKey)) {
                    for (Map.Entry<Interval, RevisionWithReferences> entry : timeLine.getRpslObjectIntervals().entrySet()) {
                        final RevisionWithReferences current = entry.getValue();
                        final Interval interval = entry.getKey();
                        KeyHolder holder = insertNewObject(timeLine, current, interval);
                        dbPrimaryKeyCache.put(current.getRevision() + RpslObjectTimeLine.KEY_SEPERATOR + redisKey, holder.getKey().longValue());
                        insertions++;
                    }
                    referencesProcessed.put(redisKey, false);
                }

                // add the references
                if (!referencesProcessed.get(redisKey)) {
                    for (Map.Entry<Interval, RevisionWithReferences> entry : timeLine.getRpslObjectIntervals().entrySet()) {
                        final RevisionWithReferences current = entry.getValue();
                        if (!current.isDeleted()) {
                            int objectRevision = current.getRevision();
                            for (RpslObjectReference reference : entry.getValue().getOutgoingReferences()) {
                                String referenceRedisKey = reference.getKey().toString();
                                if (reference.getKey().getObjectType() != 999) {
                                    // make sure the referenced object gets stored
                                    if (!referencesProcessed.containsKey(referenceRedisKey)) {
                                        final RpslObjectTimeLine referenceTimeline = gson.fromJson(jedis.get(referenceRedisKey), RpslObjectTimeLine.class);
                                        for (Map.Entry<Interval, RevisionWithReferences> referenceEntry : referenceTimeline.getRpslObjectIntervals().entrySet()) {
                                            final RevisionWithReferences referenceRevisionWithReferences = referenceEntry.getValue();
                                            final Interval referenceInterval = referenceEntry.getKey();
                                            KeyHolder referenceHolder = insertNewObject(referenceTimeline, referenceRevisionWithReferences, referenceInterval);
                                            dbPrimaryKeyCache.put(referenceRevisionWithReferences.getRevision() + RpslObjectTimeLine.KEY_SEPERATOR + referenceRedisKey,
                                                    referenceHolder.getKey().longValue());
                                            insertions++;
                                        }
                                        referencesProcessed.put(referenceRedisKey, false);
                                    }
                                    for (int referenceRevision : reference.getRevisions()) {
                                        Long objectPk = dbPrimaryKeyCache.get(objectRevision + RpslObjectTimeLine.KEY_SEPERATOR + redisKey);
                                        Long referencePk = dbPrimaryKeyCache.get(referenceRevision + RpslObjectTimeLine.KEY_SEPERATOR + referenceRedisKey);
                                        if (objectPk == null || referencePk == null) {
                                            LOGGER.error("PK for {} is {}, PK for {} is {}",
                                                    objectRevision + RpslObjectTimeLine.KEY_SEPERATOR + redisKey, objectPk,
                                                    referenceRevision + RpslObjectTimeLine.KEY_SEPERATOR + referenceRedisKey, referencePk);
                                        } else {
                                            insertNewReference(
                                                    objectPk,
                                                    referencePk);
                                            insertions++;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    referencesProcessed.put(redisKey, true);
                }
            }

            if (scanResult.getStringCursor().equalsIgnoreCase("0")) {
                scanningDone = true;
            } else {
                cursor = scanResult.getStringCursor();
            }
        }
        LOGGER.info("{} object insertions", insertions);
    }

    private void insertNewReference(Long fromObjectRevision, Long toObjectRevision) {
        jdbcTemplate.update(
                "INSERT INTO object_reference (from_version, to_version) VALUES (?, ?)",
                fromObjectRevision, toObjectRevision);
    }

    private KeyHolder insertNewObject(final RpslObjectTimeLine timeLine, final RevisionWithReferences current, final Interval interval) {
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
                if (interval.getEnd().isAfterNow()) {
                    ps.setNull(4, Types.INTEGER);
                } else {
                    ps.setInt(4, new Long(interval.getEndMillis() / 1000L).intValue());

                }
                ps.setInt(5, current.getRevision());
                return ps;
            }
        }, holder);
        return holder;
    }
}
