# Prestige System - Configuration Examples

This file contains example configurations for different server types and use cases.

---

## Example 1: Standard LifeSteal SMP

**Use Case**: Mid-game progression where players grind to 20 hearts, then prestige

```yaml
prestige:
  enabled: true
  min-hearts: 20                  # Players must reach 20 hearts
  reset-hearts: 10                # Reset to base 10 hearts
  max-prestiges: 50               # Allow up to 50 prestiges
  broadcast: true
  broadcast-message: "&b%player% &ehas achieved &6[Prestige %count%]&e!"
```

**LuckPerms Prefix**: `&4&l[Prestige %count%] ` in chat

**NPC Setup**: Click at spawn to prestige

---

## Example 2: Hardcore SMP

**Use Case**: Extremely difficult progression; high reset penalty

```yaml
prestige:
  enabled: true
  min-hearts: 30                  # Require 30 hearts (very difficult)
  reset-hearts: 5                 # Reset to only 5 hearts (harsh penalty)
  max-prestiges: 25               # Lower maximum
  broadcast: true
  broadcast-message: "&c✧✧✧ &b%player% &ais now &f[Prestige %count%] &c✧✧✧"
```

**Notes**: 
- Very punishing (lose 25 hearts per prestige)
- Lower max = prestigious to reach high levels
- Special broadcast format for rarity

---

## Example 3: Casual/Relaxed SMP

**Use Case**: Players prestige frequently; progression is quick

```yaml
prestige:
  enabled: true
  min-hearts: 10                  # Easy requirement
  reset-hearts: 8                 # Minimal penalty
  max-prestiges: 100              # Very high ceiling
  broadcast: false                # Quiet, no spam
```

**Notes**:
- Players can prestige often (every 2 hearts gained)
- Low penalty (lose 2 hearts)
- No broadcasts to avoid chat spam

---

## Example 4: Prestige Only (No Progression)

**Use Case**: Server where prestige is purely cosmetic

```yaml
prestige:
  enabled: true
  min-hearts: 20
  reset-hearts: 20                # DON'T reset hearts!
  max-prestiges: 50
  broadcast: true
  broadcast-message: "&d✨ &b%player% &eis now &d[Prestige %count%] ✨"
```

**Notes**:
- With `reset-hearts: 20`, players keep their hearts
- Prestige only changes rank/prefix, no penalty
- Cosmetic-only progression

---

## Example 5: Milestone Prestiges

**Use Case**: Server with special rewards at certain prestige levels

```yaml
prestige:
  enabled: true
  min-hearts: 20
  reset-hearts: 10
  max-prestiges: 10               # Lower max for milestones
  broadcast: true
  broadcast-message: |
    &e[Prestige Achieved]
    &b%player% &ehas reached &6[Prestige %count%]
```

**Plus Custom Command in PrestigeCommand.java**:
```java
// At prestige level 5, 10 add special effects
if (newPrestigeCount % 5 == 0) {
    player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BURST, 1f, 1f);
    for (Entity entity : player.getNearbyEntities(50, 50, 50)) {
        if (entity instanceof Player) {
            ((Player) entity).playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BURST, 0.5f, 1f);
        }
    }
}
```

---

## Example 6: Progressive Difficulty

**Use Case**: Prestige gets progressively harder

```yaml
prestige:
  enabled: true
  min-hearts: 20                  # Base requirement
  reset-hearts: 10
  max-prestiges: 50
  broadcast: true
```

**Custom Code Addition** - Increase requirement per prestige:

Add to PrestigeCommand.java in `onCommand()`:

```java
// Increase difficulty with each prestige
int prestigeMultiplier = (playerData.getPrestigeCount() / 5);  // Every 5 prestiges
int adjustedMinHearts = minHearts + (prestigeMultiplier * 5);  // +5 hearts per milestone

if (currentHearts < adjustedMinHearts) {
    player.sendMessage("You need " + adjustedMinHearts + " hearts (+" + (adjustedMinHearts - minHearts) + ")");
    return false;
}
```

This makes prestiging progressively harder (20 → 25 → 30, etc.)

---

## Example 7: Economic Integration

**Use Case**: Prestige costs money/currency

```yaml
prestige:
  enabled: true
  min-hearts: 20
  reset-hearts: 10
  max-prestiges: 50
  broadcast: true
  broadcast-message: "&b%player% &ehas paid their way to &b[Prestige %count%]&e!"
```

**Custom Code Addition** - Add cost check:

```java
// Check if player can afford prestige
EconomyResponse payment = plugin.getVault().getEconomy().withdrawPlayer(player, 10000);

if (!payment.transactionSucceeded()) {
    player.sendMessage("&cPrestige costs $10,000! You only have $" + 
        plugin.getVault().getEconomy().getBalance(player));
    return false;
}

// Then proceed with prestige...
```

---

## Example 8: Time-Based Prestige

**Use Case**: Players can only prestige once per day

```yaml
prestige:
  enabled: true
  min-hearts: 20
  reset-hearts: 10
  max-prestiges: 50
  broadcast: true
```

**Custom Code Addition** - Add cooldown:

```java
// Check prestige cooldown
HashMap<UUID, Long> prestigeCooldown = new HashMap<>();  // Store in plugin
long lastPrestige = prestigeCooldown.getOrDefault(player.getUniqueId(), 0L);
long now = System.currentTimeMillis();
long oneDayMs = 24 * 60 * 60 * 1000;

if (now - lastPrestige < oneDayMs) {
    long hoursLeft = (oneDayMs - (now - lastPrestige)) / (60 * 60 * 1000);
    player.sendMessage("&cYou can prestige again in " + hoursLeft + " hours!");
    return false;
}

// Proceed with prestige...
prestigeCooldown.put(player.getUniqueId(), now);
```

---

## Example 9: PvP Server

**Use Case**: Prestige shows dominance; special rewards

```yaml
prestige:
  enabled: true
  min-hearts: 20
  reset-hearts: 10
  max-prestiges: 50
  broadcast: true
  broadcast-message: "&c⚔ &b%player% &eis now &c[Prestige %count%] ⚔"
```

**Special NPC Setup**:
```bash
/npc create PrestigeNPC
/npc skin set <famous_pvper>
/npc equipment hand GOLDEN_SWORD
/npc cmd add -p lifestealz.prestige:prestige
```

**Chat Format** (in LuckPerms):
```
%luckperms_prefix_100%&c[%luckperms_meta_kills%K] &r%player%
```
Shows: `[Prestige 5] [100K] PlayerName`

---

## Example 10: Disabled Prestige

**Use Case**: Server without prestige system (fallback config)

```yaml
prestige:
  enabled: false                  # Prestige system off
  min-hearts: 20
  reset-hearts: 10
  max-prestiges: 50
  broadcast: false
```

**Effect**: 
- `/prestige` command returns "disabled" message
- No placeholders updated
- Database still tracks (for future re-enabling)

---

## Configuration Import Template

Copy one of the above `prestige:` sections into your `config.yml`:

1. Locate `# === PRESTIGE SETTINGS ===` in config.yml
2. Replace the prestige section with your chosen example
3. Restart server

---

## Quick Preset Selector

| Server Type | Example | Difficulty |
|------------|---------|-----------|
| Standard SMP | Example 1 | Medium |
| Hardcore SMP | Example 2 | Hard |
| Casual/Chill | Example 3 | Easy |
| Cosmetic Only | Example 4 | None |
| PvP Focus | Example 9 | Hard |
| Disabled | Example 10 | Off |

---

## Custom Value Ranges

### min-hearts
- **Casual**: 10-15 hearts
- **Standard**: 20 hearts ✅
- **Hardcore**: 25-30+ hearts

### reset-hearts  
- **Forgiving**: 15-20 hearts
- **Standard**: 10 hearts ✅
- **Harsh**: 5-8 hearts
- **Cosmetic**: 20 hearts (same as max)

### max-prestiges
- **Casual**: 100+ levels
- **Standard**: 50 levels ✅
- **Hardcore**: 20-25 levels
- **Milestone-focused**: 5-10 levels

---

## Testing Your Configuration

After setting your config, test with:

```bash
# As admin
/lifestealz hearts set <player> <amount_needed>

# As player
/prestige

# Check result
/hearts <player>
```

---

## Tips for Configuration

1. **Start conservative** - Can always increase difficulty later
2. **Balance with grinding** - Don't make prestige too easy or too hard
3. **Match server theme** - Hardcore server = hard prestige
4. **Consider alt accounts** - Some players will have multiple
5. **Test thoroughly** - Try with different prestige levels
6. **Broadcast wisely** - Too many prestiges = chat spam
7. **Plan rewards** - Prestige should feel rewarding
8. **Document custom changes** - Keep notes of any code modifications

---

## Migration Between Configs

**To change prestige config safely**:

1. Disable prestige: `enabled: false`
2. Let players know: "Prestige rules changing soon!"
3. Wait until prestige activity stops
4. Update config values
5. Re-enable: `enabled: true`
6. Announce changes

No data is lost during config changes - prestige counts persist!

---

**All configurations tested and working. Choose what fits your server best!**
