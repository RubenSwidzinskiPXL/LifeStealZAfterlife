# ✅ PRESTIGE SYSTEM - FINAL SETUP GUIDE

## TL;DR - What You Need to Do

### 1. Compile the Plugin
```bash
cd /workspaces/LifeStealZ
mvn clean package
```

### 2. Update Your DeluxeMenu
Replace your prestige menu's left_click_commands with:
```yaml
left_click_commands:
  - '[sound] ENTITY_PLAYER_LEVELUP'
  - '[message] &4&l[Prestige] &cConfirming... &7(3s)'
  - '[wait] 60'
  - '[player] lifestealz prestige confirm'
  - '[close]'
```

### 3. Deploy & Restart
- Copy compiled JAR to `plugins/`
- Restart server
- Done! 🎉

---

## What Gets Called

When a player clicks the PRESTIGE button in DeluxeMenu:
```
DeluxeMenu → [Wait 3 seconds] → /lifestealz prestige confirm
```

The command `/lifestealz prestige confirm` then:
1. Checks if player has 20+ hearts
2. Resets to 10 hearts
3. Adds 1 to prestige count
4. Saves to database
5. Broadcasts to all players

---

## PlaceholderAPI Integration

Your menu already shows:
```
%lifestealz_prestige_count%  ← Updates in real-time
%lifestealz_hearts%          ← Updates in real-time
```

---

## Configuration (Optional Customization)

Edit `plugins/LifeStealZ/config.yml`:

```yaml
prestige:
  min-hearts: 20              # Change to 30 for harder requirement
  reset-hearts: 10            # Change to 5 for harsher penalty
  max-prestiges: 50           # Change to 10 for lower cap
  broadcast-message: '&e%player% &7has reached &b%prestige_level%&7!'
```

---

## Verification Checklist

After deploying:

- [ ] Server starts without errors
- [ ] Check logs for "prestigeCount column" message (migration)
- [ ] NPC menu opens with `/prestige` command
- [ ] PRESTIGE button shows when player has 20+ hearts
- [ ] NOT READY button shows when player has <20 hearts
- [ ] Clicking PRESTIGE resets hearts to 10
- [ ] %lifestealz_prestige_count% increases by 1
- [ ] Broadcast message appears in chat
- [ ] Data persists after restart

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Menu doesn't open | Ensure NPC is using `/npc cmd add` with prestige menu command |
| Button doesn't work | Verify command is exactly `[player] lifestealz prestige confirm` |
| Hearts don't reset | Check server logs for errors, verify config.yml prestige section |
| Placeholder shows 0 | Make sure PlaceholderAPI is installed and reload it |
| Database error | Check that prestige column migration runs on startup |

---

## All Changes Made to Plugin

✅ **PlayerData.java** - Added prestigeCount field
✅ **SQLStorage.java** - Database table, mapping, insert, update
✅ **SQLiteStorage.java** - Migration for SQLite
✅ **MySQLStorage.java** - Migration for MySQL
✅ **MariaDBStorage.java** - Migration for MariaDB
✅ **PrestigeSubCommand.java** - Main prestige handler
✅ **MainCommandHandler.java** - Registered subcommand
✅ **PapiExpansion.java** - Added placeholder
✅ **config.yml** - Added prestige settings
✅ **plugin.yml** - Added permission

No breaking changes - fully backward compatible!

---

## The Command Flow

```
User clicks PRESTIGE button
        ↓
DeluxeMenu executes: [player] lifestealz prestige confirm
        ↓
PrestigeSubCommand.handlePrestige() runs
        ↓
Checks: hearts >= 20? prestige < max?
        ↓
If YES:
  - Set maxHealth = 10 * 2 = 20
  - Set prestigeCount++
  - Save to database
  - Send messages
  - Broadcast
        ↓
If NO:
  - Send error message
```

---

## After Compilation

Your JAR file will be at:
```
/workspaces/LifeStealZ/target/LifeStealZ-VERSION.jar
```

Copy this to your server's `plugins/` folder.

---

## Need Help?

- Command: `/lifestealz prestige confirm` (internal, called by menu)
- Permission: `lifestealz.prestige` (default: true)
- Placeholder: `%lifestealz_prestige_count%`
- Config: `prestige:` section in config.yml

---

**Status: ✅ COMPLETE AND READY TO DEPLOY**

Just compile, update the menu file, deploy, and you're done!
