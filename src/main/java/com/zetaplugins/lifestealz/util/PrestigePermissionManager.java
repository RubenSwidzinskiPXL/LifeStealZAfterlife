package com.zetaplugins.lifestealz.util;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * Manages prestige multiplier permissions for cross-plugin integration.
 * 
 * <p>This system uses LuckPerms permissions to store prestige multipliers,
 * allowing any plugin to read the multiplier without tight coupling.</p>
 * 
 * <p><b>Permission Format:</b> {@code lifestealz.prestige.multiplier.XXX}<br>
 * Where XXX is the multiplier * 100 (e.g., 105 = 1.05x, 200 = 2.00x)</p>
 * 
 * <p><b>Examples:</b></p>
 * <ul>
 *   <li>Prestige 1 (1.05x) → {@code lifestealz.prestige.multiplier.105}</li>
 *   <li>Prestige 2 (1.10x) → {@code lifestealz.prestige.multiplier.110}</li>
 *   <li>Prestige 5 (1.25x) → {@code lifestealz.prestige.multiplier.125}</li>
 *   <li>Prestige 10 (1.50x) → {@code lifestealz.prestige.multiplier.150}</li>
 *   <li>Prestige 20 (2.00x) → {@code lifestealz.prestige.multiplier.200}</li>
 * </ul>
 * 
 * @author LifeStealZ
 * @version 1.0
 * @see <a href="https://github.com/ZetaPlugins/LifeStealZ">LifeStealZ GitHub</a>
 */
public class PrestigePermissionManager {
    
    private static final String PERMISSION_PREFIX = "lifestealz.prestige.multiplier.";
    
    private final LuckPerms luckPerms;
    private final Logger logger;
    private final FileConfiguration config;
    
    /**
     * Creates a new PrestigePermissionManager.
     * 
     * @param luckPerms The LuckPerms API instance
     * @param logger Logger for debug messages
     * @param config Configuration file for reading multiplier settings
     */
    public PrestigePermissionManager(LuckPerms luckPerms, Logger logger, FileConfiguration config) {
        this.luckPerms = luckPerms;
        this.logger = logger;
        this.config = config;
    }
    
    /**
     * Grants prestige multiplier permission to a player.
     * Automatically removes old multiplier permissions and adds the new one.
     * 
     * <p>This operation is asynchronous to avoid blocking the main thread.</p>
     * 
     * @param player The player to grant the permission to
     * @param prestigeLevel The player's new prestige level (0 or greater)
     * @return CompletableFuture that completes when the operation is done
     */
    public CompletableFuture<Boolean> setPrestigeMultiplier(OfflinePlayer player, int prestigeLevel) {
        if (prestigeLevel < 0) {
            logger.warning("Attempted to set negative prestige level for " + player.getName());
            return CompletableFuture.completedFuture(false);
        }
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                User user = luckPerms.getUserManager().loadUser(player.getUniqueId()).join();
                if (user == null) {
                    logger.warning("Failed to load LuckPerms user for " + player.getName());
                    return false;
                }
                
                // Remove all old prestige multiplier permissions
                user.data().toCollection().stream()
                    .filter(node -> node.getKey().startsWith(PERMISSION_PREFIX))
                    .forEach(node -> user.data().remove(node));
                
                // Add new multiplier permission if prestige > 0
                if (prestigeLevel > 0) {
                    String multiplierPermission = calculateMultiplierPermission(prestigeLevel);
                    Node permNode = Node.builder(multiplierPermission).build();
                    user.data().add(permNode);
                    
                    logger.info("Granted " + player.getName() + " permission: " + multiplierPermission);
                }
                
                // Save to database
                luckPerms.getUserManager().saveUser(user);
                return true;
                
            } catch (Exception e) {
                logger.severe("Failed to set prestige multiplier for " + player.getName() + ": " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        });
    }
    
    /**
     * Removes prestige multiplier permission from a player.
     * Useful for prestige reset scenarios.
     * 
     * @param player The player to remove the permission from
     * @return CompletableFuture that completes when the operation is done
     */
    public CompletableFuture<Boolean> removePrestigeMultiplier(OfflinePlayer player) {
        return setPrestigeMultiplier(player, 0);
    }
    
    /**
     * Calculates the multiplier permission string from a prestige level.
     * Reads formula from config: prestige.multiplier.base and prestige.multiplier.increment
     * 
     * <p><b>Default Formula:</b> {@code base + (level * increment)}<br>
     * Where base = 1.0 and increment = 0.05</p>
     * 
     * @param prestigeLevel The prestige level
     * @return The permission string (e.g., "lifestealz.prestige.multiplier.105")
     */
    private String calculateMultiplierPermission(int prestigeLevel) {
        double baseMultiplier = config.getDouble("prestige.multiplier.base", 1.0);
        double increment = config.getDouble("prestige.multiplier.increment", 0.05);
        
        double multiplier = baseMultiplier + (prestigeLevel * increment);
        int multiplierValue = (int) Math.round(multiplier * 100);
        return PERMISSION_PREFIX + multiplierValue;
    }
    
    /**
     * Gets the prestige multiplier for a player by reading their permissions.
     * This method can be used by any plugin that has access to the Bukkit API.
     * 
     * <p><b>Note:</b> This is a static utility method that doesn't require
     * a PrestigePermissionManager instance. It's designed to be used by
     * external plugins.</p>
     * 
     * <p>If multiple multiplier permissions exist (e.g., due to manual permission grants),
     * this method returns the <b>highest</b> multiplier value.</p>
     * 
     * @param player The player to check
     * @return The multiplier value (e.g., 1.05, 1.50, 2.00), or 1.0 if no multiplier found
     */
    public static double getPrestigeMultiplier(Player player) {
        return player.getEffectivePermissions().stream()
            .map(perm -> perm.getPermission())
            .filter(perm -> perm.startsWith(PERMISSION_PREFIX))
            .mapToDouble(perm -> {
                try {
                    String multiplierStr = perm.replace(PERMISSION_PREFIX, "");
                    // Convert "105" → 1.05, "150" → 1.50, "200" → 2.00
                    return Double.parseDouble(multiplierStr) / 100.0;
                } catch (NumberFormatException e) {
                    return 1.0; // Default if parsing fails
                }
            })
            .max() // Get the highest multiplier if multiple exist
            .orElse(1.0); // Default: no multiplier
    }
    
    /**
     * Gets a formatted multiplier string for display purposes.
     * 
     * @param player The player to check
     * @return Formatted string (e.g., "1.05x", "2.00x", "1.00x")
     */
    public static String getFormattedMultiplier(Player player) {
        double multiplier = getPrestigeMultiplier(player);
        return String.format("%.2fx", multiplier);
    }
    
    /**
     * Applies the prestige multiplier to a base value.
     * Convenience method for calculations.
     * 
     * @param player The player with the multiplier
     * @param baseValue The base value to multiply
     * @return The multiplied value
     */
    public static double applyMultiplier(Player player, double baseValue) {
        return baseValue * getPrestigeMultiplier(player);
    }
    
    /**
     * Gets the permission string that would be granted for a given prestige level.
     * Useful for administrative commands or debugging.
     * 
     * <p><b>Note:</b> This method uses default config values (1.0 base, 0.05 increment)
     * for static access. For actual permission grants, the config-based instance method is used.</p>
     * 
     * @param prestigeLevel The prestige level to check
     * @return The permission string
     */
    public static String getPermissionForLevel(int prestigeLevel) {
        if (prestigeLevel <= 0) {
            return "none";
        }
        // Use default values for static method (actual grants use config)
        double multiplier = 1.0 + (prestigeLevel * 0.05);
        int multiplierValue = (int) Math.round(multiplier * 100);
        return PERMISSION_PREFIX + multiplierValue;
    }
}
