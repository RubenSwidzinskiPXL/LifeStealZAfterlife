package com.zetaplugins.lifestealz.util;

import com.zetaplugins.lifestealz.LifeStealZ;
import com.zetaplugins.lifestealz.storage.PlayerData;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerKickEvent;

/**
 * Utility class for handling player elimination.
 * Handles both ban-based elimination and afterlife system integration.
 */
public class EliminationUtil {
    
    /**
     * Eliminates a player (either by ban or afterlife).
     * This method should be used whenever a player reaches 0 hearts to ensure
     * consistent behavior across different elimination scenarios (death, withdraw, /kill, etc.)
     * 
     * @param plugin The LifeStealZ plugin instance
     * @param player The player to eliminate
     * @param playerData The player's data
     * @param announce Whether to announce the elimination
     */
    public static void eliminatePlayer(LifeStealZ plugin, Player player, PlayerData playerData, boolean announce) {
        // Set player to 0 hearts
        playerData.setMaxHealth(0.0);
        plugin.getStorage().save(playerData);
        
        // Execute elimination commands
        final var elimCommands = plugin.getConfig().getStringList("eliminationCommands");
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            for (String command : elimCommands) {
                plugin.getServer().dispatchCommand(
                    plugin.getServer().getConsoleSender(),
                    command.replace("&player&", player.getName())
                );
            }
        }, 1L);
        
        // Check if afterlife is enabled
        if (plugin.getConfig().getBoolean("afterlife.enabled", false)) {
            // Send to afterlife
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, 
                () -> plugin.getAfterlifeManager().sendPlayerToAfterlife(player), 
                1L
            );
            
            // Announce afterlife entry
            if (announce && plugin.getConfig().getBoolean("announceElimination", true)) {
                Component message = MessageUtils.getAndFormatMsg(
                    true,
                    "eliminationAnnouncementAfterlife",
                    "&c%player% &7has been sent to the Afterlife!",
                    new MessageUtils.Replaceable("%player%", player.getName())
                );
                Bukkit.broadcast(message);
            }
        } else {
            // Ban player (traditional elimination)
            if (!plugin.getConfig().getBoolean("disablePlayerBanOnElimination", false)) {
                Component kickMsg = MessageUtils.getAndFormatMsg(
                    false,
                    "eliminatedJoin",
                    "&cYou don't have any hearts left!"
                );
                player.kick(kickMsg, PlayerKickEvent.Cause.BANNED);
                
                // Announce elimination
                if (announce && plugin.getConfig().getBoolean("announceElimination", true)) {
                    Component message = MessageUtils.getAndFormatMsg(
                        true,
                        "eliminateionAnnouncementNature",
                        "&c%player% &7has been eliminated!",
                        new MessageUtils.Replaceable("%player%", player.getName())
                    );
                    Bukkit.broadcast(message);
                }
            }
        }
    }
}
