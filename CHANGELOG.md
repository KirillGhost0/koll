# Changelog

## 1.0.0-rc2 — distant first infection (2026-10-25)

**Still source-only; not compiled with real Forge or tested in Minecraft.**

- Outbreaks start at least **768 blocks from the vanilla world spawn and all starting positions of co-op players** who join during the installation grace period. Starting positions are saved in world NBT.
- **Three Minecraft days of grace counted from first installation**, not from world age. Existing `config/endlessbrood.cfg` settings are not forcibly overwritten (if old `threatGraceDays=1`, edit it to `3`).
- New terrain seeds nests only after grace and beyond the safety radius, without generating them next to online players.
- **Existing explored world support:** up to two rare distant starter nests may be seeded in already generated, *currently loaded* chunks when someone explores beyond the safe radius. The algorithm never forces chunk loading, does not scan disk or modify near-base chunks.
- Random vanilla parasite spawning is suppressed inside remembered starting-base zones, but **external colonies and their offspring can still enter** and eventually attack the base. This is a spawning rule, not a protective force field; use brood wards/obsidian as before.
- New configurable options: `initialOutbreakDistance`, `outbreakPlayerBuffer`, `retroactiveDistantNests`, `retroSeedLimit`, `retroSeedIntervalTicks`, `blockNaturalSpawnInHomeZones`.
- Added pure Java regression tests for relative grace / extreme world coordinates / distance boundary, with Forge build and gameplay still pending.

## 1.0.0-rc1 — source release candidate (2026-10-25)

Not an installable mod JAR. ForgeGradle and in-game multiplayer have NOT been verified.

- **Colony patrols**: nest-born parasites remember their home, wander back when idle and too far away, and persist their home coordinates in NBT. The guard AI never scans for nests or loads an unloaded nest chunk.
- **Matriarch defensive alarm**: a hurt matriarch can call a limited number of already-present nearby defenders against its attacker. The action is time- and population-limited and excludes attacking creative-mode players.
- **Ward-safe natural spawning**: default wards now reject natural parasite spawning within their protected zone; they already blocked nest spawning, soil conversion, and tunnelling.
- **NTM-friendly starting grace**: newly created worlds have one default in-game day (20 minutes at 20 TPS) before passive evolution or natural/colony parasite spawning starts. The grace duration is configurable, is based on total world time, and does not affect creative test eggs. Pre-existing worlds beyond the threshold are unaffected.
- **Arbitrary-precision save format**: evolution progress is now persisted as a binary BigInteger byte array. Worlds saved by 0.1–0.9 using the original string `Points` key still load; 1.0.0-rc1 will write the new binary format. Back up worlds before downgrading.
- Added pure Java regression assertions for grace, ward gating, and nest patrol distance. Existing stage, protected block, mutation, adaptation, colony, client-renderer stub, and PNG checks retained.
- Updated mod metadata, build artifact labels, Russian release checklist and documentation.


## Previous versions

## 0.9.0 — village raids, colony alarms and AI load distribution (source candidate)

- Added bounded **night village navigation**: once global evolution reaches stage 20, idle parasites may periodically seek a nearby *inhabited* registered vanilla village (within 48 blocks). Existing target AI selects villagers when within reach. No extra mobs are spawned, no chunks loaded deliberately, and raid pathfinding ends at daytime, when targets appear or when navigation ends. This is a basic raiding mechanic, not a siege simulation.
- Added **colony defense alarms**: attacking/clicking or harvesting a nest draws at most 6 existing nearby guards (18-block default), ignoring creative-mode players. Does not clone parasites or blow up terrain.
- Added optional **matriarch support heal**: up to four injured allies regain just 1 health per existing rally pulse; no unlimited group regeneration.
- **Performance**: staggered target-sharing scans every 160 ticks per parasite instead of synchronized every 100 ticks, also staggered periodic attribute refresh. Search radius and timing of raids and guard limits are configurable.
- Added `BroodRaidRules` pure Java stage/night/range gates with smoke test assertions.
- Retained never-break obsidian/bedrock/portal-frame rule, modded-block protection, slow unlimited evolution and all six original models.
- **Important:** verified pure Java policy tests and resources only; NOT compiled with actual Minecraft Forge and NOT gameplay tested.

## 0.8.0 — evolving colonies + matriarch (source candidate)

- Added a full sixth 3D mesh/skin for the brood matriarch, with summon egg, multiple crown horns, rear egg sac, moving jaws and legs, synced in multiplayer via morph DataWatcher.
- Added model accessories for phenotype traits: extra armor plate (Brute/Plates), spore sac (Spores) and horn (Swift).
- Converted nest metadata into persistent colony tiers Seed (0), Mature (1), Citadel (2). Old nests stay at metadata 0. Added two 16×16 variant nest textures.
- New `BroodColonyRules`: pure deterministic stage thresholds, bounded upgrades, tier-specific spawning and gated rare leaders. Smoke tests cover 10^3000 stages.
- Seed -> Mature unlock at stage 12; Mature -> Citadel at stage 50. Both have a 1/16 chance per existing 2400-tick pulse, default config.
- Matriarchs become eligible at stage 60 and require a citadel; a 30-block local check prevents spamming. Every 200 ticks nearby parasites temporarily receive speed and up to six receive Strength I, with local target coordination.
- Daughter nests now grow only from citadels; new nests start at tier 0. World/ward checks guard all new colony logic.
- Kept slow NTM pacing, endless global stage, hard-block digging exclusions and conservative mob population caps unchanged.
- Validated pure Java rule tests, client mesh stub compile and PNG integrity. **Real Forge 1.7.10 compilation and gameplay remain unverified.**

# Changelog

## 0.7.0 — NTM co-op pacing, permanent safe zones and scouting (source candidate)
- Added a **persistent shared daily budget for kill/conversion progression** (`combatPointsPerDay`, default 90 per 24000 world ticks); passive evolution always advances. This keeps high-yield mob farms and two-player kill streaks from bypassing industrial tech progression. Budget rolls over with total world ticks (not `/time set`) and is stored in world-save NBT.
- Long-term colony expansion is **8× slower** by default: nest daughter attempt chance changed from 1/15 pulses to configurable 1/120 (the evolution stages themselves remain uncapped).
- **Brood Repelling Wards** now actually prevent our parasites from digging permitted blocks inside their radius; parasite AI checks protection only at final break to avoid per-tick 3D scans. Wards additionally block tainted-soil spread inside their zone.
- New craftable **Brood Signal Scanner** with a 16×16 icon: right-click shows nearby parasite count, distance and cardinal direction of nearest threat; a three-second cooldown prevents spam.
- The `/broodstage` command now shows how much event-based growth the shared world used today; extended pure-Java budget tests and texture smoke tests.
- Changed the tile-entity safety check on breakable blocks to the metadata-aware Forge signature. Vanilla obsidian, bedrock and end-portal frames remain always immune; all modded HBM/NTM blocks remain outside the dig allowlist.
- **Still source only**. Full Forge compilation, dedicated server simulation and HBM/NTM testing are intentionally deferred; this is NOT a verified playable JAR.


## 0.6.0 — combat roles, survival defenses, server performance pass (source candidate)
- Added craftable **Brood Repelling Ward** (chitin, iron and redstone), textured as a 32x32 block, with capped local pulses. It slows invading parasites and gradually reclaims adjacent corrupted soil. Colonies cannot spawn inside an active ward radius. Ward power and radius are configurable.
- Combat now differs by body plan: Scuttlers make short cooldown-bound lunges; Bulwarks mitigate 20% of blockable damage; Thornbacks hurt adjacent melee attackers; rare Colossi emit non-explosive slowness/weakness roars (all configurable).
- Serum now cleanses one tainted-soil block per use and is consumed on a player only if that player actually has poison or slowdown. Infectable mobs visibly show weakness particles when first infected.
- Tainted soil now respects `/gamerule mobGriefing false`, as advertised. Ward scans are avoided in the frequent random-soil tick path to keep NTM servers responsive.
- Protected obsidian/bedrock/end-portal-frame logic, modded-block allowlist policy, threefold slower evolution, unbounded stage arithmetic, original five 3D models, multiplayer DataWatcher genetics and rare colony expansion retained.
- Updated source version, creative block locale entries, texture inventory, README and GitHub Actions artifact label.
- **Unverified against real Forge / Minecraft** as requested: feature work first, build and live testing afterwards. Version 0.6.0 is not a playable JAR.

## 0.5.0 — broad gameplay systems, unverified Forge candidate
- Fifth original animated morph (Colossus) with separate 128×64 texture and egg; unusual late-game spawn near nests.
- Stackable genome traits (plates, swiftness, spores, regenerative), persistent and client-synchronized.
- Server-side collective resistance from accumulated fire, explosion, projectile and radiation-named damage; never complete immunity.
- Rare, small world-generated nests, throttled brood production and guarded nest expansion after stage 40.
- Original tainted soil with depth-limited spread and a mild movement hazard.
- Original chitin material and serum item with crafting, antidote use for player/pets/villagers, and parasite death drops.
- Coordinated parasite targeting at stage 12+, tamed-animal conversion protection.
- Added configuration switches for each expensive mechanic, updated locale entries and GitHub Actions artifact version.
- Expanded pure-Java and model/texture validation; these tests are *not* Forge compilation or game launch.

## 0.4.0 — creative eggs for four morphs
## 0.3.0 — original 3D models, textures and animations
## 0.2.0 — 3x slowdown, protected blocks and mutations
## 0.1.0 — infection, first parasite and BigInteger evolution
