package net.ripe.db.rndreference;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.ripe.db.whois.internal.api.rnd.domain.RpslObjectReference;
import org.joda.time.Interval;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class RemoveDuplicateReferencesFromRedis {
    public static void main(final String[] argv) throws Exception {
        final Gson gson = new GsonBuilder()
                .registerTypeAdapter(Interval.class, new JsonDeserializer<Interval>() {
                    @Override
                    public Interval deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        return Interval.parse(json.getAsString());
                    }
                }).create();

        RedisTemplate redisTemplate = new RedisTemplate();
        redisTemplate.execute(new RedisRunner() {
            @Override
            public void run(Jedis jedis) {
                int processed = 0;
                boolean scanningDone = false;
                String cursor = ScanParams.SCAN_POINTER_START;
                while (!scanningDone) {
                    ScanResult<String> scanResult = jedis.scan(cursor);
                    for (String redisKey : scanResult.getResult()) {
                        final RpslObjectTimeLine timeLine = gson.fromJson(jedis.get(redisKey), RpslObjectTimeLine.class);
                        jedis.set(redisKey, gson.toJson(timeLine));
                        processed++;
                        if (processed % 10000 == 0) {
                            System.out.println("processed " + processed);
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
}
