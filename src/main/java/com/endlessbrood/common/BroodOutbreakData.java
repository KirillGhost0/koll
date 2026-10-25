package com.endlessbrood.common;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;

/**
 * Installation time and starting-base positions saved to the world's map data.
 * No chunks are loaded to calculate safe distances.
 */
public final class BroodOutbreakData extends WorldSavedData {
    private static final String DATA_NAME = "endlessbrood_outbreak";
    private static final int MAX_PLAYER_ANCHORS = 16;
    private static final int MAX_STORED_SEEDS = 16;

    private long installationTick = -1L;
    private final List<Anchor> anchors = new ArrayList<Anchor>();
    private final List<Anchor> retroSeeds = new ArrayList<Anchor>();

    private static final class Anchor {
        final String id;
        final int x;
        final int z;
        Anchor(String id, int x, int z) { this.id = id; this.x = x; this.z = z; }
    }

    public BroodOutbreakData() { super(DATA_NAME); }
    public BroodOutbreakData(String id) { super(id); }

    public static BroodOutbreakData get(World world) {
        BroodOutbreakData result = (BroodOutbreakData) world.loadItemData(BroodOutbreakData.class, DATA_NAME);
        if (result == null) {
            result = new BroodOutbreakData(DATA_NAME);
            world.setItemData(DATA_NAME, result);
        }
        result.initialize(world);
        return result;
    }

    private void initialize(World world) {
        if (installationTick >= 0L) return;
        installationTick = Math.max(0L, world.getTotalWorldTime());
        ChunkCoordinates spawn = world.getSpawnPoint();
        if (spawn != null) anchors.add(new Anchor("spawn", spawn.posX, spawn.posZ));
        markDirty();
    }

    public boolean graceExpired(long worldTick) {
        return BroodOutbreakRules.graceExpired(worldTick, installationTick,
            BroodConfig.threatGraceDays);
    }

    public boolean shouldGrow(long worldTick) {
        return BroodOutbreakRules.shouldGrow(worldTick, installationTick,
            BroodConfig.threatGraceDays, BroodConfig.growthInterval);
    }

    /** Record each person's first co-op login during the initial grace period. */
    public void rememberPlayers(World world) {
        if (graceExpired(world.getTotalWorldTime())) return;
        for (Object entry : world.playerEntities) {
            if (!(entry instanceof EntityPlayer)) continue;
            EntityPlayer player = (EntityPlayer) entry;
            String id = "player:" + player.getUniqueID().toString();
            if (hasAnchor(id) || anchors.size() >= MAX_PLAYER_ANCHORS + 1) continue;
            addAnchor(id, (int) Math.floor(player.posX), (int) Math.floor(player.posZ));
        }
    }

    private boolean hasAnchor(String id) {
        for (Anchor anchor : anchors) if (id.equals(anchor.id)) return true;
        return false;
    }

    private void addAnchor(String id, int x, int z) {
        if (hasAnchor(id)) return;
        anchors.add(new Anchor(id, x, z));
        markDirty();
    }

    /** True outside both world spawn and all remembered co-op starting bases. */
    public boolean farFromBases(double x, double z) {
        for (Anchor anchor : anchors) {
            if (!BroodOutbreakRules.outsideRadius(x, z, anchor.x, anchor.z,
                    BroodConfig.initialOutbreakDistance)) return false;
        }
        return true;
    }

    /** Avoid popping a new hive into existence right beside a moving player. */
    public boolean farFromOnlinePlayers(World world, double x, double z) {
        for (Object entry : world.playerEntities) {
            if (!(entry instanceof EntityPlayer)) continue;
            EntityPlayer player = (EntityPlayer) entry;
            if (!BroodOutbreakRules.outsideRadius(x, z, player.posX, player.posZ,
                    BroodConfig.outbreakPlayerBuffer)) return false;
        }
        return true;
    }

    public boolean maySeedAt(World world, int x, int z) {
        return graceExpired(world.getTotalWorldTime()) && farFromBases(x, z)
            && farFromOnlinePlayers(world, x, z);
    }

    public int retroSeedCount() { return retroSeeds.size(); }

    public boolean farFromExistingRetroSeeds(int x, int z) {
        for (Anchor seed : retroSeeds) {
            if (!BroodOutbreakRules.outsideRadius(x, z, seed.x, seed.z, 160)) return false;
        }
        return true;
    }

    public void recordRetroSeed(int x, int z) {
        if (retroSeeds.size() >= MAX_STORED_SEEDS) return;
        retroSeeds.add(new Anchor("retro", x, z));
        markDirty();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        installationTick = nbt.hasKey("InstalledTick") ? nbt.getLong("InstalledTick") : -1L;
        anchors.clear();
        retroSeeds.clear();
        readAnchors(nbt.getTagList("Anchors", 10), anchors, MAX_PLAYER_ANCHORS + 1);
        readAnchors(nbt.getTagList("RetroSeeds", 10), retroSeeds, MAX_STORED_SEEDS);
    }

    private static void readAnchors(NBTTagList tags, List<Anchor> output, int max) {
        for (int i = 0; i < tags.tagCount() && output.size() < max; ++i) {
            NBTTagCompound tag = tags.getCompoundTagAt(i);
            output.add(new Anchor(tag.getString("Id"), tag.getInteger("X"), tag.getInteger("Z")));
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        nbt.setLong("InstalledTick", installationTick);
        nbt.setTag("Anchors", serialize(anchors));
        nbt.setTag("RetroSeeds", serialize(retroSeeds));
    }

    private static NBTTagList serialize(List<Anchor> list) {
        NBTTagList result = new NBTTagList();
        for (Anchor anchor : list) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("Id", anchor.id);
            tag.setInteger("X", anchor.x);
            tag.setInteger("Z", anchor.z);
            result.appendTag(tag);
        }
        return result;
    }
}
