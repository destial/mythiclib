package io.lumine.mythic.lib.listener;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.gui.PluginInventory;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    /**
     * Async pre join events are unreliable for some reason so
     * it seems to be better to initialize player data on the
     * lowest priority possible synchronously when player join
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void loadData(PlayerJoinEvent event) {

        // Setup player data
        final MMOPlayerData data = MMOPlayerData.setup(event.getPlayer());

        // Flush old modifiers
        UtilityMethods.flushOldModifiers(data.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void registerOfflinePlayers(PlayerQuitEvent event) {
        MMOPlayerData.get(event.getPlayer()).updatePlayer(null);
    }

    @EventHandler
    public void handleCustomInventoryClicks(InventoryClickEvent event) {
        if (event.getInventory().getHolder() != null && event.getInventory().getHolder() instanceof PluginInventory)
            ((PluginInventory) event.getInventory().getHolder()).whenClicked(event);
    }
}
