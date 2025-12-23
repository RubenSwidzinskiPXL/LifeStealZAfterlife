package com.zetaplugins.lifestealz.util.worldguard;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import com.zetaplugins.lifestealz.LifeStealZ;

public final class WorldGuardManager {
    private StateFlag HEARTLOSS_FLAG;

    public WorldGuardManager() {
        registerFlags();
    }

    private void registerFlags() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        StateFlag existing = (StateFlag) registry.get("heartloss");
        if (existing != null) {
            HEARTLOSS_FLAG = existing;
            return;
        }

        StateFlag heartLossFlag = new HeartLossFlag();
        try {
            registry.register(heartLossFlag);
            HEARTLOSS_FLAG = heartLossFlag;
        } catch (IllegalStateException e) {
            // Likely a hot-reload scenario where WG blocks new flag registration
            HEARTLOSS_FLAG = (StateFlag) registry.get("heartloss");
            if (HEARTLOSS_FLAG == null) {
                Bukkit.getLogger().warning("[LifeStealZ] Failed to register WorldGuard flag 'heartloss' (hot reload). Region heart-loss checks will be skipped.");
            }
        }
    }

    public StateFlag getHeartLossFlag() {
        return HEARTLOSS_FLAG;
    }

    public static boolean checkHeartLossFlag(Player player) {
        WorldGuardManager worldGuardManager = LifeStealZ.getInstance().getWorldGuardManager();

        if (worldGuardManager == null) return false;

        com.sk89q.worldguard.LocalPlayer localPlayer = com.sk89q.worldguard.bukkit.WorldGuardPlugin.inst().wrapPlayer(player);
        com.sk89q.worldedit.util.Location loc = com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(player.getLocation());
        com.sk89q.worldguard.protection.regions.RegionContainer container = com.sk89q.worldguard.WorldGuard.getInstance().getPlatform().getRegionContainer();
        com.sk89q.worldguard.protection.regions.RegionQuery query = container.createQuery();

        com.sk89q.worldguard.protection.ApplicableRegionSet set = query.getApplicableRegions(loc);

        return set.testState(localPlayer, worldGuardManager.getHeartLossFlag());
    }
}
