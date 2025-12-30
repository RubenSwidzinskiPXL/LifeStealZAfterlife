package com.zetaplugins.lifestealz.commands.MainCommand.subcommands;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import com.zetaplugins.lifestealz.LifeStealZ;
import com.zetaplugins.lifestealz.commands.SubCommand;
import com.zetaplugins.lifestealz.util.MessageUtils;
import com.zetaplugins.lifestealz.util.commands.CommandUtils;
import com.zetaplugins.lifestealz.storage.PlayerData;
import com.zetaplugins.lifestealz.storage.Storage;

import java.util.List;

public final class PrestigeSubCommand implements SubCommand {
    private final LifeStealZ plugin;
    private final FileConfiguration config;
    private final Storage storage;
    private LuckPerms luckPerms;

    public PrestigeSubCommand(LifeStealZ plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.storage = plugin.getStorage();
        
        // Try to get LuckPerms
        try {
            var provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
            if (provider != null) {
                this.luckPerms = provider.getProvider();
                plugin.getLogger().info("LuckPerms integration enabled for prestige system");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("LuckPerms not found - prestige groups won't be auto-assigned");
        }
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            if (sender instanceof Player) {
                sendPrestigeInfo((Player) sender);
                return true;
            }
            sender.sendMessage(MessageUtils.getAndFormatMsg(false, "usage", "&cUsage: /lifestealz prestige <confirm|info|set|reset|get> [player] [amount]"));
            return false;
        }

        String action = args[1].toLowerCase();

        // Admin commands
        if (action.equals("set") || action.equals("reset") || action.equals("get")) {
            return handleAdminCommand(sender, args);
        }

        // Player commands
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtils.getAndFormatMsg(
                    false,
                    "onlyPlayers",
                    "&cOnly players can use this command!"
            ));
            return false;
        }

        Player player = (Player) sender;

        if (action.equals("confirm")) {
            return handlePrestige(player);
        } else if (action.equals("info")) {
            sendPrestigeInfo(player);
            return true;
        }

        sendPrestigeInfo(player);
        return true;
    }

    /**
     * Handle admin commands for managing prestige
     */
    private boolean handleAdminCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("lifestealz.admin.prestige")) {
            CommandUtils.throwPermissionError(sender);
            return false;
        }

        String action = args[1].toLowerCase();

        if (action.equals("get")) {
            if (args.length < 3) {
                sender.sendMessage(MessageUtils.getAndFormatMsg(false, "usage", "&cUsage: /lifestealz prestige get <player>"));
                return false;
            }

            List<OfflinePlayer> players = CommandUtils.parseOfflinePlayer(args[2], false, true, plugin);
            if (players.isEmpty()) {
                sender.sendMessage(MessageUtils.getAndFormatMsg(false, "playerNotFound", "&cPlayer not found!"));
                return false;
            }

            OfflinePlayer target = players.get(0);
            PlayerData playerData = storage.load(target.getUniqueId());
            
            sender.sendMessage(MessageUtils.getAndFormatMsg(true, "prestigeGet",
                    "&7Player &c%player% &7has &b%count% &7prestiges.",
                    new MessageUtils.Replaceable("%player%", target.getName()),
                    new MessageUtils.Replaceable("%count%", String.valueOf(playerData.getPrestigeCount()))
            ));
            return true;
        }

        if (action.equals("reset")) {
            if (args.length < 3) {
                sender.sendMessage(MessageUtils.getAndFormatMsg(false, "usage", "&cUsage: /lifestealz prestige reset <player>"));
                return false;
            }

            List<OfflinePlayer> players = CommandUtils.parseOfflinePlayer(args[2], false, true, plugin);
            if (players.isEmpty()) {
                sender.sendMessage(MessageUtils.getAndFormatMsg(false, "playerNotFound", "&cPlayer not found!"));
                return false;
            }

            OfflinePlayer target = players.get(0);
            PlayerData playerData = storage.load(target.getUniqueId());
            
            int oldPrestige = playerData.getPrestigeCount();
            playerData.setPrestigeCount(0);
            storage.save(playerData);
            
            // Remove from LuckPerms group
            updateLuckPermsGroup(target, 0, oldPrestige);
            
            sender.sendMessage(MessageUtils.getAndFormatMsg(true, "prestigeReset",
                    "&7Reset &c%player%&7's prestige from &b%old% &7to &b0&7.",
                    new MessageUtils.Replaceable("%player%", target.getName()),
                    new MessageUtils.Replaceable("%old%", String.valueOf(oldPrestige))
            ));
            return true;
        }

        if (action.equals("set")) {
            if (args.length < 4) {
                sender.sendMessage(MessageUtils.getAndFormatMsg(false, "usage", "&cUsage: /lifestealz prestige set <player> <amount>"));
                return false;
            }

            List<OfflinePlayer> players = CommandUtils.parseOfflinePlayer(args[2], false, true, plugin);
            if (players.isEmpty()) {
                sender.sendMessage(MessageUtils.getAndFormatMsg(false, "playerNotFound", "&cPlayer not found!"));
                return false;
            }

            int amount;
            try {
                amount = Integer.parseInt(args[3]);
                if (amount < 0) {
                    sender.sendMessage(MessageUtils.getAndFormatMsg(false, "invalidNumber", "&cPrestige amount must be 0 or greater!"));
                    return false;
                }
                
                int maxPrestiges = config.getInt("prestige.max-prestiges", 50);
                if (maxPrestiges > 0 && amount > maxPrestiges) {
                    sender.sendMessage(MessageUtils.getAndFormatMsg(false, "invalidNumber", 
                            "&cPrestige amount cannot exceed the maximum! &7(Max: &c%max%&7)",
                            new MessageUtils.Replaceable("%max%", String.valueOf(maxPrestiges))));
                    return false;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(MessageUtils.getAndFormatMsg(false, "invalidNumber", "&cInvalid number!"));
                return false;
            }

            OfflinePlayer target = players.get(0);
            PlayerData playerData = storage.load(target.getUniqueId());
            
            int oldPrestige = playerData.getPrestigeCount();
            playerData.setPrestigeCount(amount);
            storage.save(playerData);
            
            // Update LuckPerms group
            updateLuckPermsGroup(target, amount, oldPrestige);
            
            sender.sendMessage(MessageUtils.getAndFormatMsg(true, "prestigeSet",
                    "&7Set &c%player%&7's prestige from &b%old% &7to &b%new%&7.",
                    new MessageUtils.Replaceable("%player%", target.getName()),
                    new MessageUtils.Replaceable("%old%", String.valueOf(oldPrestige)),
                    new MessageUtils.Replaceable("%new%", String.valueOf(amount))
            ));
            return true;
        }

        return false;
    }

    /**
     * Update LuckPerms group for prestige
     */
    private void updateLuckPermsGroup(OfflinePlayer player, int newPrestige, int oldPrestige) {
        if (luckPerms == null) return;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                User user = luckPerms.getUserManager().loadUser(player.getUniqueId()).join();
                if (user == null) return;

                // Remove old prestige group
                if (oldPrestige > 0) {
                    String oldGroup = "prestige" + oldPrestige;
                    user.data().remove(Node.builder("group." + oldGroup).build());
                }

                // Add new prestige group
                if (newPrestige > 0) {
                    String newGroup = "prestige" + newPrestige;
                    user.data().add(Node.builder("group." + newGroup).build());
                }

                luckPerms.getUserManager().saveUser(user);
                
                plugin.getLogger().info("Updated LuckPerms group for " + player.getName() + 
                        ": prestige" + oldPrestige + " -> prestige" + newPrestige);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to update LuckPerms group for " + player.getName() + ": " + e.getMessage());
            }
        });
    }

    /**
     * Handle the prestige command for a player
     * @param player The player who wants to prestige
     * @return True if successful, false otherwise
     */
    private boolean handlePrestige(Player player) {
        PlayerData playerData = storage.load(player.getUniqueId());
        if (playerData == null) {
            player.sendMessage(MessageUtils.getAndFormatMsg(
                    false,
                    "playerDataLoadError",
                    "&cError loading your player data!"
            ));
            return false;
        }

        // Get configuration values
        int prestigeMinHearts = config.getInt("prestige.min-hearts", 20);
        int prestigeResetHearts = config.getInt("prestige.reset-hearts", 10);
        int maxPrestiges = config.getInt("prestige.max-prestiges", 50);

        // Get current hearts
        int currentHearts = (int) (playerData.getMaxHealth() / 2);

        // Check if player meets minimum heart requirement
        if (currentHearts < prestigeMinHearts) {
            int needed = prestigeMinHearts - currentHearts;
            player.sendMessage(MessageUtils.getAndFormatMsg(
                    false,
                    "prestigeNotEnoughHearts",
                    "&cYou need &6%needed% &cmore hearts to prestige! &7(Current: &c%current%&7/&c%required%&7)",
                    new MessageUtils.Replaceable("%needed%", String.valueOf(needed)),
                    new MessageUtils.Replaceable("%current%", String.valueOf(currentHearts)),
                    new MessageUtils.Replaceable("%required%", String.valueOf(prestigeMinHearts))
            ));
            return false;
        }

        // Check if player has reached max prestiges
        int currentPrestige = playerData.getPrestigeCount();
        int nextPrestige = currentPrestige + 1;
        
        if (maxPrestiges > 0 && nextPrestige > maxPrestiges) {
            player.sendMessage(MessageUtils.getAndFormatMsg(
                    false,
                    "prestigeMaxReached",
                    "&cYou have reached the maximum prestige level! &7(Current: &b%current%&7/&b%max%&7)",
                    new MessageUtils.Replaceable("%current%", String.valueOf(currentPrestige)),
                    new MessageUtils.Replaceable("%max%", String.valueOf(maxPrestiges))
            ));
            return false;
        }

        // Perform prestige
        int newPrestigeCount = nextPrestige;
        int oldPrestigeCount = playerData.getPrestigeCount();
        double newMaxHealth = prestigeResetHearts * 2.0; // Convert to health value

        playerData.setMaxHealth(newMaxHealth);
        playerData.setPrestigeCount(newPrestigeCount);

        // Update player's health
        AttributeInstance healthAttribute = player.getAttribute(Attribute.MAX_HEALTH);
        if (healthAttribute != null) {
            healthAttribute.setBaseValue(newMaxHealth);
        }
        player.setHealth(Math.min(player.getHealth(), newMaxHealth));

        // Save to storage
        storage.save(playerData);
        
        // Update LuckPerms group
        updateLuckPermsGroup(player, newPrestigeCount, oldPrestigeCount);

        // Send success message
        String prestigeLevel = "Prestige " + newPrestigeCount;
        player.sendMessage(MessageUtils.getAndFormatMsg(
                true,
                "prestigeSuccess",
                "&6&l✦ &ePrestige Successful! &6&l✦\n&7You are now &b%prestige_level%&7!\n&7Hearts reset to &c%reset_hearts%&4❤",
                new MessageUtils.Replaceable("%prestige_level%", prestigeLevel),
                new MessageUtils.Replaceable("%reset_hearts%", String.valueOf(prestigeResetHearts))
        ));

        // Broadcast prestige
        String broadcastMsg = config.getString("prestige.broadcast-message", 
                "&e%player% &7has reached &b%prestige_level%&7!");
        if (broadcastMsg != null && !broadcastMsg.isEmpty()) {
            plugin.getServer().broadcast(MessageUtils.formatMsg(broadcastMsg,
                    new MessageUtils.Replaceable("%player%", player.getName()),
                    new MessageUtils.Replaceable("%prestige_level%", prestigeLevel)
            ));
        }

        return true;
    }

    /**
     * Send prestige information to a player
     * @param player The player to send info to
     */
    private void sendPrestigeInfo(Player player) {
        PlayerData playerData = storage.load(player.getUniqueId());
        if (playerData == null) {
            player.sendMessage(MessageUtils.getAndFormatMsg(
                    false,
                    "playerDataLoadError",
                    "&cError loading your player data!"
            ));
            return;
        }

        int currentHearts = (int) (playerData.getMaxHealth() / 2);
        int prestigeCount = playerData.getPrestigeCount();
        int prestigeMinHearts = config.getInt("prestige.min-hearts", 20);

        player.sendMessage(MessageUtils.getAndFormatMsg(
                true,
                "prestigeInfo",
                "\n&6&l✦ &ePrestige Information &6&l✦\n" +
                "&7Current Prestige: &b%prestige%\n" +
                "&7Current Hearts: &c%hearts%&4❤ &7/ &c%required%&4❤\n" +
                "&7&oUse &b/lifestealz prestige confirm &7to prestige!\n",
                new MessageUtils.Replaceable("%prestige%", prestigeCount == 0 ? "None" : "Prestige " + prestigeCount),
                new MessageUtils.Replaceable("%hearts%", String.valueOf(currentHearts)),
                new MessageUtils.Replaceable("%required%", String.valueOf(prestigeMinHearts))
        ));
    }

    @Override
    public String getUsage() {
        return "/lifestealz prestige [confirm|info]";
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission("lifestealz.prestige") || sender.isOp();
    }
}
