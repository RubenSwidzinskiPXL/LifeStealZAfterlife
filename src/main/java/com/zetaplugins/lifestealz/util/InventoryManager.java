package com.zetaplugins.lifestealz.util;

import com.zetaplugins.lifestealz.LifeStealZ;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Simple per-player, per-profile inventory storage using YAML.
 * Profiles: "main" and "afterlife".
 */
public final class InventoryManager {
    private final LifeStealZ plugin;
    private final File file;
    private FileConfiguration cfg;

    public InventoryManager(LifeStealZ plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "inventories.yml");
        if (!file.exists()) {
            try { file.getParentFile().mkdirs(); file.createNewFile(); } catch (IOException ignored) {}
        }
        this.cfg = YamlConfiguration.loadConfiguration(file);
    }

    public void saveProfile(Player player, String profile) {
        UUID uuid = player.getUniqueId();
        String base = uuid.toString() + "." + profile + ".";

        cfg.set(base + "contents", player.getInventory().getContents());
        cfg.set(base + "armor", player.getInventory().getArmorContents());
        cfg.set(base + "offhand", player.getInventory().getItemInOffHand());
        saveQuietly();
    }

    public boolean loadProfile(Player player, String profile) {
        UUID uuid = player.getUniqueId();
        String base = uuid.toString() + "." + profile + ".";

        ItemStack[] contents = (ItemStack[]) cfg.get(base + "contents");
        ItemStack[] armor = (ItemStack[]) cfg.get(base + "armor");
        ItemStack offhand = cfg.getItemStack(base + "offhand");

        if (contents == null && armor == null && offhand == null) {
            return false;
        }

        if (contents != null) player.getInventory().setContents(contents);
        if (armor != null) player.getInventory().setArmorContents(armor);
        if (offhand != null) player.getInventory().setItemInOffHand(offhand);
        return true;
    }

    public void clearStoredProfile(UUID uuid, String profile) {
        String base = uuid.toString() + "." + profile;
        cfg.set(base, null);
        saveQuietly();
    }

    private void saveQuietly() {
        try { cfg.save(file); } catch (IOException ignored) {}
    }
}
