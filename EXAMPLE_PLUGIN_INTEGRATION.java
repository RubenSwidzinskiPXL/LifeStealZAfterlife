package com.example.yourplugin.listeners;

import com.zetaplugins.lifestealz.util.PrestigePermissionManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.entity.EntityDeathEvent;

/**
 * Example listener showing how to integrate LifeStealZ prestige multipliers
 * into your plugin.
 * 
 * This example shows:
 * 1. Reading the multiplier
 * 2. Applying it to rewards
 * 3. Displaying it to players
 */
public class ExamplePrestigeMultiplierListener implements Listener {
    
    /**
     * Example 1: Welcome message with multiplier
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        
        // Get the multiplier
        double multiplier = PrestigePermissionManager.getPrestigeMultiplier(player);
        String formatted = PrestigePermissionManager.getFormattedMultiplier(player);
        
        // Show to player
        if (multiplier > 1.0) {
            player.sendMessage("§6✦ §eWelcome back! Your prestige multiplier: §b" + formatted);
        }
    }
    
    /**
     * Example 2: Mob kill rewards with multiplier
     */
    @EventHandler
    public void onMobKill(EntityDeathEvent e) {
        if (e.getEntity().getKiller() instanceof Player) {
            Player killer = e.getEntity().getKiller();
            
            // Base reward
            int baseGems = 50;
            
            // Apply multiplier
            double finalGems = PrestigePermissionManager.applyMultiplier(killer, baseGems);
            
            // Give reward (pseudo-code - use your plugin's API)
            // yourPlugin.getGemsAPI().giveGems(killer, finalGems);
            
            // Notify player
            String multiplierText = PrestigePermissionManager.getFormattedMultiplier(killer);
            killer.sendMessage("§e+§6" + (int)finalGems + " gems §7(" + 
                              baseGems + " × " + multiplierText + ")");
        }
    }
    
    /**
     * Example 3: Manual calculation
     */
    public void customReward(Player player, int baseReward) {
        // Get multiplier
        double multiplier = PrestigePermissionManager.getPrestigeMultiplier(player);
        
        // Calculate
        double finalReward = baseReward * multiplier;
        
        // Apply
        // yourRewardSystem.give(player, finalReward);
        
        // Log
        System.out.println("Gave " + player.getName() + " " + finalReward + 
                          " (base: " + baseReward + ", multiplier: " + multiplier + ")");
    }
    
    /**
     * Example 4: Conditional rewards based on prestige
     */
    public void prestigeBonus(Player player) {
        double multiplier = PrestigePermissionManager.getPrestigeMultiplier(player);
        
        // Different rewards based on multiplier
        if (multiplier >= 2.0) {
            player.sendMessage("§6✦ §eYou've unlocked §6LEGENDARY §ebonuses!");
            // Give legendary rewards
        } else if (multiplier >= 1.5) {
            player.sendMessage("§6✦ §eYou've unlocked §5EPIC §ebonuses!");
            // Give epic rewards
        } else if (multiplier >= 1.25) {
            player.sendMessage("§6✦ §eYou've unlocked §bRARE §ebonuses!");
            // Give rare rewards
        } else if (multiplier > 1.0) {
            player.sendMessage("§6✦ §eYou've unlocked §aUNCOMMON §ebonuses!");
            // Give uncommon rewards
        } else {
            player.sendMessage("§7Prestige to unlock reward multipliers!");
        }
    }
    
    /**
     * Example 5: Display multiplier in GUI/scoreboard
     */
    public String getMultiplierDisplay(Player player) {
        String formatted = PrestigePermissionManager.getFormattedMultiplier(player);
        double multiplier = PrestigePermissionManager.getPrestigeMultiplier(player);
        
        if (multiplier > 1.0) {
            return "§6Multiplier: §e" + formatted;
        } else {
            return "§7No prestige multiplier";
        }
    }
}
