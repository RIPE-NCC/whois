package net.ripe.db.rndreference;

import redis.clients.jedis.Jedis;

public interface RedisRunner {

    public void run(Jedis jedis);
}
