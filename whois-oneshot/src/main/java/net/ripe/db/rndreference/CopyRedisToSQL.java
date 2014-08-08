package net.ripe.db.rndreference;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mysql.jdbc.Driver;
import net.ripe.db.whois.internal.api.rnd.domain.RpslObjectReference;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CopyRedisToSQL {
    private static final Logger LOGGER = LoggerFactory.getLogger(CopyRedisToSQL.class);
    private final JdbcTemplate jdbcTemplate;
    private final Gson gson;


    public static void main(String[] args) {
        CopyRedisToSQL app = new CopyRedisToSQL();
        app.run();
    }

    public CopyRedisToSQL() {
        try {
            this.jdbcTemplate = new JdbcTemplate(new SimpleDriverDataSource(new Driver(), "jdbc:mysql://localhost:3306/WHOIS_REVISIONS_ONLY", "root", null));
        } catch (SQLException ex) {
            throw new RuntimeException("Unable to open database");
        }

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
        Set<String> redisKeyCache = new HashSet<>();

        while (!scanningDone) {
            ScanResult<String> scanResult = jedis.scan(cursor);
            for (String redisKey : scanResult.getResult()) {
                if (!redisKeyCache.contains(redisKey)) {
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
                                if (interval.getEnd().isAfterNow()) {
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
                if (!redisKeyCache.contains(key)) {
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
