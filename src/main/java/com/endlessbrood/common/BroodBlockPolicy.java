package com.endlessbrood.common;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/** Pure rules, without any game calls. Unrecognized and modded blocks are DENIED. */
public final class BroodBlockPolicy {
    private static final Set<String> ABSOLUTE = new HashSet<String>(Arrays.asList(
        "minecraft:obsidian", "minecraft:bedrock", "minecraft:end_portal_frame"));
    private static final Set<String> SOFT = new HashSet<String>(Arrays.asList(
        "minecraft:dirt", "minecraft:grass", "minecraft:mycelium", "minecraft:sand",
        "minecraft:gravel", "minecraft:clay", "minecraft:leaves", "minecraft:leaves2",
        "minecraft:snow", "minecraft:snow_layer"));
    private static final Set<String> WOOD = new HashSet<String>(Arrays.asList(
        "minecraft:planks", "minecraft:log", "minecraft:log2", "minecraft:fence",
        "minecraft:wooden_door", "minecraft:glass", "minecraft:glass_pane",
        "minecraft:stained_glass", "minecraft:stained_glass_pane", "minecraft:wool"));
    private static final Set<String> STONE = new HashSet<String>(Arrays.asList(
        "minecraft:stone", "minecraft:cobblestone", "minecraft:stonebrick",
        "minecraft:brick_block", "minecraft:sandstone", "minecraft:nether_brick",
        "minecraft:hardened_clay", "minecraft:stained_hardened_clay"));

    private BroodBlockPolicy() { }

    public static boolean permits(String name, BigInteger stage, int gene,
                                  Set<String> protectedNames, int softStage,
                                  int woodStage, int stoneStage) {
        if (name == null || stage == null || stage.signum() < 0) return false;
        String id = name.toLowerCase(Locale.ROOT);
        if (ABSOLUTE.contains(id) || protectedNames.contains(id)) return false;
        if (SOFT.contains(id)) return stage.compareTo(BigInteger.valueOf(softStage)) >= 0;
        if (WOOD.contains(id)) return stage.compareTo(BigInteger.valueOf(woodStage)) >= 0;
        return STONE.contains(id) && gene == ParasiteGenes.CORROSIVE &&
            stage.compareTo(BigInteger.valueOf(stoneStage)) >= 0;
    }

    public static boolean isStone(String name) {
        return name != null && STONE.contains(name.toLowerCase(Locale.ROOT));
    }
}
