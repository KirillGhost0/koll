package com.endlessbrood.common;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

/** Unique combat roles without spawning extra projectiles or entities. */
public final class BroodCombatEvents {
    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (!(event.entityLiving instanceof EntityParasite) || event.entityLiving.worldObj.isRemote ||
            event.ammount <= 0F) return;
        EntityParasite parasite = (EntityParasite) event.entityLiving;
        if (parasite.getMorph() == 2 && !event.source.isUnblockable()) {
            // Bulwark carapace. Fire and explosions still affect it.
            event.ammount *= 0.8F;
        }
        if (parasite.getMorph() == BroodColonyRules.QUEEN_MORPH &&
            event.source.getEntity() instanceof EntityLivingBase) {
            parasite.raiseLeaderDefenseAlarm((EntityLivingBase) event.source.getEntity());
        }
        if (parasite.getMorph() == 3 && BroodConfig.thornbackRetaliation) {
            Entity attacker = event.source.getEntity();
            if (attacker instanceof EntityLivingBase && !(attacker instanceof EntityParasite) &&
                parasite.getDistanceSqToEntity(attacker) < 9.0D) {
                // Cactus damage cannot cause infinite reflection (only parasites reflect).
                attacker.attackEntityFrom(DamageSource.cactus, 1.0F);
            }
        }
    }
}
