# ADR 0008: Use a hybrid Obol currency

- Date: 2026-08-28
- Related principles/ADRs: [Principles 6, 8, 9, 12, 14, 17, and 18](../design-principles.md), [ADR 0009](0009-settlements-as-building-graphs.md), [ADR 0012](0012-cross-dimensional-logistics.md), [ADR 0017](0017-player-shop-pricing.md)

## Status

Accepted. This is a locked economy representation decision.

## Context

Forever needs an economy in which a specialist, settlement, or trade route can compete
with generic farms and trading halls. Currency should support that alternative without
turning every transaction into an inventory-management contest. It should also work in
solo play, where the player may be both producer and customer, and in multiplayer, where
physical transfer helps trade feel like something happening in the world. It is
intended to make merchant and settlement routes credible alternatives to the anti-AFK-farm
checklist. Principles 6, 8, and 9 set the relevant balance between convenience and place.

A purely physical coin system makes payment tangible, but denominations and stockpiles
consume many inventory slots. A purely virtual ledger is convenient, but it removes the
possibility of handing payment to another player, finding treasure, or separating a
wallet from a warehouse. Vanilla emeralds alone carry existing villager-trade assumptions
that Forever is specifically trying not to make mandatory.

The vision's reward currency is earned convenience. A player should be able to build
infrastructure and then move value through it, but a universal magical balance must not
make logistics and geography irrelevant. Currency also touches every persistent system,
so duplication, rollback, and migration are world-save risks under Principle 18.

## Decision

Forever uses **Obols** in two linked representations:

- Physical Obol items are tangible, transferable, lootable, and usable in contexts that
  intentionally require payment in the world.
- A compact player **Coin Purse** stores a balance as persistent player state, so routine
  transactions do not consume a large portion of the normal inventory.

The player may withdraw physical Obols from the purse and deposit valid physical Obols
back into it. Each conversion is an atomic, server-authoritative transaction. The purse
must never create value without debiting an equivalent balance, and depositing must never
credit the same item twice.

The purse is a payment convenience, not a universal warehouse or a cross-dimensional
logistics network. Physical goods, storage capacity, route service, and settlement
infrastructure remain separate concerns under [ADR 0012](0012-cross-dimensional-logistics.md).
Denominations, stack limits, transaction limits, and any conversion rules are balance
data under Principle 12.

Player shops may quote Obol prices under [ADR 0017](0017-player-shop-pricing.md). NPC
willingness to pay and settlement services determine whether a sale occurs, not a forced
price list. Currency behaviour, balance changes, and failure messages must be visible in
the Field Guide and accessible without relying on colour alone.

All purse and physical-coin data use explicit schema versions. A save or transaction
failure must fail closed, preserving the player's existing value rather than silently
minting or deleting it.

## Consequences

- Physical coins make treasure, payment, gifting, wages, and player-to-player trade
  legible in the world instead of hiding all value behind a screen.
- The purse prevents everyday commerce from consuming many inventory slots, supporting
  travel and solo play without adding a large backpack by stealth.
- A specialist can sell to NPCs or players without first building a huge emerald stockpile,
  which helps alternative routes compete on their own terms.
- Currency can connect shops, settlements, and infrastructure while remaining distinct
  from the physical movement of goods. This keeps economic convenience from becoming a
  magical universal warehouse.
- Two representations create a serious duplication and consistency surface. Withdrawals,
  deposits, death, trades, rollback, and disconnects need transactional tests.
- Physical Obols can be lost, stolen, or stranded in a chest. The purse is more convenient,
  but the physical form is intentionally subject to world risk and logistics.
- A compact balance can still become an inflation engine if willingness-to-pay, production,
  and sinks are tuned poorly. Data-driven pricing alone does not guarantee a healthy
  economy.
- Conversion rules and denominations add UI, item, recipe, and migration work. The purse
  must not grow into a second inventory or an unbounded account ledger.
- Cross-dimensional use needs a deliberate policy. A purse may travel with a player, but
  moving bulk goods and settlement wealth still follows [ADR 0012](0012-cross-dimensional-logistics.md).
- Future work includes the purse component, physical item tags, atomic transaction service,
  loss and recovery rules, balance data, shop integration, and save-corruption tests.

## Rejected alternatives

### Physical coins only

A physical-only economy would maximise tangibility, but routine wages and purchases would
fill inventories and push players towards hidden storage or shulker-box logistics. It
would make the economy harder to use than the goods it is supposed to help circulate.

### A purely virtual player balance

A ledger-only currency is easy to transact, but it removes physical transfer, treasure,
robbery, and visible settlement commerce. It also makes value feel like a universal remote
permission rather than something infrastructure can move.

### Emeralds as the only currency

Emeralds are familiar and already integrated with villagers, but inheriting their
semantics would preserve the trading-hall path as the default economic centre. It also
makes Forever's new willingness-to-pay model subordinate to vanilla assumptions.

### Separate currency ledgers per settlement

Local ledgers could reinforce regional identity, but they would fragment solo play and
make ordinary trade depend on exchanging currencies before exchanging goods. Regional
identity is better expressed through goods, demand, and infrastructure than through a
collection of incompatible balances.

### A bank or universal warehouse ledger

A central ledger would be convenient, but it would erase the geographic and logistical
costs that make roads, routes, and settlements meaningful. The Coin Purse is deliberately
small in scope: payment convenience, not remote possession of every resource.
