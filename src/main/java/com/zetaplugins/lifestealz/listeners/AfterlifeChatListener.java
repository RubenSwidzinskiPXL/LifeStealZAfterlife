package com.zetaplugins.lifestealz.listeners;

import com.zetaplugins.lifestealz.LifeStealZ;
import com.zetaplugins.lifestealz.storage.PlayerData;
import com.zetaplugins.lifestealz.util.MessageUtils;
import com.zetaplugins.zetacore.annotations.AutoRegisterListener;
import io.papermc.paper.event.player.AsyncChatEvent;
import io.papermc.paper.chat.ChatRenderer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Modifies chat for players in the afterlife.
 * - Preserves existing chat format from other plugins (ranks, prefixes, etc.)
 * - Adds customizable death tag
 * - Makes entire message grey/transparent
 * Compatible with: LuckPerms, Vault, EssentialsX Chat, ChatControl, etc.
 */
@AutoRegisterListener
public final class AfterlifeChatListener implements Listener {
    private final LifeStealZ plugin;
    
    public AfterlifeChatListener(LifeStealZ plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChat(AsyncChatEvent event) {
        if (!plugin.getConfig().getBoolean("afterlife.enabled", false)) {
            return;
        }
        
        Player player = event.getPlayer();
        PlayerData data = plugin.getStorage().load(player.getUniqueId());
        
        if (data == null || !data.isAfterlife()) {
            return;
        }
        
        // Get the death tag from language file (allows customization per language)
        Component deathTag = MessageUtils.getAndFormatMsg(
            false,
            "afterlifeDeathTag",
            "&8[☠]&7"
        );
        
        // Wrap the renderer to preserve existing formatting but add our modifications
        // This approach maintains compatibility with other chat plugins
        ChatRenderer base = event.renderer();
        event.renderer((source, sourceDisplayName, message, viewer) -> {
            // Color only the player's chat message to dark gray, preserving rank/prefix coloring
            Component greyMsg = message.color(NamedTextColor.DARK_GRAY);
            Component baseRendered = base.render(source, sourceDisplayName, greyMsg, viewer);
            return Component.empty().append(deathTag).append(Component.space()).append(baseRendered);
        });
    }
}
