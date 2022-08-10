package io.github.tofodroid.mods.mimi.common.container.slot;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SlotDiskWriteResult extends Slot {
    private final CraftingContainer craftSlots;
    private final Player player;
    private int removeCount;

    public SlotDiskWriteResult(Player p_40166_, CraftingContainer p_40167_, Container p_40168_, int p_40169_, int p_40170_, int p_40171_) {
        super(p_40168_, p_40169_, p_40170_, p_40171_);
        this.player = p_40166_;
        this.craftSlots = p_40167_;
    }

    public boolean mayPlace(ItemStack p_40178_) {
        return false;
    }

    public ItemStack remove(int p_40173_) {
        if (this.hasItem()) {
            this.removeCount += Math.min(p_40173_, this.getItem().getCount());
        }
        return super.remove(p_40173_);
    }

    protected void onQuickCraft(ItemStack p_40180_, int p_40181_) {
        this.removeCount += p_40181_;
        this.checkTakeAchievements(p_40180_);
    }

    protected void onSwapCraft(int p_40183_) {
        this.removeCount += p_40183_;
    }

    protected void checkTakeAchievements(ItemStack p_40185_) {
        if (this.removeCount > 0) {
            p_40185_.onCraftedBy(this.player.level, this.player, this.removeCount);
            net.minecraftforge.event.ForgeEventFactory.firePlayerCraftingEvent(this.player, p_40185_, this.craftSlots);
        }

        this.removeCount = 0;
    }

    public void onTake(Player p_150638_, ItemStack p_150639_) {
        this.checkTakeAchievements(p_150639_);
        net.minecraftforge.common.ForgeHooks.setCraftingPlayer(p_150638_);
        net.minecraftforge.common.ForgeHooks.setCraftingPlayer(null);
        
        for(int i = 0; i < this.craftSlots.getContainerSize(); ++i) {
            ItemStack itemstack = this.craftSlots.getItem(i);
            
            if (!itemstack.isEmpty()) {
                this.craftSlots.removeItem(i, 1);
                itemstack = this.craftSlots.getItem(i);
            }
        }
    }
}