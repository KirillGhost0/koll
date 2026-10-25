# Endless Brood 1.0.0-rc2 — Minecraft 1.7.10 Forge

**STATUS: unverified SOURCE release candidate, not a compiled or in-game tested mod JAR.** Do not put the ZIP into the `mods` directory.

Update in rc2: first infection is kept far from the original co-op bases and world spawn (default 768 blocks). A three-day grace period starts when the mod is first installed even in an old world. A few nests can appear in already-generated *loaded* remote terrain without forced chunk loading; vanilla parasite spawns are kept away from remembered starting bases. Infection can later spread toward them naturally through colonies. The initial base positions must be recorded during the first three game days; this does not discover base buildings automatically.

Changes since 0.9.0: idle nest-born parasites patrol back to their saved home nest; matriarchs can call up to six existing guards when attacked (ten-second cooldown); brood wards now block natural spawning nearby; initial threat-free grace defaults to three Minecraft days (60 minutes at 20 TPS) since installation; evolution BigInteger storage now uses an NBT byte array with backward-compatible reading of the old `Points` string.

All 0.9 gameplay features remain: six original 3D models and textures, infinite stages, tiered nests, tainted soil, limited block digging with absolute obsidian/bedrock/end-portal-frame immunity, scanners, serum, damage adaptations, protective wards, staggered village raids, and configurable low entity counts for NTM-heavy packs.

The source has offline smoke tests and a GitHub Actions Java 8 / Gradle 4.4.1 / ForgeGradle 1.2 workflow, but **there has not yet been a full compilation with actual Forge 1.7.10 nor a multiplayer or HBM NTM integration test**.

Russian explanation: `README_RU.md`. Release test procedure: `RELEASE_CHECKLIST_RU.md`. Changelog: `CHANGELOG.md`.

## Build

Upload the archive contents to a GitHub repository, choose **Actions → Build Minecraft 1.7.10 mod → Run workflow**, and inspect the full logs. The action is not proof of compatibility until it actually finishes successfully. Run `bash scripts/smoke-test.sh` locally to check only pure Java rules, model syntax with fake APIs, and PNG headers/dimensions.
