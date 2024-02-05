package me.kenvera.chronocore.Handler;

import me.kenvera.chronocore.ChronoCore;
import me.kenvera.chronocore.Object.Mute;
import redis.clients.jedis.Jedis;

public class MuteHandler {
    public Mute getMute(String uuid) {
        try (Jedis jedis = ChronoCore.getInstance().getRedisManager().getJedis().getResource()) {
            String expire = jedis.hget("mute:" + uuid, "mute-expire");
            String reason = jedis.hget("mute:" + uuid, "mute-reason");
            String issuer = jedis.hget("mute:" + uuid, "mute-issuer");
            if (expire != null) {
                return new Mute(uuid, issuer, reason, Long.parseLong(expire));
            }
            return null;
        }
    }

    public boolean isMuted(String uuid) {
        return getMute(uuid) != null;
    }
}
