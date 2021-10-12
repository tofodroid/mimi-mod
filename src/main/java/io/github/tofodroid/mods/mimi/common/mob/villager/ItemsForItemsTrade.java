package io.github.tofodroid.mods.mimi.common.mob.villager;

import java.util.List;
import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.entity.merchant.villager.VillagerTrades;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MerchantOffer;
import net.minecraft.item.Item;

public class ItemsForItemsTrade implements VillagerTrades.ITrade {
   private final List<? extends Item> sellItems;
   private final int sellCount;
   private final List<? extends Item> buyItems;
   private final int buyCount;
   private final int maxUses;
   private final int xpValue;
   private final float priceMultiplier;

   public ItemsForItemsTrade(List<? extends Item> sellItems, int sellCount, List<? extends Item> buyItems, int buyCount, int maxUsesIn, int xpValueIn) {
      this.sellItems = sellItems;
      this.sellCount = sellCount;
      this.buyItems = buyItems;
      this.buyCount = buyCount;
      this.maxUses = maxUsesIn;
      this.xpValue = xpValueIn;
      this.priceMultiplier = 0.05F;
   }

   public MerchantOffer getOffer(Entity trader, Random rand) {
      return new MerchantOffer(new ItemStack(buyItems.get(rand.nextInt(buyItems.size())).asItem(), buyCount),  new ItemStack(sellItems.get(rand.nextInt(sellItems.size())).asItem(), sellCount), this.maxUses, this.xpValue, this.priceMultiplier);
   }
}