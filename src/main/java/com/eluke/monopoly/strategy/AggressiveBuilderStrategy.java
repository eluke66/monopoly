package com.eluke.monopoly.strategy;

import static com.eluke.monopoly.strategy.StrategyUtilities.findPropertiesToUnimprove;
import static com.eluke.monopoly.strategy.StrategyUtilities.improvableProperties;
import static com.eluke.monopoly.strategy.StrategyUtilities.improvementsPerMonopolySet;
import static com.eluke.monopoly.strategy.StrategyUtilities.mortgageProperties;
import static com.eluke.monopoly.strategy.StrategyUtilities.mortgageableProperties;
import static com.eluke.monopoly.strategy.StrategyUtilities.unmortgageableProperties;

import java.util.Collection;
import java.util.IntSummaryStatistics;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.eluke.monopoly.Game;
import com.eluke.monopoly.MonopolySet;
import com.eluke.monopoly.OwnedProperty;
import com.eluke.monopoly.Player;
import com.eluke.monopoly.PlayerStrategy;
import com.eluke.monopoly.Property;
import com.eluke.monopoly.actions.ImproveProperty;
import com.eluke.monopoly.actions.PlayerAction;
import com.eluke.monopoly.actions.UnmortgageProperty;

public class AggressiveBuilderStrategy implements PlayerStrategy {
	private static final int MIN_HOUSES_DESIRED = 3;

	@Override
	public boolean wantsToBuy(final Property property, final Player player) {
		// Buy it if we have the cash
		return property.getBuyPrice() <= player.getCash();
	}

	@Override
	public Optional<PlayerAction> getPreTurnAction(final Player player, final Game game) {
		// Always improve properties if we have the cash.
		Optional<OwnedProperty> propertyToImprove = getPropertyToImprove(player, player.getCash(), game);
		if (propertyToImprove.isPresent()) {
			return Optional.of(new ImproveProperty(propertyToImprove.get()));
		}

		// Otherwise, unmortgage if possible
		Optional<OwnedProperty> propertyToUnmortgage = getPropertyToUnmortgage(player, player.getCash());
		if (propertyToUnmortgage.isPresent()) {
			return Optional.of(new UnmortgageProperty(propertyToUnmortgage.get()));
		}
		return Optional.empty();
	}

	private Optional<OwnedProperty> getPropertyToUnmortgage(final Player player, final int cashAvailable) {
		Optional<OwnedProperty> propertyToImprove = unmortgageableProperties(player.getOwnedProperties(), cashAvailable)
				.collect(Collectors.maxBy(AggressiveBuilderStrategy::mortgagePropertiesByValue));
		return propertyToImprove;
	}

	static int mortgagePropertiesByValue(final OwnedProperty p1, final OwnedProperty p2) {
		return Boolean.compare(p1.isPartOfMonopoly(), p2.isPartOfMonopoly());
	}

	private Optional<OwnedProperty> getPropertyToImprove(final Player player, final int cashAvailable, final Game game) {
		Map<MonopolySet, IntSummaryStatistics> improvementsPerMonopolySet =
				improvementsPerMonopolySet(player.getOwnedProperties());

		Optional<OwnedProperty> propertyToImprove = improvableProperties(player.getOwnedProperties(), improvementsPerMonopolySet, cashAvailable, game)
				.collect(Collectors.maxBy(AggressiveBuilderStrategy::comparePropertiesByValue));
		return propertyToImprove;
	}

	static int comparePropertiesByValue(final OwnedProperty p1, final OwnedProperty p2) {
		int i1 = p1.getNumImprovements();
		int i2 = p2.getNumImprovements();

		// Prefer getting to 3 houses
		// Prefer cheaper properties to more expensive.
		if ( i1 < MIN_HOUSES_DESIRED && i2 < MIN_HOUSES_DESIRED ) {
			return Integer.compare(p1.getProperty().getHouseCost(), p2.getProperty().getHouseCost());
		}
		if (i1 < MIN_HOUSES_DESIRED && i2 >= MIN_HOUSES_DESIRED) {
			return -1;
		}
		if (i1 >= MIN_HOUSES_DESIRED && i2 < MIN_HOUSES_DESIRED) {
			return 1;
		}
		return Integer.compare(p1.getProperty().getHouseCost(), p2.getProperty().getHouseCost());
	}

	@Override
	public Collection<PlayerAction> raiseCashForExpense(final int totalCashRequired, final Player player) {
		Collection<PlayerAction> actions = new LinkedList<>();
		// If we have enough cash, then just return
		if (player.getCash() >= totalCashRequired) {
			return actions;
		}

		int cashRequired = totalCashRequired - player.getCash();
		Collection<OwnedProperty> availableProperties = new LinkedList<>(player.getOwnedProperties());

		// Mortgage non-monopoly properties.
		cashRequired = mortgageNonMonopolies(actions, cashRequired, availableProperties);
		if (cashRequired <= 0) {
			return actions;
		}

		Map<OwnedProperty,Integer> currentImprovementsPerProperty =
				availableProperties.stream()
				.collect(Collectors.toMap(Function.identity(), OwnedProperty::getNumImprovements));

		// Keep properties with >= 3 houses.
		cashRequired = removeLowValueImprovements(actions, cashRequired, availableProperties, currentImprovementsPerProperty);
		if (cashRequired <= 0) {
			return actions;
		}

		// Sell all improvements
		cashRequired = removeAllImprovements(actions, cashRequired, availableProperties, currentImprovementsPerProperty);
		if (cashRequired <= 0) {
			return actions;
		}

		// In the end, mortgage anything useful.
		mortgageAllProperties(actions, cashRequired, availableProperties);

		return actions;
	}

	private int removeAllImprovements(final Collection<PlayerAction> actions, final int cashRequired,
			final Collection<OwnedProperty> availableProperties, final Map<OwnedProperty, Integer> currentImprovementsPerProperty) {
		return findPropertiesToUnimprove(actions,cashRequired,availableProperties,currentImprovementsPerProperty,
				"sell all",
				(stream -> stream.collect(Collectors.maxBy(AggressiveBuilderStrategy::comparePropertiesByValue))));
	}

	private int removeLowValueImprovements(final Collection<PlayerAction> actions, final int cashRequired,
			final Collection<OwnedProperty> availableProperties, final Map<OwnedProperty, Integer> currentImprovementsPerProperty) {
		return findPropertiesToUnimprove(actions,cashRequired,availableProperties,currentImprovementsPerProperty,
				"being a low-value improvement",
				(stream -> stream.filter(property -> currentImprovementsPerProperty.get(property) < MIN_HOUSES_DESIRED)
						          .collect(Collectors.maxBy(AggressiveBuilderStrategy::comparePropertiesByValue))));
	}


	private int mortgageNonMonopolies(final Collection<PlayerAction> actions, final int cashRequired,
			final Collection<OwnedProperty> availableProperties) {
		List<OwnedProperty> mortgageable = mortgageableProperties(availableProperties)
				.filter(property -> !property.isPartOfMonopoly())
				.sorted((p1,p2) -> Integer.compare(p1.getProperty().getHouseCost(), p2.getProperty().getHouseCost()))
				.collect(Collectors.toList());

		return mortgageProperties(mortgageable, actions, availableProperties, cashRequired, "being not in a monopoly");
	}

	private int mortgageAllProperties(final Collection<PlayerAction> actions, final int cashRequired,
			final Collection<OwnedProperty> availableProperties) {
		List<OwnedProperty> mortgageable = availableProperties.stream()
				.filter(property -> !property.isMortgaged())
				.sorted((p1,p2) -> Integer.compare(p1.getProperty().getHouseCost(), p2.getProperty().getHouseCost()))
				.collect(Collectors.toList());

		return mortgageProperties(mortgageable, actions, availableProperties, cashRequired, "needing cash");
	}

}
