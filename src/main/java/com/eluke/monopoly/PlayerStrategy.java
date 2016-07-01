package com.eluke.monopoly;

import java.util.Collection;
import java.util.Optional;

import com.eluke.monopoly.actions.PlayerAction;

public interface PlayerStrategy {
	public boolean wantsToBuy(Property property, Player player);
	public Optional<PlayerAction> getPreTurnAction(Player player, Game game);
	public Collection<PlayerAction> raiseCashForExpense(final int totalCashRequired, Player player);
}
