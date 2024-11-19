package ru.nokton.acidauction.data;

import java.util.Date;
import java.util.UUID;
import org.bukkit.inventory.ItemStack;
import ru.nokton.acidauction.util.Values;

public class ItemOffer {
    private ItemStack item;
    private double price;
    private Date date;
    private Date end;
    private UUID owner;

    public ItemOffer(ItemStack item, double price, UUID owner) {
        this.item = item;
        this.price = price;
        this.date = new Date();
        this.end = new Date(this.date.getTime() + Values.TIME * 1000L);
        this.owner = owner;
    }

    public ItemOffer(ItemStack item, double price, Date date, UUID owner) {
        this.item = item;
        this.price = price;
        this.date = date;
        this.end = new Date(date.getTime() + Values.TIME * 1000L);
        this.owner = owner;
    }

    public ItemStack getItem() {
        return this.item;
    }

    public double getPrice() {
        return this.price;
    }

    public double getSinglePrice() {
        return this.price / (double)this.item.getAmount();
    }

    public Date getDate() {
        return this.date;
    }

    public UUID getOwner() {
        return this.owner;
    }

    public boolean isExpired() {
        return this.end.getTime() < this.date.getTime();
    }

    public void buyOne() {
        if (this.item.getAmount() == 1) {
            this.buyFull();
        } else {
            int amount = this.item.getAmount();
            int newAmount = amount - 1;
            this.item.setAmount(newAmount);
            this.price = (double)newAmount * this.price / (double)amount;
            System.out.println("New price: " + this.price + ", single: " + this.getSinglePrice());
        }

    }

    public void buyFull() {
        Auction.removeOffer(this);
    }

    public String getRemainingTime() {
        String out = new String();
        long mls = this.end.getTime() - (new Date()).getTime();
        long sec = mls / 1000L % 60L;
        long min = mls / 1000L / 60L % 60L;
        long h = mls / 1000L / 60L / 60L % 24L;
        long d = mls / 1000L / 60L / 60L / 24L % 7L;
        long w = mls / 1000L / 60L / 60L / 24L / 7L;
        out = out + (w > 0L ? w + " нед. " : new String());
        out = out + (d > 0L ? d + " дн. " : new String());
        out = out + (h > 0L ? h + " ч. " : new String());
        out = out + (min > 0L ? min + " мин. " : new String());
        out = out + (sec > 0L ? sec + " сек." : new String());
        return out.isEmpty() ? "сейчас" : out.substring(0, out.length() - 1);
    }

    public String toString() {
        return this.item.toString();
    }
}