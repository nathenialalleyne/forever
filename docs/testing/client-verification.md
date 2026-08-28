# Client verification checklist

Several results in this repository are marked "server verified, client pending". This
document is how that gap gets closed. It takes about ten minutes and needs no development
environment, only a Minecraft launcher.

## Why this exists as a separate task

Automated verification runs on a build host with no display, so no client can be launched
there. That is not a limitation of the pack: it is a property of the machine running the
checks. A dedicated server also never loads resource packs, so no amount of server testing
can substitute for the client-side half of the question.

Rather than guessing, the labs record exactly what remains unproven. This checklist turns
that into a short manual pass.

## Setup

```sh
./scripts/build-pack.sh
```

That writes `dist/many-roads-home-<version>.mrpack`. Import it into any launcher that
accepts a Modrinth pack: the Modrinth App, Prism Launcher, ATLauncher, and others all
work. The launcher resolves every dependency from the URLs and hashes in the pack
metadata, so nothing else needs installing.

Create a new world. Do not use an existing survival world.

## What to check

### 1. Matcha's resource-pack role is active

**Why it matters:** Matcha ships one archive containing both `assets/` and `data/`. The
server proves the datapack half applies, because recipe and advancement counts jump from
1585 to 2346 and 1805. The resource half can only be seen on a client. If it is not
active, the pack runs Matcha's mechanics with vanilla textures, and LAB-02 showed nothing
warns about that.

- [ ] Open Options, Resource Packs. Matcha appears in the enabled list.
- [ ] Matcha's custom item textures render in the inventory rather than showing vanilla
      or missing-texture placeholders.
- [ ] Matcha's title-screen splash text appears, which is a quick positive signal.

### 2. REI opens and reads Matcha recipes

**Why it matters:** LAB-01 adopted REI as the single recipe viewer, but only verified that
it loads server-side. Whether its UI works and whether it can display Matcha's 2346
recipes is a client question.

- [ ] The REI panel appears beside the inventory.
- [ ] Search for a Matcha-specific item and it is listed.
- [ ] Click an item to view its recipe, and the recipe displays correctly.
- [ ] No REI error toast or exception appears in the log.

### 3. Jade's tooltip works

- [ ] Looking at a block shows the Jade overlay with the block name.
- [ ] Looking at an entity shows its health and type.
- [ ] The overlay does not overlap or fight with REI's panel.

### 4. Exactly one recipe viewer is present

**Why it matters:** LAB-01 rejected JEI specifically because Jade lists it as an optional
dependency and it could arrive transitively, giving the pack two viewers.

- [ ] Only REI is present. No second viewer panel or duplicate keybind.

### 5. No guidebook pile

**Why it matters:** LAB-01 measured that the current baseline has no guidebook problem at
all, and the pack intends to keep it that way.

- [ ] No starter book, manual, or guide item is granted on first join.
- [ ] The advancement screen shows **three** Matcha tabs in addition to the vanilla ones,
      not one tab per subsystem. Verified by inspection of the archive: Matcha defines
      exactly three root advancements with a display block, and overrides no vanilla
      advancement, so it adds three tabs rather than replacing any.

### 6. Ordinary play is unaffected

- [ ] Blocks place and break normally.
- [ ] The world loads without a missing-content warning.

## Recording the result

Update the "Client behaviour" section of:

- `docs/compatibility/matcha-loading.md` for item 1
- `labs/results/LAB-01-information-and-onboarding.md` for items 2 to 5

Quote what you actually saw. If something fails, that is a finding worth a ticket rather
than something to work around silently.

## If a check fails

The most likely failure is item 1, the resource-pack role, because LAB-02 proved a
one-sided load produces no diagnostic at all. If Matcha's textures are missing, confirm
that `config/global_packs.toml` lists `datapacks/Matcha_Flavoured_1_12.zip` under **both**
`[resourcepacks].required` and `[datapacks].required`. The two entries are deliberately
the same path, and dropping either one silently delivers half the pack.
