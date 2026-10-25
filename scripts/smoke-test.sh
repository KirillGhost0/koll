#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
out="$(mktemp -d)"
trap 'rm -rf "$out"' EXIT
javac -encoding UTF-8 -source 8 -target 8 -d "$out" \
  src/main/java/com/endlessbrood/common/EvolutionMath.java \
  src/main/java/com/endlessbrood/common/ParasiteGenes.java \
  src/main/java/com/endlessbrood/common/ParasiteGenome.java \
  src/main/java/com/endlessbrood/common/BroodAdaptiveMath.java \
  src/main/java/com/endlessbrood/common/BroodDailyBudget.java \
  src/main/java/com/endlessbrood/common/BroodColonyRules.java \
  src/main/java/com/endlessbrood/common/BroodRaidRules.java \
  src/main/java/com/endlessbrood/common/BroodPacingRules.java \
  src/main/java/com/endlessbrood/common/BroodTerritoryRules.java \
  src/main/java/com/endlessbrood/common/BroodBlockPolicy.java \
  src/main/java/com/endlessbrood/common/BroodOutbreakRules.java \
  tests/BroodRulesSmoke.java
java -cp "$out" test.BroodRulesSmoke
# Stub compile catches Java syntax errors in model/renderer; it is NOT Forge.
find tests/client-api-stubs -name '*.java' -print0 | xargs -0 javac -encoding UTF-8 -source 8 -target 8 -cp "$out" -d "$out" \
    src/main/java/com/endlessbrood/client/ModelBroodParasite.java \
    src/main/java/com/endlessbrood/client/RenderParasite.java
printf '%s\n' 'OK: 6 morph models + renderer compile against mock client API' 
python3 - <<'PY'
import pathlib, struct
base=pathlib.Path('src/main/resources/assets/endlessbrood/textures')
for path in sorted(base.rglob('*.png')):
    d=path.read_bytes()
    assert d[:8] == b'\x89PNG\r\n\x1a\n', f'{path} is not PNG'
    w,h=struct.unpack('>II',d[16:24]); assert w>0 and h>0
    print('PNG OK:', path.name, f'{w}x{h}')
assert len(list((base/'entity').glob('*.png'))) == 6, 'Expect six model skins'
assert (base/'items'/'parasite_egg.png').exists(), 'Missing creative egg icon'
assert len(list((base/'blocks').glob('*.png'))) == 5
assert all((base/'items'/n).exists() for n in ['chitin.png','serum.png','scanner.png'])
print('OK: six morph textures, five block textures, four item textures')
PY
