package test;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Random;
import java.util.HashSet;
import com.endlessbrood.common.BroodBlockPolicy;
import com.endlessbrood.common.EvolutionMath;
import com.endlessbrood.common.ParasiteGenes;
import com.endlessbrood.common.ParasiteGenome;
import com.endlessbrood.common.BroodAdaptiveMath;
import com.endlessbrood.common.BroodDailyBudget;
import com.endlessbrood.common.BroodColonyRules;
import com.endlessbrood.common.BroodRaidRules;
import com.endlessbrood.common.BroodPacingRules;
import com.endlessbrood.common.BroodTerritoryRules;
import com.endlessbrood.common.BroodOutbreakRules;

/** Deterministic assertions for protection, mutation gates and huge stages. */
public final class BroodRulesSmoke {
    private static void assertYes(boolean b, String reason) { if (!b) throw new AssertionError(reason); }

    private static boolean dig(String block, long stage, int gene) {
        return BroodBlockPolicy.permits(block, BigInteger.valueOf(stage), gene,
            Collections.<String>emptySet(), 6, 20, 60);
    }

    public static void main(String[] args) {
        long[] stages = {0, 5, 6, 20, 30, 60, 1000, Long.MAX_VALUE};
        for (long stage : stages) {
            for (int gene = 0; gene < 4; ++gene) {
                assertYes(!dig("minecraft:obsidian", stage, gene), "obsidian vulnerable");
                assertYes(!dig("minecraft:bedrock", stage, gene), "bedrock vulnerable");
                assertYes(!dig("minecraft:end_portal_frame", stage, gene), "portal vulnerable");
                assertYes(!dig("hbm:reactor", stage, gene), "NTM vulnerable");
                assertYes(!dig("custommod:reinforced_wall", stage, gene), "other mod vulnerable");
                assertYes(!dig("minecraft:chest", stage, gene), "chest vulnerable");
            }
        }
        assertYes(!dig("minecraft:dirt", 5, 0), "premature soft digging");
        assertYes(dig("minecraft:dirt", 6, 0), "soft stage 6");
        assertYes(!dig("minecraft:planks", 19, 0), "premature wood digging");
        assertYes(dig("minecraft:planks", 20, 0), "wood stage 20");
        assertYes(!dig("minecraft:stone", 60, ParasiteGenes.HUNTER), "ordinary stone dig");
        assertYes(dig("minecraft:stone", 60, ParasiteGenes.CORROSIVE), "corrosive stone stage 60");
        assertYes(!dig("minecraft:stone", 59, ParasiteGenes.CORROSIVE), "premature stone dig");
        assertYes(!BroodBlockPolicy.permits("minecraft:stone", BigInteger.valueOf(100000),
            ParasiteGenes.CORROSIVE, new HashSet<String>(Collections.singletonList("minecraft:stone")), 6, 20, 60),
            "custom blacklist ignored");
        BigInteger massiveStage = BigInteger.TEN.pow(3000);
        assertYes(!BroodBlockPolicy.permits("minecraft:obsidian", massiveStage,
            ParasiteGenes.CORROSIVE, Collections.<String>emptySet(), 6, 20, 60),
            "obsidian vulnerable at 10^3000");
        assertYes(Double.isFinite(EvolutionMath.logOnePlus(massiveStage)), "BigInteger log overflow");
        assertYes(ParasiteGenes.roll(BigInteger.ZERO, new Random(22)) == 0, "mutation at stage 0");
        for (int i = 0; i < 300; i++) {
            int gene = ParasiteGenes.roll(massiveStage, new Random(i));
            assertYes(gene >= 0 && gene <= 3, "unknown gene");
        }
        assertYes(BroodAdaptiveMath.category("hbm_radiation", false, false, false) ==
            BroodAdaptiveMath.RADIATION, "radiation type");
        assertYes(BroodAdaptiveMath.category("onFire", true, false, false) ==
            BroodAdaptiveMath.FIRE, "fire type");
        assertYes(BroodAdaptiveMath.resistance(100000000L, massiveStage, .6D) <= .6D,
            "unlimited resistance");
        assertYes(BroodAdaptiveMath.resistance(0L, massiveStage, .6D) == 0.0D,
            "resistance without exposure");
        assertYes(BroodAdaptiveMath.saturatingAdd(Long.MAX_VALUE - 2, 100) ==
            Long.MAX_VALUE, "experience overflow");
        assertYes(ParasiteGenome.roll(BigInteger.ZERO, new Random(12)) == 0,
            "early-game genome not locked");
        for (int i = 0; i < 100; ++i) {
            int traits = ParasiteGenome.roll(massiveStage, new Random(i));
            assertYes((traits & ~ParasiteGenome.ALL) == 0, "unknown trait bits");
        }
        BroodDailyBudget cap = new BroodDailyBudget();
        assertYes(cap.consume(25, 0, 90) == 25, "first kill progress");
        assertYes(cap.consume(75, 500, 90) == 65, "daily combat limit not enforced");
        assertYes(cap.consume(100, 23000, 90) == 0, "combat limit exploitable");
        assertYes(cap.getUsed(23000) == 90, "incorrect spent budget");
        assertYes(cap.consume(25, 24000, 90) == 25, "budget not renewed at dawn");
        BroodDailyBudget restored = new BroodDailyBudget();
        restored.restore(cap.getDay(), cap.getPersistedUsed());
        assertYes(restored.consume(80, 24001, 90) == 65, "save/load bypasses budget");
        assertYes(restored.consume(20, 30000, 0) == 0, "disabled combat cap bypassed");
        assertYes(restored.getUsed(2*24000) == 0, "new day reports old progress");
        assertYes(restored.consume(20, 2*24000, 90) == 20, "day rollover");
        // New v0.8 nest-tier contracts: no spawn at early stages, no final global stage.
        assertYes(!BroodColonyRules.canUpgrade(0, BigInteger.valueOf(11), 12, 50),
            "seed nest upgrades before 12");
        assertYes(BroodColonyRules.canUpgrade(0, BigInteger.valueOf(12), 12, 50),
            "seed cannot mature");
        assertYes(!BroodColonyRules.canUpgrade(1, BigInteger.valueOf(49), 12, 50),
            "mature upgrades before stage 50");
        assertYes(BroodColonyRules.canUpgrade(1, BigInteger.valueOf(50), 12, 50),
            "mature cannot become citadel");
        assertYes(!BroodColonyRules.canUpgrade(2, massiveStage, 12, 50),
            "tier exceeds citadel");
        assertYes(!BroodColonyRules.maySpawnQueen(1, massiveStage, 60),
            "queen spawns without citadel");
        assertYes(!BroodColonyRules.maySpawnQueen(2, BigInteger.valueOf(59), 60),
            "queen spawns before stage 60");
        assertYes(BroodColonyRules.maySpawnQueen(2, massiveStage, 60),
            "queen blocked at enormous stage");
        assertYes(!BroodColonyRules.mayEstablishDaughter(1, massiveStage),
            "daughter spawned from non-citadel");
        assertYes(BroodColonyRules.mayEstablishDaughter(2, BigInteger.valueOf(40)),
            "daughter citadel disabled at stage 40");
        for (int tier=0; tier<=2; ++tier) {
            for (int i=0; i<1000; ++i) {
                int morph=BroodColonyRules.chooseMorph(tier, massiveStage, new Random(i));
                assertYes(morph >= 0 && morph <= 4,
                    "queen must be generated only by rare and local-cap-checked gate");
            }
        }
        assertYes(BroodColonyRules.upgradedTier(0, BigInteger.valueOf(12),
            12, 50, 1, new Random(0)) == 1, "guaranteed seed ascension");
        assertYes(BroodColonyRules.upgradedTier(1, BigInteger.valueOf(50),
            12, 50, 1, new Random(0)) == 2, "guaranteed mature ascension");
        assertYes(BroodColonyRules.upgradedTier(2, massiveStage,
            12, 50, 1, new Random(0)) == 2, "citadel unexpectedly advances");
        // v0.9.0 raid gates: no premature raids or pathfinding far away.
        assertYes(!BroodRaidRules.eligible(true, true, BigInteger.valueOf(19), 20),
            "premature village raiding");
        assertYes(!BroodRaidRules.eligible(true, false, massiveStage, 20),
            "daytime raiding");
        assertYes(!BroodRaidRules.eligible(false, true, massiveStage, 20),
            "disabled raiding");
        assertYes(BroodRaidRules.eligible(true, true, massiveStage, 20),
            "late-game raiding never unlocks");
        assertYes(!BroodRaidRules.withinRange(25, 48), "raiding too close");
        assertYes(BroodRaidRules.withinRange(900, 48), "valid village range rejected");
        assertYes(!BroodRaidRules.withinRange(2500, 48), "village search range ignored");
        // v1.0 release-candidate gates: first-day respite for NTM construction.
        assertYes(!BroodPacingRules.graceExpired(23999, 1), "grace expired early");
        assertYes(BroodPacingRules.graceExpired(24000, 1), "grace never expired");
        assertYes(BroodPacingRules.graceExpired(0, 0), "zero-day grace should work");
        assertYes(!BroodPacingRules.shouldAdvance(23999, 1, 600), "passive points during grace");
        assertYes(BroodPacingRules.shouldAdvance(24000, 1, 600), "passive points missing after grace");
        assertYes(!BroodPacingRules.shouldAdvance(24101, 1, 600), "early passive tick");
        assertYes(!BroodPacingRules.maySpawn(false, true, 23999, 1), "parasites spawned during grace");
        assertYes(!BroodPacingRules.maySpawn(true, true, 24000, 1), "ward-spawning not blocked");
        assertYes(BroodPacingRules.maySpawn(true, false, 24000, 1), "disabled ward option still blocks");
        assertYes(BroodPacingRules.maySpawn(false, true, 24000, 1), "valid spawn denied");
        assertYes(!BroodTerritoryRules.shouldReturn(10.5, 10.5, 10, 10, 18), "guard moves too soon");
        assertYes(BroodTerritoryRules.shouldReturn(40.5, 10.5, 10, 10, 18), "guard ignores distant nest");
        assertYes(!BroodTerritoryRules.shouldReturn(Double.NaN, 10, 10, 10, 18), "non-finite coordinate accepted");
        // Existing worlds: grace must start when the addon is INSTALLED, not world creation.
        assertYes(!BroodOutbreakRules.graceExpired(987999L, 960000L, 3),
            "existing save should receive three days of grace");
        assertYes(BroodOutbreakRules.graceExpired(1032000L, 960000L, 3),
            "installation grace never expires");
        assertYes(!BroodOutbreakRules.graceExpired(999L, 1000L, 0),
            "negative time deltas must not expire grace");
        assertYes(BroodOutbreakRules.graceExpired(1000L, 1000L, 0),
            "zero-day grace is not zero");
        assertYes(!BroodOutbreakRules.shouldGrow(1032001L, 960000L, 3, 600),
            "unwanted passive growth between intervals");
        assertYes(BroodOutbreakRules.shouldGrow(1032000L, 960000L, 3, 600),
            "passive growth did not begin after installation grace");
        assertYes(!BroodOutbreakRules.outsideRadius(767, 0, 0, 0, 768),
            "nest too close to base");
        assertYes(BroodOutbreakRules.outsideRadius(768, 0, 0, 0, 768),
            "valid nest rejected at 768 blocks");
        assertYes(!BroodOutbreakRules.outsideRadius(10000, 20000, 10200, 20000, 768),
            "distant spawn ignored player base anchor");
        assertYes(BroodOutbreakRules.outsideRadius(11000, 20000, 10200, 20000, 768),
            "distant spawn rejected");
        assertYes(!BroodOutbreakRules.outsideRadius(Double.NaN, 0, 0, 0, 768),
            "NaN must not pass distance check");
        assertYes(BroodOutbreakRules.outsideRadius(2000000000, 0, -2000000000, 0, 768),
            "large-world coordinates overflowed");
        System.out.println("OK: protected blocks, staged digging, 10^3000 evolution, genetics, adaptation, daily combat budget and colony tiers, NTM grace period, natural ward spawning, colony patrol distance, and distant outbreaks in old saves");
    }
}
