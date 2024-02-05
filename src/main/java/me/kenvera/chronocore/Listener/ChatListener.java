package me.kenvera.chronocore.Listener;

import me.kenvera.chronocore.ChronoCore;
import me.kenvera.chronocore.Object.Mute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.concurrent.TimeUnit;

public class ChatListener implements Listener {
    private final ChronoCore plugin;
    public ChatListener(ChronoCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Mute mute = ChronoCore.getInstance().getMuteHandler().getMute(player.getUniqueId().toString());
        if (mute != null) {
            long expire = mute.getExpire();
            if (expire >= System.currentTimeMillis()) {
                long duration = TimeUnit.MILLISECONDS.toSeconds(expire - System.currentTimeMillis());
                event.setCancelled(true);
                player.sendMessage("§cWhoops! You're temporarily muted. Chat freedom returns in §7" + duration + "s");
            }
        }
    }
}
