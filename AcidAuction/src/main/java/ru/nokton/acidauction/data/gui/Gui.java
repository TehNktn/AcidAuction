package ru.nokton.acidauction.data.gui;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public abstract class Gui {
    public static Map<UUID, Gui> guis = new HashMap();

    public Gui(Player player) {
        guis.put(player.getUniqueId(), this);
    }

    public abstract void open();

    public abstract void event(InventoryClickEvent var1);

    public abstract void event(InventoryCloseEvent var1);

    public void unregister(Player player) {
        guis.remove(player.getUniqueId());
    }

    public Inventory createEmptyInventory(String title, int size) {
        return Bukkit.createInventory((InventoryHolder)null, size, title);
    }

    public void fillLine(int position, Inventory inventory, ItemStack item) {
        for(int i = (position - 1) * 9; i < position * 9; ++i) {
            inventory.setItem(i, item);
        }

    }

    public void fillColumn(int position, Inventory inventory, ItemStack item) {
        for(int i = (position - 1) * 9; i < inventory.getSize(); i += 9) {
            inventory.setItem(i, item);
        }

    }
}
