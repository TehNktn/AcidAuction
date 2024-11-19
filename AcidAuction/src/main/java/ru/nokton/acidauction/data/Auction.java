package ru.nokton.acidauction.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Auction {
    private static Map<UUID, List<ItemOffer>> offers = new HashMap();

    public Auction() {
    }

    public static List<ItemOffer> getOffers(UUID owner) {
        return (List)(offers.get(owner) == null ? new ArrayList() : (List)offers.get(owner));
    }

    public static void addOffer(ItemOffer offer) {
        if (containsOffers(offer.getOwner())) {
            List<ItemOffer> offs = getOffers(offer.getOwner());
            offs.add(offer);
            offers.put(offer.getOwner(), offs);
        } else {
            List<ItemOffer> offs = new ArrayList();
            offs.add(offer);
            offers.put(offer.getOwner(), offs);
        }
    }

    public static void removeOffer(ItemOffer offer) {
        if (containsOffer(offer)) {
            List<ItemOffer> offs = getOffers(offer.getOwner());
            offs.remove(offer);
            offers.put(offer.getOwner(), offs);
        }

    }

    public static boolean containsOffer(ItemOffer offer) {
        return offers.containsKey(offer.getOwner()) && getOffers(offer.getOwner()).contains(offer);
    }

    public static boolean containsOffers(UUID owner) {
        return offers.containsKey(owner) && getOffers(owner).size() > 0;
    }

    public static List<ItemOffer> getOffers() {
        List<ItemOffer> out = new ArrayList();
        Iterator var1 = Auction.offers.values().iterator();

        while(var1.hasNext()) {
            List<ItemOffer> offers = (List)var1.next();
            out.addAll(offers);
        }

        return out;
    }

    public static List<ItemOffer> getOffers(UUID... ignore) {
        List<ItemOffer> out = new ArrayList();
        List<UUID> ignored = Arrays.asList(ignore);
        Iterator var3 = Auction.offers.entrySet().iterator();

        while(var3.hasNext()) {
            Map.Entry<UUID, List<ItemOffer>> offers = (Map.Entry)var3.next();
            if (!ignored.contains(offers.getKey())) {
                out.addAll((Collection)offers.getValue());
            }
        }

        return out;
    }

    public static List<ItemOffer> getNoExpiredOffers(UUID... ignore) {
        List<ItemOffer> out = new ArrayList();
        List<UUID> ignored = Arrays.asList(ignore);
        Iterator var3 = Auction.offers.entrySet().iterator();

        while(true) {
            Map.Entry offers;
            do {
                if (!var3.hasNext()) {
                    return out;
                }

                offers = (Map.Entry)var3.next();
            } while(ignored.contains(offers.getKey()));

            Iterator var5 = ((List)offers.getValue()).iterator();

            while(var5.hasNext()) {
                ItemOffer offer = (ItemOffer)var5.next();
                if (!offer.isExpired()) {
                    out.add(offer);
                }
            }
        }
    }
}