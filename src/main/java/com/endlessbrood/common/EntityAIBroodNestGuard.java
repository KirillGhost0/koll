package com.endlessbrood.common;

import com.endlessbrood.EndlessBrood;
import net.minecraft.entity.ai.EntityAIBase;

/**
 * Returns nest-born parasites to their own colony only when they are idle.
 * Does not search for nests, iterate chunks, or keep chunks loaded.
 */
public final class EntityAIBroodNestGuard extends EntityAIBase {
    private final EntityParasite parasite;
    private int cooldown;
    private int travelTicks;

    public EntityAIBroodNestGuard(EntityParasite parasite) {
        this.parasite = parasite;
        this.cooldown = parasite.getRNG().nextInt(100) + 60;
        setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        if (--cooldown > 0) return false;
        cooldown = 180 + parasite.getRNG().nextInt(80);
        if (parasite.worldObj.isRemote || !BroodConfig.colonyGuardAI ||
            !parasite.hasBroodHome() || parasite.getAttackTarget() != null) return false;

        int x = parasite.getBroodHomeX(), y = parasite.getBroodHomeY(),
            z = parasite.getBroodHomeZ();
        // Check loaded chunks first. An unloaded home never causes a chunk load.
        if (!parasite.worldObj.blockExists(x, y, z)) return false;
        if (parasite.worldObj.getBlock(x, y, z) != EndlessBrood.nest) {
            parasite.clearBroodHome();
            return false;
        }
        int radius = BroodConfig.colonyGuardReturnRadius;
        return BroodTerritoryRules.shouldReturn(parasite.posX, parasite.posZ,
            x, z, radius);
    }

    @Override
    public void startExecuting() {
        travelTicks = 180;
        parasite.getNavigator().tryMoveToXYZ(
            parasite.getBroodHomeX() + 0.5D, parasite.getBroodHomeY() + 1.0D,
            parasite.getBroodHomeZ() + 0.5D, 0.85D);
    }

    @Override
    public boolean continueExecuting() {
        return travelTicks-- > 0 && parasite.getAttackTarget() == null &&
            parasite.hasBroodHome() && !parasite.getNavigator().noPath();
    }

    @Override
    public void resetTask() {
        parasite.getNavigator().clearPathEntity();
    }
}
