# ADR 0017: Let players set shop prices while NPCs decide willingness to pay

- Date: 2026-08-28
- Related principles/ADRs: [Principles 6, 8, 9, 12, 15, 17, and 18](../design-principles.md), [ADR 0008](0008-hybrid-obol-currency.md), [ADR 0009](0009-settlements-as-building-graphs.md), [ADR 0018](0018-infrastructure-gated-journey-skipping.md)

## Status

Accepted. This is a locked player-shop economy decision.

## Context

Forever needs trade to be a genuine alternative to the anti-AFK-farm checklist, not a
thin interface placed on top of fixed villager prices. A player who invests in production,
craftsmanship, a route, or a settlement should be able to offer goods and discover whether
there is demand. Multiplayer should reward a merchant's judgement, while solo play must
remain viable through NPC customers.

Fixed prices are easy to balance on paper, but they make player shops feel like vending
machines and prevent regional supply, quality, convenience, and reputation from mattering.
A completely unconstrained market has the opposite problem: an NPC may accept absurd
prices, and one profitable recipe can become the new universal farm.

The vision does not dictate a single aesthetic or economic playstyle. It asks for parity
between comparable effort, and Principle 17 forbids making ordinary play worse merely to
sell an upgrade. The pricing system should therefore not impose artificial price floors,
forced discounts, or a tax that exists only to drive players into a shop.

There are also technical and save concerns. Prices, willingness, stock, and Obol balances
are persistent or transactional data. A client must not be able to change a listing or
complete a sale without server validation. The rules must be explainable in the Field
Guide, because an unsold item should be a visible market outcome rather than a mystery.

## Decision

Player shops use **free player pricing**. The shop owner chooses an asking price for a
listing, subject to valid item, quantity, and transaction limits. Forever does not impose
a universal fixed price or a hidden price correction that changes the owner's listing.
Player-to-player agreements may use the same Obol economy or an explicitly documented
barter path.

NPC buyers have a data-driven **willingness to pay**. It represents the conditions under
which a buyer values a good, and may consider the item, quantity, quality or craftsmanship,
local supply, need, service, relationship, and settlement context. If the asking price is
above the buyer's willingness to pay, the NPC simply does not buy. A high price is allowed
to be a bad business decision rather than a reason for the system to silently discount it.

Willingness, demand modifiers, stock limits, and any replenishment or market timing values
live in validated data under Principle 12. The Field Guide and shop UI expose enough
reason for a failed sale to be actionable without revealing an opaque formula that players
must reverse-engineer externally.

Sales are atomic and server-authoritative. The listing, item, payment, ownership, and
stock change together or not at all. Obols follow [ADR 0008](0008-hybrid-obol-currency.md),
and a shop may depend on a registered settlement service under [ADR 0009](0009-settlements-as-building-graphs.md)
without making every player shop require a large town.

Free pricing does not guarantee demand or abundance. It is a decision right for the seller,
not a promise that every price is viable.

## Consequences

- Players can express judgement and specialise in products that are convenient, rare,
  well-crafted, locally available, or valuable to a particular settlement.
- NPCs provide a solo-compatible market without forcing every player into multiplayer or
  a fixed trade hall. A seller can learn demand by observing real outcomes.
- Regional production and infrastructure can matter because supply, need, and convenience
  may change willingness to pay rather than every village sharing one price table.
- Bad prices fail naturally. The system does not need punitive fees or artificial scarcity
  to prevent an obviously poor listing from selling.
- Players may initially read an unsold listing as a bug or as unfair behaviour. The UI must
  explain the relevant reason, and the Field Guide must teach the concept before the
  market becomes a progression dependency.
- A flexible willingness model is hard to tune. If willingness is too generous, shops
  become an NPC money printer. If it is too strict, the merchant route feels decorative
  and players return to generic farms.
- Free pricing can produce price wars, hoarding, collusion, or multiplayer inequality.
  Those are consequences of an open market and may need permissions, audit history, or
  server configuration without removing seller choice.
- Dynamic demand creates more state and more migration cases than fixed recipes. The model
  must remain bounded, deterministic enough to debug, and safe across restart and rollback.
- Future work includes listing storage, buyer evaluation, market feedback, anti-duplication
  tests, balance datasets, accessibility text, and experiments comparing shop output with
  farms and settlement services.
- We accept that not every item or location will have a profitable NPC market. The goal is
  competitive alternatives in their domains, not a guarantee that every price wins.

## Rejected alternatives

### Fixed prices for every item

Fixed prices are predictable, but they make shop ownership mostly cosmetic and prevent
regional demand or craftsmanship from mattering. They would recreate a central trade hall
with a different screen.

### Forced minimum or maximum prices

Price bounds could stop scams or runaway inflation, but they would also prevent legitimate
specialist goods, scarcity, convenience, and player negotiation from finding their own
value. Data-driven willingness is a better control than a universal arbitrary cap.

### Opaque demand with no explanation

Hidden formulas can produce emergent markets, but unexplained refusals violate the
instruction and Field Guide principles. Players should be able to improve a listing or
choose a different market without consulting external archaeology.

### A central exchange with automatic clearing

An exchange would make liquidity convenient, but it would flatten geography and remove the
settlement and route work that should make a merchant valuable. It also creates a single,
large failure and save-integrity surface.

### Emeralds and vanilla trade prices only

Keeping the existing currency and pricing would reduce implementation work, but it would
preserve the trading-hall path as the default route and leave player shops subordinate to
vanilla assumptions. Obols and willingness-to-pay support a separate economic role.
