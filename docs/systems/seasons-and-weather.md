# Seasons and weather

## Status
**Concept. NOT implemented.** No climate abstraction, seasonal calendar, weather situation model, greenhouse integration, or Serene Seasons adapter exists. Field Guide documentation is FVR-200..204. A dedicated seasons implementation ticket must be assigned before code.

## Purpose
Visible seasons and weather should give regions a changing identity and create modest situations for planning. They must not turn survival into long punitive crop lockouts or make the player wait for a calendar before ordinary play is possible.

The design starts with a climate adapter abstraction. Serene Seasons is the first target, but Forever's core should not depend on that mod's identifiers or assume it will always be installed.

## Player experience
Spring, summer, autumn, and winter are visibly different through environmental cues, vegetation or sky presentation where supported, and clear calendar language. Weather can create useful situations such as a good time to travel by boat, a risk to an exposed crop, or a reason to prepare warming food.

Mechanical effects remain modest. A crop may grow more reliably in its preferred season, an animal may behave differently, or a route may become less comfortable. A player can reduce inconvenience through a greenhouse, crop selection, preservation, trade, import, shelter, or a better route. A missed season is a planning problem, not a months-long punishment.

## Core rules
- Core systems consume the climate adapter abstraction in ADR 0014 with season, local climate profile, weather situation, and confidence or availability. They do not import Serene Seasons internals.
- The first concrete climate adapter target is Serene Seasons. Without it, a no-op or vanilla-compatible fallback must keep ordinary play functional.
- Seasonal variation is visible and predictable enough to plan. The player can inspect current season, forecast band, and local conditions.
- Mechanical bonuses and penalties are modest, bounded, and attached to situations. They must not make every crop impossible for a long lockout.
- Crop and ingredient availability may vary by season, but greenhouse cultivation, suitable alternatives, preservation, import, and trade reduce inconvenience.
- A greenhouse is a functional infrastructure option. It does not require an aesthetic blueprint and should be evaluated by climate control, enclosure, light, water, and capacity where relevant.
- Weather situations can affect travel, fishing, animal handling, food comfort, or project work. They should create choices and preparation, not arbitrary item loss.
- Forecasts are not perfect omniscience if uncertainty is part of the intended experience, but the uncertainty band and consequence must be visible.
- Seasonal bonuses never override safety entirely. Warming food does not make extreme exposure harmless, and a greenhouse does not make every input free.
- Trade and import are valid solutions to a local seasonal gap. Geography remains meaningful through cost, route time, and regional identity.
- Existing vanilla and modded crops must have a documented climate mapping or safe fallback. Unknown crops are not silently killed by an invented winter rule.
- Climate changes are server-authoritative and persist through restart with a versioned calendar state.

A climate capability should answer:

| Query | Example response |
|---|---|
| season | early autumn |
| local profile | temperate river valley |
| current weather | heavy rain, short duration |
| crop suitability | preferred, possible, or unsuitable |
| mitigation | greenhouse, preservation, import, or trade |

## Data ownership
The server owns the calendar anchor, season phase, weather situations, local climate mappings, and greenhouse or mitigation records in versioned world data. Crop and effect definitions are content data. Player discovery of a climate fact belongs in player knowledge when appropriate.

An adapter owns external mod detection and mapping. Forever stores stable climate concepts and migration versions, not Serene Seasons field names or internal state formats.

## Server/client responsibilities
The server advances time, resolves climate effects, validates greenhouses and mitigations, and applies crop, animal, food, and travel outcomes. The client displays season, forecast, suitability, warnings, and options. It cannot grow a crop or declare a weather situation complete.

Network updates should send changes or summaries, not a full climate calculation for every chunk. Client forecasts are projections and may be stale, so the server rechecks time-sensitive actions.

## Dependencies
- FVR-200..204 document seasons, forecasts, greenhouse rules, and mitigation choices.
- Serene Seasons is the first optional climate adapter target. The core abstraction permits no-op fallback and future climate providers.
- Food and alchemy, animals, farming, transportation, regions, and projects consume stable climate capabilities.
- Settlements may register greenhouses or sheltered workplaces. Economy and logistics provide import and trade routes.
- All seasons, thresholds, bonuses, and penalties are balance data under principle 12.

## Extension points
A climate adapter can provide season phase, local profile, weather situation, forecast, and crop suitability. A mitigation provider can add greenhouse, shelter, irrigation, preservation, or trade support with a clear capability contract.

A future biome or dimension adapter can map local conditions without changing core consumers. New weather situations must declare visibility, duration, effects, mitigation, and fallback behaviour.

## Failure cases
- Serene Seasons is missing, incompatible, or changed. The adapter reports unavailable and the no-op or vanilla fallback keeps ordinary crops and travel working.
- A crop or animal mapping is unknown. It uses a safe neutral profile and is not destroyed by a guessed seasonal rule.
- A calendar migration is interrupted. The previous phase remains valid and the new phase is applied once, not twice.
- A greenhouse validation becomes stale after a block change. Its bonus pauses with a reason, while plants remain physical and recoverable.
- A weather event ends during a client interaction. The server rechecks the situation and explains any changed result.
- An invalid forecast or extreme multiplier is rejected at data load rather than applied to a whole world.

## Performance constraints
The calendar advances from world time and does not tick every crop individually. Climate effects are evaluated when a crop, animal, travel action, or project needs them, with cached local profiles and invalidation on relevant changes.

A weather update should touch only loaded entities and registered climate services. Greenhouse validation is explicit and bounded to at most 32,768 blocks by default. No seasonal feature may force-load chunks or scan every farm each tick.

## Accessibility and documentation requirements
The HUD and Field Guide must show season, weather, forecast band, and crop suitability as text plus icons. Warnings need a non-colour cue and should name the mitigation instead of only saying “bad conditions”.

The Field Guide must explain the climate adapter fallback, Serene Seasons compatibility, modest effects, greenhouse function, and the alternatives of suitable crops, preservation, import, and trade. It must clearly say that long punitive crop lockouts are not intended.

## Non-goals
- This is not a hard seasonal crop ban that locks ordinary food for long periods.
- It is not a weather-disaster simulator that randomly destroys a settlement.
- It does not require Serene Seasons to be installed for Forever to run.
- It does not make greenhouses mandatory or require a visual style.
- It does not expose third-party climate internals outside the compatibility adapter.

## Open balance questions
- How visible should seasonal variation be when a resource pack or optional climate mod is absent?
- What is the right strength of a greenhouse before it invalidates regional farming?
- Which weather situations create interesting choices without interrupting routine work too often?
- How much forecast uncertainty feels atmospheric rather than unfair?
- Which modded crops need explicit mappings and which can use a neutral fallback?

## Planned tests
- Run with Serene Seasons present, absent, incompatible, and changed and verify stable fallback behaviour.
- Advance seasons through crop, animal, food, travel, and project situations and assert modest bounded effects.
- Validate and invalidate a greenhouse without destroying crops or requiring a blueprint.
- Test climate calendar restart and migration around phase and weather transitions.
- Verify unknown crops and modded items use safe neutral mappings.
- Profile a large farm and multiple settlements to confirm no per-tick world scan or chunk loading.
- Check all forecast, warning, and mitigation screens for text, icons, keyboard access, and stale-state handling.
