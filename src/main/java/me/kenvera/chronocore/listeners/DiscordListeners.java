package me.kenvera.chronocore.listeners;

import me.kenvera.chronocore.ChronoCore;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class DiscordListeners extends ListenerAdapter {
    private final ChronoCore plugin;
    public DiscordListeners(ChronoCore plugin) {
        this.plugin = plugin;
    }

    public void sendMessage(Long textChannelId, String message, boolean silent) {
        TextChannel textChannel = plugin.getDiscordConnection().getTextChannel(textChannelId);

        if (textChannel != null) {
            MessageCreateData data = new MessageCreateBuilder()
                    .setContent(message)
                    .setSuppressedNotifications(silent)
                    .build();
            textChannel.sendMessage(data).queue();
        } else {
            plugin.getLogger().severe("§cText channel with id " + textChannelId.toString() + "§ccan't be found!");
        }
    }
}
