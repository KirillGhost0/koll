package com.endlessbrood.common;

import java.math.BigInteger;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.DimensionManager;

/** Persistent points are arbitrary precision: no final stage, no integer overflow. */
public class EvolutionData extends WorldSavedData {
    private static final String DATA_NAME = "endlessbrood_evolution";
    private BigInteger points = BigInteger.ZERO;
    private final long[] exposures = new long[BroodAdaptiveMath.CATEGORY_COUNT];
    private final BroodDailyBudget dailyCombatBudget = new BroodDailyBudget();

    public EvolutionData() {
        this(DATA_NAME);
    }

    public EvolutionData(String name) {
        super(name);
    }

    public static EvolutionData get(World fromWorld) {
        // Every dimension uses the overworld's world-save data when possible.
        World source = fromWorld;
        if (fromWorld.provider.dimensionId != 0) {
            WorldServer overworld = DimensionManager.getWorld(0);
            if (overworld != null) source = overworld;
        }
        EvolutionData data = (EvolutionData) source.loadItemData(EvolutionData.class, DATA_NAME);
        if (data == null) {
            data = new EvolutionData(DATA_NAME);
            source.setItemData(DATA_NAME, data);
        }
        return data;
    }

    public void addPoints(int amount) {
        if (amount <= 0) return;
        points = points.add(BigInteger.valueOf(amount));
        markDirty();
    }

    /** Event-based points share a single daily cap among all co-op players. */
    public int addCombatPoints(int requested, long worldTicks) {
        int granted = dailyCombatBudget.consume(requested, worldTicks,
            BroodConfig.combatPointsPerDay);
        addPoints(granted);
        return granted;
    }

    public int getCombatPointsToday(long worldTicks) {
        return dailyCombatBudget.getUsed(worldTicks);
    }

    public void recordExposure(int category, float incomingDamage) {
        if (category < 0 || category >= exposures.length || incomingDamage <= 0.0F ||
            Float.isNaN(incomingDamage)) return;
        long units = (long) Math.ceil(Math.min(1000.0F, incomingDamage) * 10.0D);
        exposures[category] = BroodAdaptiveMath.saturatingAdd(exposures[category], units);
        markDirty();
    }

    public double getResistance(int category) {
        if (category < 0 || category >= exposures.length) return 0.0D;
        return BroodAdaptiveMath.resistance(exposures[category], getStage(), BroodConfig.adaptationMaxResistance);
    }

    public long getExposure(int category) {
        return category < 0 || category >= exposures.length ? 0L : exposures[category];
    }

    public BigInteger getPoints() {
        return points;
    }

    public BigInteger getStage() {
        return points.divide(BroodConfig.effectiveEvolutionCost());
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        try {
            // The binary representation avoids NBT's 65 KB UTF string ceiling.
            // Older 0.1-0.9 worlds used the "Points" string key and still load.
            if (nbt.hasKey("PointsBinary", 7)) {
                byte[] encoded = nbt.getByteArray("PointsBinary");
                points = encoded.length == 0 ? BigInteger.ZERO : new BigInteger(encoded);
            } else {
                points = new BigInteger(nbt.getString("Points"));
            }
            if (points.signum() < 0) points = BigInteger.ZERO;
        } catch (NumberFormatException exception) {
            points = BigInteger.ZERO;
        }
        for (int i = 0; i < exposures.length; ++i) {
            exposures[i] = Math.max(0L, nbt.getLong("Exposure" + i));
        }
        dailyCombatBudget.restore(nbt.getLong("CombatBudgetDay"),
            nbt.getInteger("CombatBudgetUsed"));
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        nbt.setByteArray("PointsBinary", points.toByteArray());
        nbt.setLong("CombatBudgetDay", dailyCombatBudget.getDay());
        nbt.setInteger("CombatBudgetUsed", dailyCombatBudget.getPersistedUsed());
        for (int i = 0; i < exposures.length; ++i) {
            nbt.setLong("Exposure" + i, exposures[i]);
        }
    }
}
