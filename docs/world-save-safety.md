# World-save safety

Forever's name is a promise about world longevity. A missing feature can be fixed in a
later release. Silent save corruption, irreversible data loss, or a bad migration can
end a player's world permanently. These rules apply to agents, developers, scripts,
CI jobs, and manual testing.

## Rules and the failure mode each prevents

### Always use disposable development worlds

Create a new test world or clone a disposable fixture before launching the client,
starting a dedicated server, installing Matcha, testing a migration, or exercising a
feature that writes world data. Automated development must never open or modify a real
user survival world.

**Prevents:** a test run, crash, experimental command, or incompatible mod from changing
an irreplaceable world. A world that contains years of play is not a test fixture.

### Clone before dependency updates

Before changing Minecraft, Fabric, a mod, a datapack, a loader, or a toolchain, clone
the disposable world and retain the original clone. Test the update against the clone,
not the only copy.

**Prevents:** a dependency update from rewriting region files, registries, chunks, or
serialised data in the only recoverable copy. It also provides a before-and-after
comparison when a migration changes more than expected.

### Pin every mod and datapack version

Record exact versions, release identities, and checksums where available. Matcha is
especially important: use its pinned release and lock metadata rather than “latest”.
Scripts and CI must not silently replace a pinned input.

**Prevents:** a repeatable test becoming impossible because an upstream archive changed,
or a world loading a different registry and recipe set from the one that was validated.

### Never auto-update a live world

An update must be an explicit, reviewed operation with a ticket, a backup, a migration
plan, and a disposable-world test. A fetch script, launcher, or CI job must never
upgrade the dependency set of a live world as a side effect.

**Prevents:** an unattended update from applying an untested schema, removing registered
content, changing recipes, or making a save unreadable before the owner can recover it.

### Test dedicated-server startup

Run the dedicated-server path, not only the integrated client. Use `./gradlew runServer`
for a manual smoke test and `./gradlew runGametest` for the automated server harness
when applicable. Confirm that common code loads without client-only references, the
server reaches its ready state, and it stops cleanly.

**Prevents:** a client-only class reference, registration-order error, missing server
resource, or startup migration failure being discovered only after a player deploys a
server.

### Version all future persistent data

Every persistent structure must define an explicit schema version, codec or serializer,
validation, migration strategy, failure behaviour, and tests before it is released.
This includes player attachments, item components, entity data, settlement records,
warehouses, routes, shipments, and chronicle entries.

**Prevents:** the first format change becoming an irreversible choice between breaking
old worlds and freezing the design. Validation and explicit failure behaviour prevent
malformed or partial data from being accepted as valid state.

### Create backup and migration procedures before the first persistent gameplay release

Before any gameplay ticket first writes persistent data to a player or world, document
how to back up, detect the old schema, migrate it, verify the result, recover from a
failed migration, and roll back safely. Exercise those procedures against disposable
copies before release.

**Prevents:** a release from having no supported recovery path when a schema bug,
interrupted write, or unexpected old save is encountered. A migration that exists only
in code is not an operational procedure.

### Do not remove registered content from an existing save without a migration plan

Do not delete, rename, or stop registering items, blocks, components, entities, recipes,
or other identifiers that an existing save may contain unless the ticket includes a
tested migration or a supported compatibility alias. If safe migration is not known,
retain the registration and report the design gap.

**Prevents:** orphaned registry IDs, unopenable chunks, lost item stacks, broken block
entities, and saves that fail during registry loading. “Unused in the new code” does
not mean “absent from an old world”.

## Safe development procedure

1. Identify the disposable world and record its source and intended test purpose.
2. Stop all clients and servers using it before copying or installing anything.
3. Make a fresh clone before dependency, registry, or migration work.
4. Confirm exact mod and datapack versions, including Matcha's lock identity.
5. Run the smallest applicable test first, then the dedicated-server startup path.
6. Preserve logs and migration reports with the test result.
7. If a migration fails, keep the original clone intact, capture the failure, and stop.
8. Never “repair” a real world by trial and error. Report the blocker and design a
   recovery procedure first.

## Evidence required for a persistent-data ticket

The completion report must identify the schema version, codec or serializer, validation
rules, migration path, invalid-data behaviour, save/reload result, restart result,
duplication result, and client/server authority result. It must also state which checks
were not run. A passing unit test alone is not evidence that a world migration is safe.
