package com.zetaplugins.lifestealz.listeners;

import com.zetaplugins.lifestealz.LifeStealZ;
import com.zetaplugins.lifestealz.storage.PlayerData;
import com.zetaplugins.zetacore.annotations.AutoRegisterListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Prevents heart gain/loss and combat while in the afterlife.
 */
@AutoRegisterListener
public final class AfterlifeCombatListener implements Listener {
    private final LifeStealZ plugin;
    
    public AfterlifeCombatListener(LifeStealZ plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!plugin.getConfig().getBoolean("afterlife.enabled", false)) {
            return;
        }
        
        // Check if PvP is disabled in afterlife
        if (!plugin.getConfig().getBoolean("afterlife.allow-pvp", false)) {
            if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
                Player victim = (Player) event.getEntity();
                Player attacker = (Player) event.getDamager();
                
                // Check if either player is in afterlife
                PlayerData victimData = plugin.getStorage().load(victim.getUniqueId());
                PlayerData attackerData = plugin.getStorage().load(attacker.getUniqueId());
                
                if ((victimData != null && victimData.isAfterlife()) || 
                    (attackerData != null && attackerData.isAfterlife())) {
                    event.setCancelled(true);
                    attacker.sendMessage("§7Combat is disabled in the Afterlife.");
                }
            }
        }
    }
}
