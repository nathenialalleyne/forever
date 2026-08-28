package dev.forever.core.mastery.adapter;

import com.mojang.serialization.DataResult;
import dev.forever.core.mastery.domain.LoadoutRules;
import dev.forever.core.mastery.domain.MasteryDataException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import net.minecraft.resources.Identifier;

/** Immutable registry snapshot built from one server data reload. */
public final class MasteryRegistry {

	private final Map<Identifier, MasteryDefinition> definitions;
	private final LoadoutRules loadoutRules;

	public MasteryRegistry(List<MasteryDefinition> definitions, LoadoutRules loadoutRules) {
		Objects.requireNonNull(definitions, "definitions");
		this.loadoutRules = Objects.requireNonNull(loadoutRules, "loadoutRules");
		DataResult<LoadoutRules> rulesResult = loadoutRules.validate();
		if (rulesResult.error().isPresent()) {
			throw new MasteryDataException(rulesResult.error().orElseThrow().message());
		}
		if (definitions.isEmpty()) {
			throw new MasteryDataException("Mastery data must define at least one mastery path.");
		}

		TreeMap<Identifier, MasteryDefinition> byId = new TreeMap<>();
		for (MasteryDefinition definition : definitions) {
			Objects.requireNonNull(definition, "definitions cannot contain null");
			DataResult<MasteryDefinition> definitionResult = definition.validate();
			if (definitionResult.error().isPresent()) {
				throw new MasteryDataException(definitionResult.error().orElseThrow().message());
			}
			MasteryDefinition previous = byId.putIfAbsent(definition.id(), definition);
			if (previous != null) {
				throw new MasteryDataException("Duplicate mastery definition ID '" + definition.id()
						+ "'. Each path must have one stable definition.");
			}
		}

		validateDependencies(byId);
		this.definitions = Collections.unmodifiableMap(new TreeMap<>(byId));
	}

	public MasteryRegistry(Map<Identifier, MasteryDefinition> definitions, LoadoutRules loadoutRules) {
		this(new ArrayList<>(validatedMap(definitions).values()), loadoutRules);
	}

	private static Map<Identifier, MasteryDefinition> validatedMap(
			Map<Identifier, MasteryDefinition> definitions) {
		Objects.requireNonNull(definitions, "definitions");
		for (Map.Entry<Identifier, MasteryDefinition> entry : definitions.entrySet()) {
			if (entry.getKey() == null || entry.getValue() == null) {
				throw new MasteryDataException("Mastery registry map cannot contain null IDs or definitions.");
			}
			if (!entry.getKey().equals(entry.getValue().id())) {
				throw new MasteryDataException("Mastery registry map key '" + entry.getKey()
						+ "' does not match definition ID '" + entry.getValue().id() + "'.");
			}
		}
		return definitions;
	}

	public Map<Identifier, MasteryDefinition> definitions() {
		return definitions;
	}

	public List<MasteryDefinition> orderedDefinitions() {
		return List.copyOf(definitions.values());
	}

	public Set<Identifier> definitionIds() {
		return Set.copyOf(definitions.keySet());
	}

	public Optional<MasteryDefinition> definition(Identifier id) {
		return Optional.ofNullable(definitions.get(id));
	}

	public boolean contains(Identifier id) {
		return definitions.containsKey(id);
	}

	public LoadoutRules loadoutRules() {
		return loadoutRules;
	}

	public MasteryLoadout initialLoadout() {
		Identifier first = definitions.keySet().iterator().next();
		return new MasteryLoadout(Optional.of(first), List.of());
	}

	/** Validates a proposed active loadout against this registry snapshot. */
	public DataResult<MasteryLoadout> validateLoadout(MasteryLoadout loadout) {
		Objects.requireNonNull(loadout, "loadout");
		return loadout.validate(loadoutRules, definitions.keySet());
	}

	/**
	 * Deactivates IDs removed from a data pack without touching any permanent progress.
	 * A stable first definition is selected if the old Focus no longer exists.
	 */
	public MasteryLoadout deactivateUnknown(MasteryLoadout stored) {
		Objects.requireNonNull(stored, "stored");
		Optional<Identifier> focus = stored.focus().filter(this::contains);
		Identifier knownFocus = focus.orElse(null);
		List<Identifier> knownSupporting = stored.supporting().stream()
				.filter(this::contains)
				.filter(id -> knownFocus == null || !knownFocus.equals(id))
				.distinct()
				.limit(loadoutRules.supportingSlots())
				.toList();
		if (focus.isEmpty()) {
			Identifier replacement = definitions.keySet().iterator().next();
			focus = Optional.of(replacement);
			knownSupporting = knownSupporting.stream()
					.filter(id -> !replacement.equals(id))
					.limit(loadoutRules.supportingSlots())
					.toList();
		}
		return new MasteryLoadout(focus, knownSupporting);
	}

	private static void validateDependencies(Map<Identifier, MasteryDefinition> definitions) {
		for (MasteryDefinition definition : definitions.values()) {
			for (Identifier dependency : definition.dependencies()) {
				if (!definitions.containsKey(dependency)) {
					throw new MasteryDataException("Mastery '" + definition.id()
							+ "' depends on missing mastery '" + dependency + "'.");
				}
			}
		}

		Map<Identifier, VisitState> states = new HashMap<>();
		for (Identifier id : definitions.keySet()) {
			if (states.getOrDefault(id, VisitState.UNVISITED) == VisitState.UNVISITED) {
				visitDependencies(id, definitions, states, new HashSet<>());
			}
		}
	}

	private static void visitDependencies(
			Identifier id,
			Map<Identifier, MasteryDefinition> definitions,
			Map<Identifier, VisitState> states,
			Set<Identifier> path) {
		states.put(id, VisitState.VISITING);
		path.add(id);
		for (Identifier dependency : definitions.get(id).dependencies()) {
			VisitState dependencyState = states.getOrDefault(dependency, VisitState.UNVISITED);
			if (dependencyState == VisitState.VISITING) {
				throw new MasteryDataException("Mastery dependency cycle detected at '" + dependency
						+ "' while loading path " + path + ".");
			}
			if (dependencyState == VisitState.UNVISITED) {
				visitDependencies(dependency, definitions, states, path);
			}
		}
		path.remove(id);
		states.put(id, VisitState.VISITED);
	}

	private enum VisitState {
		UNVISITED,
		VISITING,
		VISITED
	}
}
