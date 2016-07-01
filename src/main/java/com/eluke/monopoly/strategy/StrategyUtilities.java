package com.eluke.monopoly.strategy;

import java.util.Collection;
import java.util.IntSummaryStatistics;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eluke.monopoly.Game;
import com.eluke.monopoly.MonopolySet;
import com.eluke.monopoly.OwnedProperty;
import com.eluke.monopoly.actions.MortgageProperty;
import com.eluke.monopoly.actions.PlayerAction;
import com.eluke.monopoly.actions.SellImprovement;

public class StrategyUtilities {
	private static final Logger logger = LoggerFactory.getLogger(StrategyUtilities.class);

	public static Map<MonopolySet, IntSummaryStatistics> improvementsPerMonopolySet(final Collection<OwnedProperty> properties) {
		return properties.stream()
				.collect(Collectors.groupingBy(
						op -> op.getProperty().getMonopolySet(),
						Collectors.summarizingInt(OwnedProperty::getNumImprovements)));
	}

	public static Stream<OwnedProperty> improvableProperties(final Collection<OwnedProperty> properties,
			final Map<MonopolySet, IntSummaryStatistics> improvementsPerMonopolySet,
			final int cashAvailable,
			final Game game) {
		return properties.stream()
				.filter(ownedProperty -> ownedProperty.getProperty().canImprove())
				.filter(ownedProperty -> ownedProperty.getNumImprovements() < (ownedProperty.getProperty().getRentCosts().length-1))
				.filter(ownedProperty -> !ownedProperty.isMortgaged())
				.filter(ownedProperty -> ownedProperty.isPartOfMonopoly())
				.filter(ownedProperty -> ownedProperty.getProperty().getHouseCost() <= cashAvailable)
				.filter(ownedProperty -> ownedProperty.getNumImprovements() == improvementsPerMonopolySet.get(ownedProperty.getProperty().getMonopolySet()).getMin())
				.filter(ownedProperty -> game.moreImprovementsAvailable(ownedProperty.getNextImprovementType()));
	}

	public static Stream<OwnedProperty> unimprovableProperties(final Collection<OwnedProperty> properties,
			final Map<OwnedProperty, Integer> currentImprovementsPerProperty) {
		return properties.stream()
				.filter(property -> !property.isMortgaged())
				.filter(property -> currentImprovementsPerProperty.get(property) > 0);
	}

	public static Stream<OwnedProperty> unmortgageableProperties(final Collection<OwnedProperty> properties, final int cashAvailable) {
		return properties.stream()
				.filter(ownedProperty -> ownedProperty.isMortgaged())
				.filter(ownedProperty -> ownedProperty.getProperty().getUnMortgageValue() <= cashAvailable);
	}

	public static Stream<OwnedProperty> mortgageableProperties(final Collection<OwnedProperty> properties) {
		return properties.stream()
				.filter(property -> !property.isMortgaged())
				.filter(property -> property.getNumImprovements() == 0);
	}


	public static int mortgageProperties(final List<OwnedProperty> sortedProperties, final Collection<PlayerAction> actions, final Collection<OwnedProperty> availableProperties,
			final int totalCashRequired, final String description) {
		Iterator<OwnedProperty> iterator = sortedProperties.iterator();
		int cashRequired = totalCashRequired;
		while (cashRequired > 0 && iterator.hasNext()) {
			OwnedProperty property = iterator.next();
			logger.info("Mortgaging " + property.getProperty().getName() + " due to " + description);
			actions.add(new MortgageProperty(property));
			availableProperties.remove(property);
			cashRequired -= property.getProperty().getMortgageValue();
		}
		return cashRequired;
	}

	public static int findPropertiesToUnimprove(
			final Collection<PlayerAction> actions,
			final int cashRequired,
			final Collection<OwnedProperty> availableProperties,
			final Map<OwnedProperty, Integer> currentImprovementsPerProperty,
			final String description,
			final Function<Stream<OwnedProperty>,Optional<OwnedProperty>> propertyFinder)
	{
		int currentCashRequired = cashRequired;
		while (currentCashRequired > 0) {
			Optional<OwnedProperty> propertyToUnimprove =
					propertyFinder.apply(unimprovableProperties(availableProperties,currentImprovementsPerProperty));

			if (propertyToUnimprove.isPresent()) {
				OwnedProperty property = propertyToUnimprove.get();
				logger.info("Unimproving " + property.getProperty().getName() + " due to " + description);
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
}
