package me.kenvera.chronocore.Command;

import me.kenvera.chronocore.ChronoCore;
import me.kenvera.chronocore.Object.Ban;
import me.kenvera.chronocore.Object.Mute;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.Jedis;

import java.awt.desktop.AboutEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class DebugCommand implements CommandExecutor {
    private final ChronoCore plugin;
    private static final String TOKEN = "SELECT discord FROM CNS1_cnplayerdata_1.token LIMIT 1";
    private static final String SET_TOKEN = "UPDATE CNS1_cnplayerdata_1.token SET discord = ? LIMIT 1";
    public DebugCommand(ChronoCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args[0].equalsIgnoreCase("group")) {
            User user = plugin.getLuckPerms().getUserManager().getUser(Bukkit.getPlayer(args[1]).getUniqueId());
            assert user != null;
            Collection<Group> inheritedGroup = user.getInheritedGroups(user.getQueryOptions());
            Set<String> groups = user.getNodes(NodeType.INHERITANCE).stream()
                    .map(InheritanceNode::getGroupName)
                    .collect(Collectors.toSet());
            List<String> groupData = List.of(plugin.getPlayerData().getGroup(Bukkit.getPlayer(args[1]).getUniqueId().toString()).split(","));
            sender.sendMessage("§7Inherited Group:");
            for (String group : groups) {
//                String groupName = group.getName();
                sender.sendMessage(group);
            }
            sender.sendMessage("");

            sender.sendMessage("§7Group Data:");
            for (String group : groupData) {
                sender.sendMessage(group);
            }
            sender.sendMessage("");
//        } else if (args[0].equalsIgnoreCase("image")) {
//            try {
//                net.dv8tion.jda.api.entities.User user = plugin.getDiscordConnection().getJda().getUserById(224339263259541506L);
//
//
//                URL fontURL = new URL("https://cdn.discordapp.com/attachments/1193020801134375052/1193028722966675456/Dish_Out.ttf?ex=65ab3994&is=6598c494&hm=d5380bd7e758b593660991b76ae154725bea8fce6af7000b29a48711aac2c700&");
//                URL avatarURL = new URL(user.getAvatarUrl());
//                Path tempFontFile = Files.createTempFile("font", ".ttf");
//                Files.copy(fontURL.openStream(), tempFontFile, StandardCopyOption.REPLACE_EXISTING);
//
//                Font font = Font.createFont(Font.TRUETYPE_FONT, tempFontFile.toFile());
//                Color textColor = new Color(30, 30, 30);
//
//                BufferedImage templateImage = ImageIO.read(new URL("https://cdn.discordapp.com/attachments/1193020801134375052/1193020868113207407/cn.png?ex=65ab3244&is=6598bd44&hm=706629d85c376d0729c195606ce0344f19dde84b9220b18bdcd1d0385b9171c2&"));
//                BufferedImage overlayImage = ImageIO.read(new URL("https://cdn.discordapp.com/attachments/1193020801134375052/1193048996000841758/logocn_2.png?ex=65ab4c76&is=6598d776&hm=b920c99a5f5a22cad1d3b28762655ba1d46c9c10bbfc89a8dce2f85ce70b0dc5&"));
//                BufferedImage avatarImage = ImageIO.read(avatarURL);
//                Graphics2D graphics = templateImage.createGraphics();
//                int imageWidth = templateImage.getWidth();
//                int imageHeight = templateImage.getHeight();
//                int centerX = imageWidth / 2;
//                int centerY = imageHeight / 2;
//
//                int overlayWidth = overlayImage.getWidth();
//                int overlayHeight = overlayImage.getHeight();
//                int overlayX = centerX - overlayWidth / 2;
//                int overlayY = centerY - overlayHeight / 2 - 50;
//
//                FontMetrics metrics = graphics.getFontMetrics();
//                int textWidth = metrics.stringWidth("Welcome to Crazy Network!");
//                int textWidthUser = metrics.stringWidth(user.getGlobalName());
//                sender.sendMessage("image width: " + imageWidth);
//                sender.sendMessage("image center X: " + centerX);
//                sender.sendMessage("text width: " + textWidth);
//                sender.sendMessage("text width user: " + textWidthUser);
//
//                int textX = (centerX - textWidth) / 2 - 20;
//                sender.sendMessage("text center X: " + textX);
//                int textY = centerY - metrics.getAscent() / 2 + 120;
//                int textXUser = centerX - textWidthUser / 2 - 20;
//                sender.sendMessage("text center X user: " + textXUser);
//                int textYUser = centerY - metrics.getAscent() / 2 + 180;
//                graphics.setFont(font.deriveFont(40f));
//                graphics.setColor(textColor);
//                graphics.drawString("Welcome to Crazy Network!", textX, textY);
//                graphics.drawString(user.getGlobalName(), textXUser, textYUser);
//
//                graphics.drawImage(overlayImage, overlayX, overlayY, null);
//                graphics.drawImage(avatarImage, overlayX, overlayY, null);
//                graphics.dispose();
//                Files.delete(tempFontFile);
//
//                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//                ImageIO.write(templateImage, "png", outputStream);
//                InputStream imageStream = new ByteArrayInputStream(outputStream.toByteArray());
//
//                TextChannel channel = plugin.getDiscordConnection().getTextChannel(1193020801134375052L);
//                MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();
//                messageCreateBuilder.addFiles(AttachedFile.fromData(imageStream, "s.png"));
//                channel.sendMessage(messageCreateBuilder.build()).queue();
//
//
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            } catch (FontFormatException e) {
//                throw new RuntimeException(e);
//            }
        } else if (args[0].equalsIgnoreCase("ban")) {
            try {
                String uuid = ChronoCore.getInstance().getPlayerData().getUUID(args[1]);
                Ban ban = ChronoCore.getInstance().getBanHandler().getBan(uuid);
                if (ban != null) {
                    long expire = ban.getExpire();
                    sender.sendMessage(String.valueOf(expire));
                }
            } catch (SQLException e) {
                e.printStackTrace(System.out);
            }

        } else if (args[0].equalsIgnoreCase("mute")) {
            String uuid = ChronoCore.getInstance().getPlayerData().getUUID(args[1]);
            Mute mute = ChronoCore.getInstance().getMuteHandler().getMute(uuid);
            if (mute != null) {
                long expire = mute.getExpire();
                String reason = mute.getReason();
                String issuer = mute.getIssuer();
                boolean muted = ChronoCore.getInstance().getMuteHandler().isMuted(uuid);
                sender.sendMessage(String.valueOf(expire));
                sender.sendMessage(reason);
                sender.sendMessage(issuer);
                sender.sendMessage(String.valueOf(muted));
            }
        } else if (args[0].equalsIgnoreCase("prefix")) {
            sender.sendMessage(ChronoCore.getInstance().getPlayerData().getPrefix(args[1]));
        } else if (args[0].equalsIgnoreCase("delredis")) {
            try (Jedis jedis = ChronoCore.getInstance().getRedisManager().getJedis().getResource()) {
                for (String key : jedis.keys("mute:*")) {
                    // Delete each key
                    if (key.matches("mute:[0-9a-fA-F\\-]+")) {
                        // Delete the key
                        jedis.del(key);
                        sender.sendMessage("Key deleted: " + key);
                    }
                }
            }
        } else if (args[0].equalsIgnoreCase("async")) {
            Player player = (Player) sender;
            if (args[1].equalsIgnoreCase("invalidate")) {
                plugin.getPlayerDataHandler().invalidateCache(player.getUniqueId());
                player.sendMessage("Invalidated cache: " + player.getUniqueId().toString());
                player.sendMessage(Thread.currentThread().getName());
            } else if (args[1].equalsIgnoreCase("get")) {
                player.sendMessage(Thread.currentThread().getName());
                plugin.getPlayerDataHandler().getPlayerData(player.getUniqueId(), player.getName(), false)
                        .thenAcceptAsync(playerData -> {
                            if (playerData != null) {
                                player.sendMessage("PlayerData Debug:");
                                player.sendMessage("PlayerName: " + playerData.getPlayerName());
                                player.sendMessage("Address: " + playerData.getAddress());
                                player.sendMessage("InheritedGroup: " + playerData.getInheritedGroup());
                                player.sendMessage(Thread.currentThread().getName());
                            } else {
                                player.sendMessage("Failed to retrieve PlayerData.");
                            }
                        })
                        .exceptionally(ex -> {
                            ex.printStackTrace();
                            player.sendMessage("Error: " + ex.getMessage());
                            return null;
                        });
            } else if (args[1].equalsIgnoreCase("load")) {
                plugin.getPlayerDataHandler().loadData(player.getUniqueId());
                player.sendMessage(Thread.currentThread().getName());
            } else if (args[1].equalsIgnoreCase("reset")) {
//                plugin.getGroupHandler().resetGroup(player.getUniqueId(), player.getName(), player.getName(), false);
                player.sendMessage(Thread.currentThread().getName());
            } else if (args[1].equalsIgnoreCase("migrate")) {
                CompletableFuture.runAsync(() -> {
                    String countSql = "SELECT COUNT(DISTINCT username) FROM player_data WHERE `group` != 'default'";
                    int totalUserCount = 0;
                    try (Connection connection = plugin.getSqlManager().getConnection();
                         PreparedStatement countStatement = connection.prepareStatement(countSql)) {

                        ResultSet countResultSet = countStatement.executeQuery();
                        if (countResultSet.next()) {
                            totalUserCount = countResultSet.getInt(1); // Total number of distinct usernames
                        }
                    } catch (SQLException e) {
                        e.printStackTrace(System.err);
                    }


                    String sql = "SELECT * FROM player_data WHERE `group` != 'default'";
                    int currentUserCount = 0;
                    Set<String> processedUsers = new HashSet<>();
                    try (Connection connection = plugin.getSqlManager().getConnection();
                         PreparedStatement statement = connection.prepareStatement(sql)) {

                        ResultSet resultSet = statement.executeQuery();
                        Map<String, String> playerGroup = new HashMap<>();
                        while (resultSet.next()) {
                            String username = resultSet.getString("username");
                            String group = resultSet.getString("group");
                            playerGroup.put(username, group);
                        }

                        for (Map.Entry<String, String> entry : playerGroup.entrySet()) {
                            String usernameMap = entry.getKey();

                            if (!processedUsers.contains(usernameMap)) {
                                processedUsers.add(usernameMap);
                                currentUserCount++;
                            }
                            String groupMap = entry.getValue();

                            String[] groups = groupMap.split(",");

                            for (String groupMapKey : groups) {
                                try (Jedis jedis = plugin.getRedisManager().getJedis().getResource()) {
                                    jedis.rpush("group::" + usernameMap, groupMapKey);
                                    double progressPercentage = Math.min((double) currentUserCount / totalUserCount * 100, 100);
                                    System.out.println("Processing " + usernameMap + ":" + groupMapKey + " (" + String.format("%.2f", progressPercentage) + "%" + ")");

                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    } catch (SQLException e) {
                        e.printStackTrace(System.err);
                    }
                }, plugin.getExecutorService());
            }

        }


//        } else if (args[0].equalsIgnoreCase("token")) {
//            try (Connection connection = plugin.getSqlManager().getConnection();
//                 PreparedStatement statement = connection.prepareStatement(TOKEN)) {
//                ResultSet resultSet = statement.executeQuery();
//                if (resultSet.next()) {
//                    sender.sendMessage(resultSet.getString("discord"));
//                }
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//        } else if (args[0].equalsIgnoreCase("settoken")) {
//            try (Connection connection = plugin.getSqlManager().getConnection();
//                 PreparedStatement statement = connection.prepareStatement(SET_TOKEN)) {
//
//                statement.setString(1, args[1]);
//                statement.executeUpdate();
//                sender.sendMessage(args[1]);
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//        }


        return false;
    }
}
