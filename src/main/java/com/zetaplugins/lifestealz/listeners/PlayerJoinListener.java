package com.zetaplugins.lifestealz.listeners;

import com.zetaplugins.zetacore.annotations.AutoRegisterListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import com.zetaplugins.lifestealz.LifeStealZ;
import com.zetaplugins.lifestealz.util.MessageUtils;
import com.zetaplugins.lifestealz.util.geysermc.GeyserManager;
import com.zetaplugins.lifestealz.util.geysermc.GeyserPlayerFile;
import com.zetaplugins.lifestealz.storage.PlayerData;
import com.zetaplugins.lifestealz.storage.Storage;

@AutoRegisterListener
public final class PlayerJoinListener implements Listener {

    private final LifeStealZ plugin;

    private final GeyserManager geyserManager;
    private final GeyserPlayerFile geyserPlayerFile;

    public PlayerJoinListener(LifeStealZ plugin) {
        this.plugin = plugin;
        this.geyserManager = plugin.getGeyserManager();
        this.geyserPlayerFile = plugin.getGeyserPlayerFile();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Storage storage = plugin.getStorage();

        if(plugin.hasGeyser()) {
            if(geyserManager.isBedrockPlayer(player)) {
                geyserPlayerFile.savePlayer(player.getUniqueId(), player.getName());
            }
        }

        PlayerData playerData = loadOrCreatePlayerData(player, storage, plugin.getConfig().getInt("startHearts", 10));
        LifeStealZ.setMaxHealth(player, playerData.getMaxHealth());
        
        // Handle afterlife on login
        if (plugin.getConfig().getBoolean("afterlife.enabled", false) && playerData.isAfterlife()) {
            // Check if they should still be in afterlife
            if (System.currentTimeMillis() >= playerData.getAfterlifeReleaseTime()) {
                // Time expired, release them
                plugin.getAfterlifeManager().releaseFromAfterlife(player);
            } else {
                // Teleport back to afterlife
                org.bukkit.Location afterlifeSpawn = plugin.getAfterlifeManager().getWorldManager().getSpawnLocation();
                if (afterlifeSpawn != null) {
                    player.teleport(afterlifeSpawn);
                    long remainingSeconds = (playerData.getAfterlifeReleaseTime() - System.currentTimeMillis()) / 1000;
                    player.sendMessage("§7You are still in the Afterlife. Remaining time: §e" + formatTime(remainingSeconds));
                }
            }
        }

        notifyOpAboutUpdate(player);
    }
    
    private String formatTime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        
        if (hours > 0) {
            return String.format("%dh %dm", hours, minutes);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, secs);
        } else {
            return String.format("%ds", secs);
        }
    }

    private PlayerData loadOrCreatePlayerData(Player player, Storage storage, int startHearts) {
        PlayerData playerData = plugin.getStorage().load(player.getUniqueId());
        if (playerData == null) {
            playerData = new PlayerData(player.getName(), player.getUniqueId());
            playerData.setFirstJoin(System.currentTimeMillis());
            playerData.setMaxHealth(startHearts * 2.0);
            storage.save(playerData);
            plugin.getGracePeriodManager().startGracePeriod(player);
            plugin.getOfflinePlayerCache().addItem(player.getName());
        }
        return playerData;
    }

    private void notifyOpAboutUpdate(Player player) {
        if (player.isOp() && plugin.getConfig().getBoolean("checkForUpdates") && plugin.getVersionChecker().isNewVersionAvailable()) {
            player.sendMessage(MessageUtils.getAndFormatMsg(true, "newVersionAvailable", "&7A new version of LifeStealZ is available!\\n&c<click:OPEN_URL:https://modrinth.com/plugin/lifestealz/versions>https://modrinth.com/plugin/lifestealz/versions</click>"));
        }
    }
}