package com.eluke.monopoly.strategy;

import java.util.Collection;
import java.util.IntSummaryStatistics;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eluke.monopoly.Game;
import com.eluke.monopoly.MonopolySet;
import com.eluke.monopoly.OwnedProperty;
import com.eluke.monopoly.Player;
import com.eluke.monopoly.PlayerStrategy;
import com.eluke.monopoly.Property;
import com.eluke.monopoly.actions.ImproveProperty;
import com.eluke.monopoly.actions.MortgageProperty;
import com.eluke.monopoly.actions.PlayerAction;
import com.eluke.monopoly.actions.SellImprovement;
import com.eluke.monopoly.actions.UnmortgageProperty;

public class AggressiveBuilderStrategy implements PlayerStrategy {
	private static final Logger logger = LoggerFactory.getLogger(AggressiveBuilderStrategy.class);

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

		Optional<OwnedProperty> propertyToUnmortgage = getPropertyToUnmortgage(player, player.getCash());
		if (propertyToUnmortgage.isPresent()) {
			return Optional.of(new UnmortgageProperty(propertyToUnmortgage.get()));
		}
		return Optional.empty();
	}

	private Optional<OwnedProperty> getPropertyToUnmortgage(final Player player, final int cashAvailable) {
		Optional<OwnedProperty> propertyToImprove = player.getOwnedProperties().stream()
				.filter(ownedProperty -> ownedProperty.isMortgaged())
				.filter(ownedProperty -> ownedProperty.getProperty().getUnMortgageValue() <= cashAvailable)
				.collect(Collectors.maxBy(AggressiveBuilderStrategy::mortgagePropertiesByValue));
		return propertyToImprove;
	}

	static int mortgagePropertiesByValue(final OwnedProperty p1, final OwnedProperty p2) {
		return Boolean.compare(p1.isPartOfMonopoly(), p2.isPartOfMonopoly());
	}

	private Optional<OwnedProperty> getPropertyToImprove(final Player player, final int cashAvailable, final Game game) {
		Map<MonopolySet, IntSummaryStatistics> improvementsPerMonopolySet =
				player.getOwnedProperties().stream()
				.collect(Collectors.groupingBy(
						op -> op.getProperty().getMonopolySet(),
						Collectors.summarizingInt(OwnedProperty::getNumImprovements)));

		Optional<OwnedProperty> propertyToImprove = player.getOwnedProperties().stream()
				.filter(ownedProperty -> ownedProperty.getProperty().canImprove())
				.filter(ownedProperty -> ownedProperty.getNumImprovements() < (ownedProperty.getProperty().getRentCosts().length-1))
				.filter(ownedProperty -> !ownedProperty.isMortgaged())
				.filter(ownedProperty -> ownedProperty.isPartOfMonopoly())
				.filter(ownedProperty -> ownedProperty.getProperty().getHouseCost() <= cashAvailable)
				.filter(ownedProperty -> ownedProperty.getNumImprovements() == improvementsPerMonopolySet.get(ownedProperty.getProperty().getMonopolySet()).getMin())
				.filter(ownedProperty -> game.moreImprovementsAvailable(ownedProperty.getNextImprovementType()))
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

		int currentCashRequired = cashRequired;

		while (currentCashRequired > 0) {
			Optional<OwnedProperty> propertyToUnimprove = availableProperties.stream()
					.filter(property -> !property.isMortgaged())
					.filter(property -> currentImprovementsPerProperty.get(property) > 0)
					.collect(Collectors.maxBy(AggressiveBuilderStrategy::comparePropertiesByValue));
			if (propertyToUnimprove.isPresent()) {
				OwnedProperty property = propertyToUnimprove.get();
				logger.info("Unimproving " + property.getProperty().getName() + " due to sell all");
				actions.add(new SellImprovement(property));
				currentImprovementsPerProperty.put(property, currentImprovementsPerProperty.get(property)-1);
				currentCashRequired -= property.getProperty().getHouseCost()/2;
			}
			else {
				// No more properties fitting this description
				break;
			}
		}
		return currentCashRequired;
	}

	private int removeLowValueImprovements(final Collection<PlayerAction> actions, final int cashRequired,
			final Collection<OwnedProperty> availableProperties, final Map<OwnedProperty, Integer> currentImprovementsPerProperty) {

		int currentCashRequired = cashRequired;
		while (currentCashRequired > 0) {
			Optional<OwnedProperty> propertyToUnimprove = availableProperties.stream()
					.filter(property -> !property.isMortgaged())
					.filter(property -> currentImprovementsPerProperty.get(property) > 0)
					.filter(property -> currentImprovementsPerProperty.get(property) < MIN_HOUSES_DESIRED)
					.collect(Collectors.maxBy(AggressiveBuilderStrategy::comparePropertiesByValue));

			if (propertyToUnimprove.isPresent()) {
				OwnedProperty property = propertyToUnimprove.get();
				logger.info("Unimproving " + property.getProperty().getName() + " due to being a low-value improvement");
				actions.add(new SellImprovement(property));
				currentImprovementsPerProperty.put(property, currentImprovementsPerProperty.get(property)-1);
				currentCashRequired -= property.getProperty().getHouseCost()/2;
			}
			else {
				// No more properties fitting this description
				break;
			}
		}
		return currentCashRequired;
	}

	private int mortgageNonMonopolies(final Collection<PlayerAction> actions, int cashRequired,
			final Collection<OwnedProperty> availableProperties) {
		List<OwnedProperty> mortgageable = availableProperties.stream()
				.filter(property -> !property.isMortgaged())
				.filter(property -> property.getNumImprovements() == 0)
				.filter(property -> !property.isPartOfMonopoly())
				.sorted((p1,p2) -> Integer.compare(p1.getProperty().getHouseCost(), p2.getProperty().getHouseCost()))
				.collect(Collectors.toList());

		Iterator<OwnedProperty> iterator = mortgageable.iterator();
		while (cashRequired > 0 && iterator.hasNext()) {
			OwnedProperty property = iterator.next();
			logger.info("Mortgaging " + property.getProperty().getName() + " due to being not in a monopoly");
			actions.add(new MortgageProperty(property));
			availableProperties.remove(property);
			cashRequired -= property.getProperty().getMortgageValue();
		}
		return cashRequired;
	}

	private int mortgageAllProperties(final Collection<PlayerAction> actions, int cashRequired,
			final Collection<OwnedProperty> availableProperties) {
		List<OwnedProperty> mortgageable = availableProperties.stream()
				.filter(property -> !property.isMortgaged())
				.sorted((p1,p2) -> Integer.compare(p1.getProperty().getHouseCost(), p2.getProperty().getHouseCost()))
				.collect(Collectors.toList());

		Iterator<OwnedProperty> iterator = mortgageable.iterator();
		while (cashRequired > 0 && iterator.hasNext()) {
			OwnedProperty property = iterator.next();
			logger.info("Mortgaging " + property.getProperty().getName() + " due to needing cash");
			actions.add(new MortgageProperty(property));
			availableProperties.remove(property);
			cashRequired -= property.getProperty().getMortgageValue();
		}
		return cashRequired;
	}

}
