# Prestige System - Quick Reference Card

## What Was Added

### Core System
✅ Prestige tracking in database (all database types)  
✅ `/prestige` command for players  
✅ Automatic heart reset on prestige  
✅ Prestige count persistence  
✅ Permission-based access control  

### Configuration
✅ Prestige enable/disable toggle  
✅ Configurable minimum hearts requirement (default: 20)  
✅ Configurable reset hearts (default: 10)  
✅ Configurable max prestige level (default: 50)  
✅ Broadcast notifications with custom messages  

### Integration Points
✅ PlaceholderAPI placeholders (`%lifestealz_prestige_count%`, `%lifestealz_prestige%`)  
✅ LuckPerms group integration (manual setup required)  
✅ Citizens2 NPC support (via click commands)  
✅ DecentHolograms support (via PAPI placeholders)  
✅ DeluxeMenus GUI support (optional confirmation screen)  

---

## Required Next Steps

### 1. Compile Plugin
```bash
cd /workspaces/LifeStealZ
mvn clean package
```

### 2. Deploy to Server
- Copy `target/LifeStealZ-*.jar` to `plugins/`
- Restart server (automatic database migration)

### 3. Basic Configuration (optional)
Edit `plugins/LifeStealZ/config.yml`:
```yaml
prestige:
  enabled: true          # Set to false to disable
  min-hearts: 20        # Change requirement
  reset-hearts: 10      # Change reset amount
  max-prestiges: 50     # Change max level
  broadcast: true       # Enable/disable announcements
```

### 4. NPC Setup (see PRESTIGE_SYSTEM_GUIDE.md)
```bash
/npc create PrestigeNPC
/npc cmd add -p lifestealz.prestige:prestige
```

### 5. LuckPerms Setup (optional)
```bash
/lp creategroup prestige1
/lp group prestige1 meta setprefix "&4&l[Prestige 1] " 100
# Repeat for each level up to max-prestiges
```

### 6. Holograms (optional)
```bash
/dh create prestige_info
/dh edit prestige_info addline "&6&lPrestige Center"
/dh edit prestige_info addline "&eHearts: &a%lifestealz_hearts%&7/&c20"
/dh edit prestige_info addline "&ePrestige: &b%lifestealz_prestige%"
```

---

## Commands & Permissions

| Command | Permission | Default | Effect |
|---------|-----------|---------|--------|
| `/prestige` | `lifestealz.prestige` | true | Prestige if requirements met |

---

## Placeholders

| Placeholder | Returns | Example |
|------------|---------|---------|
| `%lifestealz_prestige_count%` | Prestige number (0 if none) | `5` |
| `%lifestealz_prestige%` | "None" or prestige number | `Prestige 5` |
| `%lifestealz_hearts%` | Current max hearts | `20` |
| `%lifestealz_maxhearts%` | Max possible hearts | `20` |

---

## Files Changed

| File | Type | Purpose |
|------|------|---------|
| `PlayerData.java` | Modified | Added prestigeCount field |
| `SQLStorage.java` | Modified | Database column and mapping |
| `SQLiteStorage.java` | Modified | Migration support |
| `MySQLStorage.java` | Modified | Migration support |
| `MariaDBStorage.java` | Modified | Migration support |
| `PapiExpansion.java` | Modified | PAPI placeholders |
| `PrestigeCommand.java` | **Created** | Main command handler |
| `plugin.yml` | Modified | Command + permission |
| `config.yml` | Modified | Prestige settings |
| `PRESTIGE_SYSTEM_GUIDE.md` | **Created** | Full setup guide |
| `PRESTIGE_IMPLEMENTATION_SUMMARY.md` | **Created** | Change summary |

---

## Workflow

1. Player reaches 20 hearts ✓
2. Player runs `/prestige` ✓
3. System checks requirements ✓
4. Hearts reset to 10 ✓
5. Prestige count +1 ✓
6. Database saves ✓
7. Message broadcasted ✓
8. LuckPerms rank added (manual) 🔧

---

## Testing Checklist

- [ ] Plugin compiles without errors
- [ ] Server starts successfully
- [ ] Database migration runs (check logs)
- [ ] `/prestige` command exists
- [ ] Permission system works
- [ ] Placeholders show correct values
- [ ] Player gets message when prestiging
- [ ] Hearts reset to configured amount
- [ ] Prestige count increments in database
- [ ] Broadcast message appears in chat
- [ ] NPC click works (if set up)
- [ ] Holograms update (if set up)

---

## Troubleshooting

**Plugin won't start?**
→ Check Maven compilation errors in terminal

**Database errors?**
→ Check `plugins/LifeStealZ/logs/` or server logs

**Prestige doesn't work?**
→ Verify permission: `/perms check <player> lifestealz.prestige`

**Placeholders show "InvalidPlaceholder"?**
→ Ensure PlaceholderAPI is installed and reloaded: `/papi reload`

**NPC doesn't trigger prestige?**
→ Verify command: `/npc info PrestigeNPC` then check click commands

---

## Default Configuration Values

```yaml
prestige:
  enabled: true                  # System on/off
  min-hearts: 20                 # Prestige requirement
  reset-hearts: 10               # Post-prestige hearts
  max-prestiges: 50              # Level cap
  broadcast: true                # Global announcements
  broadcast-message: "&b%player% &ehas reached &b[Prestige %count%]&e!"
```

---

## Key Behaviors

| Situation | Result |
|-----------|--------|
| Player has <20 hearts | "You need 20 hearts to prestige!" |
| Player is eliminated | "You are eliminated! You cannot prestige." |
| Player at max prestige | "You have reached maximum prestige!" |
| Prestige succeeds | Hearts→10, count++, broadcast sent, data saved |
| Prestige disabled | Command fails with "disabled" message |

---

## Full Integration Checklist

### Phase 1: Plugin (✅ Complete)
- [x] Database support added
- [x] Command created
- [x] Permissions configured
- [x] PAPI placeholders added
- [x] Configuration section added

### Phase 2: Deploy (🔲 To Do)
- [ ] Compile plugin
- [ ] Deploy to server
- [ ] Restart server
- [ ] Verify database migration

### Phase 3: NPC (🔲 To Do)
- [ ] Create NPC with Citizens2
- [ ] Add click commands
- [ ] Create holograms
- [ ] Test NPC interaction

### Phase 4: Ranks (🔲 To Do - Optional)
- [ ] Create LuckPerms groups
- [ ] Add prefixes to groups
- [ ] Update chat format
- [ ] Test rank display

---

## Support

For detailed setup: See `PRESTIGE_SYSTEM_GUIDE.md`  
For technical details: See `PRESTIGE_IMPLEMENTATION_SUMMARY.md`  
For issues: Check server logs in `logs/` directory

---

**Status**: ✅ Implementation Complete - Ready for Deployment
