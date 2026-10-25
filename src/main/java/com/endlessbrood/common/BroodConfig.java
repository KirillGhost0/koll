package com.endlessbrood.common;

import java.io.File;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import net.minecraftforge.common.config.Configuration;

public final class BroodConfig {
    /** Base value remains 100 for compatibility with configuration files from 0.1.0. */
    public static int evolutionCost;
    /** A 3x multiplier is applied uniformly to passive, kill and conversion progress. */
    public static int evolutionSlowdownMultiplier;
    public static int growthInterval;
    public static int growthPoints;
    public static int killPoints;
    public static int combatPointsPerDay;
    public static int infectionPoints;
    public static int infectionTicks;
    public static int incubationSlowdownMultiplier;
    public static int spawnWeight;
    public static double infectionChance;
    public static int localPopulationLimit;
    public static double adaptationMaxResistance;
    public static boolean adaptiveDefense;
    public static boolean preserveTamedAnimals;
    public static boolean nestsEnabled;
    public static int nestChancePer10000Chunks;
    public static int nestPulseTicks;
    public static int nestSpawnUnlockStage;
    public static int nestPopulationLimit;
    public static boolean taintedSoilSpread;
    public static boolean nestExpansion;
    public static int nestExpansionChance;
    public static boolean wardsEnabled;
    public static int wardRadius;
    public static int wardPulseTicks;
    public static boolean scuttlerPounce;
    public static boolean thornbackRetaliation;
    public static boolean colossusRoar;
    public static boolean colonyTiers;
    public static int matureNestStage;
    public static int citadelNestStage;
    public static int nestUpgradeChance;
    public static int queenUnlockStage;
    public static int queenSpawnChance;
    public static int leaderRallyRadius;
    public static int leaderRallyTicks;
    public static boolean villageRaids;
    public static int raidUnlockStage;
    public static int raidSearchRadius;
    public static int raidSearchInterval;
    public static boolean nestDefense;
    public static int nestGuardRadius;
    public static int nestGuardLimit;
    public static boolean queenHeals;
    public static int threatGraceDays;
    public static int initialOutbreakDistance;
    public static int outbreakPlayerBuffer;
    public static boolean retroactiveDistantNests;
    public static int retroSeedLimit;
    public static int retroSeedIntervalTicks;
    public static boolean blockNaturalSpawnInHomeZones;
    public static boolean wardsBlockNaturalSpawns;
    public static boolean colonyGuardAI;
    public static int colonyGuardReturnRadius;
    public static boolean leaderDefenseAlarm;
    public static int leaderDefenseRadius;
    public static int leaderDefenseLimit;

    public static boolean allowTerrainDamage;
    public static boolean dropDestroyedBlocks;
    public static int blockDigTicks;
    public static int softDigStage;
    public static int woodenDigStage;
    public static int stoneDigStage;
    public static Set<String> protectedBlocks = Collections.emptySet();

    private BroodConfig() { }

    public static BigInteger effectiveEvolutionCost() {
        return BigInteger.valueOf(evolutionCost).multiply(BigInteger.valueOf(evolutionSlowdownMultiplier));
    }

    public static int effectiveIncubationTicks() {
        return infectionTicks * incubationSlowdownMultiplier;
    }

    public static void load(File file) {
        Configuration config = new Configuration(file);
        config.load();
        evolutionCost = config.getInt("pointsPerStage", "evolution", 100, 1, 1000000,
            "BASE points per stage (0.1.0-compatible). Effective cost = this value * evolutionSlowdownMultiplier.");
        evolutionSlowdownMultiplier = config.getInt("evolutionSlowdownMultiplier", "evolution", 3, 1, 100,
            "3 = THREE TIMES slower stage progression from ALL point sources. For slower NTM packs try 5 or 10.");
        growthInterval = config.getInt("ticksPerGrowth", "evolution", 600, 20, 24000,
            "Base passive progress interval in ticks. One second is 20 ticks at 20 TPS.");
        growthPoints = config.getInt("passiveGrowthPoints", "evolution", 1, 1, 10000,
            "Base passive point gain.");
        killPoints = config.getInt("pointsPerKill", "evolution", 25, 0, 10000,
            "Points gained when a parasite kills another mob.");
        combatPointsPerDay = config.getInt("combatPointsPerDay", "evolution", 90, 0, 100000,
            "Shared 24000-tick budget for kill+transformation points, per world. 0 disables combat progression (passive growth still continues). Prevents mob farms from skipping NTM's slow tech progression.");
        infectionPoints = config.getInt("pointsPerTransformation", "evolution", 10, 0, 10000,
            "Points gained when an infected creature transforms.");
        infectionTicks = config.getInt("incubationTicks", "infection", 600, 40, 12000,
            "BASE infection incubation, in ticks. Effective duration is multiplied by incubationSlowdownMultiplier.");
        incubationSlowdownMultiplier = config.getInt("incubationSlowdownMultiplier", "infection", 3, 1, 20,
            "3 = incubation takes 90 seconds instead of 30 seconds (at 20 TPS).");
        infectionChance = config.getFloat("infectionChance", "infection", 0.35F, 0F, 1F,
            "Chance to infect a supported creature per successful attack.");
        spawnWeight = config.getInt("naturalSpawnWeight", "spawning", 5, 0, 100,
            "Relative spawning weight against other monsters. Set to 0 to disable natural spawns.");
        localPopulationLimit = config.getInt("localParasiteLimit", "spawning", 12, 1, 80,
            "Maximum nearby parasites for natural spawning and infection transformations.");

        adaptiveDefense = config.getBoolean("adaptiveDefense", "mutations", true,
            "Collective resistance to fire, explosions, ranged hits and radiation-named damage. No HBM dependency.");
        adaptationMaxResistance = config.getFloat("maxAdaptiveResistance", "mutations", 0.6F, 0F, 0.75F,
            "Damage reduction ceiling: 0.6 means at most 60 percent, never full immunity.");
        preserveTamedAnimals = config.getBoolean("preserveTamedAnimals", "infection", true,
            "Prevent conversion of tamed wolves, cats and other tameable pets.");
        nestsEnabled = config.getBoolean("enableNests", "colonies", true,
            "Generate rare nest structures and enable nesting behavior.");
        nestChancePer10000Chunks = config.getInt("nestChancePer10000Chunks", "colonies", 65, 0, 10000,
            "Approximate chunks with a nest per 10000, in new overworld terrain only.");
        nestPulseTicks = config.getInt("nestPulseTicks", "colonies", 2400, 200, 24000,
            "Nest pulse timer in ticks. 2400 = 2 minutes at 20 TPS.");
        nestSpawnUnlockStage = config.getInt("nestSpawnUnlockStage", "colonies", 3, 0, 1000000,
            "Nests won't spawn parasites before this evolution stage.");
        nestPopulationLimit = config.getInt("nestLocalPopulationLimit", "colonies", 8, 1, 32,
            "Maximum parasites within 20 blocks from a nest.");
        nestExpansion = config.getBoolean("nestExpansion", "colonies", true,
            "Allow established nests at stage 40+ to establish a very rare daughter nest nearby.");
        nestExpansionChance = config.getInt("nestExpansionChancePerPulse", "colonies", 120, 5, 10000,
            "Chance 1/N per nest pulse to attempt establishing a daughter nest. Default 1/120 = about every four loaded-world hours per nest.");
        taintedSoilSpread = config.getBoolean("taintedSoilSpread", "colonies", true,
            "Slowly spread custom infected soil around nests, bounded by soil age and vanilla soil allowlist.");
        wardsEnabled = config.getBoolean("wardsEnabled", "defense", true,
            "Craftable iron/redstone/chitin ward: weakens nearby parasites and reclaims tainted soil.");
        wardRadius = config.getInt("wardRadius", "defense", 8, 3, 12,
            "Effective radius of a ward. Kept small to avoid large scans.");
        wardPulseTicks = config.getInt("wardPulseTicks", "defense", 120, 60, 600,
            "Time between ward pulses in game ticks (120 = 6 seconds).");
        scuttlerPounce = config.getBoolean("scuttlerPounce", "mobAbilities", true,
            "The small scuttler can occasionally leap at prey from a medium distance.");
        thornbackRetaliation = config.getBoolean("thornbackRetaliation", "mobAbilities", true,
            "Thornbacks can deal 1 point of retaliatory contact damage.");
        colossusRoar = config.getBoolean("colossusRoar", "mobAbilities", true,
            "Rare late-game colossi debuff nearby prey instead of creating destructive explosions.");

        colonyTiers = config.getBoolean("enableColonyTiers", "colonies", true,
            "Single nest block upgrades from Seed -> Mature -> Citadel, preserving tier as metadata.");
        matureNestStage = config.getInt("matureNestStage", "colonies", 12, 0, 1000000,
            "Global stage required before a seed nest can mature.");
        citadelNestStage = config.getInt("citadelNestStage", "colonies", 50, 0, 1000000,
            "Global stage required before a mature nest can become a citadel.");
        nestUpgradeChance = config.getInt("nestUpgradeChancePerPulse", "colonies", 16, 1, 10000,
            "Chance 1/N per nest pulse to advance one tier after the stage threshold.");
        queenUnlockStage = config.getInt("queenUnlockStage", "colonies", 60, 0, 1000000,
            "First stage eligible to produce a brood matriarch at citadel nests.");
        queenSpawnChance = config.getInt("queenSpawnChancePerPulse", "colonies", 30, 1, 10000,
            "Chance 1/N at an unprotected citadel's successful spawn opportunity. At most one matriarch nearby.");
        leaderRallyRadius = config.getInt("leaderRallyRadius", "mobAbilities", 12, 4, 18,
            "Leader buffs nearby parasites only; no world-wide search.");
        leaderRallyTicks = config.getInt("leaderRallyPeriodTicks", "mobAbilities", 200, 100, 1200,
            "Ticks between a matriarch's rally pulses; 200 = 10 seconds.");

        threatGraceDays = config.getInt("threatGraceDays", "evolution", 3, 0, 30,
            "No nests, natural/colony parasites or passive evolution for this many 20-minute Minecraft days after FIRST INSTALLATION into a world, including old saves. Previously saved values remain unchanged. Spawn eggs bypass this.");
        initialOutbreakDistance = config.getInt("initialOutbreakDistance", "outbreak", 768, 128, 4096,
            "Minimum horizontal distance in blocks between INITIAL outbreaks and world spawn / each co-op player's first-login position during the grace period.");
        outbreakPlayerBuffer = config.getInt("outbreakPlayerBuffer", "outbreak", 96, 0, 256,
            "Initial nests must also be this far from any currently online player. Does NOT protect a moving player against existing parasites.");
        retroactiveDistantNests = config.getBoolean("retroactiveDistantNests", "outbreak", true,
            "Allow a few initial nests to appear in ALREADY GENERATED and currently loaded distant chunks. No forced chunk loads.");
        retroSeedLimit = config.getInt("retroSeedLimit", "outbreak", 2, 0, 12,
            "Maximum number of special distant starter nests generated by the retroactive system. Natural new-chunk nests have their own rarity.");
        retroSeedIntervalTicks = config.getInt("retroSeedIntervalTicks", "outbreak", 1200, 200, 24000,
            "How often to attempt one retroactive distant starter nest while someone explores far from their starting base.");
        blockNaturalSpawnInHomeZones = config.getBoolean("blockNaturalSpawnInHomeZones", "outbreak", true,
            "Prevent RANDOM natural parasite spawn inside remembered starting-base areas. Parasites and infection may STILL spread into these areas from external colonies.");
        wardsBlockNaturalSpawns = config.getBoolean("wardsBlockNaturalSpawns", "defense", true,
            "Prevent naturally spawning parasites in the ward's protected area, in addition to existing colony/dig protection.");
        colonyGuardAI = config.getBoolean("colonyGuardAI", "mobAbilities", true,
            "Nest-born parasites return toward their recorded nest if idle and too far away. No scan for nests or unloaded chunks.");
        colonyGuardReturnRadius = config.getInt("colonyGuardReturnRadius", "performance", 18, 8, 32,
            "Distance at which an idle colony member tries returning home (checks approximately every 10 seconds).");
        leaderDefenseAlarm = config.getBoolean("leaderDefenseAlarm", "mobAbilities", true,
            "Hurting a matriarch can attract a limited number of already-spawned nearby defenders. No new mobs created.");
        leaderDefenseRadius = config.getInt("leaderDefenseRadius", "performance", 18, 6, 24,
            "Maximum radius of a matriarch's emergency defense alarm.");
        leaderDefenseLimit = config.getInt("leaderDefenseLimit", "performance", 6, 1, 12,
            "Maximum number of existing parasites redirected by a matriarch's alarm.");
        villageRaids = config.getBoolean("villageRaids", "mobAbilities", true,
            "After unlocking, idle parasites may approach a nearby inhabited village at night. No blocks are changed by the raid itself.");
        raidUnlockStage = config.getInt("villageRaidUnlockStage", "mobAbilities", 20, 0, 1000000,
            "First global stage when late-night village navigation is available.");
        raidSearchRadius = config.getInt("villageRaidSearchRadius", "performance", 48, 16, 64,
            "Maximum radius of a late-game village lookup; never loads destination chunks.");
        raidSearchInterval = config.getInt("villageRaidSearchIntervalTicks", "performance", 800, 200, 6000,
            "Cooldown between village lookups by a single idle parasite; adds random jitter.");
        nestDefense = config.getBoolean("nestDefense", "mobAbilities", true,
            "Clicking/attacking a nest provokes a limited number of nearby guards.");
        nestGuardRadius = config.getInt("nestGuardRadius", "performance", 18, 8, 24,
            "Local radius for guards that defend an attacked nest.");
        nestGuardLimit = config.getInt("nestGuardLimit", "performance", 6, 1, 12,
            "Maximum defenders assigned to a nest aggressor per alert.");
        queenHeals = config.getBoolean("matriarchHealing", "mobAbilities", true,
            "The queen's existing local rally also gently heals up to 4 injured allies per pulse.");

        allowTerrainDamage = config.getBoolean("allowTerrainDamage", "blockBreaking", true,
            "Let evolved parasites dig their limited vanilla-block allowlist to pursue a nearby target. Respects mobGriefing gamerule.");
        dropDestroyedBlocks = config.getBoolean("dropDestroyedBlocks", "blockBreaking", false,
            "Drop items when parasites chew a block. False reduces item-entity lag during attacks.");
        blockDigTicks = config.getInt("baseDigTicks", "blockBreaking", 100, 20, 1200,
            "Minimum sustained time (in ticks) to break one permitted block. Stone takes twice as long.");
        softDigStage = config.getInt("softDigUnlockStage", "blockBreaking", 6, 0, 1000000,
            "First stage that can dig dirt, sand, gravel, leaves, snow, etc.");
        woodenDigStage = config.getInt("woodDigUnlockStage", "blockBreaking", 20, 0, 1000000,
            "First stage that can dig wood, glass and wool.");
        stoneDigStage = config.getInt("stoneDigUnlockStage", "blockBreaking", 60, 0, 1000000,
            "First stage when the CORROSIVE mutation can dig basic stone and bricks.");
        String[] ids = config.getStringList("protectedBlocks", "blockBreaking", new String[] {
            "minecraft:obsidian", "minecraft:bedrock", "minecraft:end_portal_frame",
            "minecraft:iron_block", "minecraft:diamond_block", "minecraft:emerald_block"
        }, "Additional immune blocks by registry ID, one per line. Every non-allowlisted mod block, including HBM/NTM blocks, is protected by default.");
        Set<String> names = new HashSet<String>();
        for (String name : ids) {
            String id = name.trim().toLowerCase(Locale.ROOT);
            if (!id.isEmpty()) names.add(id);
        }
        protectedBlocks = Collections.unmodifiableSet(names);
        if (config.hasChanged()) config.save();
    }
}
