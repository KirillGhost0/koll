package com.endlessbrood.common;

import com.endlessbrood.EndlessBrood;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.world.World;

/** Survival counterplay to poison and parasitic infection, consumable. */
public final class ItemBroodSerum extends Item {
    public ItemBroodSerum() {
        setUnlocalizedName("endlessbrood.serum");
        setTextureName("endlessbrood:serum");
        setCreativeTab(CreativeTabs.tabBrewing);
        setMaxStackSize(16);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (stack == null || stack.stackSize <= 0) return stack;
        if (!world.isRemote && (player.isPotionActive(Potion.poison) ||
            player.isPotionActive(Potion.moveSlowdown))) {
            player.removePotionEffect(Potion.poison.id);
            player.removePotionEffect(Potion.moveSlowdown.id);
            if (!player.capabilities.isCreativeMode) --stack.stackSize;
        }
        return stack;
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world,
                             int x, int y, int z, int side,
                             float hitX, float hitY, float hitZ) {
        if (world.getBlock(x, y, z) != EndlessBrood.taintedSoil ||
            !player.canPlayerEdit(x, y, z, side, stack)) return false;
        if (!world.isRemote) {
            world.setBlock(x, y, z, Blocks.dirt, 0, 3);
            if (!player.capabilities.isCreativeMode) --stack.stackSize;
        }
        return true;
    }
}
