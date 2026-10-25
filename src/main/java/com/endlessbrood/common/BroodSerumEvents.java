package com.endlessbrood.common;

import com.endlessbrood.EndlessBrood;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.EntityInteractEvent;

/** A serum also cures incubating animals and villagers before conversion. */
public final class BroodSerumEvents {
    @SubscribeEvent
    public void onInteract(EntityInteractEvent event) {
        EntityPlayer player = event.entityPlayer;
        ItemStack held = player.getCurrentEquippedItem();
        if (held == null || held.getItem() != EndlessBrood.serum ||
            !(event.target instanceof EntityLivingBase)) return;
        EntityLivingBase creature = (EntityLivingBase) event.target;
        if (creature.getEntityData().hasKey(InfectionEvents.INFECTION)) {
            if (!player.worldObj.isRemote) {
                creature.getEntityData().removeTag(InfectionEvents.INFECTION);
                if (!player.capabilities.isCreativeMode) --held.stackSize;
                if (held.stackSize <= 0) player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
            }
            event.setCanceled(true);
        }
    }
}
