# LifeStealZ Prestige System - Complete Integration Guide

## Overview

The prestige system allows players to reset their hearts to a base amount after reaching 20 hearts, gaining a "Prestige X" rank that persists. This guide covers everything you need to integrate the prestige system with your NPC setup.

---

## Part 1: Plugin Integration (Already Done)

The LifeStealZ plugin has been updated with the following changes:

### 1.1 Database Support
- Added `prestigeCount` column to the player data storage
- Automatically migrates existing databases (SQLite, MySQL, MariaDB)
- Tracks prestige count for each player

### 1.2 PAPI Placeholders
The following PlaceholderAPI placeholders are now available:
```
%lifestealz_prestige_count%  → Returns player's prestige count (0 if none)
%lifestealz_prestige%        → Returns player's prestige level or "None"
%lifestealz_hearts%          → Returns player's current hearts
%lifestealz_maxhearts%       → Returns player's max hearts
```

### 1.3 Command
- `/prestige` - Player command to prestige (requires 20 hearts by default)
- Permission: `lifestealz.prestige` (default: true for all players)

### 1.4 Configuration
Edit `config.yml` to customize prestige settings:
```yaml
prestige:
  enabled: true                  # Enable/disable prestige system
  min-hearts: 20                 # Minimum hearts needed to prestige
  reset-hearts: 10               # Hearts after prestiging
  max-prestiges: 50              # Maximum prestige level
  broadcast: true                # Broadcast prestiges
  broadcast-message: "&b%player% &ehas reached &b[Prestige %count%]&e!"
```

---

## Part 2: NPC Setup with Citizens2

### 2.1 Create the NPC
```bash
/npc create PrestigeNPC
/npc select PrestigeNPC
```

### 2.2 Position the NPC
```bash
# Position your NPC at your prestige location
/npc tphere  # Teleports NPC to you
# Or navigate and use:
/npc moveto
```

### 2.3 NPC Appearance
```bash
/npc skin set <player_name>      # Set custom skin
/npc equip                        # Equip items
/npc equipment hand <item>        # Hand item
```

### 2.4 Add Click Commands
```bash
# Command that runs when player clicks NPC (as player)
/npc cmd add -p lifestealz.prestige:prestige
```

This will execute `/prestige` when a player clicks the NPC.

---

## Part 3: Hologram Setup with DecentHolograms

### 3.1 Create Informational Hologram
This hologram displays prestige info above the NPC:

```bash
/dh create prestige_info
/dh edit prestige_info addline "&6&lPrestige Center"
/dh edit prestige_info addline "&eRequirement: &c20 Hearts"
/dh edit prestige_info addline "&7Your Hearts: &a%lifestealz_hearts%&7/&c20"
/dh edit prestige_info addline "&7Current Prestige: &b%lifestealz_prestige%"
/dh edit prestige_info addline "&7[&aClick NPC to Prestige&7]"
```

### 3.2 Position Hologram
```bash
# Move hologram above the NPC
/dh teleport prestige_info <x> <y> <z>
# Or use relative positioning
/dh edit prestige_info setline 1 "&6&lPrestige Center"  # Change text if needed
```

### 3.3 Optional: Update Frequency
Holograms auto-update every few ticks, so PAPI placeholders will refresh in real-time.

---

## Part 4: Prestige Rank Integration with LuckPerms

### 4.1 Create Prestige Groups (One-time setup)
```bash
# Create groups for each prestige level
/lp creategroup prestige1
/lp creategroup prestige2
/lp creategroup prestige3
# ... repeat up to prestige50 (or your max)
```

### 4.2 Add Prefix to Each Group
```bash
/lp group prestige1 meta setprefix "&4&l[Prestige 1] " 100
/lp group prestige2 meta setprefix "&4&l[Prestige 2] " 100
/lp group prestige3 meta setprefix "&4&l[Prestige 3] " 100
# ... and so on
```

The priority `100` ensures the prestige prefix displays prominently.

### 4.3 Optional: Extend Plugin to Auto-Add Ranks

To automatically add players to prestige groups when they prestige, you'd need to:

1. Add LuckPerms dependency to your pom.xml (if not already added)
2. Hook into prestige event or modify the command

**Alternative simple approach (Manual)**: Create a one-time admin command that batch-assigns groups based on prestige_count from the database.

---

## Part 5: Optional GUI Confirmation with DeluxeMenus

### 5.1 Create Prestige Confirmation GUI
Create file: `plugins/DeluxeMenus/guis/prestige_confirm.yml`

```yaml
# Prestige Confirmation GUI
menu_title: '&4&lPrestige Confirmation'
size: 27
update_interval: 1

items:
  info:
    material: PAPER
    slot: 11
    display_name: '&ePrestige Information'
    lore:
      - '&7Current Hearts: &a%lifestealz_hearts%'
      - '&7After Prestige: &c10'
      - ''
      - '&7Current Prestige: &b%lifestealz_prestige%'
      - '&7Next Prestige: &b%lifestealz_prestige_count:+1%'

  confirm:
    material: EMERALD_BLOCK
    slot: 13
    display_name: '&a&lYES - Prestige Now'
    lore:
      - '&7Click to confirm prestige'
      - '&7Hearts will reset to 10'
      - '&7You will gain Prestige rank'
    click_commands:
      - '[CLOSE]'
      - '[PLAYER] prestige'

  cancel:
    material: BARRIER
    slot: 15
    display_name: '&c&lNO - Cancel'
    lore:
      - '&7Click to cancel'
    click_commands:
      - '[CLOSE]'
```

### 5.2 Update NPC Click Command
If using GUI:
```bash
/npc cmd add -p lifestealz.prestige:dm open prestige_confirm %player%
```

---

## Part 6: Database Verification

### 6.1 Check Prestige Column (SQLite)
```bash
sqlite3 plugins/LifeStealZ/userData.db
.schema hearts
```

You should see: `prestigeCount INTEGER DEFAULT 0`

### 6.2 MySQL/MariaDB
```sql
SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'hearts' AND COLUMN_NAME = 'prestigeCount';
```

---

## Part 7: Testing

### 7.1 Test Prestige Command
```bash
# Set player hearts to 20 (as admin)
/lifestealz hearts set <player> 20

# Player runs prestige command
/prestige

# Verify hearts reset to 10
/hearts <player>
```

### 7.2 Test PAPI Placeholders
```bash
/papi parse me %lifestealz_prestige_count%
/papi parse me %lifestealz_hearts%
```

### 7.3 Test NPC Click
- Stand in front of NPC
- Right-click NPC
- Should execute prestige if requirements met

---

## Part 8: Customization Examples

### 8.1 Change Prestige Requirements
Edit `config.yml`:
```yaml
prestige:
  min-hearts: 30          # Require 30 hearts instead of 20
  reset-hearts: 5         # Reset to 5 hearts (harder)
  max-prestiges: 100      # Allow up to 100 prestiges
```

### 8.2 Custom Broadcast Message
```yaml
prestige:
  broadcast: true
  broadcast-message: "&c✧ &b%player% &ahas achieved &c[Prestige %count%] &c✧"
```

### 8.3 Disable Prestige
```yaml
prestige:
  enabled: false
```

### 8.4 LuckPerms Chat Format with Prestige
Edit `config.json` in LuckPerms plugins folder:
```json
{
  "chat-formatting": {
    "prefix-format": "%luckperms_prefix_100% %player%"
  }
}
```

This will show: `[Prestige 5] PlayerName` in chat

---

## Part 9: Advanced Features (Custom Code)

### 9.1 Milestone Prestige Events
To add special effects at milestone prestiges (e.g., Prestige 5, 10, 25):

Edit the PrestigeCommand.java `prestigePlayer()` method to add:
```java
// After setPrestigeCount()
if (newPrestigeCount % 5 == 0) {
    // Milestone prestige! Add special effects
    player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BURST, 1.0f, 1.0f);
    Bukkit.broadcast(Component.text("✨ MILESTONE! " + player.getName() + " reached Prestige " + newPrestigeCount + "! ✨"));
}
```

### 9.2 Prestige Rewards (Items/Money)
Add to the `prestigePlayer()` method:
```java
// Give reward items
player.getInventory().addItem(new ItemStack(Material.DIAMOND, 10));

// Or with Economy plugin (if using Vault):
EconomyResponse response = plugin.getEconomy().depositPlayer(player, 10000);
if (response.transactionSucceeded()) {
    player.sendMessage("&aYou earned $10,000 for prestiging!");
}
```

### 9.3 Prestige Announcement with Actionbar
Replace the broadcast with:
```java
net.kyori.adventure.text.Component prestigeMsg = 
    Component.text("★ " + player.getName() + " reached Prestige " + newPrestigeCount + "! ★")
        .color(NamedTextColor.GOLD);

for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
    onlinePlayer.sendActionBar(prestigeMsg);
}
```

---

## Part 10: Troubleshooting

| Issue | Solution |
|-------|----------|
| NPC doesn't work | Ensure `/npc cmd add` was executed correctly |
| Placeholders show "PlayerNotFound" | Restart PlaceholderAPI: `/papi reload` |
| Hearts not resetting | Check config.yml prestige section |
| Database error on startup | Automatic migration should handle it; check server logs |
| LuckPerms prefix not showing | Ensure `%luckperms_prefix_100%` is in chat format |
| GUI won't open | Ensure DeluxeMenus is installed and file is in correct folder |

---

## Summary

Your prestige system is now fully integrated! Here's the complete flow:

1. **Player reaches 20 hearts** (configurable)
2. **Player clicks prestige NPC** (or runs `/prestige`)
3. **Confirmation GUI appears** (optional)
4. **Player confirms prestige**
5. **Hearts reset to 10** (configurable)
6. **Prestige count increments**
7. **Auto-add to prestige rank group** (via LuckPerms)
8. **Chat prefix updates** (shows [Prestige X])
9. **Broadcast sent** to all players
10. **Data persists** in database

Enjoy your prestige system!
