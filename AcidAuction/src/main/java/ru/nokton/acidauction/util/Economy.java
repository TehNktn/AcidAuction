package ru.nokton.acidauction.util;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

public class Economy {
    private static net.milkbowl.vault.economy.Economy eco;

    public Economy() {
    }

    public static boolean canTake(UUID uuid, double money) {
        return eco.getBalance(getPlayer(uuid)) >= money;
    }

    public static void give(UUID uuid, double money) {
        eco.depositPlayer(getPlayer(uuid), money);
    }

    public static void take(UUID uuid, double money) {
        eco.withdrawPlayer(getPlayer(uuid), money);
    }

    private static OfflinePlayer getPlayer(UUID uuid) {
        return Bukkit.getOfflinePlayer(uuid);
    }

    static {
        RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> reg = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (reg != null) {
            eco = (net.milkbowl.vault.economy.Economy)reg.getProvider();
        }

    }
}
