package me.kenvera.chronocore.Object;

public class Mute {
    private String uuid, issuer, reason;
    private long expire;

    public Mute(String uuid, String issuer, String reason, long expire) {
        this.uuid = uuid;
        this.issuer = issuer;
        this.reason = reason;
        this.expire = expire;
    }

    public String getIssuer() {
        return this.issuer;
    }

    public String getReason() {
        return this.reason;
    }

    public long getExpire() {
        return this.expire;
    }
}
