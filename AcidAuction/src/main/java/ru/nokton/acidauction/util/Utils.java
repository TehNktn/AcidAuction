package ru.nokton.acidauction.util;

import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;
import ru.nokton.acidauction.Main;
import ru.nokton.acidauction.data.ItemOffer;
import ru.nokton.acidauction.data.gui.SortType;

public class Utils {
    public Utils() {
    }

    public static String getName(UUID uuid) {
        return Bukkit.getPlayer(uuid) != null ? Bukkit.getPlayer(uuid).getName() : Bukkit.getOfflinePlayer(uuid).getName();
    }

    public static boolean isOnline(UUID uuid) {
        return Bukkit.getPlayer(uuid) != null;
    }

    public static String cutDouble(double value, int nums) {
        String string = String.valueOf(value);
        nums = Math.min(nums, string.substring(string.indexOf(".") + 1).length());
        string = string.substring(0, string.indexOf(".") + 1 + nums);
        return string;
    }

    public static void async(Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), runnable);
    }

    public static BukkitTask asyncDelay(Runnable runnable, int delay) {
        return Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getInstance(), runnable, (long)delay);
    }

    public static BukkitTask asyncTimer(Runnable runnable, int period) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(Main.getInstance(), runnable, (long)period, (long)period);
    }

    public static ItemStack replace(ItemStack item, String key, String value) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();
        lore.replaceAll((t) -> {
            return t.replace(key, value);
        });
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public static List<ItemOffer> getSortedOffers(SortType sortType, List<ItemOffer> offers) {
        if (sortType == SortType.NONE) {
            return offers;
        } else if (sortType == SortType.PRICE) {
            offers.sort((i1, i2) -> {
                return (int)(i1.getSinglePrice() - i2.getSinglePrice());
            });
            return offers;
        } else if (sortType == SortType.DATE) {
            offers.sort((i1, i2) -> {
                return (int)(i2.getDate().getTime() - i1.getDate().getTime());
            });
            return offers;
        } else {
            return null;
        }
    }
}
