# Prestige Admin Commands & LuckPerms Integration

## Admin Commands

### View Prestige Level
```
/lifestealz prestige get <player>
```
Shows the current prestige level of a player.

### Set Prestige Level
```
/lifestealz prestige set <player> <amount>
```
Sets a player's prestige to a specific level (0 or higher).
- Automatically updates LuckPerms groups
- Example: `/lifestealz prestige set Player123 5`

### Reset Prestige
```
/lifestealz prestige reset <player>
```
Resets a player's prestige to 0.
- Automatically removes LuckPerms prestige group

**Permission**: `lifestealz.admin.prestige` (default: op)

---

## LuckPerms Integration Setup

### Step 1: Create Prestige Groups

Create groups for each prestige level:
```bash
/lp creategroup prestige1
/lp creategroup prestige2
/lp creategroup prestige3
# ... up to prestige50 (or your max)
```

### Step 2: Set Prefixes for Each Group

Add prefixes to each prestige group:
```bash
/lp group prestige1 meta setprefix 100 "&4[Prestige 1] "
/lp group prestige2 meta setprefix 100 "&4[Prestige 2] "
/lp group prestige3 meta setprefix 100 "&4[Prestige 3] "
# ... continue for all levels
```

The priority `100` ensures prestige prefix shows as a secondary prefix.

### Step 3: Configure Chat Format

Edit LuckPerms chat meta to show both prefixes:
```bash
/lp group default meta setprefix 50 "&7"  # Lower priority for main rank
```

In your chat plugin (like EssentialsX or ChatControl), use:
```
%luckperms_prefix_100%%luckperms_prefix_50%%player%: %message%
```

This will show:
```
[Prestige 5] [VIP] PlayerName: Hello!
```

### Step 4: Automatic Assignment

The plugin will **automatically**:
- ✅ Add player to `prestige1` group when they prestige to level 1
- ✅ Remove from `prestige1` and add to `prestige2` when they prestige to level 2
- ✅ Update groups when using `/lifestealz prestige set`
- ✅ Remove prestige group when using `/lifestealz prestige reset`

---

## How It Works

### When Player Prestiges
1. Player clicks PRESTIGE in menu → `/lifestealz prestige confirm`
2. Hearts reset to 10
3. Prestige count increases by 1
4. Plugin automatically adds player to `prestige{count}` group in LuckPerms
5. Plugin removes old prestige group (if any)
6. Player's prefix updates immediately

### Manual Management
```bash
# View prestige
/lifestealz prestige get PlayerName

# Set to prestige 10
/lifestealz prestige set PlayerName 10
# → Removes old group, adds prestige10 group

# Reset prestige
/lifestealz prestige reset PlayerName
# → Removes prestige group completely
```

---

## LuckPerms Track (Optional)

You can create a prestige track for easy promotion:
```bash
/lp createtrack prestige
/lp track prestige append prestige1
/lp track prestige append prestige2
/lp track prestige append prestige3
# ... continue

# Then use:
/lp user PlayerName promote prestige
```

However, the **automatic system is better** because it:
- Updates instantly when prestiging
- Handles skipping levels (e.g., admin set to prestige 10)
- Removes old groups automatically

---

## Troubleshooting

### Groups Not Being Added?
Check that LuckPerms is installed:
```bash
/lp info
```

If you see "LuckPerms integration enabled" in server logs, it's working.

### Prefix Not Showing?
1. Check group has prefix:
   ```bash
   /lp group prestige1 info
   ```
2. Check player is in group:
   ```bash
   /lp user PlayerName info
   ```
3. Ensure chat plugin supports `%luckperms_prefix_100%`

### Player Has Wrong Group?
Manually fix:
```bash
/lp user PlayerName parent remove prestige5
/lp user PlayerName parent add prestige10
```

Or use the plugin command:
```bash
/lifestealz prestige set PlayerName 10
```

---

## Example Full Setup Script

Create all prestige groups and prefixes at once:
```bash
# Run this in your server console or as operator
for i in {1..50}
do
  lp creategroup prestige$i
  lp group prestige$i meta setprefix 100 "&4[Prestige $i] "
done
```

For Windows batch file:
```batch
@echo off
for /L %%i in (1,1,50) do (
  echo /lp creategroup prestige%%i
  echo /lp group prestige%%i meta setprefix 100 "^&4[Prestige %%i] "
)
```

---

## Summary

✅ **Admin Commands**: set, get, reset
✅ **Automatic LuckPerms Integration**: Groups added/removed automatically
✅ **Permission**: `lifestealz.admin.prestige`
✅ **Groups**: `prestige1`, `prestige2`, ... `prestige{count}`
✅ **Prefixes**: Show as secondary prefix with priority 100

**No manual LuckPerms management needed!** The plugin handles everything automatically. 🎉
