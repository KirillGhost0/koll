package com.endlessbrood.common;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

/** Collective, world-saved resistance. No hard dependency on NTM or other mods. */
public final class BroodAdaptationEvents {
    @SubscribeEvent
    public void onParasiteHurt(LivingHurtEvent event) {
        if (!(event.entityLiving instanceof EntityParasite) || event.entityLiving.worldObj.isRemote ||
            !BroodConfig.adaptiveDefense || event.ammount <= 0.0F) return;
        int category = BroodAdaptiveMath.category(event.source.getDamageType(),
            event.source.isFireDamage(), event.source.isExplosion(), event.source.isProjectile());
        if (category < 0) return;
        EvolutionData data = EvolutionData.get(event.entityLiving.worldObj);
        float incoming = event.ammount;
        event.ammount = (float) Math.max(0.1D, incoming * (1.0D - data.getResistance(category)));
        // Only actual attempted hits count; the first attacks do not grant instant protection.
        data.recordExposure(category, incoming);
    }
}
