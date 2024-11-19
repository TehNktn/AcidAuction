package ru.nokton.acidauction;

import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import ru.nokton.acidauction.data.Auction;
import ru.nokton.acidauction.data.ItemOffer;
import ru.nokton.acidauction.data.gui.GuiAuction;
import ru.nokton.acidauction.util.Values;


public class Main extends JavaPlugin {
    private static Main instance;
    private static Files files;

    public Main() {
    }

    public void onEnable() {
        instance = this;
        files = new Files(this);
        YamlConfiguration config = files.registerNewFile("config");
        YamlConfiguration data = files.registerNewFile("data");
        Values.setConfig(config);
        Values.read();
        int loaded = 0;
        Iterator var4 = data.getKeys(false).iterator();

        while(var4.hasNext()) {
            String key = (String)var4.next();
            UUID owner = UUID.fromString(key);

            for(int i = 1; i < 100 && data.contains(key + "." + i); ++i) {
                ++loaded;
                double price = data.getDouble(key + "." + i + ".price");
                long date = data.getLong(key + "." + i + ".date");
                ItemStack item = data.getItemStack(key + "." + i + ".item");
                ItemOffer offer = new ItemOffer(item, price, new Date(date), owner);
                Auction.addOffer(offer);
            }
        }

        this.getLogger().info("Loaded " + loaded + " offers.");
        Bukkit.getPluginManager().registerEvents(new Handler(), this);
        Object var15 = null;
        JavaPlugin var16 = JavaPlugin.getProvidingPlugin(this.getClass());
        String var17 = var16.getName();
        Bukkit.getConsoleSender().sendMessage("§r[" + var17 + "] §rPlugin leaked by §aHightLeak §f| §ehttps://hightleak.xyz/§r");
    }

    public void onDisable() {
        YamlConfiguration data = new YamlConfiguration();
        Map<String, Integer> last = new HashMap();
        Iterator var3 = Auction.getOffers().iterator();

        while(var3.hasNext()) {
            ItemOffer offer = (ItemOffer)var3.next();
            String uuid = offer.getOwner().toString();
            double price = offer.getPrice();
            long date = offer.getDate().getTime();
            ItemStack item = offer.getItem();
            int id = (Integer)last.getOrDefault(uuid, 0) + 1;
            data.set(uuid + "." + id + ".price", price);
            data.set(uuid + "." + id + ".date", date);
            data.set(uuid + "." + id + ".item", item);
            last.put(uuid, id);
        }

        files.save("data", data);
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("auc")) {
            return false;
        }

        if (args.length < 1) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                GuiAuction ga = new GuiAuction(player);
                ga.open();
            } else {
                sender.sendMessage("§cЭта команда может быть выполнена только игроком.");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("reload") && sender.isOp()) {
            try {
                files.reload("config");
                YamlConfiguration config = files.getYaml("config");
                Values.setConfig(config);
                Values.read();
                sender.sendMessage("§a[Auction] Конфигурация перезагружена.");
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                sender.sendMessage("§cОшибка при перезагрузке конфигурации.");
                return true;
            }
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("sell")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cЭта команда может быть выполнена только игроком.");
                return true;
            }

            Player player = (Player) sender;
            UUID uuid = player.getUniqueId();
            PlayerInventory inv = player.getInventory();
            ItemStack itemOffer = inv.getItemInMainHand().clone();

            // Проверка, что предмет в руке не пустой
            if (itemOffer == null || itemOffer.getType() == Material.AIR) {
                sender.sendMessage("§cВы должны держать предмет в руке.");
                return true;
            }

            if (Values.BLOCKED.contains(itemOffer.getType())) {
                sender.sendMessage(Values.BLOCK);
                return true;
            }

            // Проверка на максимальное количество предложений для игрока
            if (!sender.isOp() && Auction.getOffers(uuid).size() >= this.getMaxAmount(player)) {
                sender.sendMessage(Values.MAX);
                return true;
            }

            double price;
            try {
                price = Double.parseDouble(args[1].replace(",", "."));
            } catch (NumberFormatException e) {
                sender.sendMessage("§cВведите корректную цену.");
                return true;
            }

            int amount = args.length == 2 ? itemOffer.getAmount() : Integer.parseInt(args[2]);

            // Проверка, что у игрока достаточно предметов
            if (itemOffer.getAmount() < amount) {
                sender.sendMessage(Values.LOW);
                return true;
            }

            // Обновление количества предметов в инвентаре
            itemOffer.setAmount(amount);
            itemOffer.setAmount(itemOffer.getAmount() - amount);
            inv.setItemInMainHand(itemOffer);

            // Создание и добавление предложения на аукцион
            ItemOffer offer = new ItemOffer(itemOffer, price, player.getUniqueId());
            Auction.addOffer(offer);
            sender.sendMessage(Values.PUT);
            return true;
        }

        sender.sendMessage(Values.HELP);
        return true;
    }

    public static Main getInstance() {
        return instance;
    }

    private int getMaxAmount(Player player) {
        int amount = 0;
        Iterator var3 = player.getEffectivePermissions().iterator();

        while(var3.hasNext()) {
            PermissionAttachmentInfo perm = (PermissionAttachmentInfo)var3.next();
            String permission = perm.getPermission();
            if (permission.startsWith("auction.")) {
                permission = permission.substring(permission.indexOf(".") + 1);
                amount = Integer.parseInt(permission);
                break;
            }
        }

        return amount;
    }
}