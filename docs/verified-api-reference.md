# Verified Minecraft 26.2 API Reference

**Every signature below was extracted with `javap` from the actual jars on this
machine.** Do not trust memory or tutorials for these. Minecraft 26.2 is
non-obfuscated and uses **Mojang names**, not Yarn.

Regenerate any of this yourself with:

```bash
export JAVA_HOME=~/toolchains/jdk-25.0.4.1+1
MC=~/.gradle/caches/fabric-loom/26.2/minecraft-extracted_server.jar
$JAVA_HOME/bin/javap -cp $MC net.minecraft.world.item.ItemStack
```

## Naming traps

| Wrong (Yarn / older) | Correct for 26.2 |
|---|---|
| `net.minecraft.util.Identifier` | `net.minecraft.resources.Identifier` |
| `ResourceLocation` | `Identifier` (26.2 renamed it back) |
| `ServerWorld` | `net.minecraft.server.level.ServerLevel` |
| `TestContext` | `net.minecraft.gametest.framework.GameTestHelper` |
| `PlayerEntity` | `net.minecraft.world.entity.player.Player` |
| `ServerPlayerEntity` | `net.minecraft.server.level.ServerPlayer` |
| `World` | `net.minecraft.world.level.Level` |
| `Registries.ITEM` | `net.minecraft.core.registries.BuiltInRegistries.ITEM` |

`Identifier` is at `net.minecraft.resources.Identifier`. This is the single most
common mistake. It is neither `net.minecraft.util.Identifier` (Yarn) nor
`ResourceLocation` (older Mojang).

```java
Identifier id = Identifier.fromNamespaceAndPath("forever", "mastery");
Identifier parsed = Identifier.parse("forever:mastery");
```

## Persistent player/entity state: Fabric Data Attachment API

Module: `fabric-data-attachment-api-v1` (2.2.18+515ac5339e, ships with Fabric API 0.158.0+26.2)

```java
public final class AttachmentRegistry {
    public static <A> AttachmentType<A> create(Identifier, Consumer<Builder<A>>);
    public static <A> AttachmentType<A> create(Identifier);
    public static <A> AttachmentType<A> createDefaulted(Identifier, Supplier<A>);
    public static <A> AttachmentType<A> createPersistent(Identifier, Codec<A>);
    public static <A> Builder<A> builder();
}

public interface AttachmentType<A> {
    Identifier identifier();
    Codec<A> persistenceCodec();
    default boolean isPersistent();
    Supplier<A> initializer();
    boolean isSynced();
    boolean copyOnDeath();
}
```

Note `copyOnDeath()`. Mastery progress is permanent (principle 3), so mastery
attachments must set it. Verify the builder method name with `javap` on
`AttachmentRegistry$Builder` before using it.

## Large world-scoped records: SavedData

```java
public abstract class SavedData {          // net.minecraft.world.level.saveddata
    public void setDirty();
    public void setDirty(boolean);
    public boolean isDirty();
}

public record SavedDataType<T extends SavedData>(   // same package
    Identifier id,
    Supplier<T> constructor,
    Codec<T> codec,
    DataFixTypes dataFixType
) {}
```

`SavedDataType` is codec-based. There is no legacy `CompoundTag`-only constructor,
which suits the versioned-schema requirement well: put the schema version inside
the record and branch in the codec.

Use this for settlements, warehouses, routes, shipments, and the chronicle. Do not
use attachments for those (see `docs/architecture.md`).

## Per-ItemStack state: data components

```java
public interface DataComponentType<T> {    // net.minecraft.core.component
    static <T> DataComponentType.Builder<T> builder();
    Codec<T> codec();
    default boolean isTransient();
    StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec();
}
```

Vanilla components live in `net.minecraft.core.component.DataComponents`.
Register custom ones into `BuiltInRegistries.DATA_COMPONENT_TYPE`.

## Durability (relevant to FVR-101/102)

```java
public class ItemStack {                   // net.minecraft.world.item
    public boolean isDamaged();
    public int getDamageValue();
    public void setDamageValue(int);
    public int getMaxDamage();
    public void hurtAndBreak(int, ServerLevel, ServerPlayer, Consumer<Item>);
    public void hurtAndBreak(int, LivingEntity, InteractionHand);
    public void hurtAndBreak(int, LivingEntity, EquipmentSlot);
}
```

The `Consumer<Item>` in the first overload is the break callback. Preventing item
destruction (principle 5) means intercepting the break path, not clamping damage
in a tick handler. Confirm the exact interception seam before implementing FVR-101,
and record what you find in the ticket.

## GameTest

```java
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public class SomeTest {
    @GameTest
    public void doesThing(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        helper.succeed();
    }
}
```

Register the class in `src/gametest/resources/fabric.mod.json` under the
`fabric-gametest` entrypoint. Run with `./gradlew runGametest`.

Useful `GameTestHelper` methods verified present: `succeed()`, `succeedIf(Runnable)`,
`succeedWhen(Runnable)`, `succeedOnTickWhen(int, Runnable)`, `getLevel()`,
`makeMockServerPlayerInLevel()`, `succeedWhenBlockPresent(Block, BlockPos)`.

## Available Fabric API modules

Confirmed present in the resolved 0.158.0+26.2 dependency tree:
`fabric-data-attachment-api-v1`, `fabric-networking-api-v1`,
`fabric-command-api-v2`, `fabric-screen-api-v1`, `fabric-gametest-api-v1`,
`fabric-client-gametest-api-v1`.

## Rule for every agent

If you are unsure of a signature, **run `javap` and check**. A wrong guess costs a
full build cycle. The command is at the top of this file. Do not copy signatures
from tutorials or from memory of earlier Minecraft versions.

## Durability interception: the public seam (FVR-101)

This was investigated and confirmed by `javap`. Do not redo this work.

**There is no public generic "item broke" event**, and `ItemStack.applyDamage` is
private. But you do not need a mixin, because Fabric provides a purpose-built hook:

```java
// net.fabricmc.fabric.api.item.v1.CustomDamageHandler  (fabric-item-api-v1)
public interface CustomDamageHandler {
    int hurtAndBreak(ItemStack stack, int amount, LivingEntity entity,
                     EquipmentSlot slot, Runnable breakCallback);
}
```

You return the damage amount to actually apply, and you decide whether
`breakCallback` (the thing that shrinks the stack) ever runs. Clamping the returned
amount so damage never reaches max means vanilla never takes the destruction path.

Related, for attaching default components to existing vanilla items without a mixin:

```java
// net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents
public static final Event<ModifyCallback> MODIFY;
```

### Vanilla 26.2 already models "broken"

```java
public boolean isBroken();            // isDamageableItem() && getDamageValue() >= getMaxDamage()
public boolean nextDamageWillBreak();
```

In `applyDamage`, the `shrink(1)` call is **gated behind `isBroken()`**. So a stack
sitting at `damage == maxDamage` is already a coherent vanilla state. Forever's
broken-item requirement (principle 5, FVR-102) is therefore additive: hold the item
at maximum damage, suppress the shrink, and layer Forever condition semantics on top.
This is far less invasive than replacing the durability system.

`isDamageableItem()` is itself gated on the `MAX_DAMAGE` component being present,
`UNBREAKABLE` being absent, and `DAMAGE` being present. Note that using `UNBREAKABLE`
to prevent destruction is the wrong tool: it also disables the damage bar and makes
condition state unrepresentable.
