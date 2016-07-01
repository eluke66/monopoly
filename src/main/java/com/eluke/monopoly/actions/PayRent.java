/**
 *
 */
package com.eluke.monopoly.actions;

import com.eluke.monopoly.Game;
import com.eluke.monopoly.OwnedProperty;
import com.eluke.monopoly.OwnedProperty.RentOverride;
import com.eluke.monopoly.Player;

/**
 * @author luke
 *
 */
public class PayRent implements PlayerAction {
	private final Player incomingPlayer;
	private final OwnedProperty property;
	private final RentOverride override;
	private final int diceValue;


	public PayRent(final Player incomingPlayer, final OwnedProperty property, final int diceValue) {
		this.incomingPlayer = incomingPlayer;
		this.property = property;
		this.override = null;
		this.diceValue = diceValue;
	}

	public PayRent(final Player incomingPlayer, final OwnedProperty property, final int diceValue, final RentOverride override) {
		this.incomingPlayer = incomingPlayer;
		this.property = property;
		this.override = override;
		this.diceValue = diceValue;
	}

	@Override
	public void execute(final Game game) {
		int cashRequired = property.getRent(diceValue, override);
		incomingPlayer.payCashToPlayer(cashRequired, property.getOwner(), game);
	}

}
