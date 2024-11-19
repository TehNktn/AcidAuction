package ru.nokton.acidauction.util;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Values {
    private static YamlConfiguration config;
    public static long TIME;
    public static String HELP;
    public static String PUT;
    public static String SELL;
    public static String BUY;
    public static String BLOCK;
    public static String MAX;
    public static String LOW;
    public static ItemStack DECOR;
    public static ItemStack BACK;
    public static ItemStack REFLESH;
    public static ItemStack INFO;
    public static ItemStack MY_OFFERS;
    public static ItemStack SORT;
    public static ItemStack ARROW_BACK;
    public static ItemStack ARROW_NEXT;
    public static ItemStack CONFIRM;
    public static ItemStack LOW_MONEY;
    public static ItemStack PURCHASED;
    public static List<Material> BLOCKED;
    public static List<String> OFFER_LORE;
    public static List<String> MY_OFFER_LORE;
    public static List<String> MY_EXPIRED_OFFER_LORE;

    public Values() {
    }

    public static void setConfig(YamlConfiguration file) {
        config = file;
    }

    public static void read() {
        TIME = config.getLong("time");
        HELP = config.getString("messages.help").replace("&", "§");
        PUT = config.getString("messages.put").replace("&", "§");
        SELL = config.getString("messages.sell").replace("&", "§");
        BUY = config.getString("messages.buy").replace("&", "§");
        BLOCK = config.getString("messages.block").replace("&", "§");
        MAX = config.getString("messages.max").replace("&", "§");
        LOW = config.getString("messages.low").replace("&", "§");
        DECOR = itemAsConfig("decor");
        BACK = itemAsConfig("back");
        REFLESH = itemAsConfig("reflesh");
        INFO = itemAsConfig("info");
        MY_OFFERS = itemAsConfig("my-offers");
        SORT = itemAsConfig("sort");
        ARROW_BACK = itemAsConfig("arrow-back");
        ARROW_NEXT = itemAsConfig("arrow-next");
        CONFIRM = itemAsConfig("confirm");
        LOW_MONEY = itemAsConfig("low-money");
        PURCHASED = itemAsConfig("purchased");
        (OFFER_LORE = config.getStringList("items.offer-lore")).replaceAll((t) -> {
            return t.replace("&", "§");
        });
        (MY_OFFER_LORE = config.getStringList("items.my-offer-lore")).replaceAll((t) -> {
            return t.replace("&", "§");
        });
        (MY_EXPIRED_OFFER_LORE = config.getStringList("items.my-expired-offer-lore")).replaceAll((t) -> {
            return t.replace("&", "§");
        });
        BLOCKED = new ArrayList();
        config.getStringList("blocked-items").forEach((t) -> {
            BLOCKED.add(material(t));
        });
    }

    private static ItemStack itemAsConfig(String index) {
        return itemAsMeta(config.getString("items." + index + ".type").toUpperCase(), config.getString("items." + index + ".name"), config.getStringList("items." + index + ".lore"));
    }

    private static ItemStack itemAsMeta(String material, String name, List<String> lore) {
        short id = 0;
        name = name.replace("&", "§");
        lore.replaceAll((line) -> {
            return line.replace("&", "§");
        });
        if (material.contains(":")) {
            id = Short.parseShort(material.substring(material.indexOf(":") + 1));
            material = material.substring(0, material.indexOf(":"));
        }

        ItemStack item = new ItemStack(material(material), 1, id);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private static Material material(String material) {
        return Material.matchMaterial(material.toUpperCase());
    }
}
