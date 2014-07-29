package net.ripe.db.rndreference;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisTemplate {
    private static final JedisPool pool = new JedisPool(new JedisPoolConfig(), "localhost");

    public void clearAndExecute(final RedisRunner runner) {
        final Jedis jedis = pool.getResource();
        jedis.flushAll();
        jedis.close();

        runner.run(pool.getResource());

        // close redis pool
        pool.returnResource(jedis);
    }

    public void execute(final RedisRunner runner) {
        final Jedis jedis = pool.getResource();

        runner.run(jedis);

        pool.returnResource(jedis);
    }

    public static void shutdown() {
        pool.destroy();
    }
}
