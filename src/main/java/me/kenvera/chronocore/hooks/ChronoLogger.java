package me.kenvera.chronocore.hooks;

import me.kenvera.chronocore.ChronoCore;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class ChronoLogger {
    private final ChronoCore plugin;
    public ChronoLogger(ChronoCore plugin) {
        this.plugin = plugin;
    }

    public void log(String message, boolean silent, Long... textChannelIds) {
        for (Long textChannelId : textChannelIds) {
            message =  now() + " [Mix] " + message;
            plugin.getDiscordListeners().sendMessage(textChannelId, message, silent);
        }
        System.out.println(message);
    }

    private String now() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        ZoneId jakartaZoneId = ZoneId.of("Asia/Jakarta");
        LocalDateTime localDateTime = LocalDateTime.now(jakartaZoneId);
        return formatter.format(localDateTime);
    }
}
