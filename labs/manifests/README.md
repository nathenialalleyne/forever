# Lab manifests

One manifest per lab, naming the exact artifacts under test.

A manifest must record Modrinth project IDs and version IDs, not just display names,
because names are ambiguous and versions move. A lab that cannot be re-run against the
same artifacts months later is not evidence.

The baseline every lab starts from is `pack/`: Minecraft 26.2, Fabric loader 0.19.3,
Fabric API `NqwNSxwA`, Global Packs `DqrPrUMp`, and Matcha `E9rngRfK`. A manifest
records only what the lab ADDS to that baseline, plus anything it deliberately removes.
