# SimpleGems Integration with LifeStealZ - Critical Fix

## ⚠️ Misconception in SimpleGems Documentation

The SimpleGems integration documentation contains a **critical error** that will prevent it from working seamlessly with LifeStealZ.

---

## The Problem

### What SimpleGems Documentation Claims

The SimpleGems documentation states:
```
Uses PlaceholderAPI to read current prestige level: %lif steal_prestige_count%
```

❌ **This placeholder does NOT exist in LifeStealZ**

### What LifeStealZ Actually Provides

LifeStealZ provides these prestige-related PlaceholderAPI placeholders:

| Placeholder | Returns | Example |
|------------|---------|---------|
| `%lifestealz_prestige_count%` | Numeric prestige level | `3` |
| `%lifestealz_prestige%` | "None" or level number | `Prestige 3` |

---

## Why This Breaks Integration

1. **Wrong Placeholder Name**: `lif steal_prestige_count` ≠ `lifestealz_prestige_count`
2. **PlaceholderAPI Returns Error**: When SimpleGems requests the wrong placeholder, PlaceholderAPI returns `InvalidPlaceholder`
3. **Silent Failure**: SimpleGems won't detect prestige level-ups
4. **No Gem Rewards**: Players won't receive gem bonuses for prestiging

---

## The Correct Integration

### 1. Update PrestigeListener.java

Replace the prestige detection code in `PrestigeListener.java`:

```java
// ❌ WRONG (from original documentation)
String prestigeValue = player.getPlayer().getScoreboardTags();

// ✅ CORRECT (for LifeStealZ compatibility)
String prestigeValue = PlaceholderAPI.setPlaceholders(player, "%lifestealz_prestige_count%");
```

### 2. Which Placeholder to Use

**For SimpleGems**, use `prestige_count` because you need numeric parsing:

```java
// In PrestigeListener - parse the placeholder
String newPrestigeValue = PlaceholderAPI.setPlaceholders(player, "%lifestealz_prestige_count%");
int newPrestigeLevel = Integer.parseInt(newPrestigeValue);

// Compare with stored prestige level
int oldPrestigeLevel = playerData.getPrestigeLevel();

if (newPrestigeLevel > oldPrestigeLevel) {
    // Award gems
    int gemsToAward = config.PRESTIGE_GEM_REWARD_BASE * newPrestigeLevel;
    playerData.setPrestigeLevel(newPrestigeLevel);
    playerData.setGemMultiplier(1.0 + (newPrestigeLevel * config.PRESTIGE_MULTIPLIER_PER_LEVEL));
    // ... rest of reward logic
}
```

### 3. Update PlaytimeListener.java

Similarly, ensure correct placeholder for playtime:

```java
// Use the correct prestige placeholder when applying multiplier
String prestigeCountStr = PlaceholderAPI.setPlaceholders(player, "%lifestealz_prestige_count%");
int prestigeLevel = Integer.parseInt(prestigeCountStr.isEmpty() ? "0" : prestigeCountStr);
double multiplier = 1.0 + (prestigeLevel * PRESTIGE_MULTIPLIER_PER_LEVEL);
```

---

## Testing the Integration

### Test 1: Verify Placeholders Work
```bash
# In-game, check LifeStealZ placeholders
/papi parse me %lifestealz_prestige_count%
# Should show: 0 (or your current prestige level)

/papi parse me %lifestealz_prestige%
# Should show: None (or your prestige number)
```

### Test 2: Test Prestige Detection
1. Set player to 20+ hearts: `/lifestealz hearts set <player> 20`
2. Player prestiges: `/lifestealz prestige confirm`
3. Check SimpleGems gems awarded
4. Verify placeholder shows new prestige level

### Test 3: Verify Multiplier Application
1. Player joins server after prestiging
2. Check playtime rewards are multiplied correctly
3. Formula should be: `(dailyMinutes / 60) * reward_per_hour * gem_multiplier`

---

## Complete Corrected Implementation

### PrestigeListener.java (Corrected)

```java
package me.refracdevelopment.simplegems.listeners;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import me.refracdevelopment.simplegems.SimpleGems;
import me.refracdevelopment.simplegems.player.data.ProfileData;

public class PrestigeListener implements Listener {
    private final SimpleGems plugin;

    public PrestigeListener(SimpleGems plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPrestige(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage().toLowerCase();
        
        // Detect LifeStealZ prestige commands
        if (!message.contains("prestige") && !message.contains("lif steal prestige")) {
            return;
        }

        Player player = event.getPlayer();
        
        // Give command time to execute
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            // ✅ CORRECT: Use the actual LifeStealZ placeholder
            String prestigeValue = PlaceholderAPI.setPlaceholders(player, "%lifestealz_prestige_count%");
            
            if (prestigeValue == null || prestigeValue.isEmpty() || prestigeValue.equals("InvalidPlaceholder")) {
                return; // PlaceholderAPI not working or LifeStealZ not installed
            }

            try {
                int newPrestigeLevel = Integer.parseInt(prestigeValue);
                ProfileData playerData = plugin.getProfileManager().getProfileData(player.getUniqueId());
                int oldPrestigeLevel = playerData.getPrestigeLevel();

                if (newPrestigeLevel > oldPrestigeLevel) {
                    // Calculate gem reward
                    int baseReward = plugin.getConfig().getInt("prestige.gem-reward-base", 50);
                    int gemsToAward = baseReward * newPrestigeLevel;
                    
                    // Update multiplier
                    double multiplierPerLevel = plugin.getConfig().getDouble("prestige.multiplier-per-level", 0.05);
                    double newMultiplier = 1.0 + (newPrestigeLevel * multiplierPerLevel);

                    // Apply rewards
                    playerData.setPrestigeLevel(newPrestigeLevel);
                    playerData.setGemMultiplier(newMultiplier);
                    playerData.addGems(gemsToAward);
                    plugin.getProfileManager().saveProfileData(playerData);

                    // Notify player
                    String message_text = String.format(
                        "§6✦ §aPrestige %d! +§e%d gems §a+ x%.2f multiplier!",
                        newPrestigeLevel, gemsToAward, newMultiplier
                    );
                    player.sendMessage(message_text);
                }
            } catch (NumberFormatException e) {
                plugin.getLogger().warning("Failed to parse prestige level for " + player.getName());
            }
        }, 5L); // 5 tick delay to allow command to execute
    }
}
```

### PlaytimeListener.java (Corrected)

```java
package me.refracdevelopment.simplegems.listeners;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import me.refracdevelopment.simplegems.SimpleGems;
import me.refracdevelopment.simplegems.player.data.ProfileData;

public class PlaytimeListener implements Listener {
    private final SimpleGems plugin;

    public PlaytimeListener(SimpleGems plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Get playtime from playtime plugin
        String playtimeValue = PlaceholderAPI.setPlaceholders(player, "%yourplaytime_daily%");
        
        if (playtimeValue == null || playtimeValue.isEmpty() || playtimeValue.equals("InvalidPlaceholder")) {
            return; // Playtime plugin not installed
        }

        // Get prestige level from LifeStealZ
        String prestigeValue = PlaceholderAPI.setPlaceholders(player, "%lifestealz_prestige_count%");
        int prestigeLevel = 0;
        
        if (prestigeValue != null && !prestigeValue.isEmpty() && !prestigeValue.equals("InvalidPlaceholder")) {
            try {
                prestigeLevel = Integer.parseInt(prestigeValue);
            } catch (NumberFormatException ignored) {}
        }

        // Parse playtime
        int dailyMinutes = parsePlaytime(playtimeValue);
        
        if (dailyMinutes <= 0) return;

        ProfileData playerData = plugin.getProfileManager().getProfileData(player.getUniqueId());
        
        // Calculate gems: (dailyMinutes / 60) * reward_per_hour * gem_multiplier
        int rewardPerHour = plugin.getConfig().getInt("rewards.playtime-per-hour", 5);
        int dailyCap = plugin.getConfig().getInt("rewards.daily-cap", 50);
        
        // ✅ Use prestige multiplier from LifeStealZ
        double multiplierPerLevel = plugin.getConfig().getDouble("prestige.multiplier-per-level", 0.05);
        double gemMultiplier = 1.0 + (prestigeLevel * multiplierPerLevel);
        
        int gemsToAward = (int) ((dailyMinutes / 60.0) * rewardPerHour * gemMultiplier);
        gemsToAward = Math.min(gemsToAward, dailyCap);

        if (gemsToAward > 0) {
            playerData.addGems(gemsToAward);
            plugin.getProfileManager().saveProfileData(playerData);
            
            String message = String.format(
                "§e✦ +%d gems for playtime (x%.2f)",
                gemsToAward, gemMultiplier
            );
            player.sendMessage(message);
        }
    }

    private int parsePlaytime(String playtimeStr) {
        try {
            // Handle "HH:MM" format
            if (playtimeStr.contains(":")) {
                String[] parts = playtimeStr.split(":");
                int hours = Integer.parseInt(parts[0]);
                int minutes = Integer.parseInt(parts[1]);
                return hours * 60 + minutes;
            }
            // Handle plain number (minutes)
            return Integer.parseInt(playtimeStr);
        } catch (Exception e) {
            return 0;
        }
    }
}
```

---

## Key Differences from SimpleGems Documentation

| Aspect | Documentation | Correct Implementation |
|--------|---------------|----------------------|
| **Placeholder** | `%lif steal_prestige_count%` | `%lifestealz_prestige_count%` |
| **Detection Method** | Scoreboard tags | PlaceholderAPI |
| **Prestige Check** | Comparison with stored value | Numeric integer comparison |
| **Multiplier Source** | SimpleGems config | LifeStealZ prestige system |
| **Integration Style** | Event-based (incorrect plugin) | Command post-execution |

---

## Deployment Checklist

- [ ] Update SimpleGems `PrestigeListener.java` with correct placeholder
- [ ] Update SimpleGems `PlaytimeListener.java` with correct placeholder
- [ ] Verify PlaceholderAPI is installed on test server
- [ ] Verify LifeStealZ is installed on test server
- [ ] Test prestige detection with `/papi parse`
- [ ] Test gem rewards on prestige
- [ ] Test playtime rewards with multiplier
- [ ] Recompile SimpleGems JAR
- [ ] Deploy to production

---

## Support

For issues with this integration:

1. **Verify PlaceholderAPI**: `/papi version`
2. **Test Placeholder**: `/papi parse me %lifestealz_prestige_count%`
3. **Check Logs**: Look for "InvalidPlaceholder" errors
4. **Ensure Plugins Loaded**: `/plugins` should show both LifeStealZ and SimpleGems
5. **Verify LifeStealZ Works**: `/lifestealz prestige info` should work

---

## References

- LifeStealZ Prestige Placeholders: `%lifestealz_prestige_count%`, `%lifestealz_prestige%`
- LifeStealZ Repository: [ZetaPlugins/LifeStealZ](https://github.com/ZetaPlugins/LifeStealZ)
- PlaceholderAPI Documentation: [PlaceholderAPI Wiki](https://github.com/PlaceholderAPI/PlaceholderAPI/wiki)
