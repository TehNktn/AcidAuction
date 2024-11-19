package ru.nokton.acidauction.data.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.nokton.acidauction.data.Auction;
import ru.nokton.acidauction.data.ItemOffer;
import ru.nokton.acidauction.util.Utils;
import ru.nokton.acidauction.util.Values;

import java.util.*;

public class GuiOffers extends Gui {
    private final Map<Integer, ItemOffer> offers = new HashMap<>(); // Указаны типы
    private final Player player;
    private final UUID uuid;
    private final Inventory inventory;
    private boolean open = false;
    private int page = 1;

    public GuiOffers(Player player) {
        super(player);
        this.player = player;
        this.uuid = player.getUniqueId();
        this.inventory = this.createEmptyInventory("Аукцион", 54);
    }

    public void open() {
        this.prepare();
        this.open = true;
        this.player.openInventory(this.inventory);
    }

    public void prepare() {
        Utils.async(() -> {
            this.fillLine(6, this.inventory, Values.DECOR);
            Arrays.fill(this.inventory.getContents(), null); // Очистка инвентаря

            List<ItemOffer> offs = Auction.getOffers(this.uuid);
            int startPage = (this.page - 1) * 5 * 9;
            int endPage = Math.min(startPage + 45, offs.size());

            for (int j = startPage; j < endPage; j++) {
                ItemOffer offer = offs.get(j);
                ItemStack item = offer.getItem().clone();
                ItemMeta meta = item.getItemMeta();
                String time = offer.getRemainingTime();

                List<String> newLore = offer.isExpired() ? new ArrayList<>(Values.MY_EXPIRED_OFFER_LORE) : new ArrayList<>(Values.MY_OFFER_LORE);
                newLore.replaceAll(line -> line.replace("<time>", time)
                        .replace("<price>", String.valueOf(offer.getPrice()))
                        .replace("<single-price>", Utils.cutDouble(offer.getSinglePrice(), 2)));

                meta.setLore(newLore);
                item.setItemMeta(meta);
                this.inventory.setItem(j, item);
                this.offers.put(j, offer);
            }

            this.inventory.setItem(45, Values.MY_OFFERS.clone());
            this.inventory.setItem(46, Values.INFO.clone());
            this.inventory.setItem(48, Values.BACK.clone());
            this.inventory.setItem(49, Values.REFLESH.clone());
            this.inventory.setItem(52, Values.ARROW_BACK.clone());
            this.inventory.setItem(53, Values.ARROW_NEXT.clone());
        });
    }

    public void reflesh() {
        this.open = false;
        this.offers.clear();
        this.prepare();
        this.open = true;
    }

    public void event(InventoryClickEvent e) {
        e.setCancelled(true);
        if (e.getClickedInventory() != null && e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR) {
            int slot = e.getSlot();
            ItemStack current = e.getCurrentItem();
            if (slot < 45 && this.offers.containsKey(slot)) {
                ItemOffer offer = this.offers.get(slot);
                if (!Auction.containsOffer(offer)) {
                    this.offers.remove(slot);
                    this.inventory.setItem(slot, Values.PURCHASED);
                    Utils.asyncDelay(this::reflesh, 20); // Подождать 20 тиков перед обновлением
                    return;
                }

                Auction.removeOffer(offer);
                ItemStack item = offer.getItem().clone();
                if (this.player.getInventory().firstEmpty() >= 0) {
                    this.player.getInventory().addItem(item);
                } else {
                    this.player.getLocation().getWorld().dropItem(this.player.getLocation(), item);
                }

                this.reflesh();
            } else if (slot > 44 && !current.equals(Values.DECOR)) {
                switch (slot) {
                    case 48: // BACK
                        this.player.closeInventory();
                        new GuiAuction(this.player).open();
                        break;
                    case 49: // REFLESH
                        this.reflesh();
                        break;
                    case 52: // ARROW_BACK
                        this.page = Math.max(1, this.page - 1);
                        this.reflesh();
                        break;
                    case 53: // ARROW_NEXT
                        this.page = Math.min((int) Math.ceil(Auction.getOffers(this.uuid).size() / 45.0), this.page + 1);
                        this.reflesh();
                        break;
                }
            }
        }
    }

    public void event(InventoryCloseEvent e) {
        if (this.open) {
            this.unregister(this.player);
        }
    }
}
