package com.endlessbrood.common;

import java.util.List;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

/**
 * Lightweight co-op defense tool. One local entity query per three seconds
 * at most; no tile entity, network packet or server-wide search required.
 */
public final class ItemBroodScanner extends Item {
    private static final String LAST_SCAN = "EndlessBroodLastScan";
    private static final int RADIUS = 32;
    private static final int COOLDOWN_TICKS = 60;

    public ItemBroodScanner() {
        setUnlocalizedName("endlessbrood.scanner");
        setTextureName("endlessbrood:scanner");
        setCreativeTab(CreativeTabs.tabTools);
        setMaxStackSize(1);
    }

    @Override
    @SuppressWarnings("unchecked")
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (world.isRemote) return stack;
        long now = world.getTotalWorldTime();
        long previous = player.getEntityData().getLong(LAST_SCAN);
        if (player.getEntityData().hasKey(LAST_SCAN) && now >= previous &&
            now - previous < COOLDOWN_TICKS) return stack;
        player.getEntityData().setLong(LAST_SCAN, now);
        AxisAlignedBB area = player.boundingBox.expand(RADIUS, 20.0D, RADIUS);
        List<EntityParasite> found = world.getEntitiesWithinAABB(EntityParasite.class, area);
        EntityParasite closest = null;
        double shortest = RADIUS * RADIUS;
        int count = 0;
        for (EntityParasite candidate : found) {
            if (candidate.isDead) continue;
            double dist = player.getDistanceSqToEntity(candidate);
            if (dist > RADIUS * RADIUS) continue;
            ++count;
            if (dist <= shortest) {
                closest = candidate;
                shortest = dist;
            }
        }
        if (closest == null) {
            player.addChatMessage(new ChatComponentText("[Brood Scanner] No parasites within 32 blocks."));
        } else {
            double dx = closest.posX - player.posX;
            double dz = closest.posZ - player.posZ;
            String direction = Math.abs(dx) > Math.abs(dz)
                ? (dx >= 0.0D ? "E" : "W") : (dz >= 0.0D ? "S" : "N");
            player.addChatMessage(new ChatComponentText("[Brood Scanner] " + count +
                " nearby; closest: " + Math.round(Math.sqrt(shortest)) +
                " blocks " + direction + "."));
        }
        return stack;
    }
}
