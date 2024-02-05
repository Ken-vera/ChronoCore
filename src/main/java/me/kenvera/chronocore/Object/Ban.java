package me.kenvera.chronocore.Object;

public class Ban {
    private String uuid, issuer, reason;
    private long expire, bannedTime, id;

    public Ban(long id, String uuid, String issuer, String reason, long expire, long bannedTime) {
        this.id = id;
        this.uuid = uuid;
        this.issuer = issuer;
        this.reason = reason;
        this.expire = expire;
        this.bannedTime = bannedTime;
    }

    public String getReason() {
        return this.reason;
    }

    public String getIssuer() {
        return this.issuer;
    }

    public long getExpire() {
        return this.expire;
    }

    public long getBannedTime() {
        return this.bannedTime;
    }

    public long getId() {
        return this.id;
    }
}
