package com.endlessbrood.common;

import java.util.List;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/** Six creative-only testing eggs. No vanilla/global entity IDs; works on Forge servers. */
public final class ItemBroodEgg extends Item {
    private static final String[] NAMES = { "stalker", "scuttler", "bulwark", "thornback", "colossus", "matriarch" };

    public ItemBroodEgg() {
        setHasSubtypes(true);
        setMaxDamage(0);
        setMaxStackSize(64);
        setCreativeTab(CreativeTabs.tabMisc);
        setUnlocalizedName("endlessbrood.parasiteEgg");
        setTextureName("endlessbrood:parasite_egg");
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        int meta = stack.getItemDamage();
        if (meta < 0 || meta >= NAMES.length) meta = 0;
        return "item.endlessbrood.parasiteEgg." + NAMES[meta];
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void getSubItems(Item item, CreativeTabs tab, List list) {
        for (int i = 0; i < NAMES.length; ++i) list.add(new ItemStack(item, 1, i));
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world,
                             int x, int y, int z, int side,
                             float hitX, float hitY, float hitZ) {
        if (stack == null || stack.stackSize <= 0 ||
            stack.getItemDamage() < 0 || stack.getItemDamage() >= NAMES.length ||
            !player.canPlayerEdit(x, y, z, side, stack)) return false;
        switch (side) {
            case 0: --y; break;
            case 1: ++y; break;
            case 2: --z; break;
            case 3: ++z; break;
            case 4: --x; break;
            case 5: ++x; break;
            default: return false;
        }
        if (y < 1 || y > 254) return false;
        if (world.isRemote) return true;
        EntityParasite parasite = new EntityParasite(world);
        parasite.setLocationAndAngles(x + 0.5D, y, z + 0.5D, player.rotationYaw, 0F);
        parasite.configureSpawnMorph(stack.getItemDamage());
        if (!world.checkNoEntityCollision(parasite.boundingBox) ||
            !world.getCollidingBoundingBoxes(parasite, parasite.boundingBox).isEmpty() ||
            !world.spawnEntityInWorld(parasite)) return false;
        if (!player.capabilities.isCreativeMode) --stack.stackSize;
        return true;
    }
}
