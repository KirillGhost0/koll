package com.endlessbrood.common;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class InfectionEvents {
    public static final String INFECTION = "endlessbroodIncubation";

    private boolean infectable(EntityLivingBase entity) {
        if (BroodConfig.preserveTamedAnimals && entity instanceof EntityTameable &&
            ((EntityTameable) entity).isTamed()) return false;
        return !(entity instanceof EntityParasite) &&
            (entity instanceof EntityAnimal || entity instanceof EntityVillager ||
             entity instanceof EntityZombie);
    }

    @SubscribeEvent
    public void onHurt(LivingHurtEvent event) {
        Entity attacker = event.source.getEntity();
        EntityLivingBase victim = event.entityLiving;
        if (victim.worldObj.isRemote || !(attacker instanceof EntityParasite)) return;
        if (victim instanceof EntityPlayer) {
            if (victim.getRNG().nextFloat() < 0.15F) {
                victim.addPotionEffect(new PotionEffect(Potion.poison.id, 80, 0));
            }
        } else if (infectable(victim) &&
                   !victim.getEntityData().hasKey(INFECTION) &&
                   victim.getRNG().nextDouble() < BroodConfig.infectionChance) {
            victim.getEntityData().setInteger(INFECTION, BroodConfig.effectiveIncubationTicks());
            // Visible potion particles provide a warning without a client-side packet.
            victim.addPotionEffect(new PotionEffect(Potion.weakness.id, 100, 0));
        }
    }

    @SubscribeEvent
    public void onDeath(LivingDeathEvent event) {
        EntityLivingBase victim = event.entityLiving;
        if (!victim.worldObj.isRemote && !(victim instanceof EntityParasite) &&
            event.source.getEntity() instanceof EntityParasite) {
            EvolutionData.get(victim.worldObj).addCombatPoints(BroodConfig.killPoints,
                victim.worldObj.getTotalWorldTime());
        }
    }

    @SubscribeEvent
    public void onUpdate(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase victim = event.entityLiving;
        if (victim.worldObj.isRemote || victim.ticksExisted % 20 != 0 ||
            !infectable(victim)) return;
        NBTTagCompound data = victim.getEntityData();
        if (!data.hasKey(INFECTION)) return;
        int remaining = data.getInteger(INFECTION) - 20;
        if (remaining > 0) {
            data.setInteger(INFECTION, remaining);
            return;
        }
        data.removeTag(INFECTION);
        World world = victim.worldObj;
        // Limit nearby population to prevent uncontrolled entity-lag.
        if (world.getEntitiesWithinAABB(EntityParasite.class,
                victim.boundingBox.expand(24.0D, 12.0D, 24.0D)).size() >=
                BroodConfig.localPopulationLimit) return;
        if (!(victim instanceof EntityLiving)) return;
        EntityParasite replacement = new EntityParasite(world);
        replacement.setLocationAndAngles(victim.posX, victim.posY, victim.posZ,
                                         victim.rotationYaw, victim.rotationPitch);
        if (world.spawnEntityInWorld(replacement)) {
            victim.setDead();
            EvolutionData.get(world).addCombatPoints(BroodConfig.infectionPoints,
                world.getTotalWorldTime());
        }
    }
}
