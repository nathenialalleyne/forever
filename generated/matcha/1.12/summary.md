# Matcha audit summary

> Derived from the official Matcha Flavoured archive, licence CC-BY-NC-SA-4.0, project QI0EmgZ1.
> This report records observed file/reference data and explicitly labelled analysis results. It does not claim gameplay intent.

## Input identity

- Version label: `Matcha_Flavoured_1_12.zip`
- Input kind: `zip`
- Input path: `/home/natea/repos/forever/vendor/matcha/Matcha_Flavoured_1_12.zip`
- SHA-256: `6209783021c358044abedabacee471faff5bd4080437d4e3b5e51963f1804248`
- Pack-root detection: `direct-pack-root`

## File counts

- Regular files: 5190
- Data-pack files: 2433
- Resource-pack files: 2754
- JSON parse errors retained in inventory: 0
- Unknown-path rows: 95

## Namespaces

- Data: `blasting`, `blessings`, `crafting`, `custom_music`, `debug`, `endless_repairs`, `food`, `main`, `minecraft`, `smelting`, `smithing_table`, `smoking`, `stonecutting`
- Resources: `matcha`, `minecraft`

## Major data categories

| Category | Files |
|---|---:|
|advancements|244|
|dimension-type|4|
|functions|145|
|loot-tables|293|
|predicates|21|
|recipes|1076|
|registry-data|332|
|structures|43|
|tags|108|
|unknown|73|
|worldgen|94|

## Major resource-pack categories

| Category | Files |
|---|---:|
|blockstates|30|
|equipment|13|
|fonts|1|
|item-definitions|309|
|language|4|
|models|713|
|particles|3|
|sounds|5|
|textures|1675|
|unknown|1|

## Reference and override counts

- Vanilla namespace overrides observed: 3649
- Scoreboard objectives detected: 24
- Function reference edges detected: 1459
- Total reference edges detected: 28981
- Unresolved internal-looking references: 60

## Prominent warnings

- None recorded.

## Review boundary

The analyser reports literal paths, JSON fields, command text, and reference relationships where recognised. Resolution status is based only on exact matches against observed files in this input. Vanilla and external namespaces are not treated as missing files. No gameplay mechanics, balance, or gameplay intent has been inferred.
