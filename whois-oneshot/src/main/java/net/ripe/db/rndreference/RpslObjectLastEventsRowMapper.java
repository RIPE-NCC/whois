package net.ripe.db.rndreference;

import com.google.gson.Gson;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowCallbackHandler;
import redis.clients.jedis.Jedis;

import java.sql.ResultSet;
import java.sql.SQLException;

class RpslObjectLastEventsRowMapper implements RowCallbackHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpslObjectLastEventsRowMapper.class);
    private int processed = 0;
    private Gson gson = new Gson();

    @Override
    public void processRow(final ResultSet resultSet) throws SQLException {
        final int objectType = resultSet.getInt(3);
        final String key = objectType + RpslObjectTimeLine.KEY_SEPERATOR + resultSet.getString(1);
        final boolean deleteEvent = (resultSet.getInt(5) == 0);
        final long eventTimestampInMillis = resultSet.getLong(2) * 1000L;
        final byte[] object = resultSet.getBytes(4);

        new RedisTemplate().execute(new RedisRunner() {
            @Override
            public void run(final Jedis jedis) {
                if (!deleteEvent) {
                    jedis.rpush(key, gson.toJson(new LastEventForRedis(eventTimestampInMillis, RpslObject.parse(object), deleteEvent)));
                    processed++;
                } else {
                    jedis.rpush(key, gson.toJson(new LastEventForRedis(eventTimestampInMillis, null, deleteEvent)));
                    processed++;
                }
                if (processed % 10000 == 0) {
                    LOGGER.info("processed {} ", processed);
                }
            }
        });
    }

}