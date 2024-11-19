package ru.nokton.acidauction;

import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import ru.nokton.acidauction.data.gui.Gui;

public class Handler implements Listener {
    public Handler() {
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Player player = (Player)e.getWhoClicked();
        UUID uuid = player.getUniqueId();
        if (Gui.guis.containsKey(uuid)) {
            ((Gui)Gui.guis.get(uuid)).event(e);
        }

    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        Player player = (Player)e.getPlayer();
        UUID uuid = player.getUniqueId();
        if (Gui.guis.containsKey(uuid)) {
            ((Gui)Gui.guis.get(uuid)).event(e);
        }

    }
}