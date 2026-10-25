package com.endlessbrood.common;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class EvolutionTicker {
    @SubscribeEvent
    public void worldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.world.isRemote ||
            event.world.provider.dimensionId != 0) return;
        if (BroodOutbreakData.get(event.world).shouldGrow(event.world.getTotalWorldTime())) {
            EvolutionData.get(event.world).addPoints(BroodConfig.growthPoints);
        }
    }
}
