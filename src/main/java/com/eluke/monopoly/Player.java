/**
 *
 */
package com.eluke.monopoly;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

import com.eluke.monopoly.actions.MortgageProperty;
import com.eluke.monopoly.actions.PlayerAction;
import com.eluke.monopoly.actions.SellImprovement;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

/**
 * @author luke
 *
 */
public class Player {
	private static final int MAX_HOUSE_IMPROVEMENT_COUNT = 5;
	private final Collection<OwnedProperty> properties;
	private final PlayerStrategy strategy;
	private int cash;
	private int location;
	private final String name;
	private Player creditor;

	public static final Player BANK = new Player(-1, null, "BANK");

	public Player(final int cash, final PlayerStrategy strategy, final String name) {
		this.cash = cash;
		this.strategy = strategy;
		this.name = name;
		properties = new LinkedList<>();
	}

	@Override
	public String toString() {
		return name;
	}

	public void addOwnedProperty(final Property property) {
		properties.add(new OwnedProperty(property, this));
	}
	public OwnedProperty getOwnedProperty(final Property property) {
		return properties.stream().filter(p -> p.getProperty().equals(property)).findFirst().get();
	}

	public int getCash() {
		return cash;
	}
	public int getHouses() {
		int houses = 0;
		for (OwnedProperty property : properties) {
			int improvements = property.getNumImprovements() - 1; // Since we count a monopoly set as an improvement.
			if (improvements > 0 && improvements < MAX_HOUSE_IMPROVEMENT_COUNT) {
				houses += improvements;
			}
		}
		return houses;
	}

	public int getHotels() {
		int hotels = 0;
		for (OwnedProperty property : properties) {
			int improvements = property.getNumImprovements() - 1; // Since we count a monopoly set as an improvement.
			if (improvements == MAX_HOUSE_IMPROVEMENT_COUNT) {
				hotels ++;
			}
		}
		return hotels;
	}

	public void payMoney(final int value) {
		this.cash -= value;
	}

	public void receiveCash(final int value) {
		this.cash += value;
	}

	public int getLocation() {
		return this.location;
	}

	public void setLocation(final int newLocation) {
		this.location = newLocation;
	}

	public boolean isBankrupt() {
		return cash < 0;
	}
	public void setCreditor(final Player player) {
		Preconditions.checkNotNull(player);
		Preconditions.checkArgument(player != this, "Trying to set " + this + " as our own bankruptcy creditor");
		this.creditor = player;
	}
	public Player getCreditor() {
		return this.creditor;
	}

	public Collection<OwnedProperty> getOwnedProperties() {
		return properties;
	}

	public void payCashToPlayers(final int cashPerPlayer, final Collection<Player> players, final Game game) {
		int totalCashRequired = cashPerPlayer*(players.size()-1);
		raiseCashForExpense(totalCashRequired, game);
		payMoney(totalCashRequired);

		if (isBankrupt()) {
			Iterator<Player> it = players.iterator();
			while (it.hasNext()) {
				Player creditor = it.next();
				if (creditor != this) {
					setCreditor(creditor);
					break;
				}
			}
			if (this.creditor == null) {
				throw new IllegalStateException(this +" is bankrupt by paying " + players + " but cannot find a creditor");
			}
		}

		for (Player player : players) {
			if (!player.equals(this)) {
				player.receiveCash(cashPerPlayer);
			}
		}
	}


	public void payCashToPlayer(final int totalCashRequired, final Player player, final Game game) {
		raiseCashForExpense(totalCashRequired, game);
		payMoney(totalCashRequired);
		int cashToPay = totalCashRequired;

		if (isBankrupt()) {
			setCreditor(player);
		}

		player.receiveCash(cashToPay);
	}

	private void raiseCashForExpense(final int totalCashRequired, final Game game) {
		Collection<PlayerAction> actions = strategy.raiseCashForExpense(totalCashRequired, this);

		actions.stream()
		.sorted(Player::actionExecutionOrder)
		.forEach(action -> action.execute(game));
	}

	static int actionExecutionOrder(final PlayerAction a, final PlayerAction b) {
		Map<Class<? extends PlayerAction>, Integer> valuesPerClass =
				ImmutableMap.of( SellImprovement.class, 1, MortgageProperty.class, 2);
		// Do sell improvement first, followed by mortgage, followed by everything else
		int aValue = valuesPerClass.getOrDefault(a.getClass(), 3);
		int bValue = valuesPerClass.getOrDefault(b.getClass(), 3);
		return Integer.compare(aValue, bValue);
	}

	public boolean wantsToBuy(final Property property) {
		return strategy.wantsToBuy(property, this);
	}

	public Optional<PlayerAction> getPreTurnAction(final Game game) {
		return strategy.getPreTurnAction(this, game);
	}
}
