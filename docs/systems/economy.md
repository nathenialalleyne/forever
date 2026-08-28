# Economy

## Status
**Concept. NOT implemented.** No Obols ledger, Coin Purse, shop service, NPC demand model, contract record, or material-order workflow exists. The supplied foundation brief names no dedicated economy ticket, so implementation must receive an explicit FVR ticket before code. Field Guide integration is FVR-200..204 and storage integration is FVR-600..604.

## Purpose
The economy gives players an alternative route to abundance through exchange, stewardship, and reliable supply. It should make a cartographer, merchant, farmer, smith, or settlement useful without becoming a full financial simulator that demands spreadsheets or punishes players who prefer direct production.

The economy uses categories, bounded willingness, and physical goods. Money is a convenience for exchange, not a source of magical production. A player may sell directly, operate a shop, negotiate a contract, or simply build and gather as before.

## Player experience
A player finds physical Obols as treasure or receives them in a transfer when a tangible token makes sense. Everyday balances live in a Coin Purse so the player does not fill several inventory rows with currency. Obols can be withdrawn, deposited, gifted, or carried, subject to clear rules and loss risk.

A player shop has free pricing. The owner chooses what to sell and at what price. NPCs have a willingness-to-pay range informed by category, local demand, quality, distance, and current alternatives. An implausibly expensive item simply does not sell. The system does not secretly force a sale to make the shop look successful.

Demand appears as readable categories such as building stone, preserved food, repair inputs, or regional herbs. A settlement may post a bulk material order with quantity, acceptable substitutes, quality requirements, destination, deadline, and payment. A contract records the promise and validates delivery rather than presenting a chain of filler errands.

## Core rules
- Obols are the hybrid currency under ADR 0008. Physical Obols remain transferable items for treasure, gifts, and deliberate settlement. The Coin Purse is a compact balance for ordinary exchange.
- Withdrawal and deposit are explicit, atomic operations. A purse balance cannot be duplicated by reconnecting, trading, or moving between dimensions.
- Prices belong to shops, contracts, and data-defined NPC offers. Under ADR 0017, player shops may choose any non-negative price within a safe numeric range. Free pricing means the game does not force a suggested price.
- NPC willingness-to-pay is a bounded decision, not a guaranteed conversion. It considers category need, quality or trait tags, local supply, alternatives, distance, and a data-defined tolerance.
- NPC demand is recorded at category level. It should say “preserved food” or “roofing material” before it asks for a particular item, unless a contract explicitly requires a concrete tag.
- Demand must not become an infinite sink. Each settlement has bounded stock targets and replenishment intervals. Surplus lowers urgency and may lower willingness without deleting goods.
- Bulk material orders are physical commitments. The buyer reserves a payment and the seller or logistics service reserves actual goods before a shipment is considered fulfilled.
- Contracts have a scope, participants, terms, expiry, cancellation rules, delivery location, and consequence. A player can decline a contract without losing access to ordinary play.
- A contract can accept declared substitutes when its category and quality rules allow them. The Field Guide displays those rules before acceptance.
- Economic categories are the primary abstraction. The system tracks enough supply, demand, quality, and reliability to make decisions, not every individual household expenditure or a complete banking history.
- Solo players can satisfy a category through gathering, farming, preservation, crafting, import, or direct delivery. No required contract depends on another human player.
- Infrastructure can improve margins by reducing travel and spoilage. It must not create free goods or make geography irrelevant from the first transaction.
- Economic rewards should include reputation, access, specialist migration, and future opportunities as well as Obols. Currency is not the only measure of success.
- Prices, category weights, order sizes, expiry periods, and willingness formulas are balance data, not hard-coded magic numbers.

A category record may contain:

| Field | Reason |
|---|---|
| category and tags | readable demand and substitution rules |
| local stock band | urgency without infinite consumption |
| willingness band | NPC purchase decision |
| active orders | visible commitments and deadlines |
| supplier reliability | consequence of late or failed delivery |
| price reference | optional guidance, never forced player pricing |

## Data ownership
The server owns the Coin Purse on the player attachment or component and owns shop, demand, contract, and category records in world-scoped saved data. Physical goods remain in inventories, warehouses, shipments, and receiving buffers. Indexes and category summaries are derived from those physical locations.

Every purse and contract mutation uses a versioned record and an idempotency key. Definitions for categories, formulas, and reward bands live in data resources. A price or demand summary must never replace the physical source of truth for an item.

## Server/client responsibilities
The server decides balances, prices accepted, NPC willingness, demand changes, reservations, contract outcomes, and rewards. The client displays a shop, order, or contract proposal and sends an explicit accept, decline, list, or delivery request.

Client screens receive bounded category and transaction summaries. They cannot calculate a sale by trusting a local price or remove goods from a remote warehouse. A stale shop view must be revalidated by the server before any exchange.

## Dependencies
- FVR-200..204 document categories, price explanations, contracts, and Coin Purse actions in the Field Guide.
- FVR-600..604 provide local warehouse visibility and the physical inventory model that economy queries must respect.
- Settlements provide demand locations, workplaces, shops, and service capacities. Logistics provides physical movement for bulk orders.
- Villager careers and mastery can create quality, reliability, and specialist supply, while food, alchemy, equipment, and exploration supply category tags.
- Obols follow ADR 0008 and the Mason slice FVR-500..507, especially FVR-506. No dependency may introduce a second hidden currency without an explicit decision.

## Extension points
A modded item adapter can map an item to stable economic categories, quality tags, and substitution rules. A shop provider can implement player, NPC, cooperative, or specialist sellers behind the same server contract.

A future regional market can add category-specific demand and route costs without simulating a global exchange. New contract types must declare their physical goods, reward, expiry, and failure semantics before registration.

## Failure cases
- A purse transaction interrupted by restart commits either the old balance or the complete new balance. It cannot mint or consume a partial amount.
- A sale with a stale shop view is rechecked against current inventory, price, and willingness. Failed revalidation leaves the goods untouched.
- A shop or contract references an unknown category. It is disabled with an actionable error and does not accept arbitrary items.
- A demand spike cannot consume goods without a committed order or physical delivery. Repeated ticks are idempotent.
- A participant quits or a settlement becomes unavailable. Contract state moves to a documented grace, cancellation, or recovery state rather than silently paying twice.
- A counterfeit or malformed physical Obol is rejected without deleting unrelated inventory.

## Performance constraints
Demand updates run on category summaries at a bounded interval, not on every block or item tick. A settlement should evaluate no more than 256 category records per update and no more than 64 active contracts in one UI page.

A shop query returns at most 100 listings and a category query at most 64 demand rows. Price evaluation reads summaries and relevant item metadata, never the contents of every warehouse. Historical transactions are aggregated after they stop affecting active decisions.

## Accessibility and documentation requirements
Shop and contract screens must show price, quantity, willingness result, category, substitutes, deadline, destination, and failure reason as text. Currency must have an item icon and a textual amount. No sale state may rely on colour alone.

The Field Guide must explain that player pricing is free, NPCs may decline, demand is categorical, bulk orders require real goods, Obols and the Coin Purse differ, and contracts are optional. It must include examples that do not assume a multiplayer economy.

## Non-goals
- This is not a stock market, bank, loan, interest, tax, or speculative trading simulator.
- It does not guarantee that every player shop sells, regardless of price.
- It does not create infinite NPC demand or delete surplus inventory to keep numbers tidy.
- It does not replace physical storage, logistics, crafting, farming, or exploration.
- It does not require a player to use money for ordinary Minecraft survival.

## Open balance questions
- What categories are broad enough to be readable but specific enough to guide production?
- How much should local supply affect NPC willingness before a player feels a market is arbitrary?
- What is a useful bulk-order size for early, mid, and late settlements?
- When should a shop earn reputation or specialist access instead of more currency?
- How much physical Obol risk is interesting without making the Coin Purse feel pointless?

## Planned tests
- Round-trip Coin Purse deposits, withdrawals, gifts, trades, and dimension changes without duplication.
- Verify a player shop accepts free prices and that NPC willingness can decline without mutating stock.
- Fulfil, partially fulfil, expire, cancel, and substitute a bulk material order using real physical goods.
- Crash during purse mutation, sale, reservation, and contract delivery and assert exactly-once outcomes.
- Test category demand caps, surplus behaviour, and bounded updates across multiple settlements.
- Query shops and warehouses with large physical inventories and verify bounded payloads and no full-world scan.
- Verify solo acquisition paths and clear Field Guide explanations for rejected sales and contract failures.
