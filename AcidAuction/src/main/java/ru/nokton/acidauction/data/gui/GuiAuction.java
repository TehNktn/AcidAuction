package ru.nokton.acidauction.data.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;
import ru.nokton.acidauction.data.Auction;
import ru.nokton.acidauction.data.ItemOffer;
import ru.nokton.acidauction.util.Economy;
import ru.nokton.acidauction.util.Utils;
import ru.nokton.acidauction.util.Values;

public class GuiAuction extends Gui {
    private final Map<Integer, BukkitTask> tasks = new HashMap();
    private final Map<ItemOffer, ClickType> confirms = new HashMap();
    private final Map<Integer, ItemOffer> offers = new HashMap();
    private final Player player;
    private final UUID uuid;
    private BukkitTask updater;
    private SortType sortType;
    private int page;
    private boolean open;
    private Inventory inventory;

    public GuiAuction(Player player) {
        super(player);
        this.sortType = SortType.NONE;
        this.page = 1;
        this.open = false;
        this.player = player;
        this.uuid = player.getUniqueId();
    }

    public void open() {
        this.prepare();
        this.player.openInventory(this.inventory);
        this.open = true;
    }

    public void prepare() {
        Inventory inventory = this.createEmptyInventory("Аукцион", 54);
        this.fillLine(6, inventory, Values.DECOR);
        int startPage = (this.page - 1) * 5 * 9;
        int endPage = startPage + 45;
        List<ItemOffer> offs = Utils.getSortedOffers(this.sortType, Auction.getOffers(new UUID[]{this.uuid}));

        for(int i = startPage; i < Math.min(offs.size(), endPage); ++i) {
            ItemOffer offer = (ItemOffer)offs.get(i);
            if (!offer.isExpired()) {
                String owner = Utils.getName(offer.getOwner());
                ItemStack item = offer.getItem().clone();
                ItemMeta meta = item.getItemMeta();
                String time = offer.getRemainingTime();
                List<String> newLore = new ArrayList(Values.OFFER_LORE);
                if (meta.hasLore()) {
                    newLore.addAll(0, meta.getLore());
                }

                newLore.replaceAll((line) -> {
                    return line.replace("<owner>", owner).replace("<time>", time).replace("<price>", String.valueOf(offer.getPrice())).replace("<single-price>", Utils.cutDouble(offer.getSinglePrice(), 2));
                });
                meta.setLore(newLore);
                item.setItemMeta(meta);
                inventory.setItem(i, item);
                this.offers.put(i, offer);
            }
        }

        String sort = this.sortType == SortType.NONE ? "не отсортированно" : (this.sortType == SortType.DATE ? "дате" : "цене");
        inventory.setItem(45, Values.MY_OFFERS.clone());
        inventory.setItem(46, Values.INFO.clone());
        inventory.setItem(49, Values.REFLESH.clone());
        inventory.setItem(50, Utils.replace(Values.SORT.clone(), "<type>", sort));
        inventory.setItem(52, Values.ARROW_BACK.clone());
        inventory.setItem(53, Values.ARROW_NEXT.clone());
        this.inventory = inventory;
        this.updater = Utils.asyncTimer(() -> {
            if (!this.open) {
                Thread.currentThread().interrupt();
            } else {
                for(int slot = 0; slot < inventory.getSize(); ++slot) {
                    ItemOffer offer = (ItemOffer)this.offers.get(slot);
                    if (offer != null && !this.tasks.containsKey(slot) && !this.confirms.containsKey(offer) && inventory.getItem(slot) != null) {
                        String owner = Utils.getName(offer.getOwner());
                        ItemStack item = inventory.getItem(slot);
                        ItemMeta meta = item.getItemMeta();
                        ItemMeta origMeta = offer.getItem().clone().getItemMeta();
                        String time = offer.getRemainingTime();
                        List<String> newLore = new ArrayList(Values.OFFER_LORE);
                        if (origMeta.hasLore()) {
                            newLore.addAll(0, origMeta.getLore());
                        }

                        newLore.replaceAll((line) -> {
                            return line.replace("<owner>", owner).replace("<time>", time).replace("<price>", String.valueOf(offer.getPrice())).replace("<single-price>", Utils.cutDouble(offer.getSinglePrice(), 2));
                        });
                        meta.setLore(newLore);
                        item.setItemMeta(meta);
                    }
                }
            }

        }, 20);
    }

    public void reflesh() {
        this.open = false;
        this.updater.cancel();
        this.stopTasks();
        this.confirms.clear();
        this.offers.clear();
        this.open();
    }

    public void stopTasks() {
        this.tasks.values().forEach(BukkitTask::cancel);
    }

    public void event(InventoryClickEvent e) {
        e.setCancelled(true);
        if (e.getClickedInventory() != null) {
            if (this.open) {
                int slot = e.getSlot();
                ItemStack current = e.getCurrentItem();
                boolean click = e.getAction() != InventoryAction.PICKUP_HALF;
                if (slot < 45 && current != null) {
                    ItemOffer offer = (ItemOffer)this.offers.get(slot);
                    if (offer == null) {
                        return;
                    }

                    if (this.confirms.containsKey(offer)) {
                        ClickType type = (ClickType)this.confirms.get(offer);
                        if (this.tasks.containsKey(slot) && !((BukkitTask)this.tasks.get(slot)).isCancelled()) {
                            ((BukkitTask)this.tasks.get(slot)).cancel();
                        }

                        if (!Auction.containsOffer(offer)) {
                            this.offers.remove(slot);
                            this.confirms.remove(offer);
                            this.inventory.setItem(slot, Values.PURCHASED);
                            Utils.asyncDelay(this::reflesh, 20);
                            return;
                        }

                        double price = type == ClickType.LEFT ? offer.getPrice() : offer.getSinglePrice();
                        if (!Economy.canTake(this.uuid, price)) {
                            this.inventory.setItem(slot, Values.LOW_MONEY);
                            return;
                        }

                        ItemStack item = offer.getItem().clone();
                        if (type == ClickType.RIGHT) {
                            item.setAmount(1);
                        }

                        if (this.player.getInventory().firstEmpty() >= 0) {
                            this.player.getInventory().addItem(new ItemStack[]{item});
                        } else {
                            this.player.getLocation().getWorld().dropItem(this.player.getLocation(), item);
                        }

                        if (type == ClickType.RIGHT) {
                            offer.buyOne();
                        } else if (type == ClickType.LEFT) {
                            offer.buyFull();
                        }

                        this.offers.remove(slot);
                        this.confirms.remove(offer);
                        this.reflesh();
                        Economy.give(offer.getOwner(), price);
                        Economy.take(this.uuid, price);
                        this.player.sendMessage(Values.BUY.replace("<item>", item.getType().name()).replace("<price>", String.valueOf(price)));
                        if (Utils.isOnline(offer.getOwner())) {
                            Player seller = Bukkit.getPlayer(offer.getOwner());
                            seller.sendMessage(Values.SELL.replace("<item>", item.getType().name()).replace("<price>", String.valueOf(price)).replace("<player>", this.player.getName()));
                        }
                    } else {
                        if (!Auction.containsOffer(offer)) {
                            this.offers.remove(slot);
                            this.confirms.remove(offer);
                            this.inventory.setItem(slot, Values.PURCHASED);
                            Utils.asyncDelay(this::reflesh, 20);
                            return;
                        }

                        double price2 = click ? offer.getPrice() : offer.getSinglePrice();
                        if (!Economy.canTake(this.uuid, price2)) {
                            this.inventory.setItem(slot, Values.LOW_MONEY);
                            return;
                        }

                        this.inventory.setItem(slot, Values.CONFIRM.clone());
                        this.confirms.put(offer, click ? ClickType.LEFT : ClickType.RIGHT);
                        this.tasks.put(slot, Utils.asyncDelay(() -> {
                            if (this.confirms.containsKey(offer)) {
                                this.inventory.setItem(slot, offer.getItem());
                            }

                            this.confirms.remove(offer);
                        }, 50));
                    }
                } else {
                    if (slot <= 44 || current.equals(Values.DECOR)) {
                        return;
                    }

                    if (slot == 45) {
                        this.player.closeInventory();
                        GuiOffers ga = new GuiOffers(this.player);
                        ga.open();
                        return;
                    }

                    if (slot == 49) {
                        this.reflesh();
                        return;
                    }

                    if (slot == 50) {
                        this.sortType = this.sortType.next();
                        this.reflesh();
                        return;
                    }

                    if (slot == 52) {
                        this.page = Math.max(1, this.page - 1);
                        this.reflesh();
                        return;
                    }

                    if (slot == 53) {
                        this.page = Math.min(Auction.getNoExpiredOffers(new UUID[]{this.uuid}).size() / 45 + 1, this.page + 1);
                        this.reflesh();
                    }
                }

            }
        }
    }

    public void event(InventoryCloseEvent e) {
        if (this.open) {
            this.unregister(this.player);
            this.updater.cancel();
        }

    }
}
