package com.zetaplugins.lifestealz.afterlife;

import com.zetaplugins.lifestealz.LifeStealZ;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.ChunkGenerator.BiomeGrid;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import java.util.Random;

/**
 * Simple, deterministic chunk generator for the Afterlife world.
 * Modes:
 *  - default: delegate to server's normal generator
 *  - void: empty world with a small spawn platform
 *  - island: circular island centered at 0,0 at a fixed height
 */
public final class AfterlifeChunkGenerator extends ChunkGenerator {
    private final LifeStealZ plugin;
    private final String mode;

    public AfterlifeChunkGenerator(LifeStealZ plugin) {
        this.plugin = plugin;
        this.mode = plugin.getConfig().getString("afterlife.generator", "default").toLowerCase();
    }

    @Override
    public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome) {
        ChunkData data = createChunkData(world);

        switch (mode) {
            case "void":
                // Place a small stone platform in spawn chunk
                if (chunkX == 0 && chunkZ == 0) {
                    int y = plugin.getConfig().getInt("afterlife.void.platform-y", 64);
                    int size = Math.max(3, plugin.getConfig().getInt("afterlife.void.platform-size", 7));
                    int half = size / 2;
                    for (int x = 8 - half; x <= 8 + half; x++) {
                        for (int z = 8 - half; z <= 8 + half; z++) {
                            data.setBlock(x, y, z, Material.STONE);
                        }
                    }
                }
                return data;

            case "island":
                int radius = Math.max(8, plugin.getConfig().getInt("afterlife.island.radius", 64));
                int height = plugin.getConfig().getInt("afterlife.island.height", 64);
                int startX = chunkX << 4;
                int startZ = chunkZ << 4;
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        int wx = startX + x;
                        int wz = startZ + z;
                        double dist = Math.sqrt(wx * wx + wz * wz);
                        if (dist <= radius) {
                            // Simple island profile: grass -> dirt -> stone
                            data.setBlock(x, height, z, Material.GRASS_BLOCK);
                            data.setBlock(x, height - 1, z, Material.DIRT);
                            data.setBlock(x, height - 2, z, Material.DIRT);
                            for (int y = 0; y < height - 2; y++) {
                                data.setBlock(x, y, z, Material.STONE);
                            }
                        }
                    }
                }
                return data;

            default:
                // Default generation: leave data empty to let normal generator handle it
                return data;
        }
    }
}
