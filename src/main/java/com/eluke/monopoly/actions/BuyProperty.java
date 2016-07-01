/**
 *
 */
package com.eluke.monopoly.actions;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.eluke.monopoly.Game;
import com.eluke.monopoly.MonopolySet;
import com.eluke.monopoly.OwnedProperty;
import com.eluke.monopoly.Player;
import com.eluke.monopoly.Property;

/**
 * @author luke
 *
 */
public class BuyProperty implements PlayerAction {
	private final Property propertyToBuy;
	private final Player player;
	private final Map<Property, Player> ownedProperties;
	private final Set<Property> unownedProperties;

	public BuyProperty(final Property propertyToBuy, final Player player,
			final Map<Property, Player> ownedProperties,
			final Set<Property> unownedProperties) {
		this.propertyToBuy = propertyToBuy;
		this.player = player;
		this.ownedProperties = ownedProperties;
		this.unownedProperties = unownedProperties;
	}

	@Override
	public void execute(final Game game) {
		int cashRequired = propertyToBuy.getBuyPrice();

		player.payCashToPlayer(cashRequired, Player.BANK, game);

		boolean wasUnowned = unownedProperties.remove(propertyToBuy);
		if (wasUnowned) {
			ownedProperties.put(propertyToBuy, player);
		}
		else {
			throw new IllegalStateException(player + " tried to buy already owned property " + propertyToBuy);
		}

		player.addOwnedProperty(propertyToBuy);

		markSetAsMonopolyIfNeeded();
	}

	private void markSetAsMonopolyIfNeeded() {
		MonopolySet propertyMonopolySet = propertyToBuy.getMonopolySet();
		Stream<Property> allProperties = Stream.concat(ownedProperties.keySet().stream(), unownedProperties.stream());
		List<Property> allPropertiesInThisSet = allProperties
				.filter( property -> property.getMonopolySet() == propertyMonopolySet)
				.collect(Collectors.toList());
		List<OwnedProperty> ownedProperties = player.getOwnedProperties().stream()
				.filter( property -> property.getProperty().getMonopolySet() == propertyMonopolySet)
				.collect(Collectors.toList());
		if (ownedProperties.size() == allPropertiesInThisSet.size()) {
			ownedProperties.stream().forEach( property -> property.setAsPartOfMonopoly(true));
		}
	}

}
